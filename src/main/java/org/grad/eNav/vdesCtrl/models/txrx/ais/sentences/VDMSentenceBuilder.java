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
import org.grad.eNav.vdesCtrl.models.txrx.AbstractMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * The VDM Sentence Builder Class.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public class VDMSentenceBuilder {

    /**
     * Maximum number of characters in a payload for the VDM sentence;
     * Assuming the max number of characters per sentence is 82 (as per
     * IEC 61162-1) and that all sentence fields are populated (no null
     * fields).
     * Can be up to 62 under certain circumstances.
     */
    public static final int MAX_PAYLOAD_CHAR = 60;

    // Class Variables
    private Integer sequenceId;
    private AISChannel channel;
    private AbstractMessage message;

    /**
     * The sequence ID of the VDM sentence builder.
     *
     * @param sequenceId the sequence id
     * @return the vdm sentence builder
     */
    public VDMSentenceBuilder sequenceId(Integer sequenceId) {
        this.sequenceId = sequenceId;
        return this;
    }

    /**
     * The channel of the VDM sentence builder.
     *
     * @param channel the channel
     * @return the vdm sentence builder
     */
    public VDMSentenceBuilder channel(AISChannel channel) {
        this.channel = channel;
        return this;
    }

    /**
     * The message of the VDM sentence builder.
     *
     * @param message the message
     * @return the vdm sentence builder
     */
    public VDMSentenceBuilder message(AbstractMessage message) {
        this.message = message;
        return this;
    }

    /**
     * Build list.
     *
     * @return the list
     */
    public List<VDMSentence> build() {
        // Initial calculations
        byte[] payload = this.message.getBinaryMessage(true);
        int sentencesTotal = (int) Math.ceil(payload.length / MAX_PAYLOAD_CHAR);

        // Initialise the output list
        List<VDMSentence> sentences = new ArrayList<>();
        for(int sentenceNum=0; sentenceNum<sentencesTotal; sentenceNum++) {
            int start = sentenceNum*MAX_PAYLOAD_CHAR;
            int stop = Math.min(start + MAX_PAYLOAD_CHAR, payload.length);
            VDMSentence vdmSentence = new VDMSentence(sentencesTotal, sentenceNum, this.channel, Arrays.copyOfRange(payload,start, stop));
            vdmSentence.setSequenceId(Optional.ofNullable(this.sequenceId));
            sentences.add(vdmSentence);
        }

        // Returns the constructed sentences
        return sentences;
    }
}
