package kit.penny.tdlib.integration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

final class ConsoleInput {

    private final java.io.Console console;
    private final BufferedReader reader;

    ConsoleInput() {
        this.console = System.console();
        this.reader = console == null
                ? new BufferedReader(new InputStreamReader(System.in))
                : null;
    }

    String readLine(String prompt) {
        try {
            if (console != null) {
                String value = console.readLine("%s", prompt);
                return value == null ? "" : value.trim();
            }

            System.out.print(prompt);
            System.out.flush();

            String value = reader.readLine();

            if (value == null) {
                throw new IllegalStateException("Standard input was closed.");
            }

            return value.trim();
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to read input from console.",
                    e
            );
        }
    }

    String readSecret(String prompt) {
        try {
            if (console != null) {
                char[] value = console.readPassword("%s: ", prompt);

                if (value == null) {
                    throw new IllegalStateException("Console input was closed.");
                }

                return new String(value).trim();
            }

            // IDEA Application обычно не предоставляет System.console().
            // В этом случае читаем обычным stdin.
            return readLine(prompt + ": ");
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to read secret from console.",
                    e
            );
        }
    }
}