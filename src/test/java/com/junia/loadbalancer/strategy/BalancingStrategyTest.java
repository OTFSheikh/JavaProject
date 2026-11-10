package com.junia.loadbalancer.strategy;

import com.junia.loadbalancer.model.HttpRequest;
import com.junia.loadbalancer.model.Request;
import com.junia.loadbalancer.model.Server;
import com.junia.loadbalancer.model.ServerStatus;
import com.junia.loadbalancer.model.VideoStreamRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BalancingStrategyTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 15, 9, 0);
    private Server big;
    private Server small;
    private List<Server> servers;

    @BeforeEach
    void setUp() {
        big = new Server("big", 30, 3, ServerStatus.UP);
        small = new Server("small", 10, 1, ServerStatus.UP);
        servers = List.of(big, small);
    }

    @Test
    void roundRobinShouldAlternateBetweenServers() {
        BalancingStrategy strategy = new RoundRobinStrategy();
        Request request = new HttpRequest("R1", NOW);

        assertEquals(big, strategy.chooseServer(servers, request).get());
        assertEquals(small, strategy.chooseServer(servers, request).get());
        assertEquals(big, strategy.chooseServer(servers, request).get());
    }

    @Test
    void roundRobinShouldSkipServerThatIsDown() {
        Server down = new Server("down", 10, 1, ServerStatus.DOWN);
        BalancingStrategy strategy = new RoundRobinStrategy();

        Optional<Server> chosen = strategy.chooseServer(List.of(down, small), new HttpRequest("R1", NOW));

        assertEquals(small, chosen.get());
    }

    @Test
    void leastLoadedShouldChooseServerWithLowestPercentage() {
        big.accept(new VideoStreamRequest("R1", NOW, 150));
        small.accept(new HttpRequest("R2", NOW));

        Optional<Server> chosen = new LeastLoadedStrategy().chooseServer(servers, new HttpRequest("R3", NOW));

        assertEquals(small, chosen.get());
    }

    @Test
    void weightedShouldPreferServerWithBiggerWeight() {
        big.accept(new HttpRequest("R1", NOW));
        big.accept(new HttpRequest("R2", NOW));
        small.accept(new HttpRequest("R3", NOW));

        // big : 2 / 3 = 0.67, small : 1 / 1 = 1
        Optional<Server> chosen = new WeightedStrategy().chooseServer(servers, new HttpRequest("R4", NOW));

        assertEquals(big, chosen.get());
    }

    @Test
    void strategiesShouldReturnEmptyWhenNoServerCanAccept() {
        Request huge = new VideoStreamRequest("R1", NOW, 600);

        assertTrue(new RoundRobinStrategy().chooseServer(servers, huge).isEmpty());
        assertTrue(new LeastLoadedStrategy().chooseServer(servers, huge).isEmpty());
        assertTrue(new WeightedStrategy().chooseServer(servers, huge).isEmpty());
    }

    @Test
    void strategiesShouldReturnEmptyWhenServerListIsEmpty() {
        Request request = new HttpRequest("R1", NOW);

        assertTrue(new RoundRobinStrategy().chooseServer(List.of(), request).isEmpty());
        assertTrue(new LeastLoadedStrategy().chooseServer(List.of(), request).isEmpty());
    }

    @Test
    void factoryShouldThrowExceptionForUnknownChoice() {
        assertInstanceOf(WeightedStrategy.class, StrategyFactory.fromChoice(3));
        assertThrows(IllegalArgumentException.class, () -> StrategyFactory.fromChoice(9));
    }
}
