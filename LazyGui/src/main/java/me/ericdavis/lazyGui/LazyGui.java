package me.ericdavis.lazyGui;

import me.ericdavis.lazyGui.guiOther.GuiListener;
import me.ericdavis.lazyGui.guiOther.GuiManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class LazyGui {

    public void init(JavaPlugin plugin)
    {
        new GuiManager(plugin); // init gui pages

        plugin.getServer().getPluginManager().registerEvents(new GuiListener(), plugin);
    }

}
