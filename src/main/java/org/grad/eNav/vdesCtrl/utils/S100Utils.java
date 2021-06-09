package org.grad.eNav.vdesCtrl.utils;

import _int.iho.s125.gml._0.DatasetType;
import org.grad.eNav.vdesCtrl.models.dtos.S125Node;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;

/**
 * The S-100 Utility Class.
 *
 * A static utility function class that allows easily manipulation of the S-100
 * messages.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public class S100Utils {

    /**
     * The S125Node object contains the S125 XML content of the message. We
     * can easily translate that into an S125 DatasetType object so that it
     * can be accessed more efficiently.
     *
     * @param s125 the S125 message content
     * @return The unmarshalled S125 DatasetType object
     * @throws JAXBException
     */
    public static DatasetType unmarshallS125(String s125) throws JAXBException {
        // Create the JAXB objects
        JAXBContext jaxbContext = JAXBContext.newInstance(DatasetType.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

        // Transform the S125 context into an input stream
        ByteArrayInputStream is = new ByteArrayInputStream(s125.getBytes());

        // And translate
        return (DatasetType) JAXBIntrospector.getValue(jaxbUnmarshaller.unmarshal(is));
    }

}
