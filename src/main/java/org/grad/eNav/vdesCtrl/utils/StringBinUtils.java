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


import com.google.common.base.Strings;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The StringBin Utility Class.
 *
 * A list of string to binary utilities for easy translations of strings onto
 * binary data. These include padding and ASCII (6/8 bit) to binary data.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public class StringBinUtils {

    /**
     * The 6bit ASCII Vocabulary Definition
     */
    public static final String ASCII_VOCABULARY_6bit = "@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^- !\"#$%&'()*+,-./0123456789:;<=>?";

    /**
     * Right pads the provided string with zeros.
     *
     * @param s the string to be padded
     * @param n the number of padding zeros
     * @return the padded string
     */
    public static String padRight(String s, int n) {
        return String.format("%" + (n > 0 ? "-"  + n : "")  + "s", Strings.nullToEmpty(s)).replace(' ', '0');
    }

    /**
     * Left pads the provided string with zeros.
     *
     * @param s the string to be padded
     * @param n the number of padding zeros
     * @return the padded string
     */
    public static String padLeft(String s, int n) {
        return String.format("%" + (n > 0 ? n : "") + "s", Strings.nullToEmpty(s)).replace(' ', '0');
    }

    /**
     * Converts an integer into its binary
     * representation and returns that in a string format.
     *
     * @param i The integer to be converted
     * @param padding the right padding with zeros required
     * @return the binary representation as a string
     */
    public static String convertIntToBinary(int i, int padding) {
        return padLeft(Integer.toBinaryString(i), padding);
    }

    /**
     * Converts a byte into its binary representation and returns that in a
     * string format.
     *
     * @param b The byte or character to be converted
     * @param padding the right padding with zeros required
     * @return the binary representation as a string
     */
    public static String convertByteToBinary(byte b, int padding) {
        return padLeft(Integer.toBinaryString(b & 0xFF), padding);
    }

    /**
     * Converts a char into its binary representation and returns that in a
     * string format. Note that this operation supports both 6bit and 8bit
     * representations.
     *
     * @param c The character to be converted
     * @param padding the right padding with zeros required
     * @return the binary representation as a string
     */
    public static String convertCharToBinary(char c, int padding, boolean ascii_6bit) {
        // Choose between 6 and 8 bit ASCII
        if(ascii_6bit) {
            char uc = String.valueOf(c).toUpperCase().charAt(0); // 6bit only allows uppercase chars
            return padLeft(Integer.toBinaryString(ASCII_VOCABULARY_6bit.indexOf(uc)), padding);
        } else {
            return padLeft(Integer.toBinaryString(c), padding);
        }
    }

    /**
     * Converts a binary string into the matching ASCII character.
     *
     * @param binaryString the binary string to be translated
     * @return the matching ASCII character
     */
    public static char convertBinaryToChar(String binaryString, boolean ascii_6bit) {
        // Sanity check
        if(Objects.isNull(binaryString) || binaryString.isEmpty() || !binaryString.matches("[01]*")) {
            return ' ';
        }

        // If OK, try to find the ASCII value of the binary string
        int asciiValue = Integer.parseInt(binaryString, 2);
        // For 6bit ASCII we need to additional value manipulation (only uppercase chars)
        if(ascii_6bit) {
            asciiValue = (asciiValue > 39 ? asciiValue + 8 : asciiValue) + 48;
        }
        return (char) asciiValue;
    }

    /**
     * Converts a string to it's binary representation. This function supports
     * both 6bit and 8bit character representations. The padding only affects
     * additional left hand side 0s if required.
     *
     * @param input The input to be converted
     * @param padding the left padding with zeros required
     * @return the binary representation of the string
     */
    public static String convertStringToBinary(String input, int padding, Boolean ascii_6bit) {

        StringBuilder result = new StringBuilder();
        char[] chars = Strings.nullToEmpty(input).toCharArray();
        for (char c : chars) {
            result.append(convertCharToBinary(c, ascii_6bit ? 6 : 8, ascii_6bit));
        }
        return padLeft(result.toString(), padding);
    }

    /**
     * Converts a binary string o zeros and ones into its byte representation.
     *
     * Note that we translate the bits onto bytes from left to right, so keeping
     * in line with how the python bitarray library works.
     *
     * https://pypi.org/project/bitarray/
     *
     * If the string does not represent a full 8bit byte array, the additional
     * zeros will be appended in the end of the string, hence the last byte
     * of the returned array.
     *
     * @param binaryString The binary string provided
     * @return the byte representation of the input binary string
     */
    public static byte[] convertBinaryStringToBytes(String binaryString) {
        // Sanity Check
        if(StringUtils.isEmpty(binaryString) || !binaryString.matches("[01]+")) {
            return null;
        }

        // Break into bit octets and translate to bytes
       return ArrayUtils.toPrimitive(Stream.of(binaryString.split("(?<=\\G.{8})"))
                .map(bits -> StringUtils.rightPad(bits, 8, '0'))
                .map(bits ->(Integer.parseInt(bits, 2)))
                .map(i -> i.byteValue())
                .collect(Collectors.toList())
                .toArray(new  Byte[]{}));
    }

}
