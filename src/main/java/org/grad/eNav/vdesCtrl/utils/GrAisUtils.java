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

import com.google.common.base.Strings;
import com.google.common.primitives.Longs;
import org.grad.eNav.vdesCtrl.models.vdes.ais.messages.AISMessage21;
import org.grad.eNav.vdesCtrl.models.vdes.ais.messages.AISMessage6;
import org.grad.eNav.vdesCtrl.models.vdes.ais.messages.AISMessage8;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
     * Encodes the provided message string into an 8bit binary string and
     * generates the AIS binary message 6 to be send as a broadcast from the
     * defined MMSI source to the specified destination MMSI.
     *
     * @param aisMessage6 the GR-AIS Message 6 parameters to construct the AIS binary message from
     * @return the encoded AIS message 6
     */
    public static String encodeMsg6(AISMessage6 aisMessage6) {
        // Create a string builder to start with
        StringBuilder aisBuilder = new StringBuilder();

        // Build the AIS message string
        aisBuilder.append(StringBinUtils.convertIntToBinary(6,6)) // AIS Message 6
                .append(StringBinUtils.convertIntToBinary(0,2)) // Repeat Indicator
                .append(StringBinUtils.convertIntToBinary(aisMessage6.getMmsi(), 30)) // MMSI
                .append(StringBinUtils.convertIntToBinary(0, 2)) // Sequence
                .append(StringBinUtils.convertIntToBinary(aisMessage6.getDestMmsi(), 30)) // Destination MMSI
                .append(StringBinUtils.convertIntToBinary(0, 1)) // Re-Transmit
                .append(StringBinUtils.convertIntToBinary(0, 1)) // Spare
                .append(StringBinUtils.convertIntToBinary(1, 10)) // Designated area code
                .append(StringBinUtils.convertIntToBinary(1, 6)); // Functional ID

        // Add the message binary content
        for(byte b: aisMessage6.getMessage()) {
            aisBuilder.append(StringBinUtils.convertByteToBinary(b, 8));
        }

        // Finally pad to the multiple of 6 requirement
        String aisSentence = aisBuilder.toString();
        return StringBinUtils.padRight(aisSentence, aisSentence.length() + (6 - (aisSentence.length()%6))%6);
    }

    /**
     * Encodes the provided message string into an 8bit binary string and
     * generates the AIS binary message 8 to be send as a broadcast from the
     * defined MMSI source.
     *
     * @param aisMessage8 the GR-AIS Message 8 parameters to construct the AIS binary message from
     * @return the encoded AIS message 8
     */
    public static String encodeMsg8(AISMessage8 aisMessage8) {
        // Create a string builder to start with
        StringBuilder aisBuilder = new StringBuilder();

        // Build the AIS message string
        aisBuilder.append(StringBinUtils.convertIntToBinary(8,6)) // AIS Message 8
                .append(StringBinUtils.convertIntToBinary(0,2)) // Repeat Indicator
                .append(StringBinUtils.convertIntToBinary(aisMessage8.getMmsi(), 30)) // MMSI
                .append(StringBinUtils.convertIntToBinary(0, 2)) // Spare
                .append(StringBinUtils.convertIntToBinary(1, 10)) // Designated area code
                .append(StringBinUtils.convertIntToBinary(1, 6)); // Functional ID

        // Add the message binary content
        for(byte b: aisMessage8.getMessage()) {
            aisBuilder.append(StringBinUtils.convertByteToBinary(b, 8));
        }

        // Finally, pad to the multiple of 6 requirement
        String aisSentence = aisBuilder.toString();
        return StringBinUtils.padRight(aisSentence, aisSentence.length() + (6 - (aisSentence.length()%6))%6);
    }

    /**
     * The GNURadio AIS BlackToolkit requires the AIS message sentence in a
     * binary sentence as an input. This utility function is able to generate
     * the AIS binary message 21 as a string, so that is can by passed on to the
     * UDP socket GNURadio is listening to.
     *
     * @param aisMessage21 the GR-AIS Message 21 parameters to construct the AIS binary message from
     * @return The AIS binary message to be transmitted through GNURadio
     */
    public static String encodeMsg21(AISMessage21 aisMessage21) {
        // Create a string builder to start with
        StringBuilder aisBuilder = new StringBuilder();

        // Quickly calculate the extra specific message 21 information required
        String name = aisMessage21.getName();
        String nameExt = "";
        if(aisMessage21.getName().length() > 20) {
            name = aisMessage21.getName().substring(0, 20);
            nameExt = aisMessage21.getName().substring(20);
        }
        int halfLength = aisMessage21.getVaton() ? 0 : Math.round((float)(aisMessage21.getLength()/2));
        int halfWidth = aisMessage21.getVaton() ? 0 : Math.round((float)(aisMessage21.getWidth()/2));

        // Build the string
        aisBuilder.append(StringBinUtils.convertIntToBinary(21,6)) // AIS Message 21
                .append(StringBinUtils.convertIntToBinary(0,2)) // Repeat Indicator
                .append(StringBinUtils.convertIntToBinary(aisMessage21.getMmsi(), 30)) // MMSI
                .append(StringBinUtils.convertIntToBinary(aisMessage21.getAtonType().getCode(),5)) // The AtoN Type
                .append(StringBinUtils.convertStringToBinary(String.format("%" + (-20) + "s", name),120, true)) // The AtoN Name
                .append(StringBinUtils.convertIntToBinary(0,1)) // The Accuracy
                // Longitude/Latitude
                .append(StringBinUtils.convertIntToBinary(Long.valueOf(Math.round(aisMessage21.getLongitude()*600000)).intValue(),28)) // The Longitude
                .append(StringBinUtils.convertIntToBinary(Long.valueOf(Math.round(aisMessage21.getLatitude()*600000)).intValue(),27)) // The Latitude
                // Dimension/Reference of position
                .append(StringBinUtils.convertIntToBinary(halfLength,9)) // The Half Length
                .append(StringBinUtils.convertIntToBinary(halfLength,9)) // The Half Length
                .append(StringBinUtils.convertIntToBinary(halfWidth,6)) // The Half Width
                .append(StringBinUtils.convertIntToBinary(halfWidth,6)) // The Half Width
                // Additional info/flags
                .append(StringBinUtils.convertIntToBinary(0,4)) // The Fix Field
                .append(StringBinUtils.convertIntToBinary(aisMessage21.getTxTimestamp().getSecond(),6)) // The Time Field
                .append(StringBinUtils.convertIntToBinary(0,1)) // Off Position Indicator
                .append(StringBinUtils.convertIntToBinary(0,8)) // AtoN Status
                .append(StringBinUtils.convertIntToBinary(aisMessage21.getRaim()?1:0,1)) // RAIM Flag
                .append(StringBinUtils.convertIntToBinary(aisMessage21.getVaton()?1:0,1)) // The Virtual AtoN Flag
                .append(StringBinUtils.convertIntToBinary(0,2)) // ?
                // Add the name extension is required
                .append(StringBinUtils.convertStringToBinary(nameExt, nameExt.length() % 6, true));

        // Finally, pad to the multiple of 6 requirement
        String aisSentence = aisBuilder.toString();
        return StringBinUtils.padRight(aisSentence, aisSentence.length() + (6 - (aisSentence.length()%6))%6);
    }

    /**
     * This function translates the AIS message binary content string
     * to a byte array and stamps it with the provided timestamp. This
     * can be used to easily generate am SHA-256 hash for signing.
     *
     * @param aisBinaryMessage the AIS binary message to be stamped
     * @param timestamp the timestamp to append to the content
     * @return the NMEA sentence signature
     * @throws IOException when the message content operation fails
     */
    public static byte[] getStampedAISMessageHash(byte[] aisBinaryMessage, long timestamp) throws IOException, NoSuchAlgorithmException {
        // Combine the AIS message and the timestamp
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write(aisBinaryMessage);
        outputStream.write(Longs.toByteArray(timestamp));
        byte[] stampedAisMessage = outputStream.toByteArray();

        // Return the SHA-256 hash of the stamped message
        return MessageDigest.getInstance("SHA-256").digest(stampedAisMessage);
    }

    /**
     * This utility function generate the IEC 61162-1 style checksum as a
     * string. The IEC 61162-1 style checksum is computed on the entire
     * sentence including the AIVDM/AIVDO tag but excluding the leading "!".
     *
     * The checksum is merely a byte-by-byte XOR of the sentence.
     *
     * @param sentence the sentence to generate the checksum for
     * @return the generated sentence checksum
     */
    public static String calculateIECChecksum(String sentence) {
        // Remove the initial "!" character if found
        String clearedSentence =  Strings.nullToEmpty(sentence).replaceAll("^!", "");
        int sum = 0;
        char[] chars = clearedSentence.toCharArray();
        for(char c : chars) {
            sum ^= c;
        }
        return String.format("%02X", sum & 0xFFFFF);
    }

}
