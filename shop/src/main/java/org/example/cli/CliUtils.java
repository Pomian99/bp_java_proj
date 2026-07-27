package org.example.cli;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;

final class CliUtils {

    private CliUtils() {
    }

    static String formatPrice(BigDecimal price) {
        return price.setScale(2, RoundingMode.HALF_UP).toString();
    }

    static boolean askYesNo(Scanner scanner, String question) {
        while (true) {
            System.out.printf("%s (y/n): ", question);
            String input = scanner.nextLine().trim().toLowerCase();
            switch (input) {
                case "y", "yes" -> {
                    return true;
                }
                case "n", "no" -> {
                    return false;
                }
                default -> System.out.println("Please answer y or n.");
            }
        }
    }

    static String readNonBlank(Scanner scanner) {
        String input = scanner.nextLine().trim();
        while (input.isEmpty()) {
            System.out.print("This field cannot be empty, please enter a value: ");
            input = scanner.nextLine().trim();
        }
        return input;
    }
}
