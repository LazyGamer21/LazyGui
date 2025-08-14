package me.ericdavis.lazygui.gui;

import me.ericdavis.lazygui.item.GuiItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractGuiPage implements InventoryHolder {

    final JavaPlugin plugin;

    private final String displayName;
    private final int rows;
    private Inventory guiPage;

    private final HashMap<Integer, GuiItem> guiItems = new HashMap<>();

    protected ItemStack itemToAssign;

    //? >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    //? HOW TO USE                                                 >>
    //? extend this class to make a custom page                    >>
    //? call the page's constructor when enabling the plugin       >>
    //? >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    public AbstractGuiPage(JavaPlugin plugin, String pageIdentifier)
    {
        this.plugin = plugin;
        this.displayName = getDisplayName();
        this.rows = getRows();

        GuiManager.getInstance().registerPage(this, pageIdentifier);

        assignItems();

        createInventory();
    }

    protected abstract String getDisplayName();

    protected abstract int getRows();

    protected abstract void assignItems();

    /**
     *
     * @param player The player to open the inventory for
     */
    public void open(Player player)
    {
        player.closeInventory();

        Bukkit.getScheduler().runTaskLater(plugin, () -> player.openInventory(guiPage), 1);
    }

    /**
     *
     * @param slot
     * @param item
     * @implNote This called in the constructor {@link GuiItem} - To use this create new {@link GuiItem}s in assignItems
     */
    public void assignItem(int slot, GuiItem item)
    {
        if (slot >= rows * 9)
        {
            Bukkit.getLogger().warning("Attempted to assign a GUI item to a slot that doesn't exist! Item: " + displayName);
            return;
        }

        guiItems.put(slot, item);
    }

    /**
     * Creates the {@link Inventory} for this page once all items have been assigned
     */
    private void createInventory()
    {
        if (rows < 1 || rows > 6)
        {
            Bukkit.getLogger().severe("GUI rows must be 1-6 - rows is currently set to " + rows + " for AbstractGuiPage " + ChatColor.stripColor(displayName));
            return;
        }

        guiPage = Bukkit.createInventory(this, rows * 9, displayName);

        for (Map.Entry<Integer, GuiItem> entry : guiItems.entrySet())
        {
            guiPage.setItem(entry.getKey(), entry.getValue().getItem());
        }
    }

    public void handleClick(InventoryClickEvent e) {
        if (e.getCurrentItem() == null) return;
        ItemMeta meta = e.getCurrentItem().getItemMeta();
        if (meta == null) return;
        GuiItem customItem = guiItems.get(e.getSlot());
        if (customItem == null) return;

        customItem.onClick(e);
    }

    public Inventory getInventory() {return guiPage;}

    public ItemStack getItemToAssign() {
        return itemToAssign;
    }

}