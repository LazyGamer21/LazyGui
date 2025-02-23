package me.ericdavis.lazyGui;

import me.ericdavis.lazyGui.guiOther.GuiListener;
import me.ericdavis.lazyGui.guiOther.GuiManager;
import me.ericdavis.lazyGui.guiPage.AbstractGuiPage;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class LazyGui {

    public void init(JavaPlugin plugin, List<AbstractGuiPage> pages)
    {
        new GuiManager(plugin); // init gui pages

        plugin.getServer().getPluginManager().registerEvents(new GuiListener(), plugin);
    }

}
