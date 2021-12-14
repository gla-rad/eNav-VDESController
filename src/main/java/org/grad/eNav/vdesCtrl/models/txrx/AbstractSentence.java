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
 *
 */

package org.grad.eNav.vdesCtrl.models.txrx;

/**
 * The Abstract Sentence Class.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public abstract class AbstractSentence {

    // The Sentence Formatter code
    protected final String formatterCode;
    protected final String talkerId;

    /**
     * Instantiates a new Sentence.
     *
     * @param formatterCode the formatter code
     */
    public AbstractSentence(String talkerId, String formatterCode) {
        this.talkerId = talkerId;
        this.formatterCode =  formatterCode;
    }

    /**
     * Gets talker id.
     *
     * @return the talker id
     */
    public String getTalkerId() {
        return talkerId;
    }

    /**
     * Gets formatter code.
     *
     * @return the formatter code
     */
    public String getFormatterCode() {
        return formatterCode;
    }
}
