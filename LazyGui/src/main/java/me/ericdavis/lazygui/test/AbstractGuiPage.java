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

    private String displayName;
    private int rows;
    private String parentPageId = null;
    private final boolean fillBorder;
    private final boolean autoGenBackButton;
    private final boolean buttonsFollowListPages;

    protected Material borderMaterial = Material.GRAY_STAINED_GLASS_PANE;

    private final HashMap<UUID, Integer> currentPages = new HashMap<>();
    private final HashMap<UUID, Map<Integer, GuiItem>> guiItems = new HashMap<>();
    private final HashMap<UUID, Inventory> guiPages = new HashMap<>();

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
    public AbstractGuiPage(JavaPlugin plugin, boolean fillBorder, boolean buttonsFollowListPages)
    {
        this.plugin = plugin;
        this.rows = getRows();
        this.fillBorder = fillBorder;
        this.autoGenBackButton = false;
        this.buttonsFollowListPages = buttonsFollowListPages;

        GuiManager.getInstance().registerPage(this, getPageIdentifier());
    }

    /**
     *
     * @param plugin Reference to the main plugin class
     * @param fillBorder will fill empty border tiles with glass panes
     * @param parentPageId allows openParentPage for easier access to parent page
     * @param autoGenBackButton will automatically generate the back button to return to the parent page
     */
    public AbstractGuiPage(JavaPlugin plugin, boolean fillBorder, boolean buttonsFollowListPages, String parentPageId, boolean autoGenBackButton)
    {
        this.plugin = plugin;
        this.rows = getRows();
        this.fillBorder = fillBorder;
        this.autoGenBackButton = autoGenBackButton;
        this.parentPageId = parentPageId;
        this.buttonsFollowListPages = buttonsFollowListPages;

        GuiManager.getInstance().registerPage(this, getPageIdentifier());
    }

    protected abstract String getDisplayName(UUID playerId);

    protected abstract int getRows();

    protected abstract void assignItems(UUID playerId);

    public abstract String getPageIdentifier();

    protected abstract List<GuiItem> getListedButtons();

    /**
     *
     * @param player The player to open the inventory for
     */
    public void open(Player player) {
        player.closeInventory();
        UUID playerId = player.getUniqueId();

        currentPages.put(playerId, 0);
        this.displayName = getDisplayName(playerId);
        this.rows = getRows();

        createInventory(playerId);
        refreshInventory(playerId);

        Bukkit.getScheduler().runTaskLater(plugin, () -> player.openInventory(guiPages.get(playerId)), 1);
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
     * @param slot the slot to assign the item to
     * @param item the item to assign
     */
    protected void assignItem(UUID playerId, int slot, GuiItem item) {
        if (slot >= rows * 9)
        {
            Bukkit.getLogger().warning("Attempted to assign a GUI item to a slot that doesn't exist! Item: " + displayName);
            return;
        }

        guiItems.computeIfAbsent(playerId, k -> new HashMap<>()).put(slot, item);
    }

    /**
     * Creates the {@link Inventory} for this page once all items have been assigned
     */
    private void createInventory(UUID playerId) {
        if (rows < 1 || rows > 6)
        {
            Bukkit.getLogger().severe("GUI rows must be 1-6 - rows is currently set to " + rows + " for AbstractGuiPage " + ChatColor.stripColor(displayName));
            return;
        }

        guiPages.put(playerId, Bukkit.createInventory(this, rows * 9, displayName));
    }

    // Add a method to refresh the inventory smoothly
    public void refreshInventory(UUID playerId) {
        // Clear existing items
        if (guiItems.get(playerId) != null) guiItems.get(playerId).clear();
        if (guiPages.get(playerId) != null) guiPages.get(playerId).clear();

        setAssignedItems(playerId);
    }

    private void setAssignedItems(UUID playerId) {

        if (currentPages.get(playerId) == 0 || buttonsFollowListPages) {
            assignItems(playerId);
        }

        if (fillBorder) fillBorder(guiPages.get(playerId));

        Inventory playerPage = guiPages.get(playerId);
        int currentPage = currentPages.get(playerId);

        if (autoGenBackButton && parentPageId != null) {

            assignItem(playerId, playerPage.getSize() - 5, new GuiItem(Material.BARRIER, e -> {
                if (!(e.getWhoClicked() instanceof Player)) return;
                Player player = (Player) e.getWhoClicked();
                openParentPage(player);
            }).setName(ChatColor.GRAY + "Previous Page"));

        }

        Map<Integer, GuiItem> playerItems = guiItems.get(playerId);
        if (playerItems != null) {
            for (Map.Entry<Integer, GuiItem> entry : playerItems.entrySet()) {
                playerPage.setItem(entry.getKey(), entry.getValue().getItem());
            }
        }

        // Now handle the "listed" items that should page
        List<GuiItem> listed = getListedButtons();

        if (listed == null || listed.isEmpty()) return;

        // find all empty slots
        List<Integer> emptySlots = new java.util.ArrayList<>();
        for (int i = 0; i < playerPage.getSize(); i++) {
            if (isEmpty(playerPage, i)) {
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
            GuiItem item = pageItems.get(i);
            assignItem(playerId, slot, item);
            playerPage.setItem(slot, item.getItem()); // <- update inventory right away
        }

        // Navigation buttons
        if (endIndex < listed.size()) {
            GuiItem nextBtn = new GuiItem(Material.ARROW, e -> {
                currentPages.put(playerId, currentPage + 1);
                refreshInventory(playerId);
            }).setName(ChatColor.GREEN + "Next Page");

            assignItem(playerId, playerPage.getSize() - 3, nextBtn);
            playerPage.setItem(playerPage.getSize() - 3, nextBtn.getItem()); // <- add this
        }

        if (currentPage > 0) {
            GuiItem prevBtn = new GuiItem(Material.ARROW, e -> {
                currentPages.put(playerId, currentPage - 1);
                refreshInventory(playerId);
            }).setName(ChatColor.RED + "Previous Page");

            assignItem(playerId, playerPage.getSize() - 7, prevBtn);
            playerPage.setItem(playerPage.getSize() - 7, prevBtn.getItem()); // <- add this
        }
    }

    /**
     * @implNote Run in createInventory(), to enable set fillBorder to true
     */
    private void fillBorder(Inventory inv) {
        if (inv == null) return;

        int size = inv.getSize();
        int rows = size / 9;

        ItemStack pane = new ItemStack(borderMaterial);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RESET.toString()); // Blank name
            pane.setItemMeta(meta);
        }

        // Fill top row
        for (int i = 0; i < 9; i++) {
            if (isEmpty(inv, i)) {
                inv.setItem(i, pane);
            }
        }

        // Fill bottom row
        for (int i = size - 9; i < size; i++) {
            if (isEmpty(inv, i)) {
                inv.setItem(i, pane);
            }
        }

        // Fill left and right columns
        for (int row = 1; row < rows - 1; row++) {
            int left = row * 9;
            int right = row * 9 + 8;

            if (isEmpty(inv, left)) {
                inv.setItem(left, pane);
            }
            if (isEmpty(inv, right)) {
                inv.setItem(right, pane);
            }
        }
    }

    private boolean isEmpty(Inventory inventory, int slot) {
        ItemStack item = inventory.getItem(slot);
        return item == null || item.getType() == Material.AIR;
    }

    public void handleClick(InventoryClickEvent e) {
        UUID playerId = ((Player) e.getWhoClicked()).getUniqueId();
        Map<Integer, GuiItem> items = guiItems.get(playerId);
        if (items == null) return;

        GuiItem customItem = items.get(e.getSlot());
        if (customItem == null) return;

        customItem.onClick(e);
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

}