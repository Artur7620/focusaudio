package aptyp.focusaudio.focusaudio.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class FocusaudioClient implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("focusaudio");
    public static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(createKeyBinding());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                FocusConfig.instance.enabled = !FocusConfig.instance.enabled;
                FocusConfig.save();
                if (client.player != null) {
                    client.player.sendMessage(
                            Text.literal("FocusAudio: " + (FocusConfig.instance.enabled ? "§aON" : "§cOFF")),
                            true
                    );
                }
            }
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static KeyBinding createKeyBinding() {
        Constructor<?>[] ctors = KeyBinding.class.getConstructors();

        for (Constructor<?> ctor : ctors) {
            Class<?>[] params = ctor.getParameterTypes();

            // 1.21.11: (String, InputUtil.Type, int, KeyBinding$Category)
            // params[3] — это вложенный класс Category (имя неизвестно в intermediary)
            if (params.length == 4
                    && params[0] == String.class
                    && params[1] == InputUtil.Type.class
                    && params[2] == int.class
                    && !params[3].equals(String.class)) {
                try {
                    Class categoryClass = params[3];
                    // Найти статический метод create(Identifier) — единственный static factory
                    Object category = null;
                    for (Method m : categoryClass.getDeclaredMethods()) {
                        Class<?>[] mp = m.getParameterTypes();
                        if (java.lang.reflect.Modifier.isStatic(m.getModifiers())
                                && mp.length == 1
                                && !mp[0].isPrimitive()) {
                            // Создать Identifier через его собственный static factory
                            Object identifier = createIdentifier(mp[0], "focusaudio", "misc");
                            if (identifier != null) {
                                category = m.invoke(null, identifier);
                                break;
                            }
                        }
                    }
                    if (category == null) {
                        // Fallback: взять MISC из статических полей Category
                        for (java.lang.reflect.Field f : categoryClass.getDeclaredFields()) {
                            if (java.lang.reflect.Modifier.isStatic(f.getModifiers())
                                    && f.getType() == categoryClass) {
                                category = f.get(null);
                                break;
                            }
                        }
                    }
                    if (category != null) {
                        LOGGER.info("[FocusAudio] Using 1.21.11+ KeyBinding constructor");
                        return (KeyBinding) ctor.newInstance(
                                "key.focusaudio.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F7, category);
                    }
                } catch (Exception e) {
                    LOGGER.warn("[FocusAudio] 1.21.11 ctor failed: {}", e.getMessage());
                }
            }

            // 1.21.8: (String, InputUtil.Type, int, String)
            if (params.length == 4
                    && params[0] == String.class
                    && params[1] == InputUtil.Type.class
                    && params[2] == int.class
                    && params[3] == String.class) {
                try {
                    LOGGER.info("[FocusAudio] Using 1.21.8 KeyBinding constructor");
                    return (KeyBinding) ctor.newInstance(
                            "key.focusaudio.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F7, "category.focusaudio");
                } catch (Exception e) {
                    LOGGER.warn("[FocusAudio] 1.21.8 ctor failed: {}", e.getMessage());
                }
            }
        }

        throw new RuntimeException("[FocusAudio] No compatible KeyBinding constructor found on this Minecraft version");
    }

    private static Object createIdentifier(Class<?> identifierClass, String namespace, String path) {
        // Try Identifier.of(String) — 1.21+
        try {
            Method of = identifierClass.getMethod("of", String.class);
            return of.invoke(null, namespace + ":" + path);
        } catch (Exception ignored) {}
        // Try new Identifier(String, String)
        try {
            return identifierClass.getConstructor(String.class, String.class).newInstance(namespace, path);
        } catch (Exception ignored) {}
        return null;
    }
}
