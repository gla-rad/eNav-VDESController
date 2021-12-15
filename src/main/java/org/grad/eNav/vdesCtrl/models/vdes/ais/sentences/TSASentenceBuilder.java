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

package org.grad.eNav.vdesCtrl.models.vdes.ais.sentences;

import org.grad.eNav.vdesCtrl.models.domain.AISChannel;
import org.grad.eNav.vdesCtrl.models.vdes.AbstractMessage;
import org.grad.eNav.vdesCtrl.models.vdes.AbstractSentence;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The type Tsa sentence builder.
 */
public class TSASentenceBuilder {

    // Class Variables
    private String uniqueId;
    private Integer vdmLink;
    private AISChannel channel;
    private String utcHHMM;
    private String startSlot;
    private Integer priority;
    private List<AbstractSentence> sentences;
    private AbstractMessage message;

    /**
     * The unique id of the TSA sentence builder.
     *
     * @param uniqueId the unique id
     * @return the tsa sentence builder
     */
    public TSASentenceBuilder uniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
        return this;
    }

    /**
     * The VDM link of the TSA sentence builder.
     *
     * @param vdmLink the vdm link
     * @return the tsa sentence builder
     */
    public TSASentenceBuilder vdmLink(Integer vdmLink) {
        this.vdmLink = vdmLink;
        return this;
    }

    /**
     * The channel of the TSA sentence builder.
     *
     * @param channel the channel
     * @return the tsa sentence builder
     */
    public TSASentenceBuilder channel(AISChannel channel) {
        this.channel = channel;
        return this;
    }

    /**
     * The UTC HHMM of the TSA sentence builder.
     *
     * @param utcHHMM the utc hhmm
     * @return the tsa sentence builder
     */
    public TSASentenceBuilder utcHHMM(String utcHHMM) {
        this.utcHHMM = utcHHMM;
        return this;
    }

    /**
     * The start slot of the TSA sentence builder.
     *
     * @param startSlot the start slot
     * @return the tsa sentence builder
     */
    public TSASentenceBuilder startSlot(String startSlot) {
        this.startSlot = startSlot;
        return this;
    }

    /**
     * The priority of the TSA sentence builder.
     *
     * @param priority the priority
     * @return the tsa sentence builder
     */
    public TSASentenceBuilder priority(Integer priority) {
        this.priority = priority;
        return this;
    }

    /**
     * The VDM sentences of the TSA sentence builder.
     *
     * @param sentences the sentences
     * @return the tsa sentence builder
     */
    public TSASentenceBuilder sentences(List<AbstractSentence> sentences) {
        this.sentences = sentences;
        return this;
    }

    /**
     * The VDM sentences of the TSA sentence builder using a message.
     *
     * @param message the message
     * @return the tsa sentence builder
     */
    public TSASentenceBuilder message(AbstractMessage message) {
        this.message = message;
        return this;
    }

    /**
     * Build the TSA Sentence.
     *
     * @return the TSA Sentence
     */
    public TSASentence build() {
        // Generate the TSA sentence object
        TSASentence tsaSentence = new TSASentence(this.vdmLink, this.channel);
        tsaSentence.setUniqueId(Optional.ofNullable(this.uniqueId));
        tsaSentence.setStartSlot(Optional.ofNullable(this.startSlot));
        tsaSentence.setPriority(Optional.ofNullable(this.priority));

        // Populate the associated sentences based on what we got
        if(this.sentences != null) {
            tsaSentence.setSentences(this.sentences);
        } else if(this.message != null) {
            tsaSentence.setSentences(new VDMSentenceBuilder()
                    .sequenceId(this.vdmLink)
                    .message(this.message)
                    .build()
                    .stream()
                    .map(AbstractSentence.class::cast)
                    .collect(Collectors.toList()));
        }

        // And return the sentence
        return tsaSentence;
    }
}
