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

import _int.iala_aism.s125.gml._0_0.*;
import _int.iho.s100.gml.base._1_0_Ext.PointCurveSurfaceProperty;
import _net.opengis.gml.profiles.*;
import lombok.extern.slf4j.Slf4j;
import org.grad.eNav.s125.utils.S125Utils;
import org.grad.eNav.vdesCtrl.models.dtos.S125Node;
import org.grad.vdes1000.ais.messages.AISMessage21;
import org.grad.vdes1000.generic.AtonType;
import org.locationtech.jts.geom.*;
import org.springframework.cglib.core.ReflectUtils;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;

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
     * @throws JAXBException when the S125Node XML content cannot be parsed
     */
    public static AISMessage21 s125ToAisMessage21(S125Node s125Node) throws JAXBException {
        // Default at first
        AISMessage21 aisMessage21 = new AISMessage21();

        // Try to unmarshall the S125Node object
        DataSet dataset = S125Utils.unmarshallS125(s125Node.getContent());

        // Extract the S125 Member NavAid Information
        Optional.ofNullable(dataset)
                .map(DataSet::getImembersAndMembers)
                .filter(((Predicate<List<AbstractFeatureMemberType>>) List::isEmpty).negate())
                .map(l -> l.get(0))
                .filter(MemberType.class::isInstance)
                .map(MemberType.class::cast)
                .map(MemberType::getAbstractFeature)
                .map(JAXBElement::getValue)
                .filter(S125AidsToNavigationType.class::isInstance)
                .map(S125AidsToNavigationType.class::cast)
                .ifPresent(s125Aton -> {
                    Optional.of(dataset)
                            .map(DataSet::getId)
                            .ifPresent(aisMessage21::setUid);
                    Optional.of(s125Aton)
                            .map(S125AidsToNavigationType::getTextualDescription)
                            .ifPresent(aisMessage21::setName);
                    Optional.of(s125Aton).
                            map(AISMessageUtils::s125FeatureTypeToAtonType)
                            .ifPresent(aisMessage21::setAtonType);
                    Optional.of(s125Aton)
                            .map(S125Utils::geomPerS125AidsToNavigationType)
                            .map(AISMessageUtils::s125PointCurveSurfaceToGeometry)
                            .filter(GeometryCollection.class::isInstance)
                            .map(GeometryCollection.class::cast)
                            .map(GeometryCollection::getCoordinate)
                            .map(Coordinate::getX)
                            .ifPresent(aisMessage21::setLongitude);
                    Optional.of(s125Aton)
                            .map(S125Utils::geomPerS125AidsToNavigationType)
                            .map(AISMessageUtils::s125PointCurveSurfaceToGeometry)
                            .filter(GeometryCollection.class::isInstance)
                            .map(GeometryCollection.class::cast)
                            .map(GeometryCollection::getCoordinate)
                            .map(Coordinate::getY)
                            .ifPresent(aisMessage21::setLatitude);
                    aisMessage21.setMmsi(AISMessageUtils.s125FeatureTypeField(s125Aton, "MMSICode", BigDecimal.class).intValueExact());
                    aisMessage21.setLength((int)Math.round(Optional.ofNullable(AISMessageUtils.s125FeatureTypeField(s125Aton, "length", BigDecimal.class)).map(BigDecimal::doubleValue).orElse(0.0)));
                    aisMessage21.setWidth((int)Math.round(Optional.ofNullable(AISMessageUtils.s125FeatureTypeField(s125Aton, "width", BigDecimal.class)).map(BigDecimal::doubleValue).orElse(0.0)));
                    aisMessage21.setRaim(false);
                    aisMessage21.setVaton(s125Aton instanceof S125VirtualAISAidToNavigationType);
                    aisMessage21.setTimestamp(LocalDateTime.now());
                });

        //Return the populated AIS message
        return aisMessage21;
    }

    /**
     * A helper function that determines the type of the AtoN based on the
     * class of the S-125 feature type.
     *
     * @param aidsToNavigationType the S-125 AtoN feature type
     * @return the determined AtoN type for AIS
     */
    protected static AtonType s125FeatureTypeToAtonType(S125AidsToNavigationType aidsToNavigationType) {
        // Try to figure our the type of the feature and determine the AtoN
        // type accordingly
        if(aidsToNavigationType instanceof S125BeaconSafeWaterType) {
            return AtonType.BEACON_SAFE_WATER;
        } else if(aidsToNavigationType instanceof S125BeaconIsolatedDangerType) {
            return AtonType.BEACON_ISOLATED_DANGER;
        } else if(aidsToNavigationType instanceof S125BeaconLateralType) {
            switch(((S125BeaconLateralType)aidsToNavigationType).getCategoryOfLateralMark()) {
                case PORT_HAND_LATERAL_MARK: return AtonType.PORT_HAND_MARK;
                case STARBOARD_HAND_LATERAL_MARK: return AtonType.STARBOARD_HAND_MARK;
                case PREFERRED_CHANNEL_TO_PORT_LATERAL_MARK: return AtonType.PREFERRED_PORT;
                case PREFERRED_CHANNEL_TO_STARBOARD_LATERAL_MARK: return AtonType.PREFERRED_STARBOARD;
                default: return AtonType.DEFAULT;
            }
        } else if(aidsToNavigationType instanceof S125BeaconCardinalType) {
            switch(((S125BeaconCardinalType)aidsToNavigationType).getCategoryOfCardinalMark()) {
                case NORTH_CARDINAL_MARK: return AtonType.CARDINAL_NORTH;
                case EAST_CARDINAL_MARK: return AtonType.CARDINAL_EAST;
                case SOUTH_CARDINAL_MARK: return AtonType.CARDINAL_SOUTH;
                case WEST_CARDINAL_MARK: return AtonType.CARDINAL_WEST;
                default: return AtonType.DEFAULT;
            }
        } else if(aidsToNavigationType instanceof S125BeaconSpecialPurposeGeneralType) {
            return AtonType.BEACON_SPECIAL_MARK;
        } else if(aidsToNavigationType instanceof S125BuoySafeWaterType) {
            return AtonType.SAFE_WATER;
        } else if(aidsToNavigationType instanceof S125BuoyIsolatedDangerType) {
            return AtonType.ISOLATED_DANGER;
        } else if(aidsToNavigationType instanceof S125BuoyLateralType) {
            switch(((S125BuoyLateralType)aidsToNavigationType).getCategoryOfLateralMark()) {
                case PORT_HAND_LATERAL_MARK: return AtonType.PORT_HAND_MARK;
                case STARBOARD_HAND_LATERAL_MARK: return AtonType.STARBOARD_HAND_MARK;
                case PREFERRED_CHANNEL_TO_PORT_LATERAL_MARK: return AtonType.PREFERRED_PORT;
                case PREFERRED_CHANNEL_TO_STARBOARD_LATERAL_MARK: return AtonType.PREFERRED_STARBOARD;
                default: return AtonType.DEFAULT;
            }
        } else if(aidsToNavigationType instanceof S125BuoyCardinalType) {
            switch(((S125BuoyCardinalType)aidsToNavigationType).getCategoryOfCardinalMark()) {
                case NORTH_CARDINAL_MARK: return AtonType.CARDINAL_NORTH;
                case EAST_CARDINAL_MARK: return AtonType.CARDINAL_EAST;
                case SOUTH_CARDINAL_MARK: return AtonType.CARDINAL_SOUTH;
                case WEST_CARDINAL_MARK: return AtonType.CARDINAL_WEST;
                default: return AtonType.DEFAULT;
            }
        } else if(aidsToNavigationType instanceof S125BuoySpecialPurposeGeneralType) {
            return AtonType.SPECIAL_MARK;
        } else if(aidsToNavigationType instanceof S125BuoyInstallationType) {
            return AtonType.SPECIAL_MARK;
        } else if(aidsToNavigationType instanceof S125LighthouseType) {
            return ((S125LighthouseType)aidsToNavigationType).getColours().size() <= 1 ?
                    AtonType.LIGHT_WITHOUT_SECTORS : AtonType.LIGHT_WITH_SECTORS;
        } else if(aidsToNavigationType instanceof S125OffshorePlatformType) {
            return AtonType.FIXED_STRUCTURE_OFFSHORE;
        } else if(aidsToNavigationType instanceof S125LightFloatType) {
            return AtonType.LIGHT_VESSEL;
        } else if(aidsToNavigationType instanceof S125VirtualAISAidToNavigationType) {
            switch(((S125VirtualAISAidToNavigationType)aidsToNavigationType).getVirtualAISAidToNavigationType()) {
                case EMERGENCY_WRECK_MARKING: return AtonType.WRECK;
                case NORTH_CARDINAL: return AtonType.CARDINAL_NORTH;
                case EAST_CARDINAL: return AtonType.CARDINAL_EAST;
                case SPECIAL_PURPOSE: return AtonType.SPECIAL_MARK;
                case SOUTH_CARDINAL: return AtonType.CARDINAL_SOUTH;
                case WEST_CARDINAL: return AtonType.CARDINAL_WEST;
                case PORT_LATERAL: return AtonType.PORT_HAND_MARK;
                case STARBOARD_LATERAL: return AtonType.STARBOARD_HAND_MARK;
                case PREFERRED_CHANNEL_TO_PORT: return AtonType.PREFERRED_PORT;
                case PREFERRED_CHANNEL_TO_STARBOARD: return AtonType.PREFERRED_STARBOARD;
                case ISOLATED_DANGER: return AtonType.ISOLATED_DANGER;
                case SAFE_WATER: return AtonType.SAFE_WATER;
                default: return AtonType.DEFAULT;
            }
        }

        // For everything else return the default
        return  AtonType.DEFAULT;
    }

    /**
     * Translates the generic point/curve/surface property of the S-125
     * feature type into a JTS geometry (most likely a geometry collection)
     * that can be understood and handled by the services.
     *
     * @param pointCurveSurface the S-100 point/curve/surface property
     * @return the respective geometry
     */
    protected static Geometry s125PointCurveSurfaceToGeometry(PointCurveSurfaceProperty pointCurveSurface) {
        final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        return Optional.ofNullable(pointCurveSurface)
                .map(pcs -> {
                    // Map based on the type of the populated geometry
                    if(Objects.nonNull(pcs.getPointProperty())) {
                        return Optional.of(pcs.getPointProperty())
                                .map(_int.iho.s100.gml.base._1_0.PointProperty::getPoint)
                                .map(_net.opengis.gml.profiles.PointType::getPos)
                                .map(pos -> new Coordinate(pos.getValues().get(0), pos.getValues().get(1)))
                                .map(geometryFactory::createPoint)
                                .map(Geometry.class::cast)
                                .orElse(geometryFactory.createEmpty(0));
                    } else if(Objects.nonNull(pcs.getCurveProperty())) {
                        return geometryFactory.createGeometryCollection(Optional.of(pcs.getCurveProperty())
                                .map(_int.iho.s100.gml.base._1_0.CurveProperty::getCurve)
                                .map(CurveType::getSegments)
                                .map(Segments::getAbstractCurveSegments)
                                .orElse(Collections.emptyList())
                                .stream()
                                .map(JAXBElement::getValue)
                                .filter(LineStringSegmentType.class::isInstance)
                                .map(LineStringSegmentType.class::cast)
                                .map(LineStringSegmentType::getPosList)
                                .map(AISMessageUtils::gmlPosListToCoordinates)
                                .map(coords -> coords.length == 1? geometryFactory.createPoint(coords[0]) : geometryFactory.createLineString(coords))
                                .toList()
                                .toArray(Geometry[]::new));
                    } else if(Objects.nonNull(pcs.getSurfaceProperty())) {
                        return geometryFactory.createGeometryCollection(Optional.of(pcs.getSurfaceProperty())
                                .map(_int.iho.s100.gml.base._1_0.SurfaceProperty::getAbstractSurface)
                                .map(JAXBElement::getValue)
                                .filter(SurfaceType.class::isInstance)
                                .map(SurfaceType.class::cast)
                                .map(SurfaceType::getPatches)
                                .map(Patches::getAbstractSurfacePatches)
                                .orElse(Collections.emptyList())
                                .stream()
                                .map(JAXBElement::getValue)
                                .filter(PolygonPatchType.class::isInstance)
                                .map(PolygonPatchType.class::cast)
                                .map(PolygonPatchType::getExterior)
                                .map(AbstractRingPropertyType::getAbstractRing)
                                .map(JAXBElement::getValue)
                                .filter(LinearRingType.class::isInstance)
                                .map(LinearRingType.class::cast)
                                .map(LinearRingType::getPosList)
                                .map(AISMessageUtils::gmlPosListToCoordinates)
                                .map(coords -> coords.length == 1? geometryFactory.createPoint(coords[0]) : geometryFactory.createPolygon(coords))
                                .toList()
                                .toArray(Geometry[]::new));
                    }
                    return null;
                })
                .orElseGet(() -> geometryFactory.createEmpty(-1));
    }

    /**
     * A simple utility function that splits the position list values by two
     * and generates TTS geometry coordinates by them.
     *
     * @param posList the provided position list
     * @return the respective coordinates
     */
    protected static Coordinate[] gmlPosListToCoordinates(PosList posList) {
        final List<Coordinate> result = new ArrayList<>();
        final Iterator<Double> iterator = posList.getValues().iterator();
        while(iterator.hasNext()) {
            result.add(new Coordinate(iterator.next(), iterator.next()));
        }
        return result.toArray(new Coordinate[]{});
    }

    /**
     * A helper function that uses Java Reflections to easily access and field
     * in the S-125 feature type. For example the geometry or the MMSI of the
     * feature type can be accessed this way.
     *
     * @param s125AidsToNavigationType the S-125 feature type
     * @param fieldName the name of the field to be accessed
     * @return the value of the field if that exists, otherwise null
     */
    protected static <T> T s125FeatureTypeField(S125AidsToNavigationType s125AidsToNavigationType, String fieldName, Class<T> clazz) {
        // Use reflections to access the field from the S-125 feature
        final PropertyDescriptor pd;
        Object value = null;
        try {
            pd = new PropertyDescriptor(fieldName, s125AidsToNavigationType.getClass());
            ReflectUtils.getPropertyMethods(new PropertyDescriptor[]{pd}, true, false);
            value = pd.getReadMethod().invoke(s125AidsToNavigationType);
        } catch (IntrospectionException | InvocationTargetException | IllegalAccessException ex) {
            log.error(ex.getMessage());
        }
        // Check if we can cast the result and return
        return Optional.ofNullable(value)
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .orElse(null);
    }

}
