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
package org.apache.marmotta.platform.core.startup;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.modules.ModuleService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.api.ui.LMFSystrayLink;
import org.apache.marmotta.platform.core.api.user.UserService;
import org.apache.marmotta.platform.core.events.SesameStartupEvent;
import org.apache.marmotta.platform.core.events.SystemStartupEvent;
import org.apache.marmotta.platform.core.model.module.ModuleConfiguration;
import org.apache.marmotta.platform.core.util.KiWiContext;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This service unifies the different steps in the LMF startup. It offers several methods
 * for triggering the different startup sequences and can be used e.g. by web applications or
 * embedded applications to initiate LMF startup. Note that the LMF Startup requires a running
 * CDI/Weld environment before being used.
 * <p/>
 * Author: Sebastian Schaffert
 */
@ApplicationScoped
public class LMFStartupService {

    private static final String DEFAULT_KIWI_VERSION = "undefined";

    private Logger log  = LoggerFactory.getLogger(LMFStartupService.class);

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private ModuleService moduleService;

    @Inject @Any
    private Event<SystemStartupEvent> startupEvent;

    @Inject @Any
    private Event<SesameStartupEvent> sesameEvent;

    private boolean configurationStarted = false;

    private boolean hostStarted = false;

    private ReentrantLock lock;

    @PostConstruct
    public void initialise() {
        lock = new ReentrantLock();
    }

    /**
     * Startup the LMF Configuration. This method ensures that the LMF home directory is created and the
     * ConfigurationService is properly initialised. It must be called first in the startup sequence.
     * The parameters lmfHome and configurationOverride can be used to override the default settings
     * of the LMF.
     *
     * @param lmfHome                 home directory of the LMF instance (may be null, in which case the default will be used)
     * @param configurationOverride   configuration options that should override the default values from default-config.properties (may be null)
     * @param context                 the servlet context the LMF is running in (may be null)
     */
    public void startupConfiguration(String lmfHome, Configuration configurationOverride, ServletContext context) {
        lock.lock();

        //to set config version number
        String versionNumber = DEFAULT_KIWI_VERSION;

        try {
            if(configurationStarted) {
                log.warn("LMF Startup: configuration already started; ignoring second request");
                return;
            }

            ModuleConfiguration coreConfiguration = moduleService.getModuleConfiguration(this.getClass());

            if(coreConfiguration.hasBuildInfo()) {
                log.info("LMF Core Version {} starting up ... ", coreConfiguration.getModuleVersion());
                log.info("Build Information:");
                log.info(" - Build User: {}", coreConfiguration.getBuildUser());
                log.info(" - Build Host: {}", coreConfiguration.getBuildHost());
                log.info(" - Build Time: {}", coreConfiguration.getBuildTimestamp());
                log.info(" - Build OS:   {}", coreConfiguration.getBuildOS());
                log.info(" - Revision:   {}:{}", coreConfiguration.getBuildRevisionNumber(), coreConfiguration.getBuildRevisionHash());
                versionNumber = coreConfiguration.getModuleVersion();
            } else {
                log.info("LMF Core (Development Version) starting up ... ");
            }

            String kiwiHome = lmfHome;

            if(kiwiHome != null) {

            } else {
                kiwiHome = System.getProperty("kiwi.home");

                if(kiwiHome != null) {
                    log.info("Configured working directory {} from system property kiwi.home",kiwiHome);
                } else {
                    kiwiHome = System.getenv("LMF_HOME");
                    if(kiwiHome != null) {
                        log.info("Configured working directory {} from environment variable LMF_HOME",kiwiHome);
                    } else {
                        kiwiHome = System.getenv("KIWI_HOME");
                        if(kiwiHome != null) {
                            log.info("Configured working directory {} from environment variable KIWI_HOME",kiwiHome);
                        } else if (context != null) {
                            kiwiHome = context.getInitParameter("kiwi.home");
                            if(kiwiHome != null) {
                                log.info("Configured working directory {} from servlet context parameter kiwi.home",kiwiHome);
                            }
                        } else {
                            log.error("could not determine LMF home directory, please set the environment variable LMF_HOME");
                        }
                    }
                }
            }

            if(kiwiHome != null) {
                configurationService.setLMFHome(kiwiHome);
            }

            if(context != null) {
                configurationService.setServletContext(context);
            }

            configurationService.initialize(kiwiHome,configurationOverride);


            configurationService.setConfiguration("kiwi.version",versionNumber);

            if(context != null) {
                configurationService.setConfiguration("kiwi.path",context.getContextPath());

                // register the systray links provided by the different components
                Map<String, String> demoLinks  = new HashMap<String, String>();
                Map<String, String> adminLinks = new HashMap<String, String>();

                for(LMFSystrayLink link : KiWiContext.getInstances(LMFSystrayLink.class)) {
                    if(link.getSection() == LMFSystrayLink.Section.DEMO) {
                        demoLinks.put(link.getLabel(), link.getLink());
                    } else if(link.getSection() == LMFSystrayLink.Section.ADMIN) {
                        adminLinks.put(link.getLabel(), link.getLink());
                    }
                }
                context.setAttribute("systray.admin", adminLinks);
                context.setAttribute("systray.demo", demoLinks);
            }

            configurationStarted = true;
        } finally {
            lock.unlock();
        }

    }


    /**
     * Start up the LMF server environment. This method ensures that the base URL for the host (used by the
     * web interface) and the context (used for creating local Linked Data URIs) is properly set and thus
     * the services depending on this configuration can start up. This method must be called in the second
     * phase of LMF startup, i.e. when the configuration service is already configured.
     * <p/>
     * The method expects a host URL and a context URL to be given. In case the context URL is not given,
     * it will be the same as the host URL.
     *
     * @param hostUrl     the URL of the host, used as based URL for building the LMF web interface
     * @param contextUrl  the base URL used to construct Linked Data resources
     */
    public void startupHost(String hostUrl, String contextUrl) {
        lock.lock();

        try {
            if(hostStarted) {
                log.warn("LMF Startup: host already started; ignoring subsequent startup requests");
                return;
            }
 
            // check whether this is a first-time initialization
            boolean isSetup = configurationService.getBooleanConfiguration("kiwi.setup.host");

            // carry out initializations that need the server URI to be set properly
            if(!isSetup) {
                log.info("SETUP: Setting up initial host and resource configuration ({}) ...", hostUrl);

                configurationService.setConfiguration("kiwi.context", contextUrl);
                configurationService.setConfiguration("kiwi.host", hostUrl);

                configurationService.setConfiguration("kiwi.setup.host", true);
            }

            // trigger startup of the sesame service once the hostname is ready (we need the correct URIs for
            // default, cache and inferred context)
            SesameService sesameService  = KiWiContext.getInstance(SesameService.class);
            sesameService.initialise();
            sesameEvent.fire(new SesameStartupEvent());

            // trigger startup of the user service once the sesame service is ready
            UserService   userService    = KiWiContext.getInstance(UserService.class);

            userService.createDefaultUsers();

            hostStarted = true;

            startupEvent.fire(new SystemStartupEvent());
        } finally {
            lock.unlock();
        }

    }

    public void shutdown() {
        log.info("LMF Core shutting down ...");
    }

}
