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
import org.grad.eNav.vdesCtrl.models.domain.GrAisMsg21Params;
import org.grad.eNav.vdesCtrl.models.domain.Station;
import org.grad.eNav.vdesCtrl.models.dtos.S125Node;
import org.grad.eNav.vdesCtrl.services.SNodeService;
import org.grad.eNav.vdesCtrl.utils.GrAisUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.List;

/**
 * The GNURadio AIS Advertiser Component
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

    // Component Constants
    public static final Long AIS_INTERVAL = 26L;

    // Component Variables
    private Station station;
    private DatagramSocket vdesSocket;

    /**
     * The SNode Service.
     */
    @Autowired
    private SNodeService sNodeService;


    /**
     * Once the advertiser is initialised it will have all the information
     * required for schedule the AtoN advertisements transmitted through the
     * GNURadio stations.
     *
     * @param station the station to send the advertisements from
     */
    public void init(Station station) throws SocketException {
        this.station = station;

        // Create the UDP Connection to the VDES stations
        this.vdesSocket = new DatagramSocket();
    }

    /**
     * When shutting down the application we need to make sure that all
     * threads have been gracefully shutdown as well.
     */
    @PreDestroy
    public void destroy() {
        log.info("GNURadio Advertiser is shutting down...");
        this.vdesSocket.close();
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
            // Send the UDP packet
            log.info("Station {} Sending an advertisement AtoN {}", station.getName(), node.getAtonUID());
            this.sendDatagram(station.getIpAddress(), station.getPort(), node);

            // Wait to give enough time for the AIS TDMA slot
            Thread.sleep(AIS_INTERVAL);
        }
    }

    /**
     * The main function that sends the UDP package to the VDES station. To
     * make the streaming operation easier, we are actually returning the
     * provided message for each successful transmission.
     *
     * @param address       The address to send the datagram to
     * @param port          The port to send the datagram to
     * @param s125Node       The S125 message to be transmitted
     */
    private void sendDatagram(String address, int port, S125Node s125Node) {
        // Construct the UDP message for the VDES station
        byte[] buffer;
        try {
            buffer = GrAisUtils.encodeMsg21(new GrAisMsg21Params(s125Node)).getBytes();
        } catch (JAXBException ex) {
            log.error(ex.getMessage());
            return;
        }

        // Create and send the UDP datagram packet
        try {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length,
                    InetAddress.getByName(address), port);
            this.vdesSocket.send(packet);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

}
