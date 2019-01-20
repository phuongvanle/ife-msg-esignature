package com.csc.gdn.integralpos.esignature.service;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

import com.csc.gdn.integralpos.esignature.common.IposException;
import com.csc.gdn.integralpos.esignature.model.SignatureModel;

public interface SignatureService {

	List<SignatureModel> signatureProcess(String id, Principal user) throws IposException, IOException;
	
	List<SignatureModel> signToPdf(SignatureModel model, Principal user, String id) throws IposException, IOException;
	
}
