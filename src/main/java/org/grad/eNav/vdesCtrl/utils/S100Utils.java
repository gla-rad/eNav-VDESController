package org.grad.eNav.vdesCtrl.utils;

import _int.iho.s125.gml._0.DataSet;

import javax.xml.bind.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

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
     * Using the S125 utilities we can marshall back an S125 DatasetType
     * object it's XML view.
     *
     * @param datasetType the Service Instance object
     * @return the marshalled S125 message XML representation
     */
    public static String marshalS125(DataSet datasetType) throws JAXBException {
        // Create the JAXB objects
        JAXBContext jaxbContext = JAXBContext.newInstance(DataSet.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        // Transform the G1128 object to an output stream
        ByteArrayOutputStream xmlStream = new ByteArrayOutputStream();
        jaxbMarshaller.marshal(datasetType, xmlStream);

        // Return the XML string
        return xmlStream.toString();
    }

    /**
     * The S125Node object contains the S125 XML content of the message. We
     * can easily translate that into an S125 DatasetType object so that it
     * can be accessed more efficiently.
     *
     * @param s125 the S125 message content
     * @return The unmarshalled S125 DatasetType object
     * @throws JAXBException
     */
    public static DataSet unmarshallS125(String s125) throws JAXBException {
        // Create the JAXB objects
        JAXBContext jaxbContext = JAXBContext.newInstance(DataSet.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

        // Transform the S125 context into an input stream
        ByteArrayInputStream is = new ByteArrayInputStream(s125.getBytes());

        // And translate
        return (DataSet) JAXBIntrospector.getValue(jaxbUnmarshaller.unmarshal(is));
    }

}
