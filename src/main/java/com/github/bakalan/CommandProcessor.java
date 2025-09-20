package com.github.bakalan;

import java.util.*;

public class CommandProcessor {

    public enum CommandResult {
        EXIT, UNKNOWN, CONTINUE
    }

    private final Map<String, Integer> states = new HashMap<>();
    private final List<String> stateOrder = new ArrayList<>();
    private final Map<String, Integer> changes = new HashMap<>();

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

        if (normalized.equals("CLCH")) {
            changes.clear();
            System.out.println("Changes cleared");
            return CommandResult.CONTINUE;
        }

        // state command
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
        if (parts.length != 3) {
            System.out.println("Invalid ADD command. Usage: add {stateName} {stateBalance}");
            return;
        }

        String stateName = parts[1];
        String balanceStr = parts[2];

        try {
            int balance = Integer.parseInt(balanceStr);
            if (balance < 0) {
                System.out.println("Balance must be positive");
                return;
            }
            if (states.containsKey(stateName)) {
                System.out.println("State \"" + stateName + "\" already exists. Nothing changes.");
            } else {
                states.put(stateName, balance);
                stateOrder.add(stateName);
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
            case "TRD" -> handleTrdCommand(stateName, parts);
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

            int currentBalance = states.get(fromState);
            if (currentBalance < amount) {
                System.out.printf("Insufficient funds: %s has %d but tried to pay %d%n",
                    fromState, currentBalance, amount);
                return;
            }

            if (parts.length == 4) {
                String toState = parts[3];
                if (!states.containsKey(toState)) {
                    System.out.println("Target state not found: " + toState);
                    return;
                }
                states.put(fromState, currentBalance - amount);
                int fromStateChange = changes.get(fromState) == null ? 0 : changes.get(fromState);
                changes.put(fromState, fromStateChange - amount);
                states.put(toState, states.get(toState) + amount);
                int toStateChange = changes.get(toState) == null ? 0 : changes.get(toState);
                changes.put(toState, toStateChange + amount);
                System.out.printf("%s paid %d to %s%n", fromState, amount, toState);
            } else {
                states.put(fromState, currentBalance - amount);
                int change = changes.get(fromState) == null ? 0 : changes.get(fromState);
                changes.put(fromState, change - amount);
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
            int change = changes.get(stateName) == null ? 0 : changes.get(stateName);
            changes.put(stateName, change + amount);
            System.out.printf("%s received %d%n", stateName, amount);
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount format: " + parts[2]);
        }
    }

    private void handleTrdCommand(String stateName, String[] parts) {
        if (parts.length != 3) {
            System.out.println("Invalid TRD command. Usage: {state} TRD {amount}");
            return;
        }

        if (stateOrder.isEmpty()) {
            System.out.println("No states available for TRD operation.");
            return;
        }

        try {
            int amount = Integer.parseInt(parts[2]);
            if (amount <= 0) {
                System.out.println("Amount must be positive");
                return;
            }

            String firstState = stateOrder.get(0);
            if (stateName.equals(firstState)) {
                // первый штат налог не платит
                states.put(stateName, states.get(stateName) + amount);
                int change = changes.get(stateName) == null ? 0 : changes.get(stateName);
                changes.put(stateName, change + amount);
                System.out.printf("%s received %d (no tax applied)%n", stateName, amount);
            } else {
                int rounded = (amount / 100) * 100; // округляем вниз до сотен
                int tax = (int) (rounded * 0.2);
                int net = amount - tax;

                states.put(stateName, states.get(stateName) + net);
                int change = changes.get(stateName) == null ? 0 : changes.get(stateName);
                changes.put(stateName, change + net);
                if (tax > 0) {
                    int firstStateChange = changes.get(firstState) == null ? 0 : changes.get(firstState);
                    states.put(firstState, states.get(firstState) + tax);
                    changes.put(firstState, firstStateChange + tax);
                }

                System.out.printf("%s received %d, %s received %d (tax)%n",
                    stateName, net, firstState, tax);
            }
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
               Name    |   Balance   |  Change""");

        for (String stateName : stateOrder) {
            Integer balance = states.get(stateName);
            Integer change = changes.get(stateName);
            String changeString;
            if (change == null || change == 0) {
                changeString = " ---";
            } else {
                changeString = change >= 0 ? "+" + change : String.valueOf(change);
            }
            System.out.printf("    %-6s |    %5d    |  %s%n", stateName, balance, changeString);
        }

        int sum = states.values().stream().mapToInt(value -> value).sum();
        int sumOfChanges = changes.values().stream().mapToInt(value -> value).sum();
        String sumOfChangesString;
        if (sumOfChanges == 0) {
            sumOfChangesString = " ---";
        } else {
            sumOfChangesString = sumOfChanges >= 0 ? "+" + sumOfChanges : String.valueOf(sumOfChanges);
        }
        System.out.println("====================================");
        System.out.printf("    %-6s |    %5d    |  %s%n", "SUM", sum, sumOfChangesString);

        System.out.println("""
            ************************************
                                            v0.1
            ------------------------------------""");
    }
}
