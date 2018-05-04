package application.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class FileList {

	// 파일명
	private StringProperty fileName;
	// 파일 상세 데이터 리스트
	private ObservableList<FileInfo> fileInfoData;

	/**
	 * Default constructor.
	 */
	public FileList() {
		this(null, null);
	}

	/**
	 * Constructor with some initial data.
	 * 
	 * @param fileName 파일명
	 * @param fileInfoData 파일 상세 데이터 목록
	 */
	public FileList(String fileName, ObservableList<FileInfo> fileInfoData) {
		this.fileName = new SimpleStringProperty(fileName);
		this.fileInfoData = FXCollections.observableArrayList();
		this.fileInfoData = fileInfoData;
	}

	public String getFileName() {
		return fileName.get();
	}
	public void setFileName(String fileName) {
		this.fileName.set(fileName);
	}
	public StringProperty fileNameProperty() {
		return fileName;
	}
	public ObservableList<FileInfo> getFileInfoData() {
		return fileInfoData;
	}
	public void setFileInfoData(ObservableList<FileInfo> fileInfoData) {
		this.fileInfoData = fileInfoData;
	}

}
