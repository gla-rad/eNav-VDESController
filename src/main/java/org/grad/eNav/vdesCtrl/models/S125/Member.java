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

package org.grad.eNav.vdesCtrl.models.S125;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The S125 Member Class.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@XmlRootElement
public class Member {

    // Member Variables
    @XmlElement
    S125_NavAidStructure s125_navAidStructure;

    /**
     * Gets s125_navAidStructure.
     *
     * @return Value of s125_navAidStructure.
     */
    public S125_NavAidStructure getS125_navAidStructure() {
        return s125_navAidStructure;
    }

    /**
     * Sets new s125_navAidStructure.
     *
     * @param s125_navAidStructure New value of s125_navAidStructure.
     */
    public void setS125_navAidStructure(S125_NavAidStructure s125_navAidStructure) {
        this.s125_navAidStructure = s125_navAidStructure;
    }
}
