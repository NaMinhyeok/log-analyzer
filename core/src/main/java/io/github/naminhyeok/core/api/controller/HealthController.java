package io.github.naminhyeok.core.api.controller;

import io.github.naminhyeok.core.api.controller.docs.HealthControllerDocs;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController implements HealthControllerDocs {

    @Override
    @GetMapping("/health")
    public Object health() {
        return null;
    }

}
