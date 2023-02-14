package net.lumadevelopment.velox;

import androidx.annotation.NonNull;

/**
 * Glorified tuple/record. Contains the problem text (ex.
 * "3 + 9") and the solution (ex. 12)
 */
public class Problem {

    private final String problem;
    private final int solution;

    public Problem(String problem, int solution) {
        this.problem = problem;
        this.solution = solution;
    }

    public int getSolution() {
        return solution;
    }

    @NonNull
    @Override
    public String toString() {
        return problem;
    }

}
