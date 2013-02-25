/**
 * Copyright (C) 2013 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.platform.core.test.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An embedded version of the LMF. Provides support to startup and shutdown the CDI container and the LMF for test cases.
 * After the embedded LMF has been used, it should always be shutdown before being reused.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class EmbeddedMarmotta extends AbstractMarmotta {

    private static Logger log = LoggerFactory.getLogger(EmbeddedMarmotta.class);

    public EmbeddedMarmotta() {
        super();

        // initiate the first startup phase without a servlet context and with the override definition of the parent
        startupService.startupConfiguration(home.getAbsolutePath(), override,null);

        // initiate the second startup phase and pretend we are running at localhost
        startupService.startupHost("http://localhost/","http://localhost/");

        log.info("EmbeddedLMF created");
    }

}
