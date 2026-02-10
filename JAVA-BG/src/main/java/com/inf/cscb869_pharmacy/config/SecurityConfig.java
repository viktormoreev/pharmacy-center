package com.inf.cscb869_pharmacy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")  // Disable CSRF for API endpoints only
            )
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/", "/index", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/login/**", "/oauth2/**").permitAll()
                
                // Medicine endpoints - Anyone authenticated can view
                .requestMatchers("/api/medicines/**").authenticated()
                .requestMatchers("/medicines/**").authenticated()
                
                // Recipe endpoints - Doctors and Pharmacists
                .requestMatchers("/api/recipes/**").hasAnyRole("DOCTOR", "PHARMACIST", "ADMIN")
                .requestMatchers("/recipes/**").hasAnyRole("DOCTOR", "PHARMACIST", "ADMIN")
                
                // Doctor management - Admin and Pharmacists only
                .requestMatchers("/api/doctors/**").hasAnyRole("PHARMACIST", "ADMIN")
                .requestMatchers("/doctors/**").hasAnyRole("PHARMACIST", "ADMIN")
                
                // Customer management - Pharmacists and Admin
                .requestMatchers("/api/customers/**").hasAnyRole("PHARMACIST", "ADMIN")
                .requestMatchers("/customers/**").hasAnyRole("PHARMACIST", "ADMIN")
                
                // All other requests require authentication
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
            // Delegate to the default implementation for loading a user
            OidcUser oidcUser = delegate.loadUser(userRequest);

            Set<GrantedAuthority> authorities = new HashSet<>(oidcUser.getAuthorities());

            // Extract client ID from the request
            String clientId = userRequest.getClientRegistration().getClientId();
            
            System.out.println("=== DEBUGGING OIDC USER ===");
            System.out.println("User: " + oidcUser.getName());
            System.out.println("Client ID: " + clientId);
            
            // Try to get roles from ID token
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

    /**
     * Converter to extract roles from Keycloak JWT token
     */
    static class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            // Extract realm_access.roles from JWT
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