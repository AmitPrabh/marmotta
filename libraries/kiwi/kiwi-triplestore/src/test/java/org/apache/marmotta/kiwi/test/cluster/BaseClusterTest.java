/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.marmotta.kiwi.test.cluster;

import org.apache.marmotta.kiwi.caching.CacheManager;
import org.apache.marmotta.kiwi.caching.CacheManagerType;
import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.persistence.h2.H2Dialect;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public abstract class BaseClusterTest {

    private static Logger log = LoggerFactory.getLogger(BaseClusterTest.class);

    private static int datacenterIds = 1;

    private static Repository repositorySync1, repositorySync2, repositoryAsync1, repositoryAsync2;

    private static CacheManager cacheManagerSync1, cacheManagerSync2, cacheManagerAsync1, cacheManagerAsync2;


    @AfterClass
    public static void teardown() throws RepositoryException {
        repositorySync1.shutDown();
        repositorySync2.shutDown();
        repositoryAsync1.shutDown();
        repositoryAsync2.shutDown();
    }


    @Test
    public void testClusteredCacheSync() throws InterruptedException, RepositoryException {

        log.info("testing cache synchronization ...");

        URI u = repositorySync1.getValueFactory().createURI("http://localhost/test1");


        // give the cluster some time to performance asynchronous balancing
        Thread.sleep(100);


        log.debug("test if resource is in cache where it was created ...");
        URI u1 = (URI) cacheManagerSync1.getUriCache().get("http://localhost/test1");

        Assert.assertNotNull(u1);
        Assert.assertEquals(u,u1);

        log.debug("test if resource has been synced to other cache in cluster ...");
        URI u2 = (URI) cacheManagerSync2.getUriCache().get("http://localhost/test1");

        Assert.assertNotNull(u2);
        Assert.assertEquals(u,u2);
    }

    @Test
    public void testDisjointClusters() throws InterruptedException, RepositoryException {

        log.info("testing caches on different ports ...");

        URI u = repositoryAsync1.getValueFactory().createURI("http://localhost/test1");


        // give the cluster some time to performance asynchronous balancing
        Thread.sleep(100);

        log.debug("test if resource is in cache where it was created ...");
        URI u1 = (URI) cacheManagerAsync1.getUriCache().get("http://localhost/test1");

        Assert.assertNotNull(u1);
        Assert.assertEquals(u,u1);

        log.debug("test if resource has been synced to other cache in cluster ...");
        URI u2 = (URI) cacheManagerAsync2.getUriCache().get("http://localhost/test1");

        Assert.assertNull(u2);
    }


    protected static class ClusterTestSupport {

        CacheManagerType type;

        public ClusterTestSupport(CacheManagerType type) {
            this.type = type;
        }

        public void setup() {
            try {
                repositorySync1 = createConfiguration(61222);
                repositorySync2 = createConfiguration(61222);
                repositoryAsync1 = createConfiguration(61223);
                repositoryAsync2 = createConfiguration(61224);

                cacheManagerSync1 = getCacheManager(repositorySync1);
                cacheManagerSync2 = getCacheManager(repositorySync2);
                cacheManagerAsync1 = getCacheManager(repositoryAsync1);
                cacheManagerAsync2 = getCacheManager(repositoryAsync2);


            } catch (RepositoryException ex) {
                Assert.fail(ex.getMessage());
            }
        }


        private Repository createConfiguration(int port) throws RepositoryException {
            KiWiConfiguration config = new KiWiConfiguration(
                    "default-H2",
                    "jdbc:h2:mem:kiwitest;MVCC=true;DB_CLOSE_ON_EXIT=TRUE;DB_CLOSE_DELAY=-1",
                    "kiwi", "kiwi",
                    new H2Dialect());
            config.setDatacenterId(datacenterIds++);
            config.setClustered(true);
            config.setCacheManager(type);
            config.setClusterPort(port);

            KiWiStore store = new KiWiStore(config);

            Repository repository = new SailRepository(store);
            repository.initialize();

            return repository;
        }

        private static CacheManager getCacheManager(Repository repository) {
            return ((KiWiStore)((SailRepository)repository).getSail()).getPersistence().getCacheManager();
        }

    }
}
