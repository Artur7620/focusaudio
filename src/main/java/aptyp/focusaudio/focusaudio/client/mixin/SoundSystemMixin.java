package aptyp.focusaudio.focusaudio.client.mixin;

import aptyp.focusaudio.focusaudio.client.FocusConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.Channel;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Map;

@Mixin(SoundSystem.class)
public class SoundSystemMixin {

    @Shadow
    private Map<SoundInstance, Channel.SourceManager> sources;

    @Inject(method = "play", at = @At("RETURN"))
    private void onPlay(SoundInstance sound, CallbackInfoReturnable<SoundSystem.PlayResult> cir) {
        applyFocus(sound);
    }

    @Inject(method = "tick(Z)V", at = @At("RETURN"))
    private void onTick(boolean paused, CallbackInfo ci) {
        if (paused || sources == null) return;
        for (var entry : sources.entrySet()) {
            var sound = entry.getKey();
            var sm = entry.getValue();
            if (sm == null || sound.getAttenuationType() == SoundInstance.AttenuationType.NONE) continue;
            
            float vol = Math.min(sound.getVolume() * getMultiplier(sound.getX(), sound.getY(), sound.getZ()), 2.5f);
            sm.run(src -> src.setVolume(vol));
        }
    }

    private void applyFocus(SoundInstance sound) {
        if (sound == null || sources == null || sound.getAttenuationType() == SoundInstance.AttenuationType.NONE) return;
        var sm = sources.get(sound);
        if (sm == null) return;
        
        float vol = Math.min(sound.getVolume() * getMultiplier(sound.getX(), sound.getY(), sound.getZ()), 2.5f);
        sm.run(src -> src.setVolume(vol));
    }

    private float getMultiplier(double x, double y, double z) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null) return 1.0f;

        PlayerEntity player = mc.player;
        Vec3d eye = player.getEyePos();
        Vec3d toSound = new Vec3d(x - eye.x, y - eye.y, z - eye.z);
        double dist = toSound.length();
        
        double dot = player.getRotationVec(1.0f).dotProduct(toSound.normalize());
        double configAngleCos = Math.cos(Math.toRadians(FocusConfig.instance.focusAngle));
        boolean isFocused = dot >= configAngleCos;

        if (dist < 1.5) return 1.2f; 

        if (isFocused) {
            return (dist > 25.0) ? 0.8f : FocusConfig.instance.focusMultiplier; 
        } else {
            return (dist > 5.0) ? FocusConfig.instance.backgroundVolume : 0.4f;
        }
    }
}
