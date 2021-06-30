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

package org.grad.eNav.vdesCtrl.components;

import org.hibernate.search.MassIndexer;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityManager;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HibernateSearchInitTest {

    /**
     * The Tested Component.
     */
    @InjectMocks
    @Spy
    HibernateSearchInit hibernateSearchInit;

    /**
     * The Entity Manager mock.
     */
    @Mock
    EntityManager entityManager;

    /**
     * The Full Text Entity Manager mock.
     */
    @Mock
    FullTextEntityManager fullTextEntityManager;

    /**
     * Common setup for all the tests.
     */
    @BeforeEach
    void setup() {
        doReturn(fullTextEntityManager).when(this.hibernateSearchInit).getFullTextEntityManager();
    }

    /**
     * Test that the hibernate search will initialise correctly on the
     * application events.
     */
    @Test
    void testOnApplicationEvent() throws InterruptedException {
        // Mock the indexing initialisation
        MassIndexer mockedIndexer = mock(MassIndexer.class);
        doNothing().when(mockedIndexer).startAndWait();
        doReturn(mockedIndexer).when(fullTextEntityManager).createIndexer();

        // Perform the component call
        this.hibernateSearchInit.onApplicationEvent(null);

        // Verify the indexing initialisation was performed
        verify(mockedIndexer, times(1)).startAndWait();
    }

    /**
     * Test that when the hibernate search will failed to initialise we can
     * still boot the service without an error.
     */
    @Test
    void testOnApplicationEventFailed() throws InterruptedException {
        // Mock the indexing initialisation
        MassIndexer mockedIndexer = mock(MassIndexer.class);
        doThrow(InterruptedException.class).when(mockedIndexer).startAndWait();
        doReturn(mockedIndexer).when(fullTextEntityManager).createIndexer();

        // Perform the component call
        this.hibernateSearchInit.onApplicationEvent(null);

        // Verify the indexing initialisation was performed
        verify(mockedIndexer, times(1)).startAndWait();
    }

}