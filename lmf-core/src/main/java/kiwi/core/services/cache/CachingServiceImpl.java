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
package kiwi.core.services.cache;

import kiwi.core.api.cache.CachingService;
import kiwi.core.events.SystemRestartingEvent;
import kiwi.core.qualifiers.cache.LMFCache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import java.net.URL;

/**
 * A service that offers a EHCache system cache implementation for use by other components
 * <p/>
 * User: sschaffe
 */
@ApplicationScoped
public class CachingServiceImpl implements CachingService {

    /**
     * Get the seam logger for issuing logging statements.
     */
    @Inject
    private Logger log;


    private CacheManager manager;

    public CachingServiceImpl() {
    }


    @PostConstruct
    public void initialize() {
        URL url = this.getClass().getClassLoader().getResource("ehcache-lmf.xml");

        log.info("LMF Caching Service starting up (configuration at {}) ...",url);
        manager = CacheManager.create(url);
    }

    /**
     * Return the cache with the name provided to the KiWiCache annotation of the injection.
     * 
     * 
     * Usage: <code><pre>
     * &#64;Inject &#64;KiWiCache("cache-name")
     * private Ehcache cache;
     * </pre></code>
     * 
     * 
     * @param injectionPoint
     * @return
     */
    @Override
    @Produces @LMFCache("")
    public Ehcache getCache(InjectionPoint injectionPoint) {
        String cacheName = injectionPoint.getAnnotated().getAnnotation(LMFCache.class).value();

        return getCacheByName(cacheName);
    }


    @Override
    public Ehcache getCacheByName(String cacheName) {
        if(!manager.cacheExists(cacheName)) {
            log.info("added new cache with name {}",cacheName);
            manager.addCache(cacheName);
        }

        Ehcache cache = manager.getEhcache(cacheName);
        cache.setStatisticsEnabled(true);

        return cache;
    }


    @Override
    public String[] getCacheNames() {
        return manager.getCacheNames();
    }

    @Override
    public CacheManager getCacheManager() {
        return manager;
    }


    /**
     * When system is restarted, flush all cache data
     * @param e
     */
    public void systemRestart(@Observes SystemRestartingEvent e) {
        log.warn("system restarted, flushing caches ...");
        manager.clearAll();
    }


    @Override
    public void clearAll() {
        manager.clearAll();
    }


    @PreDestroy
    public void destroy() {
        log.info("LMF Caching Service shutting down ...");
        /*
        for(String cacheName : manager.getCacheNames()) {
            log.info("Disposing cache {} ...",cacheName);
            Cache cache = manager.getCache(cacheName);
            cache.dispose();
        }
         */
        manager.shutdown();
        log.info("LMF Caching Service shut down successfully.");
    }
}
