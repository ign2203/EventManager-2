package org.example.eventmanagermodule.security;

import org.example.eventmanagermodule.security.jwt.JwtTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {

    @Autowired
    private JwtTokenFilter jwtTokenFilter;

    @Autowired
    private CustomerDetailsService customerDetailsService;

    @Autowired
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Autowired//403
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers(HttpMethod.GET, "/locations/**")
                                .hasAnyAuthority("ADMIN", "USER")

                                .requestMatchers(HttpMethod.POST, "/locations/**")
                                .hasAnyAuthority("ADMIN")

                                .requestMatchers(HttpMethod.DELETE, "/locations/**")
                                .hasAnyAuthority("ADMIN")

                                .requestMatchers(HttpMethod.PUT, "/locations/**")
                                .hasAnyAuthority("ADMIN")

                                .requestMatchers(HttpMethod.GET, "/users/**")
                                .hasAnyAuthority("ADMIN")

                                .requestMatchers(HttpMethod.POST, "/users")
                                .permitAll()

                                .requestMatchers(HttpMethod.POST, "/users/auth")
                                .permitAll()

                                .requestMatchers(HttpMethod.POST, "/events")
                                .hasAnyAuthority("ADMIN", "USER")

                                .requestMatchers(HttpMethod.DELETE, "/events/**")
                                .hasAnyAuthority("ADMIN", "USER")

                                .requestMatchers(HttpMethod.GET, "/events/**")
                                .hasAnyAuthority("ADMIN", "USER")

                                .requestMatchers(HttpMethod.GET, "/events/my")
                                .hasAnyAuthority("ADMIN", "USER")

                                .requestMatchers(HttpMethod.POST, "/search")
                                .hasAnyAuthority("ADMIN", "USER")

                                .requestMatchers(HttpMethod.POST, "/registrations/**")
                                .hasAnyAuthority("ADMIN", "USER")

                                .requestMatchers(HttpMethod.GET, "/registrations/my")
                                .hasAnyAuthority("ADMIN", "USER")

                                .requestMatchers(HttpMethod.DELETE, "/registrations/cancel/**")
                                .hasAnyAuthority("ADMIN", "USER")

                                .anyRequest().authenticated()



                )
                .exceptionHandling(exception ->
                        exception
                                .authenticationEntryPoint(customAuthenticationEntryPoint)
                                .accessDeniedHandler(customAccessDeniedHandler)
                )
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        var authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customerDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
