package com.pa.spring.prueba1.pa_prueba1.security;

import com.pa.spring.prueba1.pa_prueba1.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final CustomLoginSuccessHandler successHandler;

    // jwtAuthenticationFilter removed when JWT support was disabled

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          CustomLoginSuccessHandler successHandler) {
        this.userDetailsService = userDetailsService;
        this.successHandler = successHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManager.class);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // registrar proveedor personalizado para autenticación basada en la base de datos
        http.authenticationProvider(authenticationProvider());

        http
            .csrf(csrf -> csrf.disable())
            // login uses typical session creation policy (stateful)
            .sessionManagement(sess -> sess.sessionCreationPolicy(
                    org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/home", "/login", "/admin/login", "/registro", "/recuperar-password", "/recuperar-password/**", "/css/**", "/js/**", "/images/**", "/api/auth/**").permitAll()
                .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/estilista/**").hasAuthority("ROLE_ESTILISTA")
                .requestMatchers("/cliente/**").hasAuthority("ROLE_USER")
                .requestMatchers("/reserva/**", "/mis-reservas/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(successHandler)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(exception -> exception
                .accessDeniedPage("/403")
            );

        // JWT filter removed; using standard authentication flow

        return http.build();
    }
}
