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

package org.grad.eNav.vdesCtrl.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StringBinUtilsTest {

    /**
     * Test that we can correctly pad a binary string on the right-hand side.
     */
    @Test
    public void testPadRight() {
        assertEquals("", StringBinUtils.padRight(null, 0));
        assertEquals("00000", StringBinUtils.padRight(null, 5));
        assertEquals("00000", StringBinUtils.padRight("", 5));
        assertEquals("11100", StringBinUtils.padRight("111", 5));
        assertEquals("11111", StringBinUtils.padRight("11111", 5));
        assertEquals("1111100000", StringBinUtils.padRight("11111", 10));
    }

    /**
     * Test that we can correctly pad a binary string on the right-hand side.
     */
    @Test
    public void testPadLeft() {
        assertEquals("", StringBinUtils.padRight(null, 0));
        assertEquals("00000", StringBinUtils.padLeft(null, 5));
        assertEquals("00000", StringBinUtils.padLeft("", 5));
        assertEquals("00111", StringBinUtils.padLeft("111", 5));
        assertEquals("11111", StringBinUtils.padLeft("11111", 5));
        assertEquals("0000011111", StringBinUtils.padLeft("11111", 10));
    }

    /**
     * Test that we can convert integers correctly to their binary string
     * representation with 6 bit encoding.
     */
    @Test
    public void testConvertIntToBinary() {
        // Test Integers
        assertEquals("00000000", StringBinUtils.convertIntToBinary(0, 8));
        assertEquals("00000001", StringBinUtils.convertIntToBinary(1, 8));
        assertEquals("00000010", StringBinUtils.convertIntToBinary(2, 8));
        assertEquals("00000011", StringBinUtils.convertIntToBinary(3, 8));
        assertEquals("00000100", StringBinUtils.convertIntToBinary(4, 8));
        assertEquals("00000101", StringBinUtils.convertIntToBinary(5, 8));
        assertEquals("00000110", StringBinUtils.convertIntToBinary(6, 8));
        assertEquals("00000111", StringBinUtils.convertIntToBinary(7, 8));
        assertEquals("00001000", StringBinUtils.convertIntToBinary(8, 8));
        assertEquals("00001001", StringBinUtils.convertIntToBinary(9, 8));
    }

    /**
     * Test that we can convert bytes correctly to their binary string
     * representation with 6 bit encoding.
     */
    @Test
    public void testConvertByteToBinary() {
        // Test Integers
        assertEquals("00000000", StringBinUtils.convertByteToBinary((byte)0x0, 8));
        assertEquals("00000001", StringBinUtils.convertByteToBinary((byte)0x1, 8));
        assertEquals("00000010", StringBinUtils.convertByteToBinary((byte)0x2, 8));
        assertEquals("00000011", StringBinUtils.convertByteToBinary((byte)0x3, 8));
        assertEquals("00000100", StringBinUtils.convertByteToBinary((byte)0x4, 8));
        assertEquals("00000101", StringBinUtils.convertByteToBinary((byte)0x5, 8));
        assertEquals("00000110", StringBinUtils.convertByteToBinary((byte)0x6, 8));
        assertEquals("00000111", StringBinUtils.convertByteToBinary((byte)0x7, 8));
        assertEquals("00001000", StringBinUtils.convertByteToBinary((byte)0x8, 8));
        assertEquals("00001001", StringBinUtils.convertByteToBinary((byte)0x9, 8));
        assertEquals("00001111", StringBinUtils.convertByteToBinary((byte)0xF, 8));
        assertEquals("11111111", StringBinUtils.convertByteToBinary((byte)0xFF, 8));
    }

    /**
     * Test that we can convert chars correctly to their binary string
     * representation with 6 bit encoding.
     */
    @Test
    public void testConvertCharToBinary6bit() {
        // Test Integers
        assertEquals("110000", StringBinUtils.convertCharToBinary('0', 6, true));
        assertEquals("111000", StringBinUtils.convertCharToBinary('8', 6, true));
        assertEquals("111001", StringBinUtils.convertCharToBinary('9', 6, true));

        // Test Chars -  Lowercase
        assertEquals("000001", StringBinUtils.convertCharToBinary('a', 6, true));
        assertEquals("000010", StringBinUtils.convertCharToBinary('b', 6, true));
        assertEquals("011010", StringBinUtils.convertCharToBinary('z', 6, true));

        // Test Chars -  Uppercase
        assertEquals("000001", StringBinUtils.convertCharToBinary('A', 6, true));
        assertEquals("000010", StringBinUtils.convertCharToBinary('B', 6, true));
        assertEquals("011010", StringBinUtils.convertCharToBinary('Z', 6, true));
    }

    /**
     * Test that we can convert integers (and chars) correctly to their binary
     * string representation with 8 bit encoding.
     */
    @Test
    public void testConvertCharToBinary8bit() {
        // Test Integers
        assertEquals("00110000", StringBinUtils.convertCharToBinary('0', 8, false));
        assertEquals("00111000", StringBinUtils.convertCharToBinary('8', 8, false));
        assertEquals("00111001", StringBinUtils.convertCharToBinary('9', 8, false));

        // Test Chars -  Lowercase
        assertEquals("01100001", StringBinUtils.convertCharToBinary('a', 8, false));
        assertEquals("01100010", StringBinUtils.convertCharToBinary('b', 8, false));
        assertEquals("01111010", StringBinUtils.convertCharToBinary('z', 8, false));

        // Test Chars -  Uppercase
        assertEquals("01000001", StringBinUtils.convertCharToBinary('A', 8, false));
        assertEquals("01000010", StringBinUtils.convertCharToBinary('B', 8, false));
        assertEquals("01011010", StringBinUtils.convertCharToBinary('Z', 8, false));
    }

    /**
     * Test that we can convert a whole string into binary data correctly. For
     * this operation we are going to use the 6bit character representation.
     */
    @Test
    public void testConvertStringToBinary6bit() {
        assertEquals("0000000000", StringBinUtils.convertStringToBinary(null, 10, true));
        assertEquals("0000000000", StringBinUtils.convertStringToBinary("", 10, true));
        assertEquals("001000 000101 001100 001100 001111 100000 010111 001111 010010 001100 000100".replace(" ", ""), StringBinUtils.convertStringToBinary("Hello World", 10, true));
    }

    /**
     * Test that we can convert a whole string into binary data correctly. For
     * this operation we are going to use the 8bit character representation.
     */
    @Test
    public void testConvertStringToBinary8bit() {
        assertEquals("0000000000", StringBinUtils.convertStringToBinary(null, 10, false));
        assertEquals("0000000000", StringBinUtils.convertStringToBinary("", 10, false));
        assertEquals("01001000 01100101 01101100 01101100 01101111 00100000 01010111 01101111 01110010 01101100 01100100".replace(" ", ""), StringBinUtils.convertStringToBinary("Hello World", 10, false));
    }

    /**
     * Test that we can correctly retrieve the ASCII character from its binary
     * value. Note that we are using 6bit ASCII so only uppercase characters
     * will be shown. Also the resulting char is not the same as the one used
     * for encoding the 6bit binary since the char representation is not based
     * on the custom 6bit vocabulary:
     *
     * "@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^- !\"#$%&'()*+,-./0123456789:;<=>?"
     *
     */
    @Test
    public void testConvertBinaryToChar6bit() {
        assertEquals('h', StringBinUtils.convertBinaryToChar("110000", true));
        assertEquals('p', StringBinUtils.convertBinaryToChar("111000", true));
        assertEquals('q', StringBinUtils.convertBinaryToChar("111001", true));

        assertEquals('1', StringBinUtils.convertBinaryToChar("000001", true));
        assertEquals('2', StringBinUtils.convertBinaryToChar("000010", true));
        assertEquals('J', StringBinUtils.convertBinaryToChar("011010", true));

        assertEquals('A', StringBinUtils.convertBinaryToChar("010001", true));
        assertEquals('B', StringBinUtils.convertBinaryToChar("010010", true));
        assertEquals('W', StringBinUtils.convertBinaryToChar("100111", true));
    }

    /**
     * Test that we can correctly retrieve the ASCII character from its binary
     * value. Note that we are using 8bit ASCII so lowercase characters will
     * also shown.
     */
    @Test
    public void testConvertBinaryToChar8bit() {
        assertEquals('0', StringBinUtils.convertBinaryToChar("00110000", false));
        assertEquals('8', StringBinUtils.convertBinaryToChar("00111000", false));
        assertEquals('9', StringBinUtils.convertBinaryToChar("00111001", false));

        assertEquals('a', StringBinUtils.convertBinaryToChar("01100001", false));
        assertEquals('b', StringBinUtils.convertBinaryToChar("01100010", false));
        assertEquals('z', StringBinUtils.convertBinaryToChar("01111010", false));

        assertEquals('A', StringBinUtils.convertBinaryToChar("01000001", false));
        assertEquals('B', StringBinUtils.convertBinaryToChar("01000010", false));
        assertEquals('Z', StringBinUtils.convertBinaryToChar("01011010", false));
    }

    /**
     * Test that we can correctly convert a binary string of zeros and ones
     * into its 8bit byte representation.
     */
    @Test
    public void testConvertBinaryStringToBytes8bit() {
        // Sanity Assertions
        assertNull(StringBinUtils.convertBinaryStringToBytes(null, false));
        assertNull(StringBinUtils.convertBinaryStringToBytes("", false));
        assertNull(StringBinUtils.convertBinaryStringToBytes("ThisIsNotValid", false));

        // Single bytes
        byte[] result1 = StringBinUtils.convertBinaryStringToBytes("00000000", false);
        assertEquals(1, result1.length);
        assertEquals((byte)0x00, result1[0]);
        byte[] result2 = StringBinUtils.convertBinaryStringToBytes("00000001", false);
        assertEquals(1, result2.length);
        assertEquals((byte)0x01, result2[0]);
        byte[] result3 = StringBinUtils.convertBinaryStringToBytes("00001001", false);
        assertEquals(1, result3.length);
        assertEquals((byte)0x09, result3[0]);
        byte[] result4 = StringBinUtils.convertBinaryStringToBytes("00001010", false);
        assertEquals(1, result4.length);
        assertEquals((byte)0x0A, result4[0]);

        // Multiple bytes
        byte[] result5 = StringBinUtils.convertBinaryStringToBytes("0000000100000001", false);
        assertEquals(2, result5.length);
        assertEquals((byte)0x01, result5[0]);
        assertEquals((byte)0x01, result5[1]);
        byte[] result6 = StringBinUtils.convertBinaryStringToBytes("0000101100000001", false);
        assertEquals(2, result6.length);
        assertEquals((byte)0x0B, result6[0]);
        assertEquals((byte)0x01, result6[1]);
        byte[] result7 = StringBinUtils.convertBinaryStringToBytes("111111110000101100000001", false);
        assertEquals(3, result7.length);
        assertEquals((byte)0xFF, result7[0]);
        assertEquals((byte)0x0B, result7[1]);
        assertEquals((byte)0x01, result7[2]);
        byte[] result8 = StringBinUtils.convertBinaryStringToBytes("10000001111111110000101100000001", false);
        assertEquals(4, result8.length);
        assertEquals((byte)0x81, result8[0]);
        assertEquals((byte)0xFF, result8[1]);
        assertEquals((byte)0x0B, result8[2]);
        assertEquals((byte)0x01, result8[3]);

        // Also test a string is not a complete 8bit byte string
        byte[] resultx = StringBinUtils.convertBinaryStringToBytes("0101010000011101001", false);
        assertEquals(3, resultx.length);
        assertEquals((byte)0x54, resultx[0]);
        assertEquals((byte)0x1D, resultx[1]);
        assertEquals((byte)0x20, resultx[2]);
    }

    /**
     * Test that we can correctly convert a binary string of zeros and ones
     * into its 6bit byte representation.
     */
    @Test
    public void testConvertBinaryStringToBytes6bit() {
        // Sanity Assertions
        assertNull(StringBinUtils.convertBinaryStringToBytes(null, true));
        assertNull(StringBinUtils.convertBinaryStringToBytes("", true));
        assertNull(StringBinUtils.convertBinaryStringToBytes("ThisIsNotValid", true));

        // Single bytes
        byte[] result1 = StringBinUtils.convertBinaryStringToBytes("000000", true);
        assertEquals(1, result1.length);
        assertEquals('0', result1[0]);
        byte[] result2 = StringBinUtils.convertBinaryStringToBytes("000001", true);
        assertEquals(1, result2.length);
        assertEquals('1', result2[0]);
        byte[] result3 = StringBinUtils.convertBinaryStringToBytes("001001", true);
        assertEquals(1, result3.length);
        assertEquals('9', result3[0]);
        byte[] result4 = StringBinUtils.convertBinaryStringToBytes("001010", true);
        assertEquals(1, result4.length);
        assertEquals(':', result4[0]);

        // Multiple bytes
        byte[] result5 = StringBinUtils.convertBinaryStringToBytes("000001000001", true);
        assertEquals(2, result5.length);
        assertEquals('1', result5[0]);
        assertEquals('1', result5[1]);
        byte[] result6 = StringBinUtils.convertBinaryStringToBytes("001011000001", true);
        assertEquals(2, result6.length);
        assertEquals(';', result6[0]);
        assertEquals('1', result6[1]);
        byte[] result7 = StringBinUtils.convertBinaryStringToBytes("111111001011000001", true);
        assertEquals(3, result7.length);
        assertEquals('w', result7[0]);
        assertEquals(';', result7[1]);
        assertEquals('1', result7[2]);
        byte[] result8 = StringBinUtils.convertBinaryStringToBytes("100001111111001011000001", true);
        assertEquals(4, result8.length);
        assertEquals('Q', result8[0]);
        assertEquals('w', result8[1]);
        assertEquals(';', result8[2]);
        assertEquals('1', result8[3]);

        // Also test a string is not a complete 6bit byte string
        byte[] resultx = StringBinUtils.convertBinaryStringToBytes("0101010000011", true);
        assertEquals(3, resultx.length);
        assertEquals('E', resultx[0]);
        assertEquals('1', resultx[1]);
        assertEquals('P', resultx[2]);
    }

}