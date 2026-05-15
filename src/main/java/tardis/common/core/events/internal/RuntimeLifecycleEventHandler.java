package tardis.common.core.events.internal;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import tardis.common.core.TardisRuntimeCleanup;

public class RuntimeLifecycleEventHandler {

    public static final RuntimeLifecycleEventHandler i = new RuntimeLifecycleEventHandler();

    private RuntimeLifecycleEventHandler() {}

    @SubscribeEvent
    public void clientDisconnected(ClientDisconnectionFromServerEvent event) {
        // Disconnect can fire while an integrated server is still winding down.
        // Keep provider registrations intact until next serverAboutToStart cleanup.
        TardisRuntimeCleanup.clearRuntimeCachesOnly();
    }
}
