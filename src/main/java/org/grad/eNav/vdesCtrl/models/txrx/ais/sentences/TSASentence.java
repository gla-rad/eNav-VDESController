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

import java.util.List;
import java.util.Optional;

/**
 * The TSA Sentence Class.
 * <p>
 * Implements the AIS TSA Sentence: Transmit slot assignment.
 * </p>
 * <p>
 * The Message Definition contains the following fields:
 * </p>
 * <ul>
 *     TalkerId : str, optional
 *     <br/> Talker ID. The default is "AI".
 * </ul>
 * <ul>
 *     uniqueId : str, optional
 *     <br/> Base station's unique ID. Maximum of 15 characters. The default is
 *     "".
 * </ul>
 * <ul>
 *     vdmLink : int
 *     <br/> VDM link.
 * </ul>
 * <ul>
 *     utcHHMM : str, optional
 *     <br/> UTC frame hour and minute of the requested transmission. The
 *     default is "".
 * </ul>
 * <ul>
 *     startSlot : str, optional
 *     <br/> Start slot number of the requested transmission. The default is "".
 * </ul>
 * <ul>
 *     priority
 *     <br/> Transmission priority (0-2). Lower number corresponds to higher
 *     priority. The default is 2.
 * </ul>
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public class TSASentence extends AbstractSentence {

    /**
     * The constant FORMATTER_CODE.
     */
    public static final String FORMATTER_CODE = "TSA";

    // Class Variables
    private Optional<String> uniqueId;
    private int vdmLink;
    private AISChannel channel;
    private Optional<String> utcHHMM;
    private Optional<String> startSlot;
    private Optional<Integer> priority;
    private List<AbstractSentence> sentences;

    /**
     * Instantiates a new TSA sentence.
     *
     * @param vdmLink  the vdm link
     * @param channel  the channel
     */
    public TSASentence(int vdmLink,
                       AISChannel channel) {
        this("AI", vdmLink, channel);
    }

    /**
     * Instantiates a new TSA sentence.
     *
     * @param talkerId the talker id
     * @param vdmLink  the vdm link
     * @param channel  the channel
     */
    public TSASentence(String talkerId,
                       int vdmLink,
                       AISChannel channel) {
        super(talkerId, FORMATTER_CODE);
        this.uniqueId = Optional.empty();
        this.vdmLink = vdmLink;
        this.channel = channel;
        this.utcHHMM = Optional.empty();
        this.startSlot = Optional.empty();
        this.priority = Optional.empty();
    }

    /**
     * Gets unique id.
     *
     * @return the unique id
     */
    public Optional<String> getUniqueId() {
        return uniqueId;
    }

    /**
     * Sets unique id.
     *
     * @param uniqueId the unique id
     */
    public void setUniqueId(Optional<String> uniqueId) {
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
    public Optional<String> getUtcHHMM() {
        return utcHHMM;
    }

    /**
     * Sets utc hhmm.
     *
     * @param utcHHMM the utc hhmm
     */
    public void setUtcHHMM(Optional<String> utcHHMM) {
        this.utcHHMM = utcHHMM;
    }

    /**
     * Gets start slot.
     *
     * @return the start slot
     */
    public Optional<String> getStartSlot() {
        return startSlot;
    }

    /**
     * Sets start slot.
     *
     * @param startSlot the start slot
     */
    public void setStartSlot(Optional<String> startSlot) {
        this.startSlot = startSlot;
    }

    /**
     * Gets priority.
     *
     * @return the priority
     */
    public Optional<Integer> getPriority() {
        return priority;
    }

    /**
     * Sets priority.
     *
     * @param priority the priority
     */
    public void setPriority(Optional<Integer> priority) {
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
                .append(this.uniqueId.orElse(""))
                .append(",")
                .append(Optional.ofNullable(this.vdmLink).map(String::valueOf).orElse(""))
                .append(",")
                .append(Optional.ofNullable(this.channel).map(AISChannel::getChannel).orElse("A"))
                .append(",")
                .append(this.utcHHMM.orElse(""))
                .append(",")
                .append(this.startSlot.orElse(""))
                .append(",")
                .append(this.priority.orElse(2))
                .toString();
    }
}
