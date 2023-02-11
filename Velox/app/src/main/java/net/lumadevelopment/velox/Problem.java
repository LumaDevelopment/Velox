package net.lumadevelopment.velox;

/**
 * Glorified tuple/record. Contains the problem text (ex.
 * "3 + 9") and the solution (ex. 12)
 */
public class Problem {

    private String problem;
    private int solution;

    public Problem(String problem, int solution) {
        this.problem = problem;
        this.solution = solution;
    }

    public int getSolution() {
        return solution;
    }

    @Override
    public String toString() {
        return problem;
    }

}
