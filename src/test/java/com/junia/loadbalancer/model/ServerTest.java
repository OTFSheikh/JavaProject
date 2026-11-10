package com.junia.loadbalancer.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServerTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 15, 9, 0);
    private Server server;

    @BeforeEach
    void setUp() {
        server = new Server("web-01", 10, 1, ServerStatus.UP);
    }

    @Test
    void shouldAddRequestLoadWhenAccepted() {
        server.accept(new HttpRequest("R1", NOW));
        server.accept(new FileUploadRequest("R2", NOW, 25));

        assertEquals(5, server.getCurrentLoad());
        assertEquals(50.0, server.getLoadPercentage(), 0.001);
    }

    @Test
    void shouldAcceptRequestThatFillsExactlyTheCapacity() {
        server.accept(new VideoStreamRequest("R1", NOW, 75));

        assertEquals(10, server.getCurrentLoad());
        assertEquals(0, server.getRemainingCapacity());
    }

    @Test
    void shouldRefuseRequestWhenNotEnoughCapacity() {
        server.accept(new VideoStreamRequest("R1", NOW, 30));

        Request tooBig = new FileUploadRequest("R2", NOW, 25);

        assertFalse(server.canAccept(tooBig));
        assertThrows(IllegalStateException.class, () -> server.accept(tooBig));
    }

    @Test
    void shouldRefuseRequestWhenServerIsDown() {
        Server downServer = new Server("backup", 10, 1, ServerStatus.DOWN);

        assertFalse(downServer.canAccept(new HttpRequest("R1", NOW)));
    }

    @Test
    void shouldGiveBackRequestsWhenGoingDown() {
        server.accept(new HttpRequest("R1", NOW));
        server.accept(new HttpRequest("R2", NOW));

        List<Request> lost = server.goDown();

        assertEquals(2, lost.size());
        assertEquals(ServerStatus.DOWN, server.getStatus());
        assertEquals(0, server.getCurrentLoad());
    }

    @Test
    void shouldThrowExceptionWhenCapacityIsNotPositive() {
        assertThrows(IllegalArgumentException.class, () -> new Server("web", 0, 1, ServerStatus.UP));
        assertThrows(IllegalArgumentException.class, () -> new Server("web", 10, -1, ServerStatus.UP));
    }
}
