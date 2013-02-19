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
package at.newmedialab.lmf.versioning.services;

import at.newmedialab.lmf.versioning.api.MementoService;
import at.newmedialab.lmf.versioning.exception.MementoException;
import at.newmedialab.lmf.versioning.model.MementoVersionSet;
import org.apache.marmotta.kiwi.versioning.model.Version;
import org.openrdf.model.Resource;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.sail.SailException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Date;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
@ApplicationScoped
public class MementoServiceImpl implements MementoService {

    @Inject
    VersioningSailProvider versioningService;

    /**
     * returns the version for a resource that was current on the given date
     * @param resource a uri resource
     * @param date a date
     * @return the version with respect to the date
     * @throws MementoException
     */
    @Override
    public Version getVersion(Resource resource, Date date) throws MementoException {
        try {
            return versioningService.getLatestVersion(resource,date);
        } catch (SailException e) {
            throw new MementoException("version for "+date+" cannot be returned");
        }
    }

    /**
     * returns a memento version set that includes first, last, current, prev and next version with respect
     * to a given date and resource
     * @param resource a requested resource
     * @param date a requested date
     * @return a memento version set
     * @throws MementoException
     * @see MementoVersionSet
     */
    @Override
    public MementoVersionSet getVersionSet(Resource resource, Date date) throws MementoException {
        try {
            MementoVersionSet versionset = new MementoVersionSet(resource);

            //get current version
            versionset.setCurrent(versioningService.getLatestVersion(resource,date));

            //loop to all versions to fill the versionset
            RepositoryResult<Version> versions = versioningService.listVersions(resource);

            while(versions.hasNext()) {

                Version v = versions.next();

                //set first as current if there is no current version yet
                if(versionset.getCurrent() == null) versionset.setCurrent(v);

                //set first version
                if(versionset.getFirst() == null) versionset.setFirst(v);

                //set last version
                versionset.setLast(v);

                //set previous as long as id is smaller than the current one
                if(v.getId() < versionset.getCurrent().getId()) {
                    versionset.setPrevious(v);
                }

                //set next if it is not set yet and the id is greater than the current one
                if(v.getId() > versionset.getCurrent().getId() && versionset.getNext() == null) {
                    versionset.setNext(v);
                }
            }

            return versionset;
        } catch (SailException e) {
            throw new MementoException("cannot list versions");
        } catch (RepositoryException e) {
            throw new MementoException("cannot produce version result set");
        }
    }

}
