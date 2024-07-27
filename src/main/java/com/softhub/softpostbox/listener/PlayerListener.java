package com.softhub.softpostbox.listener;

import com.softhub.softframework.task.SimpleAsync;
import com.softhub.softpostbox.PostboxData;
import com.softhub.softpostbox.database.CachedDataService;
import com.softhub.softpostbox.database.StorageDataService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        SimpleAsync.syncLater(() -> {
            StorageDataService.get(player.getUniqueId()).thenAccept(postboxData -> {
                CachedDataService.set(player.getUniqueId(), postboxData);
            }).exceptionally(ex -> {
                ex.printStackTrace();
                return null;
            });
        }, 5L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PostboxData postboxData = CachedDataService.get(player.getUniqueId());
        if (postboxData == null) return;
        StorageDataService.set(postboxData).exceptionally(ex -> {
           ex.printStackTrace();
           return null;
        }).thenRun(() -> {
           CachedDataService.remove(player.getUniqueId());
        });
    }

}
