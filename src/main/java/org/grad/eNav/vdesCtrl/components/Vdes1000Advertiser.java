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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.grad.eNav.vdesCtrl.feign.CKeeperClient;
import org.grad.eNav.vdesCtrl.models.domain.Station;
import org.grad.eNav.vdesCtrl.services.SNodeService;
import org.grad.eNav.vdesCtrl.utils.S100Utils;
import org.grad.vdes1000.ais.messages.AISMessage21;
import org.grad.vdes1000.ais.messages.AISMessage6;
import org.grad.vdes1000.ais.messages.AISMessage8;
import org.grad.vdes1000.comm.VDES1000Conn;
import org.grad.vdes1000.comm.VDESBroadcastMethod;
import org.grad.vdes1000.exceptions.VDES1000ConnException;
import org.grad.vdes1000.generic.AbstractMessage;
import org.grad.vdes1000.utils.GrAisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
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
     * The CKeeper Client
     */
    @Autowired
    CKeeperClient cKeeperClient;

    /**
     * The SNode Service.
     */
    @Autowired
    SNodeService sNodeService;

    // Component Variables
    protected Station station;
    protected VDES1000Conn vdes1000Conn;

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
        this.vdes1000Conn = new VDES1000Conn(VDESBroadcastMethod.TSA_VDM,
                "AI"+String.format("%04d", this.station.getId()),
                InetAddress.getByName(this.station.getIpAddress()),
                this.station.getPort());

        // Add logging capability to the VDES-1000 connection
        this.vdes1000Conn.setLogger(this.log);
    }

    /**
     * When shutting down the application we need to make sure that the
     * VDES-1000 connection has been shut down.
     */
    @PreDestroy
    public void destroy() {
        log.info("VDES-1000 Advertiser is shutting down...");
        this.vdes1000Conn.close();
    }

    /**
     * This is a scheduled task performed by the service. The fixed delay
     * scheduler is used to execute the tasks at a specific time. It should wait
     * for the previous task completion. Since the VDES-1000 basic operation
     * is used, we should control the periodic transmissions manually.
     */
    @Scheduled(fixedDelay = 60000, initialDelay = 1000)
    public void advertiseAtons() throws VDES1000ConnException {
        // Get all the nodes applicable for the station and build the messages
        List<AISMessage21> messages = this.sNodeService.findAllForStationDto(station.getId())
                .stream()
                .filter(Objects::nonNull)
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
        for(AISMessage21 message: messages) {
            // First send the message right away and then check if to create a signature for it
            log.info("Station {} sending an advertisement AtoN {}", station.getName(), message.getUid());
            this.vdes1000Conn.sendMessage(message, this.station.getChannel());

            // If signature messages are enabled, send one
            if(this.enableSignatures) {
                // Get the signature for the sent message
                AbstractMessage signature = this.getSignature(message);

                // If we have a signature and it's valid
                // [48, 48, 53, 112, 64, 81, 96, 100, 110, 83, 96, 83, 108, 87, 76, 58, 87, 63, 67, 116, 58, 48, 82, 101, 54, 97, 107, 98, 111, 109, 77, 105, 108, 78, 72, 107, 72, 77, 74, 106, 68, 116, 119, 64, 103, 108, 103, 61, 117, 56, 103, 102, 55, 83, 81, 54, 67]
                if(Objects.nonNull(signature)) {
                    this.vdes1000Conn.sendMessageWithBBM(signature, this.station.getChannel());
                }
            }
        }
    }

    /**
     * This function will generate a signature message for the S125Node combined
     * with the transmission UNIX timestamp and send this as an AIS message 6/8
     * to the VDES-1000 UDP port as well.
     *
     * @param aisMessage21 the AIS message 21 that was transmitted
     */
    private AbstractMessage getSignature(AISMessage21 aisMessage21) {
        // Sanity check
        if(Objects.isNull(aisMessage21)) {
            return null;
        }

        // Construct the signature message for the VDES station
        final AbstractMessage abstractMessage;
        try {
            // Combine the AIS message and the timestamp into a hash
            byte[] stampedAisMessage = GrAisUtils.getStampedAISMessageHash(aisMessage21.getBinaryMessage(false), aisMessage21.getUnixTxTimestamp(0));

            // Get the signature
            byte[] signature = this.cKeeperClient.generateAtoNSignature(aisMessage21.getUid(), String.valueOf(aisMessage21.getMmsi()), stampedAisMessage);
            log.debug(String.format("Signature sentence generated: %s", Hex.encodeHexString(signature)));

            // And generate the signature message
            abstractMessage = Optional.ofNullable(this.signatureDestMmmsi)
                    .map(destMmsi -> (AbstractMessage) new AISMessage6(aisMessage21.getMmsi(), destMmsi, signature))
                    .orElseGet(() -> (AbstractMessage) new AISMessage8(aisMessage21.getMmsi(), signature));
        } catch (NoSuchAlgorithmException | IOException ex) {
            log.error(ex.getMessage());
            return null;
        }

        // Return the constructed message
        return abstractMessage;
    }

}
