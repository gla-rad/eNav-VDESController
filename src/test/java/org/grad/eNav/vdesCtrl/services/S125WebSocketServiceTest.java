/*
 * Copyright (c) 2021 GLA Research and Development Directorate
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

package org.grad.eNav.vdesCtrl.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.IOUtils;
import org.grad.eNav.vdesCtrl.models.PubSubMsgHeaders;
import org.grad.eNav.vdesCtrl.models.domain.StationType;
import org.grad.eNav.vdesCtrl.models.dtos.S125Node;
import org.grad.eNav.vdesCtrl.utils.GeoJSONUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S125WebSocketServiceTest {

    /**
     * The Tested Service.
     */
    @InjectMocks
    @Spy
    S125WebSocketService s125WebSocketService;

    /**
     * The AtoN Publish Subscribe Channel mock.
     */
    @Mock
    PublishSubscribeChannel atonPublishChannel;

    /**
     * The Web Socket mock.
     */
    @Mock
    SimpMessagingTemplate webSocket;

    // Test Variables
    private S125Node s125Node;

    /**
     * Common setup for all the tests.
     */
    @BeforeEach
    void setup() throws IOException {
        // First read a valid S125 content to generate the publish-subscribe
        // message for.
        InputStream in = new ClassPathResource("s125-msg.xml").getInputStream();
        String xml = IOUtils.toString(in, StandardCharsets.UTF_8.name());

        // Also create a GeoJSON point geometry for our S125 message
        JsonNode point = GeoJSONUtils.createGeoJSONPoint(53.61, 1.594);

        // Now create the S125 node object
        this.s125Node = new S125Node("test_aton", point, xml);

        // Also set the web-socket service topic prefix
        this.s125WebSocketService.prefix = "topic";
    }

    /**
     * Test that the S125 web-socket service gets initialised correctly,
     * and it subscribes to the AtoN publish subscribe channel.
     */
    @Test
    void testInit() {
        // Perform the service call
        this.s125WebSocketService.init();

        verify(this.atonPublishChannel, times(1)).subscribe(this.s125WebSocketService);
    }

    /**
     * Test that the S125 web-socket service gets destroyed correctly,
     * and it un-subscribes from the AtoN publish subscribe channel.
     */
    @Test
    void testDestroy() {
        // Perform the service call
        this.s125WebSocketService.destroy();

        verify(this.atonPublishChannel, times(1)).destroy();
    }

    /**
     * Test that the Web-Socket controlling service can process correctly the
     * AtoN messages published in the AtoN publish-subscribe channel.
     */
    @Test
    void testHandleMessage() throws IOException {
        // Create a message to be handled
        Message message = Optional.of(this.s125Node).map(MessageBuilder::withPayload)
                .map(builder -> builder.setHeader(MessageHeaders.CONTENT_TYPE, StationType.VDES_1000))
                .map(builder -> builder.setHeader(PubSubMsgHeaders.ADDRESS.getHeader(), "127.0.0.1"))
                .map(builder -> builder.setHeader(PubSubMsgHeaders.PORT.getHeader(), 8000))
                .map(builder -> builder.setHeader(PubSubMsgHeaders.PI_SEQ_NO.getHeader(), 1234L))
                .map(builder -> builder.setHeader(PubSubMsgHeaders.MMSI.getHeader(), "111111111"))
                .map(MessageBuilder::build)
                .orElse(null);

        // Perform the service call
        this.s125WebSocketService.handleMessage(message);

        // Verify that we send a packet to the VDES port and get that packet
        ArgumentCaptor<String> topicArgument = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<S125Node> payLoadArgument = ArgumentCaptor.forClass(S125Node.class);
        verify(this.webSocket, times(1)).convertAndSend(topicArgument.capture(), payLoadArgument.capture());

        // Verify the packet
        assertEquals("/topic/127.0.0.1:8000", topicArgument.getValue());
        assertEquals(this.s125Node, payLoadArgument.getValue());
    }

    /**
     * Test that we can only send S125 messages down to the web-socket.
     */
    @Test
    void testHandleMessageWrongPayload() throws IOException {
        // Change the message content type to something else
        Message message = Optional.of("this is just a string").map(MessageBuilder::withPayload)
                .map(builder -> builder.setHeader(MessageHeaders.CONTENT_TYPE, StationType.VDES_1000))
                .map(builder -> builder.setHeader(PubSubMsgHeaders.ADDRESS.getHeader(), "127.0.0.1"))
                .map(builder -> builder.setHeader(PubSubMsgHeaders.PORT.getHeader(), 8000))
                .map(builder -> builder.setHeader(PubSubMsgHeaders.PI_SEQ_NO.getHeader(), 1234L))
                .map(builder -> builder.setHeader(PubSubMsgHeaders.MMSI.getHeader(), "111111111"))
                .map(MessageBuilder::build)
                .orElse(null);

        // Perform the service call
        this.s125WebSocketService.handleMessage(message);

        // Verify that we didn't send any packets to the VDES port
        verify(this.webSocket, never()).convertAndSend(any(String.class), any(Object.class));
    }

}