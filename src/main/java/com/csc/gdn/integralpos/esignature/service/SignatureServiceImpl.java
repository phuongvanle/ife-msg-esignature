package com.csc.gdn.integralpos.esignature.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.csc.gdn.integralpos.domain.document.DocumentModel;
import com.csc.gdn.integralpos.esignature.common.EsignatureRestTemplate;
import com.csc.gdn.integralpos.esignature.common.IposException;
import com.csc.gdn.integralpos.esignature.common.IposRuntimeExpectationFailedException;
import com.csc.gdn.integralpos.esignature.common.PDFTextSearcher;
import com.csc.gdn.integralpos.esignature.common.PDFUtilHelper;
import com.csc.gdn.integralpos.esignature.common.SignatureConstants;
import com.csc.gdn.integralpos.esignature.model.MatchWord;
import com.csc.gdn.integralpos.esignature.model.SignatureModel;
import com.csc.gdn.integralpos.msgcommon.constant.BaseServicePath;
import com.csc.gdn.integralpos.msgcommon.model.FileMessageResource;

@Service("signatureService")
public class SignatureServiceImpl implements SignatureService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SignatureServiceImpl.class);
	
	@Value("${esignature.text-predifine}")
	private String textPredefine;
	
	@Autowired
	private EsignatureRestTemplate restTemplate;
	
	@Override
	/**
	 * this method is get pdf file from document center and then parsing to image
	 */
	public List<SignatureModel> signatureProcess(String id, Principal user) throws IposException, IOException {
		LOGGER.debug("JUMP INTO FUNTION signatureProcess =>>>>>>>>>>>>");
		Map<String, String> headers = restTemplate.buildHeaders(user);
		String uri = restTemplate.buildURI(BaseServicePath.DOCUMENT_SERVICE_ID,
				BaseServicePath.DOCUMENT_SERVICE_PATH + BaseServicePath.GET_DOCUMENT_BY_ID_PATH);
		List<String> pathVariables = new ArrayList<>();
		pathVariables.add(id);
		DocumentModel documentModel = restTemplate.getHttp(uri, headers, pathVariables, new HashMap<>(), new ParameterizedTypeReference<DocumentModel>() {
		});
		LOGGER.debug("GO OUT FUNTION signatureProcess <<<<<<<<<<<<<<====");
		return pdfProcess(documentModel.getContent(), documentModel.getId());
	}
	
	
	private List<SignatureModel> pdfProcess(byte[] content, String id) throws IposException, IOException {
		LOGGER.debug("JUMP INTO FUNTION pdfProcess =>>>>>>>>>>>>");
		PDFTextSearcher coordinateUtil = new PDFTextSearcher();
		Path tempFile;
		List<SignatureModel> results;
		List<MatchWord> matchResult = new ArrayList<>();
		try (InputStream inputStream = new ByteArrayInputStream(content);){
			tempFile = this.writeRequestToFile(inputStream);
			results = PDFUtilHelper.pdfToImage(tempFile.toFile());
			matchResult = coordinateUtil.findCoordinateWord(tempFile.toFile(), textPredefine);
			matchResult.parallelStream().forEach(m -> 
				results.stream().forEach(page -> {
					if (page.getPageOfPDF() == m.getPageNumber()) {
					page.setXCoordinateText(m.getXCoordinator());
					page.setYCoordinateText(m.getYCoodinator());
					page.setSignSignature(true);
					page.setDocumentId(id);
				}})
			);
			
		} catch (IOException e) {
			throw new IposException(e);
		}
		deleteTempFile(tempFile);
		LOGGER.debug("GO OUT FUNTION pdfProcess <<<<<<<<<<<<<<<<<<<=");
		return results;
	}
	
	public void deleteTempFile(Path tempFile) {
		try {
			
			Files.deleteIfExists(tempFile);
			LOGGER.debug("Deleted : {}", tempFile);
		} catch (IOException e) {
			LOGGER.error("Error when delete files", e);
			throw new IposRuntimeExpectationFailedException(e.getMessage());
		}
	}
	
	public Path writeRequestToFile(InputStream is) {
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		Path tempFilePath;
		try {
			tempFilePath = Files.createTempFile(tempDir.toPath(), Long.toString(System.nanoTime()), ".temp");
			Files.copy(is, tempFilePath, StandardCopyOption.REPLACE_EXISTING);
			LOGGER.debug("Write upload request to location {} sucessfully ", tempFilePath);
		} catch (Exception e) {
			LOGGER.error("Error when write upload request to file with error {} ", e);
			throw new IposRuntimeExpectationFailedException(e.getMessage());
		}
		return tempFilePath;
	}
	
	public Path writeRequestToPNG(InputStream is) {
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		Path tempFilePath;
		try {
			tempFilePath = Files.createTempFile(tempDir.toPath(), Long.toString(System.nanoTime()), ".png");
			Files.copy(is, tempFilePath, StandardCopyOption.REPLACE_EXISTING);
			LOGGER.debug("Write upload request to location {} sucessfully ", tempFilePath);
		} catch (Exception e) {
			LOGGER.error("Error when write upload request to file with error {} ", e);
			throw new IposRuntimeExpectationFailedException(e.getMessage());
		}
		return tempFilePath;
	}


	@Override
	public List<SignatureModel> signToPdf(SignatureModel model, Principal user, String id) throws IposException, IOException {
		LOGGER.debug("JUMP INTO FUNTION signToPdf =>>>>>>>>>>>>");
		Map<String, String> headers = restTemplate.buildHeaders(user);
		String uri = restTemplate.buildURI(BaseServicePath.DOCUMENT_SERVICE_ID,
				BaseServicePath.DOCUMENT_SERVICE_PATH + BaseServicePath.GET_DOCUMENT_BY_ID_PATH);
		List<String> pathVariables = new ArrayList<>();
		pathVariables.add(id);
		DocumentModel documentModel = restTemplate.getHttp(uri, headers, pathVariables, new HashMap<>(), new ParameterizedTypeReference<DocumentModel>() {
		});
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();) {
			addSignatureToPdf(documentModel, model, outputStream);
			// update document model from dms
			documentModel.setContent(outputStream.toByteArray());
		}
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Runnable updateDms = () -> {
			try {
				SecurityContextHolder.getContext().setAuthentication(authentication);
				LOGGER.info("Set authentication for new thread in async method");
				updateContentDocument(headers, id, documentModel, documentModel.getBusinessInformation().getDocumentName());
			} catch (Exception ex) {
				LOGGER.debug("UPDATE DOCUMENT IS UNSUCCESSFULLY - REASON ", ex);
			}
		};
		Executors.newSingleThreadExecutor().submit(updateDms);
		LOGGER.debug("GO OUT FUNTION signToPdf <<<<<<<<<<<<====");
		return this.pdfProcess(documentModel.getContent(), id);
	}
	
	private void addSignatureToPdf(DocumentModel documentModel, SignatureModel model, OutputStream outputStream) throws IOException {
		LOGGER.debug("JUMP INTO FUNTION addSignatureToPdf =>>>>>>>>>>>>");
		InputStream inputStream = new ByteArrayInputStream(documentModel.getContent());
		Path tempSignedFile = this.writeRequestToFile(inputStream);
		try (PDDocument document = PDDocument.load(tempSignedFile.toFile());) {
			Path tempFileSignature = this.writeRequestToPNG(new ByteArrayInputStream(model.getContent()));
			PDPage page = document.getPage(model.getPageOfPDF());
			PDImageXObject pdImage = PDImageXObject.createFromFile(tempFileSignature.toAbsolutePath().toString(), document);
			pdImage.setHeight(50);
			pdImage.setWidth(100);
			PDPageContentStream contentStream = new PDPageContentStream(document, page, AppendMode.APPEND, false);
			//Setting the font to the Content stream  
			contentStream.drawImage(pdImage, model.getXCoordinateText(), 792 - model.getYCoordinateText());
			contentStream.close();
			document.save(outputStream);
			deleteTempFile(tempFileSignature);
		}
		deleteTempFile(tempSignedFile);
		inputStream.close();
		LOGGER.debug("GO OUT FUNTION addSignatureToPdf <<<<<<<<<<<<<====");
	}
	
	public DocumentModel updateContentDocument(Map<String, String> dmsHeaders, String documentId, DocumentModel documentModel, String fileName)throws IposException {
		LOGGER.info("Update content document by ID: [{}] ==> BEGIN", documentId);

		String dmsPath = restTemplate.buildURI(BaseServicePath.DOCUMENT_SERVICE_ID,BaseServicePath.UPDATE_SERVICE);

		Map<String, Object> dmsParts = new HashMap<>();
		dmsParts.put(SignatureConstants.FILE, new FileMessageResource(documentModel.getContent(), fileName));

		dmsHeaders.put(SignatureConstants.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE);
		List<String> dmsPathVariables = new ArrayList<>();
		dmsPathVariables.add(documentId);
		Map<String, String> dmsUrlParameters = new HashMap<>();
		dmsUrlParameters.put(SignatureConstants.OPERATION, "attach");// add later

		DocumentModel documentModelAfterSave = restTemplate.postHttp(dmsPath, dmsHeaders, dmsPathVariables, dmsUrlParameters, dmsParts,
				new ParameterizedTypeReference<DocumentModel>() {
				});
		
		LOGGER.info("Update content document by ID <== END");
		return documentModelAfterSave ;
	}
}
