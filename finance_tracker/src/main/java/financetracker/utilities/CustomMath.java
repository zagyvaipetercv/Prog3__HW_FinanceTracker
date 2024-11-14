package financetracker.utilities;

public class CustomMath {
    private static final double DEFAULT_EPS = 0.001;

    private CustomMath() {
    }

    public static boolean almostEquals(double a, double b, double eps) {
        return Math.abs(a - b) < eps;
    }

    public static boolean almostEquals(double a, double b) {
        return almostEquals(a, b, DEFAULT_EPS);
    }

}
