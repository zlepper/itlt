package ga.ozli.minecraftmods.itlt.platform.services.client;

import ga.ozli.minecraftmods.itlt.platform.Services;

public final class ClientServices {
    public static final ClientPlatformHelper PLATFORM = Services.load(ClientPlatformHelper.class);

    private ClientServices() {}
}
