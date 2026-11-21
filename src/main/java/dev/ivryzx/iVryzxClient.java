package dev.ivryzx;

import dev.ivryzx.core.ModuleManager;
import dev.ivryzx.modules.CombatModules;
import dev.ivryzx.modules.MovementModules;
import dev.ivryzx.modules.SurvivalModules;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class iVryzxClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        MovementModules.register();
        CombatModules.register();
        SurvivalModules.register();
        // each new batch of modules adds one register() line here

        ClientTickEvents.END_CLIENT_TICK.register(ModuleManager::tick);
    }
}
