package me.ericdavis.lazyGui.guiPage;

import me.ericdavis.lazyGui.guiItem.AbstractGuiItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractGuiPage implements InventoryHolder {

    final JavaPlugin plugin;

    public static final HashMap<String, AbstractGuiPage> guiPages = new HashMap<>();

    private static Inventory guiPage;

    String displayName; // name shown at the top of the GUI page
    int rows; // how many rows it has (each row is 9 slots)

    HashMap<Integer, ItemStack> guiItems = new HashMap<>(); // a record of the items to create when opening this gui

    public AbstractGuiPage(JavaPlugin plugin, String pageId)
    {
        this.plugin = plugin;
        this.displayName = getDisplayName();
        this.rows = getRows();

        guiPages.put(pageId, this);
        Bukkit.broadcastMessage("Enabled " + this.getDisplayName());
    }

    public void open(Player p)
    {
        p.closeInventory();

        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                p.openInventory(guiPage);
            }
        }, 1);
    }

    // construct a guiPage in GuiManager and then assign items. Once items are assigned you can just call guiPageChild.open(p)
    public void assignItem(int slot, AbstractGuiItem item)
    {
        if (slot >= rows * 9)
        {
            Bukkit.getLogger().warning("Attempted to assign a GUI item to a slot that doesn't exist! Item: " + displayName);
            return;
        }

        guiItems.put(slot, item);
    }

    protected void createInventory()
    {
        if (rows < 1 || rows > 6)
        {
            Bukkit.getLogger().severe("GUI rows must be 1-6 - rows is currently set to " + rows + " for AbstractGuiPage " + ChatColor.stripColor(displayName));
            return;
        }

        guiPage = Bukkit.createInventory(this, rows * 9, displayName);

        for (Map.Entry<Integer, ItemStack> entry : guiItems.entrySet())
        {
            guiPage.setItem(entry.getKey(), entry.getValue());
        }
    }

    public Inventory getInventory() {return guiPage;}

    public abstract String getDisplayName();

    protected abstract int getRows();

    public abstract void handleClick(InventoryClickEvent e);

}
