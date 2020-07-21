package com.madkroll.widgets.repository;

import com.madkroll.widgets.repository.entity.Widget;

import java.util.Optional;
import java.util.stream.Stream;

public interface WidgetRepository {

    Widget add(Widget widget);
    Optional<Widget> find(String id);
    Optional<Widget> update(Widget widget);
    Optional<Widget> delete(String id);
    Stream<Widget> listAllOrderedByZIndex();
}
