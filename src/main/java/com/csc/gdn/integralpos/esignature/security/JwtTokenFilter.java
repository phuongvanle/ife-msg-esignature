package com.csc.gdn.integralpos.esignature.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import com.csc.gdn.integralpos.msgcommon.utility.exception.CustomException;
import com.csc.gdn.integralpos.msgcommon.utility.oauth.JwtTokenProvider;

public class JwtTokenFilter extends GenericFilterBean {
	
	 private JwtTokenProvider jwtTokenProvider;
	 
	public JwtTokenFilter(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain)
			throws IOException, ServletException {
		
		String token = jwtTokenProvider.resolveToken((HttpServletRequest) req);
	    try {
	      if (token != null && jwtTokenProvider.validateToken(token)) {
	        Authentication auth = jwtTokenProvider.getAuthentication(token) ;
	        SecurityContextHolder.getContext().setAuthentication(auth);
	      }
	    } catch (CustomException ex) {
	      HttpServletResponse response = (HttpServletResponse) res;
	      response.sendError(ex.getHttpStatus().value(), ex.getMessage());
	      return;
	    }

	    filterChain.doFilter(req, res);
		
	}

}
