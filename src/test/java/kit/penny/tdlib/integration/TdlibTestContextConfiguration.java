package kit.penny.tdlib.integration;

import kit.penny.tdlib.TdlibAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public final class TdlibTestContextConfiguration {

    private TdlibTestContextConfiguration() {
    }

    public static AnnotationConfigApplicationContext create(
            int apiId,
            String apiHash,
            String phone,
            Path databaseDirectory,
            Path filesDirectory
    ) {
        try {
            Files.createDirectories(databaseDirectory);
            Files.createDirectories(filesDirectory);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to create TDLib directories.",
                    e
            );
        }

        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext();

        context.getEnvironment()
                .getPropertySources()
                .addFirst(
                        new MapPropertySource(
                                "tdlib-integration-test",
                                properties(
                                        apiId,
                                        apiHash,
                                        phone,
                                        databaseDirectory,
                                        filesDirectory
                                )
                        )
                );

        context.register(TdlibAutoConfiguration.class);
        context.refresh();

        return context;
    }

    private static Map<String, Object> properties(
            int apiId,
            String apiHash,
            String phone,
            Path databaseDirectory,
            Path filesDirectory
    ) {
        return Map.ofEntries(
                Map.entry(
                        "spring.telegram.client.use-test-dc",
                        false
                ),
                Map.entry(
                        "spring.telegram.client.database-directory",
                        databaseDirectory.toAbsolutePath().toString()
                ),
                Map.entry(
                        "spring.telegram.client.files-directory",
                        filesDirectory.toAbsolutePath().toString()
                ),
                Map.entry(
                        "spring.telegram.client.database-encryption-key",
                        "db-secret"
                ),
                Map.entry(
                        "spring.telegram.client.use-file-database",
                        true
                ),
                Map.entry(
                        "spring.telegram.client.use-chat-info-database",
                        true
                ),
                Map.entry(
                        "spring.telegram.client.use-message-database",
                        true
                ),
                Map.entry(
                        "spring.telegram.client.use-secret-chats",
                        true
                ),
                Map.entry(
                        "spring.telegram.client.api-id",
                        apiId
                ),
                Map.entry(
                        "spring.telegram.client.api-hash",
                        apiHash
                ),
                Map.entry(
                        "spring.telegram.client.phone",
                        phone
                ),
                Map.entry(
                        "spring.telegram.client.system-language-code",
                        "en"
                ),
                Map.entry(
                        "spring.telegram.client.device-model",
                        "TDLib Integration Test"
                ),
                Map.entry(
                        "spring.telegram.client.system-version",
                        "Java 21"
                ),
                Map.entry(
                        "spring.telegram.client.application-version",
                        "integration-test"
                ),
                Map.entry(
                        "spring.telegram.client.log-verbosity-level",
                        2
                )
        );
    }
}