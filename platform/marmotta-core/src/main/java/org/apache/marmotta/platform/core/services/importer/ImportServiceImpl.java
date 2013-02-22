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
package org.apache.marmotta.platform.core.services.importer;

import org.apache.marmotta.platform.core.api.importer.ImportService;
import org.apache.marmotta.platform.core.api.importer.Importer;
import org.apache.marmotta.platform.core.exception.io.MarmottaImportException;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * User: Thomas Kurz
 * Date: 02.02.11
 * Time: 11:38
 */
@ApplicationScoped
public class ImportServiceImpl implements ImportService{

	@Inject
    private Logger log;

    private Map<String,Importer> importerMap;

    public ImportServiceImpl() {
    }

    @Inject
	public void initImporters(@Any Instance<Importer> importers) {
        if(importerMap == null) {
            importerMap = new HashMap<String,Importer>();
            for( Importer i : importers ) {
                for( String s : i.getAcceptTypes() ) {
                    importerMap.put(s,i);
                    log.info("registered importer for type {}: {}",s,i.getName());
                }
            }
        }
	}

	@Override
	public Set<String> getAcceptTypes() {
		return importerMap.keySet();
	}

	@Override
	public int importData(URL url, String format, Resource user, URI context) throws MarmottaImportException {
		return getImporterInstance(format).importData(url,format,user,context);
	}

	@Override
	public int importData(InputStream is, String format, Resource user, URI context) throws MarmottaImportException {
		return getImporterInstance(format).importData(is,format,user,context);
	}

	@Override
	public int importData(Reader reader, String format, Resource user, URI context) throws MarmottaImportException {
		return getImporterInstance(format).importData(reader,format,user,context);
	}

	private Importer getImporterInstance(String type) throws MarmottaImportException {
		if(!importerMap.containsKey(type)) throw new MarmottaImportException("no importer defined for type "+type);
		return importerMap.get(type);
	}
}