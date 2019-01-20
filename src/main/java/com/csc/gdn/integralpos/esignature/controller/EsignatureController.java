package com.csc.gdn.integralpos.esignature.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.csc.gdn.integralpos.esignature.common.IposException;
import com.csc.gdn.integralpos.esignature.model.SignatureModel;
import com.csc.gdn.integralpos.esignature.service.SignatureService;

@Controller
public class EsignatureController {
	
	@Autowired
	private SignatureService signatureService;
	
	@RequestMapping(value = "/esignatures/about", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<?> about() {
		Map<String, String> about = new HashMap<>();
		about.put("value", "Hello world");
		return ResponseEntity.ok(about);
	}
	
	@RequestMapping(value = "esignatures/{id}/getImageOfFile", method = RequestMethod.GET , produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<List<SignatureModel>> signatureProcess(Principal user, @PathVariable(name = "id") String id) throws IposException, IOException {
		return ResponseEntity.ok(signatureService.signatureProcess(id, user));
	}
	
	@RequestMapping(value = "esignatures/{id}/sign", method = RequestMethod.POST , produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<List<SignatureModel>> signatureProcess(Principal user, @PathVariable(name = "id") String id,
			@RequestBody SignatureModel body) throws IposException, IOException {
		return ResponseEntity.ok(signatureService.signToPdf(body, user, id));
	}
}
