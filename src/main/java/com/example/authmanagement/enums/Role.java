package com.example.authmanagement.enums;

import lombok.Getter;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Getter
public enum Role {
    SUPER_ADMIN(new EnumMap<>(Operation.class) {{
        put(Operation.DELETE, List.of(Resource.USER));
    }}),
    ADMIN(new EnumMap<>(Operation.class) {{
        put(Operation.ADD, List.of(Resource.CATEGORY, Resource.PRODUCT));
        put(Operation.MODIFY, List.of(Resource.CATEGORY, Resource.PRODUCT));
        put(Operation.DELETE, List.of(Resource.CATEGORY, Resource.PRODUCT));
    }}),
    USER(new EnumMap<>(Operation.class) {{
        put(Operation.GET, List.of(Resource.PRODUCT, Resource.CATEGORY));
    }});

    private final Map<Operation, List<Resource>> permissions;

    Role(final Map<Operation, List<Resource>> permissions) {
        this.permissions = permissions;
    }

    public boolean hasAccessTo(Operation operation, Resource resource) {
        return permissions.getOrDefault(operation, Collections.emptyList()).stream()
                .anyMatch(resource1 -> resource1.equals(resource));
    }
}