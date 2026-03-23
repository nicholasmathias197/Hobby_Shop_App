package com.hobby.shop.security;

import com.hobby.shop.model.Customer;
import com.hobby.shop.model.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtUtilsAndUserDetailsImplTest {

    @Test
    void generatesParsesAndValidatesJwtTokens() {
        JwtUtils jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", "01234567890123456789012345678901");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 60_000);

        UserDetailsImpl principal = new UserDetailsImpl(
                1L,
                "jwt@example.com",
                "password",
                "Jwt",
                "User",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        String token = jwtUtils.generateJwtToken(authentication);

        assertThat(jwtUtils.getUserNameFromJwtToken(token)).isEqualTo("jwt@example.com");
        assertThat(jwtUtils.validateJwtToken(token)).isTrue();
        assertThat(jwtUtils.validateJwtToken(null)).isFalse();
        assertThat(jwtUtils.validateJwtToken("not-a-token")).isFalse();
    }

    @Test
    void rejectsExpiredAndWronglySignedTokens() {
        JwtUtils expiredJwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(expiredJwtUtils, "jwtSecret", "01234567890123456789012345678901");
        ReflectionTestUtils.setField(expiredJwtUtils, "jwtExpirationMs", -1);

        UserDetailsImpl principal = new UserDetailsImpl(
                1L,
                "expired@example.com",
                "password",
                "Expired",
                "User",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        String expiredToken = expiredJwtUtils.generateJwtToken(authentication);

        JwtUtils validator = new JwtUtils();
        ReflectionTestUtils.setField(validator, "jwtSecret", "01234567890123456789012345678901");
        ReflectionTestUtils.setField(validator, "jwtExpirationMs", 60_000);

        String wrongSignatureToken = Jwts.builder()
                .subject("other@example.com")
                .signWith(Keys.hmacShaKeyFor("abcdefghijklmnopqrstuvwxyz123456".getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertThat(validator.validateJwtToken(expiredToken)).isFalse();
        assertThat(validator.validateJwtToken(wrongSignatureToken)).isFalse();
    }

    @Test
    void userDetailsImplBuilderAndFlagsReflectCustomerState() {
        Role role = new Role();
        role.setName("ADMIN");

        Customer customer = new Customer();
        customer.setId(7L);
        customer.setEmail("builder@example.com");
        customer.setPassword("encoded");
        customer.setFirstName("Build");
        customer.setLastName("Er");
        customer.setRoles(Set.of(role));

        UserDetailsImpl userDetails = UserDetailsImpl.builder(customer);

        assertThat(userDetails.getId()).isEqualTo(7L);
        assertThat(userDetails.getUsername()).isEqualTo("builder@example.com");
        assertThat(userDetails.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
        assertThat(userDetails.isEnabled()).isTrue();
    }
}