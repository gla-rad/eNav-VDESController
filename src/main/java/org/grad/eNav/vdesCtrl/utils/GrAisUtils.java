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

import _int.iho.s100gml._1.AbstractFeatureType;
import _int.iho.s125.gml._0.DatasetType;
import _int.iho.s125.gml._0.MemberType;
import _int.iho.s125.gml._0.S125NavAidStructureType;
import net.opengis.gml._3.AbstractMemberType;
import net.opengis.gml._3.EnvelopeType;
import org.grad.eNav.vdesCtrl.models.domain.AtonType;
import org.grad.eNav.vdesCtrl.models.dtos.S125Node;
import org.locationtech.jts.geom.Envelope;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Predicates.not;

/**
 * The GNURadio AIS Utility Class.
 *
 * A static utility function class that allows easily manipulation of the
 * GNURadio AIS station messages.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public class GrAisUtils {

    /**
     * The GNURadio AIS BlackToolkit requires the AIS message sentence in a
     * binary sentence as an input. This utility function is able to generate
     * this binary message as a string, so that is can by passed on tho the
     * UDP socket GNURadio is listening to.
     *
     * @param s125Node the S125Node to construct the AIS binary message from
     * @return The AIS binary message to be transmitted through GNURadio
     * @throws JAXBException
     */
    public static String encodeMsg21(S125Node s125Node) throws JAXBException {
        // Create a string builder to start ith
        StringBuilder aisBuilder = new StringBuilder();

        // Unmarshall the dataset content
        DatasetType dataset = S100Utils.unmarshallS125(s125Node.getContent());
        EnvelopeType datasetEnvelope = dataset.getBoundedBy().getEnvelope();

        // For now only build one message per node - member position 0
        Optional.of(dataset)
                .map(DatasetType::getMemberOrImember)
                .filter(not(List::isEmpty))
                .map(l -> l.get(0))
                .filter(MemberType.class::isInstance)
                .map(MemberType.class::cast)
                .map(MemberType::getAbstractFeature)
                .map(JAXBElement::getValue)
                .filter(S125NavAidStructureType.class::isInstance)
                .map(S125NavAidStructureType.class::cast)
                .ifPresent(navAid -> {
                    // Extract the S125 Member NavAid Information
                    Integer mmsi = navAid.getMmsi();
                    AtonType atonType = AtonType.fromString(navAid.getAtonType().value());
                    String name = navAid.getFeatureName().getName();
                    String nameExt = "";
                    if(name.length() > 20) {
                        name = navAid.getFeatureName().getName().substring(0, 20);
                        nameExt = navAid.getFeatureName().getName().substring(20);
                    }
                    Double lat = navAid.getGeometry().getPointProperty().getPoint().getPos().getValue().get(0);
                    Double lon = navAid.getGeometry().getPointProperty().getPoint().getPos().getValue().get(1);
                    Integer halfLength = navAid.isVatonFlag() ? 0 : Math.round(Optional.ofNullable(navAid.getLength()).orElse(0)/2);
                    Integer halfWidth = navAid.isVatonFlag() ? 0 : Math.round(Optional.ofNullable(navAid.getWidth()).orElse(0)/2);
                    Boolean raim = navAid.isRaimFlag();
                    Boolean virtual = navAid.isVatonFlag();

                    // Build the string
                    aisBuilder
                            .append(StringBinUtils.convertIntToBinary(21,6)) // AIS Message 21
                            .append(StringBinUtils.convertIntToBinary(0,2)) // Repeat Indicator
                            .append(StringBinUtils.convertIntToBinary(mmsi, 30)) // MMSI
                            .append(StringBinUtils.convertIntToBinary(atonType.getCode(),5)) // The AtoN Type
                            .append(StringBinUtils.convertStringToBinary(name,120, true)) // The AtoN Name
                            .append(StringBinUtils.convertIntToBinary(0,1)) // The Accuracy
                            // Longitude/Latitude
                            .append(StringBinUtils.convertIntToBinary(new Long(Math.round(lon*600000)).intValue(),28)) // The Longitude
                            .append(StringBinUtils.convertIntToBinary(new Long(Math.round(lat*600000)).intValue(),27)) // The Latitude
                            // Dimension/Reference of position
                            .append(StringBinUtils.convertIntToBinary(halfLength,9)) // The Half Length
                            .append(StringBinUtils.convertIntToBinary(halfLength,9)) // The Half Length
                            .append(StringBinUtils.convertIntToBinary(halfWidth,6)) // The Half Width
                            .append(StringBinUtils.convertIntToBinary(halfWidth,6)) // The Half Width
                            // Additional info/flags
                            .append(StringBinUtils.convertIntToBinary(0,4)) // The Fix Field
                            .append(StringBinUtils.convertIntToBinary(60,6)) // The Time Field
                            .append(StringBinUtils.convertIntToBinary(0,1)) // Off Position Indicator
                            .append(StringBinUtils.convertIntToBinary(0,8)) // AtoN Status
                            .append(StringBinUtils.convertIntToBinary(raim?1:0,1)) // RAIM Flag
                            .append(StringBinUtils.convertIntToBinary(virtual?1:0,1)) // The Virtual Flag
                            .append(StringBinUtils.convertIntToBinary(0,2)) // ?
                            // Add the name extension is required
                            .append(StringBinUtils.convertStringToBinary(nameExt, nameExt.length() % 8, true));
                });

        // Finally pad to the multiple of 6 requirement
        String aisSentence = aisBuilder.toString();
        return StringBinUtils.padRight(aisSentence, aisSentence.length() + (6 - (aisSentence.length()%6))%6);
    }

}
