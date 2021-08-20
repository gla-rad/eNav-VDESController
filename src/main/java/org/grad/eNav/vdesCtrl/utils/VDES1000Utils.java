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

package org.grad.eNav.vdesCtrl.utils;

import _int.iho.s125.gml._0.DataSet;
import org.grad.eNav.vdesCtrl.models.VDESentences;
import org.grad.eNav.vdesCtrl.models.dtos.S125Node;

import javax.xml.bind.JAXBException;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * The VDES-1000 Utility Class.
 *
 * A static utility function class that allows easily manipulation of the VDES
 * 1000 station messages.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public class VDES1000Utils {

    /**
     * A first take to construct VDE sentences based on an S125 message.
     * It remains to be seems what kind of information is required through
     * the S125 and if we can actually send AtoN messages or something else.
     *
     * @param s125Node      The S125Node object to be translated
     * @param piSeqNo       The VDES-1000 PI Sequence Number
     * @param mmsi          The VDES-1000 MMSI number
     * @return The constructor VDE sentence
     */
    public static String s125ToVDE(S125Node s125Node, long piSeqNo, String mmsi)  throws JAXBException {
        DataSet dataset = S100Utils.unmarshallS125(s125Node.getContent());

        // Create a string builder to start ith
        StringBuilder vdeBuilder = new StringBuilder();

        // Build the prefix
        vdeBuilder.append("$AI");                       // Prefix
        vdeBuilder.append(VDESentences.EDO);            // VDE Sequence
        vdeBuilder.append(",");
        vdeBuilder.append(piSeqNo);                     // PI Sequence Number
        vdeBuilder.append(",");
        vdeBuilder.append(mmsi);                        // Source MMSI
        vdeBuilder.append(",");
        vdeBuilder.append("");                          // Dest MMSI - Null for broadcast
        vdeBuilder.append(",");
        vdeBuilder.append(dataset.getId());             // Data
        vdeBuilder.append(",");
        vdeBuilder.append("0");                         // Fill bits
        vdeBuilder.append("*");                         // Delimiter
        byte[] tmp = vdeBuilder.toString().getBytes();
        vdeBuilder.append(getCRC32Checksum(tmp));       // Checksum
        vdeBuilder.append(System.lineSeparator());      // Carriage return and line feed characters

        // Build the message
        return vdeBuilder.toString();
    }

    /***
     * Returns the CEC 32bit checksum for the provided bytes.
     *
     * @param bytes         The bytes to generate the checksum for
     * @return  The checksum of the provided bytes
     */
    public static long getCRC32Checksum(byte[] bytes) {
        Checksum crc32 = new CRC32();
        crc32.update(bytes, 0, bytes.length);
        return crc32.getValue();
    }

}
