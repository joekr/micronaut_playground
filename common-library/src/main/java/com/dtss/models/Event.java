package com.dtss.models;

import io.micronaut.core.annotation.Introspected;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@Introspected
public record Event(
    @NotBlank String id,
    @NotBlank String name,
    @NotNull LocalDateTime timestamp,
    @NotNull Boolean processed,
    String description,
    String revision
) {
        public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("_id", id);
        map.put("_rev", revision);
        map.put("name", name);
        map.put("timestamp", timestamp.toString());
        map.put("description", description);
        map.put("processed", processed);
        return map;
    }
}