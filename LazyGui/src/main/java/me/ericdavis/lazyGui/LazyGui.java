package me.ericdavis.lazyGui;

import me.ericdavis.lazyGui.guiOther.GuiListener;
import org.bukkit.plugin.java.JavaPlugin;

public class LazyGui {
    public LazyGui(JavaPlugin plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(new GuiListener(), plugin);
    }
}