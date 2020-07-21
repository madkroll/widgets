package com.madkroll.widgets.repository.inmemory;

import com.madkroll.widgets.repository.entity.Widget;
import lombok.extern.log4j.Log4j2;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static org.junit.Assert.assertNotNull;

@Log4j2
@RunWith(MockitoJUnitRunner.class)
public class SingleMapWidgetRepositoryConcurrencyIT {

    private static final Duration TEST_DURATION = Duration.ofMinutes(1);

    private static final String WIDGET_ID = "widget-id";
    private static final Widget SAMPLE_WIDGET = new Widget(
            WIDGET_ID, 1, 2, 3, 4, 5
    );

    /**
     * Purpose:
     * This integration test verifies repository is not stuck in dead-lock once many different operations performed.
     * <p>
     * Test idea:
     * - generate parallel load across all methods provided by repository
     * - majority of operations are read-only (GET, LIST)
     * <p>
     * Test is based on Project Reactor providing API and helps to simulate different patters for incoming parallel requests.
     * <p>
     * Note:
     * This test is not a benchmark test. For benchmarking use JMH library.
     */
    @Test
    public void shouldNotLeadToDeadLockWithMajorityOfReadOperations() {
        log.info("Running 1 minute integration test to indicate there are no dead-locks");

        final ConcurrentHashMap<String, Widget> widgets = new ConcurrentHashMap<>();
        widgets.put(SAMPLE_WIDGET.getId(), SAMPLE_WIDGET);

        final ReadWriteLock lock = new ReentrantReadWriteLock();
        final Supplier<ReadWriteLock> lockSupplier = () -> lock;
        final SingleMapWidgetRepository repository = new SingleMapWidgetRepository(widgets, lockSupplier);

        // preload repository with existent widgets
        IntStream.range(0, 5000)
                .forEach(
                        value -> repository.add(
                                new Widget(UUID.randomUUID().toString(), 1, 2, nextZSlot(), 4, 5)
                        )
                );

        // trigger call generator for create operation
        streamRequests(
                Duration.ofMillis(200),
                next -> assertNotNull(
                        repository.add(
                                new Widget(UUID.randomUUID().toString(), 1, 2, nextZSlot(), 4, 5)
                        )
                )

        ).subscribe();

        // trigger call generator for update operation
        streamRequests(
                Duration.ofMillis(100),
                next -> assertNotNull(
                        repository.update(
                                new Widget(WIDGET_ID, 1, 2, nextZSlot(), 4, 5)
                        ).get()
                )
        ).subscribe();

        // trigger call generator for delete operation
        streamRequests(
                Duration.ofMillis(300),
                next -> assertNotNull(
                        repository.delete(
                                widgets.values()
                                        .stream()
                                        .filter(widget -> !widget.getId().equals(WIDGET_ID))
                                        .findAny()
                                        .map(Widget::getId)
                                        .get()
                        ).get()
                )
        ).subscribe();

        // trigger call generator for get operation
        streamRequests(
                Duration.ofMillis(10),
                next -> assertNotNull(repository.find(WIDGET_ID))
        ).subscribe();

        // trigger call generator for list operation
        streamRequests(
                Duration.ofMillis(30),
                next -> assertNotNull(repository.listAllOrderedByZIndex())
        )
                // and wait until test is complete
                .blockLast();
    }

    /**
     * This method provides publisher executing given operation on specified time interval
     */
    private Flux<Long> streamRequests(
            final Duration executionFrequency,
            final Consumer<Long> operationOnRepository
    ) {
        return Flux.interval(executionFrequency)
                // make sure - even if calls are busy, publisher continues feeding new ones from other threads
                .publishOn(Schedulers.elastic())
                .doOnNext(operationOnRepository)
                // fail if any call takes longer than 5 seconds
                .timeout(Duration.ofSeconds(5))
                // release once test is ended
                .take(TEST_DURATION);
    }

    private int nextZSlot() {
        return new Random().nextInt(100);
    }
}