/*
 * Copyright (c) 2021 GLA UK Research and Development Directive
 *
 * Licensed under the Apache License, Version 2.0 (the \"License\");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an \"AS IS\" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.grad.eNav.vdesCtrl.utils;

import org.grad.eNav.vdesCtrl.models.domain.*;
import org.junit.jupiter.api.Test;

import java.security.MessageDigest;
import java.security.Signature;
import java.security.interfaces.ECPublicKey;

import static org.junit.jupiter.api.Assertions.*;

public class GrAisUtilsTest {

    // Define the test S125 Messages Expected Encoding
    public static final String AIS_MSG_6_ENCODED = "000110000001110101101111001101000101010011101011011110011010001011000100000000000100000101011000010110000101100000";
    public static final String AIS_MSG_8_ENCODED = "001000000001110101101111001101000101010000000000010000010101100001011000010110000000";
    public static final String S125_NO_1_ENCODED = "010101000001110101101111001101000101011111000000000000000000000000000000000000001010000010101001101010010000000000101010000111100111010000000111000111110000011000100000000011101001011111110000001111010101101000001110000000000000000000000000000000000000011110000000000001000000";
    public static final String S125_NO_2_ENCODED = "010101000001101001111101101011110001111010000000000000000000000000000000000000001010000010101001101010010000000000101010000111100111010000000111000111110000011001000001111010101101000001110000000000011101001011111110000000000000000000000000000000000000011110000000000001000000";
    public static final String S125_NO_3_ENCODED = "010101000001110101101111001101000101011100000000000000000000000000000000000000001010000010101001101010010000000000101010000111100111010000000111000111110000011001100000000011101001011111110000001111010101101000001110000000000010000000010000010000010000011110000000000000000000";

    /**
     * Test that we can encode an AIS Message 6 correctly based on the provided
     * GNURadio AIS Message 6 parameters. The encoded output will be checked
     * against the result produced by the AIS Blacktoolkit AIVDM_Encoder.py
     * utility for the following definition:
     * <p>
     * ./AIVDM_Encoder.py --type=6 --mmsi=123456789 --d_mmsi=987654321 --msg=XXX
     */
    @Test
    public void testEncodeMsg6() {
        // First construct the parameters
        GrAisMsg6Params msgParams = new GrAisMsg6Params();
        msgParams.setMmsi(123456789);
        msgParams.setDestMmsi(987654321);
        msgParams.setMessage("XXX".getBytes());

        // Encode the message
        String msg6 = GrAisUtils.encodeMsg6(msgParams);

        // Assert that everything seems correct
        assertFalse(msg6.isEmpty());
        assertEquals(AIS_MSG_6_ENCODED, msg6);
    }

    /**
     * Test that we can encode an AIS Message 8 correctly based on the provided
     * GNURadio AIS Message 8 parameters. The encoded output will be checked
     * against the result produced by the AIS Blacktoolkit AIVDM_Encoder.py
     * utility for the following definition:
     * <p>
     * ./AIVDM_Encoder.py --type=8 --mmsi=123456789 --d_mmsi=987654321 --msg=XXX
     */
    @Test
    public void testEncodeMsg8() {
        // First construct the parameters
        GrAisMsg8Params msgParams = new GrAisMsg8Params();
        msgParams.setMmsi(123456789);
        msgParams.setMessage("XXX".getBytes());

        // Encode the message
        String msg8 = GrAisUtils.encodeMsg8(msgParams);

        // Assert that everything seems correct
        assertFalse(msg8.isEmpty());
        assertEquals(AIS_MSG_8_ENCODED, msg8);
    }

    /**
     * Test that a basic Virtual AtoN can be encoded correctly. The encoded
     * output will be checked against the result produced by the AIS
     * Blacktoolkit AIVDM_Encoder.py utility for the following definition:
     * <p>
     * ./AIVDM_Encoder.py --type=21 --mmsi=123456789 --lat=53.61 --long=1.594 --aid_type=30 --aid_name='Test AtoN No 1' --v_AtoN
     */
    @Test
    public void testEncodeMsg21VAtoN() {
        // Create an GrAisMsg21Params parameters
        GrAisMsg21Params msgParams = new GrAisMsg21Params();
        msgParams.setMmsi(123456789);
        msgParams.setName("Test AtoN No 1");
        msgParams.setAtonType(AtonType.SPECIAL_MARK);
        msgParams.setLatitude(53.61);
        msgParams.setLongitude(1.594);
        msgParams.setVaton(Boolean.TRUE);

        // Encode the message
        String msg21 = GrAisUtils.encodeMsg21(msgParams);

        // Assert that everything seems correct
        assertFalse(msg21.isEmpty());
        assertEquals(S125_NO_1_ENCODED, msg21);
    }

    /**
     * Test that another Virtual AtoN can be encoded correctly. The encoded
     * output will be checked against the result produced by the AIS
     * Blacktoolkit AIVDM_Encoder.py utility for the following definition:
     * <p>
     * ./AIVDM_Encoder.py --type=21 --mmsi=123456789 --lat=53.61 --long=1.594 --aid_type=24 --aid_name='Test AtoN No 3' --vsize=4x4
     */
    @Test
    public void testEncodeMsg21VAtoNNo2()  {
        // Create an GrAisMsg21Params parameters
        GrAisMsg21Params msgParams = new GrAisMsg21Params();
        msgParams.setMmsi(111111111);
        msgParams.setName("Test AtoN No 2");
        msgParams.setAtonType(AtonType.NORTH_CARDINAL);
        msgParams.setLatitude(1.594);
        msgParams.setLongitude(53.61);
        msgParams.setVaton(Boolean.TRUE);

        // Encode the message
        String msg21 = GrAisUtils.encodeMsg21(msgParams);

        // Assert that everything seems correct
        assertFalse(msg21.isEmpty());
        assertEquals(S125_NO_2_ENCODED, msg21);
    }

    /**
     * Test that a basic real AtoN can be encoded correctly. The encoded
     * output will be checked against the result produced by the AIS
     * Blacktoolkit AIVDM_Encoder.py utility for the following definition:
     * <p>
     * ./AIVDM_Encoder.py --type=21 --mmsi=123456789 --lat=53.61 --long=1.594 --aid_type=30 --aid_name='Test AtoN No 3' --vsize=4x4
     */
    @Test
    public void testEncodeMsg21RealAtoN() {
        // Create an GrAisMsg21Params parameters
        GrAisMsg21Params msgParams = new GrAisMsg21Params();
        msgParams.setMmsi(123456789);
        msgParams.setName("Test AtoN No 3");
        msgParams.setAtonType(AtonType.PORT_HAND_MARK);
        msgParams.setLatitude(53.61);
        msgParams.setLongitude(1.594);
        msgParams.setLength(4);
        msgParams.setWidth(4);

        // Encode the message
        String msg21 = GrAisUtils.encodeMsg21(msgParams);

        // Assert that everything seems correct
        assertFalse(msg21.isEmpty());
        assertEquals(S125_NO_3_ENCODED, msg21);
    }

    /**
     * Test that we can generate correct NMEA sentences.
     */
    @Test
    public void testGenerateNMEASentence() {
        assertEquals("", GrAisUtils.generateNMEASentence(null, true, NMEAChannel.A));
        assertEquals("", GrAisUtils.generateNMEASentence("", true, NMEAChannel.A));
        assertEquals("!AIVDM,1,1,,A,E1mg=5O000000:2ab@0b7W@77hHP3aOh?E`>000000N0100,0*48", GrAisUtils.generateNMEASentence(S125_NO_1_ENCODED, true, NMEAChannel.A));
        assertEquals("!AIVDM,1,1,,B,E1mg=5O000000:2ab@0b7W@77hHP3aOh?E`>000000N0100", GrAisUtils.generateNMEASentence(S125_NO_1_ENCODED, false, NMEAChannel.B));
        assertEquals("!AIVDM,1,1,,B,E1mg=5O000000:2ab@0b7W@77hHP3aOh?E`>000000N0100,0*4B", GrAisUtils.generateNMEASentence(S125_NO_1_ENCODED, true, NMEAChannel.B));
        assertEquals("!AIVDM,1,1,,A,E1aucir000000:2ab@0b7W@77hI1re1h0M;v000000N0100,0*26", GrAisUtils.generateNMEASentence(S125_NO_2_ENCODED, true, NMEAChannel.A));
        assertEquals("!AIVDM,1,1,,A,E1aucir000000:2ab@0b7W@77hI1re1h0M;v000000N0100", GrAisUtils.generateNMEASentence(S125_NO_2_ENCODED, false, NMEAChannel.A));
        assertEquals("!AIVDM,1,1,,B,E1aucir000000:2ab@0b7W@77hI1re1h0M;v000000N0100,0*25", GrAisUtils.generateNMEASentence(S125_NO_2_ENCODED, true, NMEAChannel.B));
        assertEquals("!AIVDM,1,1,,A,E1mg=5L000000:2ab@0b7W@77hIP3aOh?E`>020@@@N0000,0*39", GrAisUtils.generateNMEASentence(S125_NO_3_ENCODED, true, NMEAChannel.A));
        assertEquals("!AIVDM,1,1,,A,E1mg=5L000000:2ab@0b7W@77hIP3aOh?E`>020@@@N0000", GrAisUtils.generateNMEASentence(S125_NO_3_ENCODED, false, NMEAChannel.A));
        assertEquals("!AIVDM,1,1,,B,E1mg=5L000000:2ab@0b7W@77hIP3aOh?E`>020@@@N0000,0*3A", GrAisUtils.generateNMEASentence(S125_NO_3_ENCODED, true, NMEAChannel.B));
    }

    /**
     * Test that we can generate correct NMEA sentence signatures.
     * @throws Exception when an exception is thrown while testing
     */
    @Test
    public void testSignNMEASentence() throws Exception {
        // Retrieve the public key to initialise a signature with
        ECPublicKey publicKey = CryptoUtils.readECPublicKey("CorkHoleTest-Public.pem");
        Signature sign = Signature.getInstance("SHA256withECDSA");
        sign.initVerify(publicKey);

        // Define various NMEA sentences and a UNIX timestamp to append to the messages
        String nmeaSentance1 = "!AIVDM,1,1,,A,E1mgAHg0000000000022VWh0b7W03aOh?E`>000000N010,0*43";
        String nmeaSentance2 = "!AIVDM,1,1,,B,E1mg=5O000000:2ab@0b7W@77hHP3aOh?E`>000000N010";
        String nmeaSentance3 = "!AIVDM,1,1,,B,E1mg=5O000000:2ab@0b7W@77hHP3aOh?E`>000000N010,0*7B";
        String nmeaSentance4 = "!AIVDM,1,1,,A,E1aucir000000:2ab@0b7W@77hI1re1h0M;v000000N010,0*16";
        String nmeaSentance5 = "!AIVDM,1,1,,A,E1aucir000000:2ab@0b7W@77hI1re1h0M;v000000N010";
        String nmeaSentance6 = "!AIVDM,1,1,,B,E1aucir000000:2ab@0b7W@77hI1re1h0M;v000000N010,0*15";
        String nmeaSentance7 = "!AIVDM,1,1,,A,E1mg=5L000000:2ab@0b7W@77hIP3aOh?E`>020@@@N000,0*09";
        String nmeaSentance8 = "!AIVDM,1,1,,A,E1mg=5L000000:2ab@0b7W@77hIP3aOh?E`>020@@@N000";
        String nmeaSentance9 = "!AIVDM,1,1,,B,E1mg=5L000000:2ab@0b7W@77hIP3aOh?E`>020@@@N000,0*0A";
        long timestamp = System.currentTimeMillis()/1000L;

        // Generate the NMEA signature for sentence 1
        byte[] signature1 = GrAisUtils.getNMEASentenceSignature(nmeaSentance1, timestamp);

        //Verify the signature is correct
        sign.update(MessageDigest.getInstance("SHA-256").digest((nmeaSentance1).getBytes()));
        assertTrue(sign.verify(signature1));
        assertTrue(signature1.length < 120);

        // Generate the NMEA signature for sentence 2
        byte[] signature2 = GrAisUtils.getNMEASentenceSignature(nmeaSentance2, timestamp);

        //Verify the signature is correct
        sign.update(MessageDigest.getInstance("SHA-256").digest((nmeaSentance2).getBytes()));
        assertTrue(sign.verify(signature2));
        assertTrue(signature2.length < 120);

        // Generate the NMEA signature for sentence 3
        byte[] signature3 = GrAisUtils.getNMEASentenceSignature(nmeaSentance3, timestamp);

        //Verify the signature is correct
        sign.update(MessageDigest.getInstance("SHA-256").digest((nmeaSentance3).getBytes()));
        assertTrue(sign.verify(signature3));
        assertTrue(signature3.length < 1203);

        // Generate the NMEA signature for sentence 4
        byte[] signature4 = GrAisUtils.getNMEASentenceSignature(nmeaSentance4, timestamp);

        //Verify the signature is correct
        sign.update(MessageDigest.getInstance("SHA-256").digest((nmeaSentance4).getBytes()));
        assertTrue(sign.verify(signature4));
        System.out.println(signature4.length);
        assertTrue(signature4.length < 120);

        // Generate the NMEA signature for sentence 5
        byte[] signature5 = GrAisUtils.getNMEASentenceSignature(nmeaSentance5, timestamp);

        //Verify the signature is correct
        sign.update(MessageDigest.getInstance("SHA-256").digest((nmeaSentance5).getBytes()));
        assertTrue(sign.verify(signature5));
        assertTrue(signature5.length < 120);

        // Generate the NMEA signature for sentence 6
        byte[] signature6 = GrAisUtils.getNMEASentenceSignature(nmeaSentance6, timestamp);

        //Verify the signature is correct
        sign.update(MessageDigest.getInstance("SHA-256").digest((nmeaSentance6).getBytes()));
        assertTrue(sign.verify(signature6));
        System.out.println(signature6.length);
        assertTrue(signature6.length < 120);

        // Generate the NMEA signature for sentence 7
        byte[] signature7 = GrAisUtils.getNMEASentenceSignature(nmeaSentance7, timestamp);

        //Verify the signature is correct
        sign.update(MessageDigest.getInstance("SHA-256").digest((nmeaSentance7).getBytes()));
        assertTrue(sign.verify(signature7));
        assertTrue(signature7.length < 120);

        // Generate the NMEA signature for sentence 8
        byte[] signature8 = GrAisUtils.getNMEASentenceSignature(nmeaSentance8, timestamp);

        //Verify the signature is correct
        sign.update(MessageDigest.getInstance("SHA-256").digest((nmeaSentance8).getBytes()));
        assertTrue(sign.verify(signature8));
        assertTrue(signature8.length < 120);

        // Generate the NMEA signature for sentence 9
        byte[] signature9 = GrAisUtils.getNMEASentenceSignature(nmeaSentance9, timestamp);

        //Verify the signature is correct
        sign.update(MessageDigest.getInstance("SHA-256").digest((nmeaSentance9).getBytes()));
        assertTrue(sign.verify(signature9));
        assertTrue(signature9.length < 120);
    }

}
