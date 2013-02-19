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
package at.newmedialab.lmf.client.clients;

import at.newmedialab.lmf.client.ClientConfiguration;
import at.newmedialab.lmf.client.exception.LMFClientException;
import at.newmedialab.lmf.client.util.HTTPUtil;
import com.google.common.io.ByteStreams;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * This client class provides support for importing ontologies in various formats into the Linked Media Framework.
 * 
 * Author: Sebastian Schaffert
 */
public class ImportClient {

    private static Logger log = LoggerFactory.getLogger(ImportClient.class);

    private static final String URL_TYPES_SERVICE = "/import/types";
    private static final String URL_UPLOAD_SERVICE = "/import/upload";

    private ClientConfiguration config;
    
    private Set<String> acceptableTypes;

    public ImportClient(ClientConfiguration config) {
        this.acceptableTypes = new HashSet<String>();
        this.config = config;
        try {
            this.acceptableTypes = getSupportedTypes();
        } catch (IOException e) {
            log.error("I/O Exception while trying to retrieve supported types",e);
        } catch (LMFClientException e) {
            log.error("Client Exception while trying to retrieve supported types",e);
        }
    }

    /**
     * Return a set of mime types representing the types that are accepted by the LMF server.
     * 
     * @return
     * @throws IOException
     * @throws LMFClientException
     */
    public Set<String> getSupportedTypes() throws IOException, LMFClientException {
        HttpClient httpClient = HTTPUtil.createClient(config);

        String serviceUrl = config.getLmfUri() + URL_TYPES_SERVICE;

        HttpGet get = new HttpGet(serviceUrl);
        get.setHeader("Accept", "application/json");
        
        try {
            HttpResponse response = httpClient.execute(get);

            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    log.debug("list of import types retrieved successfully");
                    ObjectMapper mapper = new ObjectMapper();
                    Set<String> result =
                            mapper.readValue(response.getEntity().getContent(),new TypeReference<Set<String>>(){});
                    return result;
                 default:
                    log.error("error retrieving list of import types: {} {}",new Object[] {response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase()});
                    throw new LMFClientException("error retrieving list of import types: "+response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
            }

        } finally {
            get.releaseConnection();
        }
    }

    /**
     * Upload/Import a dataset in the LMF Server. The dataset is given as an Inputstream that contains data of the
     * mime type passed as argument. The mime type must be one of the acceptable types of the server.
     *
     * @param in InputStream to read the dataset from; will be consumed by this method
     * @param mimeType mime type of the input data
     * @throws IOException
     * @throws LMFClientException
     */
    public void uploadDataset(final InputStream in, final String mimeType) throws IOException, LMFClientException {
        //Preconditions.checkArgument(acceptableTypes.contains(mimeType));

        HttpClient httpClient = HTTPUtil.createClient(config);

        HttpPost post = HTTPUtil.createPost(URL_UPLOAD_SERVICE, config);
        post.setHeader("Content-Type", mimeType);
        
        ContentProducer cp = new ContentProducer() {
            @Override
            public void writeTo(OutputStream outstream) throws IOException {
                ByteStreams.copy(in,outstream);
            }
        };
        post.setEntity(new EntityTemplate(cp));
        
        ResponseHandler<Boolean> handler = new ResponseHandler<Boolean>() {
            @Override
            public Boolean handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                EntityUtils.consume(response.getEntity());
                switch(response.getStatusLine().getStatusCode()) {
                    case 200:
                        log.debug("dataset uploaded updated successfully");
                        return true;
                    case 412:
                        log.error("mime type {} not acceptable by import service",mimeType);
                        return false;
                    default:
                        log.error("error uploading dataset: {} {}",new Object[] {response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase()});
                        return false;
                }
            }
        };

        try {
            httpClient.execute(post, handler);
        } catch(IOException ex) {
            post.abort();
            throw ex;
        } finally {
            post.releaseConnection();
        }

    }

    /**
     * Upload the data contained in the string using the given mime type; convenience method wrapping the generic
     * InputStream-based method.
     *
     * @param data
     * @param mimeType
     * @throws IOException
     * @throws LMFClientException
     */
    public void uploadDataset(String data, String mimeType) throws IOException, LMFClientException {
        uploadDataset(new ByteArrayInputStream(data.getBytes("utf-8")), mimeType);
    }

}
