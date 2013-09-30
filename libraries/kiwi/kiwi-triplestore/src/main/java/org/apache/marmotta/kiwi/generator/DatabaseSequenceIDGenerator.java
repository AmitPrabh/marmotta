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

package org.apache.marmotta.kiwi.generator;

import org.apache.marmotta.kiwi.persistence.KiWiConnection;
import org.apache.marmotta.kiwi.persistence.KiWiPersistence;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Generate IDs using database sequences
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class DatabaseSequenceIDGenerator implements IDGenerator {

    public DatabaseSequenceIDGenerator() {
    }


    /**
     * Initialise the generator for the given persistence and module
     */
    @Override
    public void init(KiWiPersistence persistence, String scriptName) {
    }

    /**
     * Commit the current state of memory sequences to the database using the connection passed as second argument.
     *
     * @param persistence
     * @param con
     * @throws java.sql.SQLException
     */
    @Override
    public void commit(KiWiPersistence persistence, Connection con) throws SQLException {
    }

    /**
     * Shut down this id generator, performing any cleanups that might be necessary.
     *
     * @param persistence
     */
    @Override
    public void shutdown(KiWiPersistence persistence) {

    }

    /**
     * Return the next unique id for the type with the given name using the generator's id generation strategy.
     *
     * @param name
     * @return
     */
    @Override
    public long getId(String name, KiWiConnection connection) throws SQLException {
        return connection.getDatabaseSequence(name);
    }
}
