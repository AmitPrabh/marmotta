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
package org.apache.marmotta.platform.ldf.services;

import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.sesame.repository.ResultUtils;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.ldf.api.LdfService;
import org.apache.marmotta.platform.ldf.sesame.LdfRDFHandler;
import org.openrdf.model.*;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.OutputStream;
import java.net.URISyntaxException;

/**
 * Linked Media Fragments service implementation
 *
 * @author Sergio Fernández
 */
public class LdfServiceImpl implements LdfService {

    private static final Logger log = LoggerFactory.getLogger(LdfServiceImpl.class);

    @Inject
    private SesameService sesameService;

    @Override
    public void writeFragment(String subjectStr, String predicateStr, String objectStr, int page, RDFFormat format, OutputStream out) throws RepositoryException, RDFHandlerException {
        writeFragment(subjectStr, predicateStr, objectStr, null, page, format, out);
    }

    @Override
    public void writeFragment(URI subject, URI predicate, Value object, int page, RDFFormat format, OutputStream out) throws RepositoryException, RDFHandlerException {
        writeFragment(subject, predicate, object, null, page, format, out);
    }

    @Override
    public void writeFragment(String subjectStr, String predicateStr, String objectStr, String contextStr, int page, RDFFormat format, OutputStream out) throws RepositoryException, RDFHandlerException {
        final ValueFactoryImpl vf = new ValueFactoryImpl();

        URI subject = null;
        if (StringUtils.isNotBlank(subjectStr)) {
            try {
                new java.net.URI(subjectStr);
                subject = vf.createURI(subjectStr);
            } catch (URISyntaxException e) {
                log.error("invalid subject '{}': {}", subjectStr, e.getMessage());
            }
        }

        URI predicate = null;
        if (StringUtils.isNotBlank(predicateStr)) {
            try {
                new java.net.URI(predicateStr);
                predicate = vf.createURI(predicateStr);
            } catch (URISyntaxException e) {
                log.error("invalid predicate '{}': {}", predicateStr, e.getMessage());
            }
        }

        Value object = null;
        if (StringUtils.isNotBlank(objectStr)) {
            try {
                new java.net.URI(objectStr);
                object = vf.createURI(objectStr);
            } catch (URISyntaxException e) {
                object = vf.createLiteral(objectStr);
            }
        }

        URI context = null;
        if (StringUtils.isNotBlank(contextStr)) {
            try {
                new java.net.URI(contextStr);
                context = vf.createURI(contextStr);
            } catch (URISyntaxException e) {
                log.error("invalid context '{}': {}", contextStr, e.getMessage());
            }
        }

        writeFragment(subject, predicate, object, context, page, format, out);
    }

    @Override
    public void writeFragment(URI subject, URI predicate, Value object, Resource context, int page, RDFFormat format, OutputStream out) throws RepositoryException, RDFHandlerException {
        final RepositoryConnection conn = sesameService.getConnection();
        try {
            conn.begin();
            RepositoryResult<Statement> statements = conn.getStatements(subject, predicate, object, true, context);
            RDFHandler handler = new LdfRDFHandler(Rio.createWriter(format, out), context, page);
            Rio.write(ResultUtils.iterable(statements), handler);
        } finally {
            if (conn != null && conn.isOpen()) {
                conn.close();
            }
        }
    }

}
