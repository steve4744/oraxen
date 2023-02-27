package io.th0rgal.oraxen.mechanics.provided.misc.food;

import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.PlayerInventory;

public class FoodMechanicListener implements Listener {
    private final FoodMechanicFactory factory;

    public FoodMechanicListener(FoodMechanicFactory factory) {
        this.factory = factory;
    }

    @EventHandler
    public void onEatingFood(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();
        String itemID = OraxenItems.getIdByItem(event.getItem());
        if (itemID == null || factory.isNotImplementedIn(itemID)) return;
        FoodMechanic mechanic = (FoodMechanic) factory.getMechanic(itemID);
        event.setCancelled(true);

        if (player.getGameMode() != GameMode.CREATIVE) {
            if (mechanic.hasReplacement())
                inventory.setItemInMainHand(mechanic.getReplacement());
            else inventory.getItemInMainHand().setAmount(inventory.getItemInMainHand().getAmount() - 1);

            if (mechanic.hasEffects() && Math.random() <= mechanic.getEffectProbability())
                player.addPotionEffects(mechanic.getEffects());
        }

        player.setFoodLevel(player.getFoodLevel() + Math.min(mechanic.getHunger(), 20));
        player.setSaturation(player.getSaturation() + Math.min(mechanic.getSaturation(), 20));
    }
}
