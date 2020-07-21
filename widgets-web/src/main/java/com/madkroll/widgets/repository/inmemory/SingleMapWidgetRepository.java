package com.madkroll.widgets.repository.inmemory;

import com.madkroll.widgets.repository.WidgetRepository;
import com.madkroll.widgets.repository.entity.Widget;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Thread-safe repository storing widgets and providing operations on them.
 * */
@Repository
public class SingleMapWidgetRepository implements WidgetRepository {

    /**
     * This lock is used to synchronize access to widgets while shifting is in progress
     */
    private final ReadWriteLock shiftingLock;
    private final ConcurrentHashMap<String, Widget> widgets;

    public SingleMapWidgetRepository(
            final ConcurrentHashMap<String, Widget> widgets,
            final Supplier<ReadWriteLock> lockSupplier
    ) {
        this.widgets = widgets;
        this.shiftingLock = lockSupplier.get();
    }

    /**
     * Stores a new widget.
     * In case new widget is placed into Z-index occupied already by another widget:
     * triggers shifting asynchronously upwards all elements starting from this Z-index and higher.
     *
     * @return stored widget
     * @throws IllegalStateException if widget with such ID is present already.
     * */
    @Override
    public Widget add(final Widget widget) {
        final Widget alreadyPresent = widgets.putIfAbsent(widget.getId(), widget);

        if (Objects.nonNull(alreadyPresent)) {
            throw new IllegalStateException("Such widget already exists");
        }

        // running on ForkJoinPool.commonPool(), does not block calling thread
        asyncShiftUpwardIfZIndexIsTaken(widget.getZ(), widget.getId());

        return widget;
    }

    /**
     * Finds widget by id.
     *
     * @return Optional containing widget found by id, otherwise Optional.empty()
     * */
    @Override
    public Optional<Widget> find(final String id) {
        return Optional.ofNullable(widgets.get(id));
    }

    /**
     * Updates existent widget.
     * In case new widget is placed into Z-index occupied already by another widget:
     * triggers shifting asynchronously upwards all elements starting from this Z-index and higher.
     *
     * @return Optional containing updated widget. Otherwise Optional.empty()
     * */
    @Override
    public Optional<Widget> update(final Widget widget) {
        return Optional.ofNullable(
                widgets.computeIfPresent(
                        widget.getId(),
                        (key, oldValue) -> {
                            // running on ForkJoinPool.commonPool(), does not block caller thread
                            asyncShiftUpwardIfZIndexIsTaken(widget.getZ(), oldValue.getId());

                            return widget;
                        }
                )
        );
    }

    /**
     * Deletes widget by id.
     *
     * @return removed widget. If widget is not present returns Optional.empty()
     * */
    @Override
    public Optional<Widget> delete(final String id) {
        return Optional.ofNullable(widgets.remove(id));
    }

    /**
     * Streams all widgets ordered by Z-index.
     *
     * @return stream of widgets ordered by Z-index.
     * */
    @Override
    public Stream<Widget> listAllOrderedByZIndex() {
        final Lock listingInProgress = shiftingLock.readLock();

        listingInProgress.lock();
        try {
            return List.copyOf(widgets.values())
                    .stream()
                    .sorted(Comparator.comparing(Widget::getZ));
        } finally {
            listingInProgress.unlock();
        }
    }

    /**
     * If given Z-index is occupied by any widget - shifts upwards all widgets starting from the this Z-index and higher.
     * The whole execution is done asynchronously on ForkJoinPool.commonPool().
     *
     * To enforce consistency for parallel read and write operations - locks access for them.
     * */
    private void asyncShiftUpwardIfZIndexIsTaken(final long zIndex, final String except) {
        CompletableFuture
                .runAsync(
                        () -> {
                            final Lock shiftingInProgress = shiftingLock.writeLock();
                            shiftingInProgress.lock();

                            try {
                                final boolean zIndexIsAlreadyTaken = widgets.values()
                                        .stream()
                                        .filter(widget -> !widget.getId().equals(except))
                                        .anyMatch(widget -> widget.getZ() == zIndex);

                                if (zIndexIsAlreadyTaken) {
                                    widgets.values()
                                            .parallelStream()
                                            .filter(widget -> !widget.getId().equals(except))
                                            .filter(widget -> widget.getZ() >= zIndex)
                                            .forEach(
                                                    widget -> widgets.computeIfPresent(
                                                            widget.getId(),
                                                            (key, oldValue) -> shiftedUpwardWidget(oldValue)
                                                    )
                                            );
                                }
                            } finally {
                                shiftingInProgress.unlock();
                            }
                        }
                );
    }

    /**
     * Produces new immutable copy of existent widget shifted upward on Z-index coordinate.
     * */
    private Widget shiftedUpwardWidget(final Widget originalWidget) {
        return new Widget(
                originalWidget.getId(),
                originalWidget.getX(),
                originalWidget.getY(),
                originalWidget.getZ() + 1,
                originalWidget.getWidth(),
                originalWidget.getHeight()
        );
    }
}
