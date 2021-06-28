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

import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The type Paging Request.
 *
 * The Datatables Paging Request Class definition.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public class DtPagingRequest {

    // Class Variables
    private int start;
    private int length;
    private int draw;
    private List<Order> order;
    private List<Column> columns;
    private Search search;

    /**
     * Instantiates a new Paging request.
     */
    public DtPagingRequest() {

    }

    /**
     * Gets start.
     *
     * @return the start
     */
    public int getStart() {
        return start;
    }

    /**
     * Sets start.
     *
     * @param start the start
     */
    public void setStart(int start) {
        this.start = start;
    }

    /**
     * Gets length.
     *
     * @return the length
     */
    public int getLength() {
        return length;
    }

    /**
     * Sets length.
     *
     * @param length the length
     */
    public void setLength(int length) {
        this.length = length;
    }

    /**
     * Gets draw.
     *
     * @return the draw
     */
    public int getDraw() {
        return draw;
    }

    /**
     * Sets draw.
     *
     * @param draw the draw
     */
    public void setDraw(int draw) {
        this.draw = draw;
    }

    /**
     * Gets order.
     *
     * @return the order
     */
    public List<Order> getOrder() {
        return order;
    }

    /**
     * Sets order.
     *
     * @param order the order
     */
    public void setOrder(List<Order> order) {
        this.order = order;
    }

    /**
     * Gets columns.
     *
     * @return the columns
     */
    public List<Column> getColumns() {
        return columns;
    }

    /**
     * Sets columns.
     *
     * @param columns the columns
     */
    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    /**
     * Gets search.
     *
     * @return the search
     */
    public Search getSearch() {
        return search;
    }

    /**
     * Sets search.
     *
     * @param search the search
     */
    public void setSearch(Search search) {
        this.search = search;
    }

    /**
     * Constructs a Springboot Data Sort object based on the information of the
     * datatables page request.
     *
     * @return the Springboot JPA page request
     */
    public org.springframework.data.domain.Sort getSpringbootSort() {
        // Create the Springboot sorting and direction
        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.unsorted();
        List<Column> columns = this.getColumns();
        for(Order order : this.getOrder()) {
            String columnName = columns.get(order.getColumn()).getData();
            sort = sort.and(order.getDir() == Direction.asc ?
                    org.springframework.data.domain.Sort.by(columnName).ascending() :
                    org.springframework.data.domain.Sort.by(columnName).descending());
        }
        return sort;
    }

    /**
     * Constructs a Springboot Data Sort object based on the information of the
     * datatables page request.
     *
     * @return the Springboot JPA page request
     */
    public org.apache.lucene.search.Sort getLucenceSort() {
        // Create the Lucene sorting and direction
        List<org.apache.lucene.search.SortField> sortFields = this.getOrder().stream()
                .map(order -> new org.apache.lucene.search.SortField(
                        columns.get(order.getColumn()).getData() + "_sort",
                        SortField.Type.STRING,
                        order.getDir() == Direction.asc))
                .collect(Collectors.toList());
        return new org.apache.lucene.search.Sort(sortFields.toArray(new org.apache.lucene.search.SortField[]{}));
    }

    /**
     * Constructs a Springboot JPA page request of the of the datatables page
     * request.
     *
     * @return the Springboot JPA page request
     */
    public PageRequest toPageRequest() {
        // Create the springboot pagination request
        return PageRequest.of(this.getStart()/this.getLength(), this.getLength(), this.getSpringbootSort());
    }

}
