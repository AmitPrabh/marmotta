/**
 * Copyright (C) 2013 Salzburg Research.
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
package org.apache.marmotta.kiwi.reasoner.model.query;

import info.aduna.iteration.CloseableIteration;

import java.sql.SQLException;

/**
 * An iterator over query results. Allows lazy iterating over the database results of the query.
 *
 * @see kiwi.core.api.persistence.ResultIterator
 * @see kiwi.core.services.persistence.PersistenceServiceImpl#evaluateNamedCursorQuery(String, java.util.Map)
 *
 * <p/>
 * Author: Sebastian Schaffert
 */
public interface QueryResultIterator extends CloseableIteration<QueryResult, SQLException> {

    /**
     * Close the underlying result set and database connection.
     */
    public void close();
}
