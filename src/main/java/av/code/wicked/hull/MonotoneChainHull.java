package av.code.wicked.hull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javafx.geometry.Point2D;

public class MonotoneChainHull {

    public List<HullStep> compute(List<Point2D> inputPoints) {
        List<Point2D> points = new ArrayList<>(inputPoints);
        List<HullStep> steps = new ArrayList<>();

        // Step 1: Sort the points by x-coordinate, resulting in a sequence p1, ..., pn.
        points.sort(Comparator.comparing(Point2D::getX).thenComparing(Point2D::getY));
        steps.add(step(1, HullAction.SORTED, points, List.of(), List.of(), "Points sorted by x then y"));

        if (points.size() <= 2) {
            steps.add(step(14, HullAction.FINALIZED, points, List.of(), List.of(), "Trivial hull"));
            return steps;
        }

        // Step 2: Put the points p1 and p2 in a list L_upper, with p1 as the first point.
        List<Point2D> upper = new ArrayList<>();
        upper.add(points.get(0));
        upper.add(points.get(1));
        steps.add(step(2, HullAction.UPPER_APPEND, points, upper, List.of(), "Initialize upper hull"));

        // Step 3-6: For i = 3 to n...
        for (int i = 2; i < points.size(); i++) {
            Point2D pi = points.get(i);
            // Step 4: Append pi to L_upper.
            upper.add(pi);
            steps.add(step(4, HullAction.UPPER_APPEND, points, upper, List.of(), "Append point to upper hull"));
            // Step 5-6: While L_upper contains more than two points and the last three points do not make right turn
            while (upper.size() > 2 && !isRightTurn(upper)) {
                Point2D removed = upper.remove(upper.size() - 2);
                steps.add(step(6, HullAction.UPPER_REDUCTION, points, upper, List.of(), "Remove middle point from upper hull: " + formatPoint(removed)));
            }
        }

        // Step 7: Put the points pn and p(n-1) in a list L_lower, with pn as the first point.
        List<Point2D> lower = new ArrayList<>();
        int n = points.size();
        lower.add(points.get(n - 1));
        lower.add(points.get(n - 2));
        steps.add(step(7, HullAction.LOWER_APPEND, points, upper, lower, "Initialize lower hull"));

        // Step 8-11: For i = n-2 down to 1...
        for (int i = n - 3; i >= 0; i--) {
            Point2D pi = points.get(i);
            // Step 9: Append pi to L_lower.
            lower.add(pi);
            steps.add(step(9, HullAction.LOWER_APPEND, points, upper, lower, "Append point to lower hull"));
            // Step 10-11: While not right turn remove middle.
            while (lower.size() > 2 && !isRightTurn(lower)) {
                Point2D removed = lower.remove(lower.size() - 2);
                steps.add(step(11, HullAction.LOWER_REDUCTION, points, upper, lower, "Remove middle point from lower hull: " + formatPoint(removed)));
            }
        }

        // Step 12: Remove duplicates from L_lower ends.
        if (!lower.isEmpty()) {
            lower.remove(0);
        }
        if (!lower.isEmpty()) {
            lower.remove(lower.size() - 1);
        }
        steps.add(step(12, HullAction.LOWER_REDUCTION, points, upper, lower, "Trim lower hull endpoints"));

        // Step 13: Append L_lower to L_upper and call the resulting list L.
        List<Point2D> hull = new ArrayList<>(upper);
        hull.addAll(lower);
        steps.add(step(13, HullAction.FINALIZED, hull, upper, lower, "Combine upper and lower hull"));

        // Step 14: Return L.
        steps.add(step(14, HullAction.FINALIZED, hull, upper, lower, "Convex hull ready"));
        return steps;
    }

    private HullStep step(int number, HullAction action, List<Point2D> sorted, List<Point2D> upper, List<Point2D> lower, String description) {
        return new HullStep(number, action, List.copyOf(upper), List.copyOf(lower), description, sorted.isEmpty() ? null : sorted.get(sorted.size() - 1));
    }

    private boolean isRightTurn(List<Point2D> hull) {
        int size = hull.size();
        Point2D a = hull.get(size - 3);
        Point2D b = hull.get(size - 2);
        Point2D c = hull.get(size - 1);
        double cross = cross(b.subtract(a), c.subtract(b));
        return cross <= 0;
    }

    private double cross(Point2D u, Point2D v) {
        return u.getX() * v.getY() - u.getY() * v.getX();
    }

    private String formatPoint(Point2D point) {
        return String.format("(%.1f, %.1f)", point.getX(), point.getY());
    }
}
