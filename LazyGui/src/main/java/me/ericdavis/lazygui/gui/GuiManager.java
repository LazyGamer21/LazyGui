package me.ericdavis.lazygui.gui;

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
        if (!(e.getInventory().getHolder() instanceof AbstractGuiPage guiPage)) return;
        e.setCancelled(true);
        guiPage.handleClick(e);
    }

    public void registerPage(AbstractGuiPage page, String pageIdentifier) {
        guiPages.put(pageIdentifier, page);
    }

    public void openPage(String pageIdentifier, Player player) {
        guiPages.get(pageIdentifier).open(player);
    }

}
