package com.backend.athlete.support.jwt.service;

import com.backend.athlete.domain.user.User;
import com.backend.athlete.domain.user.type.UserStatusType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CustomUserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    @Getter
    private Long id;
    private String code;
    private String userId;
    private String name;
    @JsonIgnore
    private String password;
    private UserStatusType status;

    private Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetailsImpl(Long id, String code, String userId, String name, String password, UserStatusType status,
                                 Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.code = code;
        this.userId = userId;
        this.name = name;
        this.password = password;
        this.status = status;
        this.authorities = authorities;
    }

    public static CustomUserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());

        return new CustomUserDetailsImpl(
                user.getId(),
                user.getCode(),
                user.getUserId(),
                user.getName(),
                user.getPassword(),
                user.getStatus(),  // 추가된 status 필드 설정
                authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public String getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }

    public UserStatusType getStatus() {
        return this.status;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return userId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CustomUserDetailsImpl user = (CustomUserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }
}
