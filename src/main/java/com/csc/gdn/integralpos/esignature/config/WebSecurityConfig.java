package com.csc.gdn.integralpos.esignature.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.csc.gdn.integralpos.esignature.security.JwtTokenFilterConfigurer;
import com.csc.gdn.integralpos.msgcommon.utility.oauth.JwtTokenProvider;

@Configuration
@ConditionalOnProperty(prefix = "security.authservice", name = "jwt", havingValue = "true")
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private JwtTokenProvider jwtTokenProvider;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http.csrf().disable();

		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

		// Entry points
        http
    		.authorizeRequests()
    		.antMatchers("/swagger-ui.html", "/swagger-resources/**", "/v2/api-docs")
    		.permitAll()
    		.and()
    		.antMatcher("/**")
    		// Add below
    		.authorizeRequests()
            .anyRequest().authenticated();
		http.apply(new JwtTokenFilterConfigurer(jwtTokenProvider));
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(12);
	}

}
