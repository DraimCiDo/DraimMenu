package me.draimgoose.draimmenu.api;

import me.draimgoose.draimmenu.openguimanager.GUIPosition;

public class GUIInterface {

    public String playerName;
    private GUI top,middle,bottom = null;

    public GUIInterface(String player){
        playerName = player;
    }

    public boolean allClosed(){
        return top == null && middle == null && bottom == null;
    }

    public void setGui(GUI gui, GUIPosition position){
        switch(position){
            case Top:{
                if(gui == null && top != null){
                    top.isOpen = false;
                }
                top = gui;
                return;
            }
            case Middle:{
                if(gui == null && middle != null){
                    middle.isOpen = false;
                }
                middle = gui;
                return;
            }
            case Bottom:{
                if(gui == null && bottom != null){
                    bottom.isOpen = false;
                }
                bottom = gui;
            }
        }
    }

    public GUI getGui(GUIPosition position){
        switch(position){
            case Top:{
                return top;
            }
            case Middle:{
                return middle;
            }
            case Bottom:{
                return bottom;
            }
        }
        return null;
    }
}

