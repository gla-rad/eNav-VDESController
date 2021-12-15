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

class TSASentenceTest {

    /**
     * Test that the TSA Sentences can be initialised correctly with the
     * default constructor.
     */
    @Test
    public void testConstructor() {
        TSASentence tsaSentence = new TSASentence(1, AISChannel.A);
        assertEquals("AI", tsaSentence.getTalkerId());
        assertEquals("TSA", tsaSentence.getFormatterCode());
        assertTrue(tsaSentence.getUniqueId().isEmpty());
        assertEquals(1, tsaSentence.getVdmLink());
        assertEquals(AISChannel.A, tsaSentence.getChannel());
        assertTrue(tsaSentence.getUtcHHMM().isEmpty());
        assertTrue(tsaSentence.getStartSlot().isEmpty());
        assertTrue(tsaSentence.getPriority().isEmpty());
    }

    /**
     * Test that the TSA Sentences can be initialised correctly with the
     * Talker ID constructor.
     */
    @Test
    public void testConstructorWithTalkerId() {
        TSASentence tsaSentence = new TSASentence("CUSTOM",1, AISChannel.A);
        assertEquals("CUSTOM", tsaSentence.getTalkerId());
        assertEquals("TSA", tsaSentence.getFormatterCode());
        assertTrue(tsaSentence.getUniqueId().isEmpty());
        assertEquals(1, tsaSentence.getVdmLink());
        assertEquals(AISChannel.A, tsaSentence.getChannel());
        assertTrue(tsaSentence.getUtcHHMM().isEmpty());
        assertTrue(tsaSentence.getStartSlot().isEmpty());
        assertTrue(tsaSentence.getPriority().isEmpty());
    }

    /**
     * Test that we can correctly construct the string representation of the
     * sentence.
     */
    @Test
    public void testSentenceToString() {
        TSASentence tsaSentence = new TSASentence(1, AISChannel.A);
        assertEquals("!AITSA,,1,A,,,2", tsaSentence.toString());
    }

    /**
     * Test that we can correctly construct the string representation of the
     * sentence with the checksum.
     */
    @Test
    public void testSentenceToStringWithChecksum() {
        TSASentence tsaSentence = new TSASentence(1, AISChannel.A);
        assertEquals("!AITSA,,1,A,,,2*0C", tsaSentence.toStringWithChecksum());
    }

}