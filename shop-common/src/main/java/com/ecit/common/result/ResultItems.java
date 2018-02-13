package com.ecit.common.result;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ResultItems<T> {

	/**
     * 请求状态
	 */
	private int status = 0;
	/**
	 * 总条数
	 */
	private int total = 0;
	/**
	 * 查询结果集
	 */
	private List<T> items;

	private String message;

	public ResultItems() {
	}

	public ResultItems(int status, int total, List<T> items) {
		this.status = status;
		this.total = total;
		this.items = items;
	}

	public ResultItems(int status, String message) {
		this.total = total;
		this.message = message;
	}

	public static ResultItems getReturnItems(int status, String message){
		return new ResultItems(status, message);
	}

	public static ResultItems getReturnItemsSuccess(String message){
		return new ResultItems(0, message);
	}

	public static ResultItems getReturnItemsFailure(String message){
		return new ResultItems(-1, message);
	}

	public static JsonObject getJsonObject(ResultItems items) {
		return JsonObject.mapFrom(items);
	}

	public static String getEncodePrettily(ResultItems items){
		return getJsonObject(items).encodePrettily();
	}
}