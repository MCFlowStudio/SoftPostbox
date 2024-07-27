package com.softhub.softpostbox.inventory;

import com.softhub.softframework.config.convert.MessageComponent;
import com.softhub.softframework.inventory.SimpleClickEvent;
import com.softhub.softframework.inventory.SimpleCloseEvent;
import com.softhub.softframework.inventory.SimpleInventory;
import com.softhub.softframework.inventory.SimpleInventoryProvider;
import com.softhub.softframework.item.SimpleItem;
import com.softhub.softframework.task.SimpleAsync;
import com.softhub.softpostbox.BukkitInitializer;
import com.softhub.softpostbox.ItemContainer;
import com.softhub.softpostbox.PostboxContainer;
import com.softhub.softpostbox.PostboxData;
import com.softhub.softpostbox.config.ConfigManager;
import com.softhub.softpostbox.database.CachedDataService;
import com.softhub.softpostbox.database.StorageDataService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PostboxManageInventory implements SimpleInventoryProvider {

    private final Map<Integer, ItemContainer> slotItemMap = new HashMap<>();
    private int page = 1;
    private SimpleItem backButton;
    private SimpleItem nextButton;
    private String targetName;
    private UUID targetId;
    private PostboxData postboxData;
    private static final int ITEMS_PER_PAGE = 45;

    public PostboxManageInventory(String targetName) {
        this.targetName = targetName;
        this.backButton = new SimpleItem(Material.ARROW);
        this.backButton.setName("이전 페이지");
        this.nextButton = new SimpleItem(Material.ARROW);
        this.nextButton.setName("다음 페이지");
    }

    @Override
    public void init(Player player) {
        Player target = Bukkit.getPlayer(this.targetName);
        if (target != null) {
            this.targetId = target.getUniqueId();
            PostboxData postboxData = CachedDataService.get(target.getUniqueId());
            PostboxContainer container = postboxData.getContainer();
            this.postboxData = postboxData;
            container.sortItems();
            SimpleInventory simpleInventory = new SimpleInventory(MessageComponent.formatMessage(BukkitInitializer.getInstance().getConfig(), "postbox_inventory_title", this.page), 54);
            simpleInventory.register(this);

            slotItemMap.clear();

            int startIndex = (page - 1) * ITEMS_PER_PAGE;
            int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, container.getItemList().size());
            int slot = 0;

            for (int i = startIndex; i < endIndex; i++) {
                ItemContainer item = container.getItemList().get(i);
                if (item != null && item.getType() != Material.AIR) {
                    this.slotItemMap.put(slot, item);
                    SimpleItem displayItem = new SimpleItem(item);
                    displayItem.addLore(" ");
                    displayItem.addLore(" 만료까지 남은 시간: " + MessageComponent.formatTime(item.getTimeUntilExpire()));
                    simpleInventory.setItem(slot, displayItem);
                    slot++;
                }
            }

            if (page > 1) {
                simpleInventory.setItem(46, this.backButton);
            }
            if (endIndex < container.getItemList().size()) {
                simpleInventory.setItem(52, this.nextButton);
            }

            simpleInventory.open(player);
        } else {
            OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(this.targetName);
            if (!offlineTarget.hasPlayedBefore()) {
                player.sendMessage("접속한 적 없는 대상");
                return;
            }
            this.targetId = offlineTarget.getUniqueId();
            StorageDataService.get(offlineTarget.getUniqueId()).thenAccept(postboxData -> {
                PostboxContainer container = postboxData.getContainer();
                this.postboxData = postboxData;
                container.sortItems();
                SimpleInventory simpleInventory = new SimpleInventory("우편함 - " + this.page + "페이지", 54);
                simpleInventory.register(this);

                slotItemMap.clear();

                int startIndex = (page - 1) * ITEMS_PER_PAGE;
                int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, container.getItemList().size());
                int slot = 0;

                for (int i = startIndex; i < endIndex; i++) {
                    ItemContainer item = container.getItemList().get(i);
                    if (item != null && item.getType() != Material.AIR) {
                        this.slotItemMap.put(slot, item);
                        SimpleItem displayItem = new SimpleItem(item);
                        if (ConfigManager.getExpiredEnabled()) {
                            displayItem.addLore(" ");
                            displayItem.addLore(MessageComponent.formatMessage(BukkitInitializer.getInstance().getConfig(), "expired_time", MessageComponent.formatTime(item.getTimeUntilExpire())));
                        }
                        simpleInventory.setItem(slot, displayItem);
                        slot++;
                    }
                }

                if (page > 1) {
                    simpleInventory.setItem(46, this.backButton);
                }
                if (endIndex < container.getItemList().size()) {
                    simpleInventory.setItem(52, this.nextButton);
                }

                SimpleAsync.sync(() -> simpleInventory.open(player));
            });
        }
    }

    @Override
    public void onClick(SimpleClickEvent event) {
        event.setCancelled(true);
        if (event.getClickItem() == null || event.getClickItem().getType() == Material.AIR) return;
        Player player = event.getPlayer();
        PostboxManageInventory postboxInventory = (PostboxManageInventory) event.getSimpleInventory().getProvider();
        if (postboxInventory == null) return;

        if (event.getClickItem().equals(postboxInventory.backButton)) {
            if (postboxInventory.page > 1) {
                postboxInventory.page--;
                postboxInventory.init(player);
            }
            return;
        }

        if (event.getClickItem().equals(postboxInventory.nextButton)) {
            postboxInventory.page++;
            postboxInventory.init(player);
            return;
        }

        PostboxData postboxData = postboxInventory.postboxData;
        ItemContainer itemContainer = postboxInventory.slotItemMap.get(event.getSlot());

        if (postboxData == null) {
            player.sendMessage(MessageComponent.formatMessage(BukkitInitializer.getInstance().getConfig(), "not_load_data"));
            player.closeInventory();
            return;
        }

        PostboxContainer container = postboxInventory.postboxData.getContainer();
        Player target = Bukkit.getPlayer(postboxInventory.targetId);
        container.give(player, itemContainer);
        if (target == null) {
            StorageDataService.set(postboxInventory.postboxData).thenRun(() -> {
                SimpleAsync.sync(() -> postboxInventory.init(player));
            });
        } else {
            postboxInventory.init(player);
        }
    }

    @Override
    public void onClose(SimpleCloseEvent event) {

    }

}
