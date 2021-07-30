package me.draimgoose.draimmenu.ioclasses.legacy;

import me.draimgoose.draimmenu.DraimMenu;

public class PlayerHeads {
    DraimMenu plugin;

    public PlayerHeads(DraimMenu pl) {
        this.plugin = pl;
    }

    public boolean ifSkullOrHead(String material) {
        return material.equalsIgnoreCase("PLAYER_HEAD") || material.equalsIgnoreCase("SKULL_ITEM");
    }

    public String playerHeadString() {
        if (plugin.legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_12)) {
            return "SKULL_ITEM";
        } else {
            return "PLAYER_HEAD";
        }
    }
}
