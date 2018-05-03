package application.view;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import application.MainApp;
import application.model.FileInfo;
import application.model.FileList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

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
	 * TODO 검색 실행 예정
	 */
	@FXML
	private void search() {
		System.out.println("findFolder Text : "  + this.findFolder.getText());
		System.out.println("findFile Text : "    + this.findFile.getText());
		System.out.println("findContent Text : " + this.findContent.getText());

		String folder = this.findFolder.getText();
		String fileText = this.findFile.getText();
		String[] fileNamePatterns = fileText.split(",");
		String contentText = this.findContent.getText();
		String[] contents = contentText.split(",");

		folder = "".equals(folder) ? "*" : folder.replace("\\\\", "\\");
		fileText = "".equals(fileText) ? "*" : fileText;

	    ObservableList<FileList> fileListArrays = FXCollections.observableArrayList();

		String path = folder;
		File dirFile = new File(path);
		ArrayList<File> subFiles= new ArrayList<File>();
		//File[] fileList = dirFile.listFiles();
		
		if(!dirFile.exists()) 
		{ 
		    System.out.println("디렉토리가 존재하지 않습니다"); 
		    return; 
		} 

		findSubFiles(dirFile, subFiles, fileNamePatterns);

		for (File tempFile : subFiles) {
			if (tempFile.isFile()) {
				String tempPath = tempFile.getParent();
				String tempFileName = tempFile.getName();
				// System.out.println("Path=" + tempFile.getAbsolutePath());
				// System.out.println("Path=" + tempPath);
				// System.out.println("FileName=" + tempFileName);
				// System.out.println("FullFileName=" + tempPath + "\\" + tempFileName);

				// TODO 파일명은 \* 미처리
				Pattern txtFilePattern = Pattern.compile("(?i).+\\.(txt|jsp|asp|js|ini|properties|java|xml)$");
				Matcher txtFiles = txtFilePattern.matcher(tempFileName);

				int lineNumber = 1; // 행 번호
				boolean isFind = false;

				try {
					////////////////////////////////////////////////////////////////
					BufferedReader in = new BufferedReader(new FileReader(tempFile));
					String s;
					FileList tempFileList = new FileList();
				    ObservableList<FileInfo> list = FXCollections.observableArrayList();

					while ((s = in.readLine()) != null && txtFiles.find()) {
						for (String content : contents) {
							if("".equals(content)) {
								isFind = true;
								break;
							}
							String findStr = "(?i).*" + content.trim() + ".*";
							System.out.println(findStr);
							if (s.matches(findStr)) {
								System.out.format("%3d: %s%n", lineNumber, s);
								isFind = true;
								list.add(new FileInfo(lineNumber + "", s, content));
							}
						}

						lineNumber++; // 행 번호 증가
					}
					if (isFind) {
						tempFileList.setFileName(tempFile.getAbsolutePath());
						tempFileList.setFileInfoData(list);
						fileListArrays.add(tempFileList);
					}
					in.close();
					////////////////////////////////////////////////////////////////
				} catch (IOException e) {
					System.err.println(e); // 에러가 있다면 메시지 출력
					e.printStackTrace();
					System.exit(1);
				} catch (PatternSyntaxException e) { // 정규식에 에러가 있다면
					System.err.println(e);
					System.exit(1);
				}
			}
			/*** Do something withd tempPath and temp FileName ^^; ***/
		}

		//System.out.println(fileListArrays.toString());

		// 1.

		if (fileListArrays.size() > 0) {
			listView.setItems(fileListArrays);
		} else { 
			listView.setItems(null);
			alertMessage("알림", "검색 결과가 없습니다.", "");
		}
		mainApp.setFileListData(fileListArrays);
		tableView.setItems(fileListArrays.size() > 0 ? (ObservableList<FileInfo>) mainApp.getFileListData().get(0).getFileInfoData() : null);
	}

	@FXML
	public void setLabels() {
		tableView.setItems((ObservableList<FileInfo>) mainApp.getFileListData().get(listView.getSelectionModel().selectedIndexProperty().getValue()).getFileInfoData());
	}

	/**
	 * 알림 생성
	 * @param title		알림 타이틀명
	 * @param header	알림 헤더명
	 * @param content	알림 내용
	 */
	public void alertMessage(String title, String header, String content) {

		Alert alert = new Alert(AlertType.WARNING);
		alert.initOwner(mainApp.getPrimaryStage());
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		
		alert.showAndWait();
	}

	/**
	 * 하위 디렉토리 파일을 재귀호출
	 * @param parentFile		부모파일객체
	 * @param subFiles			하위 파일 목록
	 * @param fileNamePatterns	파일 탐지 패턴
	 */
	public static void findSubFiles(File parentFile, ArrayList<File> subFiles, String[] fileNamePatterns) {
		try {
			if (!parentFile.getName().equals("System Volume Information")) {
				boolean isFind = false;
				for (String patternString : fileNamePatterns) {
					if (patternString.equals("*")) {
						isFind = true;
						break;
					}

					Pattern p = Pattern.compile(patternString);
					Matcher m = p.matcher(parentFile.getName());
					if(!isFind && m.find()) isFind = true;
				}
				if (parentFile.isFile() && isFind) {
					subFiles.add(parentFile);
				} else if (parentFile.isDirectory()) {
					subFiles.add(parentFile);
					File[] childFiles = parentFile.listFiles();
					for (File childFile : childFiles) {
						findSubFiles(childFile, subFiles, fileNamePatterns);
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Error File : " + parentFile);
			System.out.println("Error fileNamePatterns : " + fileNamePatterns.toString());
			e.printStackTrace();
			//System.exit(-1);
		}
	}

	/**
	 * 참조를 다시 유지하기 위해 메인 애플리케이션이 호출한다.
	 *
	 * @param mainApp
	 */
	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;

	}
}
