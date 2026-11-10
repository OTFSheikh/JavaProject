package com.junia.loadbalancer.service;

import com.junia.loadbalancer.model.FileUploadRequest;
import com.junia.loadbalancer.model.HttpRequest;
import com.junia.loadbalancer.model.RequestType;
import com.junia.loadbalancer.model.Server;
import com.junia.loadbalancer.model.ServerStatus;
import com.junia.loadbalancer.model.VideoStreamRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StatisticsServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 15, 9, 0);
    private StatisticsService statistics;
    private Server web1;
    private Server web2;

    @BeforeEach
    void setUp() {
        statistics = new StatisticsService();
        web1 = new Server("web-01", 10, 1, ServerStatus.UP);
        web2 = new Server("web-02", 10, 1, ServerStatus.UP);
    }

    @Test
    void shouldComputeAverageOnlyOnServersThatAreUp() {
        web1.accept(new FileUploadRequest("R1", NOW, 60));
        Server down = new Server("down", 10, 1, ServerStatus.DOWN);

        // web1 = 80 %, web2 = 0 %, le serveur DOWN est ignore
        assertEquals(40.0, statistics.averageLoadPercentage(List.of(web1, web2, down)), 0.001);
    }

    @Test
    void shouldFindMostLoadedServer() {
        web2.accept(new HttpRequest("R1", NOW));

        assertEquals(web2, statistics.mostLoadedServer(List.of(web1, web2)).get());
    }

    @Test
    void shouldDetectUnbalancedServers() {
        web1.accept(new VideoStreamRequest("R1", NOW, 30));

        // 70 % contre 0 % : ecart trop grand
        assertFalse(statistics.isBalanced(List.of(web1, web2)));
    }

    @Test
    void shouldBeBalancedWhenGapIsExactlyTheThreshold() {
        web1.accept(new FileUploadRequest("R1", NOW, 1));

        // 20 % contre 0 % : pile la limite
        assertTrue(statistics.isBalanced(List.of(web1, web2)));
    }

    @Test
    void shouldReturnZeroAndEmptyWhenNoServer() {
        assertEquals(0.0, statistics.averageLoadPercentage(List.of()), 0.001);
        assertTrue(statistics.mostLoadedServer(List.of()).isEmpty());
        assertTrue(statistics.isBalanced(List.of()));
    }

    @Test
    void shouldCountRequestsByType() {
        web1.accept(new HttpRequest("R1", NOW));
        web1.accept(new HttpRequest("R2", NOW));
        web2.accept(new FileUploadRequest("R3", NOW, 10));

        Map<RequestType, Long> counts = statistics.countRequestsByType(List.of(web1, web2));

        assertEquals(2L, counts.get(RequestType.HTTP));
        assertEquals(1L, counts.get(RequestType.UPLOAD));
        assertNull(counts.get(RequestType.STREAM));
    }
}
