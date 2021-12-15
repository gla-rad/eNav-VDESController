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
 *
 */

package org.grad.eNav.vdesCtrl.models.vdes.ais.sentences;

import org.grad.eNav.vdesCtrl.models.domain.AISChannel;
import org.grad.eNav.vdesCtrl.utils.StringBinUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VDMSentenceTest {

    // Define the test AIS Messages Expected Encoding
    public static final String AIS_MSG_6_ENCODED = "000110000001110101101111001101000101010011101011011110011010001011000100000000000100000101011000010110000101100000";
    public static final String AIS_MSG_8_ENCODED = "001000000001110101101111001101000101010000000000010000010101100001011000010110000000";
    public static final String AIS_MSG_21_NO_1_ENCODED = "010101000001110101101111001101000101011111001010000010101001101010010000000000101010000111100111010000000111000111110000011000110000010000010000010000010000010000000000000011101001011111110000001111010101101000001110000000000000000000000000000000000000000000000000000001000000";
    public static final String AIS_MSG_21_NO_2_ENCODED = "010101000001110101101111001101000101011100001010000010101001101010010000000000101010000111100111010000000111000111110000011001010000010000010000010000010000010000000001111010101101000001110000000000011101001011111110000000000000000000000000000000000000000000000000000001000000";
    public static final String AIS_MSG_21_NO_3_ENCODED = "010101000001110101101111001101000101011111001010000010101001101010010000000000101010000111100111010000000111000111110000011001110000010000010000010000010000010000000000000011101001011111110000001111010101101000001110000000000010000000010000010000010000000000000000000000000000";

    /**
     * Test that the VDM Sentences can be initialised correctly with the
     * default constructor.
     */
    @Test
    public void testConstructor() {
        VDMSentence vdmSentence = new VDMSentence(10, 1, AISChannel.A, new byte[]{0x61, 0x62, 0x63, 0x64, 0x65});
        assertEquals("AI", vdmSentence.getTalkerId());
        assertEquals("VDM", vdmSentence.getFormatterCode());
        assertEquals(10, vdmSentence.getSentencesTotal());
        assertEquals(1, vdmSentence.getSentenceNum());
        assertTrue(vdmSentence.getSequenceId().isEmpty());
        assertEquals(AISChannel.A, vdmSentence.getChannel());
        assertEquals(5, vdmSentence.getPayload().length);
        assertEquals(2, vdmSentence.getNoFillBits());
    }

    /**
     * Test that the VDM Sentences can be initialised correctly with the
     * Talker ID constructor.
     */
    @Test
    public void testConstructorWithTalkerId() {
        VDMSentence vdmSentence = new VDMSentence("CUSTOM", 10, 1, AISChannel.A, new byte[]{0x61, 0x62, 0x63, 0x64, 0x65});
        assertEquals("CUSTOM", vdmSentence.getTalkerId());
        assertEquals("VDM", vdmSentence.getFormatterCode());
        assertEquals(10, vdmSentence.getSentencesTotal());
        assertEquals(1, vdmSentence.getSentenceNum());
        assertTrue(vdmSentence.getSequenceId().isEmpty());
        assertEquals(AISChannel.A, vdmSentence.getChannel());
        assertEquals(5, vdmSentence.getPayload().length);
        assertEquals(2, vdmSentence.getNoFillBits());
    }

    /**
     * Test that we can correctly construct the string representation of the
     * sentence.
     */
    @Test
    public void testSentenceToString() {
        VDMSentence vdmSentence = new VDMSentence(10, 1, AISChannel.A, new byte[]{0x61, 0x62, 0x63, 0x64, 0x65});
        assertEquals("!AIVDM,10,1,,A,abcde,2", vdmSentence.toString());
    }

    /**
     * Test that we can correctly construct the string representation of the
     * sentence with the checksum.
     */
    @Test
    public void testSentenceToStringWithChecksum() {
        VDMSentence vdmSentence = new VDMSentence(10, 1, AISChannel.A, new byte[]{0x61, 0x62, 0x63, 0x64, 0x65});
        assertEquals("!AIVDM,10,1,,A,abcde,2*75", vdmSentence.toStringWithChecksum());
    }

    /**
     * Test that we can generate correct NMEA sentences.
     */
    @Test
    public void testGenerateNMEASentence() {
        VDMSentence nullMessage = new VDMSentence(1, 1, AISChannel.A, null);
        assertEquals("!AIVDM,1,1,,A,,0*26", nullMessage.toStringWithChecksum());
        assertEquals("!AIVDM,1,1,,A,,0", nullMessage.toString());

        VDMSentence emptyMessage = new VDMSentence(1, 1, AISChannel.A, new byte[]{});
        assertEquals("!AIVDM,1,1,,A,,0*26", emptyMessage.toStringWithChecksum());
        assertEquals("!AIVDM,1,1,,A,,0", emptyMessage.toString());

        VDMSentence aisMsg6No1A = new VDMSentence(1, 1, AISChannel.A, StringBinUtils.convertBinaryStringToBytes(AIS_MSG_6_ENCODED, true));
        assertEquals("!AIVDM,1,1,,A,61mg=5CcNJ;4045HF5P,4*56", aisMsg6No1A.toStringWithChecksum());
        assertEquals("!AIVDM,1,1,,A,61mg=5CcNJ;4045HF5P,4", aisMsg6No1A.toString());
        VDMSentence aisMsg6No1B = new VDMSentence(1, 1, AISChannel.B, StringBinUtils.convertBinaryStringToBytes(AIS_MSG_6_ENCODED, true));
        assertEquals("!AIVDM,1,1,,B,61mg=5CcNJ;4045HF5P,4*55", aisMsg6No1B.toStringWithChecksum());

        VDMSentence aisMsg8No1A = new VDMSentence(1, 1, AISChannel.A, StringBinUtils.convertBinaryStringToBytes(AIS_MSG_8_ENCODED, true));
        assertEquals("!AIVDM,1,1,,A,81mg=5@0@EQHF0,2*35", aisMsg8No1A.toStringWithChecksum());
        assertEquals("!AIVDM,1,1,,A,81mg=5@0@EQHF0,2", aisMsg8No1A.toString());
        VDMSentence aisMsg8No1B = new VDMSentence(1, 1, AISChannel.B, StringBinUtils.convertBinaryStringToBytes(AIS_MSG_8_ENCODED, true));
        assertEquals("!AIVDM,1,1,,B,81mg=5@0@EQHF0,2*36", aisMsg8No1B.toStringWithChecksum());

        VDMSentence aisMsg21No1A = new VDMSentence(1, 1, AISChannel.A, StringBinUtils.convertBinaryStringToBytes(AIS_MSG_21_NO_1_ENCODED, true));
        assertEquals("!AIVDM,1,1,,A,E1mg=5O:2ab@0b7W@77hHh@@@@@03aOh?E`>0000000010,4*4A", aisMsg21No1A.toStringWithChecksum());
        assertEquals("!AIVDM,1,1,,A,E1mg=5O:2ab@0b7W@77hHh@@@@@03aOh?E`>0000000010,4", aisMsg21No1A.toString());
        VDMSentence aisMsg21No1B = new VDMSentence(1, 1, AISChannel.B, StringBinUtils.convertBinaryStringToBytes(AIS_MSG_21_NO_1_ENCODED, true));
        assertEquals("!AIVDM,1,1,,B,E1mg=5O:2ab@0b7W@77hHh@@@@@03aOh?E`>0000000010,4*49", aisMsg21No1B.toStringWithChecksum());

        VDMSentence aisMsg21No2A = new VDMSentence(1, 1, AISChannel.A, StringBinUtils.convertBinaryStringToBytes(AIS_MSG_21_NO_2_ENCODED, true));
        assertEquals("!AIVDM,1,1,,A,E1mg=5L:2ab@0b7W@77hI@@@@@@1re1h0M;v0000000010,4*4E", aisMsg21No2A.toStringWithChecksum());
        assertEquals("!AIVDM,1,1,,A,E1mg=5L:2ab@0b7W@77hI@@@@@@1re1h0M;v0000000010,4", aisMsg21No2A.toString());
        VDMSentence aisMsg21No2B = new VDMSentence(1, 1, AISChannel.B, StringBinUtils.convertBinaryStringToBytes(AIS_MSG_21_NO_2_ENCODED, true));
        assertEquals("!AIVDM,1,1,,B,E1mg=5L:2ab@0b7W@77hI@@@@@@1re1h0M;v0000000010,4*4D", aisMsg21No2B.toStringWithChecksum());

        VDMSentence aisMsg21No3A = new VDMSentence(1, 1, AISChannel.A, StringBinUtils.convertBinaryStringToBytes(AIS_MSG_21_NO_3_ENCODED, true));
        assertEquals("!AIVDM,1,1,,A,E1mg=5O:2ab@0b7W@77hIh@@@@@03aOh?E`>020@@@0000,4*38", aisMsg21No3A.toStringWithChecksum());
        assertEquals("!AIVDM,1,1,,A,E1mg=5O:2ab@0b7W@77hIh@@@@@03aOh?E`>020@@@0000,4", aisMsg21No3A.toString());
        VDMSentence aisMsg21No3B = new VDMSentence(1, 1, AISChannel.B, StringBinUtils.convertBinaryStringToBytes(AIS_MSG_21_NO_3_ENCODED, true));
        assertEquals("!AIVDM,1,1,,B,E1mg=5O:2ab@0b7W@77hIh@@@@@03aOh?E`>020@@@0000,4*3B", aisMsg21No3B.toStringWithChecksum());
    }

}