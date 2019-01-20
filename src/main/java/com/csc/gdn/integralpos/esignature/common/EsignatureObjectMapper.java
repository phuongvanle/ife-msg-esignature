package com.csc.gdn.integralpos.esignature.common;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
@Configuration
@ConfigurationProperties()
public class EsignatureObjectMapper {
	
	@Value("${spring.jackson.date-format}")
	protected String dateformat;
	
	@Value("${spring.jackson.time-zone}")
	protected String timeZone;
	
	@Bean
	@ConditionalOnClass({ ObjectMapper.class, Jackson2ObjectMapperBuilder.class })
	public Jackson2ObjectMapperBuilder jacksonBuilder()
	{ 
	    Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder(); 
	    builder.indentOutput(true).dateFormat(new SimpleDateFormat(dateformat));
	    System.setProperty("user.timezone", timeZone);
	    TimeZone.setDefault(null);
	    builder.timeZone(TimeZone.getTimeZone(timeZone));
	    return builder;
	}

}
