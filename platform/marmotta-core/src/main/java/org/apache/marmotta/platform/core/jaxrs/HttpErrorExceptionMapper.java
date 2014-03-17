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

package org.apache.marmotta.platform.core.jaxrs;

import freemarker.template.TemplateException;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.templating.TemplatingService;
import org.apache.marmotta.platform.core.exception.HttpErrorException;
import org.slf4j.Logger;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Map HttpErrorExceptionMapper to a internal server error and return the default error object
 *
 * @author Sergio Fernández
 */
@Provider
@Dependent
public class HttpErrorExceptionMapper implements CDIExceptionMapper<HttpErrorException> {

    private final String TEMPLATE = "error.ftl";

    @Inject
    private Logger log;

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private TemplatingService templatingService;

    /**
     * Map an exception to a {@link javax.ws.rs.core.Response}. Returning
     * {@code null} results in a {@link javax.ws.rs.core.Response.Status#NO_CONTENT}
     * response. Throwing a runtime exception results in a
     * {@link javax.ws.rs.core.Response.Status#INTERNAL_SERVER_ERROR} response
     *
     * @param exception the exception to map to a response
     * @return a response mapped from the supplied exception
     */
    @Override
    public Response toResponse(HttpErrorException exception) {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("status", exception.getStatus());
        data.put("reason", exception.getReason());
        data.put("uri", exception.getUri());
        data.put("message", exception.getMessage());
        try {
            data.put("encoded_uri", URLEncoder.encode(exception.getUri(), "UTF-8"));
        } catch (UnsupportedEncodingException uee) {
            data.put("encoded_uri", exception.getUri());
        }

        Response.ResponseBuilder responseBuilder;
        try {
            responseBuilder = Response.status(exception.getStatus()).entity(templatingService.process(TEMPLATE, data));
        } catch (IOException | TemplateException e) {
            log.error("Error rendering template {}: {}", TEMPLATE, e.getMessage());
            responseBuilder = Response.status(exception.getStatus()).entity(e.getMessage());
        }
        Response response = responseBuilder.build();
        for (Map.Entry<String, String> entry : exception.getHeaders().entrySet()) {
            response.getMetadata().add(entry.getKey(), entry.getValue());
        }
        return response;
    }
}
