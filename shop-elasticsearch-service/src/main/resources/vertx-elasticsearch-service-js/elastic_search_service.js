/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

/** @module vertx-elasticsearch-service-js/elastic_search_service */
var utils = require('vertx-js/util/utils');
var Vertx = require('vertx-js/vertx');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JElasticSearchService = com.hubrick.vertx.elasticsearch.ElasticSearchService;
var IndexResponse = com.hubrick.vertx.elasticsearch.model.IndexResponse;
var IndexOptions = com.hubrick.vertx.elasticsearch.model.IndexOptions;
var GetOptions = com.hubrick.vertx.elasticsearch.model.GetOptions;
var DeleteByQueryResponse = com.hubrick.vertx.elasticsearch.model.DeleteByQueryResponse;
var SearchResponse = com.hubrick.vertx.elasticsearch.model.SearchResponse;
var DeleteResponse = com.hubrick.vertx.elasticsearch.model.DeleteResponse;
var UpdateOptions = com.hubrick.vertx.elasticsearch.model.UpdateOptions;
var GetResponse = com.hubrick.vertx.elasticsearch.model.GetResponse;
var UpdateResponse = com.hubrick.vertx.elasticsearch.model.UpdateResponse;
var SearchOptions = com.hubrick.vertx.elasticsearch.model.SearchOptions;
var BulkIndexResponse = com.hubrick.vertx.elasticsearch.model.BulkIndexResponse;
var DeleteOptions = com.hubrick.vertx.elasticsearch.model.DeleteOptions;
var SuggestOptions = com.hubrick.vertx.elasticsearch.model.SuggestOptions;
var SuggestResponse = com.hubrick.vertx.elasticsearch.model.SuggestResponse;
var SearchScrollOptions = com.hubrick.vertx.elasticsearch.model.SearchScrollOptions;
var DeleteByQueryOptions = com.hubrick.vertx.elasticsearch.model.DeleteByQueryOptions;

/**
 ElasticSearch service

 @class
*/
var ElasticSearchService = function(j_val) {

  var j_elasticSearchService = j_val;
  var that = this;

  /**

   @public

   */
  this.start = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_elasticSearchService["start()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   */
  this.stop = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_elasticSearchService["stop()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   http://www.elastic.co/guide/en/elasticsearch/client/java-api/1.4/index_.html

   @public
   @param index {string} the index name 
   @param type {string} the type name 
   @param source {Object} the source to be indexed 
   @param options {Object} optional index options (id, timeout, ttl, etc.) 
   @param resultHandler {function} result handler callback 
   */
  this.index = function(index, type, source, options, resultHandler) {
    var __args = arguments;
    if (__args.length === 5 && typeof __args[0] === 'string' && typeof __args[1] === 'string' && (typeof __args[2] === 'object' && __args[2] != null) && (typeof __args[3] === 'object' && __args[3] != null) && typeof __args[4] === 'function') {
      j_elasticSearchService["index(java.lang.String,java.lang.String,io.vertx.core.json.JsonObject,com.hubrick.vertx.elasticsearch.model.IndexOptions,io.vertx.core.Handler)"](index, type, utils.convParamJsonObject(source), options != null ? new IndexOptions(new JsonObject(JSON.stringify(options))) : null, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnDataObject(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param index {string} 
   @param type {string} 
   @param sources {Array.<Object>} 
   @param options {Object} 
   @param resultHandler {function} 
   */
  this.bulkIndex = function(index, type, sources, options, resultHandler) {
    var __args = arguments;
    if (__args.length === 5 && typeof __args[0] === 'string' && typeof __args[1] === 'string' && typeof __args[2] === 'object' && __args[2] instanceof Array && (typeof __args[3] === 'object' && __args[3] != null) && typeof __args[4] === 'function') {
      j_elasticSearchService["bulkIndex(java.lang.String,java.lang.String,java.util.List,com.hubrick.vertx.elasticsearch.model.IndexOptions,io.vertx.core.Handler)"](index, type, utils.convParamListJsonObject(sources), options != null ? new IndexOptions(new JsonObject(JSON.stringify(options))) : null, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnDataObject(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   http://www.elastic.co/guide/en/elasticsearch/client/java-api/1.4/java-update-api.html

   @public
   @param index {string} the index name 
   @param type {string} the type name 
   @param id {string} the source id to update 
   @param options {Object} the update options (doc, script, etc.) 
   @param resultHandler {function} result handler callback 
   */
  this.update = function(index, type, id, options, resultHandler) {
    var __args = arguments;
    if (__args.length === 5 && typeof __args[0] === 'string' && typeof __args[1] === 'string' && typeof __args[2] === 'string' && (typeof __args[3] === 'object' && __args[3] != null) && typeof __args[4] === 'function') {
      j_elasticSearchService["update(java.lang.String,java.lang.String,java.lang.String,com.hubrick.vertx.elasticsearch.model.UpdateOptions,io.vertx.core.Handler)"](index, type, id, options != null ? new UpdateOptions(new JsonObject(JSON.stringify(options))) : null, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnDataObject(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   http://www.elastic.co/guide/en/elasticsearch/client/java-api/1.4/get.html

   @public
   @param index {string} the index name 
   @param type {string} the type name 
   @param id {string} the source id to update 
   @param options {Object} the update options 
   @param resultHandler {function} result handler callback 
   */
  this.get = function(index, type, id, options, resultHandler) {
    var __args = arguments;
    if (__args.length === 5 && typeof __args[0] === 'string' && typeof __args[1] === 'string' && typeof __args[2] === 'string' && (typeof __args[3] === 'object' && __args[3] != null) && typeof __args[4] === 'function') {
      j_elasticSearchService["get(java.lang.String,java.lang.String,java.lang.String,com.hubrick.vertx.elasticsearch.model.GetOptions,io.vertx.core.Handler)"](index, type, id, options != null ? new GetOptions(new JsonObject(JSON.stringify(options))) : null, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnDataObject(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param indices {Array.<string>} 
   @param options {Object} 
   @param resultHandler {function} 
   */
  this.search = function(indices, options, resultHandler) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] === 'object' && __args[0] instanceof Array && (typeof __args[1] === 'object' && __args[1] != null) && typeof __args[2] === 'function') {
      j_elasticSearchService["search(java.util.List,com.hubrick.vertx.elasticsearch.model.SearchOptions,io.vertx.core.Handler)"](utils.convParamListBasicOther(indices), options != null ? new SearchOptions(new JsonObject(JSON.stringify(options))) : null, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnDataObject(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   http://www.elastic.co/guide/en/elasticsearch/reference/1.4/search-request-scroll.html

   @public
   @param scrollId {string} 
   @param options {Object} 
   @param resultHandler {function} 
   */
  this.searchScroll = function(scrollId, options, resultHandler) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] === 'string' && (typeof __args[1] === 'object' && __args[1] != null) && typeof __args[2] === 'function') {
      j_elasticSearchService["searchScroll(java.lang.String,com.hubrick.vertx.elasticsearch.model.SearchScrollOptions,io.vertx.core.Handler)"](scrollId, options != null ? new SearchScrollOptions(new JsonObject(JSON.stringify(options))) : null, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnDataObject(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   http://www.elastic.co/guide/en/elasticsearch/client/java-api/1.4/delete.html

   @public
   @param index {string} the index name 
   @param type {string} the type name 
   @param id {string} the source id to delete 
   @param options {Object} optional delete options (timeout, etc.) 
   @param resultHandler {function} result handler callback 
   */
  this.delete = function(index, type, id, options, resultHandler) {
    var __args = arguments;
    if (__args.length === 5 && typeof __args[0] === 'string' && typeof __args[1] === 'string' && typeof __args[2] === 'string' && (typeof __args[3] === 'object' && __args[3] != null) && typeof __args[4] === 'function') {
      j_elasticSearchService["delete(java.lang.String,java.lang.String,java.lang.String,com.hubrick.vertx.elasticsearch.model.DeleteOptions,io.vertx.core.Handler)"](index, type, id, options != null ? new DeleteOptions(new JsonObject(JSON.stringify(options))) : null, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnDataObject(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   https://www.elastic.co/guide/en/elasticsearch/reference/current/search-suggesters.html

   @public
   @param indices {Array.<string>} the index names 
   @param options {Object} optional suggest options 
   @param resultHandler {function} result handler callback 
   */
  this.suggest = function(indices, options, resultHandler) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] === 'object' && __args[0] instanceof Array && (typeof __args[1] === 'object' && __args[1] != null) && typeof __args[2] === 'function') {
      j_elasticSearchService["suggest(java.util.List,com.hubrick.vertx.elasticsearch.model.SuggestOptions,io.vertx.core.Handler)"](utils.convParamListBasicOther(indices), options != null ? new SuggestOptions(new JsonObject(JSON.stringify(options))) : null, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnDataObject(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-delete-by-query.html

   @public
   @param indices {Array.<string>} the index names 
   @param query {Object} the query that will be used for deletion 
   @param options {Object} delete by query options (timeout, etc.) 
   @param resultHandler {function} result handler callback 
   */
  this.deleteByQuery = function(indices, query, options, resultHandler) {
    var __args = arguments;
    if (__args.length === 4 && typeof __args[0] === 'object' && __args[0] instanceof Array && (typeof __args[1] === 'object' && __args[1] != null) && (typeof __args[2] === 'object' && __args[2] != null) && typeof __args[3] === 'function') {
      j_elasticSearchService["deleteByQuery(java.util.List,io.vertx.core.json.JsonObject,com.hubrick.vertx.elasticsearch.model.DeleteByQueryOptions,io.vertx.core.Handler)"](utils.convParamListBasicOther(indices), utils.convParamJsonObject(query), options != null ? new DeleteByQueryOptions(new JsonObject(JSON.stringify(options))) : null, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnDataObject(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_elasticSearchService;
};

/**

 @memberof module:vertx-elasticsearch-service-js/elastic_search_service
 @param vertx {Vertx} 
 @param address {string} 
 @return {ElasticSearchService}
 */
ElasticSearchService.createEventBusProxy = function(vertx, address) {
  var __args = arguments;
  if (__args.length === 2 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] === 'string') {
    return utils.convReturnVertxGen(JElasticSearchService["createEventBusProxy(io.vertx.core.Vertx,java.lang.String)"](vertx._jdel, address), ElasticSearchService);
  } else throw new TypeError('function invoked with invalid arguments');
};

// We export the Constructor function
module.exports = ElasticSearchService;