package com.github.bakalan;

import java.util.*;

public class CommandProcessor {

    public enum CommandResult {
        EXIT, UNKNOWN, CONTINUE
    }

    private final Map<String, Integer> states = new HashMap<>();
    private final List<String> stateOrder = new ArrayList<>();

    public CommandResult process(String input) {
        if (input == null || input.isBlank()) {
            return CommandResult.UNKNOWN;
        }

        String normalized = input.trim().toUpperCase();
        String[] parts = normalized.split("\\s+");

        if (parts.length == 0) {
            return CommandResult.UNKNOWN;
        }

        if (isExitCommand(normalized)) {
            return CommandResult.EXIT;
        }

        if (normalized.startsWith("ADD ")) {
            handleAddCommand(parts);
            return CommandResult.CONTINUE;
        }

        // новый блок: проверяем, что это похоже на stateCommand
        String possibleState = parts[0];
        if (states.containsKey(possibleState)) {
            handleStateCommand(possibleState, parts);
            return CommandResult.CONTINUE;
        }

        return CommandResult.UNKNOWN;
    }

    private boolean isExitCommand(String command) {
        return command.equals("Q")
            || command.equals("QUIT")
            || command.equals("EXIT");
    }

    private void handleAddCommand(String[] parts) {
        // ожидаем строку вида: ADD STATE BALANCE
        if (parts.length != 3) {
            System.out.println("Invalid ADD command. Usage: add {stateName} {stateBalance}");
            return;
        }

        String stateName = parts[1];
        String balanceStr = parts[2];

        try {
            int balance = Integer.parseInt(balanceStr);
            if (states.containsKey(stateName)) {
                System.out.println("State \"" + stateName + "\" already exists. Nothing changes.");
            } else {
                states.put(stateName, balance);
                stateOrder.add(stateName); // сохраняем порядок добавления
                System.out.println("New state \"" + stateName + "\" added");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid balance format. It must be a positive integer.");
        }
    }

    private void handleStateCommand(String stateName, String[] parts) {
        if (parts.length < 3) {
            System.out.println("Invalid state command format");
            return;
        }

        String action = parts[1];
        switch (action) {
            case "PAY" -> handlePayCommand(stateName, parts);
            case "REC" -> handleRecCommand(stateName, parts);
            default -> System.out.println("Unknown action for state: " + action);
        }
    }

    private void handlePayCommand(String fromState, String[] parts) {
        if (parts.length < 3) {
            System.out.println("Invalid PAY command. Usage: {state1} PAY {amount} {state2?}");
            return;
        }

        try {
            int amount = Integer.parseInt(parts[2]);
            if (amount <= 0) {
                System.out.println("Amount must be positive");
                return;
            }

            if (parts.length == 4) {
                String toState = parts[3];
                if (!states.containsKey(toState)) {
                    System.out.println("Target state not found: " + toState);
                    return;
                }
                // перевод
                states.put(fromState, states.get(fromState) - amount);
                states.put(toState, states.get(toState) + amount);
                System.out.printf("%s paid %d to %s%n", fromState, amount, toState);
            } else {
                // списание
                states.put(fromState, states.get(fromState) - amount);
                System.out.printf("%s lost %d%n", fromState, amount);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount format: " + parts[2]);
        }
    }

    private void handleRecCommand(String stateName, String[] parts) {
        if (parts.length != 3) {
            System.out.println("Invalid REC command. Usage: {state} REC {amount}");
            return;
        }

        try {
            int amount = Integer.parseInt(parts[2]);
            if (amount <= 0) {
                System.out.println("Amount must be positive");
                return;
            }

            states.put(stateName, states.get(stateName) + amount);
            System.out.printf("%s received %d%n", stateName, amount);
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount format: " + parts[2]);
        }
    }

    public void printStatesTable() {
        System.out.println("""
                ------------------------------------
                         current condition:
                ************************************
                        States Balance Sheet
                ************************************
                   Name        |   Balance""");

        for (String stateName : stateOrder) {
            Integer balance = states.get(stateName);
            System.out.printf("    %-10s |    %5d%n", stateName, balance);
        }

        int sum = states.values().stream().mapToInt(value -> value).sum();
        System.out.println("====================================");
        System.out.printf("    %-10s |    %5d%n", "SUM", sum);

        System.out.println("""
                ************************************
                                                v0.1
                ------------------------------------""");
    }
}
