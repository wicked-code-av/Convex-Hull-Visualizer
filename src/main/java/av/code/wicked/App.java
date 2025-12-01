package av.code.wicked;

import av.code.wicked.view.UIController;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        new UIController(primaryStage).launchUI();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
