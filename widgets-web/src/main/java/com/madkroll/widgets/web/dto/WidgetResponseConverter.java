package com.madkroll.widgets.web.dto;

import com.madkroll.widgets.repository.entity.Widget;
import org.springframework.stereotype.Service;

@Service
public class WidgetResponseConverter {

    public WidgetResponse convert(final Widget widget) {
        return new WidgetResponse(
                widget.getId(),
                widget.getX(),
                widget.getY(),
                widget.getZ(),
                widget.getWidth(),
                widget.getHeight(),
                widget.getLastUpdate()
        );
    }
}
