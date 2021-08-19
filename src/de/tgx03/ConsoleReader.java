package de.tgx03;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * A class for reading inputs from the console
 */
public final class ConsoleReader {

    private static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    private ConsoleReader() {
        throw new IllegalAccessError("Cannot be instantiated");
    }

    /**
     * Reads and returns the next string typed on the console
     *
     * @return The typed string
     */
    public static String readLine() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            return "";
        }
    }

    /**
     * Gets the input from the console and splits it by its spaces
     *
     * @return The input split by spaces
     */
    public static String[] getSplitInput() {
        return getSplitInput(" ", false);
    }

    /**
     * Splits the next input typed on the console by a provided string
     *
     * @param splitter The string to split the input by
     * @return The input split by the provided string
     */
    public static String[] getSplitInput(String splitter) {
        return getSplitInput(splitter, false);
    }

    /**
     * Returns the input by the user split by a provided splitter.
     * It's also possible to catch trailing symbols if wished. Under normal cirumstances, that isn't needed,
     * but during some special assignments it was required to not allow input where the command was followed up
     * by lots of spaces without anything after them
     *
     * @param splitter      The string to split the input by
     * @param catchTrailing Whether to catch trailing strings
     * @return The split input
     */
    public static String[] getSplitInput(String splitter, boolean catchTrailing) {
        try {
            String input = reader.readLine();
            if (catchTrailing) {
                String ending = "/>";
                String alternateEnding = "}";
                if (splitter.equals(ending) || splitter.equals(ending.substring(0, 0)) || splitter.equals(ending.substring(1, 1))) {
                    input = input + splitter + alternateEnding;
                } else {
                    input = input + splitter + ending;
                }
                String[] splitInput = input.split(splitter);
                String[] result = new String[splitInput.length - 1];
                System.arraycopy(splitInput, 0, result, 0, result.length);
                return result;
            } else {
                return input.split(splitter);
            }
        } catch (IOException e) {
            return new String[0];
        }
    }
}
