/**
 * Implements common discrete Shannon Entropy functions.
 * Provides: univariate entropy H(X),
 * conditional entropy H(X|Y),
 * joint entropy H(X,Y).
 * Defaults to log_2, and so the entropy is calculated in bits.
 *
 * @author apocock
 */
public abstract class Entropy {
    public static double LOG_BASE = 2.0;

    private Entropy() {
    }

    /**
     * Calculates the univariate entropy H(X) from a vector.
     * Uses histograms to estimate the probability distributions, and thus the entropy.
     * The entropy is bounded 0 &#8804; H(X) &#8804; log |X|, where log |X| is the log of the number
     * of states in the random variable X.
     *
     * @param dataVector Input vector (X). It is discretised to the floor of each value before calculation.
     * @return The entropy H(X).
     */
    public static double calculateEntropy(double[] dataVector) {
        ProbabilityState state = new ProbabilityState(dataVector);

        double entropy = 0.0;
        for (Double prob : state.probMap.values()) {
            if (prob > 0) {
                entropy -= prob * Math.log(prob);
            }
        }

        entropy /= Math.log(LOG_BASE);

        return entropy;
    }//calculateEntropy(double [])


}//class Entropy
