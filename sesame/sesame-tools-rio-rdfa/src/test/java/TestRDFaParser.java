/*
 * Copyright (c) 2013 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeThat;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
@RunWith(Parameterized.class)
public class TestRDFaParser {

    private static Logger log = LoggerFactory.getLogger(TestRDFaParser.class);

    private String fileName;

    public TestRDFaParser(String fileName) {
        this.fileName = fileName;
    }

    // return the list of rdf-NNNN.jsonld files
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        int[] skip = new int[] {42,60,107,108,114,122,140,173,184,185,189,193,195,200,204,205,210};
        ArrayList<Object[]> list = new ArrayList<Object[]>();
        for(int i=1; i<=213; i++) {
            if(Arrays.binarySearch(skip, i) == -1 && TestRDFaParser.class.getResourceAsStream("testfiles/xhtml/"+String.format("%04d",i)+".xhtml") != null) {
                list.add(new Object[] {String.format("%04d",i)});
            }
        }
        return list;
    }

    @Test
    public void runTest() throws Exception {
        log.info("running test {} ...", fileName);

        InputStream rdfa = this.getClass().getResourceAsStream("testfiles/xhtml/"+fileName+".xhtml");
        InputStream sparql = this.getClass().getResourceAsStream("testfiles/sparql/"+fileName+".sparql");
        assumeThat("Could not load testfiles", asList(rdfa, sparql), everyItem(notNullValue(InputStream.class)));

        Repository repository = new SailRepository(new MemoryStore());
        repository.initialize();

        RepositoryConnection connection = repository.getConnection();
        connection.setNamespace("","http://www.w3.org/1999/xhtml");
        connection.setNamespace("rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        connection.setNamespace("rdfs","http://www.w3.org/2000/01/rdf-schema#");
        connection.setNamespace("owl","http://www.w3.org/2002/07/owl#");
        try {
            connection.add(rdfa,"http://localhost/rdfa/"+fileName+".xhtml", RDFFormat.RDFA);
            connection.commit();
        } catch(Exception ex) {
            fail("parsing "+fileName+" failed!");
        }
        assertTrue("No statements added from " + fileName, connection.size() > 0);

        int count = connection.getStatements(null, null, null, false).asList().size();
        assertTrue("No statements added from " + fileName, count > 0);

        BooleanQuery sparqlQuery = (BooleanQuery)connection.prepareQuery(QueryLanguage.SPARQL, IOUtils.toString(sparql).replaceAll("http://rdfa.digitalbazaar.com/test-suite/test-cases/xhtml1/rdfa1.1/","http://localhost/rdfa/"));
        assertTrue("SPARQL query evaluation for "+fileName+" failed",sparqlQuery.evaluate());

        connection.close();
        repository.shutDown();
    }


}
