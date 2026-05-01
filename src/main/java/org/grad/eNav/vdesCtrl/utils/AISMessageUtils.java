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

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.grad.eNav.vdesCtrl.models.dtos.FeatureNameDto;
import org.grad.eNav.vdesCtrl.models.dtos.S100AbstractNode;
import org.grad.eNav.vdesCtrl.models.dtos.S125Node;
import org.grad.vdes1000.formats.ais.messages.AISMessage21;
import org.grad.vdes1000.formats.generic.AtonType;
import org.locationtech.jts.geom.*;
import tools.jackson.dataformat.xml.XmlMapper;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;

/**
 * The S-100 Utility Class.
 *
 * A static utility function class that allows easily manipulation of the S-100
 * messages.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Slf4j
public class AISMessageUtils {

    /**
     * Constructors from an S125Node object.
     *
     * @param s125Node the S125Node object
     */
    public static AISMessage21 s125ToAisMessage21(S125Node s125Node) throws JacksonException {
        // Default at first
        final AISMessage21 aisMessage21 = new AISMessage21();

        // ===================================================================//
        // The JAXB parsing functionality here might cause issues if we       //
        // decide to build this application natively. Therefore, we can opt   //
        // for a much simpler solution where we process the S-100 data using  //
        // JSON and try to pick the minimal fields we are interested in.      //
        // ===================================================================//
        final JsonNode datasetNode = new XmlMapper().readTree(s125Node.getContent());
        final Iterator<Map.Entry<String, JsonNode>> datasetMembers = datasetNode.get("members").properties().iterator();
        // ===================================================================//

        String atonNodeType;
        JsonNode atonNode;
        AtonType atonType;
        do {
            Map.Entry<String, JsonNode> atonNodeEntry = datasetMembers.next();
            atonNodeType = atonNodeEntry.getKey();
            atonNode = atonNodeEntry.getValue();
            atonType = AISMessageUtils.s125TypeToAtonType(atonNodeType, atonNode);
        } while(datasetMembers.hasNext() && atonType == AtonType.DEFAULT);

        // Extract the AtoN Information
        Optional.of(s125Node)
                .map(S125Node::getIdCode)
                .ifPresent(aisMessage21::setUid);
        Optional.of(s125Node)
                .map(S125Node::getFeatureNames)
                .orElse(Collections.emptySet())
                .stream()
                .filter(FeatureNameDto::getDisplayName)
                .map(FeatureNameDto::getName)
                .findFirst()
                .ifPresent(aisMessage21::setName);
        Optional.of(s125Node)
                .map(S100AbstractNode::getGeometry)
                .filter(Point.class::isInstance)
                .map(Point.class::cast)
                .map(Point::getCoordinate)
                .map(Coordinate::getX)
                .ifPresent(aisMessage21::setLatitude);
        Optional.of(s125Node)
                .map(S100AbstractNode::getGeometry)
                .filter(Point.class::isInstance)
                .map(Point.class::cast)
                .map(Point::getCoordinate)
                .map(Coordinate::getY)
                .ifPresent(aisMessage21::setLongitude);
        Optional.of(s125Node)
                .map(S125Node::getMmsiCode)
                .map(BigInteger::intValueExact)
                .ifPresent(aisMessage21::setMmsi);
        aisMessage21.setLength((int) Math.round(Optional.ofNullable(atonNode.get("length")).map(JsonNode::asDouble).orElse(0.0)));
        aisMessage21.setWidth((int) Math.round(Optional.ofNullable(atonNode.get("width")).map(JsonNode::asDouble).orElse(0.0)));
        aisMessage21.setRaim(false);
        aisMessage21.setAtonType(AISMessageUtils.s125TypeToAtonType(atonNodeType, atonNode));
        aisMessage21.setVaton(atonNodeType.equals("VirtualAISAidToNavigation"));
        aisMessage21.setTimestamp(LocalDateTime.now());

        //Return the populated AIS message
        return aisMessage21;
    }

    /**
     * A helper function that determines the type of the AtoN based on the
     * class of the S-125 feature type.
     *
     * @param atonNodeType the S-125 AtoN Node Type
     * @param atonNode the S-125 AtoN node object in JSON format
     * @return the determined AtoN type for AIS
     */
    protected static AtonType s125TypeToAtonType(String atonNodeType, JsonNode atonNode) {
        // Try to figure our the type of the feature and determine the AtoN
        // type accordingly
        if(Objects.equals(atonNodeType, "BeaconSafeWater")) {
            return AtonType.BEACON_SAFE_WATER;
        } else if(Objects.equals(atonNodeType, "BeaconIsolatedDanger")) {
            return AtonType.BEACON_ISOLATED_DANGER;
        } else if(Objects.equals(atonNodeType, "BeaconLateral")) {
            return switch (atonNode.get("categoryOfLateralMark").asText()) {
                    case "port-hand lateral mark" -> AtonType.PORT_HAND_MARK;
                    case "starboard-hand lateral mark" -> AtonType.STARBOARD_HAND_MARK;
                    case "preferred channel to starboard lateral mark" -> AtonType.PREFERRED_PORT;
                    case "preferred channel to port lateral mark" -> AtonType.PREFERRED_STARBOARD;
                    default -> AtonType.DEFAULT;
            };
        } else if(Objects.equals(atonNodeType, "BeaconCardinal")) {
            return switch (atonNode.get("categoryOfCardinalMark").asText()) {
                case "north cardinal mark" -> AtonType.CARDINAL_NORTH;
                case "east cardinal mark" -> AtonType.CARDINAL_EAST;
                case "south cardinal mark" -> AtonType.CARDINAL_SOUTH;
                case "west cardinal mark" -> AtonType.CARDINAL_WEST;
                default -> AtonType.DEFAULT;
            };
        } else if(Objects.equals(atonNodeType, "BeaconSpecialPurposeGeneral")) {
            return AtonType.BEACON_SPECIAL_MARK;
        } else if(Objects.equals(atonNodeType, "BuoySafeWater")) {
            return AtonType.SAFE_WATER;
        } else if(Objects.equals(atonNodeType, "BuoyIsolatedDanger")) {
            return AtonType.ISOLATED_DANGER;
        } else if(Objects.equals(atonNodeType, "BuoyLateral")) {
            return switch (atonNode.get("categoryOfLateralMark").asText()) {
                case "port-hand lateral mark" -> AtonType.PORT_HAND_MARK;
                case "starboard-hand lateral mark" -> AtonType.STARBOARD_HAND_MARK;
                case "preferred channel to starboard lateral mark" -> AtonType.PREFERRED_PORT;
                case "preferred channel to port lateral mark" -> AtonType.PREFERRED_STARBOARD;
                default -> AtonType.DEFAULT;
            };
        } else if(Objects.equals(atonNodeType, "BuoyCardinal")) {
            return switch (atonNode.get("categoryOfCardinalMark").asText()) {
                case "north cardinal mark" -> AtonType.CARDINAL_NORTH;
                case "east cardinal mark" -> AtonType.CARDINAL_EAST;
                case "south cardinal mark" -> AtonType.CARDINAL_SOUTH;
                case "west cardinal mark" -> AtonType.CARDINAL_WEST;
                default -> AtonType.DEFAULT;
            };
        } else if(Objects.equals(atonNodeType, "BuoySpecialPurposeGeneral")) {
            return AtonType.SPECIAL_MARK;
        } else if(Objects.equals(atonNodeType, "BuoyInstallation")) {
            return AtonType.SPECIAL_MARK;
        } else if(Objects.equals(atonNodeType, "Lighthouse")) {
            return atonNode.get("colour").size() <= 1 ? AtonType.LIGHT_WITHOUT_SECTORS : AtonType.LIGHT_WITH_SECTORS;
        } else if(Objects.equals(atonNodeType, "OffshorePlatform")) {
            return AtonType.FIXED_STRUCTURE_OFFSHORE;
        } else if(Objects.equals(atonNodeType, "LightFloat")) {
            return AtonType.LIGHT_VESSEL;
        } else if(Objects.equals(atonNodeType, "VirtualAISAidToNavigation")) {
            return switch (atonNode.get("virtualAISAidToNavigationType").asText()) {
                case "New Danger Marking" -> AtonType.WRECK;
                case "North Cardinal" -> AtonType.CARDINAL_NORTH;
                case "East Cardinal" -> AtonType.CARDINAL_EAST;
                case "Special Purpose" -> AtonType.SPECIAL_MARK;
                case "South Cardinal" -> AtonType.CARDINAL_SOUTH;
                case "West Cardinal" -> AtonType.CARDINAL_WEST;
                case "Port Lateral" -> AtonType.PORT_HAND_MARK;
                case "Starboard Lateral" -> AtonType.STARBOARD_HAND_MARK;
                case "Preferred Channel to Port" -> AtonType.PREFERRED_PORT;
                case "Preferred Channel to Starboard" -> AtonType.PREFERRED_STARBOARD;
                case "Isolated Danger" -> AtonType.ISOLATED_DANGER;
                case "Safe Water" -> AtonType.SAFE_WATER;
                default -> AtonType.DEFAULT;
            };
        }

        // For everything else return the default
        return  AtonType.DEFAULT;
    }

}
