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
package ${package}.webservices;

import kiwi.core.model.Constants;
import org.apache.commons.lang.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import ${package}.api.MyService;

@Path("/${moduleKey}")
@ApplicationScoped
public class MyWebService {

    @Inject
    private MyService myService;

    @GET
    @Produces(Constants.MIME_TYPE_HTML)
    public Response hello(@QueryParam("name") String name) {
        if (StringUtils.isEmpty(name)) {
            // No name given? Invalid request.
            return Response.status(Status.BAD_REQUEST).entity("Missing Parameter 'name'").build();
        }

        // Return the greeting.
        return Response.ok(myService.helloWorld(name)).build();
    }

    @POST
    public Response doThis(@QueryParam("turns") @DefaultValue("2") int turns) {
        myService.doThis(turns);
        return Response.ok().build();
    }

}
