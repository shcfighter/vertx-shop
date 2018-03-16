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
package com.hubrick.vertx.elasticsearch.model;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Optional;

/**
 * Sort option
 */
@DataObject
public class ScriptFieldOption {

    private String script;
    private ScriptType scriptType;
    private String lang;
    private JsonObject params = new JsonObject();

    public static final String JSON_FIELD_SCRIPT = "script";
    public static final String JSON_FIELD_SCRIPT_TYPE = "scriptType";
    public static final String JSON_FIELD_LANG = "lang";
    public static final String JSON_FIELD_PARAMS = "params";

    public ScriptFieldOption() {
    }

    public ScriptFieldOption(ScriptFieldOption other) {
        script = other.getScript();
        scriptType = other.getScriptType();
        lang = other.getLang();
        params = other.getParams();
    }

    public ScriptFieldOption(JsonObject json) {
        script = json.getString(JSON_FIELD_SCRIPT);
        scriptType = Optional.ofNullable(json.getString(JSON_FIELD_SCRIPT_TYPE)).map(ScriptType::valueOf).orElse(null);
        lang = json.getString(JSON_FIELD_LANG);
        params = json.getJsonObject(JSON_FIELD_PARAMS);
    }

    public String getScript() {
        return script;
    }

    public ScriptFieldOption setScript(String script) {
        this.script = script;
        return this;
    }

    public ScriptType getScriptType() {
        return scriptType;
    }

    public ScriptFieldOption setScriptType(ScriptType scriptType) {
        this.scriptType = scriptType;
        return this;
    }

    public String getLang() {
        return lang;
    }

    public ScriptFieldOption setLang(String lang) {
        this.lang = lang;
        return this;
    }

    @GenIgnore
    public ScriptFieldOption addParam(String name, String value) {
        params.put(name, value);
        return this;
    }

    public JsonObject getParams() {
        return params;
    }

    public ScriptFieldOption setParams(JsonObject params) {
        this.params = params;
        return this;
    }

    public JsonObject toJson() {

        final JsonObject jsonObject = new JsonObject();

        if (script != null) jsonObject.put(JSON_FIELD_SCRIPT, script);
        if (scriptType != null) jsonObject.put(JSON_FIELD_SCRIPT_TYPE, scriptType.name());
        if (lang != null) jsonObject.put(JSON_FIELD_LANG, lang);
        if (params != null) jsonObject.put(JSON_FIELD_PARAMS, params);

        return jsonObject;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
