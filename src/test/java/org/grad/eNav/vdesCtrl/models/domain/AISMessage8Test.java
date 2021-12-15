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

import org.grad.eNav.vdesCtrl.models.vdes.ais.messages.AISMessage8;
import org.grad.eNav.vdesCtrl.utils.StringBinUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AISMessage8Test {

    /**
     * Test that the AIS Message 8 will be constructed by default with
     * empty parameters.
     */
    @Test
    public void testEmptyConstructor() {
        AISMessage8 aisMessage8 = new AISMessage8();
        assertNull(aisMessage8.getMmsi());
        assertEquals(0, aisMessage8.getMessage().length);
    }

    /**
     * Test that the AIS Message 8 can be constructed with initialised
     * parameters.
     */
    @Test
    public void testConstructor() {
        AISMessage8 aisMessage8 = new AISMessage8(123456789, new byte[]{0b0, 0b1, 0b0, 0b1});
        assertEquals(123456789, aisMessage8.getMmsi());
        assertEquals(4, aisMessage8.getMessage().length);
    }

    /**
     * Test that we can correctly construct the NMEA message representation.
     * Note that we need to use the 6bit conversion to do so.
     */
    @Test
    public void testGetBinaryMessage6bit() {
        AISMessage8 aisMessage8 = new AISMessage8(123456789, new byte[]{0b0, 0b1, 0b0, 0b1});
        assertEquals("81mg=5@0@@01004", new String(aisMessage8.getBinaryMessage(true)));
    }

    /**
     * Test that we can correctly construct the NMEA message representation.
     * Note that when using the normal 8 bit conversion, the result should
     * is not the correct string representation but can be used anywhere else.
     */
    @Test
    public void testGetBinaryMessage8bit() {
        AISMessage8 aisMessage8 = new AISMessage8(123456789, new byte[]{0b0, 0b1, 0b0, 0b1});
        byte[] bin = StringBinUtils.convertBinaryStringToBytes("001000000001110101101111001101000101010000000000010000010000000000000001000000000000000100", false);
        byte[] result = aisMessage8.getBinaryMessage(false);
        assertEquals(bin.length, result.length);
        // Check each byte separately
        for(int b=0; b<result.length; b++) {
            assertEquals(bin[b], result[b]);
        }
    }

    /**
     * Test that we can correctly construct the NMEA message binary
     * representation.
     */
    @Test
    public void testGetBinaryMessageString() {
        AISMessage8 aisMessage8 = new AISMessage8(123456789, new byte[]{0b0, 0b1, 0b0, 0b1});
        String binString = "001000000001110101101111001101000101010000000000010000010000000000000001000000000000000100";
        assertEquals(binString, aisMessage8.getBinaryMessageString());
    }

}