package kit.penny.tdlib.query;

import kit.penny.tdlib.exception.TdlibException;
import org.drinkless.tdlib.TdApi;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TdlibResponseTest {

    @Test
    void successResponse_shouldContainObjectAndNoError() {
        TdApi.Ok object = new TdApi.Ok();

        TdlibResponse<TdApi.Ok> response =
                new TdlibResponse<>(object, null);

        assertThat(response.getObject())
                .containsSame(object);

        assertThat(response.getError())
                .isEmpty();
    }

    @Test
    void errorResponse_shouldContainErrorAndNoObject() {
        TdApi.Error error =
                new TdApi.Error(400, "Bad request");

        TdlibResponse<TdApi.Ok> response =
                new TdlibResponse<>(null, error);

        assertThat(response.getObject())
                .isEmpty();

        assertThat(response.getError())
                .containsSame(error);
    }

    @Test
    void map_shouldTransformObject() {
        TdApi.Ok object = new TdApi.Ok();

        TdlibResponse<TdApi.Ok> response =
                new TdlibResponse<>(object, null);

        TdApi.User expected = new TdApi.User();

        TdlibResponse<TdApi.User> mapped =
                response.map(value -> expected);

        assertThat(mapped.getObject())
                .containsSame(expected);

        assertThat(mapped.getError())
                .isEmpty();
    }

    @Test
    void map_shouldPreserveError_whenObjectIsAbsent() {
        TdApi.Error error =
                new TdApi.Error(400, "Bad request");

        TdlibResponse<TdApi.Ok> response =
                new TdlibResponse<>(null, error);

        TdlibResponse<TdApi.User> mapped =
                response.map(value -> new TdApi.User());

        assertThat(mapped.getObject())
                .isEmpty();

        assertThat(mapped.getError())
                .containsSame(error);
    }

    @Test
    void map_shouldNotInvokeFunction_whenObjectIsAbsent() {
        TdApi.Error error =
                new TdApi.Error(400, "Bad request");

        TdlibResponse<TdApi.Ok> response =
                new TdlibResponse<>(null, error);

        AtomicReference<Boolean> invoked =
                new AtomicReference<>(false);

        response.map(value -> {
            invoked.set(true);
            return new TdApi.User();
        });

        assertThat(invoked)
                .hasValue(false);
    }

    @Test
    void onSuccess_shouldInvokeAction_whenObjectExists() {
        TdApi.Ok object = new TdApi.Ok();

        TdlibResponse<TdApi.Ok> response =
                new TdlibResponse<>(object, null);

        AtomicReference<TdApi.Ok> received =
                new AtomicReference<>();

        TdlibResponse<TdApi.Ok> returned =
                response.onSuccess(received::set);

        assertThat(received.get())
                .isSameAs(object);

        assertThat(returned)
                .isSameAs(response);
    }

    @Test
    void onSuccess_shouldNotInvokeAction_whenObjectIsAbsent() {
        TdApi.Error error =
                new TdApi.Error(400, "Bad request");

        TdlibResponse<TdApi.Ok> response =
                new TdlibResponse<>(null, error);

        AtomicReference<Boolean> invoked =
                new AtomicReference<>(false);

        TdlibResponse<TdApi.Ok> returned =
                response.onSuccess(value -> invoked.set(true));

        assertThat(invoked)
                .hasValue(false);

        assertThat(returned)
                .isSameAs(response);
    }

    @Test
    void onError_shouldInvokeAction_whenErrorExists() {
        TdApi.Error error =
                new TdApi.Error(400, "Bad request");

        TdlibResponse<TdApi.Ok> response =
                new TdlibResponse<>(null, error);

        AtomicReference<TdApi.Error> received =
                new AtomicReference<>();

        TdlibResponse<TdApi.Ok> returned =
                response.onError(received::set);

        assertThat(received.get())
                .isSameAs(error);

        assertThat(returned)
                .isSameAs(response);
    }

    @Test
    void onError_shouldNotInvokeAction_whenErrorIsAbsent() {
        TdApi.Ok object = new TdApi.Ok();

        TdlibResponse<TdApi.Ok> response =
                new TdlibResponse<>(object, null);

        AtomicReference<Boolean> invoked =
                new AtomicReference<>(false);

        TdlibResponse<TdApi.Ok> returned =
                response.onError(error -> invoked.set(true));

        assertThat(invoked)
                .hasValue(false);

        assertThat(returned)
                .isSameAs(response);
    }

    @Test
    void getObjectOrThrow_shouldReturnObject_whenObjectExists() {
        TdApi.Ok object = new TdApi.Ok();

        TdlibResponse<TdApi.Ok> response =
                new TdlibResponse<>(object, null);

        assertThat(response.getObjectOrThrow())
                .isSameAs(object);
    }

    @Test
    void getObjectOrThrow_shouldThrowTdlibException_whenObjectIsAbsent() {
        TdApi.Error error =
                new TdApi.Error(400, "Bad request");

        TdlibResponse<TdApi.Ok> response =
                new TdlibResponse<>(null, error);

        assertThatThrownBy(response::getObjectOrThrow)
                .isInstanceOf(TdlibException.class)
                .hasMessage("TdApi.Object is null");
    }
}