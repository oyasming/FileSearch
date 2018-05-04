package application.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class FileInfo {

	// 검색어를 찾은 줄 번호
	private final StringProperty findLine;
	// 검색어
	private final StringProperty findPattern;
	// 찾은 텍스트
	private final StringProperty findText;

	/**
	 * Default constructor.
	 */
	public FileInfo() {
		this(null, null, null);
	}

	/**
	 * Constructor with some initial data.
	 * 
	 * @param findLine
	 * @param findPattern
	 * @param findText
	 */
	public FileInfo(String findLine, String findPattern, String findText) {
		this.findLine = new SimpleStringProperty(findLine);
		this.findPattern = new SimpleStringProperty(findPattern);
		this.findText = new SimpleStringProperty(findText);
	}

	public String getFindLine() {
		return findLine.get();
	}

	public void setFindLine(String findLine) {
		this.findLine.set(findLine);
	}

	public StringProperty findLineProperty() {
		return findLine;
	}

	public String getFindPattern() {
		return findPattern.get();
	}

	public void setFindPattern(String findPattern) {
		this.findPattern.set(findPattern);
	}

	public StringProperty findPatternProperty() {
		return findPattern;
	}

	public String getFindText() {
		return findText.get();
	}

	public void setFindText(String findText) {
		this.findText.set(findText);
	}

	public StringProperty findTextProperty() {
		return findText;
	}
}
