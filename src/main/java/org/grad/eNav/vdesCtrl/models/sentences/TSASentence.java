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

package org.grad.eNav.vdesCtrl.models.sentences;

import org.grad.eNav.vdesCtrl.models.domain.AISChannel;

import java.util.List;

/**
 * The TSA Sentence Class.
 * <p>
 * Implements the AIS TSA Sentence: Transmit slot assignment.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public class TSASentence extends AbstractSentence {

    /**
     * The constant FORMATTER_CODE.
     */
    public static final String FORMATTER_CODE = "TSA";

    // Class Variables
    private String uniqueId;
    private int vdmLink;
    private AISChannel channel;
    private String utcHHMM;
    private String startSlot;
    private int priority;
    private List<AbstractSentence> sentences;

    /**
     * Instantiates a new Tsa sentence.
     */
    public TSASentence() {
        super("AI", FORMATTER_CODE);
    }

    /**
     * Instantiates a new Tsa sentence.
     *
     * @param talkerId the talker id
     */
    public TSASentence(String talkerId) {
        super(talkerId, FORMATTER_CODE);
    }

    /**
     * Gets unique id.
     *
     * @return the unique id
     */
    public String getUniqueId() {
        return uniqueId;
    }

    /**
     * Sets unique id.
     *
     * @param uniqueId the unique id
     */
    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    /**
     * Gets vdm link.
     *
     * @return the vdm link
     */
    public int getVdmLink() {
        return vdmLink;
    }

    /**
     * Sets vdm link.
     *
     * @param vdmLink the vdm link
     */
    public void setVdmLink(int vdmLink) {
        this.vdmLink = vdmLink;
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
     * Gets utc hhmm.
     *
     * @return the utc hhmm
     */
    public String getUtcHHMM() {
        return utcHHMM;
    }

    /**
     * Sets utc hhmm.
     *
     * @param utcHHMM the utc hhmm
     */
    public void setUtcHHMM(String utcHHMM) {
        this.utcHHMM = utcHHMM;
    }

    /**
     * Gets start slot.
     *
     * @return the start slot
     */
    public String getStartSlot() {
        return startSlot;
    }

    /**
     * Sets start slot.
     *
     * @param startSlot the start slot
     */
    public void setStartSlot(String startSlot) {
        this.startSlot = startSlot;
    }

    /**
     * Gets priority.
     *
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Sets priority.
     *
     * @param priority the priority
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * Gets sentences.
     *
     * @return the sentences
     */
    public List<AbstractSentence> getSentences() {
        return sentences;
    }

    /**
     * Sets sentences.
     *
     * @param sentences the sentences
     */
    public void setSentences(List<AbstractSentence> sentences) {
        this.sentences = sentences;
    }
}
