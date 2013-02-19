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
package at.newmedialab.ldpath.model.transformers;

import at.newmedialab.ldpath.api.backend.RDFBackend;
import at.newmedialab.ldpath.api.transformers.NodeTransformer;

public class ByteTransformer<Node> implements NodeTransformer<Byte,Node> {

    @Override
    public Byte transform(RDFBackend<Node> backend, Node node) throws IllegalArgumentException {
        if(backend.isLiteral(node)) {
            return backend.decimalValue(node).byteValueExact();
        } else {
            throw new IllegalArgumentException("cannot transform node of type "+
                node.getClass().getCanonicalName()+" to byte");
        }
    }

}
