package av.code.wicked;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import javafx.geometry.Point2D;

/**
 * Utility class for generating random points within a rectangular area.
 */
public final class RandomPointGenerator {

    private final Random random;

    public RandomPointGenerator() {
        this(new SecureRandom());
    }

    public RandomPointGenerator(Random random) {
        this.random = Objects.requireNonNull(random, "random");
    }

    public List<Point2D> generatePoints(int count, double width, double height, double padding) {
        if (count <= 0) {
            return List.of();
        }
        double usableWidth = Math.max(width - padding * 2, 0);
        double usableHeight = Math.max(height - padding * 2, 0);
        if (usableWidth == 0 || usableHeight == 0) {
            return List.of();
        }
        List<Point2D> points = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            double x = padding + random.nextDouble() * usableWidth;
            double y = padding + random.nextDouble() * usableHeight;
            points.add(new Point2D(x, y));
        }
        return points;
    }
}

