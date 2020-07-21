package com.madkroll.widgets.repository.inmemory;

import com.madkroll.widgets.repository.entity.Widget;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;

@RunWith(MockitoJUnitRunner.class)
public class SingleMapWidgetRepositoryTest {

    private static final String WIDGET_ID = "widget-id";
    private static final int FREE_Z = 10;
    private static final Widget SAMPLE_WIDGET = new Widget(
            WIDGET_ID, 1, 2, 3, 4, 5
    );

    private static final Widget WIDGET_ON_3 = new Widget(
            WIDGET_ID + "-3", 1, 2, 3, 4, 5
    );

    private static final Widget WIDGET_ON_4 = new Widget(
            WIDGET_ID + "-4", 1, 2, 4, 4, 5
    );

    private static final Widget WIDGET_ON_6 = new Widget(
            WIDGET_ID + "-6", 1, 2, 6, 4, 5
    );

    @Test
    public void shouldInsertWidget() {
        final ConcurrentHashMap<String, Widget> widgets = new ConcurrentHashMap<>();
        final ReadWriteLock lock = new ReentrantReadWriteLock();
        final Supplier<ReadWriteLock> lockSupplier = () -> lock;
        final Widget addedWidget = new SingleMapWidgetRepository(widgets, lockSupplier).add(SAMPLE_WIDGET);

        assertThat(addedWidget).isEqualTo(SAMPLE_WIDGET);
        assertThat(widgets).containsOnly(entry(WIDGET_ID, addedWidget));
    }

    @Test
    public void shouldFailOnReinsertingWidget() {
        final ConcurrentHashMap<String, Widget> widgets = new ConcurrentHashMap<>();
        final ReadWriteLock lock = new ReentrantReadWriteLock();
        final Supplier<ReadWriteLock> lockSupplier = () -> lock;
        widgets.put(SAMPLE_WIDGET.getId(), SAMPLE_WIDGET);

        assertThatThrownBy(
                () -> new SingleMapWidgetRepository(widgets, lockSupplier).add(SAMPLE_WIDGET)
        ).isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("Such widget already exists");
    }

    @Test
    public void shouldShiftElementsWhenInsertingOnTakenZ() throws InterruptedException {
        final ConcurrentHashMap<String, Widget> widgets = new ConcurrentHashMap<>();
        final ReadWriteLock lock = new ReentrantReadWriteLock();
        final Supplier<ReadWriteLock> lockSupplier = () -> lock;
        widgets.put(WIDGET_ON_3.getId(), WIDGET_ON_3);
        widgets.put(WIDGET_ON_4.getId(), WIDGET_ON_4);
        widgets.put(WIDGET_ON_6.getId(), WIDGET_ON_6);

        new SingleMapWidgetRepository(widgets, lockSupplier).add(SAMPLE_WIDGET);

        Thread.sleep(10); // to have enough time to catch a read lock
        lock.readLock().lock();
        try {
            assertThat(widgets.get(SAMPLE_WIDGET.getId()).getZ()).isEqualTo(3);
            assertThat(widgets.get(WIDGET_ON_3.getId()).getZ()).isEqualTo(4);
            assertThat(widgets.get(WIDGET_ON_4.getId()).getZ()).isEqualTo(5);
            assertThat(widgets.get(WIDGET_ON_6.getId()).getZ()).isEqualTo(7);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Test
    public void shouldNotShiftElementsWhenInsertingOnFreeZIndex() throws InterruptedException {
        final ConcurrentHashMap<String, Widget> widgets = new ConcurrentHashMap<>();
        final ReadWriteLock lock = new ReentrantReadWriteLock();
        final Supplier<ReadWriteLock> lockSupplier = () -> lock;
        widgets.put(WIDGET_ON_4.getId(), WIDGET_ON_4);
        widgets.put(WIDGET_ON_6.getId(), WIDGET_ON_6);

        new SingleMapWidgetRepository(widgets, lockSupplier).add(SAMPLE_WIDGET);

        Thread.sleep(10); // to have enough time to catch a read lock
        lock.readLock().lock();
        try {
            assertThat(widgets.get(SAMPLE_WIDGET.getId()).getZ()).isEqualTo(3);
            assertThat(widgets.get(WIDGET_ON_4.getId()).getZ()).isEqualTo(4);
            assertThat(widgets.get(WIDGET_ON_6.getId()).getZ()).isEqualTo(6);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Test
    public void shouldNotFindWidget() {
        final ConcurrentHashMap<String, Widget> widgets = new ConcurrentHashMap<>();
        final ReadWriteLock lock = new ReentrantReadWriteLock();
        final Supplier<ReadWriteLock> lockSupplier = () -> lock;
        widgets.put(WIDGET_ON_3.getId(), WIDGET_ON_3);

        assertThat(
                new SingleMapWidgetRepository(widgets, lockSupplier)
                        .find(WIDGET_ID)
        ).isEmpty();
    }

    @Test
    public void shouldFindWidget() {
        final ConcurrentHashMap<String, Widget> widgets = new ConcurrentHashMap<>();
        final ReadWriteLock lock = new ReentrantReadWriteLock();
        final Supplier<ReadWriteLock> lockSupplier = () -> lock;
        widgets.put(SAMPLE_WIDGET.getId(), SAMPLE_WIDGET);

        assertThat(
                new SingleMapWidgetRepository(widgets, lockSupplier)
                        .find(WIDGET_ID)
        ).hasValue(SAMPLE_WIDGET);
    }

    @Test
    public void shouldDeleteWidget() {
        final ConcurrentHashMap<String, Widget> widgets = new ConcurrentHashMap<>();
        final ReadWriteLock lock = new ReentrantReadWriteLock();
        final Supplier<ReadWriteLock> lockSupplier = () -> lock;
        widgets.put(SAMPLE_WIDGET.getId(), SAMPLE_WIDGET);

        assertThat(
                new SingleMapWidgetRepository(widgets, lockSupplier)
                        .delete(WIDGET_ID)
        ).hasValue(SAMPLE_WIDGET);
    }

    @Test
    public void shouldNotDeleteWidget() {
        final ConcurrentHashMap<String, Widget> widgets = new ConcurrentHashMap<>();
        final ReadWriteLock lock = new ReentrantReadWriteLock();
        final Supplier<ReadWriteLock> lockSupplier = () -> lock;
        widgets.put(WIDGET_ON_3.getId(), WIDGET_ON_3);

        assertThat(
                new SingleMapWidgetRepository(widgets, lockSupplier)
                        .delete(WIDGET_ID)
        ).isEmpty();
    }

    @Test
    public void shouldShiftElementsWhenMovingOnTakenZ() throws InterruptedException {
        final ConcurrentHashMap<String, Widget> widgets = new ConcurrentHashMap<>();
        final ReadWriteLock lock = new ReentrantReadWriteLock();
        final Supplier<ReadWriteLock> lockSupplier = () -> lock;
        widgets.put(SAMPLE_WIDGET.getId(), SAMPLE_WIDGET);
        widgets.put(WIDGET_ON_3.getId(), WIDGET_ON_3);
        widgets.put(WIDGET_ON_4.getId(), WIDGET_ON_4);
        widgets.put(WIDGET_ON_6.getId(), WIDGET_ON_6);

        final Widget replaceWidgetOn3 = new Widget(
                SAMPLE_WIDGET.getId(),
                SAMPLE_WIDGET.getX(),
                SAMPLE_WIDGET.getY(),
                WIDGET_ON_3.getZ(),
                SAMPLE_WIDGET.getWidth(),
                SAMPLE_WIDGET.getHeight()
        );

        new SingleMapWidgetRepository(widgets, lockSupplier).update(replaceWidgetOn3);

        Thread.sleep(10); // to have enough time to catch a read lock
        lock.readLock().lock();
        try {
            assertThat(widgets.get(SAMPLE_WIDGET.getId()).getZ()).isEqualTo(3);
            assertThat(widgets.get(WIDGET_ON_3.getId()).getZ()).isEqualTo(4);
            assertThat(widgets.get(WIDGET_ON_4.getId()).getZ()).isEqualTo(5);
            assertThat(widgets.get(WIDGET_ON_6.getId()).getZ()).isEqualTo(7);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Test
    public void shouldNotShiftElementsWhenMovingOnFreeZIndex() throws InterruptedException {
        final ConcurrentHashMap<String, Widget> widgets = new ConcurrentHashMap<>();
        final ReadWriteLock lock = new ReentrantReadWriteLock();
        final Supplier<ReadWriteLock> lockSupplier = () -> lock;
        widgets.put(SAMPLE_WIDGET.getId(), SAMPLE_WIDGET);
        widgets.put(WIDGET_ON_4.getId(), WIDGET_ON_4);
        widgets.put(WIDGET_ON_6.getId(), WIDGET_ON_6);

        final Widget moveOnFreeZ = new Widget(
                SAMPLE_WIDGET.getId(),
                SAMPLE_WIDGET.getX(),
                SAMPLE_WIDGET.getY(),
                FREE_Z,
                SAMPLE_WIDGET.getWidth(),
                SAMPLE_WIDGET.getHeight()
        );
        new SingleMapWidgetRepository(widgets, lockSupplier).update(moveOnFreeZ);

        Thread.sleep(10); // to have enough time to catch a read lock
        lock.readLock().lock();
        try {
            assertThat(widgets.get(WIDGET_ON_4.getId()).getZ()).isEqualTo(4);
            assertThat(widgets.get(WIDGET_ON_6.getId()).getZ()).isEqualTo(6);
            assertThat(widgets.get(SAMPLE_WIDGET.getId()).getZ()).isEqualTo(10);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Test
    public void shouldListAllWidgetsOrderedByZ() {
        final ConcurrentHashMap<String, Widget> widgets = new ConcurrentHashMap<>();
        final ReadWriteLock lock = new ReentrantReadWriteLock();
        final Supplier<ReadWriteLock> lockSupplier = () -> lock;
        widgets.put(WIDGET_ON_4.getId(), WIDGET_ON_4);
        widgets.put(WIDGET_ON_3.getId(), WIDGET_ON_3);
        widgets.put(WIDGET_ON_6.getId(), WIDGET_ON_6);

        assertThat(
                new SingleMapWidgetRepository(widgets, lockSupplier).listAllOrderedByZIndex()
        )
                .containsExactly(
                        WIDGET_ON_3,
                        WIDGET_ON_4,
                        WIDGET_ON_6
                );
    }

    @Test
    public void shouldReturnEmptyListIfNoWidgets() {
        final ConcurrentHashMap<String, Widget> widgets = new ConcurrentHashMap<>();
        final ReadWriteLock lock = new ReentrantReadWriteLock();
        final Supplier<ReadWriteLock> lockSupplier = () -> lock;

        assertThat(
                new SingleMapWidgetRepository(widgets, lockSupplier).listAllOrderedByZIndex()
        ).isEmpty();
    }
}