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

import _int.iho.s125.gml._0.DatasetType;
import _int.iho.s125.gml._0.MemberType;
import _int.iho.s125.gml._0.S125NavAidStructureType;
import org.grad.eNav.vdesCtrl.models.domain.AtonType;
import org.grad.eNav.vdesCtrl.models.dtos.S125Node;

import javax.xml.bind.JAXBException;

/**
 * The GNURadio AIS Utility Class.
 *
 * A static utility function class that allows easily manipulation of the
 * GNURadio AIS station messages.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public class GrAisUtils {

    /**
     * The 6bit ASCII Vocabulary Definition
     */
    public static final String ASCII_VOCABULARY_6bit = "@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^- !\"#$%&'()*+,-./0123456789:;<=>?";

    /**
     * The GNURadio AIS BlackToolkit requires the AIS message sentence in a
     * binary sentence as an input. This utility function is able to generate
     * this binary message as a string, so that is can by passed on tho the
     * UDP socket GNURadio is listening to.
     *
     * @param s125Node the S125Node to construct the AIS binary message from
     * @return The AIS binary message to be transmitted through GNURadio
     * @throws JAXBException
     */
    public static String s125ToAisMsg21(S125Node s125Node) throws JAXBException {
        DatasetType dataset = S100Utils.unmarshallS125(s125Node.getContent());

        // Create a string builder to start ith
        StringBuilder aisBuilder = new StringBuilder();

        // For now only build one message per node - member position 0
        if(dataset.getMemberOrImember().size() > 0) {
            MemberType member = (MemberType) dataset.getMemberOrImember().get(0);

            // Extract the S125 Member NavAid Information
            S125NavAidStructureType navAid = (S125NavAidStructureType) member.getAbstractFeature().getValue();
            Integer mmsi = navAid.getMmsi();
            AtonType atonType = AtonType.fromString(navAid.getAtonType().value());
            String name = navAid.getFeatureName().getName();
            String nameExt = null;
            if(name.length() > 20) {
                name = navAid.getFeatureName().getName().substring(0, 20);
                nameExt = navAid.getFeatureName().getName().substring(20);
            }
            Double lat = dataset.getBoundedBy().getEnvelope().getLowerCorner().getValue().get(0);
            Double lon = dataset.getBoundedBy().getEnvelope().getLowerCorner().getValue().get(1);
            Boolean raim = navAid.isRaimFlag();
            Boolean virtual = navAid.isVatonFlag();

            // Build the string
            aisBuilder.append(convertIntToBinary(21,6)); // AIS Message 21
            aisBuilder.append(convertIntToBinary(0,2)); // Repeat Indicator
            aisBuilder.append(convertIntToBinary(mmsi, 30)); // MMSI
            aisBuilder.append(convertIntToBinary(atonType.getCode(),5)); // The AtoN Type
            aisBuilder.append(convertStringToBinary(name,120, true)); // The AtoN Name
            aisBuilder.append(convertIntToBinary(0,1)); // The Accuracy

            // Longitude/Latitude
            aisBuilder.append(convertIntToBinary(new Long(Math.round(lon*600000)).intValue(),28)); // The Longitude
            aisBuilder.append(convertIntToBinary(new Long(Math.round(lat*600000)).intValue(),27)); // The Latitude

            // Dimension/Reference of position
            aisBuilder.append(convertIntToBinary(0,9)); // The Half Length
            aisBuilder.append(convertIntToBinary(0,9)); // The Half Length
            aisBuilder.append(convertIntToBinary(0,6)); // The Half Width
            aisBuilder.append(convertIntToBinary(0,6)); // The Half Width

            // Additional info/flags
            aisBuilder.append(convertIntToBinary(0,4)); // The Fix Field
            aisBuilder.append(convertIntToBinary(60,6)); // The Time Field
            aisBuilder.append(convertIntToBinary(0,1)); // Off Position Indicator
            aisBuilder.append(convertIntToBinary(0,8)); // AtoN Status
            aisBuilder.append(convertIntToBinary(raim?1:0,1)); // RAIM Flag
            aisBuilder.append(convertIntToBinary(virtual?1:0,1)); // The Virtual Flag
            aisBuilder.append(convertIntToBinary(0,2)); // ?

            // Add the name extention is required
            if(nameExt != null) {
                aisBuilder.append(convertStringToBinary(nameExt, nameExt.length() % 8, true)); // Optional The Name Extension
            }
        }

        // Finally pad to the multiple of 6 requirement
        String aisSentence = aisBuilder.toString();
        return padRight(aisSentence, aisSentence.length() + (6 - (aisSentence.length()%6))%6);
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
        char[] chars = input.toCharArray();
        for (char c : chars) {
            // Choose between 6 and 8 bit ASCEE
            if(ascii_6bit) {
                c = String.valueOf(c).toUpperCase().charAt(0); // 6bit only allows uppercase chars
                result.append(convertIntToBinary(ASCII_VOCABULARY_6bit.indexOf(c), 6));
            } else {
                result.append(convertIntToBinary(c, 8));
            }
        }
        return padLeft(result.toString(), padding);
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
     * Right pads the provided string with zeros.
     *
     * @param s the string to be padded
     * @param n the number of padding zeros
     * @return the padded string
     */
    public static String padRight(String s, int n) {
        return String.format("%-" + n + "s", s).replace(' ', '0');
    }

    /**
     * Left pads the provided string with zeros.
     *
     * @param s the string to be padded
     * @param n the number of padding zeros
     * @return the padded string
     */
    public static String padLeft(String s, int n) {
        return String.format("%" + n + "s", s).replace(' ', '0');
    }

}
