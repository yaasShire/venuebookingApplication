package com.sporton.SportOn.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@RequiredArgsConstructor
public enum Role {

  //  USER(Collections.emptySet()),
    ADMIN(
            Set.of(
                    Permission.ADMIN_CREATE,
                    Permission.ADMIN_READ,
                    Permission.ADMIN_UPDATE,
                    Permission.ADMIN_DELETE,
                    Permission.USER_CREATE,
                    Permission.USER_READ,
                    Permission.USER_UPDATE,
                    Permission.USER_DELETE
            )
    ),
    PROVIDER(
            Set.of(
                    Permission.PROVIDER_CREATE,
                    Permission.PROVIDER_READ,
                    Permission.PROVIDER_UPDATE,
                    Permission.PROVIDER_DELETE
            )
    ),
    USER(
            Set.of(
                    Permission.USER_CREATE,
                    Permission.USER_READ,
                    Permission.USER_UPDATE,
                    Permission.USER_DELETE
            )
    )

    ;

    @Getter
    private final Set<Permission> permissions;

    public List<SimpleGrantedAuthority> getAuthorities() {
        var authorities = getPermissions()
                .stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return authorities;
    }
}
