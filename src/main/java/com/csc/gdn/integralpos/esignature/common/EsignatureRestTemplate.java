package com.csc.gdn.integralpos.esignature.common;

import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.csc.gdn.integralpos.msgcommon.model.AbstractBaseModel;
import com.csc.gdn.integralpos.msgcommon.utility.discovery.DiscoveryHelper;
import com.csc.gdn.integralpos.msgcommon.utility.oauth.OauthHelper;
import com.csc.gdn.integralpos.msgcommon.utility.resttemplate.RestTemplateHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
@Component
public class EsignatureRestTemplate extends RestTemplateHelper {
	
	@Autowired
	private EsignatureObjectMapper esignatureObjectMapper;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EsignatureRestTemplate.class);
	
	@Autowired
	protected OauthHelper oauthHelper;
	
	@Autowired
	protected DiscoveryHelper discoveryHelper;
	
	@Override
	public RestTemplate build()
	{
		RestTemplate resTemplate  = new RestTemplate(Collections.<HttpMessageConverter<?>> singletonList(buildMessageConverter()));
		resTemplate.getMessageConverters().add(new ResourceHttpMessageConverter());
		resTemplate.getMessageConverters().add(new FormHttpMessageConverter());
		resTemplate.getMessageConverters().add(new StringHttpMessageConverter());
		return resTemplate;
	}
	
	@Override
	public MappingJackson2HttpMessageConverter buildMessageConverter()
	{
		ObjectMapper mapper = new ObjectMapper();
		esignatureObjectMapper.jacksonBuilder().configure(mapper);
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setObjectMapper(mapper);
		return converter;
	}
	
	public <T> T getHttp(String uri, Map<String, String> headers, List<String> pathVariables,
			Map<String, String> urlParameters, ParameterizedTypeReference<T> type) throws IposException {
		ResponseEntity<T> response = this.get(uri, headers, pathVariables, urlParameters, type);
		if (response == null || response.getStatusCode() != HttpStatus.OK)
			throw new IposException(response == null ? "" : response.getBody().toString());
		LOGGER.debug("Do get method success to {}", uri);
		return response.getBody();
	}
	
	public <T> T putHttp(String uri, Map<String, String> headers, List<String> pathVariables,
			Map<String, String> urlParameters, Object body, ParameterizedTypeReference<T> type) throws IposException {
		ResponseEntity<T> response = this.put(uri, MediaType.APPLICATION_JSON_VALUE, headers,
				pathVariables, urlParameters, body, type);
		if (response == null || response.getStatusCode() != HttpStatus.OK)
			throw new IposException(response == null ? "" : response.getBody().toString());
		LOGGER.debug("Do put method success to {}", uri);
		return response.getBody();
	}
	
	public <T> T postHttp(String uri, Map<String, String> headers, List<String> pathVariables,
			Map<String, String> urlParameters, Object body, ParameterizedTypeReference<T> type) throws IposException {
		ResponseEntity<T> response = null;
		if (body instanceof AbstractBaseModel) {
			response = this.post(uri, MediaType.APPLICATION_JSON_VALUE, headers, pathVariables,
					urlParameters, body, type);
		} else if (body instanceof Map) {
			response = this.post(uri, MediaType.MULTIPART_FORM_DATA_VALUE, headers, pathVariables,
					urlParameters, body, type);
		}
		if (response == null
				|| response.getStatusCode() != HttpStatus.OK && response.getStatusCode() != HttpStatus.CREATED)
			throw new IposException(response == null ? "" : response.getBody().toString());
		return response.getBody();
	}
	
	public Map<String, String> buildHeaders(Principal user) {
		// Build headers
		HttpHeaders headers = this.buildHeaderWithToken(oauthHelper.getAccessToken(user));
		Map<String, String> headersMap = new HashMap<>();
		for (String header : headers.keySet()) {
			headersMap.put(header, headers.getFirst(header));
		}
		return headersMap;
	}
	
	public String buildURI(String serviceId, String path) {
		return discoveryHelper.getURI(serviceId) + path;
	}
	
}
