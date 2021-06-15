/*
 * Copyright (c) 2021 GLA UK Research and Development Directive
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.grad.eNav.vdesCtrl.utils;

import org.junit.Test;

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
     * Test that we can convert integers (and chars) correctly to their binary
     * string representation with 6 bit encoding.
     */
    @Test
    public void testConvertIntToBinary6bit() {
        // Test Integers
        assertEquals("110000", StringBinUtils.convertIntToBinary('0', 6, true));
        assertEquals("111000", StringBinUtils.convertIntToBinary('8', 6, true));
        assertEquals("111001", StringBinUtils.convertIntToBinary('9', 6, true));

        // Test Chars -  Lowercase
        assertEquals("000001", StringBinUtils.convertIntToBinary('a', 6, true));
        assertEquals("000010", StringBinUtils.convertIntToBinary('b', 6, true));
        assertEquals("011010", StringBinUtils.convertIntToBinary('z', 6, true));

        // Test Chars -  Uppercase
        assertEquals("000001", StringBinUtils.convertIntToBinary('A', 6, true));
        assertEquals("000010", StringBinUtils.convertIntToBinary('B', 6, true));
        assertEquals("011010", StringBinUtils.convertIntToBinary('Z', 6, true));
    }

    /**
     * Test that we can convert integers (and chars) correctly to their binary
     * string representation with 8 bit encoding.
     */
    @Test
    public void testConvertIntToBinary8bit() {
        // Test Integers
        assertEquals("00110000", StringBinUtils.convertIntToBinary('0', 8, false));
        assertEquals("00111000", StringBinUtils.convertIntToBinary('8', 8, false));
        assertEquals("00111001", StringBinUtils.convertIntToBinary('9', 8, false));

        // Test Chars -  Lowercase
        assertEquals("01100001", StringBinUtils.convertIntToBinary('a', 8, false));
        assertEquals("01100010", StringBinUtils.convertIntToBinary('b', 8, false));
        assertEquals("01111010", StringBinUtils.convertIntToBinary('z', 8, false));

        // Test Chars -  Uppercase
        assertEquals("01000001", StringBinUtils.convertIntToBinary('A', 8, false));
        assertEquals("01000010", StringBinUtils.convertIntToBinary('B', 8, false));
        assertEquals("01011010", StringBinUtils.convertIntToBinary('Z', 8, false));
    }

    /**
     * Test that we can correctly retrieve the ASCII character from its binary
     * value. Note that we are using 6bit ASCII so only uppercase characters
     * will be shown. Also the resulting char is not the same as the one used
     * for encoding the 6bit binary since the char representation is not based
     * on the custom 6bit vocabulary:
     *
     *    @ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^- !\"#$%&'()*+,-./0123456789:;<=>?
     *
     */
    @Test
    public void testConvertBinaryToInt6bit() {
        assertEquals('h', StringBinUtils.convertBinaryToInt("110000", true));
        assertEquals('p', StringBinUtils.convertBinaryToInt("111000", true));
        assertEquals('q', StringBinUtils.convertBinaryToInt("111001", true));

        assertEquals('1', StringBinUtils.convertBinaryToInt("000001", true));
        assertEquals('2', StringBinUtils.convertBinaryToInt("000010", true));
        assertEquals('J', StringBinUtils.convertBinaryToInt("011010", true));

        assertEquals('A', StringBinUtils.convertBinaryToInt("010001", true));
        assertEquals('B', StringBinUtils.convertBinaryToInt("010010", true));
        assertEquals('W', StringBinUtils.convertBinaryToInt("100111", true));
    }

    /**
     * Test that we can correctly retrieve the ASCII character from its binary
     * value. Note that we are using 8bit ASCII so lowercase characters will
     * also shown.
     */
    @Test
    public void testConvertBinaryToInt8bit() {
        assertEquals('0', StringBinUtils.convertBinaryToInt("00110000", false));
        assertEquals('8', StringBinUtils.convertBinaryToInt("00111000", false));
        assertEquals('9', StringBinUtils.convertBinaryToInt("00111001", false));

        assertEquals('a', StringBinUtils.convertBinaryToInt("01100001", false));
        assertEquals('b', StringBinUtils.convertBinaryToInt("01100010", false));
        assertEquals('z', StringBinUtils.convertBinaryToInt("01111010", false));

        assertEquals('A', StringBinUtils.convertBinaryToInt("01000001", false));
        assertEquals('B', StringBinUtils.convertBinaryToInt("01000010", false));
        assertEquals('Z', StringBinUtils.convertBinaryToInt("01011010", false));
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

}