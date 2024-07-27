package com.softhub.softpostbox;

import com.softhub.softframework.config.convert.MessageComponent;
import com.softhub.softpostbox.config.ConfigManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.time.Instant;
import java.util.*;

public class PostboxContainer {

    @Getter
    @Setter
    private List<ItemContainer> itemList = new ArrayList<>();

    public PostboxContainer(List<ItemContainer> itemList) {
        this.itemList = itemList;
        sortItems();
    }

    public void addItem(ItemContainer item) {
        this.itemList.add(item);
        sortItems();
    }

    public void removeItem(ItemContainer item) {
        int amountToRemove = item.getAmount();
        Iterator<ItemContainer> iterator = this.itemList.iterator();

        while (iterator.hasNext() && amountToRemove > 0) {
            ItemContainer currentItem = iterator.next();
            if (currentItem.isSimilar(item)) {
                int currentAmount = currentItem.getAmount();
                if (currentAmount > amountToRemove) {
                    currentItem.setAmount(currentAmount - amountToRemove);
                    amountToRemove = 0;
                } else {
                    amountToRemove -= currentAmount;
                    iterator.remove();
                }
            }
        }
    }

    public void sortItems() {
        Map<String, List<ItemContainer>> itemMap = new HashMap<>();

        for (ItemContainer item : itemList) {
            if (item.isExpired() && ConfigManager.getExpiredEnabled()) {
                continue;
            }

            String key = item.getType().toString() + ":" + item.getDurability() + ":" + item.getItemMeta().toString();
            itemMap.putIfAbsent(key, new ArrayList<>());

            List<ItemContainer> stacks = itemMap.get(key);
            int amount = item.getAmount();

            for (ItemContainer stack : stacks) {
                int space = 64 - stack.getAmount();
                if (space > 0) {
                    int toAdd = Math.min(space, amount);
                    stack.setAmount(stack.getAmount() + toAdd);
                    amount -= toAdd;
                    if (amount <= 0) {
                        break;
                    }
                }
            }

            while (amount > 0) {
                int toAdd = Math.min(64, amount);
                ItemContainer newStack = item.clone();
                newStack.setAmount(toAdd);
                stacks.add(newStack);
                amount -= toAdd;
            }
        }

        itemList = new ArrayList<>();
        for (List<ItemContainer> stacks : itemMap.values()) {
            ItemContainer mergedItem = mergeItemContainers(stacks);
            itemList.add(mergedItem);
        }
    }

    private ItemContainer mergeItemContainers(List<ItemContainer> containers) {
        if (containers.isEmpty()) {
            return null;
        }

        ItemContainer result = containers.get(0).clone();
        int totalAmount = 0;
        Instant maxExpireTime = result.getExpireTime();

        for (ItemContainer container : containers) {
            totalAmount += container.getAmount();
            if (maxExpireTime == null || (container.getExpireTime() != null && container.getExpireTime().isAfter(maxExpireTime))) {
                maxExpireTime = container.getExpireTime();
            }
        }

        result.setAmount(totalAmount);
        result.setExpireTime(maxExpireTime);
        return result;
    }

    public void give(Player player, ItemContainer item) {
        Inventory playerInventory = player.getInventory();
        if (playerInventory.firstEmpty() == -1) {
            player.sendMessage(MessageComponent.formatMessage(BukkitInitializer.getInstance().getConfig(), "full_inventory"));
            return;
        }

        boolean itemFound = false;
        Iterator<ItemContainer> iterator = this.itemList.iterator();

        while (iterator.hasNext()) {
            ItemContainer currentItem = iterator.next();
            if (currentItem.isSameItem(item)) {
                itemFound = true;

                ItemContainer itemToGive = currentItem.clone();

                for (ItemStack remainingItem : player.getInventory().addItem(itemToGive).values()) {
                    player.getWorld().dropItem(player.getLocation(), remainingItem);
                }

                iterator.remove();

                player.sendMessage(MessageComponent.formatMessage(BukkitInitializer.getInstance().getConfig(), "success_receive_item"));
                break;
            }
        }

        if (!itemFound) {
            player.sendMessage(MessageComponent.formatMessage(BukkitInitializer.getInstance().getConfig(), "no_data_item"));
        }
    }

    public void giveSilent(Player player, ItemContainer item) {
        Inventory playerInventory = player.getInventory();
        if (playerInventory.firstEmpty() == -1) {
            return;
        }

        boolean itemFound = false;
        Iterator<ItemContainer> iterator = this.itemList.iterator();

        while (iterator.hasNext()) {
            ItemContainer currentItem = iterator.next();
            if (currentItem.isSameItem(item)) {
                itemFound = true;

                ItemContainer itemToGive = currentItem.clone();

                for (ItemStack remainingItem : player.getInventory().addItem(itemToGive).values()) {
                    player.getWorld().dropItem(player.getLocation(), remainingItem);
                }

                iterator.remove();

                break;
            }
        }

        if (!itemFound) {
            player.sendMessage(MessageComponent.formatMessage(BukkitInitializer.getInstance().getConfig(), "no_data_item"));
        }
    }

    public void collectAll(Player player) {
        List<ItemContainer> itemsToCollect = new ArrayList<>(this.getItemList());
        int totalItems = itemsToCollect.size();
        int itemsCollected = 0;
        int itemsFailed = 0;

        player.sendMessage(MessageComponent.formatMessage(BukkitInitializer.getInstance().getConfig(), "start_collect_item", totalItems));

        for (ItemContainer item : itemsToCollect) {
            if (player.getInventory().firstEmpty() != -1) {
                this.giveSilent(player, item);
                itemsCollected++;
            } else {
                itemsFailed++;
                break;
            }
        }

        if (itemsFailed > 0) {
            player.sendMessage(MessageComponent.formatMessage(BukkitInitializer.getInstance().getConfig(), "error_collect_item", itemsFailed));
        }

        player.sendMessage(MessageComponent.formatMessage(BukkitInitializer.getInstance().getConfig(), "success_collect_item", totalItems, itemsCollected));
    }

}
