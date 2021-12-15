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

package org.grad.eNav.vdesCtrl.models.txrx.asm.sentences;

import org.grad.eNav.vdesCtrl.models.domain.AISChannel;
import org.grad.eNav.vdesCtrl.models.txrx.AbstractSentence;

import java.util.Optional;

/**
 * The type ABB Sentence Class.
 * <p>
 * Implements the ASM broadcast message.
 * </p>
 * <p>
 * The Message Definition contains the following fields:
 * </p>
 * <ul>
 *     TalkerId : str, optional
 *     <<br/> Talker ID. The default is "AI".
 * </ul>
 * <ul>
 *     sentencesTotal : int
 *     <br/> Total number of sentences needed to transfer the message (1-99).
 * </ul>
 * <ul>
 *     sentenceNum : int
 *     <br/> Sentence number (1-99).
 * </ul>
 * <ul>
 *     sequenceId : int
 *     <br/> Sequential message identifier (0-9).
 * </ul>
 * <ul>
 *     sourceId : int
 *     <br/> Source ID (10 digits as per the draft IEC VDES-ASM PAS; VDES1000
 *     <br/> Currently only supports 9 digits).
 * </ul>
 * <ul>
 *     channel : int
 *     <br/> AIS channel for broadcast of the message:
 *     <li>0: No preference
 *     <li>1: ASM 1</li>
 *     <li>2: ASM 2</li>
 *     <li>3: Both channels.</li>
 * </ul>
 * <ul>
 *     asmId : str, optional
 *     <br/> ASM message ID as per Rec. ITU-R M.2092. Reserved for future use;
 *     <br/> shall be set to null (""). The default is "".
 * </ul>
 * <ul>
 *     transmissionFormat : int
 *     <br/> Transmission format:
 *     <li>0: No error coding</li>
 *     <li>1: 3/4 FEC</li>
 *     <li>2: ASM SAT uplink message</li>
 *     <li>3-9: Reserved for future use.</li>
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
public class ABBSentence extends AbstractSentence {

    /**
     * The constant FORMATTER_CODE.
     */
    public static final String FORMATTER_CODE = "ABB";

    // Class Variables
    private int sentencesTotal;
    private int  sentenceNum;
    private int  sequenceId;
    private Optional<Integer> sourceId;
    private AISChannel channel;
    private Optional<String> asmId;
    private int transmissionFormat;
    private byte[] payload;

    /**
     * Instantiates a new ABB sentence.
     *
     * @param sentencesTotal     the sentences total
     * @param sentenceNum        the sentence num
     * @param sequenceId         the sequence id
     * @param channel            the channel
     * @param transmissionFormat the transmission format
     * @param payload            the payload
     */
    public ABBSentence(int sentencesTotal,
                       int sentenceNum,
                       int sequenceId,
                       AISChannel channel,
                       int transmissionFormat,
                       byte[] payload) {
        this("AI", sentencesTotal, sentenceNum, sequenceId, channel, transmissionFormat, payload);
    }

    /**
     * Instantiates a new ABB sentence.
     *
     * @param talkerId           the talker id
     * @param sentencesTotal     the sentences total
     * @param sentenceNum        the sentence num
     * @param sequenceId         the sequence id
     * @param channel            the channel
     * @param transmissionFormat the transmission format
     * @param payload            the payload
     */
    public ABBSentence(String talkerId,
                       int sentencesTotal,
                       int sentenceNum,
                       int sequenceId,
                       AISChannel channel,
                       int transmissionFormat,
                       byte[] payload) {
        super(talkerId, FORMATTER_CODE);
        this.sentencesTotal = sentencesTotal;
        this.sentenceNum = sentenceNum;
        this.sequenceId = sequenceId;
        this.sourceId = Optional.empty();
        this.channel = channel;
        this.asmId = Optional.empty();
        this.transmissionFormat = transmissionFormat;
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
    public int getSequenceId() {
        return sequenceId;
    }

    /**
     * Sets sequence id.
     *
     * @param sequenceId the sequence id
     */
    public void setSequenceId(int sequenceId) {
        this.sequenceId = sequenceId;
    }

    /**
     * Gets source id.
     *
     * @return the source id
     */
    public Optional<Integer> getSourceId() {
        return sourceId;
    }

    /**
     * Sets source id.
     *
     * @param sourceId the source id
     */
    public void setSourceId(Optional<Integer> sourceId) {
        this.sourceId = sourceId;
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
     * Gets transmission format.
     *
     * @return the transmission format
     */
    public int getTransmissionFormat() {
        return transmissionFormat;
    }

    /**
     * Sets transmission format.
     *
     * @param transmissionFormat the transmission format
     */
    public void setTransmissionFormat(int transmissionFormat) {
        this.transmissionFormat = transmissionFormat;
    }

    /**
     * Get payload byte [ ].
     *
     * @return the byte [ ]
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
     * Gets asm id.
     *
     * @return the asm id
     */
    public Optional<String> getAsmId() {
        return asmId;
    }

    /**
     * Sets asm id.
     *
     * @param asmId the asm id
     */
    public void setAsmId(Optional<String> asmId) {
        this.asmId = asmId;
    }

    /**
     * Returns the string representation of the sentence, formatted as per
     * Sentence string, formatted as per the draft IEC VDES ASM PAS, Oct. 2020.
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
                .append(String.format("%02d", this.sentencesTotal))
                .append(",")
                .append(String.format("%02d", this.sentenceNum))
                .append(",")
                .append(this.sequenceId)
                .append(",")
                .append(this.sourceId.map(String::valueOf).orElse(""))
                .append(",")
                .append(Optional.ofNullable(this.channel).map(AISChannel::getIndex).orElse(0))
                .append(",")
                .append(this.asmId.orElse(""))
                .append(",")
                .append(this.transmissionFormat)
                .append(",")
                .append(Optional.ofNullable(this.payload).map(String::new).orElse(""))
                .append(",")
                .append(this.getNoFillBits())
                .toString();
    }
}
