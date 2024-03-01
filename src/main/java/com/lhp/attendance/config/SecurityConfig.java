package com.lhp.attendance.config;

import com.lhp.attendance.service.UserService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig{

    private final CustomLoginFailureHandler loginFailureHandler;
    private final CustomLoginSuccessHandler loginSuccessHandler;
    private final UserService userService;

    @Qualifier("securityDataSource")
    private final DataSource appDataSource;

    public SecurityConfig(CustomLoginFailureHandler loginFailureHandler, CustomLoginSuccessHandler loginSuccessHandler, UserService userService, DataSource appDataSource) {
        this.loginFailureHandler = loginFailureHandler;
        this.loginSuccessHandler = loginSuccessHandler;
        this.userService = userService;
        this.appDataSource = appDataSource;
    }

    @Bean
    public UserDetailsManager userDetailsManager() {
        return new JdbcUserDetailsManager(appDataSource);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return http
                .csrf((csrf) -> csrf.disable())
                //Uncomment the following lines if using API with separate front-end
                //.cors(Customizer.withDefaults())
                //.sessionManagement((sessionManagement) ->
                //        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((configurer) ->
                        configurer
                                .requestMatchers("/css/**").permitAll()
                                .requestMatchers("/images/**").permitAll()
                                .requestMatchers("/js/**").permitAll()
                                .requestMatchers("/loginPage").permitAll()
                                .requestMatchers("/error/**").permitAll()
                                .requestMatchers("/").authenticated()
                                .requestMatchers("/managers/**").hasAnyRole("MANAGER", "ADMIN")
                                .requestMatchers("/fragments/**").hasAnyRole("MANAGER", "ADMIN")
                                .requestMatchers("/admins/**").hasRole("ADMIN")
                                .requestMatchers("/actuator/**").hasRole("ADMIN"))
                .formLogin(configurer ->
                        configurer
                                .loginPage("/loginPage")
                                .loginProcessingUrl("/authenticateUser")
                                .defaultSuccessUrl("/managers/index", true)
                                .failureHandler(loginFailureHandler)
                                .successHandler(loginSuccessHandler)
                                .usernameParameter("username")
                                .permitAll())
                .logout(configurer ->
                        configurer
                                .logoutUrl("/logout")
                                .invalidateHttpSession(true)
                                .clearAuthentication(true)
                                .deleteCookies("JSESSIONID")
                                .permitAll())
                .rememberMe(configurer ->
                        configurer
                                .rememberMeParameter("remember-me")
                                .tokenRepository(tokenRepository())
                                .key("352d55e652251fa1ce38009492f14cf19253a248")
                                .tokenValiditySeconds(432000)
                                .userDetailsService(userService))
                .exceptionHandling(configurer ->
                        configurer.accessDeniedPage("/access-denied"))
                .build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(userService);
        auth.setPasswordEncoder(passwordEncoder());
        return auth;
    }

    @Bean
    public PersistentTokenRepository tokenRepository() {
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(appDataSource);
        return jdbcTokenRepository;
    }

    //Enable if using API with separate front end
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        CorsConfiguration config = new CorsConfiguration();
//        config.setAllowedOrigins(List.of("http://localhost:8080"));
//        config.setAllowedMethods(List.of("*"));
//        config.setAllowedHeaders(List.of("*"));
//        config.setAllowCredentials(false);
//        config.applyPermitDefaultValues();
//
//        source.registerCorsConfiguration("/**", config);
//        return source;
//    }
}
