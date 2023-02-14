package net.lumadevelopment.velox;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Mathematically determines viable problems, generates them, and
 * packages them into Problem objects.
 */
public class ProblemGenerator {

    public static final String LOG_TAG = ProblemGenerator.class.getSimpleName();

    private final Random random;

    /**
     * Solutions should never go beyond this number.
     */
    private final int maxNumber;

    /**
     * All non-prime numbers that are less than maxNumber.
     *
     * We use these values often, so we store them so we don't
     * have to recalculate them every time.
     */
    private final List<Integer> nonPrimeSubMaxNums;

    public ProblemGenerator() {

        this.random = new Random();
        this.maxNumber = Config.MAX_NUMBER;

        // Assigned via function
        this.nonPrimeSubMaxNums = getNonPrimeNumbers(maxNumber);

    }

    /**
     * Randomly chooses what type of problem to generate and returns
     * a Problem of that type.
     * @return Randomly generated problem.
     */
    public Problem generateProblem() {

        int typeOfProblem = random.nextInt(4);

        switch (typeOfProblem) {
            case 0:
                return randomAdditionProblem();
            case 1:
                return randomSubtractionProblem();
            case 2:
                return randomDivisionProblem();
            case 3:
                return randomMultiplicationProblem();
            default:
                // How did we get here?
                Log.e(LOG_TAG, "Generated something other than 0, 1, 2, or 3 in generateProblem()");
                return null;
        }

    }

    /**
     * Creates a random addition problem (x + y = z) where
     * z <= maxNumber and (x, y, and z) > 0
     * @return Random addition problem
     */
    private Problem randomAdditionProblem() {

        // If maxNumber = 30, numberOne could be any number
        // between 1 (inclusive) and 29 (inclusive)
        int numberOne = random.nextInt(maxNumber - 1) + 1;

        // If numberOne = 15 and maxNumber = 30, numberTwo could
        // be any number between 1 (inclusive) and 15 (inclusive)
        int numberTwo = random.nextInt(maxNumber - numberOne) + 1;

        String equationText = numberOne + " + " + numberTwo;
        int solution = numberOne + numberTwo;

        return new Problem(equationText, solution);

    }

    /**
     * Creates a random subtraction problem (x - y = z) where
     * x <= maxNumber and (x, y, and z) > 0
     * @return A random subtraction problem
     */
    private Problem randomSubtractionProblem() {

        // If maxNumber = 30, numberOne could be any number
        // between 2 (inclusive) and 30 (inclusive)
        int numberOne = random.nextInt(maxNumber - 1) + 2;

        // If numberOne = 15, numberTwo could be any number
        // between 1 (inclusive) and 14 (inclusive)
        int numberTwo = random.nextInt(numberOne - 1) + 1;

        String equationText = numberOne + " - " + numberTwo;
        int solution = numberOne - numberTwo;

        return new Problem(equationText, solution);

    }

    /**
     * Creates a random division problem (x / y = z) where x is
     * not prime, x <= maxNumber, y != 1, and x % y = 0
     * @return A random division problem
     */
    private Problem randomDivisionProblem() {

        // Get a random non-prime number that caps out at maxNumber
        int randomIndex = random.nextInt(nonPrimeSubMaxNums.size());
        int numberOne = nonPrimeSubMaxNums.get(randomIndex);

        // Get a random non-1 number that evenly divides numberOne
        List<Integer> factorsOfNumOne = getFactorsOfNumber(numberOne);
        randomIndex = random.nextInt(factorsOfNumOne.size());
        int numberTwo = factorsOfNumOne.get(randomIndex);

        String equation = numberOne + " / " + numberTwo;
        int solution = numberOne / numberTwo;

        return new Problem(equation, solution);

    }

    /**
     * Creates a random multiplication problem (x * y = z) where
     * x <= (maxNumber / 2), y <= (maxNumber / x), and z <= maxNumber
     * @return A random multiplication problem
     */
    private Problem randomMultiplicationProblem() {

        // If maxNumber = 30, generates a number between
        // 2 (inclusive) and 15 (inclusive)
        int numberOne = random.nextInt((maxNumber / 2) - 1) + 2;

        // If maxNumber = 30 and numberOne = 7, generates a
        // number between 2 (inclusive) and 4 (inclusive)
        int numberTwo = random.nextInt((maxNumber / numberOne) - 1) + 2;

        String equation = numberOne + " * " + numberTwo;
        int solution = numberOne * numberTwo;

        return new Problem(equation, solution);

    }

    /**
     * Returns whether or not the parameter is prime.
     * @param number Number to check if prime or not.
     * @return Whether or not the parameter is prime.
     */
    private boolean isPrime(int number) {

        // Loop from 2 (inclusive) to number/2 (inclusive)
        for (int i = 2; i <= (number / 2); i++) {

            if ((number % i) == 0) {

                // If the number is evenly divisible by a number
                // that is not itself or 1, then it is not prime.
                return false;

            }
        }

        // Number is prime
        return true;

    }

    /**
     * Get all numbers that are not prime from 2 (inclusive) to max (inclusive)
     * @param max The stopping point for the non prime number search.
     * @return An Integer list of all non-prime numbers in the defined range.
     */
    private List<Integer> getNonPrimeNumbers(int max) {

        List<Integer> nonPrimes = new ArrayList<>();

        for (int i = 2; i <= max; i++) {

            // Use the isPrime() function to check if
            // i is prime or not
            if (!isPrime(i)) {
                nonPrimes.add(i);
            }

        }

        return nonPrimes;

    }

    /**
     * Get all numbers that evenly divide the parameter that
     * are not 1 or itself. Basically, every number that would
     * make the parameter not prime.
     * @param number The number we are checking for factors of
     * @return An Integer list of all factors of the parameter
     */
    private List<Integer> getFactorsOfNumber(int number) {

        List<Integer> factors = new ArrayList<>();

        for (int i = 2; i < number; i++) {

            // if the modulus is 0, the number divides evenly
            // this means we add it to the list
            if (number % i == 0) {
                factors.add(i);
            }

        }

        return factors;

    }

}
