package application;

import java.io.IOException;

import application.model.FileList;
import application.view.RootLayoutController;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainApp extends Application {

	private Stage primaryStage;
	private BorderPane rootLayout;
    /**
     * The data as an observable list of Persons.
     */
    private ObservableList<FileList> fileListData = FXCollections.observableArrayList();

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("파일 찾기");

        initRootLayout();

        //showPersonOverview();
	}

    /**
     * 상위 레이아웃을 초기화한다.
     */
    public void initRootLayout() {
        try {
            // fxml 파일에서 상위 레이아웃을 가져온다.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();

            // 메인 애플리케이션이 컨트롤러를 이용할 수 있게 한다.
            RootLayoutController controller = loader.getController();
            controller.setMainApp(this);

            // 상위 레이아웃을 포함하는 scene을 보여준다.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 생성자
     */
    public MainApp() {
        // 샘플 데이터를 추가한다
    }

    /**
     * 메인 스테이지를 반환한다.
     * @return
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

	public ObservableList<FileList> getFileListData() {
		return fileListData;
	}

	public void setFileListData(ObservableList<FileList> fileListData) {
		this.fileListData = fileListData;
	}

	public static void main(String[] args) {
		launch(args);
	}
}
