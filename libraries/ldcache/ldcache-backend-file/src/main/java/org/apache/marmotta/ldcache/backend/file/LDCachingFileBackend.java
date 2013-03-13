/**
 *
 */
package org.apache.marmotta.ldcache.backend.file;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.ConvertingIteration;
import info.aduna.iteration.FilterIteration;
import info.aduna.iteration.IteratorIteration;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.marmotta.ldcache.api.LDCachingBackend;
import org.apache.marmotta.ldcache.api.LDCachingConnection;
import org.apache.marmotta.ldcache.backend.file.repository.LDCachingFileRepositoryConnection;
import org.apache.marmotta.ldcache.backend.file.util.FileBackendUtils;
import org.apache.marmotta.ldcache.model.CacheEntry;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.nativerdf.NativeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jakob
 *
 */
public class LDCachingFileBackend implements LDCachingBackend {

    private static Logger log = LoggerFactory.getLogger(LDCachingFileBackend.class);

    private final File storageDir;

    private Repository cacheRepository;

    public LDCachingFileBackend(File storageDir) throws RepositoryException {
        if (storageDir == null) throw new NullPointerException();
        this.storageDir = storageDir;

    }

    /* (non-Javadoc)
     * @see org.apache.marmotta.ldcache.api.LDCachingBackend#getCacheConnection(java.lang.String)
     */
    @Override
    public LDCachingConnection getCacheConnection(String resource) throws RepositoryException {

        return new LDCachingFileRepositoryConnection(cacheRepository, cacheRepository.getConnection(), storageDir);
    }

    /* (non-Javadoc)
     * @see org.apache.marmotta.ldcache.api.LDCachingBackend#listExpiredEntries()
     */
    @Override
    public CloseableIteration<CacheEntry, RepositoryException> listExpiredEntries()
            throws RepositoryException {
        final Date now = new Date();
        return new FilterIteration<CacheEntry, RepositoryException>(listCacheEntries()) {

            @Override
            protected boolean accept(CacheEntry object)
                    throws RepositoryException {
                return object.getExpiryDate().after(now);
            }
        };
    }

    /* (non-Javadoc)
     * @see org.apache.marmotta.ldcache.api.LDCachingBackend#listCacheEntries()
     */
    @Override
    public CloseableIteration<CacheEntry, RepositoryException> listCacheEntries()
            throws RepositoryException {

        final IteratorIteration<File, RepositoryException> ii = new IteratorIteration<File, RepositoryException>(FileBackendUtils.listMetaFiles(storageDir).iterator());
        return new ConvertingIteration<File, CacheEntry, RepositoryException>(ii) {

            @Override
            protected CacheEntry convert(File sourceObject)
                    throws RepositoryException {
                try {
                    return FileBackendUtils.readCacheEntry(sourceObject, cacheRepository.getValueFactory());
                } catch (IOException e) {
                    log.warn("Could not read caching properties from '{}'", sourceObject.getPath());
                    throw new RepositoryException(e);
                }
            }

        };
    }

    /* (non-Javadoc)
     * @see org.apache.marmotta.ldcache.api.LDCachingBackend#initialize()
     */
    @Override
    public void initialize() {
        if (!storageDir.exists() && !storageDir.mkdirs()){
            log.error("Could not create storage directory: " + storageDir.getPath());
        } else if (!storageDir.isDirectory()) {
            log.error(storageDir.getPath() + " is not a directory");
        }

        File tripleDir = new File(storageDir,"triples");

        try {
            cacheRepository = new SailRepository(new NativeStore(tripleDir, "spoc"));
            cacheRepository.initialize();
        } catch (RepositoryException ex) {
            log.error("could not initialize cache directory",ex);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.marmotta.ldcache.api.LDCachingBackend#shutdown()
     */
    @Override
    public void shutdown() {
        try {
            cacheRepository.shutDown();
        } catch (RepositoryException e) {
            log.error("error while shutting down cache repository", e);
        }

    }

}
