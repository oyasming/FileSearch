package application;

public class FileSearchConstant {

	/**
	 *  파일 중 txt 아닌 파일을 거르기 위한 필터 패턴. (exe, class 등 바이너리 파일을 거르기 위해 사용. WhiteList)
	 */
	public final static String TEXT_FILE_FILTER_PATTERN = "(?i).*\\.(txt|jsp|asp|php|js|html|ini|properties|c|py|scala|java|xml|css|sh|bat|log)$";
}
