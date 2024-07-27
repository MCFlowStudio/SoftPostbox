package com.softhub.softpostbox;

import com.softhub.softframework.command.CommandRegister;
import com.softhub.softpostbox.command.PostboxCommand;
import com.softhub.softpostbox.config.ConfigManager;
import com.softhub.softpostbox.database.CachedDataService;
import com.softhub.softpostbox.database.StorageDataService;
import com.softhub.softpostbox.listener.PlayerListener;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class BukkitInitializer extends JavaPlugin {

    @Getter
    private static BukkitInitializer instance;

    @Override
    public void onEnable() {
        instance = this;
        ConfigManager.init();
        StorageDataService.init();
        CommandRegister.registerCommands(new PostboxCommand());
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        for (Player player : Bukkit.getOnlinePlayers()) {
            StorageDataService.get(player.getUniqueId()).thenAccept(postboxData -> {
                CachedDataService.set(player.getUniqueId(), postboxData);
            });
        }
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PostboxData postboxData = CachedDataService.get(player.getUniqueId());
            if (postboxData == null) return;
            StorageDataService.setSync(postboxData);
        }
    }
}
