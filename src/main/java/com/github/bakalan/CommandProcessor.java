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

        if (isExitCommand(normalized)) {
            return CommandResult.EXIT;
        }

        if (normalized.startsWith("ADD ")) {
            handleAddCommand(normalized);
            return CommandResult.CONTINUE;
        }

        return CommandResult.UNKNOWN;
    }

    private boolean isExitCommand(String command) {
        return command.equals("Q")
            || command.equals("QUIT")
            || command.equals("EXIT");
    }

    private void handleAddCommand(String input) {
        // ожидаем строку вида: ADD STATE BALANCE
        String[] parts = input.split("\\s+");
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
