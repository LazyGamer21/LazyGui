package me.ericdavis.lazygui.item;

import me.ericdavis.lazygui.gui.AbstractGuiPage;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class GuiItem {

    private final ItemStack item;
    private final Consumer<InventoryClickEvent> action;

    public GuiItem(AbstractGuiPage page, Integer slot, Consumer<InventoryClickEvent> action) {
        page.assignItem(slot, this);
        this.item = page.getItemToAssign();
        this.action = action;
    }

    public ItemStack getItem() {
        return item;
    }

    public void onClick(InventoryClickEvent event) {
        if (action != null) {
            action.accept(event);
        }
    }
}
