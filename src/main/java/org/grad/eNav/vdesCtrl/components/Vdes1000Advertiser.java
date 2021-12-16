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
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.grad.eNav.vdesCtrl.feign.CKeeperClient;
import org.grad.eNav.vdesCtrl.models.domain.Station;
import org.grad.eNav.vdesCtrl.models.vdes.AbstractMessage;
import org.grad.eNav.vdesCtrl.models.vdes.AbstractSentence;
import org.grad.eNav.vdesCtrl.models.vdes.ais.messages.AISMessage21;
import org.grad.eNav.vdesCtrl.models.vdes.ais.messages.AISMessage6;
import org.grad.eNav.vdesCtrl.models.vdes.ais.messages.AISMessage8;
import org.grad.eNav.vdesCtrl.models.vdes.ais.sentences.TSASentence;
import org.grad.eNav.vdesCtrl.models.vdes.ais.sentences.TSASentenceBuilder;
import org.grad.eNav.vdesCtrl.models.vdes.comm.VDES1000Conn;
import org.grad.eNav.vdesCtrl.models.vdes.comm.VDESBroadcastMethod;
import org.grad.eNav.vdesCtrl.models.vdes.iec61162_450.IEC61162_450Message;
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
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public void init(Station station) throws SocketException {
        this.station = station;

        // Create the VDES-1000 Connection
        this.vdes1000Conn = new VDES1000Conn(VDESBroadcastMethod.TSA_VDM,
                "AI"+String.format("%04d", this.station.getId()),
                this.station.getIpAddress(),
                this.station.getPort());

        // Add the Bouncy castle as a security provider to make signatures
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * When shutting down the application we need to make sure that all
     * threads have been gracefully shutdown as well.
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
    public void advertiseAtons() throws InterruptedException {
        // Get all the nodes applicable for the station and build the messages
        List<AISMessage21> messages = this.sNodeService.findAllForStationDto(station.getId())
                .stream()
                .filter(Objects::nonNull)
                .map(s125 -> {
                    try {
                        return new AISMessage21(s125);
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
            this.vdes1000Conn.sendMessage(message, this.station.getChannel());
            log.info("Station {} sending an advertisement AtoN {}", station.getName(), message.getUid());

            // If signature messages are enabled, send one
            if(this.enableSignatures) {
                Optional.of(message)
                        .map(this::getSignature)
                        .ifPresent(signature -> {
                            this.vdes1000Conn.sendMessage(signature, this.station.getChannel());
                            log.debug(String.format("Signature NMEA sentence sent: %s", new String(signature.getBinaryMessage(true))));
                        });
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
