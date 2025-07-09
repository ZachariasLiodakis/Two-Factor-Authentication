package com.fa._FASim.Config;

import com.fa._FASim.Security.CustomAuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    private UserDetailsService userDetailsService;
    private BCryptPasswordEncoder passwordEncoder;

    // Inject values from application.properties
    @Value("${security.rememberme.key}")
    private String rememberMeKey;

    @Value("${security.rememberme.token-validity}")
    private int tokenValiditySeconds;

    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService, BCryptPasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((requests) -> requests
                        // Allow public access to registration, login, 2FA setup, static resources, and error page
                        .requestMatchers("/", "/home", "/register", "/2fa-setup", "/css/**", "/js/**", "/images/**", "/2fa", "/error")
                        .permitAll()
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        // Custom login page
                        .loginPage("/login")
                        // Custom success handler to check 2FA
                        .successHandler(customAuthenticationSuccessHandler)
                        .permitAll()
                )
                .rememberMe((remember) -> remember
                        // Enable "remember me" with custom key and duration
                        .key(rememberMeKey)
                        .tokenValiditySeconds(tokenValiditySeconds)
                )
                .logout((logout) -> logout.permitAll()); // Allow everyone to access logout

        return http.build();
    }
}
