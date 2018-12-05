package com.ecit.common.result;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import io.vertx.core.json.JsonObject;

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
	private T items;

	private String message;

	private int page = 0;

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public T getItems() {
		return items;
	}

	public void setItems(T items) {
		this.items = items;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public ResultItems() {
	}

	public ResultItems(int status, int total, T items) {
		this.status = status;
		this.total = total;
		this.items = items;
	}

	public ResultItems(int status, String message) {
		this.status = status;
		this.message = message;
	}

	public ResultItems(int status, int total, T items, String message) {
		this.status = status;
		this.total = total;
		this.items = items;
		this.message = message;
	}

	public ResultItems(int status, int total, T items, String message, int page) {
		this.status = status;
		this.total = total;
		this.items = items;
		this.message = message;
		this.page = page;
	}

	public static ResultItems getReturnItems(int status, String message){
		return new ResultItems(status, message);
	}

	public static ResultItems getReturnItemsSuccess(String message){
		return new ResultItems(0, message);
	}

	public static <T> ResultItems getReturnItemsSuccess(int total, T items){
		return new ResultItems(0, total, items);
	}

	public static <T> ResultItems getReturnItemsSuccess(int total, T items, String message){
		return new ResultItems(0, total, items, message);
	}

	public static <T> ResultItems getReturnItemsSuccess(int total, T items, String message, int page){
		return new ResultItems(0, total, items, message, page);
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