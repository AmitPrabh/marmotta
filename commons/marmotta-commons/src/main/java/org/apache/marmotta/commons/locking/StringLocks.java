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

package org.apache.marmotta.commons.locking;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.Monitor;

import java.util.HashSet;

/**
 * An implementation of dynamic name-based locks that allows more fine-grained locking methods based on a string name.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class StringLocks {

    private LoadingCache<String,Monitor> stringLocks;

    // keeps strong references for locks that are in use
    private HashSet<Monitor> activeLocks;

    public StringLocks() {
        stringLocks = CacheBuilder.newBuilder().weakValues().build(new LockCacheLoader());
        activeLocks = new HashSet<>();
    }


    public void lock(String name) {
        Monitor lock = stringLocks.getUnchecked(name);
        lock.enter();
        activeLocks.add(lock);
    }

    public void unlock(String name) {
        Monitor lock = stringLocks.getUnchecked(name);
        lock.leave();
        if(lock.getOccupiedDepth() == 0) {
            activeLocks.remove(lock);
        }
    }

    public boolean tryLock(String name) {
        Monitor lock = stringLocks.getUnchecked(name);
        boolean result = lock.tryEnter();
        if(result) {
            activeLocks.add(lock);
        }
        return result;
    }

    /**
     * A simple Guava cache loader implementation for generating object-based locks
     */
    private static class LockCacheLoader extends CacheLoader<Object,Monitor> {
        @Override
        public Monitor load(Object key) throws Exception {
            return new Monitor();
        }
    }

}
