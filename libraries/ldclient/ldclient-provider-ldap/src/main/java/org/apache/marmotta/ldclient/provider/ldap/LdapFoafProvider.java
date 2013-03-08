/**
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
package org.apache.marmotta.ldclient.provider.ldap;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.apache.marmotta.commons.constants.Namespace;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.api.ldclient.LDClientService;
import org.apache.marmotta.ldclient.api.provider.DataProvider;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.provider.ldap.mapping.LiteralPredicateFactory;
import org.apache.marmotta.ldclient.provider.ldap.mapping.PredicateObjectFactory;
import org.apache.marmotta.ldclient.provider.ldap.mapping.UriPredicateFactory;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LdapFoafProvider maps LDAP accounts to foaf:Person instances
 * 
 * @author Daniel Trabe <daniel.trabe@salzburgresearch.at>
 * @author Jakob Frank <jakob.frank@salzburgresearch.at>
 * @author Sergio  Fernández <wikier@apache.org>
 * 
 */
public class LdapFoafProvider implements DataProvider {

    private static Logger log = LoggerFactory.getLogger(LdapFoafProvider.class);

    /**
     * Mapping for the Attributes in the LDAP-Directory
     */
    private static final Map<String, PredicateObjectFactory> MAPPING;
    static {
        Map<String, PredicateObjectFactory> m = new HashMap<String, PredicateObjectFactory>();
        m.put("distinguishedName", new LiteralPredicateFactory(Namespace.DCTERMS.identifier));
        m.put("name", new LiteralPredicateFactory(Namespace.FOAF.name));
        m.put("givenName", new LiteralPredicateFactory(Namespace.FOAF.firstName));
        m.put("sn", new LiteralPredicateFactory(Namespace.FOAF.surname));
        m.put("mail", new UriPredicateFactory(Namespace.FOAF.mbox) {
            @Override
            public Set<Value> createObjects(String value, ValueFactory valueFactory) {
                return super.createObjects("mailto:" + value, valueFactory);
            }
        });
        m.put("objectClass", new UriPredicateFactory(Namespace.RDF.type) {
            @Override
            public Set<Value> createObjects(String value, ValueFactory valueFactory) {
                if (value.equalsIgnoreCase("person"))
                    return super.createObjects(Namespace.FOAF.Person, valueFactory);
                else
                    return Collections.emptySet();
            }
        });

        MAPPING = Collections.unmodifiableMap(m);
    }

    @Override
    public String getName() {
        return "LdapFoafProvider";
    }

    @Override
    public String[] listMimeTypes() {
        return new String[] {};
    }

    /**
     * Opens a connection tho the LDAP-server. in most cases an Account + Password is needed to
     * connect
     * to the Server. This need to be configured at the endpoint
     * 
     * @param endpoint
     * @return
     * @throws LDAPException if connecting failed.
     * @throws DataRetrievalException
     */
    private LdapConnection openLdapConnection(Endpoint endpoint) throws DataRetrievalException {
    	//TODO
        String loginDN = endpoint.getProperty("loginDN");
        String loginPW = endpoint.getProperty("loginPW");
        java.net.URI u;
		try {
			u = new java.net.URI(endpoint.getEndpointUrl());
		} catch (URISyntaxException e) {
			throw new DataRetrievalException("Invalid enpooint URI", e);
		}
        LdapNetworkConnection connection = new LdapNetworkConnection(u.getHost(), u.getPort() > 0 ? u.getPort() : 389);
        try {
			connection.bind();
		} catch (Exception e) {
			throw new DataRetrievalException("LDAP connnection could not be bind", e);
		}
        if (connection.isAuthenticated()) { 
        	return connection;
        } else {
        	throw new DataRetrievalException("LDAP connnection could not be stablished");
        }

    }

    @Override
    public ClientResponse retrieveResource(String resource, LDClientService client, Endpoint endpoint) throws DataRetrievalException {
        String account = java.net.URI.create(resource.replaceAll(" ", "%20")).getPath().substring(1);
        String prefix = getEndpointSuffix(endpoint);
        try {
            final LdapConnection ldap = openLdapConnection(endpoint);

            Repository rep = new SailRepository(new MemoryStore());
            rep.initialize();

            RepositoryConnection conn = rep.getConnection();
            try {
                ValueFactory vf = conn.getValueFactory();
                String userDN = buildDN(prefix, account, ldap);

                Map<String, java.util.List<String>> accountData = getAccountData(userDN, ldap);

                final URI subject = vf.createURI(resource);
                for (String attr : MAPPING.keySet()) {
                    if (!accountData.containsKey(attr)) {
                        continue;
                    }

                    final PredicateObjectFactory factory = MAPPING.get(attr);
                    final URI predicate = factory.createPredicate(vf);

                    for (String val : accountData.get(attr)) {
                        for (Value object : factory.createObjects(val, vf)) {
                            conn.add(vf.createStatement(subject, predicate, object));
                        }
                    }

                }

                final ClientResponse resp = new ClientResponse(rep);
                resp.setExpires(new Date());
                return resp;

            } catch (Exception e) {
				throw new DataRetrievalException(e);
			} finally {
                conn.close();
            }
        } catch (RepositoryException e1) {
            log.warn("Could not create SailRep: {}", e1.getMessage());
            throw new DataRetrievalException(e1);
        }
    }

    /**
     * Returns the suffix (e.g. "dc=salzburgresearch,dc=at" of the Endpoint. This suffix is needed
     * to perform
     * miscellaneous LDAP-Operations
     * 
     * @param endpoint
     * @return
     * @throws DataRetrievalException when Config of LDAP-Suffix for endpoint is invalid
     */
    private String getEndpointSuffix(Endpoint endpoint) throws DataRetrievalException {
        try {
            java.net.URI u;
            u = new java.net.URI(endpoint.getEndpointUrl());
            return u.getPath() != null ? u.getPath().substring(1) : "";
        } catch (URISyntaxException e) {
            throw new DataRetrievalException("Invalid LDAP-Suffix config for endpoint '" + endpoint.getName() + "'!");
        }
    }

    /**
     * Builds an distinguished name which is needed for many LDAP operations based
     * on the URL we get from the resource.
     * (e.g "/SRFG/USERS/Daniel%20Trabe" ==>
     * "cn=Daniel  Trabe,ou=USERS,ou=SRFG,dc=salzburgresearch,dc=at")
     * 
     * @param suffix the LDAP-Suffix configured at the Endpoint
     * @param path part of the URL (e.g. /SRFG/USERS/DanielTrabe)
     * @param con an initialized LDAP-Context
     * @return the distinguished name of an Entry in the LDAP
     * @throws LDAPException if there is a problem with the LDAP connection
     * @throws DataRetrievalException when the Entry cannot be found
     */
    private String buildDN(String suffix, String path, LdapConnection con) throws DataRetrievalException {
        if (path.length() == 0) return suffix;

        String current = path.split("/")[0];

        List<String> data;
		try {
			data = getChildList(suffix, con);
		} catch (Exception e) {
			log.error("Error getting childs: {}", e.getMessage(), e);
			throw new DataRetrievalException(e);
		}
        String next = null;
        for (String dn : data) {
            if (dn.toLowerCase().endsWith((current + "," + suffix).toLowerCase())) {
                next = dn;
                break;
            }
        }

        if (next == null) throw new DataRetrievalException("The Object '" + current + "' cannot be found");

        return buildDN(next, path.replaceFirst("^[^/]*(/|$)", ""), con);
    }

    /**
     * Get child list
     * 
     * @param entryDN The distinguished name of an Entry in the LDAP
     * @param con An initialized LDAP-Context
     * @return All child's of an Entry
     * @throws IOException 
     * @throws CursorException 
     * @throws LdapException 
     * @throws LDAPSearchException
     */
    private List<String> getChildList(String entryDN, LdapConnection connection) throws CursorException, IOException, LdapException {
        List<String> childs = new ArrayList<String>();

        EntryCursor cursor = connection.search("ou=system", "(objectclass=*)", SearchScope.ONELEVEL);
        while (cursor.next()) {
            Entry entry = cursor.get();
            childs.add(entry.get("distinguishedName").getString());
        }

        //SearchResultDone done = cursor.getSearchResultDone();
        //ResultCodeEnum resultCode = done.getLdapResult().getResultCode();
        cursor.close();
        // ResultCodeEnum.SUCCESS == resultCode
        return childs;
    }

    /**
     * Get account data
     * 
     * @param accountDN The distinguished Name of the Account (e.g
     *            "cn=Daniel  Trabe,ou=USERS,ou=SRFG,dc=salzburgresearch,dc=at")
     * @param connection An initialized LDAP-Context
     * @return a Map of Attributes and Values of an Account
     * @throws DataRetrievalException
     * @throws IOException 
     * @throws CursorException 
     * @throws LdapException 
     * @throws LDAPException
     */
    private Map<String, List<String>> getAccountData(String accountDN, LdapConnection connection) throws DataRetrievalException, LdapException, CursorException, IOException {
        Map<String, List<String>> account = new HashMap<String, List<String>>();
        Dn dn = new Dn(accountDN);
        EntryCursor cursor = connection.search(dn, accountDN, SearchScope.ONELEVEL, (String[])null);
        if (cursor.next()) {
        	//FIXME: only the first entry?
            Entry entry = cursor.get();
            for (Attribute attr : entry.getAttributes()) {
                String id = attr.getId();
            	List<String> values;
                if (account.containsKey(id)) {
                	values = account.get(id);  
                } else {
                	values = new ArrayList<String>();
                }
                values.add(attr.get().getValue().toString());
                account.put(id, values);
            }
        }
        return account;
    }

}
