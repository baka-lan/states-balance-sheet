package com.github.bakalan;

import java.util.Scanner;

public class Main {

    private static final String WELCOME_MESSAGE = """
        ------------------------------------
               Hello! And welcome to:
        ************************************
        
                States Balance Sheet
        
        ************************************
                                        v0.1
        ------------------------------------
        """;

    private static final String TERMINATE_MESSAGE = """
        ------------------------------------
        States Balance Sheet is terminating.
          Thank you for using our product.
              Please come back again!
        ------------------------------------
        """;

    public static void main(String[] args) {
        System.out.println(WELCOME_MESSAGE);

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("> "); // приглашение для ввода
            String input = scanner.nextLine();
            System.out.println("You entered: " + input);

            if (input == null) {
                continue;
            }

            String normalized = input.trim().toLowerCase();
            if (normalized.equals("q") ||
                normalized.equals("quit") ||
                normalized.equals("exit")) {
                System.out.println(TERMINATE_MESSAGE);
                break;
            }
        }

        scanner.close();
    }
}
