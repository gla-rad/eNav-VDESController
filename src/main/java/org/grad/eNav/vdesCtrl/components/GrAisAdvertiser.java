/*
 * Copyright (c) 2024 GLA Research and Development Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.grad.eNav.vdesCtrl.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.PreDestroy;
import jakarta.xml.bind.JAXBException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.grad.eNav.vdesCtrl.exceptions.ValidationException;
import org.grad.eNav.vdesCtrl.feign.CKeeperClient;
import org.grad.eNav.vdesCtrl.models.domain.McpEntityType;
import org.grad.eNav.vdesCtrl.models.domain.SignatureMode;
import org.grad.eNav.vdesCtrl.models.domain.Station;
import org.grad.eNav.vdesCtrl.models.dtos.S125Node;
import org.grad.eNav.vdesCtrl.services.StationService;
import org.grad.eNav.vdesCtrl.utils.AISMessageUtils;
import org.grad.vdes1000.formats.ais.messages.AISMessage21;
import org.grad.vdes1000.formats.ais.messages.AISMessage6;
import org.grad.vdes1000.formats.ais.messages.AISMessage8;
import org.grad.vdes1000.formats.generic.AbstractMessage;
import org.grad.vdes1000.utils.GrAisUtils;
import org.grad.vdes1000.utils.StringBinUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The GNURadio AIS Advertiser Component Class
 *
 * This component is responsible for scheduling the advertisements published
 * towards a GNURadio AIS transmitter. This is initialised by a service like
 * the GrAIsService but after that each GrAisAdvertiser schedules its own
 * operation.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Slf4j
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Component
public class GrAisAdvertiser {

    /**
     * The interval between the node advertisements
     */
    @Value("${gla.rad.vdes-ctrl.gr-ais-advertiser.ais-interval:500}")
    Long aisInterval;

    /**
     * The sSignature Message Destination MMSI
     */
    @Value("${gla.rad.vdes-ctrl.gr-ais-advertiser.destMmsi:}")
    Integer signatureDestMmmsi;

    /**
     * The Signature Algorithm.
     */
    @Value("${gla.rad.vdes-ctrl.gr-ais-advertiser.algorithm:SHA256withCVC-ECDSA}")
    String signatureAlgorithm;

    /**
     * The CKeeper Client
     */
    @Autowired
    CKeeperClient cKeeperClient;

    /**
     * The Station Service.
     */
    @Autowired
    StationService stationService;

    // Component Variables
    protected Station station;
    protected DatagramSocket gnuRadioSocket;

    /**
     * Once the advertiser is initialised it will have all the information
     * required for schedule the AtoN advertisements transmitted through the
     * GNURadio stations.
     *
     * @param station the station to send the advertisements from
     */
    public void init(Station station) throws SocketException {
        this.station = station;

        // Create the UDP Connection to the GNURadio stations
        this.gnuRadioSocket = new DatagramSocket();
    }

    /**
     * When shutting down the application we need to make sure that all
     * threads have been gracefully shutdown as well.
     */
    @PreDestroy
    public void destroy() {
        log.info("GNURadio Advertiser is shutting down...");
        this.gnuRadioSocket.close();
    }

    /**
     * This is the actual advertising task that is periodically called by the
     * service. A fixed delay scheduler is used to execute the tasks at a
     * specific time. Since the GNURadio transmission are quite primitive we
     * should control the periodic transmissions manually.
     */
    @Async("taskExecutor")
    public void advertiseAtons() {
        // Get all the nodes applicable for the station and build the messages
        List<AISMessage21> messages =  Optional.of(this.station)
                .map(Station::getId)
                .map(id -> this.stationService.findMessagesForStation(id, false))
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .filter(S125Node.class::isInstance)
                .map(S125Node.class::cast)
                .map(s125 -> {
                    try {
                        return AISMessageUtils.s125ToAisMessage21(s125);
                    }
                    catch (JsonProcessingException ex) {
                        log.error(ex.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(AISMessage21::getVaton) // Only transmit Virtual AtoNs
                .toList();

        // Get the signature mode for this station - NONE by default
        final SignatureMode stationSignatureMode = Optional.ofNullable(station.getSignatureMode())
                .orElse(SignatureMode.NONE);

        // Now create the AIS advertisements - wait in between
        try {
            for (AISMessage21 message : messages) {
                // First send the message right away and then check if to create a signature for it
                log.info("Station {} sending an advertisement AtoN {}", station.getName(), message.getUid());
                this.sendDatagram(station.getIpAddress(), station.getPort(), message);

                // If signature messages are enabled, send one
                if (stationSignatureMode != SignatureMode.NONE) {
                    // Get the signature for the message sent
                    final byte[] signature = this.getSignature(message);

                    // If we have a signature and it's valid
                    if (Objects.nonNull(signature)) {
                        switch(stationSignatureMode) {
                            case SignatureMode.AIS -> {
                                // In AIS add the 2 least significant bytes of
                                // the timestamp in the signature. These should
                                // be added in the least significant part of
                                // the signature in a bit endian manner.
                                final byte[] signatureWithTimestamp = ByteBuffer.allocate(signature.length + 2)
                                        .put(signature)
                                        .putShort((short) (message.getUnixTxTimestamp() & 0xFFFFL))
                                        .order(ByteOrder.BIG_ENDIAN)
                                        .array();
                                // Now construct the message
                                final var msg = Optional.ofNullable(this.signatureDestMmmsi)
                                        .map(destMmsi -> (AbstractMessage) new AISMessage6(message.getMmsi(), destMmsi, signatureWithTimestamp))
                                        .orElseGet(() -> (AbstractMessage) new AISMessage8(message.getMmsi(), signatureWithTimestamp));
                                // And send as a UDP packet
                                this.sendDatagram(station.getIpAddress(), station.getPort(), msg);
                            }
                            default -> throw new ValidationException("Only the AIS signature transmission mode is supported for GNU_Radio stations");
                        }

                        // Also log the signature transmission
                        log.debug(String.format("Message signature sent: %s", new String(StringBinUtils.convertBytes(signature, true))));
                        log.debug(String.format("Message signature timestamp: %d", message.getUnixTxTimestamp()));
                    }
                }

                // Wait to give enough time for the AIS TDMA slot
                Thread.sleep(this.aisInterval);
            }
        } catch (ValidationException | InterruptedException ex) {
            log.error(ex.getMessage());
        }
    }

    /**
     * This function will generate a signature byte-array for the AIS Message 21
     * combined with the transmission UNIX timestamp. The output will then get
     * packaged into a generic message (AIS-8, AIS-8, VDE) depending on
     * the operation requirements and returned back.
     *
     * @param aisMessage21 the generated signature
     */
    private byte[] getSignature(AISMessage21 aisMessage21) {
        // Sanity check
        if(Objects.isNull(aisMessage21)) {
            return null;
        }

        // Construct the NMEA sentence of message 21 to be signed
        log.debug(String.format("Generating signature for Message 21 NMEA Sentence: %s", new String(aisMessage21.getBinaryMessage(true))));

        // Construct the UDP message for the VDES station
        final AbstractMessage abstractMessage;
        final byte[] signature;
        try {
            // Combine the AIS message and the timestamp into a hash
            log.debug(String.format("Stamping AIS message with timestamp %d", aisMessage21.getUnixTxTimestamp()));
            byte[] stampedAisMessage = GrAisUtils.getStampedAISMessage(aisMessage21.getBinaryMessage(false), aisMessage21.getUnixTxTimestamp());

            // Get the signature
            signature = this.cKeeperClient.generateEntitySignature(
                    aisMessage21.getUid(),
                    Optional.of(aisMessage21).map(AISMessage21::getMmsi).map(String::valueOf).orElse("0"),
                    this.signatureAlgorithm,
                    McpEntityType.DEVICE.getValue(),
                    stampedAisMessage);
            log.debug(String.format("Signature sentence generated: %s", Hex.encodeHexString(signature)));
        } catch (IOException ex) {
            log.error(ex.getMessage());
            return null;
        }

        // Return the constructed message
        return signature;
    }

    /**
     * Sends the provided message as a UDP datagram to the specified address and
     * port. Any exceptions will be handled internally and an error message will
     * be logged.
     *
     * @param address the IP address to send the UDP package to
     * @param port the UDP port number to direct the package to
     * @param abstractMessage the message to be sent
     */
    private void sendDatagram(String address, int port, AbstractMessage abstractMessage) {
        // Sanity check
        if(Objects.isNull(abstractMessage)) {
            return;
        }

        // Create and send the UDP datagram packet
        byte[] buffer = (abstractMessage.getBinaryMessageString() + '\n').getBytes();
        try {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length,
                    InetAddress.getByName(address), port);
            this.gnuRadioSocket.send(packet);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

}
