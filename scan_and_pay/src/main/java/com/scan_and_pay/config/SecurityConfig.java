package com.scan_and_pay.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.scan_and_pay.security.CustomUserDetailsService;
import com.scan_and_pay.security.JwtAuthenticationEntryPoint;
import com.scan_and_pay.security.JwtRequestFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final JwtRequestFilter jwtRequestFilter;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService,
                         JwtAuthenticationEntryPoint unauthorizedHandler,
                         JwtRequestFilter jwtRequestFilter) {
        this.customUserDetailsService = customUserDetailsService;
        this.unauthorizedHandler = unauthorizedHandler;
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(unauthorizedHandler)
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                // ===================== STATIC RESOURCES =====================
                .requestMatchers(
                    "/",
                    "/index",
                    "/index.html",
                    "/login",
                    "/login.html", 
                    "/register",
                    "/forgot-password",
                    "/forgot-password.html",
                    "/register.html",
                    "/home",
                    "/home.html"
                ).permitAll()
                
                // All static file patterns
                .requestMatchers(
                    "/**.html",
                    "/**.css",
                    "/**.js",
                    "/**.png",
                    "/**.jpg", 
                    "/**.jpeg",
                    "/**.gif",
                    "/**.ico",
                    "/**.svg",
                    "/**.woff",
                    "/**.woff2",
                    "/**.ttf",
                    "/**.json"
                ).permitAll()
                
                // All static folders
                .requestMatchers(
                    "/static/**",
                    "/resources/**",
                    "/public/**",
                    "/assets/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/fonts/**",
                    "/templates/**",
                    "/webjars/**",
                    "/META-INF/resources/**"
                ).permitAll()
                
                // ===================== API ENDPOINTS =====================
                .requestMatchers(
                    "/api/auth/**",
                    "/api/public/**",
                    "/api/otp/**",
                    "/api/users/register",
                    "/api/users/verify-email",
                    "/api/qrcodes/validate"
                ).permitAll()
                
                // ===================== DOCUMENTATION & TOOLS =====================
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()
                
                // ===================== MANAGEMENT & TOOLS =====================
                .requestMatchers(
                    "/actuator/**",
                    "/management/**",
                    "/h2-console/**"
                ).permitAll()
                
                // ===================== ERROR PAGES =====================
                .requestMatchers(
                    "/error",
                    "/error/**",
                    "/favicon.ico"
                ).permitAll()
                
                // ===================== PROTECTED ENDPOINTS =====================
                // Admin only endpoints
                .requestMatchers(
                    "/api/admin/**", 
                    "/admin/**",
                    "/api/dashboard/admin/**", 
                    "/api/dashboard/stats/**"
                ).hasRole("ADMIN")
                
                // Merchant endpoints
                .requestMatchers(
                    "/api/merchants/**",
                    "/merchant/**"
                ).hasAnyRole("MERCHANT", "ADMIN")
                
                // User endpoints  
                .requestMatchers(
                    "/api/users/**",
                    "/user/**",
                    "/dashboard/**",
                    "/profile/**"
                ).hasAnyRole("USER", "MERCHANT", "ADMIN")
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            // For H2 Console in development
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .userDetailsService(customUserDetailsService)
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type", 
            "X-Auth-Token", 
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers",
            "Cache-Control"
        ));
        configuration.setExposedHeaders(Arrays.asList(
            "X-Auth-Token",
            "Authorization", 
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}