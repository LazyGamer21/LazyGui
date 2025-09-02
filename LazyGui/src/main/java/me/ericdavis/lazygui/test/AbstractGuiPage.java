package me.ericdavis.lazygui.test;

import me.ericdavis.lazygui.item.GuiItem;
import me.ericdavis.lazygui.item.ItemBuilder;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class AbstractGuiPage implements InventoryHolder {

    final JavaPlugin plugin;

    private final String displayName;
    private final int rows;
    private Inventory guiPage;
    private String parentPageId = null;
    private final boolean fillBorder;
    private final boolean updatingPage;
    private final boolean autoGenBackButton;

    private final boolean isListed;
    private List<?> listItems; // The list of items to display
    private Function<Object, GuiItem> itemGenerator; // Function to create GuiItems from list items
    private int listStartSlot = 10; // Default starting slot for list items (row 2, col 2)
    private int itemsPerPage = 28; // Default items that can fit in a 6-row inventory

    protected Material borderMaterial = Material.GRAY_STAINED_GLASS_PANE;

    private final HashMap<Integer, GuiItem> guiItems = new HashMap<>();

    protected ItemStack itemToAssign;

    //? >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    //? HOW TO USE                                                 >>
    //? extend this class to make a custom page                    >>
    //? call the page's constructor when enabling the plugin       >>
    //? >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    /**
     *
     * @param plugin Reference to the main plugin class
     * @param fillBorder will fill empty border tiles with glass panes
     * @param updatingPage will run assignItems() every time the page is opened to allow for variable buttons
     * @param isListed
     * @implNote Recommendation: Create a public static String for the pageIdentifier to make opening pages easier
     */
    public AbstractGuiPage(JavaPlugin plugin, boolean fillBorder, boolean updatingPage, boolean isListed)
    {
        this.plugin = plugin;
        this.displayName = getDisplayName();
        this.rows = getRows();
        this.fillBorder = fillBorder;
        this.updatingPage = updatingPage;
        this.isListed = isListed;
        this.autoGenBackButton = false;

        GuiManager.getInstance().registerPage(this, getPageIdentifier());

        createInventory();
    }

    /**
     *
     * @param plugin Reference to the main plugin class
     * @param fillBorder will fill empty border tiles with glass panes
     * @param updatingPage will run assignItems() every time the page is opened to allow for variable buttons
     * @param isListed
     * @param parentPageId allows openParentPage for easier access to parent page
     * @param autoGenBackButton
     */
    public AbstractGuiPage(JavaPlugin plugin, boolean fillBorder, boolean updatingPage, boolean isListed, String parentPageId, boolean autoGenBackButton)
    {
        this.plugin = plugin;
        this.displayName = getDisplayName();
        this.rows = getRows();
        this.fillBorder = fillBorder;
        this.updatingPage = updatingPage;
        this.isListed = isListed;
        this.autoGenBackButton = autoGenBackButton;
        this.parentPageId = parentPageId;

        GuiManager.getInstance().registerPage(this, getPageIdentifier());

        createInventory();
    }

    protected abstract String getDisplayName();

    protected abstract int getRows();

    protected abstract void assignItems();

    public abstract String getPageIdentifier();

    /**
     *
     * @param player The player to open the inventory for
     */
    public void open(Player player) {
        player.closeInventory();

        // reassign items to update them
        if (updatingPage) assignItemsHelper();

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

        if (guiPage == null) guiPage = Bukkit.createInventory(this, rows * 9, displayName);

        assignItemsHelper();
    }

    // Add a method to refresh the inventory smoothly
    public void refreshInventory() {
        // Clear existing items
        this.guiItems.clear();

        assignItemsHelper();
    }

    private void assignItemsHelper() {
        assignItems();

        // Handle listed content
        if (isListed && listItems != null && itemGenerator != null) {
            generateListedItems();
        }

        if (fillBorder) fillBorder();

        if (autoGenBackButton && parentPageId != null) {
            int slot;
            if (fillBorder) slot = 37;
            else slot = 45;
            itemToAssign = new ItemBuilder(Material.OAK_LOG)
                    .setName(ChatColor.GRAY + "Previous Page")
                    .build();
            new GuiItem(this, slot, e -> {
                if (!(e.getWhoClicked() instanceof Player)) return;
                Player player = (Player) e.getWhoClicked();
                openParentPage(player);
            });
        }

        for (Map.Entry<Integer, GuiItem> entry : guiItems.entrySet())
        {
            guiPage.setItem(entry.getKey(), entry.getValue().getItem());
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

    // Make guiItems accessible
    public HashMap<Integer, GuiItem> getGuiItems() {
        return this.guiItems;
    }

    // Make fillBorder accessible for refresh
    public void refreshBorder(Inventory inventory) {
        if (this.fillBorder) {
            this.fillBorder();
        }
    }

    /**
     * Set the list of items to display and the function to generate GUI items from them
     * @param listItems The list of objects to display
     * @param itemGenerator Function that converts list items to GuiItems
     */
    public void setListItems(List<?> listItems, Function<Object, GuiItem> itemGenerator) {
        this.listItems = listItems;
        this.itemGenerator = itemGenerator;
        calculateItemsPerPage();
    }

    public void setListStartSlot(int listStartSlot) {
        this.listStartSlot = listStartSlot;
        calculateItemsPerPage();
    }

    /**
     * Get the current page of list items (for pagination)
     */
    public List<?> getCurrentPageItems(int page) {
        if (listItems == null) return List.of();

        int start = page * itemsPerPage;
        int end = Math.min(start + itemsPerPage, listItems.size());

        if (start >= listItems.size()) return List.of();

        return listItems.subList(start, end);
    }

    /**
     * Calculate how many items can fit on the page based on inventory size and fillBorder
     */
    private void calculateItemsPerPage() {
        if (fillBorder) {
            // Account for border - items can only be placed in the center area
            this.itemsPerPage = (rows - 2) * 7; // (rows - top/bottom border) * (9 - left/right border)
        } else {
            // Full inventory available
            this.itemsPerPage = rows * 9;
        }
    }

    /**
     * Generate GUI items from the list
     */
    private void generateListedItems() {
        int currentSlot = listStartSlot;

        for (Object item : listItems) {
            if (currentSlot >= rows * 9) break; // Don't exceed inventory size

            GuiItem guiItem = itemGenerator.apply(item);
            assignItem(currentSlot, guiItem);

            currentSlot++;

            // Skip border slots if fillBorder is enabled
            if (fillBorder) {
                int row = currentSlot / 9;
                int col = currentSlot % 9;

                // If we're at the right border, move to next row
                if (col == 8) {
                    currentSlot += 2; // Skip left border of next row
                }
            }
        }
    }

    public Inventory getInventory() {return guiPage;}

    public ItemStack getItemToAssign() {
        return itemToAssign;
    }

}