package application.view;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import application.MainApp;
import application.model.FileInfo;
import application.model.FileList;
import application.util.Util;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

public class RootLayoutController {
	// 상단 검색 패널
	@FXML
	private Button searchButton;
	@FXML
	private TextField findFolder;
	@FXML
	private TextField findFile;
	@FXML
	private TextField findContent;
	@FXML
	private CheckBox caseSensitive;

	// 하단 좌측 테이블 뷰
	@FXML
	private TableView<FileList> listView;
	@FXML
	private TableColumn<FileList, String> fileName;

	// 하단 오측 테이블 뷰
	@FXML
	private TableView<FileInfo> tableView;
	@FXML
	private TableColumn<FileInfo, String> findLine;
	@FXML
	private TableColumn<FileInfo, String> findPattern;
	@FXML
	private TableColumn<FileInfo, String> findText;

	// 메인 애플리케이션 참조
	private MainApp mainApp;

	/**
	 * 생성자. initialize() 메서드 이전에 호출된다.
	 */
	public RootLayoutController() {
	}

	/**
	 * 컨트롤러 클래스를 초기화한다. fxml 파일이 로드되고 나서 자동으로 호출된다.
	 */
	@FXML
	private void initialize() {
		this.findFolder.setText("d:\\");
		this.findFile.setText("eula*");
		this.findContent.setText("MICROSOFT");
		fileName.setCellValueFactory(cellData -> cellData.getValue().fileNameProperty());

		findLine.setCellValueFactory(cellData -> cellData.getValue().findLineProperty());
		findPattern.setCellValueFactory(cellData -> cellData.getValue().findPatternProperty());
		findText.setCellValueFactory(cellData -> cellData.getValue().findTextProperty());
	}

	/**
	 * 초기화 기능.
	 * RootLayout.fxml
	 * 버튼 [초기화] ON MOUSE CLICKED => initText()
	 */
	@FXML
	private void initText() {
		this.findFolder.setText("c:\\");
		this.findFile.setText("*");
		this.findContent.setText("");
	}

	/**
	 * 검색 기능.
	 * RootLayout.fxml
	 * 버튼 [검색] ON MOUSE CLICKED => search()
	 */
	@FXML
	private void search() {
		System.out.println("findFolder Text :  [" + this.findFolder.getText()  + "]");
		System.out.println("findFile Text :    [" + this.findFile.getText()    + "]");
		System.out.println("findContent Text : [" + this.findContent.getText() + "]");

		String path = this.findFolder.getText();
		String fileText = this.findFile.getText();
		String[] fileNamePatterns = fileText.split(",");
		String contentText = this.findContent.getText();
		contentText = "*".equals(contentText) ? "" : contentText; 
		String[] contents = contentText.split(",");

		if ("".equals(path) || path == null) {
			Util.alertMessage(mainApp, "알림", "검색할 경로를 입력해주세요.", "(ex : D:\\ 또는  C:\\download)");
			return;
		}

		path = "".equals(path) ? "*" : path.replace("\\\\", "\\");
		fileText = "".equals(fileText) ? "*" : fileText;
		File dirFile = new File(path);
		ArrayList<File> subFiles= new ArrayList<File>();

		if(!dirFile.exists()) 
		{ 
			System.out.println("디렉토리가 존재하지 않습니다");
			return; 
		} 

		ObservableList<FileList> fileListArrays = FXCollections.observableArrayList();

		// 파일 패턴에 해당되는 목록을 가져온다.
		Util.findSubFiles(dirFile, subFiles, fileNamePatterns);

		// 파일 패턴에 해당되는 목록 중 검색어에 해당되는 줄번호, 텍스트를 찾는다.
		Util.findStrInSubFiles( subFiles, fileListArrays, contents, caseSensitive.isPressed(), mainApp);

		if (fileListArrays.size() > 0) {
			listView.setItems(fileListArrays);
		} else { 
			listView.setItems(null);
			Util.alertMessage(mainApp, "알림", "검색 결과가 없습니다.", "");
		}
		mainApp.setFileListData(fileListArrays);
		tableView.setItems(fileListArrays.size() > 0 ? (ObservableList<FileInfo>) mainApp.getFileListData().get(0).getFileInfoData() : null);
	}

	/**
	 * 검색 기능.
	 * RootLayout.fxml
	 * 버튼 [검색] ON MOUSE CLICKED => search()
	 */
	@FXML
	private void keyEventHandler(KeyEvent e) {
		if ("ENTER".equals(e.getCode().toString())) {
			search();
		}
	}

	/**
	 * 하단 우측 테이블 뷰에서 각 컬럼 선택 시 실행하는 이벤트
	 */
	@FXML
	public void setLabels() {
		if (listView.getSelectionModel().selectedIndexProperty().getValue() >= 0)
			tableView.setItems((ObservableList<FileInfo>) mainApp.getFileListData().get(listView.getSelectionModel().selectedIndexProperty().getValue()).getFileInfoData());
	}
	
	/**
	 * 참조를 다시 유지하기 위해 메인 애플리케이션이 호출한다.
	 *
	 * @param mainApp
	 */
	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}
/*
	public static void main(String[] args) {

		Pattern p = Pattern.compile("(?i).*\\.dll");
		Matcher m = p.matcher("install.res.1040.dll");
		
		System.out.println(m.find());
	}
*/
}
