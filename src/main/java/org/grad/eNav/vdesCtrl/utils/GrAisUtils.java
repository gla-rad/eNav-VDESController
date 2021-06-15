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

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.grad.eNav.vdesCtrl.models.domain.GrAisMsg21Params;
import org.grad.eNav.vdesCtrl.models.domain.GrAisMsg6Params;
import org.grad.eNav.vdesCtrl.models.domain.GrAisMsg8Params;
import org.grad.eNav.vdesCtrl.models.domain.NMEAChannel;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Objects;

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
     * @param msgParams the GR-AIS Message 6 parameters to construct the AIS binary message from
     * @return the encoded AIS message 6
     */
    public static String encodeMsg6(GrAisMsg6Params msgParams) {
        // Create a string builder to start with
        StringBuilder aisBuilder = new StringBuilder();

        // Build the AIS message string
        aisBuilder.append(StringBinUtils.convertIntToBinary(8,6)) // AIS Message 8
                .append(StringBinUtils.convertIntToBinary(0,2)) // Repeat Indicator
                .append(StringBinUtils.convertIntToBinary(msgParams.getMmsi(), 30)) // MMSI
                .append(StringBinUtils.convertIntToBinary(0, 2)) // Sequence
                .append(StringBinUtils.convertIntToBinary(msgParams.getDestMmsi(), 30)) // Destination MMSI
                .append(StringBinUtils.convertIntToBinary(0, 1)) // Re-Transmit
                .append(StringBinUtils.convertIntToBinary(0, 1)) // Spare
                .append(StringBinUtils.convertIntToBinary(1, 10)) // Designated area code
                .append(StringBinUtils.convertIntToBinary(1, 6)); // Functional ID

        // Add the message binary content
        for(byte b: msgParams.getMessage()) {
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
     * @param msgParams the GR-AIS Message 8 parameters to construct the AIS binary message from
     * @return the encoded AIS message 8
     */
    public static String encodeMsg8(GrAisMsg8Params msgParams) {
        // Create a string builder to start with
        StringBuilder aisBuilder = new StringBuilder();

        // Build the AIS message string
        aisBuilder.append(StringBinUtils.convertIntToBinary(8,6)) // AIS Message 8
                .append(StringBinUtils.convertIntToBinary(0,2)) // Repeat Indicator
                .append(StringBinUtils.convertIntToBinary(msgParams.getMmsi(), 30)) // MMSI
                .append(StringBinUtils.convertIntToBinary(0, 2)) // Spare
                .append(StringBinUtils.convertIntToBinary(1, 10)) // Designated area code
                .append(StringBinUtils.convertIntToBinary(1, 6)); // Functional ID

        // Add the message binary content
        for(byte b: msgParams.getMessage()) {
            aisBuilder.append(StringBinUtils.convertByteToBinary(b, 8));
        }

        // Finally pad to the multiple of 6 requirement
        String aisSentence = aisBuilder.toString();
        return StringBinUtils.padRight(aisSentence, aisSentence.length() + (6 - (aisSentence.length()%6))%6);
    }

    /**
     * The GNURadio AIS BlackToolkit requires the AIS message sentence in a
     * binary sentence as an input. This utility function is able to generate
     * the AIS binary message 21 as a string, so that is can by passed on to the
     * UDP socket GNURadio is listening to.
     *
     * @param msgParams the GR-AIS Message 21 parameters to construct the AIS binary message from
     * @return The AIS binary message to be transmitted through GNURadio
     */
    public static String encodeMsg21(GrAisMsg21Params msgParams) {
        // Create a string builder to start with
        StringBuilder aisBuilder = new StringBuilder();

        // Quickly calculate the extra specific message 21 information required
        String name = msgParams.getName();
        String nameExt = "";
        if(msgParams.getName().length() > 20) {
            name = msgParams.getName().substring(0, 20);
            nameExt = msgParams.getName().substring(20);
        }
        int halfLength = msgParams.getVaton() ? 0 : Math.round((float)(msgParams.getLength()/2));
        int halfWidth = msgParams.getVaton() ? 0 : Math.round((float)(msgParams.getWidth()/2));

        // Build the string
        aisBuilder.append(StringBinUtils.convertIntToBinary(21,6)) // AIS Message 21
                .append(StringBinUtils.convertIntToBinary(0,2)) // Repeat Indicator
                .append(StringBinUtils.convertIntToBinary(msgParams.getMmsi(), 30)) // MMSI
                .append(StringBinUtils.convertIntToBinary(msgParams.getAtonType().getCode(),5)) // The AtoN Type
                .append(StringBinUtils.convertStringToBinary(name,120, true)) // The AtoN Name
                .append(StringBinUtils.convertIntToBinary(0,1)) // The Accuracy
                // Longitude/Latitude
                .append(StringBinUtils.convertIntToBinary(new Long(Math.round(msgParams.getLongitude()*600000)).intValue(),28)) // The Longitude
                .append(StringBinUtils.convertIntToBinary(new Long(Math.round(msgParams.getLatitude()*600000)).intValue(),27)) // The Latitude
                // Dimension/Reference of position
                .append(StringBinUtils.convertIntToBinary(halfLength,9)) // The Half Length
                .append(StringBinUtils.convertIntToBinary(halfLength,9)) // The Half Length
                .append(StringBinUtils.convertIntToBinary(halfWidth,6)) // The Half Width
                .append(StringBinUtils.convertIntToBinary(halfWidth,6)) // The Half Width
                // Additional info/flags
                .append(StringBinUtils.convertIntToBinary(0,4)) // The Fix Field
                .append(StringBinUtils.convertIntToBinary(60,6)) // The Time Field
                .append(StringBinUtils.convertIntToBinary(0,1)) // Off Position Indicator
                .append(StringBinUtils.convertIntToBinary(0,8)) // AtoN Status
                .append(StringBinUtils.convertIntToBinary(msgParams.getRaim()?1:0,1)) // RAIM Flag
                .append(StringBinUtils.convertIntToBinary(msgParams.getVaton()?1:0,1)) // The Virtual AtoN Flag
                .append(StringBinUtils.convertIntToBinary(0,2)) // ?
                // Add the name extension is required
                .append(StringBinUtils.convertStringToBinary(nameExt, nameExt.length() % 6, true));

        // Finally pad to the multiple of 6 requirement
        String aisSentence = aisBuilder.toString();
        return StringBinUtils.padRight(aisSentence, aisSentence.length() + (6 - (aisSentence.length()%6))%6);
    }

    /**
     * Based on the provided binary message it will generate the NMEA sentence
     * for it by splitting it every 6 bits and translating it to the respective
     * ASCII character.
     *
     * @param binaryMsg the binary message to be translated
     * @param enableNMEA whether to enable the NMEA
     * @param nmeaChannel the NMEA channel to be used
     * @return the generated NMEA sentence for the binary message
     */
    public static String generateNMEASentence(String binaryMsg, boolean enableNMEA, NMEAChannel nmeaChannel) {
        //Sanity Check
        if(Objects.isNull(binaryMsg) || binaryMsg.isEmpty()) {
            return "";
        }

        // Create a string builder to start with
        StringBuilder aisBuilder = new StringBuilder()
                .append("!AIVDM,1,1,,")
                .append(nmeaChannel.getChannel())
                .append(",");

        // Split the input binary message every 6 bits
        Splitter.fixedLength(6).split(binaryMsg).iterator().forEachRemaining(piece ->
            aisBuilder.append(StringBinUtils.convertBinaryToChar(piece, true))
        );

        // Append the checksum and close the sentence
        if(enableNMEA) {
            aisBuilder.append(",0");
            String tempSentence = aisBuilder.toString(); // Check out the sentence temporarily
            aisBuilder.append("*").append(calculateNMEAChecksum(tempSentence));
        }

        // Return the generated NMEA sentence
        return aisBuilder.toString();
    }

    /**
     * This functions accepts an NMEA sentence (careful, we do NOT check
     * whether the sentence is actually valid!) and generates a signature for
     * it based on a predefined OpenSSL key.
     *
     * @param nmeaSentence the NMEA sentence to be signed
     * @param timestamp the timestamp to append to the sentence
     * @return the NMEA sentence signature
     * @throws NoSuchAlgorithmException when the cryptographic algorithm is not found
     * @throws IOException when the key resource file is not found
     * @throws InvalidKeySpecException when the loaded key specification is invalid
     * @throws InvalidKeyException when the loaded key is invalid
     * @throws SignatureException when the signature operation fails
     */
    public static byte[] getNMEASentenceSignature(String nmeaSentence, long timestamp) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        // Create the signature parameters
        String stampedNmeaSentence = nmeaSentence + timestamp;
        byte[] stampedNmeaSentenceHashed = MessageDigest.getInstance("SHA-256")
                .digest(stampedNmeaSentence.getBytes());

        // Load the private key to sign with
        ECPrivateKey privateKey = CryptoUtils.readECPrivateKey("classpath:CorkHoleTest-PrivateKeyPair.pem");

        // Create the signature
        Signature sign = Signature.getInstance("SHA256withECDSA");
        sign.initSign(privateKey);
        sign.update(stampedNmeaSentenceHashed);

        // Return the result
        return sign.sign();
    }

    /**
     * This utility function generate the NMEA sentence checksum as a string.
     * The NMEA checksum is computed on the entire sentence including the
     * AIVDM/AIVDO tag but excluding the leading "!".
     * The checksum is merely a bybe-by-byte XOR of the sentence.
     *
     * @param nmeaSentence the NMEA sentence to generate the checksum for
     * @return the generated NMEA sentence checksum
     */
    public static String calculateNMEAChecksum(String nmeaSentence) {
        // Remove the initial "!" character if found
        String sentence =  Strings.nullToEmpty(nmeaSentence).replaceAll("^!", "");
        int sum = 0;
        char[] chars = sentence.toCharArray();
        for(char c : chars) {
            sum ^= c;
        }
        return String.format("%02X", sum & 0xFFFFF);
    }

}
