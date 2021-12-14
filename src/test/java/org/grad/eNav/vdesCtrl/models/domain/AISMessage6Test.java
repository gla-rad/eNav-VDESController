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
 */

package org.grad.eNav.vdesCtrl.models.domain;

import org.grad.eNav.vdesCtrl.models.txrx.ais.messages.AISMessage6;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AISMessage6Test {

    /**
     * Test that the GR-AIS Message 6 will be constructed by default with
     * empty parameters.
     */
    @Test
    public void testEmptyConstructor() {
        AISMessage6 msgParams = new AISMessage6();
        assertNull(msgParams.getMmsi());
        assertNull(msgParams.getDestMmsi());
        assertEquals(0, msgParams.getMessage().length);
    }

    /**
     * Test that the GR-AIS Message 6 can be constructed with initialised
     * parameters.
     */
    @Test
    public void testConstructor() {
        AISMessage6 msgParams = new AISMessage6(123456789, 987654321, new byte[]{0b0, 0b1, 0b0, 0b1});
        assertEquals(123456789, msgParams.getMmsi());
        assertEquals(987654321, msgParams.getDestMmsi());
        assertEquals(4, msgParams.getMessage().length);
    }

}