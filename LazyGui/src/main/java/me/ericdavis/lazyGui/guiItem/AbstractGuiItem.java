package me.ericdavis.lazyGui.guiItem;

import me.ericdavis.lazyGui.guiPage.AbstractGuiPage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.HashMap;

public abstract class AbstractGuiItem extends ItemStack {

    public static final NamespacedKey ITEM_KEY = new NamespacedKey(JavaPlugin.getProvidingPlugin(AbstractGuiItem.class), "custom_item_id");

    public static HashMap<String, AbstractGuiItem> guiItems = new HashMap<>();

    public AbstractGuiItem(Material material, int customModelNumber, String itemId)
    {
        super(material);
        ItemMeta meta = getItemMeta();

        if (meta != null)
        {
            meta.setCustomModelData(customModelNumber);
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(ITEM_KEY, PersistentDataType.STRING, itemId);
            guiItems.put(itemId, this);

            setItemMeta(meta);
        }
    }

    public AbstractGuiItem(Material material, int customModelNumber, String itemId, String name)
    {
        super(material);
        ItemMeta meta = getItemMeta();

        if (meta != null)
        {
            meta.setCustomModelData(customModelNumber);
            meta.setDisplayName(name);
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(ITEM_KEY, PersistentDataType.STRING, itemId);
            guiItems.put(itemId, this);

            setItemMeta(meta);
        }
    }

    public AbstractGuiItem(Material material, int customModelNumber, String itemId, String name, String... lore)
    {
        super(material);
        ItemMeta meta = getItemMeta();

        if (meta == null) {
            throw new IllegalStateException("ItemMeta is null for material: " + material);
        }

        meta.setCustomModelData(customModelNumber);
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(ITEM_KEY, PersistentDataType.STRING, itemId);
        guiItems.put(itemId, this);

        setItemMeta(meta);
    }

    public static AbstractGuiItem getGuiItem(ItemStack item)
    {
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return null;

        String itemKey = meta.getPersistentDataContainer().get(AbstractGuiItem.ITEM_KEY, PersistentDataType.STRING);

        if (!AbstractGuiItem.guiItems.containsKey(itemKey)) return null;

        return AbstractGuiItem.guiItems.get(itemKey);
    }

    public abstract void onClicked(InventoryClickEvent e, AbstractGuiPage abstractGuiPage);

}
