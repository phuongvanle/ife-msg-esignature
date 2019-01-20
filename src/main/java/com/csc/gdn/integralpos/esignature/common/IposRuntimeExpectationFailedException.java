/**
 * //*******************************************************************************
 * // * Copyright (C) 2010-2016 CSC - All rights reserved.
 * // *
 * // * The information contained in this document is the exclusive property of
 * // * CSC.  This work is private under USA copyright law
 * // * and the copyright laws of given countries of origin and international
 * // * laws, treaties and/or conventions. No part of this document may be
 * // * reproduced or transmitted in any form or by any means, electronic or
 * // * mechanical including photocopying or by any informational storage or
 * // * retrieval system, unless as expressly permitted by CSC.
 * // *
 * // * Design, Develop and Manage by Team Integral Point-of-Sales & Services
 * // ******************************************************************************
 */

package com.csc.gdn.integralpos.esignature.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.csc.gdn.integralpos.msgcommon.utility.exception.IposRuntimeException;

/**
 * 
 * @author TacVong - tvong3
 *
 */
@ResponseStatus(value = HttpStatus.EXPECTATION_FAILED, reason = SignatureConstants.EXPECTATION_FAILED)
public class IposRuntimeExpectationFailedException extends IposRuntimeException {
	private static final long serialVersionUID = 1L;

	public IposRuntimeExpectationFailedException() {
		super(SignatureConstants.EXPECTATION_FAILED);
	}

	public IposRuntimeExpectationFailedException(String cause) {
		super(cause);
	}
}