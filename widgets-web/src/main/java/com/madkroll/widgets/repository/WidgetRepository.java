package com.madkroll.widgets.repository;

import com.madkroll.widgets.repository.entity.Widget;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Repository storing widgets and providing operations on them.
 * */
public interface WidgetRepository {

    /**
     * Stores a new widget.
     * In case new widget is placed into Z-index occupied already by another widget:
     * shifts upwards all elements starting from this Z-index and higher.
     *
     * @return stored widget
     * */
    Widget add(Widget widget);

    /**
     * Finds widget by id.
     *
     * @return Optional containing widget found by id, otherwise Optional.empty()
     * */
    Optional<Widget> find(String id);

    /**
     * Updates existent widget.
     * In case new widget is placed into Z-index occupied already by another widget:
     * shifts upwards all elements starting from this Z-index and higher.
     *
     * @return Optional containing updated widget. Otherwise Optional.empty()
     * */
    Optional<Widget> update(Widget widget);

    /**
     * Deletes widget by id.
     *
     * @return removed widget. If widget is not present returns Optional.empty()
     * */
    Optional<Widget> delete(String id);

    /**
     * Streams all widgets ordered by Z-index.
     *
     * @return stream of widgets ordered by Z-index.
     * */
    Stream<Widget> listAllOrderedByZIndex();
}
