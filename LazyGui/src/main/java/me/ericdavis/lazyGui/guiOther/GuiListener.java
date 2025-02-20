package me.ericdavis.lazyGui.guiOther;

import me.ericdavis.lazyGui.guiPage.AbstractGuiPage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GuiListener implements Listener {

    @EventHandler
    public void onGuiClick(InventoryClickEvent e)
    {
        if (!(e.getInventory().getHolder() instanceof AbstractGuiPage guiPage)) return;
        e.setCancelled(true);
        guiPage.handleClick(e);
    }

}
