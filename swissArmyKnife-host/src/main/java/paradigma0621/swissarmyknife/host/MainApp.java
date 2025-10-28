package paradigma0621.swissarmyknife.host;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/main_view.fxml"));
        Parent root = loader.load();

        stage.setTitle("SwissArmyKnife — Host + Plugins (Lazy)");
        stage.setScene(new Scene(root, 1000, 650));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
