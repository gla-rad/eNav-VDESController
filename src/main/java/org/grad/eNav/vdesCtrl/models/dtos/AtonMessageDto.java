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

package org.grad.eNav.vdesCtrl.models.dtos;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * The Blacklist UID DTO class.
 * <p>
 * This class is used to transfer information for the station's allocated
 * messages, which is based on the S125Node class, with additional blacklisting
 * information
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public class AtonMessageDto extends S125Node {

    //Class Variables
    private boolean blacklisted;

    /**
     * Empty Constructor
     */
    public AtonMessageDto() {

    }

    /**
     * The Fully Populated Constructor.
     * @param s125Node the S125 node to populate the DTO from
     * @param blacklisted whether the entry has been blacklisted
     */
    public AtonMessageDto(S125Node s125Node, boolean blacklisted) {
        super(s125Node.getIdCode(), s125Node.getGeometry(), s125Node.getContent());
        this.setDateEnd(s125Node.getDateEnd());
        this.setDateStart(s125Node.getDateStart());
        this.setPeriodEnd(s125Node.getPeriodEnd());
        this.setPeriodStart(s125Node.getPeriodStart());
        this.setSeasonalActionRequireds(s125Node.getSeasonalActionRequireds());
        this.setScaleMinimum(s125Node.getScaleMinimum());
        this.setPictorialRepresentation(s125Node.getPictorialRepresentation());
        this.setFeatureNames(s125Node.getFeatureNames());
        this.setInformations(s125Node.getInformations());
        this.setAtonType(s125Node.getAtonType());
        this.setMmsiCode(s125Node.getMmsiCode());
        this.setBlacklisted(blacklisted);
    }

    /**
     * Is blacklisted boolean.
     *
     * @return the boolean
     */
    public boolean isBlacklisted() {
        return blacklisted;
    }

    /**
     * Sets blacklisted.
     *
     * @param blacklisted the blacklisted
     */
    public void setBlacklisted(boolean blacklisted) {
        this.blacklisted = blacklisted;
    }

}
