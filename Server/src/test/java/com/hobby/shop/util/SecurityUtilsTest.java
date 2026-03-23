package com.hobby.shop.util;

import com.hobby.shop.security.UserDetailsImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityUtilsTest {

    private final SecurityUtils securityUtils = new SecurityUtils();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsCurrentUserEmailAndIdWhenAuthenticated() {
        UserDetailsImpl principal = new UserDetailsImpl(
                99L,
                "secure@example.com",
                "password",
                "Secure",
                "User",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        assertThat(securityUtils.getCurrentUserEmail()).isEqualTo("secure@example.com");
        assertThat(securityUtils.getCurrentUserId()).isEqualTo(99L);
        assertThat(securityUtils.isAuthenticated()).isTrue();
        assertThat(securityUtils.hasRole("ADMIN")).isTrue();
        assertThat(securityUtils.hasRole("USER")).isFalse();
    }

    @Test
    void treatsAnonymousUserAsNotAuthenticated() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anonymousUser", null, List.of())
        );

        assertThat(securityUtils.isAuthenticated()).isFalse();
    }

    @Test
    void throwsWhenNoAuthenticatedUserExists() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("anonymousUser", null, List.of())
        );

        assertThatThrownBy(securityUtils::getCurrentUserEmail)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No authenticated user found");

        assertThatThrownBy(securityUtils::getCurrentUserId)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No authenticated user found");
    }
}