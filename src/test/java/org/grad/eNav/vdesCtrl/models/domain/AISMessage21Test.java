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

package org.grad.eNav.vdesCtrl.models.domain;

import org.grad.eNav.vdesCtrl.models.dtos.S125Node;
import org.grad.eNav.vdesCtrl.models.vdes.ais.messages.AISMessage21;
import org.grad.eNav.vdesCtrl.utils.StringBinUtils;
import org.junit.jupiter.api.Test;

import javax.xml.bind.JAXBException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class AISMessage21Test {

    // Define the test S125 Messages Content
    public static final String S125_NO_1_CONTENT = "<S125:DataSet xmlns:S125=\"http://www.iho.int/S125/gml/0.1\" xmlns:S100=\"http://www.iho.int/s100gml/1.0\" xmlns:gml=\"http://www.opengis.net/gml/3.2\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" gml:id=\"aton.uk.test_aton_no_1\" xsi:schemaLocation=\"http://www.iho.int/S125/gml/1.0 S125.xsd\"><gml:boundedBy><gml:Envelope srsName=\"EPSG:4326\"><gml:lowerCorner>53.61 1.594</gml:lowerCorner><gml:upperCorner>53.61 1.594</gml:upperCorner></gml:Envelope></gml:boundedBy><member><S125:S125_NavAidStructure><featureName><displayName>true</displayName><language>eng</language><name>Test AtoN No 1</name></featureName><geometry><S100:pointProperty><S100:Point gml:id=\"G.aton.uk.test_aton_no_1.1\" srsName=\"EPSG:4326\"><gml:pos>53.61 1.594</gml:pos></S100:Point></S100:pointProperty></geometry><mmsi>123456789</mmsi><atonType>Special Mark</atonType><deploymentType>Mobile</deploymentType><raimFlag>false</raimFlag><vatonFlag>true</vatonFlag></S125:S125_NavAidStructure></member></S125:DataSet>";
    public static final String S125_NO_2_CONTENT = "<S125:DataSet xmlns:S125=\"http://www.iho.int/S125/gml/0.1\" xmlns:S100=\"http://www.iho.int/s100gml/1.0\" xmlns:gml=\"http://www.opengis.net/gml/3.2\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" gml:id=\"aton.uk.test_aton_no_2\" xsi:schemaLocation=\"http://www.iho.int/S125/gml/1.0 S125.xsd\"><gml:boundedBy><gml:Envelope srsName=\"EPSG:4326\"><gml:lowerCorner>1.594 53.61</gml:lowerCorner><gml:upperCorner>1.594 53.61</gml:upperCorner></gml:Envelope></gml:boundedBy><member><S125:S125_NavAidStructure><featureName><displayName>true</displayName><language>eng</language><name>Test AtoN No 2</name></featureName><geometry><S100:pointProperty><S100:Point gml:id=\"G.aton.uk.test_aton_no_2.1\" srsName=\"EPSG:4326\"><gml:pos>1.594 53.61</gml:pos></S100:Point></S100:pointProperty></geometry><mmsi>111111111</mmsi><atonType>Cardinal Mark N</atonType><deploymentType>Mobile</deploymentType><raimFlag>false</raimFlag><vatonFlag>true</vatonFlag></S125:S125_NavAidStructure></member></S125:DataSet>";
    public static final String S125_NO_3_CONTENT = "<S125:DataSet xmlns:S125=\"http://www.iho.int/S125/gml/0.1\" xmlns:S100=\"http://www.iho.int/s100gml/1.0\" xmlns:gml=\"http://www.opengis.net/gml/3.2\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" gml:id=\"aton.uk.test_aton_no_3\" xsi:schemaLocation=\"http://www.iho.int/S125/gml/1.0 S125.xsd\"><gml:boundedBy><gml:Envelope srsName=\"EPSG:4326\"><gml:lowerCorner>53.61 1.594</gml:lowerCorner><gml:upperCorner>53.61 1.594</gml:upperCorner></gml:Envelope></gml:boundedBy><member><S125:S125_NavAidStructure><featureName><displayName>true</displayName><language>eng</language><name>Test AtoN No 3</name></featureName><geometry><S100:pointProperty><S100:Point gml:id=\"G.aton.uk.test_aton_no_3.1\" srsName=\"EPSG:4326\"><gml:pos>53.61 1.594</gml:pos></S100:Point></S100:pointProperty></geometry><mmsi>123456789</mmsi><length>4</length><width>4</width><atonType>Port hand Mark</atonType><deploymentType>Mobile</deploymentType><raimFlag>false</raimFlag><vatonFlag>false</vatonFlag></S125:S125_NavAidStructure></member></S125:DataSet>";

    /**
     * Test that the AIS Message 21 will be constructed by default with
     * empty parameters.
     */
    @Test
    public void testEmptyConstructor() {
        AISMessage21 aisMessage21 = new AISMessage21();
        assertNull(aisMessage21.getMmsi());
        assertEquals(AtonType.DEFAULT, aisMessage21.getAtonType());
        assertEquals("", aisMessage21.getName());
        assertEquals(0.0, aisMessage21.getLatitude());
        assertEquals(0.0, aisMessage21.getLongitude());
        assertEquals(0, aisMessage21.getLength());
        assertEquals(0, aisMessage21.getWidth());
        assertEquals(Boolean.FALSE, aisMessage21.getRaim());
        assertEquals(Boolean.FALSE, aisMessage21.getVaton());
    }

    /**
     * Test that by using the S125Node constructor, all parameters will be
     * correctly picked up, for a test Virtual AtoN.
     */
    @Test
    public void testS125NodeConstructorNo1() throws JAXBException {
        // Create an S125Node message
        S125Node node = new S125Node("aton.uk.test_aton_no_1", null, S125_NO_1_CONTENT);

        // Create the GR-AIS Message 21
        AISMessage21 msgParams = new AISMessage21(node);

        // Assert that all variables have been initialised correctly
        assertEquals(123456789, msgParams.getMmsi());
        assertEquals(AtonType.SPECIAL_MARK, msgParams.getAtonType());
        assertEquals("Test AtoN No 1", msgParams.getName());
        assertEquals(53.61, msgParams.getLatitude());
        assertEquals(1.594, msgParams.getLongitude());
        assertEquals(0, msgParams.getLength());
        assertEquals(0, msgParams.getWidth());
        assertEquals(Boolean.FALSE, msgParams.getRaim());
        assertEquals(Boolean.TRUE, msgParams.getVaton());
    }

    /**
     * Test that by using the S125Node constructor, all parameters will be
     * correctly picked up, for a different Virtual AtoN.
     */
    @Test
    public void testS125NodeConstructorNo2() throws JAXBException {
        // Create an S125Node message
        S125Node node = new S125Node("aton.uk.test_aton_no_2", null, S125_NO_2_CONTENT);

        // Create the GR-AIS Message 21
        AISMessage21 aisMessage21 = new AISMessage21(node);

        // Assert that all variables have been initialised correctly
        assertEquals(111111111, aisMessage21.getMmsi());
        assertEquals(AtonType.NORTH_CARDINAL, aisMessage21.getAtonType());
        assertEquals("Test AtoN No 2", aisMessage21.getName());
        assertEquals(1.594, aisMessage21.getLatitude());
        assertEquals(53.61, aisMessage21.getLongitude());
        assertEquals(0, aisMessage21.getLength());
        assertEquals(0, aisMessage21.getWidth());
        assertEquals(Boolean.FALSE, aisMessage21.getRaim());
        assertEquals(Boolean.TRUE, aisMessage21.getVaton());
    }

    /**
     * Test that by using the S125Node constructor, all parameters will be
     * correctly picked up, for a real AtoN.
     */
    @Test
    public void testS125NodeConstructorNo3() throws JAXBException {
        // Create an S125Node message
        S125Node node = new S125Node("aton.uk.test_aton_no_3", null, S125_NO_3_CONTENT);

        // Create the GR-AIS Message 21 Parameters
        AISMessage21 aisMessage21 = new AISMessage21(node);

        // Assert that all variables have been initialised correctly
        assertEquals(123456789, aisMessage21.getMmsi());
        assertEquals(AtonType.PORT_HAND_MARK, aisMessage21.getAtonType());
        assertEquals("Test AtoN No 3", aisMessage21.getName());
        assertEquals(53.61, aisMessage21.getLatitude());
        assertEquals(1.594, aisMessage21.getLongitude());
        assertEquals(4, aisMessage21.getLength());
        assertEquals(4, aisMessage21.getWidth());
        assertEquals(Boolean.FALSE, aisMessage21.getRaim());
        assertEquals(Boolean.FALSE, aisMessage21.getVaton());
    }

    /**
     * Test that by using the S125Node constructor, if it fails, a JAXBException
     * will be thrown.
     */
    @Test
    public void testS125NodeConstructorFails() {
        // Create an S125Node message
        S125Node node = new S125Node("aton.uk.test_aton_no_1", null, "Erroneous Content");

        // Create the GR-AIS Message 21 Parameters and see it fail
        assertThrows(JAXBException.class, () -> new AISMessage21(node));
    }

    /**
     * Test that we can correctly construct the NMEA message representation.
     * Note that we need to use the 6bit conversion to do so.
     */
    @Test
    public void testGetBinaryMessage6bit() throws JAXBException {
        // Create an S125Node message
        S125Node node = new S125Node("aton.uk.test_aton_no_1", null, S125_NO_1_CONTENT);

        // Create the GR-AIS Message 21
        AISMessage21 aisMessage21 = new AISMessage21(node);
        LocalDateTime now = LocalDateTime.now();
        aisMessage21.setTimestamp(now.minusSeconds(now.getSecond()));

        assertEquals("E1mg=5O:2ab@0b7W@77hHh@@@@@03aOh?E`>0000000010", new String(aisMessage21.getBinaryMessage(true)));
    }

    /**
     * Test that we can correctly construct the NMEA message representation.
     * Note that when using the normal 8 bit conversion, the result should
     * is not the correct string representation but can be used anywhere else.
     */
    @Test
    public void testGetBinaryMessage8bit() throws JAXBException {
        // Create an S125Node message
        S125Node node = new S125Node("aton.uk.test_aton_no_1", null, S125_NO_1_CONTENT);

        // Create the AIS Message 21
        AISMessage21 aisMessage21 = new AISMessage21(node);
        LocalDateTime now = LocalDateTime.now();
        aisMessage21.setTimestamp(now.minusSeconds(now.getSecond()));

        byte[] bin = StringBinUtils.convertBinaryStringToBytes("010101000001110101101111001101000101011111001010000010101001101010010000000000101010000111100111010000000111000111110000011000110000010000010000010000010000010000000000000011101001011111110000001111010101101000001110000000000000000000000000000000000000000000000000000001000000", false);
        byte[] result = aisMessage21.getBinaryMessage(false);
        assertEquals(bin.length, result.length);
        // Check each byte separately
        for(int b=0; b<result.length; b++) {
            assertEquals(bin[b], result[b]);
        }
    }

    /**
     * Test that we can correctly construct the NMEA message binary
     * representation.
     */
    @Test
    public void testGetBinaryMessageString() throws JAXBException {
        // Create an S125Node message
        S125Node node = new S125Node("aton.uk.test_aton_no_1", null, S125_NO_1_CONTENT);

        // Create the AIS Message 21
        AISMessage21 aisMessage21 = new AISMessage21(node);
        LocalDateTime now = LocalDateTime.now();
        aisMessage21.setTimestamp(now.minusSeconds(now.getSecond()));

        String binString = "010101000001110101101111001101000101011111001010000010101001101010010000000000101010000111100111010000000111000111110000011000110000010000010000010000010000010000000000000011101001011111110000001111010101101000001110000000000000000000000000000000000000000000000000000001000000";
        assertEquals(binString, aisMessage21.getBinaryMessageString());
    }

}