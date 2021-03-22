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
 * The S125_NavAidStructure Class.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@XmlRootElement
public class S125_NavAidStructure {

    // S125_NavAidStructure Variables
    private String atonType;
    private String deploymentType;

    /**
     * Sets new atonType.
     *
     * @param atonType New value of atonType.
     */
    @XmlElement
    public void setAtonType(String atonType) {
        this.atonType = atonType;
    }

    /**
     * Sets new deploymentType.
     *
     * @param deploymentType New value of deploymentType.
     */
    @XmlElement
    public void setDeploymentType(String deploymentType) {
        this.deploymentType = deploymentType;
    }

    /**
     * Gets atonType.
     *
     * @return Value of atonType.
     */
    public String getAtonType() {
        return atonType;
    }

    /**
     * Gets deploymentType.
     *
     * @return Value of deploymentType.
     */
    public String getDeploymentType() {
        return deploymentType;
    }

}
