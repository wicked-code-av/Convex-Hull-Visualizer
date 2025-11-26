package av.code.wicked;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class UIController {
    private static final int RANDOM_POINT_COUNT = 25;
    private static final double POINT_RADIUS = 4.0;

    private final Stage stage;
    private final ObservableList<Point2D> points = FXCollections.observableArrayList();
    private final RandomPointGenerator pointGenerator = new RandomPointGenerator();

    @FXML private Pane pointCanvas;
    @FXML private Button clearButton;
    @FXML private Button randomPointsButton;
    @FXML private Button computeButton;

    public UIController(Stage stage) {
        this.stage = stage;
    }

    public void launchUI() {
        try {
            Parent root = loadView();
            Scene scene = buildScene(root);
            showStage(scene);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to initialize UI", ex);
        }
    }

    private Parent loadView() throws java.io.IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("MainView.fxml"));
        loader.setController(this);
        return loader.load();
    }

    private Scene buildScene(Parent root) {
        return new Scene(root, 800, 600);
    }

    private void showStage(Scene scene) {
        stage.setTitle("Convex Hull");
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void initialize() {
        configureCanvasInteraction();
        configureButtons();
    }

    private void configureCanvasInteraction() {
        if (pointCanvas == null) {
            return;
        }
        pointCanvas.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                addPoint(event.getX(), event.getY());
            }
        });
    }

    private void configureButtons() {
        if (clearButton != null) {
            clearButton.setOnAction(event -> clearAllPoints());
        }
        if (randomPointsButton != null) {
            randomPointsButton.setOnAction(event -> populateWithRandomPoints());
        }
        // computeButton reserved for future hull computation wiring.
    }

    private void addPoint(double x, double y) {
        Point2D point = new Point2D(x, y);
        points.add(point);
        drawPoint(point);
    }

    private void addPoints(List<Point2D> newPoints) {
        newPoints.forEach(point -> {
            points.add(point);
            drawPoint(point);
        });
    }

    private void clearAllPoints() {
        points.clear();
        pointCanvas.getChildren().clear();
    }

    private void populateWithRandomPoints() {
        double width = resolveCanvasDimension(pointCanvas.getWidth(), pointCanvas.getScene() != null ? pointCanvas.getScene().getWidth() : 0);
        double height = resolveCanvasDimension(pointCanvas.getHeight(), pointCanvas.getScene() != null ? pointCanvas.getScene().getHeight() : 0);
        List<Point2D> generated = pointGenerator.generatePoints(RANDOM_POINT_COUNT, width, height, POINT_RADIUS);
        addPoints(generated);
    }

    private double resolveCanvasDimension(double paneExtent, double fallbackExtent) {
        return paneExtent > 0 ? paneExtent : fallbackExtent;
    }

    private void drawPoint(Point2D point) {
        Circle circle = new Circle(point.getX(), point.getY(), POINT_RADIUS, Color.DODGERBLUE);
        circle.setStroke(Color.WHITE);
        circle.setStrokeWidth(1.5);
        attachTooltip(circle, point);
        enableRemoval(circle, point);
        pointCanvas.getChildren().add(circle);
    }

    private void attachTooltip(Circle circle, Point2D point) {
        Tooltip tooltip = new Tooltip(formatPoint(point));
        tooltip.setShowDelay(Duration.ZERO);
        tooltip.setHideDelay(Duration.millis(100));
        Tooltip.install(circle, tooltip);
    }

    private void enableRemoval(Circle circle, Point2D point) {
        circle.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                points.remove(point);
                pointCanvas.getChildren().remove(circle);
                event.consume();
            }
        });
    }

    private String formatPoint(Point2D point) {
        return String.format("(%.1f, %.1f)", point.getX(), point.getY());
    }
}
