package me.draimgoose.draimmenu.interactives.input;

import me.draimgoose.draimmenu.api.GUI;

import java.util.List;

public class PlayerInput {
    public GUI gui;
    public List<String> commands;

    public PlayerInput(GUI gui1, List<String> commands1){
        gui = gui1;
        commands = commands1;
    }
}
