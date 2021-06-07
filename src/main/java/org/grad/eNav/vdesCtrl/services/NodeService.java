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

package org.grad.eNav.vdesCtrl.services;

import lombok.extern.slf4j.Slf4j;
import org.grad.eNav.vdesCtrl.models.domain.Node;
import org.grad.eNav.vdesCtrl.repos.NodeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

/**
 * Service Implementation for managing Station Nodes.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Service
@Slf4j
@Transactional
public class NodeService {

    @Autowired
    NodeRepo nodeRepo;

    /**
     * Save a node.
     *
     * @param node the entity to save
     * @return the persisted entity
     */
    public Node save(Node node) {
        log.debug("Request to save Station : {}", node);
        return this.nodeRepo.save(node);
    }

    /**
     * Get all the nodes.
     *
     * @return the list of nodes
     */
    @Transactional(readOnly = true)
    public List<Node> findAll() {
        log.debug("Request to get all Nodes");
        return this.nodeRepo.findAll();
    }

    /**
     * Get all the nodes in a pageable search.
     *
     * @param pageable the pagination information
     * @return the list of nodes
     */
    @Transactional(readOnly = true)
    public Page<Node> findAll(Pageable pageable) {
        log.debug("Request to get all Nodes in a pageable search");
        Page<Node> result = this.nodeRepo.findAll(pageable);
        return result;
    }

    /**
     * Get one node by id.
     *
     * @param id the id of the node
     * @return the node
     */
    @Transactional(readOnly = true)
    public Node findOne(BigInteger id) {
        log.debug("Request to get Node : {}", id);
        Node node = this.nodeRepo.findOneWithEagerRelationships(id);
        return node;
    }

    /**
     * Delete the node by id.
     *
     * @param id the id of the node
     */
    public void delete(BigInteger id) {
        log.debug("Request to delete Node : {}", id);
        this.nodeRepo.deleteById(id);
    }
}
