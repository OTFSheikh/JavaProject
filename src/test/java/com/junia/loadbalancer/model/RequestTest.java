package com.junia.loadbalancer.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RequestTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 15, 9, 0);

    @Test
    void shouldComputeLoadDependingOnRequestType() {
        assertEquals(1, new HttpRequest("R1", NOW).getLoad());
        assertEquals(4, new FileUploadRequest("R2", NOW, 25).getLoad());
        assertEquals(7, new VideoStreamRequest("R3", NOW, 30).getLoad());
    }

    @Test
    void shouldKnowItsOwnType() {
        Request request = new VideoStreamRequest("R1", NOW, 30);

        assertEquals(RequestType.STREAM, request.getType());
    }

    @Test
    void shouldUseMinimumLoadForSmallUploadAndShortStream() {
        assertEquals(2, new FileUploadRequest("R1", NOW, 1).getLoad());
        assertEquals(5, new VideoStreamRequest("R2", NOW, 1).getLoad());
    }

    @Test
    void shouldThrowExceptionWhenFileSizeIsNotPositive() {
        assertThrows(IllegalArgumentException.class, () -> new FileUploadRequest("R1", NOW, 0));
    }

    @Test
    void shouldThrowExceptionWhenIdIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new HttpRequest(" ", NOW));
    }
}
