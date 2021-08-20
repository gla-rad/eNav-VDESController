/*
 * Copyright (c) 2021 GLA Research and Development Directorate
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

/**
 * The enum Aton type.
 *
 * For reference, here is the Niord FTL Definition:
 * <#switch atonCategory>
 *     <#case "north_cardinal">
 *         <atonType>Cardinal Mark N</atonType>
 *         <deploymentType>Mobile</deploymentType>
 *         <#break>
 *     <#case "east_cardinal">
 *         <atonType>Cardinal Mark E</atonType>
 *         <deploymentType>Mobile</deploymentType>
 *         <#break>
 *     <#case "south_cardinal">
 *         <atonType>Cardinal Mark S</atonType>
 *         <deploymentType>Mobile</deploymentType>
 *         <#break>
 *     <#case "west_cardinal">
 *         <atonType>Cardinal Mark W</atonType>
 *         <deploymentType>Mobile</deploymentType>
 *         <#break>
 *     <#case "port_lateral">
 *         <atonType>Port hand Mark</atonType>
 *         <deploymentType>Mobile</deploymentType>
 *         <#break>
 *     <#case "starboard_lateral">
 *         <atonType>Starboard hand Mark</atonType>
 *         <deploymentType>Mobile</deploymentType>
 *         <#break>
 *     <#case "preferred_port">
 *         <atonType>Preferred Channel Port hand</atonType>
 *         <deploymentType>Mobile</deploymentType>
 *         <#break>
 *     <#case "preferred_starboard">
 *         <atonType>Preferred Channel Starboard hand</atonType>
 *         <deploymentType>Mobile</deploymentType>
 *         <#break>
 *     <#case "isolated_danger">
 *         <atonType>Isolated Danger</atonType>
 *         <deploymentType>Mobile</deploymentType>
 *         <#break>
 *     <#case "safe_water">
 *         <atonType>Safe Water</atonType>
 *         <deploymentType>Mobile</deploymentType>
 *         <#break>
 *     <#case "special_purpose">
 *         <atonType>Special Mark</atonType>
 *         <deploymentType>Mobile</deploymentType>
 *         <#break>
 *     <#case "wreck">
 *         <atonType>Emergency Wreck Marking Buoy</atonType>
 *         <deploymentType>Mobile</deploymentType>
 *         <#break>
 *     <#default>
 *         <atonType>Default</atonType>
 *         <deploymentType>Mobile</deploymentType>
 * </#switch>
 *
 * Hence we only need to support that for the time being I guess.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public enum AtonType {
    /**
     * Default aton type.
     */
    DEFAULT("Default", 0),
    WRECK("Emergency Wreck Marking Buoy", 4),
    NORTH_CARDINAL("Cardinal Mark N", 20),
    EAST_CARDINAL("Cardinal Mark E", 21),
    SOUTH_CARDINAL("Cardinal Mark Sy", 22),
    WEST_CARDINAL("Cardinal Mark W", 23),
    PORT_HAND_MARK("Port hand Mark", 24),
    STARBOARD_HAND_MARK("Starboard hand Mark", 25),
    PREFERRED_PORT("Preferred Channel Port hand", 26),
    PREFERRED_STARBOARD("Preferred Channel Starboard handy", 27),
    ISOLATED_DANGER("Isolated Danger", 28),
    SAFE_WATER("Safe Water", 29),
    SPECIAL_MARK("Special Mark", 30);

    // Enum Variables
    private String name;
    private int code;

    /**
     * The AtoN Type Enum Constructor.
     *
     * @param name the AtoN type name
     * @param code the AtoN type code
     */
    AtonType(String name, int code) {
        this.name = name;
        this.code = code;
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
     * Gets code.
     *
     * @return the code
     */
    public int getCode() {
        return code;
    }

    /**
     * Sets code.
     *
     * @param code the code
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * Returns the AtonType enum that matches the provided name.
     *
     * @param name the name to create the AtonType enum from
     * @return the AtonType enum that matches the provided name
     */
    public static AtonType fromString(String name) {
        for (AtonType b : AtonType.values()) {
            if (b.name.equalsIgnoreCase(name)) {
                return b;
            }
        }
        return null;
    }

}
