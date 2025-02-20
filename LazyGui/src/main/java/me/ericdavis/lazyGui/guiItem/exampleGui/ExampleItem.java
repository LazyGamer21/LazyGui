package me.ericdavis.lazyGui.guiItem.exampleGui;

import me.ericdavis.lazyGui.guiItem.AbstractGuiItem;
import me.ericdavis.lazyGui.guiPage.AbstractGuiPage;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ExampleItem extends AbstractGuiItem {

    public ExampleItem(Material material, String name, String... lore) {
        super(material,
                1,
                "test_item",
                name,
                lore);
    }

    @Override
    public void onClicked(InventoryClickEvent e, AbstractGuiPage abstractGuiPage) {
        HumanEntity p = e.getWhoClicked();

        p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 20 * 10, 2, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 10, 2, false, false));
        p.sendMessage(ChatColor.LIGHT_PURPLE + "Zoom Zoom Weeeee");
    }

}
