package com.github.bakalan;

public class CommandProcessor {

    public enum CommandResult {
        EXIT, UNKNOWN, CONTINUE
    }

    public CommandResult process(String input) {
        if (input == null || input.isBlank()) {
            return CommandResult.UNKNOWN;
        }

        String normalized = input.trim().toUpperCase();

        if (isExitCommand(normalized)) {
            return CommandResult.EXIT;
        }

        return CommandResult.UNKNOWN;
    }

    private boolean isExitCommand(String command) {
        return command.equals("Q")
            || command.equals("QUIT")
            || command.equals("EXIT");
    }
}
