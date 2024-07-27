package com.softhub.softpostbox.config;

import com.softhub.softpostbox.BukkitInitializer;
import lombok.Getter;

public class ConfigManager {

    @Getter
    private static Boolean expiredEnabled = true;
    @Getter
    private static Integer expiredTime;

    public static void init() {
        BukkitInitializer.getInstance().saveDefaultConfig();
        BukkitInitializer.getInstance().reloadConfig();
        expiredTime = BukkitInitializer.getInstance().getConfig().getInt("settings.expired_time");
        if (expiredTime == -1)
            expiredEnabled = false;
    }

}
