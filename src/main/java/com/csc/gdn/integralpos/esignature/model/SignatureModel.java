package com.csc.gdn.integralpos.esignature.model;

import lombok.Data;

@Data
public class SignatureModel {
	
	private String documentId;
	
	private int pageOfPDF;
	
	private float xCoordinateText;
	
	private float yCoordinateText;
	
	private boolean isSignSignature;
	
	private byte[] content;
	

}
