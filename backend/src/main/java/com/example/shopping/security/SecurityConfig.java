
package com.example.shopping.security;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final int BCRYPT_ROUNDS = 10;
    private static final String ADMIN_ROLE_PREFIX = "ROLE_";

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jf) {
        this.jwtFilter = jf;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration ac) throws Exception {
        AuthenticationManager am = ac.getAuthenticationManager();
        return am;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        int cost = BCRYPT_ROUNDS;
        if (cost < 4) cost = 4;
        if (cost > 31) cost = 31;
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(cost);
        return encoder;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable());

        SessionCreationPolicy sessionPolicy = SessionCreationPolicy.STATELESS;
        http.sessionManagement(sm -> sm.sessionCreationPolicy(sessionPolicy));

        String[] openEndpoints = publicApiPaths();
        http.authorizeHttpRequests(auth -> {
            auth.requestMatchers(openEndpoints).permitAll();
            auth.requestMatchers("/api/admin/**").hasRole("ADMIN");
            auth.requestMatchers("/api/cart/**").authenticated();
            auth.requestMatchers("/api/orders/**").authenticated();
            auth.requestMatchers("/api/favorites/**").authenticated();
            auth.anyRequest().authenticated();
        });

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private String[] publicApiPaths() {
        String authRegister = "/api/auth/register";
        String authLogin = "/api/auth/login";
        String authMe = "/api/auth/me";
        String adminLogin = "/api/admin/login";
        String goodsApi = "/api/goods/**";
        String catsApi = "/api/categories/**";
        String reviewsApi = "/api/reviews/**";
        String feedbackApi = "/api/feedback";

        String[] paths = new String[8];
        int index = 0;
        paths[index++] = authRegister;
        paths[index++] = authLogin;
        paths[index++] = authMe;
        paths[index++] = adminLogin;
        paths[index++] = goodsApi;
        paths[index++] = catsApi;
        paths[index++] = reviewsApi;
        paths[index++] = feedbackApi;

        return paths;
    }
}
