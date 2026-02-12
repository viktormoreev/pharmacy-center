package com.inf.cscb869_pharmacy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.*;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    private static final String[] STAFF_ROLES = {"DOCTOR", "ADMIN"};

    private static final String[] PUBLIC_PATHS = {
            "/", "/index", "/css/**", "/js/**", "/images/**", "/login/**", "/oauth2/**"
    };

    private static final String[] ADMIN_ONLY_PATHS = {
            "/medicines/create-medicine",
            "/medicines/create",
            "/medicines/edit-medicine/**",
            "/medicines/update/**",
            "/medicines/delete/**",
            "/doctors/create",
            "/doctors/edit/**",
            "/doctors/delete/**",
            "/customers/create",
            "/customers/edit/**",
            "/customers/delete/**",
            "/api/doctors/**",
            "/api/customers/**"
    };

    private static final String[] STAFF_FEATURE_PATHS = {
            "/medicines/**",
            "/api/recipes/**",
            "/recipes/**",
            "/diagnoses/**",
            "/api/diagnoses/**",
            "/sick-leaves/**",
            "/api/sick-leaves/**",
            "/doctors/**",
            "/customers/**",
            "/dashboard/**",
            "/reports/**",
            "/api/reports/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**")
                )
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .requestMatchers("/my/**").hasRole("CUSTOMER")
                        .requestMatchers(ADMIN_ONLY_PATHS).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/medicines/**").hasAnyRole(STAFF_ROLES)
                        .requestMatchers("/api/medicines/**").hasRole("ADMIN")
                        .requestMatchers(STAFF_FEATURE_PATHS).hasAnyRole(STAFF_ROLES)
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(this.oidcUserService())
                        )
                        .defaultSuccessUrl("/", true)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .oidcLogout(oidcLogout -> oidcLogout
                        .backChannel(Customizer.withDefaults())
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
        return converter;
    }

    @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        final OidcUserService delegate = new OidcUserService();

        return (userRequest) -> {
            OidcUser oidcUser = delegate.loadUser(userRequest);

            Set<GrantedAuthority> authorities = new HashSet<>(oidcUser.getAuthorities());

            String clientId = userRequest.getClientRegistration().getClientId();

            System.out.println("=== DEBUGGING OIDC USER ===");
            System.out.println("User: " + oidcUser.getName());
            System.out.println("Client ID: " + clientId);

            Map<String, Object> idTokenClaims = oidcUser.getIdToken().getClaims();
            System.out.println("ID Token Claims: " + idTokenClaims.keySet());
            System.out.println("Full Claims: " + idTokenClaims);

            // 1. Try realm roles first (realm_access.roles)
            Object realmAccessObj = idTokenClaims.get("realm_access");
            if (realmAccessObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> realmAccess = (Map<String, Object>) realmAccessObj;
                if (realmAccess.containsKey("roles")) {
                    @SuppressWarnings("unchecked")
                    List<String> roles = (List<String>) realmAccess.get("roles");
                    System.out.println("Found realm roles: " + roles);
                    roles.stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .forEach(authorities::add);
                }
            }

            // 2. Try client roles (resource_access.{client-id}.roles)
            Object resourceAccessObj = idTokenClaims.get("resource_access");
            if (resourceAccessObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> resourceAccess = (Map<String, Object>) resourceAccessObj;

                // Get roles for our specific client
                Object clientAccessObj = resourceAccess.get(clientId);
                if (clientAccessObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> clientAccess = (Map<String, Object>) clientAccessObj;
                    if (clientAccess.containsKey("roles")) {
                        @SuppressWarnings("unchecked")
                        List<String> clientRoles = (List<String>) clientAccess.get("roles");
                        System.out.println("Found client roles for " + clientId + ": " + clientRoles);
                        clientRoles.stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                .forEach(authorities::add);
                    }
                }
            }

            System.out.println("Final authorities: " + authorities);

            // Create a new OidcUser with the combined authorities
            return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
        };
    }

    static class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");

            if (realmAccess == null || !realmAccess.containsKey("roles")) {
                return List.of();
            }

            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) realmAccess.get("roles");

            return roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
        }
    }
}
