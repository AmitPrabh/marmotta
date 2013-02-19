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
package at.newmedialab.ldpath.api.tests;

import at.newmedialab.ldpath.api.backend.RDFBackend;
import at.newmedialab.ldpath.api.functions.NodeFunction;

import java.util.Collection;

/**
 * Node tests take a node as argument and return a boolean if the node matches the test.
 * <p/>
 * Author: Sebastian Schaffert <sebastian.schaffert@salzburgresearch.at>
 */
public abstract class NodeTest<Node> implements NodeFunction<Boolean, Node> {

    @Override
    public final Boolean apply(RDFBackend<Node> backend, Node context, Collection<Node>... args)
            throws IllegalArgumentException {

        if (args.length != 1 || args[0].isEmpty()) { throw new IllegalArgumentException("a test only takes one parameter"); }
        if (args[0].size() != 1) { throw new IllegalArgumentException("a test can only be applied to a single node"); }

        Node node = args[0].iterator().next();

        return accept(backend, context, node);
    }

    public abstract boolean accept(RDFBackend<Node> backend, Node context, Node candidate) throws IllegalArgumentException;

}
