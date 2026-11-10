package com.junia.loadbalancer.model;

import java.time.LocalDateTime;

public class HttpRequest extends Request {

    public HttpRequest(String id, LocalDateTime arrivalTime) {
        super(id, arrivalTime);
    }

    @Override
    public int getLoad() {
        return 1;
    }

    @Override
    public RequestType getType() {
        return RequestType.HTTP;
    }
}
