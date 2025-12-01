package av.code.wicked.view;

import java.util.Objects;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

/**
 * Lightweight ruler-style overlay that renders border axes, ticks, and labels
 * around the drawing canvas.
 */
public final class AxisOverlay extends Pane {

    private static final double TICK_LENGTH = 8;
    private static final double LABEL_OFFSET = 4;
    private static final double MIN_PIXEL_STEP = 70;

    private final Group graphics = new Group();
    private final ChangeListener<Number> sizeListener = (obs, oldVal, newVal) -> redraw();

    private CoordinateMapper mapper;

    public AxisOverlay() {
        setPickOnBounds(false);
        setMouseTransparent(true);
        getChildren().add(graphics);
        widthProperty().addListener(sizeListener);
        heightProperty().addListener(sizeListener);
    }

    public void setCoordinateMapper(CoordinateMapper mapper) {
        if (this.mapper != null) {
            this.mapper.widthProperty().removeListener(sizeListener);
            this.mapper.heightProperty().removeListener(sizeListener);
        }
        this.mapper = Objects.requireNonNull(mapper, "mapper");
        this.mapper.widthProperty().addListener(sizeListener);
        this.mapper.heightProperty().addListener(sizeListener);
        redraw();
    }

    private void redraw() {
        graphics.getChildren().clear();
        double width = getWidth();
        double height = getHeight();
        if (width <= 0 || height <= 0 || mapper == null) {
            return;
        }
        drawBorder(width, height);
        drawHorizontalRuler(width, height);
        drawVerticalRuler(width, height);
    }

    private void drawBorder(double width, double height) {
        Line top = new Line(0, 0, width, 0);
        Line bottom = new Line(0, height, width, height);
        Line left = new Line(0, 0, 0, height);
        Line right = new Line(width, 0, width, height);
        top.setStroke(Color.GRAY);
        bottom.setStroke(Color.GRAY);
        left.setStroke(Color.GRAY);
        right.setStroke(Color.GRAY);
        graphics.getChildren().addAll(top, bottom, left, right);
    }

    private void drawHorizontalRuler(double width, double height) {
        double step = computeStep(width);
        for (double value = 0; value <= width + 0.5; value += step) {
            double x = Math.min(value, width);
            Line bottomTick = new Line(x, height, x, height - TICK_LENGTH);
            Line topTick = new Line(x, 0, x, TICK_LENGTH);
            bottomTick.setStroke(Color.DARKGRAY);
            topTick.setStroke(Color.DARKGRAY);
            graphics.getChildren().addAll(bottomTick, topTick);
            addLabel(formatValue(x), x - 12, height - TICK_LENGTH - LABEL_OFFSET, true);
        }
    }

    private void drawVerticalRuler(double width, double height) {
        double step = computeStep(height);
        for (double offset = 0; offset <= height + 0.5; offset += step) {
            double y = Math.min(height - offset, height);
            Line leftTick = new Line(0, y, TICK_LENGTH, y);
            Line rightTick = new Line(width - TICK_LENGTH, y, width, y);
            leftTick.setStroke(Color.DARKGRAY);
            rightTick.setStroke(Color.DARKGRAY);
            graphics.getChildren().addAll(leftTick, rightTick);
            double value = mapper.toModelY(y);
            addLabel(formatValue(value), LABEL_OFFSET, y - 10, false);
        }
    }

    private double computeStep(double span) {
        double rawStep = Math.max(MIN_PIXEL_STEP, span / 8d);
        double magnitude = Math.pow(10, Math.floor(Math.log10(rawStep)));
        double normalized = rawStep / magnitude;
        double nice;
        if (normalized < 1.5) {
            nice = 1;
        } else if (normalized < 3) {
            nice = 2;
        } else if (normalized < 7) {
            nice = 5;
        } else {
            nice = 10;
        }
        return nice * magnitude;
    }

    private void addLabel(String text, double x, double y, boolean bottomAxis) {
        Label label = new Label(text);
        label.setPadding(Insets.EMPTY);
        label.setTextFill(Color.DARKGRAY);
        label.setLayoutX(x);
        label.setLayoutY(bottomAxis ? Math.min(getHeight() - 18, y) : Math.max(2, y));
        graphics.getChildren().add(label);
    }

    private String formatValue(double value) {
        double rounded = Math.rint(value * 10) / 10;
        if (Math.abs(rounded - Math.rint(rounded)) < 1e-4) {
            return String.format("%.0f", rounded);
        }
        return String.format("%.1f", rounded);
    }
}

