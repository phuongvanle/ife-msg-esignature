package com.csc.gdn.integralpos.esignature.config;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.oauth2.resource.FixedPrincipalExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;


@Configuration
@EnableResourceServer
@ConditionalOnProperty(prefix = "security.authservice", name = "jwt", havingValue="false")
public class OAuth2ResourceServerConfig extends ResourceServerConfigurerAdapter {
	
	@Override
    public void configure(HttpSecurity http) throws Exception {
        http
        	.authorizeRequests()
        	.antMatchers("/swagger-ui.html", "/swagger-resources/**", "/v2/api-docs")
        	.permitAll()
        	.and()
            .antMatcher("/**")
            // Add below
            .authorizeRequests()
                .anyRequest().authenticated();
    }
	
	@Override
    public void configure(ResourceServerSecurityConfigurer config) {
    	config.resourceId("webresource");
        config.tokenServices(tokenServices());
    }
	
	@Bean
	@Primary
	public UserInfoTokenServices tokenServices() {
		ClientResources client = oauth2();
		return new UserInfoTokenServices(client.getResource().getUserInfoUri(), client.getClient().getClientId()) {
			private PrincipalExtractor principalExtractor = new FixedPrincipalExtractor();

			@Override
			protected Object getPrincipal(Map<String, Object> map) {
				String principal = (String) this.principalExtractor.extractPrincipal(map);
				return (principal == null ? "unknown" : principal.toLowerCase());
			}
		};
	}
	 
	@Bean
	@ConfigurationProperties("security.oauth2")
	public ClientResources oauth2() {
		return new ClientResources();
	}
	

}

class ClientResources {

	@NestedConfigurationProperty
	private AuthorizationCodeResourceDetails client = new AuthorizationCodeResourceDetails();

	@NestedConfigurationProperty
	private ResourceServerProperties resource = new ResourceServerProperties();

	public AuthorizationCodeResourceDetails getClient() {
		return client;
	}

	public ResourceServerProperties getResource() {
		return resource;
	}
}
