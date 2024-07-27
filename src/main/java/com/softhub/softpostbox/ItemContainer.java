package com.softhub.softpostbox;

import com.softhub.softframework.item.SimpleItem;
import com.softhub.softpostbox.config.ConfigManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class ItemContainer extends SimpleItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    private Instant expireTime;

    public ItemContainer(Material material) {
        super(material);
    }

    public ItemContainer(ItemStack item) {
        super(item);
    }

    public ItemContainer() {
        super(Material.AIR);
    }

    public void registerItem() {
        Instant now = Instant.now();
        this.expireTime = now.plus(ConfigManager.getExpiredTime(), ChronoUnit.HOURS);
    }

    public int getTimeUntilExpire() {
        if (expireTime == null) {
            return Integer.MAX_VALUE;
        }
        long secondsUntilExpire = expireTime.getEpochSecond() - Instant.now().getEpochSecond();
        return (int) Math.max(0, Math.min(secondsUntilExpire, Integer.MAX_VALUE));
    }


    public boolean isExpired() {
        return expireTime != null && Instant.now().isAfter(expireTime);
    }

    public boolean isSameItem(SimpleItem other) {
        if (other == null) return false;
        return this.getType() == other.getType() &&
                this.getAmount() == other.getAmount() &&
                (this.getItemMeta() == null ? other.getItemMeta() == null : this.getItemMeta().equals(other.getItemMeta()));
    }

    @Override
    public ItemContainer clone() {
        ItemContainer clone = new ItemContainer(this);
        clone.setExpireTime(this.expireTime);
        return clone;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        if (this.getType() != Material.AIR) {
            out.writeObject(this.serializeAsBytes());
        } else {
            out.writeObject(null);
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        byte[] itemBytes = (byte[]) in.readObject();
        if (itemBytes != null) {
            ItemStack itemStack = ItemStack.deserializeBytes(itemBytes);
            this.setType(itemStack.getType());
            this.setAmount(itemStack.getAmount());
            this.setItemMeta(itemStack.getItemMeta());
        } else {
            this.setType(Material.AIR);
            this.setAmount(0);
            this.setItemMeta(null);
        }
    }

}
