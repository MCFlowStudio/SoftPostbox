package com.softhub.softpostbox.manager;

import com.softhub.softframework.config.convert.MessageComponent;
import com.softhub.softframework.task.SimpleAsync;
import com.softhub.softpostbox.BukkitInitializer;
import com.softhub.softpostbox.ItemContainer;
import com.softhub.softpostbox.PostboxContainer;
import com.softhub.softpostbox.PostboxData;
import com.softhub.softpostbox.database.CachedDataService;
import com.softhub.softpostbox.database.StorageDataService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PostboxManager {

    public static void gift(Player player, String targetName) {
        if (player.getItemInHand() == null || player.getItemInHand().getType() == Material.AIR) {
            player.sendMessage(MessageComponent.formatMessage(BukkitInitializer.getInstance().getConfig(), "no_add_air"));
            return;
        }
        SimpleAsync.async(() -> {
            Player target = Bukkit.getPlayer(targetName);
            if (target != null) {
                PostboxData postboxData = CachedDataService.get(target.getUniqueId());
                PostboxContainer container = postboxData.getContainer();
                ItemContainer item = new ItemContainer(player.getItemInHand().clone());
                player.setItemInHand(new ItemStack(Material.AIR));
                ItemContainer itemContainer = new ItemContainer(item);
                itemContainer.registerItem();
                container.addItem(itemContainer);
                String itemDisplay = item.getType().name();
                if (item.hasItemMeta() && item.getItemMeta().hasDisplayName())
                    itemDisplay = item.getItemMeta().getDisplayName();
                player.sendMessage((MessageComponent.formatMessage(BukkitInitializer.getInstance().getConfig(), "success_gift", targetName, itemDisplay)));
                target.sendMessage((MessageComponent.formatMessage(BukkitInitializer.getInstance().getConfig(), "success_gift_target", player.getName(), itemDisplay)));
                StorageDataService.set(postboxData);
            } else {
                OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetName);
                if (!offlineTarget.hasPlayedBefore()) {
                    player.sendMessage(MessageComponent.formatMessage(BukkitInitializer.getInstance().getConfig(), "no_target_player", targetName));
                    return;
                }
                StorageDataService.get(offlineTarget.getUniqueId()).thenAccept(postboxData -> {
                    PostboxContainer container = postboxData.getContainer();
                    ItemStack item = player.getItemInHand().clone();
                    player.setItemInHand(new ItemStack(Material.AIR));
                    ItemContainer itemContainer = new ItemContainer(item);
                    itemContainer.registerItem();
                    container.addItem(itemContainer);
                    StorageDataService.set(postboxData);
                    String itemDisplay = item.getType().name();
                    if (item.hasItemMeta() && item.getItemMeta().hasDisplayName())
                        itemDisplay = item.getItemMeta().getDisplayName();
                    player.sendMessage((MessageComponent.formatMessage(BukkitInitializer.getInstance().getConfig(), "success_gift", targetName, itemDisplay)));
                });
            }
        });
    }

    public static void give(Player player, String targetName, Integer amount) {
        if (player.getItemInHand() == null || player.getItemInHand().getType() == Material.AIR) {
            player.sendMessage(MessageComponent.formatMessage(BukkitInitializer.getInstance().getConfig(), "no_add_air"));
            return;
        }
        SimpleAsync.async(() -> {
            Player target = Bukkit.getPlayer(targetName);
            if (target != null) {
                PostboxData postboxData = CachedDataService.get(target.getUniqueId());
                if (postboxData == null) {
                    player.sendMessage(MessageComponent.formatMessage(BukkitInitializer.getInstance().getConfig(), "not_load_data"));
                    return;
                }
                PostboxContainer container = postboxData.getContainer();
                ItemStack item = player.getItemInHand().clone();
                item.setAmount(amount);
                ItemContainer itemContainer = new ItemContainer(item);
                itemContainer.registerItem();
                container.addItem(itemContainer);
                StorageDataService.set(postboxData);
                String itemDisplay = item.getType().name();
                if (item.hasItemMeta() && item.getItemMeta().hasDisplayName())
                    itemDisplay = item.getItemMeta().getDisplayName();
                player.sendMessage((MessageComponent.formatMessage(BukkitInitializer.getInstance().getConfig(), "success_give", targetName, itemDisplay)));
                target.sendMessage((MessageComponent.formatMessage(BukkitInitializer.getInstance().getConfig(), "success_give_target", player.getName(), itemDisplay)));
            } else {
                OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetName);
                if (!offlineTarget.hasPlayedBefore()) {
                    player.sendMessage(MessageComponent.formatMessage(BukkitInitializer.getInstance().getConfig(), "no_target_player", targetName));
                    return;
                }
                StorageDataService.get(offlineTarget.getUniqueId()).thenAccept(postboxData -> {
                    if (postboxData == null) {
                        player.sendMessage(MessageComponent.formatMessage(BukkitInitializer.getInstance().getConfig(), "not_load_data"));
                        return;
                    }
                    PostboxContainer container = postboxData.getContainer();
                    ItemStack item = player.getItemInHand().clone();
                    item.setAmount(amount);
                    ItemContainer itemContainer = new ItemContainer(item);
                    itemContainer.registerItem();
                    container.addItem(itemContainer);
                    StorageDataService.set(postboxData);
                    String itemDisplay = item.getType().name();
                    if (item.hasItemMeta() && item.getItemMeta().hasDisplayName())
                        itemDisplay = item.getItemMeta().getDisplayName();
                    player.sendMessage((MessageComponent.formatMessage(BukkitInitializer.getInstance().getConfig(), "success_give", targetName, itemDisplay)));
                });
            }
        });
    }

}
