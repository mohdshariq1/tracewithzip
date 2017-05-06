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


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.apache.cxf.jaxrs.provider.jsrjsonp.JsrJsonpProvider;
import org.apache.cxf.tracing.brave.jaxrs.BraveFeature;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.Sampler;

import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.Sender;
import zipkin.reporter.okhttp3.OkHttpSender;

@ApplicationPath("/")
public class CatalogApplication extends Application {
    @Override
    public Set<Object> getSingletons() {
       	System.out.println("In CatApp *************8");
        
    	OkHttpSender sender = OkHttpSender.create("http://127.0.0.1:9411/api/v1/spans");
        AsyncReporter<Span> reporter = AsyncReporter.builder(sender).build();
        Brave brave = new Brave.Builder("server").reporter(reporter).build();
        return new HashSet<>(
            Arrays.asList(
                new Catalog(),
                new BraveFeature(brave),
                new JsrJsonpProvider()
            )
        );
    }
}
/*
@ApplicationPath("/")
public class CatalogApplication extends Application {
    @Override
    public Set<Object> getSingletons() {
    	System.out.println("Starting CatalogApp.....******");
    	 Sender sender = OkHttpSender.create("http://localhost:9411/api/v1/spans");
     	
    	 final Brave brave = new Brave.Builder("tracer")
    		    .reporter(AsyncReporter.builder(sender).build())
    		    .traceSampler(Sampler.ALWAYS_SAMPLE)  or any other Sampler 
    		    .build();
    	
    	
        return new HashSet<>(
            Arrays.asList(
                new Catalog(),
                new BraveFeature(brave),
                new JsrJsonpProvider()
            )
        );
    }
}
*/