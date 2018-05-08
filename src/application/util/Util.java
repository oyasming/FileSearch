package application.util;

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

import application.FileSearchConstant;
import application.MainApp;
import application.model.FileInfo;
import application.model.FileList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class Util {

	/**
	 * 입력된 탐지파일에 따른 파일 중 검색어가 포함된 파일을 찾는다.
	 * @param subFiles			탐지된 파일 목록
	 * @param fileListArrays	리스트에 추가될 파일 목록
	 * @param contents			검색어 배열
	 * @param caseSensitive		검색어 대소문자 체크여부. (true : 구분, false : 미 구분)
	 * @param mainApp			알람 발생 주체
	 */
	public static void findStrInSubFiles(ArrayList<File> subFiles, ObservableList<FileList> fileListArrays, String[] contents, Boolean caseSensitive, MainApp mainApp) {

		// 파일 패턴에 해당되는 목록 중 검색어에 해당되는 줄번호, 텍스트를 찾는다.
		for (File tempFile : subFiles) {
			if (tempFile.isFile()) {
				String tempFileName = tempFile.getName();
				 System.out.println("Path=" + tempFile.getAbsolutePath());
				// System.out.println("Path=" + tempPath);
				// System.out.println("FileName=" + tempFileName);

				// 파일명은 \* 미처리
				Pattern txtFilePattern = Pattern.compile(FileSearchConstant.TEXT_FILE_FILTER_PATTERN);
				Matcher txtFileFilter = txtFilePattern.matcher(tempFileName);

				int lineNumber = 1; // 행 번호
				boolean isFind = false;

				try {
					////////////////////////////////////////////////////////////////
					FileList tempFileList = new FileList();
					ObservableList<FileInfo> list = FXCollections.observableArrayList();
					if (txtFileFilter.find()) {
						// 해당 파일의 인코딩 방식을 가져온다.
						String textFileEncoding = Util.findFileEncoding(tempFile);
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
									String findStr = caseSensitive ? ".*" : "(?i).*" + matchContent + ".*";
									//System.out.println("FindStr :: " + findStr + ", Encoding ::" + textFileEncoding.replace("WINDOWS-1252", "MS949"));
									//System.out.println("LineText :: "+s);
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
					alertMessage(mainApp, "알림", "파일 입출력 중 에러가 발생하였습니다.", "");
					return;
					//System.exit(1);
				} catch (PatternSyntaxException e) { // 정규식에 에러가 있다면
					System.out.println("Error File : " + tempFile.getAbsolutePath());
					System.out.println("Error contents : " + contents.toString());
					e.printStackTrace();
					alertMessage(mainApp, "알림", "정규식 비교 중 에러가 발생하였습니다.", "");
					return;
					//System.exit(1);
				}
			}
		}
	}

	/**
	 * 알림 생성
	 * @param mainApp	알람 발생 주체
	 * @param title		알림 타이틀명
	 * @param header	알림 헤더명
	 * @param content	알림 내용
	 */
	public static void alertMessage(MainApp mainApp, String title, String header, String content) {

		Alert alert = new Alert(AlertType.WARNING);
		alert.initOwner(mainApp.getPrimaryStage());
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);

		alert.showAndWait();
	}

	/**
	 * 파일 인코딩 가져오기
	 * @param file		파일객체
	 */
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
}
