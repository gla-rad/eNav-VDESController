/*
 * Copyright (c) 2024 GLA Research and Development Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.grad.eNav.vdesCtrl.utils;

import org.grad.eNav.vdesCtrl.models.dtos.FeatureNameDto;
import org.grad.eNav.vdesCtrl.models.dtos.S125Node;
import org.grad.vdes1000.formats.ais.messages.AISMessage21;
import org.grad.vdes1000.formats.generic.AtonType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import tools.jackson.core.JacksonException;

import java.math.BigInteger;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class AISMessageUtilsTest {

    // Define the test S125 Messages Content
    public static final String S125_NO_1_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ns4:Dataset xmlns:ns1=\"http://www.w3.org/1999/xlink\" xmlns:ns2=\"http://www.iho.int/s100gml/5.0\" xmlns:ns3=\"http://www.opengis.net/gml/3.2\" xmlns:ns4=\"http://www.iho.int/S125/gml/cs0/1.0\" ns3:id=\"CorkHoleTestDataset\"><ns3:boundedBy><ns3:Envelope srsName=\"EPSG:4326\" srsDimension=\"1\"><ns3:lowerCorner>1.594 53.61</ns3:lowerCorner><ns3:upperCorner>1.594 53.61</ns3:upperCorner></ns3:Envelope></ns3:boundedBy><ns2:DatasetIdentificationInformation><ns2:encodingSpecification>S-100 Part 10b</ns2:encodingSpecification><ns2:encodingSpecificationEdition>1.0</ns2:encodingSpecificationEdition><ns2:productIdentifier>S-125</ns2:productIdentifier><ns2:productEdition>0.0.1</ns2:productEdition><ns2:applicationProfile>test</ns2:applicationProfile><ns2:datasetFileIdentifier>junit</ns2:datasetFileIdentifier><ns2:datasetTitle>AtoN Test Dataset</ns2:datasetTitle><ns2:datasetReferenceDate>2001-01-01</ns2:datasetReferenceDate><ns2:datasetLanguage>eng</ns2:datasetLanguage><ns2:datasetAbstract>Test dataset for unit testing</ns2:datasetAbstract><ns2:datasetTopicCategory>oceans</ns2:datasetTopicCategory><ns2:datasetPurpose>base</ns2:datasetPurpose><ns2:updateNumber>0</ns2:updateNumber></ns2:DatasetIdentificationInformation><ns4:members><ns4:AtoNStatusInformation xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"ns4:AtoNStatusInformationType\" ns3:id=\"ID002\"><ns4:changeDetails><ns4:radioAidsChange>AIS transmitter operating properly</ns4:radioAidsChange></ns4:changeDetails><ns4:changeTypes>Advance notice of changes</ns4:changeTypes></ns4:AtoNStatusInformation><ns4:VirtualAISAidToNavigation xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"ns4:VirtualAISAidToNavigationType\" ns3:id=\"ID001\"><ns3:boundedBy><ns3:Envelope srsName=\"EPSG:4326\" srsDimension=\"1\"><ns3:lowerCorner>1.594 53.61</ns3:lowerCorner><ns3:upperCorner>1.594 53.61</ns3:upperCorner></ns3:Envelope></ns3:boundedBy><ns4:dateEnd><ns4:date>2099-01-01</ns4:date></ns4:dateEnd><ns4:dateStart><ns4:date>2001-01-01</ns4:date></ns4:dateStart><ns4:idCode>urn:mrn:grad:aton:test:test_aton_no_1</ns4:idCode><ns4:featureName><ns4:displayName>true</ns4:displayName><ns4:language>eng</ns4:language><ns4:name>Test AtoN No 1</ns4:name></ns4:featureName><ns4:seasonalActionRequired>1</ns4:seasonalActionRequired><ns4:atonStatus ns1:href=\"ID001\" ns1:role=\"association\" ns1:arcrole=\"urn:IALA:S125:roles:association\"/><ns4:estimatedRangeOfTransmission>20</ns4:estimatedRangeOfTransmission><ns4:MMSICode>992359598</ns4:MMSICode><ns4:status>confirmed</ns4:status><ns4:virtualAISAidToNavigationType>Special Purpose</ns4:virtualAISAidToNavigationType><ns4:geometry><ns2:pointProperty><ns2:Point srsName=\"EPSG:4326\" srsDimension=\"1\" ns3:id=\"AtoNPoint1\"><ns3:pos srsName=\"EPSG:4326\" srsDimension=\"1\">1.594 53.61</ns3:pos></ns2:Point></ns2:pointProperty></ns4:geometry></ns4:VirtualAISAidToNavigation></ns4:members></ns4:Dataset>";
    public static final String S125_NO_2_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ns4:Dataset xmlns:ns1=\"http://www.w3.org/1999/xlink\" xmlns:ns2=\"http://www.iho.int/s100gml/5.0\" xmlns:ns3=\"http://www.opengis.net/gml/3.2\" xmlns:ns4=\"http://www.iho.int/S125/gml/cs0/1.0\" ns3:id=\"CorkHoleTestDataset\"><ns3:boundedBy><ns3:Envelope srsName=\"EPSG:4326\" srsDimension=\"1\"><ns3:lowerCorner>1.4233333 51.8916667</ns3:lowerCorner><ns3:upperCorner>1.4233333 51.8916667</ns3:upperCorner></ns3:Envelope></ns3:boundedBy><ns2:DatasetIdentificationInformation><ns2:encodingSpecification>S-100 Part 10b</ns2:encodingSpecification><ns2:encodingSpecificationEdition>1.0</ns2:encodingSpecificationEdition><ns2:productIdentifier>S-125</ns2:productIdentifier><ns2:productEdition>0.0.1</ns2:productEdition><ns2:applicationProfile>test</ns2:applicationProfile><ns2:datasetFileIdentifier>junit</ns2:datasetFileIdentifier><ns2:datasetTitle>AtoN Test Dataset</ns2:datasetTitle><ns2:datasetReferenceDate>2001-01-01</ns2:datasetReferenceDate><ns2:datasetLanguage>eng</ns2:datasetLanguage><ns2:datasetAbstract>Test dataset for unit testing</ns2:datasetAbstract><ns2:datasetTopicCategory>oceans</ns2:datasetTopicCategory><ns2:datasetPurpose>base</ns2:datasetPurpose><ns2:updateNumber>0</ns2:updateNumber></ns2:DatasetIdentificationInformation><ns4:members><ns4:AtoNStatusInformation xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"ns4:AtoNStatusInformationType\" ns3:id=\"ID002\"><ns4:changeDetails><ns4:radioAidsChange>AIS transmitter operating properly</ns4:radioAidsChange></ns4:changeDetails><ns4:changeTypes>Advance notice of changes</ns4:changeTypes></ns4:AtoNStatusInformation><ns4:VirtualAISAidToNavigation xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"ns4:VirtualAISAidToNavigationType\" ns3:id=\"ID001\"><ns3:boundedBy><ns3:Envelope srsName=\"EPSG:4326\" srsDimension=\"1\"><ns3:lowerCorner>53.61 1.594</ns3:lowerCorner><ns3:upperCorner>53.61 1.594</ns3:upperCorner></ns3:Envelope></ns3:boundedBy><ns4:dateEnd><ns4:date>2099-01-01</ns4:date></ns4:dateEnd><ns4:dateStart><ns4:date>2001-01-01</ns4:date></ns4:dateStart><ns4:idCode>urn:mrn:grad:aton:test:test_aton_no_2</ns4:idCode><ns4:featureName><ns4:displayName>true</ns4:displayName><ns4:language>eng</ns4:language><ns4:name>Test AtoN No 2</ns4:name></ns4:featureName><ns4:seasonalActionRequired>1</ns4:seasonalActionRequired><ns4:atonStatus ns1:href=\"ID001\" ns1:role=\"association\" ns1:arcrole=\"urn:IALA:S125:roles:association\"/><ns4:estimatedRangeOfTransmission>20</ns4:estimatedRangeOfTransmission><ns4:MMSICode>992359598</ns4:MMSICode><ns4:status>confirmed</ns4:status><ns4:virtualAISAidToNavigationType>Special Purpose</ns4:virtualAISAidToNavigationType><ns4:geometry><ns2:pointProperty><ns2:Point srsName=\"EPSG:4326\" srsDimension=\"1\" ns3:id=\"AtoNPoint1\"><ns3:pos srsName=\"EPSG:4326\" srsDimension=\"1\">53.61 1.594</ns3:pos></ns2:Point></ns2:pointProperty></ns4:geometry></ns4:VirtualAISAidToNavigation></ns4:members></ns4:Dataset>";
    public static final String S125_NO_3_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ns4:Dataset xmlns:ns1=\"http://www.w3.org/1999/xlink\" xmlns:ns2=\"http://www.iho.int/s100gml/5.0\" xmlns:ns3=\"http://www.opengis.net/gml/3.2\" xmlns:ns4=\"http://www.iho.int/S125/gml/cs0/1.0\" ns3:id=\"CorkHoleTestDataset\"><ns3:boundedBy><ns3:Envelope srsName=\"EPSG:4326\" srsDimension=\"1\"><ns3:lowerCorner>1.594 53.61</ns3:lowerCorner><ns3:upperCorner>1.594 53.61</ns3:upperCorner></ns3:Envelope></ns3:boundedBy><ns2:DatasetIdentificationInformation><ns2:encodingSpecification>S-100 Part 10b</ns2:encodingSpecification><ns2:encodingSpecificationEdition>1.0</ns2:encodingSpecificationEdition><ns2:productIdentifier>S-125</ns2:productIdentifier><ns2:productEdition>0.0.1</ns2:productEdition><ns2:applicationProfile>test</ns2:applicationProfile><ns2:datasetFileIdentifier>junit</ns2:datasetFileIdentifier><ns2:datasetTitle>AtoN Test Dataset</ns2:datasetTitle><ns2:datasetReferenceDate>2001-01-01</ns2:datasetReferenceDate><ns2:datasetLanguage>eng</ns2:datasetLanguage><ns2:datasetAbstract>Test dataset for unit testing</ns2:datasetAbstract><ns2:datasetTopicCategory>oceans</ns2:datasetTopicCategory><ns2:datasetPurpose>base</ns2:datasetPurpose><ns2:updateNumber>0</ns2:updateNumber></ns2:DatasetIdentificationInformation><ns4:members><ns4:AtoNStatusInformation xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"ns4:AtoNStatusInformationType\" ns3:id=\"ID002\"><ns4:changeDetails><ns4:radioAidsChange>AIS transmitter operating properly</ns4:radioAidsChange></ns4:changeDetails><ns4:changeTypes>Advance notice of changes</ns4:changeTypes></ns4:AtoNStatusInformation><ns4:VirtualAISAidToNavigation xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"ns4:VirtualAISAidToNavigationType\" ns3:id=\"ID001\"><ns3:boundedBy><ns3:Envelope srsName=\"EPSG:4326\" srsDimension=\"1\"><ns3:lowerCorner>1.594 53.61</ns3:lowerCorner><ns3:upperCorner>1.594 53.61</ns3:upperCorner></ns3:Envelope></ns3:boundedBy><ns4:dateEnd><ns4:date>2099-01-01</ns4:date></ns4:dateEnd><ns4:dateStart><ns4:date>2001-01-01</ns4:date></ns4:dateStart><ns4:idCode>urn:mrn:grad:aton:test:test_aton_no_3</ns4:idCode><ns4:featureName><ns4:displayName>true</ns4:displayName><ns4:language>eng</ns4:language><ns4:name>Test AtoN No 3</ns4:name></ns4:featureName><ns4:seasonalActionRequired>1</ns4:seasonalActionRequired><ns4:atonStatus ns1:href=\"ID001\" ns1:role=\"association\" ns1:arcrole=\"urn:IALA:S125:roles:association\"/><ns4:estimatedRangeOfTransmission>20</ns4:estimatedRangeOfTransmission><ns4:MMSICode>992359598</ns4:MMSICode><ns4:status>confirmed</ns4:status><ns4:virtualAISAidToNavigationType>Special Purpose</ns4:virtualAISAidToNavigationType><ns4:geometry><ns2:pointProperty><ns2:Point srsName=\"EPSG:4326\" srsDimension=\"1\" ns3:id=\"AtoNPoint1\"><ns3:pos srsName=\"EPSG:4326\" srsDimension=\"1\">1.594 53.61</ns3:pos></ns2:Point></ns2:pointProperty></ns4:geometry></ns4:VirtualAISAidToNavigation></ns4:members></ns4:Dataset>";

    // Class Variables
    GeometryFactory factory ;

    /**
     * Common setup for all the tests.
     */
    @BeforeEach
    void setUp() {
        // Create a temp geometry factory to get a test geometries
        this.factory = new GeometryFactory(new PrecisionModel(), 4326);
    }

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
    public void testS125NodeConstructorNo1() throws JacksonException {
        // Create an S125Node message
        S125Node node = new S125Node("aton.uk.test_aton_no_1", null, S125_NO_1_CONTENT);
        FeatureNameDto featureNameDto = new FeatureNameDto();
        featureNameDto.setName("Test AtoN No 1");
        featureNameDto.setDisplayName(true);
        node.setFeatureNames(Collections.singleton(featureNameDto));
        node.setGeometry(this.factory.createPoint(new Coordinate(53.61, 1.594)));
        node.setMmsiCode(BigInteger.valueOf(992359598));

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
    public void testS125NodeConstructorNo2() throws JacksonException {
        // Create an S125Node message
        S125Node node = new S125Node("aton.uk.test_aton_no_2", null, S125_NO_2_CONTENT);
        FeatureNameDto featureNameDto = new FeatureNameDto();
        featureNameDto.setName("Test AtoN No 2");
        featureNameDto.setDisplayName(true);
        node.setFeatureNames(Collections.singleton(featureNameDto));
        node.setGeometry(this.factory.createPoint(new Coordinate(1.594, 53.61)));
        node.setMmsiCode(BigInteger.valueOf(992359598));

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
    public void testS125NodeConstructorNo3() throws JacksonException {
        // Create an S125Node message
        S125Node node = new S125Node("aton.uk.test_aton_no_3", null, S125_NO_3_CONTENT);
        FeatureNameDto featureNameDto = new FeatureNameDto();
        featureNameDto.setName("Test AtoN No 3");
        featureNameDto.setDisplayName(true);
        node.setFeatureNames(Collections.singleton(featureNameDto));
        node.setGeometry(this.factory.createPoint(new Coordinate(53.61, 1.594)));
        node.setMmsiCode(BigInteger.valueOf(992359598));

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
        assertThrows(JacksonException.class, () -> AISMessageUtils.s125ToAisMessage21(node));
    }

}