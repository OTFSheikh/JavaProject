package com.junia.loadbalancer.service;

import com.junia.loadbalancer.exception.NoServerAvailableException;
import com.junia.loadbalancer.model.FileUploadRequest;
import com.junia.loadbalancer.model.HttpRequest;
import com.junia.loadbalancer.model.Request;
import com.junia.loadbalancer.model.Server;
import com.junia.loadbalancer.model.ServerStatus;
import com.junia.loadbalancer.model.VideoStreamRequest;
import com.junia.loadbalancer.strategy.LeastLoadedStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LoadBalancerTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 15, 9, 0);
    private Server web1;
    private Server web2;
    private Server backup;
    private LoadBalancer loadBalancer;

    @BeforeEach
    void setUp() {
        web1 = new Server("web-01", 10, 1, ServerStatus.UP);
        web2 = new Server("web-02", 10, 1, ServerStatus.UP);
        backup = new Server("backup", 10, 1, ServerStatus.DOWN);
        loadBalancer = new LoadBalancer(List.of(web1, web2, backup), new LeastLoadedStrategy());
    }

    @Test
    void shouldAssignRequestToAServer() throws NoServerAvailableException {
        Server chosen = loadBalancer.assign(new HttpRequest("R1", NOW));

        assertEquals(1, chosen.getCurrentLoad());
    }

    @Test
    void shouldThrowExceptionWhenAllServersAreFull() {
        Request huge = new FileUploadRequest("R1", NOW, 500);

        assertThrows(NoServerAvailableException.class, () -> loadBalancer.assign(huge));
    }

    @Test
    void shouldRejectRequestsThatDoNotFitWithoutStopping() {
        List<Request> requests = List.of(
                new VideoStreamRequest("R1", NOW, 75),
                new VideoStreamRequest("R2", NOW.plusSeconds(1), 75),
                new HttpRequest("R3", NOW.plusSeconds(2))
        );

        loadBalancer.assignAll(requests);

        // les deux serveurs sont pleins apres les 2 streams
        assertEquals(1, loadBalancer.getRejectedRequests().size());
        assertEquals("R3", loadBalancer.getRejectedRequests().get(0).getId());
    }

    @Test
    void shouldAssignRequestsInArrivalOrder() {
        Request late = new VideoStreamRequest("LATE", NOW.plusMinutes(5), 75);
        Request early1 = new VideoStreamRequest("EARLY1", NOW, 75);
        Request early2 = new VideoStreamRequest("EARLY2", NOW.plusMinutes(1), 75);

        loadBalancer.assignAll(List.of(late, early1, early2));

        assertEquals(List.of(late), loadBalancer.getRejectedRequests());
    }

    @Test
    void shouldMoveRequestsToOtherServerWhenServerFails() throws NoServerAvailableException {
        web1.accept(new HttpRequest("R1", NOW));
        web1.accept(new HttpRequest("R2", NOW));

        List<Request> lost = loadBalancer.failServer("web-01");

        assertTrue(lost.isEmpty());
        assertEquals(0, web1.getCurrentLoad());
        assertEquals(2, web2.getCurrentLoad());
    }

    @Test
    void shouldLoseRequestsWhenNoPlaceAfterFailure() {
        web1.accept(new VideoStreamRequest("R1", NOW, 75));
        web2.accept(new VideoStreamRequest("R2", NOW, 75));

        List<Request> lost = loadBalancer.failServer("web-01");

        assertEquals(1, lost.size());
        assertEquals(1, loadBalancer.getRejectedRequests().size());
    }

    @Test
    void shouldPlaceRejectedRequestsWhenServerRecovers() {
        loadBalancer.assignAll(List.of(
                new VideoStreamRequest("R1", NOW, 75),
                new VideoStreamRequest("R2", NOW.plusSeconds(1), 75),
                new VideoStreamRequest("R3", NOW.plusSeconds(2), 75)
        ));
        assertEquals(1, loadBalancer.getRejectedRequests().size());

        List<Request> placed = loadBalancer.recoverServer("backup");

        assertEquals(1, placed.size());
        assertTrue(loadBalancer.getRejectedRequests().isEmpty());
        assertEquals(10, backup.getCurrentLoad());
    }

    @Test
    void shouldThrowExceptionForUnknownServer() {
        assertThrows(IllegalArgumentException.class, () -> loadBalancer.failServer("toto"));
        assertThrows(IllegalArgumentException.class, () -> loadBalancer.recoverServer("toto"));
    }
}
