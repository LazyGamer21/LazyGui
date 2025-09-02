package me.ericdavis.lazygui.test;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.HashMap;

public class GuiManager implements Listener {

    private final static HashMap<String, AbstractGuiPage> guiPages = new HashMap<>();

    private static GuiManager instance;

    private GuiManager() {}

    public static GuiManager getInstance() {
        if (instance == null) {
            instance = new GuiManager();
        }
        return instance;
    }

    @EventHandler
    public void onGuiClick(InventoryClickEvent e)
    {
        if (!(e.getInventory().getHolder() instanceof AbstractGuiPage)) return;
        AbstractGuiPage guiPage = (AbstractGuiPage) e.getInventory().getHolder();
        e.setCancelled(true);
        guiPage.handleClick(e);
    }

    public void registerPage(AbstractGuiPage page, String pageIdentifier) {
        if (guiPages.containsKey(pageIdentifier)) Bukkit.getLogger().warning("[LazyGui] At Least Two Pages Share a Page Identifier String: " + pageIdentifier);
        guiPages.put(pageIdentifier, page);
    }

    /**
     *
     * @param pageIdentifier Identifier String for the page to open -- should be unique to each page
     * @param player The player to open the page for
     * @return If the page was successfully opened
     */
    public boolean openPage(String pageIdentifier, Player player) {
        AbstractGuiPage pageToOpen = guiPages.get(pageIdentifier);
        if (pageToOpen == null) return false;
        pageToOpen.open(player);
        return true;
    }

    /**
     *
     * @implNote Will reload variable buttons in all pages that are open
     */
    public void refreshPages() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (AbstractGuiPage pageToOpen : guiPages.values()) {

                if (pageToOpen == null) continue;

                if (!(player.getOpenInventory().getTopInventory().getHolder() instanceof AbstractGuiPage)) continue;

                AbstractGuiPage openPage = (AbstractGuiPage) player.getOpenInventory().getTopInventory().getHolder();
                if (!openPage.getPageIdentifier().equalsIgnoreCase(pageToOpen.getPageIdentifier())) continue;

                // Smooth refresh without closing/reopening
                openPage.refreshInventory();
            }
        }
    }

}
