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

package org.apache.marmotta.kiwi.hazelcast.serializer;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.io.DataIO;
import org.apache.marmotta.kiwi.model.rdf.KiWiNode;
import org.apache.marmotta.kiwi.model.rdf.KiWiResource;
import org.apache.marmotta.kiwi.model.rdf.KiWiTriple;
import org.apache.marmotta.kiwi.model.rdf.KiWiUriResource;

import java.io.IOException;
import java.util.Date;

/**
 * An externalizer for Infinispan allowing to more efficiently transport triples by only serializing the node
 * IDs instead of the whole nodes.
 */
public class TripleSerializer implements StreamSerializer<KiWiTriple> {


    public static final int MODE_DEFAULT = 1;
    public static final int MODE_PREFIX  = 2;



    @Override
    public int getTypeId() {
        return ExternalizerIds.TRIPLE;
    }

    @Override
    public void write(ObjectDataOutput output, KiWiTriple object) throws IOException {
        output.writeLong(object.getId());

        // in case subject and object are both uris we use a special prefix-compressed mode
        if(object.getSubject().isUriResource() && object.getObject().isUriResource()) {
            String sUri = object.getSubject().stringValue();
            String oUri = object.getObject().stringValue();

            String prefix = StringUtils.getCommonPrefix(sUri,oUri);

            output.writeByte(MODE_PREFIX);
            DataIO.writeString(output,prefix);

            output.writeLong(object.getSubject().getId());
            DataIO.writeString(output, sUri.substring(prefix.length()));
            output.writeLong(object.getSubject().getCreated().getTime());

            output.writeObject(object.getPredicate());

            output.writeLong(object.getObject().getId());
            DataIO.writeString(output, oUri.substring(prefix.length()));
            output.writeLong(object.getObject().getCreated().getTime());
        } else {
            output.writeByte(MODE_DEFAULT);

            output.writeObject(object.getSubject());
            output.writeObject(object.getPredicate());
            output.writeObject(object.getObject());
        }

        output.writeObject(object.getContext());
        output.writeObject(object.getCreator());
        output.writeBoolean(object.isDeleted());
        output.writeBoolean(object.isInferred());
        output.writeBoolean(object.isNewTriple());
        output.writeLong(object.getCreated().getTime());
        if(object.getDeletedAt() != null) {
            output.writeLong(object.getDeletedAt().getTime());
        } else {
            output.writeLong(0);
        }
    }

    @Override
    public KiWiTriple read(ObjectDataInput input) throws IOException {

        KiWiTriple result = new KiWiTriple();
        result.setId(input.readLong());

        int mode = input.readByte();
        if(mode == MODE_PREFIX) {
            String prefix = DataIO.readString(input);

            long sId = input.readLong();
            String sUri = prefix + DataIO.readString(input);
            long sTime = input.readLong();
            KiWiUriResource s = new KiWiUriResource(sUri);
            s.setId(sId);
            s.setCreated(new Date(sTime));
            result.setSubject(s);

            result.setPredicate((KiWiUriResource) input.readObject());

            long oId = input.readLong();
            String oUri = prefix + DataIO.readString(input);
            long oTime = input.readLong();
            KiWiUriResource o = new KiWiUriResource(oUri);
            o.setId(oId);
            o.setCreated(new Date(oTime));
            result.setObject(o);

        } else {
            result.setSubject((KiWiResource) input.readObject());
            result.setPredicate((KiWiUriResource) input.readObject());
            result.setObject((KiWiNode) input.readObject());
        }
        result.setContext((KiWiResource) input.readObject());
        result.setCreator((KiWiResource) input.readObject());
        result.setDeleted(input.readBoolean());
        result.setInferred(input.readBoolean());
        result.setNewTriple(input.readBoolean());

        result.setCreated(new Date(input.readLong()));

        long deletedAt = input.readLong();
        if(deletedAt > 0) {
            result.setDeletedAt(new Date(deletedAt));
        }


        return result;
    }

    @Override
    public void destroy() {

    }
}
