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

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VDES1000ServiceTest {

    /**
     * The Tested Service.
     */
    @InjectMocks
    @Spy
    VDES1000Service vdes1000Service;

    /**
     * The AtoN Publish Subscribe Channel mock.
     */
    @Mock
    PublishSubscribeChannel atonPublishChannel;

    /**
     * The VDES-100 UDP Socket mock.
     */
    @Mock
    DatagramSocket vdesSocket;

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
    }

    /**
     * Test that the VDES-1000 controlling service gets initialised correctly,
     * and it subscribes to the AtoN publish subscribe channel.
     */
    @Test
    void testInit() throws SocketException {
        // Perform the service call
        this.vdes1000Service.init();

        verify(this.atonPublishChannel, times(1)).subscribe(this.vdes1000Service);
    }

    /**
     * Test that the VDES-1000 controlling service gets destroyed correctly,
     * and it un-subscribes from the AtoN publish subscribe channel.
     */
    @Test
    void testDestroy() {
        // Perform the service call
        this.vdes1000Service.destroy();

        verify(this.atonPublishChannel, times(1)).destroy();
    }

    /**
     * Test that the VDES-1000 controlling service can process correctly the
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
        this.vdes1000Service.handleMessage(message);

        // Verify that we send a packet to the VDES port and get that packet
        ArgumentCaptor<DatagramPacket> argument = ArgumentCaptor.forClass(DatagramPacket.class);
        verify(this.vdesSocket, times(1)).send(argument.capture());

        // Verify the packet
        assertEquals(InetAddress.getByName("127.0.0.1"), argument.getValue().getAddress());
        assertEquals(8000, argument.getValue().getPort());

        // And make sure we are sending a VDES sentence
        assertTrue(new String(argument.getValue().getData()).startsWith("$AI"));
    }

    /**
     * Test that if the received message from the publish-subscribe channel is
     * NOT for our VDES station, then it will not be sent.
     */
    @Test
    void testHandleMessageNotVDESMessage() throws IOException {
        // Change the message content type to something else
        Message message = Optional.of(this.s125Node).map(MessageBuilder::withPayload)
                .map(builder -> builder.setHeader(MessageHeaders.CONTENT_TYPE, StationType.GNU_RADIO))
                .map(builder -> builder.setHeader(PubSubMsgHeaders.ADDRESS.getHeader(), "127.0.0.1"))
                .map(builder -> builder.setHeader(PubSubMsgHeaders.PORT.getHeader(), 8000))
                .map(builder -> builder.setHeader(PubSubMsgHeaders.PI_SEQ_NO.getHeader(), 1234L))
                .map(builder -> builder.setHeader(PubSubMsgHeaders.MMSI.getHeader(), "111111111"))
                .map(MessageBuilder::build)
                .orElse(null);

        // Perform the service call
        this.vdes1000Service.handleMessage(message);

        // Verify that we didn't send any packets to the VDES port
        verify(this.vdesSocket, never()).send(any());
    }

    /**
     * Test that we can only send S125 messages down to the VDES-1000 station
     * port.
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
        this.vdes1000Service.handleMessage(message);

        // Verify that we didn't send any packets to the VDES port
        verify(this.vdesSocket, never()).send(any());
    }

    /**
     * Test that for any issues while sending the packet, the service will
     * not stop it's execution, just a log should be OK.
     */
    @Test
    void testHandleMessageSendingError() throws IOException {
        // Create a message to be handled
        Message message = Optional.of(this.s125Node).map(MessageBuilder::withPayload)
                .map(builder -> builder.setHeader(MessageHeaders.CONTENT_TYPE, StationType.VDES_1000))
                .map(builder -> builder.setHeader(PubSubMsgHeaders.ADDRESS.getHeader(), "127.0.0.1"))
                .map(builder -> builder.setHeader(PubSubMsgHeaders.PORT.getHeader(), 8000))
                .map(builder -> builder.setHeader(PubSubMsgHeaders.PI_SEQ_NO.getHeader(), 1234L))
                .map(builder -> builder.setHeader(PubSubMsgHeaders.MMSI.getHeader(), "111111111"))
                .map(MessageBuilder::build)
                .orElse(null);

        doThrow(IOException.class).when(this.vdesSocket).send(any());

        // Perform the service call
        this.vdes1000Service.handleMessage(message);

        // Verify that we didn't send any packets to the VDES port
        verify(this.vdesSocket, times(1)).send(any());
    }

}