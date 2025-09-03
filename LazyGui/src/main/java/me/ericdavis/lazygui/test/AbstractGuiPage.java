package me.ericdavis.lazygui.test;

import me.ericdavis.lazygui.item.GuiItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public abstract class AbstractGuiPage implements InventoryHolder {

    final JavaPlugin plugin;

    private final String displayName;
    private final int rows;
    private Inventory guiPage;
    private String parentPageId = null;
    private final boolean fillBorder;
    private final boolean autoGenBackButton;

    private int currentPage = 0;
    private List<GuiItem> itemsToShow;

    protected Material borderMaterial = Material.GRAY_STAINED_GLASS_PANE;

    private final HashMap<Integer, GuiItem> guiItems = new HashMap<>();

    //? >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    //? HOW TO USE                                                 >>
    //? extend this class to make a custom page                    >>
    //? call the page's constructor when enabling the plugin       >>
    //? >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    /**
     *
     * @param plugin Reference to the main plugin class
     * @param fillBorder will fill empty border tiles with glass panes
     * @implNote Recommendation: Create a public static String for the pageIdentifier to make opening pages easier
     */
    public AbstractGuiPage(JavaPlugin plugin, boolean fillBorder)
    {
        this.plugin = plugin;
        this.displayName = getDisplayName();
        this.rows = getRows();
        this.fillBorder = fillBorder;
        this.autoGenBackButton = false;

        GuiManager.getInstance().registerPage(this, getPageIdentifier());

        createInventory();
    }

    /**
     *
     * @param plugin Reference to the main plugin class
     * @param fillBorder will fill empty border tiles with glass panes
     * @param parentPageId allows openParentPage for easier access to parent page
     * @param autoGenBackButton
     */
    public AbstractGuiPage(JavaPlugin plugin, boolean fillBorder, String parentPageId, boolean autoGenBackButton)
    {
        this.plugin = plugin;
        this.displayName = getDisplayName();
        this.rows = getRows();
        this.fillBorder = fillBorder;
        this.autoGenBackButton = autoGenBackButton;
        this.parentPageId = parentPageId;

        GuiManager.getInstance().registerPage(this, getPageIdentifier());

        createInventory();
    }

    protected abstract String getDisplayName();

    protected abstract int getRows();

    protected abstract void assignItems();

    public abstract String getPageIdentifier();

    protected abstract List<GuiItem> getListedButtons();

    /**
     *
     * @param player The player to open the inventory for
     */
    public void open(Player player) {
        player.closeInventory();

        setAssignedItems();

        Bukkit.getScheduler().runTaskLater(plugin, () -> player.openInventory(guiPage), 1);
    }

    /**
     *
     * @param player The player to open the inventory for
     * @implNote Just an easier way of keeping track of what page came before this if there are multiple pages to one GUI
     */
    public void openParentPage(Player player) {
        if (parentPageId == null) return;

        if (!GuiManager.getInstance().openPage(parentPageId, player)) {
            Bukkit.getLogger().warning("[MCOHexRoyale] Failed to open parent page for page: " + getPageIdentifier());
        }
    }

    /**
     *
     * @param slot
     * @param item
     */
    protected void assignItem(int slot, GuiItem item) {
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
    private void createInventory() {
        if (rows < 1 || rows > 6)
        {
            Bukkit.getLogger().severe("GUI rows must be 1-6 - rows is currently set to " + rows + " for AbstractGuiPage " + ChatColor.stripColor(displayName));
            return;
        }

        if (guiPage == null) guiPage = Bukkit.createInventory(this, rows * 9, displayName);

        setAssignedItems();
    }

    // Add a method to refresh the inventory smoothly
    public void refreshInventory() {
        // Clear existing items
        this.guiItems.clear();

        setAssignedItems();
    }

    private void setAssignedItems() {
        assignItems();

        if (fillBorder) fillBorder();

        if (autoGenBackButton && parentPageId != null) {

            assignItem(guiPage.getSize() - 5, new GuiItem(Material.BARRIER, e -> {
                if (!(e.getWhoClicked() instanceof Player)) return;
                Player player = (Player) e.getWhoClicked();
                openParentPage(player);
            }).setName(ChatColor.GRAY + "Previous Page").build());

        }

        for (Map.Entry<Integer, GuiItem> entry : guiItems.entrySet())
        {
            guiPage.setItem(entry.getKey(), entry.getValue().getItem());
        }

        // Now handle the "listed" items that should page
        List<GuiItem> listed = getListedButtons();
        if (listed != null && !listed.isEmpty()) {
            // find all empty slots
            List<Integer> emptySlots = new java.util.ArrayList<>();
            for (int i = 0; i < guiPage.getSize(); i++) {
                if (isEmpty(guiPage, i)) {
                    emptySlots.add(i);
                }
            }

            int openSlots = emptySlots.size();
            int startIndex = currentPage * openSlots;
            int endIndex = Math.min(startIndex + openSlots, listed.size());

            List<GuiItem> pageItems = listed.subList(startIndex, endIndex);

            // assign the listed items into the empty slots
            for (int i = 0; i < pageItems.size(); i++) {
                int slot = emptySlots.get(i);
                assignItem(slot, pageItems.get(i));
            }

            // Navigation buttons (if more than one page)
            if (endIndex < listed.size()) {
                assignItem(guiPage.getSize() - 3, new GuiItem(Material.ARROW, e -> {
                    currentPage++;
                    refreshInventory();
                    Player player = (Player) e.getWhoClicked();
                    open(player);
                }).setName(ChatColor.GREEN + "Next Page").build());
            }

            if (currentPage > 0) {
                assignItem(guiPage.getSize() - 7, new GuiItem(Material.ARROW, e -> {
                    currentPage--;
                    refreshInventory();
                    Player player = (Player) e.getWhoClicked();
                    open(player);
                }).setName(ChatColor.RED + "Previous Page").build());
            }
        }
    }

    /**
     * @implNote Run in createInventory(), to enable set fillBorder to true
     */
    private void fillBorder() {
        int size = guiPage.getSize();
        int rows = size / 9;

        ItemStack pane = new ItemStack(borderMaterial);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RESET.toString()); // Blank name
            pane.setItemMeta(meta);
        }

        // Fill top row
        for (int i = 0; i < 9; i++) {
            if (isEmpty(guiPage, i)) {
                guiPage.setItem(i, pane);
            }
        }

        // Fill bottom row
        for (int i = size - 9; i < size; i++) {
            if (isEmpty(guiPage, i)) {
                guiPage.setItem(i, pane);
            }
        }

        // Fill left and right columns
        for (int row = 1; row < rows - 1; row++) {
            int left = row * 9;
            int right = row * 9 + 8;

            if (isEmpty(guiPage, left)) {
                guiPage.setItem(left, pane);
            }
            if (isEmpty(guiPage, right)) {
                guiPage.setItem(right, pane);
            }
        }
    }

    private boolean isEmpty(Inventory inventory, int slot) {
        ItemStack item = inventory.getItem(slot);
        return item == null || item.getType() == Material.AIR;
    }

    public void handleClick(InventoryClickEvent e) {
        if (e.getCurrentItem() == null) return;
        ItemMeta meta = e.getCurrentItem().getItemMeta();
        if (meta == null) return;
        GuiItem customItem = guiItems.get(e.getSlot());
        if (customItem == null) return;

        customItem.onClick(e);
    }

    @Override
    public Inventory getInventory() {
        return guiPage;
    }

}