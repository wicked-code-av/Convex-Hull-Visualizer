package av.code.wicked;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class UIController {
    private final Stage stage;
    private final ObservableList<Point2D> points = FXCollections.observableArrayList();

    public UIController(Stage stage) {
        this.stage = stage;
    }

    public void init() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("MainView.fxml"));
            loader.setController(this);
            Parent root = loader.load();
            Scene scene = new Scene(root, 800, 600);
            stage.setTitle("Convex Hull");
            stage.setScene(scene);
            stage.show();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to initialize UI", ex);
        }
    }


    @FXML
    private Pane pointCanvas;

    @FXML
    private void initialize() {
        if (pointCanvas != null) {
            pointCanvas.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    Point2D point = new Point2D(event.getX(), event.getY());
                    points.add(point);
                    drawPoint(point);
                }
            });
        }
    }

    private void drawPoint(Point2D point) {
        Circle circle = new Circle(point.getX(), point.getY(), 4, Color.DODGERBLUE);
        circle.setStroke(Color.WHITE);
        circle.setStrokeWidth(1.5);
        Tooltip tooltip = new Tooltip(formatPoint(point));
        tooltip.setShowDelay(Duration.ZERO);
        tooltip.setHideDelay(Duration.millis(100));
        Tooltip.install(circle, tooltip);
        circle.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                points.remove(point);
                pointCanvas.getChildren().remove(circle);
                event.consume();
            }
        });
        pointCanvas.getChildren().add(circle);
    }

    private String formatPoint(Point2D point) {
        return String.format("(%.1f, %.1f)", point.getX(), point.getY());
    }
}
