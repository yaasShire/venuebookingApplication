package com.sporton.SportOn.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Permission {

    ADMIN_READ("admin:read"),
    ADMIN_UPDATE("admin:update"),
    ADMIN_CREATE("admin:create"),
    ADMIN_DELETE("admin:delete"),
    USER_READ("user:read"),
    USER_UPDATE("user:update"),
    USER_CREATE("user:create"),
    USER_DELETE("user:delete"),
    PROVIDER_READ("provider:read"),
    PROVIDER_UPDATE("provider:update"),
    PROVIDER_CREATE("provider:create"),
    PROVIDER_DELETE("provider:delete")
    ;

    @Getter
    private final String permission;
}
