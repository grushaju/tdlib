package kit.penny.tdlib.integration;

import kit.penny.tdlib.updates.TdlibAuthorizationIntegrationTest;
import kit.penny.tdlib.updates.TdlibRealIntegrationTest;

public final class TdlibRealIntegrationRunner {

    private TdlibRealIntegrationRunner() {
    }

    public static void main(String[] args) throws Exception {
        TdlibRealIntegrationTest.main(args);
        //TdlibAuthorizationIntegrationTest.main(args);
    }
}