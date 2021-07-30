package me.draimgoose.draimmenu.classresources.placeholders;

import java.util.HashMap;

public class GUIPlaceholders {
    public HashMap<String,String> keys;

    public void addPlaceholder(String placeholder, String argument){
        keys.put(placeholder,argument);
    }

    public GUIPlaceholders(){
        keys = new HashMap<>();
    }
}

