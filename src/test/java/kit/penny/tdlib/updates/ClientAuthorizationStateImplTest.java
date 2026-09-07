package kit.penny.tdlib.updates;

import kit.penny.tdlib.AbstractTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static kit.penny.clientbus.connector.telegram.client.updates.AuthorizationStateCache.*;
import static org.junit.jupiter.api.Assertions.*;

class ClientAuthorizationStateImplTest extends AbstractTest {

    @Autowired
    private kit.penny.tdlib.updates.ITelegramAuthorizationManager ITelegramAuthorizationManager;

    @BeforeEach
    void clearCacheValues() {
        codeInputToCheck = null;
        passwordInputToCheck = null;
        emailAddressInputToCheck = null;
        waitAuthenticationCode.set(false);
        waitAuthenticationPassword.set(false);
        waitEmailAddress.set(false);
        haveAuthorization.set(false);
        stateClosed.set(false);
    }

    @Test
    void checkAuthenticationCode() {
        //setup flag that client waits authentication code
        waitAuthenticationCode.set(true);
        assertTrue(ITelegramAuthorizationManager.isWaitAuthenticationCode());

        //check code
        var code = "code";
        ITelegramAuthorizationManager.checkAuthenticationCode(code);

        //code accepted
        assertFalse(ITelegramAuthorizationManager.isWaitAuthenticationCode());
        assertEquals(code, AuthorizationStateCache.codeInputToCheck);
    }

    @Test
    void checkAuthenticationPassword() {
        //setup flag that client waits authentication password
        waitAuthenticationPassword.set(true);
        assertTrue(ITelegramAuthorizationManager.isWaitAuthenticationPassword());

        //check password
        var password = "password";
        ITelegramAuthorizationManager.checkAuthenticationPassword(password);

        //password accepted
        assertFalse(ITelegramAuthorizationManager.isWaitAuthenticationPassword());
        assertEquals(password, AuthorizationStateCache.passwordInputToCheck);
    }

    @Test
    void checkEmailAddress() {
        //setup flag that client waits authentication email
        waitEmailAddress.set(true);
        assertTrue(ITelegramAuthorizationManager.isWaitEmailAddress());

        //check email
        var email = "some_email";
        ITelegramAuthorizationManager.checkEmailAddress(email);

        //email accepted
        assertFalse(ITelegramAuthorizationManager.isWaitEmailAddress());
        assertEquals(email, emailAddressInputToCheck);
    }

    @Test
    void checkDefaults() {
        assertFalse(ITelegramAuthorizationManager.haveAuthorization());
        assertFalse(ITelegramAuthorizationManager.isWaitAuthenticationCode());
        assertFalse(ITelegramAuthorizationManager.isWaitAuthenticationPassword());
        assertFalse(ITelegramAuthorizationManager.isWaitEmailAddress());
        assertFalse(ITelegramAuthorizationManager.isStateClosed());
    }

}