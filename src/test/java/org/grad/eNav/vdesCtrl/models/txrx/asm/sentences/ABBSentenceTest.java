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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ABBSentenceTest {

    /**
     * Test that the ABB Sentences can be initialised correctly with the
     * default constructor.
     */
    @Test
    public void testConstructor() {
        ABBSentence abbSentence = new ABBSentence(10, 1, 123, AISChannel.A, 0, new byte[]{0x61, 0x62, 0x63, 0x64, 0x65});
        assertEquals("AI", abbSentence.getTalkerId());
        assertEquals("ABB", abbSentence.getFormatterCode());
        assertEquals(10, abbSentence.getSentencesTotal());
        assertEquals(1, abbSentence.getSentenceNum());
        assertEquals(123, abbSentence.getSequenceId());
        assertEquals(AISChannel.A, abbSentence.getChannel());
        assertTrue(abbSentence.getAsmId().isEmpty());
        assertEquals(0, abbSentence.getTransmissionFormat());
        assertEquals(5, abbSentence.getPayload().length);
        assertEquals(2, abbSentence.getNoFillBits());
    }

    /**
     * Test that the ABB Sentences can be initialised correctly with the
     * Talker ID constructor.
     */
    @Test
    public void testConstructorWithTalkerId() {
        ABBSentence abbSentence = new ABBSentence("CUSTOM", 10, 1, 123, AISChannel.A, 0, new byte[]{0x61, 0x62, 0x63, 0x64, 0x65});
        assertEquals("CUSTOM", abbSentence.getTalkerId());
        assertEquals("ABB", abbSentence.getFormatterCode());
        assertEquals(10, abbSentence.getSentencesTotal());
        assertEquals(1, abbSentence.getSentenceNum());
        assertEquals(123, abbSentence.getSequenceId());
        assertEquals(AISChannel.A, abbSentence.getChannel());
        assertTrue(abbSentence.getAsmId().isEmpty());
        assertEquals(0, abbSentence.getTransmissionFormat());
        assertEquals(5, abbSentence.getPayload().length);
        assertEquals(2, abbSentence.getNoFillBits());
    }

    /**
     * Test that we can correctly construct the string representation of the
     * sentence.
     */
    @Test
    public void testSentenceToString() {
        ABBSentence abbSentence = new ABBSentence(10, 1, 123, AISChannel.A, 0, new byte[]{0x61, 0x62, 0x63, 0x64, 0x65});
        assertEquals("!AIABB,10,01,123,,1,,0,abcde,2", abbSentence.toString());
    }

    /**
     * Test that we can correctly construct the string representation of the
     * sentence with the checksum.
     */
    @Test
    public void testSentenceToStringWithChecksum() {
        ABBSentence abbSentence = new ABBSentence(10, 1, 123, AISChannel.A, 0, new byte[]{0x61, 0x62, 0x63, 0x64, 0x65});
        assertEquals("!AIABB,10,01,123,,1,,0,abcde,2*07", abbSentence.toStringWithChecksum());
    }

}