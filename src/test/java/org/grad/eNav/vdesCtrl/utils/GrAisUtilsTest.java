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

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.grad.eNav.vdesCtrl.models.domain.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

import static org.junit.jupiter.api.Assertions.*;

public class GrAisUtilsTest {

    // Define the test S125 Messages Expected Encoding
    public static final String AIS_MSG_6_ENCODED = "000110000001110101101111001101000101010011101011011110011010001011000100000000000100000101011000010110000101100000";
    public static final String AIS_MSG_8_ENCODED = "001000000001110101101111001101000101010000000000010000010101100001011000010110000000";
    public static final String S125_NO_1_ENCODED = "010101000001110101101111001101000101011111001010000010101001101010010000000000101010000111100111010000000111000111110000011000110000010000010000010000010000010000000000000011101001011111110000001111010101101000001110000000000000000000000000000000000000011110000000000001000000";
    public static final String S125_NO_2_ENCODED = "010101000001110101101111001101000101011100001010000010101001101010010000000000101010000111100111010000000111000111110000011001010000010000010000010000010000010000000001111010101101000001110000000000011101001011111110000000000000000000000000000000000000011110000000000001000000";
    public static final String S125_NO_3_ENCODED = "010101000001110101101111001101000101011111001010000010101001101010010000000000101010000111100111010000000111000111110000011001110000010000010000010000010000010000000000000011101001011111110000001111010101101000001110000000000010000000010000010000010000011110000000000000000000";

    /**
     * Add the Bouncy Castle as a security provider for the unit tests.
     */
    @BeforeAll
    static void addSecurityProvider() {
        Security.addProvider(new BouncyCastleProvider());
    }

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
        msgParams.setUid("Test AtoN No 1");
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
     * ./AIVDM_Encoder.py --type=21 --mmsi=123456789 --lat=1.594 --long=53.61 --aid_type=24 --aid_name='Test AtoN No 2' --v_AtoN
     */
    @Test
    public void testEncodeMsg21VAtoNNo2()  {
        // Create an GrAisMsg21Params parameters
        GrAisMsg21Params msgParams = new GrAisMsg21Params();
        msgParams.setMmsi(123456789);
        msgParams.setUid("Test AtoN No 2");
        msgParams.setName("Test AtoN No 2");
        msgParams.setAtonType(AtonType.PORT_HAND_MARK);
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
        msgParams.setUid("Test AtoN No 3");
        msgParams.setName("Test AtoN No 3");
        msgParams.setAtonType(AtonType.SPECIAL_MARK);
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
        assertEquals("!AIVDM,1,1,,A,E1mg=5O:2ab@0b7W@77hHh@@@@@03aOh?E`>000000N0100,2*02", GrAisUtils.generateNMEASentence(S125_NO_1_ENCODED, true, NMEAChannel.A));
        assertEquals("!AIVDM,1,1,,A,E1mg=5O:2ab@0b7W@77hHh@@@@@03aOh?E`>000000N0100", GrAisUtils.generateNMEASentence(S125_NO_1_ENCODED, false, NMEAChannel.A));
        assertEquals("!AIVDM,1,1,,B,E1mg=5O:2ab@0b7W@77hHh@@@@@03aOh?E`>000000N0100,2*01", GrAisUtils.generateNMEASentence(S125_NO_1_ENCODED, true, NMEAChannel.B));
        assertEquals("!AIVDM,1,1,,A,E1mg=5L:2ab@0b7W@77hI@@@@@@1re1h0M;v000000N0100,2*06", GrAisUtils.generateNMEASentence(S125_NO_2_ENCODED, true, NMEAChannel.A));
        assertEquals("!AIVDM,1,1,,A,E1mg=5L:2ab@0b7W@77hI@@@@@@1re1h0M;v000000N0100", GrAisUtils.generateNMEASentence(S125_NO_2_ENCODED, false, NMEAChannel.A));
        assertEquals("!AIVDM,1,1,,B,E1mg=5L:2ab@0b7W@77hI@@@@@@1re1h0M;v000000N0100,2*05", GrAisUtils.generateNMEASentence(S125_NO_2_ENCODED, true, NMEAChannel.B));
        assertEquals("!AIVDM,1,1,,A,E1mg=5O:2ab@0b7W@77hIh@@@@@03aOh?E`>020@@@N0000,2*70", GrAisUtils.generateNMEASentence(S125_NO_3_ENCODED, true, NMEAChannel.A));
        assertEquals("!AIVDM,1,1,,A,E1mg=5O:2ab@0b7W@77hIh@@@@@03aOh?E`>020@@@N0000", GrAisUtils.generateNMEASentence(S125_NO_3_ENCODED, false, NMEAChannel.A));
        assertEquals("!AIVDM,1,1,,B,E1mg=5O:2ab@0b7W@77hIh@@@@@03aOh?E`>020@@@N0000,2*73", GrAisUtils.generateNMEASentence(S125_NO_3_ENCODED, true, NMEAChannel.B));
    }

    /**
     * Test that we can generate stamped AIS message hash byte array with the
     * correct length.
     *
     * Note that we are hashing the AIVDM content + timestamp with an SHA-256
     * hash which should give us 256/8 = 32 byte long result.
     */
    @Test
    public void testGetStampedAISMessageHash() throws NoSuchAlgorithmException,  IOException {
        // Test parameters
        long timestamp = System.currentTimeMillis()/1000L;

        // Generate the stamped message for AIS Msg 6
        byte[] stampedMessage1 = GrAisUtils.getStampedAISMessageHash(AIS_MSG_6_ENCODED, timestamp);
        assertEquals(256/8, stampedMessage1.length);

        // Generate the stamped message for AIS Msg 8
        byte[] stampedMessage2 = GrAisUtils.getStampedAISMessageHash(AIS_MSG_8_ENCODED, timestamp);
        assertEquals(256/8, stampedMessage2.length);

        // Generate the stamped message for S125 No1
        byte[] stampedMessage3 = GrAisUtils.getStampedAISMessageHash(S125_NO_1_ENCODED, timestamp);
        assertEquals(256/8, stampedMessage3.length);

        // Generate the stamped message for S125 No2
        byte[] stampedMessage4 = GrAisUtils.getStampedAISMessageHash(S125_NO_2_ENCODED, timestamp);
        assertEquals(256/8, stampedMessage4.length);

        // Generate the stamped message for S125 No3
        byte[] stampedMessage5 = GrAisUtils.getStampedAISMessageHash(S125_NO_3_ENCODED, timestamp);
        assertEquals(256/8, stampedMessage5.length);
    }

}
