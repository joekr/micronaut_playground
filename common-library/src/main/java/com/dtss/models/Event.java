package com.dtss.record;

import io.micronaut.core.annotation.Introspected;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable.Deserializable
@Introspected
public record Event(
    @NotBlank String id,
    @NotBlank String name,
    @NotNull LocalDateTime timestamp,
    String description
) {
        public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("_id", id);
        map.put("name", name);
        map.put("timestamp", timestamp.toString());
        map.put("description", description);
        return map;
    }
}