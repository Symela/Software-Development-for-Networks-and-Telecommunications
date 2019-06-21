public class Experiment {
    private double[] feature_vector;
    private String name;

    public Experiment(double[] f_vector, String exp_name) {
        name = exp_name;
        feature_vector = new double[14];
        System.arraycopy(f_vector, 0, feature_vector, 0, 14);
    }

    public String get_name() {
        return name;
    }

    public double[] get_feature_vector() {
        return feature_vector;
    }
}
