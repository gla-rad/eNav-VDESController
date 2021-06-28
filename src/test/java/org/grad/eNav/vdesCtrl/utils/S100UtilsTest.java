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

import _int.iho.s125.gml._0.*;
import net.opengis.gml._3.BoundingShapeType;
import net.opengis.gml._3.EnvelopeType;
import net.opengis.gml._3.Pos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class S100UtilsTest {

    // Test Variables
    private DataSet dataset;
    private String datasetXml;

    /**
     * Common setup for all the tests.
     */
    @BeforeEach
    void setup() {
        this.datasetXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<ns5:DataSet xmlns:ns1=\"http://www.opengis.net/gml/3.2\" xmlns:ns2=\"http://www.iho.int/s100gml/1.0\" xmlns:ns4=\"http://www.w3.org/1999/xlink\" xmlns:ns5=\"http://www.iho.int/S125/gml/0.1\" ns1:id=\"aton.uk.demo_aton\">\n"+
                "    <ns1:boundedBy>\n" +
                "        <ns1:Envelope srsName=\"EPSG:4326\">\n" +
                "            <ns1:lowerCorner>53.61 1.594</ns1:lowerCorner>\n" +
                "            <ns1:upperCorner>53.61 1.594</ns1:upperCorner>\n" +
                "        </ns1:Envelope>\n" +
                "    </ns1:boundedBy>\n" +
                "    <member>\n" +
                "        <ns5:S125_NavAidStructure>\n" +
                "            <ns1:boundedBy xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/>\n" +
                "            <mmsi>123457890</mmsi>\n" +
                "            <atonType>Special Mark</atonType>\n" +
                "            <deploymentType>Mobile</deploymentType>\n" +
                "            <raimFlag>false</raimFlag>\n" +
                "            <vatonFlag>true</vatonFlag>\n" +
                "        </ns5:S125_NavAidStructure>\n" +
                "    </member>\n" +
                "</ns5:DataSet>\n";

        // Create an S125 dataset type similar to the static XML defined here
        this.dataset = new DataSet();
        this.dataset.setId("aton.uk.demo_aton");

        // Create the bounding envelope
        EnvelopeType envelopeType = new EnvelopeType();
        envelopeType.setSrsName("EPSG:4326");
        Pos pos = new Pos();
        pos.getValues().addAll(Arrays.asList(new Double[]{53.61,1.594}));
        envelopeType.setLowerCorner(pos);
        envelopeType.setUpperCorner(pos);
        BoundingShapeType boundingShapeType = new BoundingShapeType();
        boundingShapeType.setEnvelope(envelopeType);
        this.dataset.setBoundedBy(boundingShapeType);

        // Add the S125 NavAidStructure feature
        S125NavAidStructureType s125NavAidStructureType = new S125NavAidStructureType();
        s125NavAidStructureType.setMmsi(123457890);
        s125NavAidStructureType.setAtonType(S125AtonType.SPECIAL_MARK);
        s125NavAidStructureType.setDeploymentType(S125DeploymentType.MOBILE);
        s125NavAidStructureType.setRaimFlag(Boolean.FALSE);
        s125NavAidStructureType.setVatonFlag(Boolean.TRUE);
        JAXBElement<S125StructureFeatureType> jaxbElement = new JAXBElement<>(
                new QName("http://www.iho.int/S125/gml/0.1", "S125_NavAidStructure"),
                S125StructureFeatureType.class,
                null,
                s125NavAidStructureType
        );
        MemberType memberType = new MemberType();
        memberType.setAbstractFeature(jaxbElement);
        this.dataset.getMembersAndImembers().add(memberType);
    }

    /**
     * Test that we can create (marshall) and XML based on an S125 dataset type
     * object.
     *
     * @throws JAXBException a JAXB exception thrown during the marshalling operation
     */
    @Test
    void testMarchallS125() throws JAXBException {
        String xml = S100Utils.marshalS125(this.dataset);
        assertNotNull(xml);
        assertEquals(this.datasetXml, xml);
    }

    /**
     * Test that we can generate (unmarshall) a G1128 POJO based on a valid
     * XML G1128 specification.
     *
     * @throws IOException any IO exceptions while reading the input XML file
     * @throws JAXBException a JAXB exception thrown during the unmarshalling operation
     */
    @Test
    void testUnmarshalS125() throws IOException, JAXBException {
        // Unmarshall it to a G1128 service instance object
        DataSet result = S100Utils.unmarshallS125(this.datasetXml);

        // Assert all information is correct
        assertNotNull(result);
        assertEquals(this.dataset.getId(), result.getId());

        // Assert the dataset type envelopes are correct
        EnvelopeType datasetTypeEnvelope = this.dataset.getBoundedBy().getEnvelope();
        EnvelopeType resultEnvelope = result.getBoundedBy().getEnvelope();
        assertEquals(datasetTypeEnvelope.getSrsName(), resultEnvelope.getSrsName());
        assertEquals(datasetTypeEnvelope.getLowerCorner().getValues(), resultEnvelope.getLowerCorner().getValues());
        assertEquals(datasetTypeEnvelope.getUpperCorner().getValues(), resultEnvelope.getUpperCorner().getValues());

        // Assert the S125 NavAidStructure feature information is correct
        S125NavAidStructureType datasetTypeMember = (S125NavAidStructureType) ((MemberType) this.dataset.getMembersAndImembers().get(0)).getAbstractFeature().getValue();
        S125NavAidStructureType resultMember =  (S125NavAidStructureType) ((MemberType) result.getMembersAndImembers().get(0)).getAbstractFeature().getValue();
        assertEquals(datasetTypeMember.getMmsi(), resultMember.getMmsi());
        assertEquals(datasetTypeMember.getAtonType(), resultMember.getAtonType());
        assertEquals(datasetTypeMember.getDeploymentType(), resultMember.getDeploymentType());
        assertEquals(datasetTypeMember.isRaimFlag(), resultMember.isRaimFlag());
        assertEquals(datasetTypeMember.isVatonFlag(), resultMember.isVatonFlag());
    }

}