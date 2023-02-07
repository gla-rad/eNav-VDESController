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

import org.grad.eNav.vdesCtrl.models.dtos.S125Node;
import org.grad.vdes1000.ais.messages.AISMessage21;
import org.grad.vdes1000.generic.AtonType;
import org.junit.jupiter.api.Test;

import jakarta.xml.bind.JAXBException;

import static org.junit.jupiter.api.Assertions.*;

class AISMessageUtilsTest {


    // Define the test S125 Messages Content
    public static final String S125_NO_1_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ns6:DataSet xmlns:ns1=\"http://www.opengis.net/gml/3.2\" xmlns:ns2=\"http://www.iho.int/s100gml/1.0\" xmlns:ns4=\"http://www.w3.org/1999/xlink\" xmlns:ns5=\"http://www.iala-aism.int/S125/gml/0.0.1Base\" xmlns:ns6=\"http://www.iala-aism.int/S125/gml/0.0.1\" xmlns:ns7=\"http://www.iho.int/s100gml/1.0+EXT\" ns1:id=\"Cork Hole Test Dataset\"><ns1:boundedBy><ns1:Envelope srsName=\"EPSG:4326\"><ns1:lowerCorner>1.594 53.61</ns1:lowerCorner><ns1:upperCorner>1.594 53.61</ns1:upperCorner></ns1:Envelope></ns1:boundedBy><DatasetIdentificationInformation><ns2:encodingSpecification>S100 Part 10b</ns2:encodingSpecification><ns2:encodingSpecificationEdition>1.0</ns2:encodingSpecificationEdition><ns2:productIdentifier>S-125</ns2:productIdentifier><ns2:productEdition>0.0.1</ns2:productEdition><ns2:datasetFileIdentifier>junit</ns2:datasetFileIdentifier><ns2:datasetTitle>S-125 Cork Hole Test Dataset</ns2:datasetTitle><ns2:datasetReferenceDate>2001-01-01</ns2:datasetReferenceDate><ns2:datasetLanguage>en</ns2:datasetLanguage><ns2:datasetAbstract>Test dataset for unit testing</ns2:datasetAbstract></DatasetIdentificationInformation><member><ns6:S125_VirtualAISAidToNavigation ns1:id=\"ID001\"><ns1:boundedBy><ns1:Envelope srsName=\"EPSG:4326\"><ns1:lowerCorner>1.594, 53.616</ns1:lowerCorner><ns1:upperCorner>1.594, 53.61</ns1:upperCorner></ns1:Envelope></ns1:boundedBy><ns2:featureObjectIdentifier><ns2:agency>GRAD</ns2:agency><ns2:featureIdentificationNumber>1</ns2:featureIdentificationNumber><ns2:featureIdentificationSubdivision>0</ns2:featureIdentificationSubdivision></ns2:featureObjectIdentifier><atonNumber>aton.uk.test_aton_no_1</atonNumber><idCode>001</idCode><textualDescription>Test AtoN No 1</textualDescription><textualDescriptionInNationalLanguage>Description of Test AtoN for Cork Hole</textualDescriptionInNationalLanguage><virtualAISAidToNavigationType>special purpose</virtualAISAidToNavigationType><objectNameInNationalLanguage>Cork Hole Test</objectNameInNationalLanguage><objectName>Cork Hole Test</objectName><status>confirmed</status><estimatedRangeOfTransmission>20</estimatedRangeOfTransmission><MMSICode>992359598</MMSICode><geometry><ns2:pointProperty><ns2:Point ns1:id=\"ID002\"><ns1:pos>1.594 53.61</ns1:pos></ns2:Point></ns2:pointProperty></geometry></ns6:S125_VirtualAISAidToNavigation></member></ns6:DataSet>";
    public static final String S125_NO_2_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ns6:DataSet xmlns:ns1=\"http://www.opengis.net/gml/3.2\" xmlns:ns2=\"http://www.iho.int/s100gml/1.0\" xmlns:ns4=\"http://www.w3.org/1999/xlink\" xmlns:ns5=\"http://www.iala-aism.int/S125/gml/0.0.1Base\" xmlns:ns6=\"http://www.iala-aism.int/S125/gml/0.0.1\" xmlns:ns7=\"http://www.iho.int/s100gml/1.0+EXT\" ns1:id=\"Cork Hole Test Dataset\"><ns1:boundedBy><ns1:Envelope srsName=\"EPSG:4326\"><ns1:lowerCorner>53.61 1.594</ns1:lowerCorner><ns1:upperCorner>53.61 1.594</ns1:upperCorner></ns1:Envelope></ns1:boundedBy><DatasetIdentificationInformation><ns2:encodingSpecification>S100 Part 10b</ns2:encodingSpecification><ns2:encodingSpecificationEdition>1.0</ns2:encodingSpecificationEdition><ns2:productIdentifier>S-125</ns2:productIdentifier><ns2:productEdition>0.0.1</ns2:productEdition><ns2:datasetFileIdentifier>junit</ns2:datasetFileIdentifier><ns2:datasetTitle>S-125 Cork Hole Test Dataset</ns2:datasetTitle><ns2:datasetReferenceDate>2001-01-01</ns2:datasetReferenceDate><ns2:datasetLanguage>en</ns2:datasetLanguage><ns2:datasetAbstract>Test dataset for unit testing</ns2:datasetAbstract></DatasetIdentificationInformation><member><ns6:S125_VirtualAISAidToNavigation ns1:id=\"ID001\"><ns1:boundedBy><ns1:Envelope srsName=\"EPSG:4326\"><ns1:lowerCorner>1.594, 53.616</ns1:lowerCorner><ns1:upperCorner>1.594, 53.61</ns1:upperCorner></ns1:Envelope></ns1:boundedBy><ns2:featureObjectIdentifier><ns2:agency>GRAD</ns2:agency><ns2:featureIdentificationNumber>1</ns2:featureIdentificationNumber><ns2:featureIdentificationSubdivision>0</ns2:featureIdentificationSubdivision></ns2:featureObjectIdentifier><atonNumber>aton.uk.test_aton_no_2</atonNumber><idCode>001</idCode><textualDescription>Test AtoN No 2</textualDescription><textualDescriptionInNationalLanguage>Description of Test AtoN for Cork Hole</textualDescriptionInNationalLanguage><virtualAISAidToNavigationType>special purpose</virtualAISAidToNavigationType><objectNameInNationalLanguage>Cork Hole Test</objectNameInNationalLanguage><objectName>Cork Hole Test</objectName><status>confirmed</status><estimatedRangeOfTransmission>20</estimatedRangeOfTransmission><MMSICode>992359598</MMSICode><geometry><ns2:pointProperty><ns2:Point ns1:id=\"ID002\"><ns1:pos>53.61 1.594</ns1:pos></ns2:Point></ns2:pointProperty></geometry></ns6:S125_VirtualAISAidToNavigation></member></ns6:DataSet>";
    public static final String S125_NO_3_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ns6:DataSet xmlns:ns1=\"http://www.opengis.net/gml/3.2\" xmlns:ns2=\"http://www.iho.int/s100gml/1.0\" xmlns:ns4=\"http://www.w3.org/1999/xlink\" xmlns:ns5=\"http://www.iala-aism.int/S125/gml/0.0.1Base\" xmlns:ns6=\"http://www.iala-aism.int/S125/gml/0.0.1\" xmlns:ns7=\"http://www.iho.int/s100gml/1.0+EXT\" ns1:id=\"Cork Hole Test Dataset\"><ns1:boundedBy><ns1:Envelope srsName=\"EPSG:4326\"><ns1:lowerCorner>1.594 53.61</ns1:lowerCorner><ns1:upperCorner>1.594 53.61</ns1:upperCorner></ns1:Envelope></ns1:boundedBy><DatasetIdentificationInformation><ns2:encodingSpecification>S100 Part 10b</ns2:encodingSpecification><ns2:encodingSpecificationEdition>1.0</ns2:encodingSpecificationEdition><ns2:productIdentifier>S-125</ns2:productIdentifier><ns2:productEdition>0.0.1</ns2:productEdition><ns2:datasetFileIdentifier>junit</ns2:datasetFileIdentifier><ns2:datasetTitle>S-125 Cork Hole Test Dataset</ns2:datasetTitle><ns2:datasetReferenceDate>2001-01-01</ns2:datasetReferenceDate><ns2:datasetLanguage>en</ns2:datasetLanguage><ns2:datasetAbstract>Test dataset for unit testing</ns2:datasetAbstract></DatasetIdentificationInformation><member><ns6:S125_VirtualAISAidToNavigation ns1:id=\"ID001\"><ns1:boundedBy><ns1:Envelope srsName=\"EPSG:4326\"><ns1:lowerCorner>1.594, 53.616</ns1:lowerCorner><ns1:upperCorner>1.594, 53.61</ns1:upperCorner></ns1:Envelope></ns1:boundedBy><ns2:featureObjectIdentifier><ns2:agency>GRAD</ns2:agency><ns2:featureIdentificationNumber>1</ns2:featureIdentificationNumber><ns2:featureIdentificationSubdivision>0</ns2:featureIdentificationSubdivision></ns2:featureObjectIdentifier><atonNumber>aton.uk.test_aton_no_3</atonNumber><idCode>001</idCode><textualDescription>Test AtoN No 3</textualDescription><textualDescriptionInNationalLanguage>Description of Test AtoN for Cork Hole</textualDescriptionInNationalLanguage><virtualAISAidToNavigationType>special purpose</virtualAISAidToNavigationType><objectNameInNationalLanguage>Cork Hole Test</objectNameInNationalLanguage><objectName>Cork Hole Test</objectName><status>confirmed</status><estimatedRangeOfTransmission>20</estimatedRangeOfTransmission><MMSICode>992359598</MMSICode><geometry><ns2:pointProperty><ns2:Point ns1:id=\"ID002\"><ns1:pos>1.594 53.61</ns1:pos></ns2:Point></ns2:pointProperty></geometry></ns6:S125_VirtualAISAidToNavigation></member></ns6:DataSet>";

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
        AISMessage21 msgParams = AISMessageUtils.s125ToAisMessage21(node);

        // Assert that all variables have been initialised correctly
        assertEquals(992359598, msgParams.getMmsi());
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
        AISMessage21 aisMessage21 = AISMessageUtils.s125ToAisMessage21(node);

        // Assert that all variables have been initialised correctly
        assertEquals(992359598, aisMessage21.getMmsi());
        assertEquals(AtonType.SPECIAL_MARK, aisMessage21.getAtonType());
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
        AISMessage21 aisMessage21 = AISMessageUtils.s125ToAisMessage21(node);

        // Assert that all variables have been initialised correctly
        assertEquals(992359598, aisMessage21.getMmsi());
        assertEquals(AtonType.SPECIAL_MARK, aisMessage21.getAtonType());
        assertEquals("Test AtoN No 3", aisMessage21.getName());
        assertEquals(53.61, aisMessage21.getLatitude());
        assertEquals(1.594, aisMessage21.getLongitude());
        assertEquals(0, aisMessage21.getLength());
        assertEquals(0, aisMessage21.getWidth());
        assertEquals(Boolean.FALSE, aisMessage21.getRaim());
        assertEquals(Boolean.TRUE, aisMessage21.getVaton());
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
        assertThrows(JAXBException.class, () -> AISMessageUtils.s125ToAisMessage21(node));
    }

}