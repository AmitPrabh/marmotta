/**
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
package org.apache.marmotta.commons.sesame.transactions.wrapper;

import org.apache.marmotta.commons.sesame.transactions.api.TransactionListener;
import org.apache.marmotta.commons.sesame.transactions.api.TransactionalSailConnection;
import org.openrdf.sail.helpers.NotifyingSailConnectionWrapper;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class TransactionalSailConnectionWrapper extends NotifyingSailConnectionWrapper implements TransactionalSailConnection {

    private TransactionalSailConnection parent;

    public TransactionalSailConnectionWrapper(TransactionalSailConnection parent) {
        super(parent);
        this.parent = parent;
    }

    /**
     * Add a transaction listener to the KiWiTransactionalStore. The listener will be notified whenever a connection
     * commits or rolls back. The listeners are collected in a list, i.e. a listener that is added first is also executed
     * first.
     *
     * @param listener the listener to add to the list
     */
    @Override
    public void addTransactionListener(TransactionListener listener) {
        parent.addTransactionListener(listener);
    }

    /**
     * Remove a transaction listener from the list.
     *
     * @param listener the listener to remove
     */
    @Override
    public void removeTransactionListener(TransactionListener listener) {
        parent.removeTransactionListener(listener);
    }
}
