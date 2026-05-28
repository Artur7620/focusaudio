package aptyp.focusaudio.focusaudio;

import aptyp.focusaudio.focusaudio.client.FocusConfig;
import net.fabricmc.api.ModInitializer;

public class Focusaudio implements ModInitializer {
    @Override
    public void onInitialize() {
        FocusConfig.load();
    }
}
