package com.csc.gdn.integralpos.esignature.common;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import com.csc.gdn.integralpos.esignature.model.MatchWord;

public class PDFTextSearcher extends PDFTextStripper {
	
	 private StringBuilder oneWord = new StringBuilder();
	 private StringBuilder sequenceWords = new StringBuilder();
	 private List<String> wordsCoordinate = new ArrayList<>();
	 private List<MatchWord> listMatchWords = new ArrayList<>();
	 private boolean is1stChar = true;
	 private double lastYVal;
	 private String seek;
	 private String[] seekA;

	public PDFTextSearcher() throws IOException {
		super.setSortByPosition(true);
		this.output = new Writer() {
			
			@Override
			public void write(char[] cbuf, int off, int len) throws IOException {
				// the method's not implement yet
				
			}
			
			@Override
			public void flush() throws IOException {
				// the method's not implement yet
				
			}
			
			@Override
			public void close() throws IOException {
				// the method's not implement yet
				
			}
		};
	}
	
	
	@Override
	protected void processTextPosition(TextPosition textPosition) {
		boolean lineMatch;
		 String tChar = textPosition.getUnicode();
	        char c = tChar.charAt(0);
	        lineMatch = matchCharLine(textPosition);
	        if ((!Character.isWhitespace(c))) {	
	            if ((!is1stChar) && (lineMatch)) {
	                appendChar(tChar);
	            } else if (is1stChar) {
	                setWordCoord(textPosition, tChar);
	            }
	        } else {
	            endWord();
	        }
	}
	
	protected void appendChar(String tChar) {
		oneWord.append(tChar);
        is1stChar = false;
    }
	
	protected void setWordCoord(TextPosition text, String tChar) {
    	int pageNo = getCurrentPageNo() - 1;
    	oneWord.append("(").append(pageNo).append(")[").append(roundVal(Float.valueOf(text.getX()))).append(" : ").append(roundVal(Float.valueOf(text.getY()))).append("] ").append(tChar);
        is1stChar = false;
    }
	
	protected MatchWord createMatchWord(String coodinateText, String original) {
    	MatchWord matchWord = new MatchWord();
    	String sWord = "";
    	if (original != null) {
    		sWord = original;
    	} else {
    		sWord = coodinateText.substring(coodinateText.lastIndexOf(' ') + 1);
    	}
		String xCoor = coodinateText.substring(coodinateText.indexOf('[') + 1, coodinateText.indexOf(':'));
		String yCoor = coodinateText.substring(coodinateText.indexOf(':') + 1, coodinateText.indexOf(']'));
		String pageNo = coodinateText.substring(coodinateText.indexOf('(') + 1, coodinateText.indexOf(')'));
		matchWord.setPageNumber(Integer.valueOf(pageNo));
		matchWord.setText(sWord);
		matchWord.setXCoordinator(Float.valueOf(xCoor));
		matchWord.setYCoodinator(Float.valueOf(yCoor));
		return matchWord;
    }
	
	protected Double roundVal(Float yVal) {
        DecimalFormat rounded = new DecimalFormat("0.0'0'");
        return new Double(rounded.format(yVal));
    }
	
	protected boolean matchCharLine(TextPosition text) {
        Double yVal = roundVal(Float.valueOf(text.getYDirAdj()));
        if (yVal.doubleValue() == lastYVal) {
            return true;
        }
        lastYVal = yVal.doubleValue();
        endWord();
        return false;
    }
	
	protected void endWord() {
		String newWord = oneWord.toString().replaceAll("[^\\x00-\\x7F]", "");
		String sWord = newWord.substring(newWord.lastIndexOf(' ') + 1);
		boolean potentialMatch = false;
		if (!"".equals(sWord)) {
			for (String text : seekA) {
				if (text.indexOf(sequenceWords.toString() + sWord) != -1 && !text.equals(sWord)
						&& !wordsCoordinate.contains(newWord) || (sequenceWords.toString() + sWord).indexOf(text) != -1 
						&& !text.equals(sWord)
						&& !wordsCoordinate.contains(newWord)) {
					sequenceWords.append(sWord);
					wordsCoordinate.add(newWord);
					potentialMatch = true;
				}
				if (text.indexOf(sWord) != -1 && !text.equals(sWord) && !wordsCoordinate.contains(newWord)) {
					sequenceWords.delete(0, sequenceWords.length());
					wordsCoordinate.clear();
					sequenceWords.append(sWord);
					wordsCoordinate.add(newWord);
					potentialMatch = true;
				}
				if (text.equals(sWord)) {
					listMatchWords.add(createMatchWord(newWord, null));
					sequenceWords.delete(0, sequenceWords.length());
				} else if (sequenceWords.toString().lastIndexOf(text, 0) == 0) {
					listMatchWords.add(createMatchWord(wordsCoordinate.get(0), text));
					sequenceWords.delete(0, sequenceWords.length());
					wordsCoordinate.clear();
				}
			}
			if (!potentialMatch) {
				sequenceWords.delete(0, sequenceWords.length());
				wordsCoordinate.clear();
			}
		}
		oneWord.delete(0, oneWord.length());
		is1stChar = true;
	}
	
	public List<MatchWord> findCoordinateWord(File file, String searchText) throws IOException {
		this.seekA = searchText.split(",");
		PDDocument document = PDDocument.load(file);
		processPages(document.getDocumentCatalog().getPages());
		document.close();
		return listMatchWords;
	}

}
