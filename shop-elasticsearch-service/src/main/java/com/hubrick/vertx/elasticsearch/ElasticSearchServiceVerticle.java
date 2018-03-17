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
package com.hubrick.vertx.elasticsearch;

import com.hubrick.vertx.elasticsearch.impl.DefaultElasticSearchAdminService;
import com.hubrick.vertx.elasticsearch.impl.DefaultElasticSearchService;
import com.hubrick.vertx.elasticsearch.impl.DefaultTransportClientFactory;
import com.hubrick.vertx.elasticsearch.impl.JsonElasticSearchConfigurator;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * ElasticSearch event bus service verticle
 */
public class ElasticSearchServiceVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LogManager.getLogger(ElasticSearchServiceVerticle.class);

    private ElasticSearchService service;
    private ElasticSearchAdminService adminService;

    public ElasticSearchServiceVerticle() {}

    @Override
    public void start() throws Exception {

        // workaround for problem between ES netty and vertx (both wanting to set the same value)
        System.setProperty("es.set.netty.runtime.available.processors", "false");
        this.service = new DefaultElasticSearchService(new DefaultTransportClientFactory(), new JsonElasticSearchConfigurator(this.config()));
        this.adminService = new DefaultElasticSearchAdminService(new DefaultElasticSearchService(new DefaultTransportClientFactory(),
                new JsonElasticSearchConfigurator(this.config())));

        String address = config().getString("address");
        if (address == null || address.isEmpty()) {
            throw new IllegalStateException("address field must be specified in config for service verticle");
        }
        String adminAddress = config().getString("address.admin");
        if (adminAddress == null || adminAddress.isEmpty()) {
            adminAddress = address + ".admin";
        }

        // Register service as an event bus proxy
        new ServiceBinder(vertx).setAddress(address).register(ElasticSearchService.class, service);
        new ServiceBinder(vertx).setAddress(adminAddress).register(ElasticSearchAdminService.class, adminService);

        // Start the service
        service.start();
        LOGGER.info("shop-elasticsearch-service server started!");
    }

    @Override
    public void stop() throws Exception {
        service.stop();
    }

}
