package com.ecit.common.utils;


import io.vertx.core.http.HttpServerRequest;

public class IpUtils {

	public static String getIpAddr(HttpServerRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if(ip == null || ip.length() == 0 || "Proxy-Client-IP".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if(ip == null || ip.length() == 0 || "WL-Proxy-Client-IP".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if(ip == null || ip.length() == 0 || "X-Real-IP".equalsIgnoreCase(ip)) {
			ip = request.getHeader("X-Real-IP");
		}
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.host();
		}
		return ip;
	}
}
