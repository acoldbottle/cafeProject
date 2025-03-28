package cafeLogProject.cafeLog.common.config;


import cafeLogProject.cafeLog.common.auth.jwt.JWTFilter;
import cafeLogProject.cafeLog.common.auth.jwt.JWTLoginHandler;
import cafeLogProject.cafeLog.common.auth.jwt.JWTLogoutHandler;
import cafeLogProject.cafeLog.common.auth.jwt.JWTUtil;
import cafeLogProject.cafeLog.common.auth.jwt.token.JWTTokenService;
import cafeLogProject.cafeLog.common.auth.oauth2.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService oAuth2UserService;
    private final JWTLoginHandler loginHandler;
    private final JWTUtil jwtUtil;
    private final JWTTokenService tokenService;
    private final JWTLogoutHandler logoutHandler;
    public static final String[] whiteList = {
            "/api/auth/login",
            "/api/auth/check",
            "/login/oauth2/code/**",
            "/login/oauth2/authorization/**",
            "/login",
            "/api/logout"
    };

    @Bean
    public JWTFilter jwtFilter() {
        return new JWTFilter(jwtUtil, tokenService, whiteList);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {


        http
                .csrf(AbstractHttpConfigurer::disable);

        http
                .cors(AbstractHttpConfigurer::disable);

        http
                .formLogin(AbstractHttpConfigurer::disable);

        http
                .oauth2Login((auth) -> auth
                        .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                                .userService(oAuth2UserService))
                        .successHandler(loginHandler))
                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .addLogoutHandler(logoutHandler)
                        .logoutSuccessHandler(((request, response, authentication) -> SecurityContextHolder.clearContext())));

        http
                .addFilterAfter(jwtFilter(), UsernamePasswordAuthenticationFilter.class);


        http
                .exceptionHandling(entrypoint -> entrypoint
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                            response.setContentType("application/json; charset=UTF-8");

                            String json = String.format("{\"status\": %d, \"message\": \"%s\"}",
                                    HttpServletResponse.SC_NOT_FOUND,
                                    "정의 되지 않은 엔드포인트입니다.");
                            response.getWriter().write(json);
                        }));

        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers(whiteList).permitAll()
                        .anyRequest().authenticated());

        http
                .sessionManagement((auth) -> auth
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

}