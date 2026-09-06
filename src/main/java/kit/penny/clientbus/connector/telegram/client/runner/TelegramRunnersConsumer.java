package kit.penny.clientbus.connector.telegram.client.runner;

import kit.penny.clientbus.connector.telegram.TelegramRunner;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * Consumer of {@link TelegramRunner} implementations.
 *
 * @author Pavel Vorobyev
 */
public sealed interface TelegramRunnersConsumer
        extends Consumer<Collection<TelegramRunner>>
        permits TelegramRunnersConsumerImpl {
}
