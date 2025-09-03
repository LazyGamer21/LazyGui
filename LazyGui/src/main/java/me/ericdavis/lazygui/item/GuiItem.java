package me.ericdavis.lazygui.item;

import me.ericdavis.lazygui.test.AbstractGuiPage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GuiItem {

    private final ItemStack item;
    private final ItemMeta meta;

    private final Consumer<InventoryClickEvent> action;

    public GuiItem(Material material, Consumer<InventoryClickEvent> action) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
        this.action = action;
    }

    public void onClick(InventoryClickEvent event) {
        if (action != null) {
            action.accept(event);
        }
    }

    public GuiItem setName(String name) {
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        return this;
    }

    public GuiItem setLore(String... lore) {
        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(coloredLore);
        return this;
    }

    public GuiItem setLore(List<String> lore) {
        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(coloredLore);
        return this;
    }

    public GuiItem setAmount(int amount) {
        item.setAmount(amount);
        return this;
    }

    public GuiItem setSkullOwner(Player target) {
        if (!(item.getType() == Material.PLAYER_HEAD || item.getType() == Material.PLAYER_WALL_HEAD)) {
            throw new IllegalStateException("Item is not a player head!");
        }

        SkullMeta skullMeta = (SkullMeta) meta;
        skullMeta.setOwningPlayer(target);
        return this;
    }

    public GuiItem build() {
        item.setItemMeta(meta);
        return this;
    }

    public ItemStack getItem() {
        return item;
    }
}
