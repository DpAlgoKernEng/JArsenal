package com.jguard.domain.audit.valueobject;

import com.jguard.domain.shared.exception.DomainException;

/**
 * 追踪信息值对象
 * 包含 IP 和 TraceId
 */
public class TraceInfo {

    private final String ip;
    private final String traceId;

    public TraceInfo(String ip, String traceId) {
        this.ip = ip;
        this.traceId = traceId;
    }

    public String ip() {
        return ip;
    }

    public String traceId() {
        return traceId;
    }

    public static TraceInfo of(String ip, String traceId) {
        return new TraceInfo(ip, traceId);
    }
}