# 🔐 Keycloak Authentication Setup Guide
**Date:** January 14, 2026  
**Project:** CSCB869 Pharmacy System with Keycloak

---

## 📋 Overview

This guide will help you set up Keycloak as an external authentication system with role-based access control for your pharmacy application.

**What you'll get:**
- ✅ Keycloak running in Docker
- ✅ Separate authentication server (not embedded in your Java app)
- ✅ Role-based access control (DOCTOR, PHARMACIST, CUSTOMER, ADMIN)
- ✅ OAuth2/OpenID Connect standard
- ✅ Professional authentication UI
- ✅ Token-based security (JWT)

---

## 🚀 Step 1: Start Keycloak and PostgreSQL

### 1.1 Start Docker Containers

```bash
# From your project root directory
cd /Users/viktor.moreev/Dev/pharmacy-center/JAVA-BG

# Start Keycloak and PostgreSQL
docker-compose up -d

# Check if containers are running
docker ps
```

**Expected output:**
```
CONTAINER ID   IMAGE                              STATUS         PORTS
xxx            quay.io/keycloak/keycloak:23.0     Up 30 seconds  0.0.0.0:8080->8080/tcp
xxx            postgres:15-alpine                 Up 30 seconds  0.0.0.0:5433->5432/tcp
```

### 1.2 Wait for Keycloak to Start

```bash
# This takes about 30-60 seconds
# Watch the logs
docker logs -f pharmacy-keycloak
```

Wait until you see: `Keycloak 23.0 started`

Then press `Ctrl+C` to stop watching logs.

### 1.3 Access Keycloak Admin Console

Open your browser and go to: **http://localhost:8080**

**Login credentials:**
- Username: `admin`
- Password: `admin`

---

## 🔧 Step 2: Configure Keycloak

### 2.1 Create a Realm

A realm manages a set of users, credentials, roles, and groups.

1. **Click** the dropdown in top-left (currently shows "master")
2. **Click** "Create Realm"
3. **Enter:**
   - Realm name: `pharmacy-realm`
4. **Click** "Create"

### 2.2 Create a Client

A client represents your Spring Boot application.

1. **Go to:** Clients → Create client
2. **General Settings:**
   - Client type: `OpenID Connect`
   - Client ID: `pharmacy-app`
   - Click "Next"

3. **Capability config:**
   - ✅ Client authentication: ON
   - ✅ Authorization: ON
   - ✅ Standard flow: ON (for web login)
   - ✅ Direct access grants: ON (for API access)
   - Click "Next"

4. **Login settings:**
   - Valid redirect URIs: `http://localhost:8084/*`
   - Valid post logout redirect URIs: `http://localhost:8084/*`
   - Web origins: `http://localhost:8084`
   - Click "Save"

5. **Get Client Secret:**
   - Go to "Credentials" tab
   - Copy the "Client secret" (you'll need this later)
   - Example: `xK9fP2mN8vL3qR7wT1yU4bC5dE6fG8hJ` - LaCFdyTarZq0dVgPgPDomZVp5u8UzcKz

### 2.3 Create Roles

Roles define what users can do in your system.

1. **Go to:** Realm roles → Create role
2. **Create these 4 roles:**

   **Role 1: CUSTOMER**
   - Role name: `CUSTOMER`
   - Description: `Customer who can view medicines and their prescriptions`
   - Click "Save"

   **Role 2: PHARMACIST**
   - Role name: `PHARMACIST`
   - Description: `Pharmacist who can manage medicines and view prescriptions`
   - Click "Save"

   **Role 3: DOCTOR**
   - Role name: `DOCTOR`
   - Description: `Doctor who can create and manage prescriptions`
   - Click "Save"

   **Role 4: ADMIN**
   - Role name: `ADMIN`
   - Description: `Administrator with full system access`
   - Click "Save"

### 2.4 Create Test Users

Let's create 4 test users, one for each role.

1. **Go to:** Users → Create new user

**User 1: Admin**
- Username: `admin`
- Email: `admin@pharmacy.com`
- First name: `Admin`
- Last name: `User`
- Email verified: ON
- Click "Create"
- Go to "Credentials" tab
  - Click "Set password"
  - Password: `admin123`
  - Temporary: OFF
  - Click "Save"
- Go to "Role mapping" tab
  - Click "Assign role"
  - **Important:** Toggle OFF "Filter by clients" (or select "Filter by realm roles")
  - Now you'll see: ADMIN, DOCTOR, PHARMACIST, CUSTOMER
  - Select: `ADMIN`
  - Click "Assign"

**User 2: Doctor**
- Username: `doctor`
- Email: `doctor@pharmacy.com`
- First name: `Dr. John`
- Last name: `Smith`
- Email verified: ON
- Click "Create"
- Go to "Credentials" tab
  - Password: `doctor123`
  - Temporary: OFF
- Go to "Role mapping" tab
  - Click "Assign role"
  - Toggle OFF "Filter by clients"
  - Assign role: `DOCTOR`

**User 3: Pharmacist**
- Username: `pharmacist`
- Email: `pharmacist@pharmacy.com`
- First name: `Jane`
- Last name: `Pharmacist`
- Email verified: ON
- Click "Create"
- Go to "Credentials" tab
  - Password: `pharmacist123`
  - Temporary: OFF
- Go to "Role mapping" tab
  - Click "Assign role"
  - Toggle OFF "Filter by clients"
  - Assign role: `PHARMACIST`

**User 4: Customer**
- Username: `customer`
- Email: `customer@pharmacy.com`
- First name: `Bob`
- Last name: `Customer`
- Email verified: ON
- Click "Create"
- Go to "Credentials" tab
  - Password: `customer123`
  - Temporary: OFF
- Go to "Role mapping" tab
  - Click "Assign role"
  - Toggle OFF "Filter by clients"
  - Assign role: `CUSTOMER`

---

## 🔌 Step 3: Integrate Spring Boot with Keycloak

### 3.1 Add Keycloak Dependencies

Add these to your `build.gradle`:

```gradle
dependencies {
    // Existing dependencies...
    
    // Keycloak Spring Boot Starter
    implementation 'org.springframework.boot:spring-boot-starter-oauth2-resource-server'
    implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'
    
    // JWT support
    implementation 'org.springframework.security:spring-security-oauth2-jose'
}
```

### 3.2 Configure application.properties

Add these properties:

```properties
# Keycloak Configuration
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/pharmacy-realm
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8080/realms/pharmacy-realm/protocol/openid-connect/certs

# OAuth2 Client Configuration
spring.security.oauth2.client.registration.keycloak.client-id=pharmacy-app
spring.security.oauth2.client.registration.keycloak.client-secret=YOUR_CLIENT_SECRET_HERE
spring.security.oauth2.client.registration.keycloak.scope=openid,profile,email
spring.security.oauth2.client.registration.keycloak.authorization-grant-type=authorization_code
spring.security.oauth2.client.registration.keycloak.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}

# OAuth2 Provider Configuration
spring.security.oauth2.client.provider.keycloak.issuer-uri=http://localhost:8080/realms/pharmacy-realm
spring.security.oauth2.client.provider.keycloak.user-name-attribute=preferred_username
```

**Important:** Replace `YOUR_CLIENT_SECRET_HERE` with the actual client secret from Step 2.2.5

---

## 🛡️ Step 4: Update SecurityConfig

Your new SecurityConfig with Keycloak integration:

```java
package com.inf.cscb869_pharmacy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/", "/index", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/login/**", "/oauth2/**").permitAll()
                
                // Medicine endpoints - Customers can view, Pharmacists can manage
                .requestMatchers("/api/medicines/**").hasAnyRole("CUSTOMER", "PHARMACIST", "ADMIN")
                .requestMatchers("/medicines/**").hasAnyRole("CUSTOMER", "PHARMACIST", "ADMIN")
                
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
                .defaultSuccessUrl("/", true)
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
        return converter;
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
```

---

## 🎨 Step 5: Update Frontend

### 5.1 Update fragments.html Navigation

Add login/logout buttons and show user info:

```html
<nav class="navbar navbar-expand-lg navbar-dark">
    <div class="container">
        <a class="navbar-brand" href="/" th:href="@{/}">
            <span class="brand-icon">💊</span> Pharmacy Center
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" 
                data-bs-target="#navbarNav">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
            <ul class="navbar-nav ms-auto">
                <li class="nav-item">
                    <a class="nav-link" href="/" th:href="@{/}">🏠 Home</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="/medicines" th:href="@{/medicines}">
                        💊 Medicines
                    </a>
                </li>
                
                <!-- Show these only for authenticated users -->
                <li class="nav-item" sec:authorize="isAuthenticated()">
                    <a class="nav-link" href="/doctors" th:href="@{/doctors}">
                        👨‍⚕️ Doctors
                    </a>
                </li>
                <li class="nav-item" sec:authorize="hasAnyRole('DOCTOR', 'PHARMACIST', 'ADMIN')">
                    <a class="nav-link" href="/recipes" th:href="@{/recipes}">
                        📋 Prescriptions
                    </a>
                </li>
                <li class="nav-item" sec:authorize="hasAnyRole('PHARMACIST', 'ADMIN')">
                    <a class="nav-link" href="/customers" th:href="@{/customers}">
                        👥 Customers
                    </a>
                </li>
                
                <!-- User info and logout for authenticated users -->
                <li class="nav-item dropdown" sec:authorize="isAuthenticated()">
                    <a class="nav-link dropdown-toggle" href="#" id="userDropdown" 
                       role="button" data-bs-toggle="dropdown">
                        👤 <span sec:authentication="name">User</span>
                    </a>
                    <ul class="dropdown-menu">
                        <li><a class="dropdown-item" href="/logout" th:href="@{/logout}">
                            🚪 Logout
                        </a></li>
                    </ul>
                </li>
                
                <!-- Login button for anonymous users -->
                <li class="nav-item" sec:authorize="!isAuthenticated()">
                    <a class="nav-link btn btn-primary" href="/oauth2/authorization/keycloak">
                        🔐 Login
                    </a>
                </li>
            </ul>
        </div>
    </div>
</nav>
```

### 5.2 Add Thymeleaf Security Dialect

Add to `build.gradle`:

```gradle
dependencies {
    // ...
    implementation 'org.thymeleaf.extras:thymeleaf-extras-springsecurity6'
}
```

---

## 🧪 Step 6: Test the Integration

### 6.1 Rebuild and Restart Your Application

```bash
# Clean and build
./gradlew clean build -x test

# Run the application
./gradlew bootRun
```

### 6.2 Test Authentication

1. **Go to:** http://localhost:8084
2. **Click** "Login" button
3. **You'll be redirected to Keycloak login page**
4. **Login with:**
   - Username: `pharmacist`
   - Password: `pharmacist123`
5. **You'll be redirected back to your app**
6. **You should see your username in navigation**

### 6.3 Test Role-Based Access

**Test as Pharmacist:**
- ✅ Can access /medicines
- ✅ Can access /doctors
- ✅ Can access /recipes
- ✅ Can access /customers

**Test as Customer:**
- ✅ Can access /medicines
- ❌ Cannot access /doctors
- ❌ Cannot access /recipes
- ❌ Cannot access /customers

**Test as Doctor:**
- ✅ Can access /medicines
- ❌ Cannot access /doctors
- ✅ Can access /recipes
- ❌ Cannot access /customers

---

## 📊 Role Permission Matrix

| Endpoint | CUSTOMER | DOCTOR | PHARMACIST | ADMIN |
|----------|----------|--------|------------|-------|
| View Medicines | ✅ | ✅ | ✅ | ✅ |
| Create/Edit Medicines | ❌ | ❌ | ✅ | ✅ |
| View Doctors | ❌ | ❌ | ✅ | ✅ |
| Manage Doctors | ❌ | ❌ | ✅ | ✅ |
| View Prescriptions | Own Only | Own Only | ✅ | ✅ |
| Create Prescriptions | ❌ | ✅ | ✅ | ✅ |
| View Customers | ❌ | ❌ | ✅ | ✅ |
| Manage Customers | ❌ | ❌ | ✅ | ✅ |

---

## 🔍 Troubleshooting

### Issue: "401 Unauthorized" when accessing endpoints

**Solution:**
1. Make sure you're logged in
2. Check if your role has access to that endpoint
3. Check browser console for errors

### Issue: "CORS errors"

**Solution:** Add this to SecurityConfig:
```java
.cors(cors -> cors.configurationSource(request -> {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:8084"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
    config.setAllowedHeaders(List.of("*"));
    return config;
}))
```

### Issue: "Invalid redirect URI"

**Solution:**
1. Go to Keycloak Admin Console
2. Go to Clients → pharmacy-app → Settings
3. Make sure redirect URIs include: `http://localhost:8084/*`

### Issue: Keycloak won't start

**Solution:**
```bash
# Stop all containers
docker-compose down

# Remove volumes
docker volume rm java-bg_postgres_data

# Start again
docker-compose up -d
```

---

## 📝 Summary

**What you've accomplished:**
1. ✅ Keycloak running as external auth server
2. ✅ 4 roles configured (CUSTOMER, DOCTOR, PHARMACIST, ADMIN)
3. ✅ 4 test users created
4. ✅ Spring Boot integrated with OAuth2
5. ✅ Role-based endpoint security
6. ✅ Login/logout functionality
7. ✅ Professional auth UI from Keycloak

**Test Users:**
- admin / admin123 (ADMIN role)
- doctor / doctor123 (DOCTOR role)
- pharmacist / pharmacist123 (PHARMACIST role)
- customer / customer123 (CUSTOMER role)

**Keycloak Admin Console:**
- URL: http://localhost:8080
- Username: admin
- Password: admin

**Your Application:**
- URL: http://localhost:8084
- Uses Keycloak for authentication
- Secured with role-based access control

---

## 🎓 For Your Teacher

**Key Points to Highlight:**
1. ✅ **External Authentication System:** Using industry-standard Keycloak
2. ✅ **OAuth2/OpenID Connect:** Modern authentication protocol
3. ✅ **Role-Based Access Control:** 4 different user roles
4. ✅ **Separation of Concerns:** Auth server separate from application
5. ✅ **Production-Ready:** Keycloak is used by major enterprises
6. ✅ **Standards Compliant:** Following OAuth2 and JWT standards

---

*Setup guide created on January 14, 2026*
