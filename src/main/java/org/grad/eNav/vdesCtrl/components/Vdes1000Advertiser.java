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
 *
 */

package org.grad.eNav.vdesCtrl.components;

import jakarta.annotation.PreDestroy;
import jakarta.xml.bind.JAXBException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.grad.eNav.vdesCtrl.feign.CKeeperClient;
import org.grad.eNav.vdesCtrl.models.PubSubMsgHeaders;
import org.grad.eNav.vdesCtrl.models.domain.McpEntityType;
import org.grad.eNav.vdesCtrl.models.domain.Station;
import org.grad.eNav.vdesCtrl.models.dtos.S125Node;
import org.grad.eNav.vdesCtrl.services.StationService;
import org.grad.eNav.vdesCtrl.utils.AISMessageUtils;
import org.grad.vdes1000.ais.messages.AISMessage21;
import org.grad.vdes1000.ais.messages.AISMessage6;
import org.grad.vdes1000.ais.messages.AISMessage8;
import org.grad.vdes1000.comm.VDES1000Conn;
import org.grad.vdes1000.comm.VDESBroadcastMethod;
import org.grad.vdes1000.exceptions.VDES1000ConnException;
import org.grad.vdes1000.generic.AbstractMessage;
import org.grad.vdes1000.utils.GrAisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.*;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The VDES-1000 Advertiser Component.
 *
 * This component is responsible for scheduling the advertisements published
 * towards a VDES-1000 transmitter. This is initialised by a service like the
 * VDES1000Service but after that each Vdes1000Advertiser schedules its own
 * operation.
 */
@Slf4j
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Component
public class Vdes1000Advertiser {

    /**
     * Whether to enable signature messages
     */
    @Value("${gla.rad.vdes-ctrl.vdes-1000-advertiser.enableSignatures:false}")
    Boolean enableSignatures;

    /**
     * The sSignature Message Destination MMSI
     */
    @Value("${gla.rad.vdes-ctrl.vdes-1000-advertiser.destMmsi:}")
    Integer signatureDestMmmsi;

    /**
     * The Publish-Subscribe Channel to publish the incoming data to.
     */
    @Autowired
    @Qualifier("publishSubscribeChannel")
    PublishSubscribeChannel publishSubscribeChannel;

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
    protected VDES1000Conn vdes1000Conn;
    protected DatagramSocket fwdSocket;

    /**
     * Once the advertiser is initialised it will have all the information
     * required for schedule the AtoN advertisements transmitted through the
     * GNURadio stations.
     *
     * @param station the station to send the advertisements from
     */
    public void init(Station station) throws SocketException, UnknownHostException {
        this.station = station;

        // Create the VDES-1000 Connection
        this.setVdes1000Conn(new VDES1000Conn(VDESBroadcastMethod.TSA_VDM,
                String.format("%04d", this.station.getId()),
                InetAddress.getByName(this.station.getIpAddress()),
                this.station.getPort(),
                this.station.getBroadcastPort()));

        // Add logging capability to the VDES-1000 connection
        this.getVdes1000Conn().setLogger(this.log);

        // Enable the connection monitoring
        this.getVdes1000Conn().addVdesListener(this::handleMessage);
        this.getVdes1000Conn().startMonitoring();

        // If we also have a forward address, open a forward port
        if(Objects.nonNull(this.station.getFwdIpAddress()) && Objects.nonNull(this.station.getFwdPort())) {
            this.setFwdSocket(new DatagramSocket());
        }
    }

    /**
     * When shutting down the application we need to make sure that the
     * VDES-1000 connection has been shut down.
     */
    @PreDestroy
    public void destroy() {
        log.info("VDES-1000 Advertiser is shutting down...");
        // Try to close the connections and don't worry about the interrupts
        try {
            this.getVdes1000Conn().close();
        } catch (InterruptedException ex) {
            this.log.error(ex.getMessage());
        }
    }

    /**
     * This is the actual advertising task that is periodically called by the
     * service. A fixed delay scheduler is used to execute the tasks at a
     * specific time. Since the VDES-1000 basic TSA-VDM and DDM operations are
     * used we should control the periodic transmissions manually.
     */
    @Async("taskExecutor")
    public void advertiseAtons() {
        // Get all the nodes applicable for the station and build the messages
        List<AISMessage21> messages = Optional.of(this.station)
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
                    catch (JAXBException ex) {
                        log.error(ex.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(AISMessage21::getVaton) // Only transmit Virtual AtoNs
                .collect(Collectors.toList());

        // Now create the AIS advertisements - wait in between
        try {
            for (AISMessage21 message : messages) {
                // First send the message right away and then check if to create a signature for it
                log.info("Station {} sending an advertisement AtoN {}", station.getName(), message.getUid());
                this.getVdes1000Conn().sendMessage(message, this.station.getChannel());

                // If signature messages are enabled, send one
                if (this.enableSignatures) {
                    // Get the signature for the message sent
                    AbstractMessage signature = this.getSignature(message);

                    // If we have a signature and it's valid
                    if (Objects.nonNull(signature)) {
                        this.getVdes1000Conn().sendMessageWithBBM(signature, this.station.getChannel());
                    }
                }
            }
        } catch (VDES1000ConnException ex) {
            this.log.error(ex.getMessage());
        }
    }

    /**
     * This function will generate a signature message for the AIS Message 21
     * combined with the transmission UNIX timestamp and send this as an AIS
     * Message 6/8 to the VDES-1000 UDP port as well.
     *
     * @param aisMessage21 the AIS message 21 that was transmitted
     */
    private AbstractMessage getSignature(AISMessage21 aisMessage21) {
        // Sanity check
        if(Objects.isNull(aisMessage21)) {
            return null;
        }

        // Construct the NMEA sentence of message 21 to be signed
        log.debug(String.format("Generating signature for Message 21 NMEA Sentence: %s", new String(aisMessage21.getBinaryMessage(true))));

        // Construct the signature message for the VDES station
        final AbstractMessage abstractMessage;
        try {
            // Combine the AIS message and the timestamp into a hash
            log.debug(String.format("Stamping AIS message with timestamp %d", aisMessage21.getUnixTxTimestamp(0)));
            byte[] stampedAisMessage = GrAisUtils.getStampedAISMessage(aisMessage21.getBinaryMessage(false), /*aisMessage21.getUnixTxTimestamp(0)*/0L);

            // Get the signature
            byte[] signature = this.cKeeperClient.generateEntitySignature(aisMessage21.getUid(), String.valueOf(aisMessage21.getMmsi()), McpEntityType.DEVICE.getValue(), stampedAisMessage);
            log.debug(String.format("Signature sentence generated: %s", Hex.encodeHexString(signature)));

            // And generate the signature message
            abstractMessage = Optional.ofNullable(this.signatureDestMmmsi)
                    .map(destMmsi -> (AbstractMessage) new AISMessage6(aisMessage21.getMmsi(), destMmsi, signature))
                    .orElseGet(() -> (AbstractMessage) new AISMessage8(aisMessage21.getMmsi(), signature));
        } catch (IOException ex) {
            log.error(ex.getMessage());
            return null;
        }

        // Return the constructed message
        return abstractMessage;
    }

    /**
     * Handle the received message.
     *
     * Messages coming from a VDES-1000 station are always string, such as NMEA
     * sentences.
     *
     * @param message   The message to be handled.
     */
    public void handleMessage(String message) {
        // Send the packet to our pub-sub messaging bus
        this.publishSubscribeChannel.send(MessageBuilder.withPayload(message)
                .setHeader(MessageHeaders.CONTENT_TYPE, this.station.getType())
                .setHeader(PubSubMsgHeaders.ADDRESS.getHeader(), this.station.getIpAddress())
                .setHeader(PubSubMsgHeaders.PORT.getHeader(), this.station.getBroadcastPort())
                .setHeader(PubSubMsgHeaders.MMSI.getHeader(), this.station.getMmsi())
                .build());

        // If we also have a forward address, send it there too
        if(Objects.nonNull(this.getFwdSocket())) {
            try {
                this.getFwdSocket().send(new DatagramPacket(
                        message.getBytes(),
                        message.length(),
                        Optional.of(this.station)
                                .map(Station::getFwdIpAddress)
                                .map(ip -> { try { return InetAddress.getByName(ip); } catch (UnknownHostException ex) { return null; } })
                                .orElse(null),
                        Optional.of(this.station)
                                .map(Station::getFwdPort)
                                .orElse(-1)
                ));
            } catch (IOException ex) {
                this.log.error(ex.getMessage());
            }
        }
    }

    /**
     * Gets the VDES-1000 connection.
     *
     * @return the VDES-1000 connection
     */
    protected VDES1000Conn getVdes1000Conn() {
        return vdes1000Conn;
    }

    /**
     * Sets the VDES-1000 connection.
     *
     * @param vdes1000Conn the VDES-1000 connection
     */
    protected void setVdes1000Conn(VDES1000Conn vdes1000Conn) {
        this.vdes1000Conn = vdes1000Conn;
    }

    /**
     * Gets the forward port socket.
     *
     * @return the forward port socket
     */
    protected DatagramSocket getFwdSocket() {
        return fwdSocket;
    }

    /**
     * Sets the forward port socket.
     *
     * @param fwdSocket the forward port socket
     */
    protected void setFwdSocket(DatagramSocket fwdSocket) {
        this.fwdSocket = fwdSocket;
    }
}
