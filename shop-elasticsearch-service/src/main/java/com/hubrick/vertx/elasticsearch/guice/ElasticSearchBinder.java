/**
 * Copyright (C) 2016 Etaia AS (oss@hubrick.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hubrick.vertx.elasticsearch.guice;

import com.hubrick.vertx.elasticsearch.ElasticSearchAdminService;
import com.hubrick.vertx.elasticsearch.ElasticSearchConfigurator;
import com.hubrick.vertx.elasticsearch.ElasticSearchService;
import com.hubrick.vertx.elasticsearch.TransportClientFactory;
import com.hubrick.vertx.elasticsearch.impl.DefaultElasticSearchAdminService;
import com.hubrick.vertx.elasticsearch.impl.DefaultElasticSearchService;
import com.hubrick.vertx.elasticsearch.impl.DefaultTransportClientFactory;
import com.hubrick.vertx.elasticsearch.impl.EnvElasticSearchConfigurator;
import com.hubrick.vertx.elasticsearch.internal.InternalElasticSearchAdminService;
import com.hubrick.vertx.elasticsearch.internal.InternalElasticSearchService;
import com.google.inject.AbstractModule;

import javax.inject.Singleton;

/**
 * ElasticSearch Guice Binder
 */
public class ElasticSearchBinder extends AbstractModule {
    /**
     * Implement to provide binding definitions using the exposed binding
     * methods.
     */
    @Override
    protected void configure() {

        bind(TransportClientFactory.class).to(DefaultTransportClientFactory.class);
        bind(ElasticSearchConfigurator.class).to(EnvElasticSearchConfigurator.class);

        bind(DefaultElasticSearchService.class).in(Singleton.class);
        bind(DefaultElasticSearchAdminService.class).in(Singleton.class);

        bind(ElasticSearchService.class).to(DefaultElasticSearchService.class);
        bind(InternalElasticSearchService.class).to(DefaultElasticSearchService.class);

        bind(ElasticSearchAdminService.class).to(DefaultElasticSearchAdminService.class);
        bind(InternalElasticSearchAdminService.class).to(DefaultElasticSearchAdminService.class);

    }
}
