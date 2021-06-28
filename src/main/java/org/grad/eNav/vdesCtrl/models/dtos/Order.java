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

/**
 * The type Order.
 *
 * The Datatables Order Class definition.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public class Order {

    // Class Variables
    private Integer column;
    private Direction dir;

    /**
     * Instantiates a new Order.
     */
    public Order() {

    }

    /**
     * Gets column.
     *
     * @return the column
     */
    public Integer getColumn() {
        return column;
    }

    /**
     * Sets column.
     *
     * @param column the column
     */
    public void setColumn(Integer column) {
        this.column = column;
    }

    /**
     * Gets dir.
     *
     * @return the dir
     */
    public Direction getDir() {
        return dir;
    }

    /**
     * Sets dir.
     *
     * @param dir the dir
     */
    public void setDir(Direction dir) {
        this.dir = dir;
    }
}

