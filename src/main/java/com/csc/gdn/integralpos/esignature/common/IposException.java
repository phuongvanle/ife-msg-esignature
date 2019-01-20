package com.csc.gdn.integralpos.esignature.common;

import com.csc.gdn.integralpos.esignature.common.SignatureConstants;

public class IposException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public IposException(Throwable e) {
		super(e);
	}

	public IposException() {
		super(SignatureConstants.IPOS_RUNTIME_EXCEPTION);
	}

	public IposException(String cause) {
		super(cause);
	}
}
