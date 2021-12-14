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

/**
 * The VDM Sentence Class.
 * <p>
 * Implements sentences for the AIS VHF data-link messages.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public class VDMSentence extends AbstractSentence {

    /**
     * The constant FORMATTER_CODE.
     */
    public static final String FORMATTER_CODE = "VDM";

    // Class Variables
    private int sentencesTotal;
    private int  sentenceNum;
    private int  sequenceId;
    private AISChannel channel;
    private byte[] payload;
    private int noFillBits;

    /**
     * Instantiates a new Vdm sentence.
     */
    public VDMSentence() {
        super("AI", FORMATTER_CODE);
    }

    /**
     * Instantiates a new Vdm sentence.
     *
     * @param talkerId the talker id
     */
    public VDMSentence(String talkerId) {
        super(talkerId, FORMATTER_CODE);
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
        return noFillBits;
    }

    /**
     * Sets no fill bits.
     *
     * @param noFillBits the no fill bits
     */
    public void setNoFillBits(int noFillBits) {
        this.noFillBits = noFillBits;
    }
}
