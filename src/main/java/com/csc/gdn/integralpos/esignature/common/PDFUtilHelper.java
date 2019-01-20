package com.csc.gdn.integralpos.esignature.common;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csc.gdn.integralpos.esignature.model.SignatureModel;
import com.csc.gdn.integralpos.msgcommon.utility.exception.IposRuntimeException;

public final class PDFUtilHelper {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PDFUtilHelper.class);
	
	private static final String OUTPUT_DIR = "/tmp/";
	
	private PDFUtilHelper() {
		throw new IposRuntimeException(SignatureConstants.UNSUPPORTED_OPERATION_EXCEPTION);
	}
	public static final List<SignatureModel> pdfToImage(File file) {
		List<SignatureModel> results = new ArrayList<>();
		try (final PDDocument document = PDDocument.load(file)) {
			PDFRenderer pdfRender = new PDFRenderer(document);
			ByteArrayOutputStream out;
			for (int page = 0; page < document.getNumberOfPages(); page++) {
				out = new ByteArrayOutputStream();
				SignatureModel pdfImage = new SignatureModel();
				BufferedImage bim = pdfRender.renderImageWithDPI(page, 300, ImageType.RGB);
				ImageIOUtil.writeImage(bim, "png", out, 300);
				pdfImage.setContent(out.toByteArray());
				pdfImage.setPageOfPDF(page);
				results.add(pdfImage);
				out.close();
			}
		} catch (IOException e) {
			LOGGER.debug("ERROR DURING CONVERT PDF TO IMAGE - REASON ", e);
		}
		return results;
	}

}
