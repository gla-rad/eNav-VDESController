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

    // Define a test S125 Message Content
    public static final String S125_CONTENT = "<S125:DataSet xmlns:S125=\"http://www.iho.int/S125/gml/0.1\" xmlns:S100=\"http://www.iho.int/s100gml/1.0\" xmlns:gml=\"http://www.opengis.net/gml/3.2\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" gml:id=\"aton.uk.test_aton\" xsi:schemaLocation=\"http://www.iho.int/S125/gml/1.0 S125.xsd\"><gml:boundedBy><gml:Envelope srsName=\"EPSG:4326\"><gml:lowerCorner>53.61 1.594</gml:lowerCorner><gml:upperCorner>53.61 1.594</gml:upperCorner></gml:Envelope></gml:boundedBy><member><S125:S125_NavAidStructure><featureName><displayName>true</displayName><language>eng</language><name>Test AtoN</name></featureName><geometry><S100:pointProperty><S100:Point gml:id=\"G.aton.uk.test_aton.1\" srsName=\"EPSG:4326\"><gml:pos>53.61 1.594</gml:pos></S100:Point></S100:pointProperty></geometry><mmsi>123456789</mmsi><atonType>Special Mark</atonType><deploymentType>Mobile</deploymentType><raimFlag>false</raimFlag><vatonFlag>true</vatonFlag></S125:S125_NavAidStructure></member></S125:DataSet>";

    @Test
    public void testS125ToAisMsg21() throws JAXBException {
        // Create an S125Node message
        S125Node node = new S125Node("aton.uk.test_aton", null, S125_CONTENT);

        String msg21 = GrAisUtils.s125ToAisMsg21(node);
        System.out.println(msg21);
        assert(!msg21.isEmpty());
    }
}