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

package org.grad.eNav.vdesCtrl.models.txrx.iec61162_450;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IEC61162_450MessageTest {

    /**
     * Test that the IEC 61162-450 Messages can be initialised correctly with
     * the default constructor.
     */
    @Test
    public void testConstructor() {
        IEC61162_450Message message = new IEC61162_450Message(1, 2,3 , "source", "sentence");
        assertEquals(1, message.getSentencesTotal());
        assertEquals(2, message.getSentenceNum());
        assertEquals(3, message.getGroupId());
        assertEquals("source", message.getSourceId());
        assertEquals("sentence", message.getSentence());
    }

    /**
     * Test that we can correctly construct the string representation of the
     * IEC 61162-450 message.
     */
    @Test
    public void testMessageToString() {
        IEC61162_450Message message = new IEC61162_450Message(1, 2,3 , "source", "sentence");
        assertEquals("\\g:1-2-3,s:source*15\\sentence", message.toString());
    }

}