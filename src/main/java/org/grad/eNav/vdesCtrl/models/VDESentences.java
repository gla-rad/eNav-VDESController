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

package org.grad.eNav.vdesCtrl.models;

/**
 * The VDE Sentences Enum
 *
 * This enum represents all the available VDE Sentences of the VDES-1000 station
 * as per the manual. Visit:
 * https://www.cmlmicro.com/products/vhfdataexchangesystem/
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public enum VDESentences {
    EDM("EDM"),
    EDO("EDO"),
    EAK("EAK"),
    ERM("ERM"),
    ERO("ERO"),
    ETA("ETA"),
    ESI("ESI"),;

    // Enum Variables
    private String sentence;

    /**
     * The VDE Sentence Constructor.
     */
    VDESentences(String sentence) {
        this.sentence = sentence;
    }
}
