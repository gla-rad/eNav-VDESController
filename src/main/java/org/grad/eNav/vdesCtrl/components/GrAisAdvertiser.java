/*
 * Copyright (c) 2021 GLA Research and Development Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.grad.eNav.vdesCtrl.components;

import lombok.extern.slf4j.Slf4j;
import org.grad.eNav.vdesCtrl.feign.CKeeperClient;
import org.grad.eNav.vdesCtrl.models.domain.Station;
import org.grad.eNav.vdesCtrl.models.dtos.AtonMessageDto;
import org.grad.eNav.vdesCtrl.models.dtos.S125Node;
import org.grad.eNav.vdesCtrl.services.StationService;
import org.grad.eNav.vdesCtrl.utils.S100Utils;
import org.grad.vdes1000.ais.messages.AISMessage21;
import org.grad.vdes1000.ais.messages.AISMessage6;
import org.grad.vdes1000.ais.messages.AISMessage8;
import org.grad.vdes1000.generic.AbstractMessage;
import org.grad.vdes1000.utils.GrAisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

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
     * Whether to enable signature messages
     */
    @Value("${gla.rad.vdes-ctrl.gr-ais-advertiser.enableSignatures:false}")
    Boolean enableSignatures;

    /**
     * The sSignature Message Destination MMSI
     */
    @Value("${gla.rad.vdes-ctrl.gr-ais-advertiser.destMmsi:}")
    Integer signatureDestMmmsi;

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
                .map(this.stationService::findMessagesForStation)
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .filter(not(AtonMessageDto::isBlacklisted))
                .filter(S125Node.class::isInstance)
                .map(S125Node.class::cast)
                .map(s125 -> {
                    try {
                        return S100Utils.s125ToAisMessage21(s125);
                    }
                    catch (JAXBException ex) {
                        log.error(ex.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Now create the AIS advertisements - wait in between
        try {
            for (AISMessage21 message : messages) {
                // First send the message right away and then check if to create a signature for it
                this.sendMsg21Datagram(station.getIpAddress(), station.getPort(), message);

                // If signature messages are enabled, send one
                if (this.enableSignatures) {
                    this.sendSignatureDatagram(station.getIpAddress(), station.getPort(), message);
                }

                // Wait to give enough time for the AIS TDMA slot
                Thread.sleep(this.aisInterval);
            }
        } catch (InterruptedException ex) {
            this.log.error(ex.getMessage());
        }
    }

    /**
     * The main function that sends the UDP package to the GNURadio station.
     *
     * @param address the address to send the datagram to
     * @param port the port to send the datagram to
     * @param aisMessage21 the AIS message 21 to be transmitted
     */
    private void sendMsg21Datagram(String address, int port, AISMessage21 aisMessage21) {
        // Sanity check
        if(Objects.isNull(aisMessage21)) {
            return;
        }

        // Log the operation
        log.info("Station {} sending an advertisement AtoN {}", station.getName(), aisMessage21.getUid());

        // Create and send the UDP datagram packet
        byte[] buffer = (aisMessage21.getBinaryMessageString() +'\n').getBytes();
        try {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length,
                    InetAddress.getByName(address), port);
            this.gnuRadioSocket.send(packet);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * This function will generate a signature message for the S125Node combined
     * with the transmission UNIX timestamp and send this as an AIS message 6/8
     * to the GNURadio UDP port as well.
     *
     * @param address the address to send the datagram to
     * @param port the port to send the datagram to
     * @param aisMessage21 the AIS message 21 that was transmitted
     */
    private void sendSignatureDatagram(String address, int port, AISMessage21 aisMessage21) {
        // Sanity check
        if(Objects.isNull(aisMessage21)) {
            return;
        }

        // Construct the NMEA sentence of message 21 to be signed
        log.debug(String.format("Generating signature for Message 21 NMEA Sentence: %s", new String(aisMessage21.getBinaryMessage(true))));

        // Construct the UDP message for the VDES station
        final AbstractMessage abstractMessage;
        try {
            // Combine the AIS message and the timestamp into a hash
            byte[] stampedAisMessage = GrAisUtils.getStampedAISMessageHash(aisMessage21.getBinaryMessage(false), aisMessage21.getUnixTxTimestamp(0));

            // Get the signature
            byte[] signature = this.cKeeperClient.generateAtoNSignature(aisMessage21.getUid(), String.valueOf(aisMessage21.getMmsi()), stampedAisMessage);

            // And generate the signature message
            abstractMessage = Optional.ofNullable(this.signatureDestMmmsi)
                    .map(destMmsi -> (AbstractMessage) new AISMessage6(aisMessage21.getMmsi(), destMmsi, signature))
                    .orElseGet(() -> (AbstractMessage) new AISMessage8(aisMessage21.getMmsi(), signature));
        } catch (NoSuchAlgorithmException | IOException ex) {
            log.error(ex.getMessage());
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

        // Generate some debug information
        log.debug(String.format("Signature NMEA sentence sent: %s", new String(abstractMessage.getBinaryMessage(true))));
    }

}
