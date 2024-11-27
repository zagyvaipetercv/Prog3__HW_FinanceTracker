package financetracker.utilities;

public class CustomMath {
    private static final double DEFAULT_EPS = 0.001;

    private CustomMath() {
    }

    /**
     * Checks if 2 doubles are almost the same
     * 
     * @param a   first double value
     * @param b   second double value
     * @param eps margin of error
     * @return true if a and b are equal within the margin of error, false if not
     */
    public static boolean almostEquals(double a, double b, double eps) {
        return Math.abs(a - b) < eps;
    }

    /**
     * Checks if 2 doubles are almost the same
     * 
     * @param a   first double value
     * @param b   second double value
     * @param eps margin of error
     * @return true if a and b are equal within a margin of error of 0.001, false if
     *         not
     */
    public static boolean almostEquals(double a, double b) {
        return almostEquals(a, b, DEFAULT_EPS);
    }

}
