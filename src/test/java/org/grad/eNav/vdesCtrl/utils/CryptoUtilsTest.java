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

package org.grad.eNav.vdesCtrl.utils;

import org.junit.Test;

import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;

import static org.junit.jupiter.api.Assertions.*;

public class CryptoUtilsTest {

    /**
     * Test that we can read correctly an Elliptic Cryptography public key.
     */
    @Test
    public void testReadPublicECKey() throws Exception {
        ECPublicKey publicKey = CryptoUtils.readECPublicKey("classpath:CorkHoleTest-Public.pem");
        assertNotNull(publicKey);
    }

    /**
     * Test that we can read correctly an Elliptic Cryptography private key.
     */
    @Test
    public void testReadPrivateECKey() throws Exception {
        ECPrivateKey privateKey = CryptoUtils.readECPrivateKey("classpath:CorkHoleTest-PrivateKeyPair.pem");
        assertNotNull(privateKey);
    }

}