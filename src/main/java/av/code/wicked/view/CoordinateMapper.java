package av.code.wicked.view;

import java.util.Objects;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;

/**
 * Converts between JavaFX view coordinates (origin top-left) and
 * mathematical coordinates (origin bottom-left) while tracking the
 * current canvas dimensions.
 */
public final class CoordinateMapper {

    private final DoubleProperty width = new SimpleDoubleProperty();
    private final DoubleProperty height = new SimpleDoubleProperty();

    public void bindTo(ObservableValue<? extends Number> widthSource,
                       ObservableValue<? extends Number> heightSource) {
        width.unbind();
        height.unbind();
        width.bind(Objects.requireNonNull(widthSource, "widthSource"));
        height.bind(Objects.requireNonNull(heightSource, "heightSource"));
    }

    public Point2D toModel(Point2D viewPoint) {
        return toModel(viewPoint.getX(), viewPoint.getY());
    }

    public Point2D toModel(double viewX, double viewY) {
        return new Point2D(viewX, toModelY(viewY));
    }

    public Point2D toView(Point2D modelPoint) {
        return toView(modelPoint.getX(), modelPoint.getY());
    }

    public Point2D toView(double modelX, double modelY) {
        return new Point2D(modelX, toViewY(modelY));
    }

    public double toModelY(double viewY) {
        double h = height.get();
        return h <= 0 ? viewY : h - viewY;
    }

    public double toViewY(double modelY) {
        double h = height.get();
        return h <= 0 ? modelY : h - modelY;
    }

    public double getWidth() {
        return width.get();
    }

    public double getHeight() {
        return height.get();
    }

    public ReadOnlyDoubleProperty widthProperty() {
        return width;
    }

    public ReadOnlyDoubleProperty heightProperty() {
        return height;
    }
}
