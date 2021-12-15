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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BBMSentenceTest {

    /**
     * Test that the BBM Sentences can be initialised correctly with the
     * default constructor.
     */
    @Test
    public void testConstructor() {
        BBMSentence bbmSentence = new BBMSentence(10, 1, AISChannel.A, 8, new byte[]{0x61, 0x62, 0x63, 0x64, 0x65});
        assertEquals("AI", bbmSentence.getTalkerId());
        assertEquals("BBM", bbmSentence.getFormatterCode());
        assertEquals(10, bbmSentence.getSentencesTotal());
        assertEquals(1, bbmSentence.getSentenceNum());
        assertTrue(bbmSentence.getSequenceId().isEmpty());
        assertEquals(AISChannel.A, bbmSentence.getChannel());
        assertEquals(8, bbmSentence.getMessageId());
        assertEquals(5, bbmSentence.getPayload().length);
        assertEquals(2, bbmSentence.getNoFillBits());
    }

    /**
     * Test that the BBM Sentences can be initialised correctly with the
     * Talker ID constructor.
     */
    @Test
    public void testConstructorWithTalkerId() {
        BBMSentence bbmSentence = new BBMSentence("CUSTOM", 10, 1, AISChannel.A, 8, new byte[]{0x61, 0x62, 0x63, 0x64, 0x65});
        assertEquals("CUSTOM", bbmSentence.getTalkerId());
        assertEquals("BBM", bbmSentence.getFormatterCode());
        assertEquals(10, bbmSentence.getSentencesTotal());
        assertEquals(1, bbmSentence.getSentenceNum());
        assertTrue(bbmSentence.getSequenceId().isEmpty());
        assertEquals(AISChannel.A, bbmSentence.getChannel());
        assertEquals(8, bbmSentence.getMessageId());
        assertEquals(5, bbmSentence.getPayload().length);
        assertEquals(2, bbmSentence.getNoFillBits());
    }

    /**
     * Test that we can correctly construct the string representation of the
     * sentence.
     */
    @Test
    public void testSentenceToString() {
        BBMSentence bbmSentence = new BBMSentence(10, 1, AISChannel.A, 8, new byte[]{0x61, 0x62, 0x63, 0x64, 0x65});
        assertEquals("!AIBBM,10,1,,1,8,abcde,2", bbmSentence.toString());
    }

    /**
     * Test that we can correctly construct the string representation of the
     * sentence with the checksum.
     */
    @Test
    public void testSentenceToStringWithChecksum() {
        BBMSentence bbmSentence = new BBMSentence(10, 1, AISChannel.A, 8, new byte[]{0x61, 0x62, 0x63, 0x64, 0x65});
        assertEquals("!AIBBM,10,1,,1,8,abcde,2*03", bbmSentence.toStringWithChecksum());
    }

}