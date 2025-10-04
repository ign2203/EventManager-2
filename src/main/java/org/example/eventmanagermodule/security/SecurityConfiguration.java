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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

@Configuration
@EnableMethodSecurity
// @EnableMethodSecurity — включает поддержку аннотаций вроде @PreAuthorize("hasAuthority('ADMIN')").
// То есть ты можешь проверять права прямо на уровне методов, а не только по URL.
public class SecurityConfiguration {

    // класс отвечает за хранение пользователя в контексте секюрити, и выдачи аутентификации
    @Autowired
    private JwtTokenFilter jwtTokenFilter;


    //этот класс оборачивает твоего пользователя в объект UserDetails, который Security может понимать и проверять.
    @Autowired
    private CustomerDetailsService customerDetailsService;

    //обработчик ошибок аутентификации — если пользователь не вошёл в систему, но пытается попасть в защищённый эндпоинт
    @Autowired//401
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;


    //🚫 Этот обработчик вызывается, если пользователь аутентифицирован, но не имеет прав.
    @Autowired//403
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .formLogin(login -> login.disable())//— отключаем стандартную форму логина (мы не хотим HTML-формы, у нас REST + JWT).
                .csrf(csrf -> csrf.disable()) // — отключаем защиту от CSRF, так как у нас stateless REST API, не храним сессии.
                .sessionManagement(session ->// — каждый запрос независим, данные не хранятся в сессии
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)// так как неизменяемый, можно было поставить флан, на сохранение запросов
                )
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers(HttpMethod.GET, "/locations/**")
                                .hasAnyAuthority("ADMIN", "USER")// все могут получить инфу по локациям

                                .requestMatchers(HttpMethod.POST, "/locations/**")
                                .hasAnyAuthority("ADMIN")// добавлять локацию может только админ

                                .requestMatchers(HttpMethod.DELETE, "/locations/**")
                                .hasAnyAuthority("ADMIN")// удалять локацию может только админ

                                .requestMatchers(HttpMethod.PUT, "/locations/**")
                                .hasAnyAuthority("ADMIN")// изменять локацию может только админ

                                .requestMatchers(HttpMethod.GET, "/users/**")
                                .hasAnyAuthority("ADMIN")// получать инфу о пользователе может только админ

                                .requestMatchers(HttpMethod.POST, "/users")
                                .permitAll()// регистрация пользователя, все

                                .requestMatchers(HttpMethod.POST, "/users/auth")
                                .permitAll()// аутентификация пользователей, все
                                .anyRequest().authenticated()//— всё остальное требует JWT и аутентификации.
                )
                .exceptionHandling(exception -> // — подключаем кастомные обработчики ошибок 401 и 403.
                        exception
                                .authenticationEntryPoint(customAuthenticationEntryPoint)
                                .accessDeniedHandler(customAccessDeniedHandler)
                )
                .addFilterBefore(jwtTokenFilter, AnonymousAuthenticationFilter.class)//— вставляем наш JwtTokenFilter перед стандартным фильтром Spring, чтобы он первым проверял токен.
                .build();// собираем
    }

    //AuthenticationManager → главный "дирижёр" аутентификации, вызывает правильный AuthenticationProvider.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    //AuthenticationProvider (например DaoAuthenticationProvider) → проверяет логин/пароль через UserDetailsService.
    @Bean
    public AuthenticationProvider authenticationProvider() {
        var authProvider = new DaoAuthenticationProvider();
/*
//— стандартный провайдер Spring:
        использует customerDetailsService для поиска пользователя;
        сверяет пароли через PasswordEncoder.
        */
        authProvider.setUserDetailsService(customerDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();//— безопасный способ хэширования паролей при регистрации и проверке при логине.
    }

}
