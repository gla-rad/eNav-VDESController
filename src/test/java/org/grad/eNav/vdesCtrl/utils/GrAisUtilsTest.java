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

import org.grad.eNav.vdesCtrl.models.dtos.S125Node;
import org.junit.jupiter.api.Test;

import javax.xml.bind.JAXBException;

import static org.junit.jupiter.api.Assertions.*;

public class GrAisUtilsTest {

    // Define the test S125 Messages Content
    public static final String S125_NO_1_CONTENT = "<S125:DataSet xmlns:S125=\"http://www.iho.int/S125/gml/0.1\" xmlns:S100=\"http://www.iho.int/s100gml/1.0\" xmlns:gml=\"http://www.opengis.net/gml/3.2\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" gml:id=\"aton.uk.test_aton_no_1\" xsi:schemaLocation=\"http://www.iho.int/S125/gml/1.0 S125.xsd\"><gml:boundedBy><gml:Envelope srsName=\"EPSG:4326\"><gml:lowerCorner>53.61 1.594</gml:lowerCorner><gml:upperCorner>53.61 1.594</gml:upperCorner></gml:Envelope></gml:boundedBy><member><S125:S125_NavAidStructure><featureName><displayName>true</displayName><language>eng</language><name>Test AtoN No 1</name></featureName><geometry><S100:pointProperty><S100:Point gml:id=\"G.aton.uk.test_aton_no_1.1\" srsName=\"EPSG:4326\"><gml:pos>53.61 1.594</gml:pos></S100:Point></S100:pointProperty></geometry><mmsi>123456789</mmsi><atonType>Special Mark</atonType><deploymentType>Mobile</deploymentType><raimFlag>false</raimFlag><vatonFlag>true</vatonFlag></S125:S125_NavAidStructure></member></S125:DataSet>";
    public static final String S125_NO_2_CONTENT = "<S125:DataSet xmlns:S125=\"http://www.iho.int/S125/gml/0.1\" xmlns:S100=\"http://www.iho.int/s100gml/1.0\" xmlns:gml=\"http://www.opengis.net/gml/3.2\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" gml:id=\"aton.uk.test_aton_no_2\" xsi:schemaLocation=\"http://www.iho.int/S125/gml/1.0 S125.xsd\"><gml:boundedBy><gml:Envelope srsName=\"EPSG:4326\"><gml:lowerCorner>1.594 53.61</gml:lowerCorner><gml:upperCorner>1.594 53.61</gml:upperCorner></gml:Envelope></gml:boundedBy><member><S125:S125_NavAidStructure><featureName><displayName>true</displayName><language>eng</language><name>Test AtoN No 2</name></featureName><geometry><S100:pointProperty><S100:Point gml:id=\"G.aton.uk.test_aton_no_2.1\" srsName=\"EPSG:4326\"><gml:pos>1.594 53.61</gml:pos></S100:Point></S100:pointProperty></geometry><mmsi>111111111</mmsi><atonType>Cardinal Mark N</atonType><deploymentType>Mobile</deploymentType><raimFlag>false</raimFlag><vatonFlag>true</vatonFlag></S125:S125_NavAidStructure></member></S125:DataSet>";
    public static final String S125_NO_3_CONTENT = "<S125:DataSet xmlns:S125=\"http://www.iho.int/S125/gml/0.1\" xmlns:S100=\"http://www.iho.int/s100gml/1.0\" xmlns:gml=\"http://www.opengis.net/gml/3.2\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" gml:id=\"aton.uk.test_aton_no_3\" xsi:schemaLocation=\"http://www.iho.int/S125/gml/1.0 S125.xsd\"><gml:boundedBy><gml:Envelope srsName=\"EPSG:4326\"><gml:lowerCorner>53.61 1.594</gml:lowerCorner><gml:upperCorner>53.61 1.594</gml:upperCorner></gml:Envelope></gml:boundedBy><member><S125:S125_NavAidStructure><featureName><displayName>true</displayName><language>eng</language><name>Test AtoN No 3</name></featureName><geometry><S100:pointProperty><S100:Point gml:id=\"G.aton.uk.test_aton_no_3.1\" srsName=\"EPSG:4326\"><gml:pos>53.61 1.594</gml:pos></S100:Point></S100:pointProperty></geometry><mmsi>123456789</mmsi><length>4</length><width>4</width><atonType>Port hand Mark</atonType><deploymentType>Mobile</deploymentType><raimFlag>false</raimFlag><vatonFlag>false</vatonFlag></S125:S125_NavAidStructure></member></S125:DataSet>";


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
        // Create an S125Node message
        S125Node node = new S125Node("aton.uk.test_aton_no_1", null, S125_NO_1_CONTENT);

        // Encode the message
        String msg21 = GrAisUtils.encodeMsg21(node);

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
        // Create an S125Node message
        S125Node node = new S125Node("aton.uk.test_aton_no_2", null, S125_NO_2_CONTENT);

        // Encode the message
        String msg21 = GrAisUtils.encodeMsg21(node);

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
        // Create an S125Node message
        S125Node node = new S125Node("aton.uk.test_aton_no_3", null, S125_NO_3_CONTENT);

        // Encode the message
        String msg21 = GrAisUtils.encodeMsg21(node);

        // Assert that everything seems correct
        assertFalse(msg21.isEmpty());
        assertEquals(S125_NO_3_ENCODED, msg21);
    }
}