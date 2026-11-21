package dev.ivryzx;

import dev.ivryzx.core.*;
import dev.ivryzx.modules.AutoRespawn;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class iVryzxClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        ModuleManager.register(new AutoRespawn(), Section.PVP, Section.PVE);
        // each new module = one register(...) line, added in batches

        ClientTickEvents.END_CLIENT_TICK.register(ModuleManager::tick);
    }
}
