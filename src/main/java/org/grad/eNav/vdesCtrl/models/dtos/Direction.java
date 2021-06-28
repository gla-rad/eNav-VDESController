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

package org.grad.eNav.vdesCtrl.models.dtos;

import org.springframework.data.domain.Sort;

/**
 * The Direction Enum.
 * <p>
 * The Datatables Direction Enumeration definition.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public enum Direction {
    /**
     * Asc direction.
     */
    asc(Sort.Direction.ASC),
    /**
     * Desc direction.
     */
    desc(Sort.Direction.DESC);

    // Enum Variables
    Sort.Direction direction;

    Direction(Sort.Direction direction) {
        this.direction = direction;
    }

    /**
     * Gets direction.
     *
     * @return the direction
     */
    public Sort.Direction getDirection() {
        return direction;
    }

    /**
     * Sets direction.
     *
     * @param direction the direction
     */
    public void setDirection(Sort.Direction direction) {
        this.direction = direction;
    }

}
