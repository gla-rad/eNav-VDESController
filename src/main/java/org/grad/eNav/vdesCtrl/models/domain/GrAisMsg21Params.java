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

package org.grad.eNav.vdesCtrl.models.domain;

import _int.iho.s125.gml._0.DatasetType;
import _int.iho.s125.gml._0.MemberType;
import _int.iho.s125.gml._0.S125NavAidStructureType;
import lombok.extern.slf4j.Slf4j;
import org.grad.eNav.vdesCtrl.models.dtos.S125Node;
import org.grad.eNav.vdesCtrl.utils.S100Utils;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Predicates.not;

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
    private Integer mmsi;
    private AtonType atonType;
    private String name;
    private Double latitude;
    private Double longitude;
    private Integer length;
    private Integer width;
    private Boolean raim;
    private Boolean vaton;

    /**
     * Empty Constructor.
     */
    public GrAisMsg21Params() {
        this.mmsi = null;
        this.atonType = AtonType.DEFAULT;
        this.name = "";
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.length = 0;
        this.width = 0;
        this.raim = Boolean.FALSE;
        this.vaton = Boolean.FALSE;
    }

    /**
     * Constructors from an S125Node object.
     *
     * @param s125Node the S125Node object
     */
    public GrAisMsg21Params(S125Node s125Node) {
        // Default at first
        this();

        // Try to unmarshall the S125Node object
        DatasetType dataset = null;
        try {
            // Unmarshall the dataset content
            dataset = S100Utils.unmarshallS125(s125Node.getContent());
        } catch(JAXBException ex) {
            log.error(ex.getMessage());
            return;
        }

        // Extract the S125 Member NavAid Information
        Optional.ofNullable(dataset)
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
                    this.mmsi = navAid.getMmsi();
                    this.atonType = AtonType.fromString(navAid.getAtonType().value());
                    this.name = navAid.getFeatureName().getName();
                    this.latitude = navAid.getGeometry().getPointProperty().getPoint().getPos().getValue().get(0);
                    this.longitude = navAid.getGeometry().getPointProperty().getPoint().getPos().getValue().get(1);
                    this.length = navAid.isVatonFlag() ? 0 : Math.round(Optional.ofNullable(navAid.getLength()).orElse(0));
                    this.width = navAid.isVatonFlag() ? 0 : Math.round(Optional.ofNullable(navAid.getWidth()).orElse(0));
                    this.raim = navAid.isRaimFlag();
                    this.vaton = navAid.isVatonFlag();
                });
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
}
