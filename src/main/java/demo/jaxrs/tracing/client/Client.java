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

package demo.jaxrs.tracing.client;

import java.util.Arrays;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.tracing.brave.jaxrs.BraveClientProvider;

import com.github.kristofa.brave.Brave;

import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.okhttp3.OkHttpSender;

public final class Client {
    private Client() {
    }

    public static void main(final String[] args) throws Exception {
       	System.out.println("In Client *************8");
        OkHttpSender sender = OkHttpSender.create("http://127.0.0.1:9411/api/v1/spans");
        AsyncReporter<Span> reporter = AsyncReporter.builder(sender).build();
        Brave brave = new Brave.Builder("client1").reporter(reporter).build();
        final BraveClientProvider provider = new BraveClientProvider(brave);

        final Response response = WebClient
            .create("http://localhost:9000/catalog/2", Arrays.asList(provider))
            .accept(MediaType.APPLICATION_JSON)
            .get();

        System.out.println(response.getStatus());
        System.out.println(response.readEntity(String.class));
        Brave brave2 = new Brave.Builder("client2").reporter(reporter).build();
        final BraveClientProvider provider2 = new BraveClientProvider(brave2);
    
        final Response response1 = WebClient
                .create("http://localhost:9000/catalog/list", Arrays.asList(provider2))
                .accept(MediaType.APPLICATION_JSON)
                .get();

            System.out.println("http://localhost:9000/catalog/list>>"+ response1.getStatus());
            System.out.println(response1.readEntity(String.class));
        response.close();
        response1.close();
        reporter.close();
        sender.close();
    }
}

/*public final class Client {
    private Client() {
    }

    public static void main(final String[] args) throws Exception {
    	System.out.println("In Client *************8");
    	final Brave brave = new Brave.Builder().build();
        final BraveClientProvider provider = new BraveClientProvider(brave);

        final Response response = WebClient
            .create("http://localhost:9000/catalog/1", Arrays.asList(provider))
            .accept(MediaType.APPLICATION_JSON)
            .get();

        System.out.println(response.readEntity(String.class));
        
    	OkHttpSender sender = OkHttpSender.create("http://localhost:9411/api/v1/spans");
      	
    	
    	 final Brave brave = new Brave.Builder("web-client")
    	    .reporter(AsyncReporter.builder(sender).build())
    	    .traceSampler(Sampler.ALWAYS_SAMPLE) 
    	    .build();
    	         
    	Response response = WebClient
    	    .create("http://localhost:9000/catalog/1", Arrays.asList(new BraveClientProvider(brave)))
    	    .accept(MediaType.APPLICATION_JSON)
    	    .get();
       
        
        response.close();
    }
}
*/