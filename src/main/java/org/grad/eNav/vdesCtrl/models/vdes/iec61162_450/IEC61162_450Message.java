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

package org.grad.eNav.vdesCtrl.models.vdes.iec61162_450;

import org.grad.eNav.vdesCtrl.utils.GrAisUtils;

import java.util.Optional;

/**
 * The IEC 61162-450 Message Class.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public class IEC61162_450Message {

    // Class Variables
    private int sentencesTotal;
    private int  sentenceNum;
    private int  groupId;
    private String  sourceId;
    private String sentence;

    /**
     * Instantiates a new Iec 61162450 message.
     *
     * @param sentencesTotal the sentences total
     * @param sentenceNum    the sentence num
     * @param groupId        the group id
     * @param sourceId       the source id
     * @param sentence       the sentence
     */
    public IEC61162_450Message(int sentencesTotal,
                               int sentenceNum,
                               int groupId,
                               String sourceId,
                               String sentence)
    {
        this.sentencesTotal = sentencesTotal;
        this.sentenceNum = sentenceNum;
        this.groupId = groupId;
        this.sourceId = sourceId;
        this.sentence = sentence;
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
     * Gets group id.
     *
     * @return the group id
     */
    public int getGroupId() {
        return groupId;
    }

    /**
     * Sets group id.
     *
     * @param groupId the group id
     */
    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    /**
     * Gets source id.
     *
     * @return the source id
     */
    public String getSourceId() {
        return sourceId;
    }

    /**
     * Sets source id.
     *
     * @param sourceId the source id
     */
    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    /**
     * Gets sentence.
     *
     * @return the sentence
     */
    public String getSentence() {
        return sentence;
    }

    /**
     * Sets sentence.
     *
     * @param sentence the sentence
     */
    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    /**
     * Returns the string representation of the sentence, formatted as per
     * IEC 61162-450.
     *
     * @return the string representation of the sentence
     */
    @Override
    public String toString() {
        // Create the IEC 61162-450 Message Tag
        String tag =  new StringBuilder()
                .append("g:")
                .append(this.sentencesTotal)
                .append("-")
                .append(this.sentenceNum)
                .append("-")
                .append(this.groupId)
                .append(",s:")
                .append(Optional.ofNullable(this.sourceId).orElse(""))
                .toString();

        // Create the IEC 61162-450 Message Tag Checksum
        String tagChecksum = GrAisUtils.calculateIECChecksum(tag);

        // Build and return the message
        return new StringBuilder()
                .append("\\")
                .append(tag)
                .append("*")
                .append(tagChecksum)
                .append("\\")
                .append(this.sentence)
                .toString();
    }
}
