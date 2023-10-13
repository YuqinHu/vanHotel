package com.example.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private DataSource dataSource;

    @Bean(name = "myUserDetailsService")
    public UserDetailsService userDetailsService() {
        return new CustomUserDetailsService();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/users/**").hasAnyAuthority("ADMIN")
            .antMatchers("/users").hasAnyAuthority("ADMIN")
            .antMatchers("/home").authenticated()
            .antMatchers("/home/customer").hasAnyAuthority("USER","ADMIN")
            .antMatchers("/home/hotelManager").hasAnyAuthority("HOTEL", "ADMIN")
            .antMatchers("/home/admin").hasAnyAuthority("ADMIN")
            .antMatchers("/hotels").hasAnyAuthority("HOTEL","ADMIN","USER")
            .antMatchers("/hotels/**").hasAnyAuthority("HOTEL","ADMIN", "USER")
            .antMatchers("/comments").hasAnyAuthority("USER","ADMIN")
            .antMatchers("/comments/**").hasAnyAuthority("USER","ADMIN")
            .antMatchers("/rooms").hasAnyAuthority("HOTEL", "ADMIN")
            .antMatchers("/rooms/**").hasAnyAuthority("HOTEL", "ADMIN")
            .antMatchers("/assets/**").permitAll()
            .antMatchers("/stylesheets/**").permitAll()
            .antMatchers("vendor/**").permitAll()
            .anyRequest().permitAll()
            .and()
            .formLogin().usernameParameter("email").defaultSuccessUrl("/home").permitAll()
            .and()
            .logout().logoutSuccessUrl("/").permitAll();
    }

}