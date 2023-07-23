package com.ecit.model;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

@DataObject
public class Total {

    private Long value;
    private String relation;

    public static final String JSON_FIELD_VALUE = "value";
    public static final String JSON_FIELD_RELATION = "relation";

    public Total() {
    }

    public Total(Total other) {
        this.value = other.getValue();
        this.relation = other.getRelation();
    }

    public Total(JsonObject jsonObject) {
        this.value = jsonObject.getLong(JSON_FIELD_VALUE);
        this.relation = jsonObject.getString(JSON_FIELD_RELATION);
    }

    public Long getValue() {
        return value;
    }

    public Total setValue(Long value) {
        this.value = value;
        return this;
    }

    public String getRelation() {
        return relation;
    }

    public Total setRelation(String relation) {
        this.relation = relation;
        return this;
    }

    public JsonObject toJson() {

        final JsonObject json = new JsonObject();

        if (value != null) json.put(JSON_FIELD_VALUE, value);
        if (relation != null) json.put(JSON_FIELD_RELATION, relation);

        return json;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
