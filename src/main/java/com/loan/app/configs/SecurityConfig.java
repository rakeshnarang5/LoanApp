package com.loan.app.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails alpha = User.withUsername("alpha")
                .password(encoder.encode("alpha@123"))
                .roles("ADMIN", "USER")
                .build();
        UserDetails rohan = User.withUsername("rohan")
                .password(encoder.encode("rohan@123"))
                .roles("USER")
                .build();
        UserDetails rajeev = User.withUsername("rajeev")
                .password(encoder.encode("rajeev@123"))
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(alpha, rohan, rajeev);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests().requestMatchers("/loans/create", "/loans/view").authenticated()
                .and()
                .authorizeHttpRequests().requestMatchers("/loans/pending", "/loans/viewAll", "/loans/approve/**").authenticated()
                .and().formLogin()
                .and().build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
