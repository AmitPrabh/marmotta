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
package org.apache.marmotta.platform.sparql.services.sparqlio.sparqlhtml;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.util.CDIContext;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.transform.JDOMResult;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.BooleanQueryResultWriter;
import org.openrdf.query.resultio.QueryResultFormat;
import org.openrdf.query.resultio.sparqlxml.SPARQLBooleanXMLWriter;
import org.openrdf.rio.RioSetting;
import org.openrdf.rio.WriterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SPARQL Boolean HTML RIO Writer
 * 
 * @author Sebastian Schaffert
 * @author Sergio Fernández
 */
public class SPARQLBooleanHTMLWriter implements BooleanQueryResultWriter {

    private static final Logger log = LoggerFactory.getLogger(SPARQLBooleanHTMLWriter.class);

    private OutputStream out;
    private ByteArrayOutputStream xmlOut;

    private SPARQLBooleanXMLWriter writer;
    private WriterConfig config;

    private Templates stylesheet;

    public SPARQLBooleanHTMLWriter(OutputStream out) {
        this.out = out;
        this.xmlOut = new ByteArrayOutputStream();
        this.writer = new SPARQLBooleanXMLWriter(xmlOut);

        Source s_stylesheet = new StreamSource(SPARQLBooleanHTMLWriter.class.getResourceAsStream("style.xsl"));
        try {
            stylesheet = TransformerFactory.newInstance().newTemplates(s_stylesheet);
        } catch (TransformerConfigurationException e) {
            log.error("could not compile stylesheet for rendering SPARQL results; result display not available!");
        }
    }

    /**
     * Gets the query result format that this writer uses.
     */
    @Override
    public BooleanQueryResultFormat getBooleanQueryResultFormat() {
        return new BooleanQueryResultFormat("SPARQL/HTML","text/html", Charset.forName("UTF-8"), "html");
    }

    /**
     * Writes the specified boolean value.
     */
    @Override
    @Deprecated
    public void write(boolean value) throws IOException {
    	try {
			handleBoolean(value);
		} catch (QueryResultHandlerException e) {
			throw new IOException(e);
		}
    }

	@Override
	public void endHeader() throws QueryResultHandlerException {
		
	}

	@Override
	public QueryResultFormat getQueryResultFormat() {
		return new QueryResultFormat("HTML", "text/html", Charset.forName("utf-8"), "html");
	}

	@Override
	public Collection<RioSetting<?>> getSupportedSettings() {
		return new ArrayList<RioSetting<?>>();
	}

	@Override
	public WriterConfig getWriterConfig() {
		return config;
	}

	@Override
	public void handleNamespace(String arg0, String arg1) throws QueryResultHandlerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleStylesheet(String arg0) throws QueryResultHandlerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setWriterConfig(WriterConfig config) {
		this.config = config;
	}

	@Override
	public void startDocument() throws QueryResultHandlerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startHeader() throws QueryResultHandlerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void endQueryResult() throws TupleQueryResultHandlerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleBoolean(boolean value) throws QueryResultHandlerException { 	
        try {
            writer.write(value);

            byte[] queryResult = xmlOut.toByteArray();

            // get server uri
            String server_uri = CDIContext.getInstance(ConfigurationService.class).getServerUri();

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
        	
        	Source input = new StreamSource(new ByteArrayInputStream(queryResult));

            Transformer transformer = stylesheet.newTransformer();
            transformer.setParameter("serverurl", server_uri);

            JDOMResult result = new JDOMResult();
            transformer.transform(input,result);
            Document output = result.getDocument();

            XMLOutputter printer = new XMLOutputter(Format.getPrettyFormat());
            printer.output(output, writer);
            writer.flush();
        } catch (Exception ex) {
            throw new QueryResultHandlerException("error while transforming XML results to HTML",ex);
        } finally {
            //writer.close();
        }
		
	}

	@Override
	public void handleLinks(List<String> links) throws QueryResultHandlerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleSolution(BindingSet bindings) throws TupleQueryResultHandlerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startQueryResult(List<String> start) throws TupleQueryResultHandlerException {
		// TODO Auto-generated method stub
		
	}
    
}
