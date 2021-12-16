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

package org.grad.eNav.vdesCtrl.models.vdes.comm;

import lombok.extern.slf4j.Slf4j;
import org.grad.eNav.vdesCtrl.models.domain.AISChannel;
import org.grad.eNav.vdesCtrl.models.vdes.AbstractMessage;
import org.grad.eNav.vdesCtrl.models.vdes.AbstractSentence;
import org.grad.eNav.vdesCtrl.models.vdes.ais.sentences.BBMSentence;
import org.grad.eNav.vdesCtrl.models.vdes.ais.sentences.BBMSentenceBuilder;
import org.grad.eNav.vdesCtrl.models.vdes.ais.sentences.TSASentence;
import org.grad.eNav.vdesCtrl.models.vdes.ais.sentences.TSASentenceBuilder;
import org.grad.eNav.vdesCtrl.models.vdes.iec61162_450.IEC61162_450Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The VDES-1000 Connection Class.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Slf4j
public class VDES1000Conn {

    // Class Variables
    protected final DatagramSocket vdes1000Socket;
    protected final VDESBroadcastMethod broadcastMethod;
    protected final String address;
    protected final int port;
    protected String sourceId;
    protected int groupId;
    protected int vdmSequentialId;
    protected int bbmSequentialId;

    /**
     * Instantiates a new Vdes 1000 conn.
     *
     * @param broadcastMethod the broadcast method
     * @param address         the address
     * @param port            the port
     */
    public VDES1000Conn(VDESBroadcastMethod broadcastMethod, String sourceId, String address, int port) throws SocketException {
        // Initialise the connections
        this.vdes1000Socket = new DatagramSocket();
        this.groupId = 1;
        this.vdmSequentialId = 0;
        this.bbmSequentialId = 0;

        // And copy the input parameters
        this.broadcastMethod = broadcastMethod;
        this.address = address;
        this.port = port;
        this.sourceId = sourceId;
    }

    /**
     * Destroys the VDES-1000 Connection
     */
    public void close() {
        this.vdes1000Socket.close();
    }

    /**
     * Gets vdes 1000 socket.
     *
     * @return the vdes 1000 socket
     */
    public DatagramSocket getVdes1000Socket() {
        return vdes1000Socket;
    }

    /**
     * Gets broadcast method.
     *
     * @return the broadcast method
     */
    public VDESBroadcastMethod getBroadcastMethod() {
        return broadcastMethod;
    }

    /**
     * Gets address.
     *
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Gets port.
     *
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * Sends an VDES-1000 message to the VDES-1000 connection.
     *
     * @param message the message to be sent
     */
    public void sendMessage(AbstractMessage message, AISChannel channel) {
        if(this.broadcastMethod == VDESBroadcastMethod.TSA_VDM) {
            this.sendMessageWithTSAVDM(message, channel);
        }
        else if(this.broadcastMethod == VDESBroadcastMethod.BBM) {
            this.sendMessageWithBBM(message, channel);
        } else {
            throw new RuntimeException("An invalid VDES-1000 broadcast type was detected!");
        }
    }

    /**
     * Sends an VDES-1000 message to the VDES-1000 connection using the TSA-VDM
     * broadcast mechanism.
     *
     * @param message the message to be sent
     */
    protected void sendMessageWithTSAVDM(AbstractMessage message, AISChannel channel) {
        // Sanity check
        if(Objects.isNull(message)) {
            return;
        }

        // First build the TSA sentence, that contains all the associated VDMs
        TSASentence tsaSentence = new TSASentenceBuilder()
                .vdmLink(this.vdmSequentialId)
                .channel(channel)
                .message(message)
                .build();

        // If this is a mutli-sequence message increase the sequential ID
        if(tsaSentence.getSentences().size() > 0) {
            this.vdmSequentialId = (this.vdmSequentialId++)%10;
        }

        // First sent the main TSA sentence
        this.sendSentences(Collections.singletonList(tsaSentence));

        // And the all the associated VDMs
        this.sendSentences(tsaSentence.getSentences());
    }

    /**
     * Sends an VDES-1000 message to the VDES-1000 connection using the BBM
     * broadcast mechanism.
     *
     * @param message the message to be sent
     */
    protected void sendMessageWithBBM(AbstractMessage message, AISChannel channel) {
        // Sanity check
        if(Objects.isNull(message)) {
            return;
        }

        // First build the BBM sentences
        List<BBMSentence> bbmSentences = new BBMSentenceBuilder()
                .sequenceId(this.bbmSequentialId)
                .channel(channel)
                .message(message)
                .build();

        // If this is a multi-sequence message increase the sequential ID
        if(bbmSentences.size() > 0) {
            this.bbmSequentialId = (this.bbmSequentialId++)%10;
        }

        // And the all the associated BBMs
        this.sendSentences(bbmSentences.stream().map(AbstractSentence.class::cast).collect(Collectors.toList()));
    }

    /**
     * Sends a list of sentences directly to the VDES-1000 connection.
     *
     * @param sentences The list of sentences to be sent
     */
    public void sendSentences(List<AbstractSentence> sentences) {
        // Sanity check
        if(Objects.isNull(sentences)) {
            return;
        }

        Optional.ofNullable(sentences)
                .orElse(Collections.emptyList())
                .stream()
                .map(sentence -> new IEC61162_450Message(sentences.size(),
                        sentences.indexOf(sentence)+1,
                        this.groupId,
                        this.sourceId,
                        sentence.toStringWithChecksum())
                )
                .forEach(this::sendIEC61162_450);
        this.groupId = (this.groupId+1)%99;
    }

    /**
     * Sends an IEC 61162-450 message to the VDES-1000 connection.
     *
     * @param iec61162_450Message The IEC 61162-450 message to be sent
     */
    public void sendIEC61162_450(IEC61162_450Message iec61162_450Message) {
        // Sanity check
        if(Objects.isNull(iec61162_450Message)) {
            return;
        }

        // Create and send the UDP datagram packet
        try {
            DatagramPacket packet = new DatagramPacket(
                    iec61162_450Message.toString().getBytes(),
                    iec61162_450Message.toString().length(),
                    InetAddress.getByName(address),
                    port);
            this.vdes1000Socket.send(packet);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

}
