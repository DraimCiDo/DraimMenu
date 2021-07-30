package me.draimgoose.draimmenu.openwithitem;

import me.draimgoose.draimmenu.api.GUI;

import java.util.HashMap;

public class HotbarPlayerManager {
    public HashMap<Integer, GUI> list = new HashMap<>();

    public HotbarPlayerManager(){
    }

    public void addSlot(int slot, GUI panel){
        list.put(slot,panel);
    }

    public GUI getGUI(int slot){
        return list.get(slot).copy();
    }
}
