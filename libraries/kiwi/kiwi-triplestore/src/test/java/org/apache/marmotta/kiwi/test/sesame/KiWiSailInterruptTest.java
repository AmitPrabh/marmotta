/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.kiwi.test.sesame;

import java.util.ArrayList;
import java.util.List;

import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.h2.H2Dialect;
import org.apache.marmotta.kiwi.persistence.mysql.MySQLDialect;
import org.apache.marmotta.kiwi.persistence.pgsql.PostgreSQLDialect;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.test.helper.DBConnectionChecker;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.SailInterruptTest;

/**
 * Run the Sesame {@link SailInterruptTest} suite.
 * @author Jakob Frank <jakob@apache.org>
 *
 */
@RunWith(Parameterized.class)
public class KiWiSailInterruptTest extends SailInterruptTest {

    /**
     * Return database configurations if the appropriate parameters have been set.
     *
     * @return an array (database name, url, user, password)
     */
    @Parameterized.Parameters(name="Database Test {index}: {0} at {1}")
    public static Iterable<Object[]> databases() {
        String[] databases = {"H2", "PostgreSQL", "MySQL"};

        List<Object[]> result = new ArrayList<Object[]>(databases.length);
        for(String database : databases) {
            if(System.getProperty(database.toLowerCase()+".url") != null) {
                result.add(new Object[] {
                        database,
                        System.getProperty(database.toLowerCase()+".url"),
                        System.getProperty(database.toLowerCase()+".user","lmf"),
                        System.getProperty(database.toLowerCase()+".pass","lmf")
                });
            }
        }
        return result;
    }

    private final String jdbcPass;
    private final String jdbcUrl;
    private final String jdbcUser;
    private final KiWiDialect dialect;
    
    public KiWiSailInterruptTest(String database, String jdbcUrl, String jdbcUser, String jdbcPass) {
        super(String.format("%s (%S)", KiWiSailInterruptTest.class.getSimpleName(), database));
        
        this.jdbcPass = jdbcPass;
        this.jdbcUrl = jdbcUrl;
        this.jdbcUser = jdbcUser;

        if("H2".equals(database)) {
            this.dialect = new H2Dialect();
        } else if("MySQL".equals(database)) {
            this.dialect = new MySQLDialect();
        } else if("PostgreSQL".equals(database)) {
            this.dialect = new PostgreSQLDialect();
        } else {
            Assert.fail("unknown database dialect: " + database);
            throw new AssertionError();
        }
        
        DBConnectionChecker.checkDatabaseAvailability(jdbcUrl, jdbcUser, jdbcPass, this.dialect);
    }
    
    
    
    @Override
    protected Sail createSail() throws SailException {
        KiWiStore store = new KiWiStore(new KiWiConfiguration("test",jdbcUrl,jdbcUser,jdbcPass,dialect, "http://localhost/context/default", "http://localhost/context/inferred"));
        return store;
    }
    
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }
    
    @Override
    @Test
    public void testQueryInterrupt() throws Exception {
        super.testQueryInterrupt();
    }
}
