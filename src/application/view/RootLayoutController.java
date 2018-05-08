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

import org.mozilla.universalchardet.UniversalDetector;

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

	// 파일 중 txt 아닌 파일을 거르기 위한 필터 패턴. (exe, class 등 바이너리 파일을 거르기 위해 사용. WhiteList)
	private final static String TEXT_FILE_FILTER_PATTERN = "(?i).*\\.(txt|jsp|asp|php|js|html|ini|properties|c|py|scala|java|xml|css|sh|bat|log)$";

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
			alertMessage("알림", "검색할 경로를 입력해주세요.", "(ex : D:\\ 또는  C:\\download)");
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
		findSubFiles(dirFile, subFiles, fileNamePatterns);

		// 파일 패턴에 해당되는 목록 중 검색어에 해당되는 줄번호, 텍스트를 찾는다.
		for (File tempFile : subFiles) {
			if (tempFile.isFile()) {
				String tempFileName = tempFile.getName();
				 System.out.println("Path=" + tempFile.getAbsolutePath());
				// System.out.println("Path=" + tempPath);
				// System.out.println("FileName=" + tempFileName);

				// 파일명은 \* 미처리
				Pattern txtFilePattern = Pattern.compile(TEXT_FILE_FILTER_PATTERN);
				Matcher txtFileFilter = txtFilePattern.matcher(tempFileName);

				int lineNumber = 1; // 행 번호
				boolean isFind = false;

				try {
					////////////////////////////////////////////////////////////////
					FileList tempFileList = new FileList();
					ObservableList<FileInfo> list = FXCollections.observableArrayList();
					if (txtFileFilter.find()) {
						// 해당 파일의 인코딩 방식을 가져온다.
						String textFileEncoding = findFileEncoding(tempFile);
						// 인코딩이 없을 시 다음 파일로 넘어간다.
						if(textFileEncoding != null) {
	
							BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(tempFile), textFileEncoding.replace("WINDOWS-1252", "MS949")));
							String s;
	
							while ((s = in.readLine()) != null) {
								for (String content : contents) {
	
									//content = new String(content.getBytes("utf-8"), textFileEncoding);
									content = content.trim();
	
									if("".equals(content)) {
										isFind = true;
										break;
									}
	
									String matchContent = content.replace("(", "\\(").replace(")", "\\)").replace("{", "\\{").replace("}", "\\}").replace("+", "\\+").replace("[", "\\[").replace("]", "\\]")
											  .replace("^", "\\^").replace("&", "\\$").replace("|", "\\|").replace("?", "\\?").replace("*", ".*");
									matchContent = matchContent.replace("\\.*", "\\*");
									/**
									 * 파일 내 검색어 패턴에 대해 두가지가 상충함.
									 * 1. * 처리. 현재 *를 .*로 치환하여 동작하게 한 상태.
									 * 2. \* 처리. *를 .*로 치환한 상태로 \.*를 \\*로 치환....?
									 * 띠용
									 * 실제 들어온 건에서 \.* 들어올 시 고려 안된 상태.
									 * 케릭터 배열로 변경하여 읽어 들인 후 새로운 문자열 만드는 방식으로 고려 중.
									 */
									String findStr = "(?i).*" + matchContent + ".*";
									System.out.println(findStr + "::" + textFileEncoding.replace("WINDOWS-1252", "MS949"));
									System.out.println(s);
									if ("*".equals(content.trim()) || s.matches(findStr)) {
										// System.out.format("%3d: %s%n", lineNumber, s);
										isFind = true;
										list.add(new FileInfo(lineNumber + "", content, s));
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
						}
					} else {
						tempFileList.setFileName(tempFile.getAbsolutePath());
						tempFileList.setFileInfoData(list);
						fileListArrays.add(tempFileList);
					}
					////////////////////////////////////////////////////////////////
				} catch (IOException e) {
					System.out.println("Error File : " + tempFile.getAbsolutePath());
					e.printStackTrace();
					alertMessage("알림", "파일 입출력 중 에러가 발생하였습니다.", "");
					return;
					//System.exit(1);
				} catch (PatternSyntaxException e) { // 정규식에 에러가 있다면
					System.out.println("Error File : " + tempFile.getAbsolutePath());
					System.out.println("Error contents : " + contents.toString());
					e.printStackTrace();
					alertMessage("알림", "정규식 비교 중 에러가 발생하였습니다.", "");
					return;
					//System.exit(1);
				}
			}
		}

		if (fileListArrays.size() > 0) {
			listView.setItems(fileListArrays);
		} else { 
			listView.setItems(null);
			alertMessage("알림", "검색 결과가 없습니다.", "");
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
					if(!patternString.equals("*")) {
						String compileString = "(?i)^" + patternString.replace("(", "\\(").replace(")", "\\)").replace("{", "\\{").replace("}", "\\}")
														.replace("+", "\\+").replace("[", "\\[").replace("]", "\\]").replace("^", "\\^").replace("&", "\\$")
														.replace("|", "\\|").replace("?", "\\?").replace(".", "\\.").replace("*", ".*") + "$";
						Pattern p = Pattern.compile(compileString);
						Matcher m = p.matcher(parentFile.getName());
						if(patternString.equals("*") || (!isFind && m.find())) {
							//System.out.println("FileName : " + parentFile.getName() + ", pattern : (?i)" + patternString.replace(".", "\\.").replace("*", ".*") + "$");
							isFind = true;
							break;
						}
					} else {
						isFind = true;
						break;
					}
				}
				if (parentFile.isFile() && isFind) {
					subFiles.add(parentFile);
				} else if (parentFile.isDirectory()) {
					//subFiles.add(parentFile);
					File[] childFiles = parentFile.listFiles();
					for (File childFile : childFiles) {
						findSubFiles(childFile, subFiles, fileNamePatterns);
					}
				}
			}
		} catch (Exception e) {
			System.out.println("---------- findSubFiles Error --------");
			System.out.println("Error File : " + parentFile);
			System.out.println("Error Pattern : " + fileNamePatterns.toString());
			e.printStackTrace();
			//System.exit(-1);
		} 
	}

	public static String findFileEncoding(File file) {
		String encoding = null;
		try {
			byte[] buf = new byte[4096];
			java.io.FileInputStream fis = new java.io.FileInputStream(file);
	
			// (1)
			UniversalDetector detector = new UniversalDetector(null);
	
			// (2)
			int nread;
			while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
				detector.handleData(buf, 0, nread);
			}
			// (3)
			detector.dataEnd();
	
			// (4)
			encoding = detector.getDetectedCharset();
			/*
			if (encoding != null) {
				System.out.println("Detected encoding = " + encoding);
			} else {
				System.out.println("No encoding detected.");
			}*/
	
			// (5)
			detector.reset();
			
			fis.close();
		
		} catch(IOException e) {
			e.printStackTrace();
		}

		return encoding;
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
