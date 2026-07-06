package net.omni.crateyBackpack.util;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;

public final class SoundUtil {

    private SoundUtil() {}

    public static void playSound(Player player, String soundName) {
        if (soundName == null || soundName.isEmpty()) return;
        try {
            player.playSound(Sound.sound(Key.key(soundName.toLowerCase()), Sound.Source.MASTER, 1.0f, 1.0f));
        } catch (Exception ignored) {}
    }
}
