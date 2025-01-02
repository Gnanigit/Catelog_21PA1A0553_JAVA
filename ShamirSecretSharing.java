import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ShamirSecretSharing {

    public static Map<Integer, BigInteger> parseAndDecode(JSONObject testCase) {
        Map<Integer, BigInteger> roots = new HashMap<>();

        JSONObject keys = (JSONObject) testCase.get("keys");
        if (keys == null) {
            throw new IllegalArgumentException("JSON must contain a 'keys' object with 'n' and 'k'.");
        }

        Long nValue = (Long) keys.get("n");
        if (nValue == null) {
            throw new IllegalArgumentException("The 'keys' object must contain 'n'.");
        }

        int n = nValue.intValue(); // Number of roots provided

        for (int i = 1; i <= n; i++) {
            JSONObject root = (JSONObject) testCase.get(String.valueOf(i));

            if (root == null) {
                System.err.println("Warning: Root " + i + " is missing in the JSON file. Skipping...");
                continue;
            }

            try {
                int x = i; // Use the key (i) as x

                // Parse the base and value
                String baseStr = (String) root.get("base");
                String valueStr = (String) root.get("value");

                if (baseStr == null || valueStr == null) {
                    System.err.println("Warning: Missing base or value for root " + i + ". Skipping...");
                    continue;
                }

                int base = Integer.parseInt(baseStr); // Base of the y value
                BigInteger y = new BigInteger(valueStr, base); // Decode the y value

                roots.put(x, y); // Store (x, y) pair
            } catch (NumberFormatException e) {
                System.err.println("Error decoding root " + i + ": " + e.getMessage());
            }
        }

        return roots;
    }

    // Use Lagrange interpolation to find the constant term of the polynomial
    public static BigInteger findConstantTerm(Map<Integer, BigInteger> roots, int k) {
        List<Map.Entry<Integer, BigInteger>> points = new ArrayList<>(roots.entrySet());

        if (points.size() < k) {
            throw new IllegalArgumentException("Not enough roots to solve the polynomial. Expected at least " + k + " roots.");
        }

        BigInteger constant = BigInteger.ZERO;

        // Iterate over all points for Lagrange interpolation
        for (int i = 0; i < k; i++) {
            int xi = points.get(i).getKey();
            BigInteger yi = points.get(i).getValue();

            BigInteger term = yi; // Start with the y-value

            // Compute the product for the Lagrange basis polynomial
            for (int j = 0; j < k; j++) {
                if (i != j) {
                    int xj = points.get(j).getKey();
                    term = term.multiply(BigInteger.valueOf(-xj))
                               .divide(BigInteger.valueOf(xi - xj));
                }
            }

            constant = constant.add(term);
        }

        return constant;
    }

    public static void main(String[] args) {
        JSONParser parser = new JSONParser();

        try {
            // Read and parse JSON test cases
            JSONObject testCase1 = (JSONObject) parser.parse(new FileReader("testcase1.json"));
            JSONObject testCase2 = (JSONObject) parser.parse(new FileReader("testcase2.json"));

            // Parse and decode roots for both test cases
            Map<Integer, BigInteger> roots1 = parseAndDecode(testCase1);
            Map<Integer, BigInteger> roots2 = parseAndDecode(testCase2);

            // Extract k values for both test cases
            JSONObject keys1 = (JSONObject) testCase1.get("keys");
            JSONObject keys2 = (JSONObject) testCase2.get("keys");

            if (keys1 == null || keys2 == null) {
                throw new IllegalArgumentException("Both test cases must contain a 'keys' object.");
            }

            int k1 = ((Long) keys1.get("k")).intValue();
            int k2 = ((Long) keys2.get("k")).intValue();

            // Find the constant term (c) for both test cases
            BigInteger constant1 = findConstantTerm(roots1, k1);
            BigInteger constant2 = findConstantTerm(roots2, k2);

            // Print results
            System.out.println("Constant term for TestCase 1: " + constant1);
            System.out.println("Constant term for TestCase 2: " + constant2);

        } catch (IOException e) {
            System.err.println("Error reading JSON file: " + e.getMessage());
        } catch (ParseException e) {
            System.err.println("Error parsing JSON file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Input error: " + e.getMessage());
        } catch (ArithmeticException e) {
            System.err.println("Math error: " + e.getMessage());
        }
    }
}
