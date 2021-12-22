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
import org.grad.eNav.vdesCtrl.models.PubSubMsgHeaders;
import org.grad.eNav.vdesCtrl.models.domain.Station;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.*;
import java.util.Objects;

/**
 * The VDES-1000 Listener Component.
 *
 * This component listens to the output of a VDES-1000 station and forwards the
 * incoming data messages to the local web-socket stream, while it is also
 * able to forward them to a specified service port, such as for OpenCPN etc,
 * for additional processing.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Slf4j
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Component
public class Vdes1000Listener {

    /**
     * The AtoN Data Channel to publish the incoming data to.
     */
    @Autowired
    @Qualifier("atonPublishChannel")
    PublishSubscribeChannel atonPublishChannel;

    // Define the UDP packet buffer size
    private static final int UDP_BUFFER_SIZE = 2048;

    // Component Variables
    protected Station station;
    protected DatagramSocket vdes1000Socket;
    protected DatagramSocket fwdSocket;

    /**
     * Once the advertiser is initialised it will have all the information
     * required for schedule the AtoN advertisements transmitted through the
     * GNURadio stations.
     *
     * @param station the station to send the advertisements from
     */
    public void init(Station station) throws UnknownHostException, SocketException {
        // Copy the station information
        this.station = station;

        // Open the datagram socket to be monitored
        this.vdes1000Socket = new DatagramSocket(this.station.getBroadcastPort());

        // If we also have a forward address, open a forward port
        if(Objects.nonNull(this.station.getFwdIpAddress()) && Objects.nonNull(this.station.getFwdIpAddress())) {
            fwdSocket = new DatagramSocket();
        }
    }

    /**
     * When shutting down the application we need to make sure that the
     * VDES-1000 connection has been shut down.
     */
    @PreDestroy
    public void destroy() {
        log.info("VDES-1000 Advertiser is shutting down...");
        this.vdes1000Socket.close();
    }

    /**
     * Define an asynchronous task which continuously monitors the VDES-1000
     * output port and collects the incoming packets.
     */
    @Async("taskExecutor")
    public void listen() {
        while(!this.vdes1000Socket.isClosed()) {
            DatagramPacket receivePacket = new DatagramPacket(new byte[UDP_BUFFER_SIZE], UDP_BUFFER_SIZE);
            try {
                // Block while receiving a packet
                this.vdes1000Socket.receive(receivePacket);

                // Get the incoming message as a string
                String payload = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());

                // Log a debug message
                this.log.debug("Packet received: " + new String(receivePacket.getData()));

                // Send the packet to our pub-sub messaging bus
                Message<String> msg = MessageBuilder.withPayload(payload)
                        .setHeader(MessageHeaders.CONTENT_TYPE, this.station.getType())
                        .setHeader(PubSubMsgHeaders.ADDRESS.getHeader(), this.station.getIpAddress())
                        .setHeader(PubSubMsgHeaders.PORT.getHeader(), this.station.getBroadcastPort())
                        .setHeader(PubSubMsgHeaders.MMSI.getHeader(), this.station.getMmsi())
                        .build();
                this.atonPublishChannel.send(msg);

                // If we also have a forward address, send it there too
                if(Objects.nonNull(this.fwdSocket)) {
                    DatagramPacket packet = new DatagramPacket(
                            payload.getBytes(),
                            payload.length(),
                            InetAddress.getByName(this.station.getFwdIpAddress()),
                            this.station.getFwdPort());
                    this.fwdSocket.send(packet);
                }
            } catch (IOException ex) {
                this.log.error(ex.getMessage());
            }
        }

        //  Not that it matters
        this.log.info("The VDES-1000 listener connection is now closed...");
    }

}
