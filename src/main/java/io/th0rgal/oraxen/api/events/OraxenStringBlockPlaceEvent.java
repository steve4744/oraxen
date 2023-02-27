package io.th0rgal.oraxen.api.events;

import io.th0rgal.oraxen.mechanics.provided.gameplay.stringblock.StringBlockMechanic;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OraxenStringBlockPlaceEvent extends Event implements Cancellable {

    private final StringBlockMechanic mechanic;
    private final Player player;
    private final Block block;
    private boolean isCancelled;
    private static final HandlerList HANDLERS = new HandlerList();

    public OraxenStringBlockPlaceEvent(StringBlockMechanic mechanic, Block block, @Nullable Player player) {
        this.mechanic = mechanic;
        this.player = player;
        this.block = block;
        this.isCancelled = false;
    }

    /**
     * @return The StringBlockMechanic of this block
     */
    public StringBlockMechanic getMechanic() {
        return mechanic;
    }

    /**
     * @return The player who broke this block, or null if the event is not triggered by player
     */
    @Nullable
    public Player getPlayer() {
        return player;
    }

    /**
     * @return The block that was placed
     */
    public Block getBlock() {
        return block;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        isCancelled = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}

