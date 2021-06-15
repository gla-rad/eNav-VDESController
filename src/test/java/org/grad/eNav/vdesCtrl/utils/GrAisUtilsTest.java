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

import org.grad.eNav.vdesCtrl.models.domain.AtonType;
import org.grad.eNav.vdesCtrl.models.domain.GrAisMsg21Params;
import org.grad.eNav.vdesCtrl.models.domain.NMEAChannel;
import org.junit.jupiter.api.Test;

import javax.xml.bind.JAXBException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class GrAisUtilsTest {

    // Define the test S125 Messages Expected Encoding
    public static final String S125_NO_1_ENCODED = "010101000001110101101111001101000101011111000000000000000000000000000000000000001010000010101001101010010000000000101010000111100111010000000111000111110000011000100000000011101001011111110000001111010101101000001110000000000000000000000000000000000000011110000000000001000000";
    public static final String S125_NO_2_ENCODED = "010101000001101001111101101011110001111010000000000000000000000000000000000000001010000010101001101010010000000000101010000111100111010000000111000111110000011001000001111010101101000001110000000000011101001011111110000000000000000000000000000000000000011110000000000001000000";
    public static final String S125_NO_3_ENCODED = "010101000001110101101111001101000101011100000000000000000000000000000000000000001010000010101001101010010000000000101010000111100111010000000111000111110000011001100000000011101001011111110000001111010101101000001110000000000010000000010000010000010000011110000000000000000000";

    /**
     * Test that a basic Virtual AtoN can be encoded correctly. The encoded
     * output will be checked against the result produced by the AIS
     * Blacktoolkit AIVDM_Encoder.py utility for the following definition:
     *
     * ./AIVDM_Encoder.py --type=21 --mmsi=123456789 --lat=53.61 --long=1.594 --aid_type=30 --aid_name='Test AtoN No 1' --v_AtoN
     *
     * @throws JAXBException
     */
    @Test
    public void testS125ToAisMsg21VAtoN() throws JAXBException {
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
     *
     * ./AIVDM_Encoder.py --type=21 --mmsi=123456789 --lat=53.61 --long=1.594 --aid_type=24 --aid_name='Test AtoN No 3' --vsize=4x4
     *
     * @throws JAXBException
     */
    @Test
    public void testS125ToAisMsg21RVAtoNNo2() throws JAXBException {
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
     *
     * ./AIVDM_Encoder.py --type=21 --mmsi=123456789 --lat=53.61 --long=1.594 --aid_type=30 --aid_name='Test AtoN No 3' --vsize=4x4
     *
     * @throws JAXBException
     */
    @Test
    public void testS125ToAisMsg21RealAtoN() throws JAXBException {
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
        assertEquals("!AIVDM,1,1,,A,E1mg=5O000000:2ab@0b7W@77hHP3aOh?E`>000000N010,0*78", GrAisUtils.generateNMEASentence(S125_NO_1_ENCODED, true, NMEAChannel.A));
        assertEquals("!AIVDM,1,1,,B,E1mg=5O000000:2ab@0b7W@77hHP3aOh?E`>000000N010", GrAisUtils.generateNMEASentence(S125_NO_1_ENCODED, false, NMEAChannel.B));
        assertEquals("!AIVDM,1,1,,B,E1mg=5O000000:2ab@0b7W@77hHP3aOh?E`>000000N010,0*7B", GrAisUtils.generateNMEASentence(S125_NO_1_ENCODED, true, NMEAChannel.B));
        assertEquals("!AIVDM,1,1,,A,E1aucir000000:2ab@0b7W@77hI1re1h0M;v000000N010,0*16", GrAisUtils.generateNMEASentence(S125_NO_2_ENCODED, true, NMEAChannel.A));
        assertEquals("!AIVDM,1,1,,A,E1aucir000000:2ab@0b7W@77hI1re1h0M;v000000N010", GrAisUtils.generateNMEASentence(S125_NO_2_ENCODED, false, NMEAChannel.A));
        assertEquals("!AIVDM,1,1,,B,E1aucir000000:2ab@0b7W@77hI1re1h0M;v000000N010,0*15", GrAisUtils.generateNMEASentence(S125_NO_2_ENCODED, true, NMEAChannel.B));
        assertEquals("!AIVDM,1,1,,A,E1mg=5L000000:2ab@0b7W@77hIP3aOh?E`>020@@@N000,0*09", GrAisUtils.generateNMEASentence(S125_NO_3_ENCODED, true, NMEAChannel.A));
        assertEquals("!AIVDM,1,1,,A,E1mg=5L000000:2ab@0b7W@77hIP3aOh?E`>020@@@N000", GrAisUtils.generateNMEASentence(S125_NO_3_ENCODED, false, NMEAChannel.A));
        assertEquals("!AIVDM,1,1,,B,E1mg=5L000000:2ab@0b7W@77hIP3aOh?E`>020@@@N000,0*0A", GrAisUtils.generateNMEASentence(S125_NO_3_ENCODED, true, NMEAChannel.B));
    }
}