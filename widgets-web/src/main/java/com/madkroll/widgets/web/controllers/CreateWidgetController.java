package com.madkroll.widgets.web.controllers;

import com.madkroll.widgets.repository.WidgetRepository;
import com.madkroll.widgets.repository.entity.Widget;
import com.madkroll.widgets.web.dto.WidgetResponseConverter;
import com.madkroll.widgets.web.dto.WidgetRequestData;
import com.madkroll.widgets.web.dto.WidgetResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/widgets/create")
@Validated
@AllArgsConstructor
public class CreateWidgetController {

    private final WidgetResponseConverter responseConverter;
    private final WidgetRepository widgetRepository;

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<WidgetResponse> handleCreate(
            @RequestBody @Valid final WidgetRequestData widgetRequestData
    ) {
        return ResponseEntity.ok(
                responseConverter.convert(
                        widgetRepository.add(
                                new Widget(
                                        UUID.randomUUID().toString(),
                                        widgetRequestData.getX(),
                                        widgetRequestData.getY(),
                                        widgetRequestData.getZ(),
                                        widgetRequestData.getWidth(),
                                        widgetRequestData.getHeight()
                                )
                        )
                )
        );
    }
}
