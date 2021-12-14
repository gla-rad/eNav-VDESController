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

package org.grad.eNav.vdesCtrl.services;

import lombok.extern.slf4j.Slf4j;
import org.grad.eNav.vdesCtrl.components.GrAisAdvertiser;
import org.grad.eNav.vdesCtrl.components.Vdes1000Advertiser;
import org.grad.eNav.vdesCtrl.models.domain.StationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The VDES-1000 Service Class
 *
 * This class implements a handler for the AtoN messages coming into a Spring
 * Integration channel. It will then translate the content using JAXB and
 * generate the UDP sentences to be sent down to the VDES-1000 stations.
 *
 * @author Nikolaos Vastardis
 */
@Service
@Slf4j
public class VDES1000Service {

    /**
     * The Application Context
     */
    @Autowired
    ApplicationContext applicationContext;

    /**
     * The Station Service.
     */
    @Autowired
    StationService stationService;

    // Service Variables
    protected List<Vdes1000Advertiser> vdes1000Advertisers;

    /**
     * The service post-construct operations where the handler auto-registers
     * it-self to the aton publication channel. Once successful, it will then
     * monitor the channel for all inputs coming through the REST API.
     */
    @PostConstruct
    public void init() {
        log.info("VDES-1000 Service is booting up...");

        // Initialise the GNURadio AIS Advertisers, one per each station
        this.vdes1000Advertisers = Optional.of(StationType.VDES_1000)
                .map(this.stationService::findAllByType)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(station -> {
                    Vdes1000Advertiser vdes1000Advertiser = this.applicationContext.getBean(Vdes1000Advertiser.class);
                    try {
                        vdes1000Advertiser.init(station);
                    } catch (SocketException ex) {
                        log.error(ex.getMessage());
                        return null;
                    }
                    return vdes1000Advertiser;
                })
                .collect(Collectors.toList());
    }

    /**
     * When shutting down the application we need to make sure that all
     * threads have been gracefully shutdown as well.
     */
    @PreDestroy
    public void destroy() {
        this.vdes1000Advertisers.forEach(Vdes1000Advertiser::destroy);
    }

//    /**
//     * This is a simple handler for the incoming messages. This is a generic
//     * handler for any type of Spring Integration messages but it should really
//     * only be used for the ones containing S-125 message payloads.
//     *
//     * @param message               The message to be handled
//     * @throws MessagingException   The Messaging exceptions that might occur
//     */
//    @Override
//    public void handleMessage(Message<?> message) throws MessagingException {
//        // Check that we only listen to VDES-1000 Content
//        StationType contentType = (StationType) message.getHeaders().get(MessageHeaders.CONTENT_TYPE);
//        if(contentType != StationType.VDES_1000) {
//            return;
//        }
//
//        // Check that this seems ot be a valid message
//        if(!(message.getPayload() instanceof S125Node)) {
//            log.warn("Radar message handler received a message with erroneous format.");
//            return;
//        }
//
//        // Get the header and payload of the incoming message
//        String address = (String) message.getHeaders().get(PubSubMsgHeaders.ADDRESS.getHeader());
//        Integer port = (Integer) message.getHeaders().get(PubSubMsgHeaders.PORT.getHeader());
//        Long piseqno = (Long) message.getHeaders().get(PubSubMsgHeaders.PI_SEQ_NO.getHeader());
//        String mmsi = (String) message.getHeaders().get(PubSubMsgHeaders.MMSI.getHeader());
//        S125Node s125Node = (S125Node) message.getPayload();
//
//        // A simple debug message;
//        log.debug(String.format("Sending AtoN Message with UID: %s to VDES station %s:%d.", s125Node.getAtonUID(), address, port));
//
//        // Now send the S125 message to the VDES station
//        this.sendDatagram(address, port, piseqno, mmsi, s125Node);
//    }
//
//    /**
//     * The main function that sends the UDP package to the VDES station. To
//     * make the streaming operation easier, we are actually returning the
//     * provided message for each successful transmission.
//     *
//     * @param address       The address to send the datagram to
//     * @param port          The port to send the datagram to
//     * @param piseqno       The PI sequence number of the VDES station
//     * @param mmsi          The MMSI of the VDES station
//     * @param s125Node       The S125 message to be transmitted
//     * @return The S125 message to be transmitted
//     */
//    private void sendDatagram(String address, int port, long piseqno, String mmsi, S125Node s125Node) {
//        // Construct the UDP message for the VDES station
//        byte[] buffer = null;
//        try {
//            buffer = VDES1000Utils.s125ToVDE(s125Node, piseqno, mmsi).getBytes();
//        } catch (JAXBException ex) {
//            log.error(ex.getMessage());
//            return;
//        }
//
//        // Create and send the UDP datagram packet
//        try {
//            DatagramPacket packet = new DatagramPacket(buffer, buffer.length,
//                    InetAddress.getByName(address), port);
//            this.vdesSocket.send(packet);
//        } catch (IOException e) {
//            log.error(e.getMessage());
//        }
//    }

}
