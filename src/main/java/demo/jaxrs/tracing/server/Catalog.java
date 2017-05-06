/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package demo.jaxrs.tracing.server;


import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.tracing.Traceable;
import org.apache.cxf.tracing.TracerContext;
import org.apache.cxf.tracing.brave.TraceScope;
import org.apache.cxf.tracing.brave.jaxrs.BraveClientProvider;

import com.github.kristofa.brave.Brave;

import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.okhttp3.OkHttpSender;

@Path("/catalog")
public class Catalog {
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final CatalogStore store;

    public Catalog()  {
        store = new CatalogStore();
        System.out.println("Populating catalog");
        try {
			store.put("1", "myTitle");

	        store.put("2", "myTitle");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response addBook(@Context final UriInfo uriInfo, @Context final TracerContext tracing,
            @FormParam("title") final String title)  {
        try {
            final String id = UUID.randomUUID().toString();

            executor.submit(
                tracing.wrap("Inserting New Book",
                    new Traceable<Void>() {
                        public Void call(final TracerContext context) throws Exception {
                            store.put(id, title);
                            return null;
                        }
                    }
                )
            ).get(10, TimeUnit.SECONDS);

            return Response
                .created(uriInfo.getRequestUriBuilder().path(id).build())
                .build();
        } catch (final Exception ex) {
            return Response
                .serverError()
                .entity(Json
                     .createObjectBuilder()
                     .add("error", ex.getMessage())
                     .build())
                .build();
        }
    }

    
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public void getBooks(@Suspended final AsyncResponse response,
            @Context final TracerContext tracing) throws Exception {
        tracing.continueSpan(new Traceable<Void>() {
            @Override
            public Void call(final TracerContext context) throws Exception {
                System.out.println("In GetBooks...................");
            	executor.submit(tracing.wrap("Looking for books", new Traceable<Void>() {
                    @Override
                    public Void call(final TracerContext context) throws Exception {
                        System.out.println("In GetBooks...................");
                        response.resume(store.scan());
                        return null;
                    }
                }));

                return null;
            }
        });
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getBook(@PathParam("id") final String id) throws IOException {
        System.out.println("In GetBook(1)...................");
        System.out.println("Sleeping");
    	try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	System.out.println("Sleeping done");
    	final JsonObject book = store.get(id);

        if (book == null) {
            throw new NotFoundException("Book with does not exists: " + id);
        }

        return book;
    }
    
    @Produces( { MediaType.APPLICATION_JSON } )
    @GET
    @Path("/list")
    public JsonObject getBooks() throws Exception {
        System.out.println("Sleeping");
    	Thread.sleep(500);
    	System.out.println("Sleeping done");
    	final JsonObject book = store.get("2");
    	
    	 OkHttpSender sender = OkHttpSender.create("http://127.0.0.1:9411/api/v1/spans");
         AsyncReporter<Span> reporter = AsyncReporter.builder(sender).build();
         Brave brave = new Brave.Builder("myService2").reporter(reporter).build();
         final BraveClientProvider provider = new BraveClientProvider(brave);

         final Response response = WebClient
             .create("http://localhost:9000/catalog/1", Arrays.asList(provider))
             .accept(MediaType.APPLICATION_JSON)
             .get();
         
         
         
         
         Brave brave3 = new Brave.Builder("myServiceMap").reporter(reporter).build();
         final BraveClientProvider provider3 = new BraveClientProvider(brave3);

         final Response response3 = WebClient
             .create("http://localhost:9000/catalog/map", Arrays.asList(provider3))
             .accept(MediaType.APPLICATION_JSON)
             .get();

         response.close();
         response3.close();
         reporter.close();
         sender.close();
         
         

         response.close();
         reporter.close();
         sender.close();
  
		return book;

        
    }
    


    @Produces( { MediaType.APPLICATION_JSON } )
    @GET
    @Path("/map")
    public JsonObject getBooksMap() throws Exception {
        System.out.println("Sleeping");
    	Thread.sleep(500);
    	System.out.println("Sleeping done");
    	final JsonObject book = store.get("2");
    	
    	 OkHttpSender sender = OkHttpSender.create("http://127.0.0.1:9411/api/v1/spans");
         AsyncReporter<Span> reporter = AsyncReporter.builder(sender).build();
         Brave brave = new Brave.Builder("myServiceData").reporter(reporter).build();
         final BraveClientProvider provider = new BraveClientProvider(brave);

         final Response response = WebClient
             .create("http://localhost:9000/catalog/data", Arrays.asList(provider))
             .accept(MediaType.APPLICATION_JSON)
             .get();

         Thread.sleep(500);
         response.close();
         reporter.close();
         sender.close();
  
		return book;

        
    }
    


    @Produces( { MediaType.APPLICATION_JSON } )
    @GET
    @Path("/data")
    public JsonObject getBooksData() throws Exception {
        System.out.println("Sleeping");
    	Thread.sleep(500);
    	System.out.println("Sleeping done");
    	final JsonObject book = store.get("2");
    	Thread.sleep(300);
    	/* OkHttpSender sender = OkHttpSender.create("http://127.0.0.1:9411/api/v1/spans");
         AsyncReporter<Span> reporter = AsyncReporter.builder(sender).build();
         Brave brave = new Brave.Builder("myService2").reporter(reporter).build();
         final BraveClientProvider provider = new BraveClientProvider(brave);

         final Response response = WebClient
             .create("http://localhost:9000/catalog/1", Arrays.asList(provider))
             .accept(MediaType.APPLICATION_JSON)
             .get();


         response.close();
         reporter.close();
         sender.close();
  */
		return book;

        
    }
    

    
    
    

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") final String id) throws IOException {
        if (!store.remove(id)) {
            throw new NotFoundException("Book with does not exists: " + id);
        }

        return Response.ok().build();
    }
}


