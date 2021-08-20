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

package org.grad.eNav.vdesCtrl.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.util.Base64;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * This class provides utility methods for loading and using the RSA keys.
 * These are mainly used to sign AIS messages when transmitting them to
 * an open/unauthorised channel.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public class CryptoUtils {

    /**
     * Reads a file containing an Elliptic Cryptography type public key and
     * returns it.
     * @param resource the resource to read the key from
     * @return the loaded public key
     * @throws IOException when the key resource file is not found
     * @throws NoSuchAlgorithmException when the key factory algorithm is not found
     * @throws InvalidKeySpecException when the specified key is invalid
     */
    public static ECPublicKey readECPublicKey(String resource) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        InputStream in = new ClassPathResource(resource).getInputStream();
        String key = IOUtils.toString(in, StandardCharsets.UTF_8.name());

        String publicKeyPEM = key
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PUBLIC KEY-----", "");

        byte[] encoded = Base64.decodeBase64(publicKeyPEM);

        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        return (ECPublicKey) keyFactory.generatePublic(keySpec);
    }

    /**
     * Reads a file containing an Elliptic Cryptography type private key and
     * returns it.
     *
     * @param resource the resource to read the key from
     * @return the loaded private key
     * @throws IOException when the key resource file is not found
     * @throws NoSuchAlgorithmException when the key factory algorithm is not found
     * @throws InvalidKeySpecException when the specified key is invalid
     */
    public static ECPrivateKey readECPrivateKey(String resource) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        InputStream in = new ClassPathResource(resource).getInputStream();
        String key = IOUtils.toString(in, StandardCharsets.UTF_8.name());

        String privateKeyPEM = key
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PRIVATE KEY-----", "");

        byte[] encoded = Base64.decodeBase64(privateKeyPEM);

        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return (ECPrivateKey) keyFactory.generatePrivate(keySpec);
    }

}
