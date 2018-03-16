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
package com.hubrick.vertx.elasticsearch.hk2;

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
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Singleton;

/**
 * ElasticSearch HK2 Binder
 */
public class ElasticSearchBinder extends AbstractBinder {
    /**
     * Implement to provide binding definitions using the exposed binding
     * methods.
     */
    @Override
    protected void configure() {
        bind(DefaultTransportClientFactory.class).to(TransportClientFactory.class);
        bind(EnvElasticSearchConfigurator.class).to(ElasticSearchConfigurator.class);
        bind(DefaultElasticSearchService.class).to(InternalElasticSearchService.class).to(ElasticSearchService.class).in(Singleton.class);
        bind(DefaultElasticSearchAdminService.class).to(InternalElasticSearchAdminService.class).to(ElasticSearchAdminService.class).in(Singleton.class);
    }
}
