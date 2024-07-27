package com.softhub.softpostbox.inventory;

import com.softhub.softframework.config.convert.ItemConponent;
import com.softhub.softframework.config.convert.MessageComponent;
import com.softhub.softframework.inventory.SimpleClickEvent;
import com.softhub.softframework.inventory.SimpleCloseEvent;
import com.softhub.softframework.inventory.SimpleInventory;
import com.softhub.softframework.inventory.SimpleInventoryProvider;
import com.softhub.softframework.item.SimpleItem;
import com.softhub.softpostbox.BukkitInitializer;
import com.softhub.softpostbox.ItemContainer;
import com.softhub.softpostbox.PostboxContainer;
import com.softhub.softpostbox.PostboxData;
import com.softhub.softpostbox.config.ConfigManager;
import com.softhub.softpostbox.database.CachedDataService;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PostboxInventory implements SimpleInventoryProvider {

    private final Map<Integer, ItemContainer> slotItemMap = new HashMap<>();
    private int page = 1;
    private SimpleItem backButton;
    private SimpleItem nextButton;
    private SimpleItem collectButton;
    private static final int ITEMS_PER_PAGE = 45;

    public PostboxInventory() {
        this.backButton = ItemConponent.loadItem(BukkitInitializer.getInstance().getConfig(), "back_button");
        this.nextButton = ItemConponent.loadItem(BukkitInitializer.getInstance().getConfig(), "next_button");
        this.collectButton = ItemConponent.loadItem(BukkitInitializer.getInstance().getConfig(), "collect_button");
    }

    @Override
    public void init(Player player) {
        PostboxData postboxData = CachedDataService.get(player.getUniqueId());
        PostboxContainer container = postboxData.getContainer();
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

        simpleInventory.setItem(49, this.collectButton);

        simpleInventory.open(player);
    }

    @Override
    public void onClick(SimpleClickEvent event) {
        event.setCancelled(true);
        if (event.getClickItem() == null || event.getClickItem().getType() == Material.AIR) return;
        Player player = event.getPlayer();
        PostboxInventory postboxInventory = (PostboxInventory) event.getSimpleInventory().getProvider();
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

        if (event.getClickItem().equals(postboxInventory.collectButton)) {
            PostboxData postboxData = CachedDataService.get(player.getUniqueId());
            postboxData.getContainer().collectAll(player);
            player.closeInventory();
            return;
        }

        PostboxData postboxData = CachedDataService.get(player.getUniqueId());
        ItemContainer itemContainer = postboxInventory.slotItemMap.get(event.getSlot());

        if (postboxData == null) {
            player.sendMessage(MessageComponent.formatMessage(BukkitInitializer.getInstance().getConfig(), "not_load_data"));
            player.closeInventory();
            return;
        }

        PostboxContainer container = postboxData.getContainer();
        container.give(player, itemContainer);
        postboxInventory.init(player);
    }

    @Override
    public void onClose(SimpleCloseEvent event) {

    }

}
