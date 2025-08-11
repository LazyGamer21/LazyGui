package me.ericdavis.lazyGui.guiPage.exampleGui;

import me.ericdavis.lazyGui.guiItem.AbstractGuiItem;
import me.ericdavis.lazyGui.guiItem.exampleGui.ExampleItem;
import me.ericdavis.lazyGui.guiPage.AbstractGuiPage;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class ExampleMainPage extends AbstractGuiPage {

    public static String pageId = "example";

    public ExampleMainPage(JavaPlugin plugin) {
        super(plugin, pageId);

        assignItem(23, new ExampleItem(Material.STONE, "Test Item", "This is a test Item", "Hi"));

        createInventory();
    }

    @Override
    public String getDisplayName() {
        return ChatColor.AQUA + "Example Main";
    }

    @Override
    protected int getRows() {
        return 6;
    }

}
