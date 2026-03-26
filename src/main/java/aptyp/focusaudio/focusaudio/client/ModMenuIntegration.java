package aptyp.focusaudio.focusaudio.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.literal("FocusAudio Settings"))
                    .setSavingRunnable(FocusConfig::save);

            ConfigEntryBuilder eb = builder.entryBuilder();
            ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));

            general.addEntry(eb.startFloatField(Text.literal("Focus Boost (1.0 - 5.0)"), FocusConfig.instance.focusMultiplier)
                    .setDefaultValue(2.0f)
                    .setSaveConsumer(v -> FocusConfig.instance.focusMultiplier = v)
                    .build());

            general.addEntry(eb.startFloatField(Text.literal("Background Volume (0.0 - 1.0)"), FocusConfig.instance.backgroundVolume)
                    .setDefaultValue(0.1f)
                    .setSaveConsumer(v -> FocusConfig.instance.backgroundVolume = v)
                    .build());

            general.addEntry(eb.startIntSlider(Text.literal("Focus Angle"), FocusConfig.instance.focusAngle, 5, 90)
                    .setDefaultValue(15)
                    .setSaveConsumer(v -> FocusConfig.instance.focusAngle = v)
                    .build());

            return builder.build();
        };
    }
}
