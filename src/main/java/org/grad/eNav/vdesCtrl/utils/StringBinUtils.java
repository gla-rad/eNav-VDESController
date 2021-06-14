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
     * Converts and integer (or a character as well) into its binary
     * representation and returns that in a string format.
     *
     * @param c The integer or character to be converted
     * @param padding the right padding with zeros required
     * @return the binary representation as a string
     */
    public static String convertIntToBinary(int c, int padding) {
        return padLeft(Integer.toBinaryString(c), padding);
    }

    /**
     * Converts a string to it's binary representation.
     *
     * @param input The input to be converted
     * @param padding the right padding with zeros required
     * @return the binary representation of the string
     */
    public static String convertStringToBinary(String input, int padding, Boolean ascii_6bit) {

        StringBuilder result = new StringBuilder();
        char[] chars = Strings.nullToEmpty(input).toCharArray();
        for (char c : chars) {
            // Choose between 6 and 8 bit ASCII
            if(ascii_6bit) {
                c = String.valueOf(c).toUpperCase().charAt(0); // 6bit only allows uppercase chars
                result.append(convertIntToBinary(ASCII_VOCABULARY_6bit.indexOf(c), 6));
            } else {
                result.append(convertIntToBinary(c, 8));
            }
        }
        return padLeft(result.toString(), padding);
    }

}
