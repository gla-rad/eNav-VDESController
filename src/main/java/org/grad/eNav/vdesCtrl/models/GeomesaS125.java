/*
 * Copyright 2021 GLA UK Research and Development Directive
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.grad.eNav.vdesCtrl.models;

import org.geotools.data.Query;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;
import org.geotools.util.factory.Hints;
import org.locationtech.geomesa.utils.interop.SimpleFeatureTypes;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The implementation of the S-125 data entries transported through the Geomesa
 * data stores.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public class GeomesaS125 implements GeomesaData<S125Node>{

    // Class Variables
    private SimpleFeatureType sft = null;
    private List<SimpleFeature> features = null;
    private List<Query> queries = null;
    private List<Double> bounds = null;

    /**
     * Empty Constructor
     */
    public GeomesaS125() {

    }

    /**
     * Constructor with a specified bounds area.
     *
     * @param bounds    The bounds area edges list
     */
    public GeomesaS125(List<Double> bounds) {
        this.bounds = bounds;
    }

    /**
     * Sets new bounds.
     *
     * @param bounds New value of bounds.
     */
    public void setBounds(List<Double> bounds) {
        this.bounds = bounds;
    }

    /**
     * Gets bounds.
     *
     * @return Value of bounds.
     */
    public List<Double> getBounds() {
        return bounds;
    }


    /**
     * Defines the Geomesa Data Type - AtoN.
     *
     * @return The Geomesa Data Type
     */
    @Override
    public String getTypeName() {
        return "S125";
    }

    /**
     * Constructs the Geomesa Feature type. This is pretty similar to a database
     * schema definition. We need to specify the attributes (columns) and their
     * types. For AtoN nodes, we also have a generic list of tags. The easiest
     * way of hadling this is to encode it as a JSON array.
     *
     * @return      The AtoN node simple feature type
     */
    @Override
    public SimpleFeatureType getSimpleFeatureType() {
        if (sft == null) {
            // list the attributes that constitute the feature type
            // this is a reduced set of the attributes from GDELT 2.0
            StringBuilder attributes = new StringBuilder();

            attributes.append("atonUID:String,");
            attributes.append("*geom:Point:srid=4326,"); // the "*" denotes the default geometry (used for indexing)
            attributes.append("content:String");

            // create the simple-feature type - use the GeoMesa 'SimpleFeatureTypes' class for best compatibility
            // may also use geotools DataUtilities or SimpleFeatureTypeBuilder, but some features may not work
            sft = SimpleFeatureTypes.createType(getTypeName(), attributes.toString());

            // use the user-data (hints) to specify which date field to use for primary indexing
            // if not specified, the first date attribute (if any) will be used
            // could also use ':default=true' in the attribute specification string
            sft.getUserData().put(SimpleFeatureTypes.DEFAULT_DATE_KEY, "atonUID");
        }
        return sft;
    }

    /**
     * A helper function that constructs a list of simple features based on the
     * provided list of input objects i.e. in this case AtoN nodes. It must
     * follow the same construct as the definition provided in the simple
     * feature type.
     *
     * @param s125Nodes     The list of AtoNs to generate the simple features
     * @return The simple features based on the provided object list
     */
    @Override
    public List<SimpleFeature> getFeatureData(List<S125Node> s125Nodes) {
        if (features == null) {
            List<SimpleFeature> features = new ArrayList<>();

            // Use a geotools SimpleFeatureBuilder to create our features
            SimpleFeatureBuilder builder = new SimpleFeatureBuilder(getSimpleFeatureType());

            for(S125Node node: s125Nodes) {
                builder.set("atonUID", node.getAtonUID());
                builder.set("geom", "POINT (" + node.getBbox()[0] + " " + node.getBbox()[1] + ")");
                builder.set("content", node.getContent());

                // be sure to tell GeoTools explicitly that we want to use the ID we provided
                builder.featureUserData(Hints.USE_PROVIDED_FID, Boolean.TRUE);

                // build the feature - this also resets the feature builder for the next entry
                // use the AtoN UID as the feature ID
                features.add(builder.buildFeature(node.getAtonUID()));
            }
            this.features = Collections.unmodifiableList(features);
        }
        return features;
    }

    /**
     * A helper function that reverses the operation of the getFeatureData().
     * It will reconstruct the list of AtoNs form the provided list of simple
     * features based on the construct defined in the simple feature type.
     *
     * @param features  The list of simple features to be used
     * @return The list of objects reconstructed
     */
    public List<S125Node> retrieveData(List<SimpleFeature> features) {
        // A sanity check
        if(features == null) {
            return Collections.emptyList();
        }

        // Otherwise map all the provided features
        return features.stream()
                .map(feature ->
                        // Create the S125 Node message
                        new S125Node(
                                ((String)feature.getAttribute("atonUID")),
                                new Double[]{((Point)feature.getAttribute("geom")).getX(), ((Point)feature.getAttribute("geom")).getY()},
                                ((String)feature.getAttribute("content"))
                        )
                )
                .collect(Collectors.toList());
    }

    /**
     * Returns the list of queries through which data can be filtered. Currently
     * we want to show all data.
     *
     * @return The list of queries to filter the data for
     */
    @Override
    public List<Query> getFeatureQueries() {
        if (queries == null) {
            List<Query> queries = new ArrayList<>();
            // this data set is meant to show streaming updates over time, so just return all features
            queries.add(new Query(getTypeName(), Filter.INCLUDE));
            this.queries = Collections.unmodifiableList(queries);
        }
        return queries;
    }

    /**
     * A subsequent filter to further refine the feature search.
     *
     * In the current context of S125 this could be a generic polygon that
     * defines the area of a VDES station.
     *
     * @return The subsequent filter to further refine the search
     */
    @Override
    public Filter getSubsetFilter() {
        // For no or invalid filters, just reject everything
        if(Optional.ofNullable(bounds).map(List::size).orElse(0) <= 0) {
            return Filter.EXCLUDE;
        }

        // there are many different geometric predicates that might be used;
        // here, we use a polygon (POLYGON) predicate as an example. This is
        // useful for a general query area.
        try {
            String cqlGeometry = "BBOX(geom, "
                    + String.join(", ", this.bounds.stream().map(String::valueOf).collect(Collectors.toList()))
                    + ")";

            // We use geotools ECQL class to parse a CQL string into a Filter object
            return ECQL.toFilter(cqlGeometry);
        } catch (CQLException ex) {
            // Any errors... then exclude by default
            return Filter.EXCLUDE;
        }
    }

}
