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

import _int.iho.s100gml._1.PointProperty;
import _int.iho.s125.gml._0.*;
import lombok.extern.slf4j.Slf4j;
import net.opengis.gml._3.AbstractFeatureMemberType;
import net.opengis.gml._3.PointType;
import net.opengis.gml._3.Pos;
import org.grad.eNav.vdesCtrl.models.dtos.S125Node;
import org.grad.eNav.vdesCtrl.utils.S100Utils;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * The GR-AIS Message 21 Parameters Class.
 * <p>
 * This class contains all the required parameters for generating an AIS binary
 * message through the GrAisUtils class function.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Slf4j
public class GrAisMsg21Params {

    // Class Variables
    private String uid;
    private Integer mmsi;
    private AtonType atonType;
    private String name;
    private Double latitude;
    private Double longitude;
    private Integer length;
    private Integer width;
    private Boolean raim;
    private Boolean vaton;
    private LocalDateTime timestamp;

    /**
     * Empty Constructor.
     */
    public GrAisMsg21Params() {
        this.uid = null;
        this.mmsi = null;
        this.atonType = AtonType.DEFAULT;
        this.name = "";
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.length = 0;
        this.width = 0;
        this.raim = Boolean.FALSE;
        this.vaton = Boolean.FALSE;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructors from an S125Node object.
     *
     * @param s125Node the S125Node object
     * @throws JAXBException when the S125Node XML content cannot be parsed
     */
    public GrAisMsg21Params(S125Node s125Node) throws JAXBException {
        // Default at first
        this();

        // Try to unmarshall the S125Node object
        DataSet dataset = S100Utils.unmarshallS125(s125Node.getContent());

        // Extract the S125 Member NavAid Information
        Optional.ofNullable(dataset)
                .map(DataSet::getMembersAndImembers)
                .filter(((Predicate<List<AbstractFeatureMemberType>>) List::isEmpty).negate())
                .map(l -> l.get(0))
                .filter(MemberType.class::isInstance)
                .map(MemberType.class::cast)
                .map(MemberType::getAbstractFeature)
                .map(JAXBElement::getValue)
                .filter(S125NavAidStructureType.class::isInstance)
                .map(S125NavAidStructureType.class::cast)
                .ifPresent(navAid -> {
                    this.uid = Optional.of(dataset)
                            .map(DataSet::getId)
                            .orElse(null);
                    this.atonType = AtonType.fromString(Optional.of(navAid).
                            map(S125NavAidStructureType::getAtonType)
                            .map(S125AtonType::value)
                            .orElse(null));
                    this.name = Optional.of(navAid)
                            .map(S125NavAidStructureType::getFeatureName)
                            .map(S125FeatureNameType::getName)
                            .orElse(null);
                    this.atonType = AtonType.fromString(Optional.of(navAid).
                            map(S125NavAidStructureType::getAtonType)
                            .map(S125AtonType::value)
                            .orElse(null));
                    this.latitude = Optional.of(navAid)
                            .map(S125NavAidStructureType::getGeometry)
                            .map(PointCurveSurface::getPointProperty)
                            .map(PointProperty::getPoint)
                            .map(PointType::getPos)
                            .map(Pos::getValues)
                            .map(list -> list.get(0))
                            .orElse(null);
                    this.longitude = Optional.of(navAid)
                            .map(S125NavAidStructureType::getGeometry)
                            .map(PointCurveSurface::getPointProperty)
                            .map(PointProperty::getPoint)
                            .map(PointType::getPos)
                            .map(Pos::getValues)
                            .map(list -> list.get(1))
                            .orElse(null);
                    this.mmsi = navAid.getMmsi();
                    this.length = navAid.isVatonFlag() ? 0 : Math.round(Optional.ofNullable(navAid.getLength()).orElse(0));
                    this.width = navAid.isVatonFlag() ? 0 : Math.round(Optional.ofNullable(navAid.getWidth()).orElse(0));
                    this.raim = navAid.isRaimFlag();
                    this.vaton = navAid.isVatonFlag();
                    this.timestamp = LocalDateTime.now();
                });
    }

    /**
     * Gets uid.
     *
     * @return the uid
     */
    public String getUid() {
        return uid;
    }

    /**
     * Sets uid.
     *
     * @param uid the uid
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * Gets mmsi.
     *
     * @return the mmsi
     */
    public Integer getMmsi() {
        return mmsi;
    }

    /**
     * Sets mmsi.
     *
     * @param mmsi the mmsi
     */
    public void setMmsi(Integer mmsi) {
        this.mmsi = mmsi;
    }

    /**
     * Gets aton type.
     *
     * @return the aton type
     */
    public AtonType getAtonType() {
        return atonType;
    }

    /**
     * Sets aton type.
     *
     * @param atonType the aton type
     */
    public void setAtonType(AtonType atonType) {
        this.atonType = atonType;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets latitude.
     *
     * @return the latitude
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * Sets latitude.
     *
     * @param latitude the latitude
     */
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    /**
     * Gets longitude.
     *
     * @return the longitude
     */
    public Double getLongitude() {
        return longitude;
    }

    /**
     * Sets longitude.
     *
     * @param longitude the longitude
     */
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    /**
     * Gets length.
     *
     * @return the length
     */
    public Integer getLength() {
        return length;
    }

    /**
     * Sets length.
     *
     * @param length the length
     */
    public void setLength(Integer length) {
        this.length = length;
    }

    /**
     * Gets width.
     *
     * @return the width
     */
    public Integer getWidth() {
        return width;
    }

    /**
     * Sets width.
     *
     * @param width the width
     */
    public void setWidth(Integer width) {
        this.width = width;
    }

    /**
     * Gets raim.
     *
     * @return the raim
     */
    public Boolean getRaim() {
        return raim;
    }

    /**
     * Sets raim.
     *
     * @param raim the raim
     */
    public void setRaim(Boolean raim) {
        this.raim = raim;
    }

    /**
     * Gets vaton.
     *
     * @return the vaton
     */
    public Boolean getVaton() {
        return vaton;
    }

    /**
     * Sets vaton.
     *
     * @param vaton the vaton
     */
    public void setVaton(Boolean vaton) {
        this.vaton = vaton;
    }

    /**
     * Gets timestamp.
     *
     * @return the timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Sets timestamp.
     *
     * @param timestamp the timestamp
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Returns the transmission timestamp, which is truncated to the seconds
     * level (so no nanos or millis). This is the correct format for gettting
     * the transmission time.
     *
     * @return the transmission timestamp
     */
    public LocalDateTime getTxTimestamp() {
        return this.timestamp.truncatedTo(ChronoUnit.SECONDS);
    }

    /**
     * Returns the transmission timestamp value as a UNIX timestamp.
     *
     * @param offset the zone offset seconds
     * @return the transmission UNIX timestamp
     */
    public long getUnixTxTimestamp(int offset) {
        return this.getTxTimestamp().toInstant(ZoneOffset.ofTotalSeconds(offset)).getEpochSecond();
    }

}
