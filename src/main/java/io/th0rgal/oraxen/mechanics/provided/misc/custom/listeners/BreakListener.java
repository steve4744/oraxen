package io.th0rgal.oraxen.mechanics.provided.misc.custom.listeners;

import io.th0rgal.oraxen.items.OraxenItems;
import io.th0rgal.oraxen.mechanics.provided.misc.custom.fields.CustomEvent;
import io.th0rgal.oraxen.utils.actions.ClickAction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;

public class BreakListener extends CustomListener {

    public BreakListener(String itemID, long cooldown, CustomEvent event, ClickAction clickAction) {
        super(itemID, cooldown, event, clickAction);
    }

    @EventHandler
    public void onBroken(PlayerItemBreakEvent event) {
        ItemStack item = event.getBrokenItem();
        if (!itemID.equals(OraxenItems.getIdByItem(item)))
            return;
        perform(event.getPlayer(), item);
    }
}
