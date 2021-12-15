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

import org.grad.eNav.vdesCtrl.models.vdes.ais.messages.AISMessage6;
import org.grad.eNav.vdesCtrl.utils.StringBinUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AISMessage6Test {

    /**
     * Test that the AIS Message 6 will be constructed by default with
     * empty parameters.
     */
    @Test
    public void testEmptyConstructor() {
        AISMessage6 aisMessage6 = new AISMessage6();
        assertNull(aisMessage6.getMmsi());
        assertNull(aisMessage6.getDestMmsi());
        assertEquals(0, aisMessage6.getMessage().length);
    }

    /**
     * Test that the AIS Message 6 can be constructed with initialised
     * parameters.
     */
    @Test
    public void testConstructor() {
        AISMessage6 aisMessage6 = new AISMessage6(123456789, 987654321, new byte[]{0b0, 0b1, 0b0, 0b1});
        assertEquals(123456789, aisMessage6.getMmsi());
        assertEquals(987654321, aisMessage6.getDestMmsi());
        assertEquals(4, aisMessage6.getMessage().length);
    }

    /**
     * Test that we can correctly construct the NMEA message representation.
     * Note that we need to use the 6bit conversion to do so.
     */
    @Test
    public void testGetBinaryMessage6bit() {
        AISMessage6 aisMessage6 = new AISMessage6(123456789, 987654321, new byte[]{0b0, 0b1, 0b0, 0b1});
        assertEquals("61mg=5CcNJ;404400@01", new String(aisMessage6.getBinaryMessage(true)));
    }

    /**
     * Test that we can correctly construct the NMEA message representation.
     * Note that when using the normal 8 bit conversion, the result should
     * is not the correct string representation but can be used anywhere else.
     */
    @Test
    public void testGetBinaryMessage8bit() {
        AISMessage6 aisMessage6 = new AISMessage6(123456789, 987654321, new byte[]{0b0, 0b1, 0b0, 0b1});
        byte[] bin = StringBinUtils.convertBinaryStringToBytes("000110000001110101101111001101000101010011101011011110011010001011000100000000000100000100000000000000010000000000000001", false);
        byte[] result = aisMessage6.getBinaryMessage(false);
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
        AISMessage6 aisMessage6 = new AISMessage6(123456789, 987654321, new byte[]{0b0, 0b1, 0b0, 0b1});
        String binString = "000110000001110101101111001101000101010011101011011110011010001011000100000000000100000100000000000000010000000000000001";
        assertEquals(binString, aisMessage6.getBinaryMessageString());
    }

}