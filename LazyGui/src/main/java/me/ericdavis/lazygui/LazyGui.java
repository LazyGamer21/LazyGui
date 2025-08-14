package me.ericdavis.lazygui;

import me.ericdavis.lazygui.gui.GuiManager;
import org.bukkit.plugin.java.JavaPlugin;

public class LazyGui {
    public LazyGui(JavaPlugin plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(GuiManager.getInstance(), plugin);
    }
}