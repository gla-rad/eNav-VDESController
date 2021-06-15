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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GrAisMsg8ParamsTest {

    /**
     * Test that the GR-AIS Message 21 will be constructed by default with
     * empty parameters.
     */
    @Test
    public void testEmptyConstructor() {
        GrAisMsg8Params msgParams = new GrAisMsg8Params();
        assertNull(msgParams.getMmsi());
        assertEquals(0, msgParams.getMessage().length);
    }

}