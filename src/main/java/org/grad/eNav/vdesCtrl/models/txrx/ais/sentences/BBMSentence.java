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

package org.grad.eNav.vdesCtrl.models.txrx.ais.sentences;

import org.grad.eNav.vdesCtrl.models.domain.AISChannel;
import org.grad.eNav.vdesCtrl.models.txrx.AbstractSentence;

import java.util.Optional;

/**
 * The BBM Sentence Class.
 * <p>
 * Implements the AIS binary broadcast message.
 * </p>
 * <p>
 * The Message Definition contains the following fields:
 * </p>
 * <ul>
 *     TalkerId : str, optional
 *     <br/> Talker ID. The default is "AI".
 * </ul>
 * <ul>
 *     sentencesTotal : int
 *     <br/> Total number of sentences needed to transfer the message (1-99).
 * </ul>
 * <ul>
 *     sentenceNum : int, optional
 *     <br/> Sentence number (1-99).
 * </ul>
 * <ul>
 *     sequenceId : int
 *     <br/> Sequential message identifier (0-9).
 * </ul>
 * <ul>
 *     channel : int
 *     <br/> AIS channel for broadcast of the message:
 *     <li>0: No preference
 *     <li>1: Channel A / AIS 1</li>
 *     <li>2: Channel B / AIS 2</li>
 *     <li>3: Both channels.</li>
 * </ul>
 * <ul>
 *     messageId : int
 *     <br/> Message ID as per Rec. ITU-R M.1371.
 * </ul>
 * <ul>
 *     payload : str
 *     <br/> ASM payload (the Binary Data portion of the message).
 * </ul>
 * <ul>
 *     noFillBits : int
 *     <br/> Number of fill bits (0-5).
 * </ul>
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public class BBMSentence extends AbstractSentence {

    /**
     * The constant FORMATTER_CODE.
     */
    public static final String FORMATTER_CODE = "BBM";

    // Class Variables
    private int sentencesTotal;
    private int  sentenceNum;
    private Optional<Integer>  sequenceId;
    private AISChannel channel;
    private String messageId;
    private byte[] payload;

    /**
     * Instantiates a new BBM sentence.
     *
     * @param sentencesTotal the sentences total
     * @param sentenceNum    the sentence num
     * @param channel        the channel
     * @param messageId      the message id
     * @param payload        the payload
     */
    public BBMSentence(int sentencesTotal, int sentenceNum, AISChannel channel, String messageId, byte[] payload) {
        this("AI", sentencesTotal, sentenceNum, channel, messageId, payload);
    }

    /**
     * Instantiates a new BBM sentence.
     *
     * @param talkerId       the talker id
     * @param sentencesTotal the sentences total
     * @param sentenceNum    the sentence num
     * @param channel        the channel
     * @param messageId      the message id
     * @param payload        the payload
     */
    public BBMSentence(String talkerId,
                       int sentencesTotal,
                       int sentenceNum,
                       AISChannel channel,
                       String messageId,
                       byte[] payload) {
        super(talkerId, FORMATTER_CODE);
        this.sentencesTotal = sentencesTotal;
        this.sentenceNum = sentenceNum;
        this.sequenceId = Optional.empty();
        this.channel = channel;
        this.messageId = messageId;
        this.payload = payload;
    }

    /**
     * Gets sentences total.
     *
     * @return the sentences total
     */
    public int getSentencesTotal() {
        return sentencesTotal;
    }

    /**
     * Sets sentences total.
     *
     * @param sentencesTotal the sentences total
     */
    public void setSentencesTotal(int sentencesTotal) {
        this.sentencesTotal = sentencesTotal;
    }

    /**
     * Gets sentence num.
     *
     * @return the sentence num
     */
    public int getSentenceNum() {
        return sentenceNum;
    }

    /**
     * Sets sentence num.
     *
     * @param sentenceNum the sentence num
     */
    public void setSentenceNum(int sentenceNum) {
        this.sentenceNum = sentenceNum;
    }

    /**
     * Gets sequence id.
     *
     * @return the sequence id
     */
    public Optional<Integer> getSequenceId() {
        return sequenceId;
    }

    /**
     * Sets sequence id.
     *
     * @param sequenceId the sequence id
     */
    public void setSequenceId(Optional<Integer> sequenceId) {
        this.sequenceId = sequenceId;
    }

    /**
     * Gets channel.
     *
     * @return the channel
     */
    public AISChannel getChannel() {
        return channel;
    }

    /**
     * Sets channel.
     *
     * @param channel the channel
     */
    public void setChannel(AISChannel channel) {
        this.channel = channel;
    }

    /**
     * Gets message id.
     *
     * @return the message id
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Sets message id.
     *
     * @param messageId the message id
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * Gets payload.
     *
     * @return the payload
     */
    public byte[] getPayload() {
        return payload;
    }

    /**
     * Sets payload.
     *
     * @param payload the payload
     */
    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    /**
     * Gets no fill bits.
     *
     * @return the no fill bits
     */
    public int getNoFillBits() {
        return Optional.ofNullable(this.payload).map(p-> p.length*8).map(l -> (6 - l%6)%6).orElse(0);
    }

    /**
     * Returns the string representation of the sentence, formatted as per
     * IEC 62320-1.
     *
     * @return the string representation of the sentence
     */
    @Override
    public String toString() {
        return new StringBuilder()
                .append("!")
                .append(this.talkerId)
                .append(this.formatterCode)
                .append(",")
                .append(this.sentencesTotal)
                .append(",")
                .append(this.sentenceNum)
                .append(",")
                .append(this.sequenceId.map(String::valueOf).orElse(""))
                .append(",")
                .append(Optional.ofNullable(this.channel).map(AISChannel::getIndex).orElse(0))
                .append(",")
                .append(this.messageId)
                .append(",")
                .append(Optional.ofNullable(this.payload).map(String::new).orElse(""))
                .append(",")
                .append(this.getNoFillBits())
                .toString();
    }
}
