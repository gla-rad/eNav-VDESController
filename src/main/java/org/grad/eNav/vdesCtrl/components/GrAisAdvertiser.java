/*
 * Copyright (c) 2021 GLA UK Research and Development Directive
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.grad.eNav.vdesCtrl.components;

import lombok.extern.slf4j.Slf4j;
import org.grad.eNav.vdesCtrl.models.domain.*;
import org.grad.eNav.vdesCtrl.models.dtos.S125Node;
import org.grad.eNav.vdesCtrl.services.SNodeService;
import org.grad.eNav.vdesCtrl.utils.GrAisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The GNURadio AIS Advertiser Component Class
 *
 * This component is responsible for scheduling the advertisements published
 * from a GNURadio AIS transmitter. This is initialised by a service like the
 * GrAIsService but after that each GrAisAdvertiser schedules its own
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
    @Value("${gla.rad.vdes-ctrl.gr-aid-advertiser.ais-interval:500}")
    Long aisInterval;

    /**
     * Whether to enable signature messages
     */
    @Value("${gla.rad.vdes-ctrl.gr-aid-advertiser.enableSignatures:false}")
    Boolean enableSignatures;

    /**
     * The sSignature Message Destination MMSI
     */
    @Value("${gla.rad.vdes-ctrl.gr-aid-advertiser.destMmsi:}")
    Integer signatureDestMmmsi;

    /**
     * The SNode Service.
     */
    @Autowired
    SNodeService sNodeService;

    /**
     * A helper class definition to keep info on the Msg21 transmission
     * information.
     */
    private class Msg21TxInfo {
        GrAisMsg21Params params;
        String message21;
        long txTimestamp;
    }

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
     * This is a scheduled task performed by the service. The fixed delay
     * scheduler is used to execute the tasks at a specific time. It should wait
     * for the previous task completion. Since the GNURadio transmission are
     * quite primitive we should control the periodic transmissions manually.
     */
    @Scheduled(fixedDelay = 60000, initialDelay = 1000)
    public void advertiseAtons() throws InterruptedException {
        // Get all the nodes applicable for the station
        List<S125Node> nodes = this.sNodeService.findAllForStationDto(station.getId());

        // Now create the AIS advertisements - wait in between
        for(S125Node node: nodes) {
            // Keep a reference to when the message was send to create a signature for it
            Msg21TxInfo txInfo = this.sendMsg21Datagram(station.getIpAddress(), station.getPort(), node);

            // If signature messages are enabled, send one
            if(this.enableSignatures) {
                this.sendSignatureDatagram(station.getIpAddress(), station.getPort(), txInfo);
            }

            // Wait to give enough time for the AIS TDMA slot
            Thread.sleep(this.aisInterval);
        }
    }

    /**
     * The main function that sends the UDP package to the VDES station. To
     * make the streaming operation easier, we are actually returning the
     * provided message for each successful transmission.
     *
     * @param address the address to send the datagram to
     * @param port the port to send the datagram to
     * @param s125Node the S125 message to be transmitted
     */
    private Msg21TxInfo sendMsg21Datagram(String address, int port, S125Node s125Node) {
        // Sanity check
        if(Objects.isNull(s125Node)) {
            return null;
        }

        // Send the UDP packet
        log.info("Station {} Sending an advertisement AtoN {}", station.getName(), s125Node.getAtonUID());

        // Construct the UDP message for the VDES station
        Msg21TxInfo txinfo = new Msg21TxInfo();
        try {
            txinfo.params = new GrAisMsg21Params(s125Node);
            txinfo.message21 = GrAisUtils.encodeMsg21(txinfo.params);
        } catch (JAXBException ex) {
            log.error(ex.getMessage());
            return null;
        }

        // Create and send the UDP datagram packet
        byte[] buffer = (txinfo.message21+'\n').getBytes();
        try {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length,
                    InetAddress.getByName(address), port);
            this.gnuRadioSocket.send(packet);
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }

        // Note the time of the transmission and return the transmission info
        txinfo.txTimestamp = System.currentTimeMillis()/1000L;
        return txinfo;
    }

    /**
     * This function will generate a signature message for the S125Node combined
     * with the transmission UNIX timestamp and send this as an AIS message 6/8
     * to the GNURadio UDP port as well.
     *
     * @param address the address to send the datagram to
     * @param port the port to send the datagram to
     * @param txInfo the AIS message 21 transmission information
     */
    private void sendSignatureDatagram(String address, int port, Msg21TxInfo txInfo) {
        // Sanity check
        if(Objects.isNull(txInfo)) {
            return;
        }

        // Construct the NMEA sentence of message 21 to be signed
        String msg21NmeaSentence = GrAisUtils.generateNMEASentence(txInfo.message21, true, this.station.getChannel());
        log.debug(String.format("Generating signature for Message 21 NMEA Sentence: %s", msg21NmeaSentence));

        // Construct the UDP message for the VDES station
        String signatureMessage;
        try {
            byte[] signature = GrAisUtils.getNMEASentenceSignature(msg21NmeaSentence, txInfo.txTimestamp);
            signatureMessage = Optional.ofNullable(this.signatureDestMmmsi)
                    .map(destMmsi -> new GrAisMsg6Params(txInfo.params.getMmsi(), destMmsi, signature))
                    .map(GrAisUtils::encodeMsg6)
                    .orElseGet(() -> GrAisUtils.encodeMsg8(new GrAisMsg8Params(txInfo.params.getMmsi(), signature)));
        } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException | InvalidKeyException | SignatureException ex) {
            log.error(ex.getMessage());
            return;
        }

        // Create and send the UDP datagram packet
        byte[] buffer = (signatureMessage+'\n').getBytes();
        try {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length,
                    InetAddress.getByName(address), port);
            this.gnuRadioSocket.send(packet);
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        // Generate some debug information
        String signatureNmeaSentence = GrAisUtils.generateNMEASentence(signatureMessage, true, this.station.getChannel());
        log.debug(String.format("Signature NMEA sentence sent: %s", signatureNmeaSentence));
    }

}
