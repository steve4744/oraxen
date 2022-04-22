package io.th0rgal.oraxen.mechanics.provided.gameplay.furniture.evolution;

import io.th0rgal.oraxen.mechanics.provided.gameplay.furniture.BlockLocation;
import io.th0rgal.oraxen.mechanics.provided.gameplay.furniture.FurnitureFactory;
import io.th0rgal.oraxen.mechanics.provided.gameplay.furniture.FurnitureMechanic;
import org.bukkit.*;
import org.bukkit.entity.ItemFrame;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public class EvolutionTask extends BukkitRunnable {

    private final FurnitureFactory furnitureFactory;
    private final int delay;

    public EvolutionTask(FurnitureFactory furnitureFactory, int delay) {
        this.furnitureFactory = furnitureFactory;
        this.delay = delay;
    }

    @Override
    public void run() {
        for (World world : Bukkit.getWorlds())
            for (ItemFrame frame : world.getEntitiesByClass(ItemFrame.class))
                if (frame.getPersistentDataContainer().has(FurnitureMechanic.EVOLUTION_KEY,
                        PersistentDataType.INTEGER)) {

                    String itemID = frame.getPersistentDataContainer()
                            .get(FurnitureMechanic.FURNITURE_KEY, PersistentDataType.STRING);
                    FurnitureMechanic mechanic = (FurnitureMechanic) furnitureFactory.getMechanic(itemID);

                    if (mechanic.farmlandRequired &&
                            frame.getLocation().clone().subtract(0, 1, 0).getBlock().getType()
                                    != Material.FARMLAND) {
                        mechanic.remove(frame);
                        continue;
                    }
                    EvolvingFurniture evolution = mechanic.getEvolution();
                    int evolutionStep = frame.getPersistentDataContainer()
                            .get(FurnitureMechanic.EVOLUTION_KEY, PersistentDataType.INTEGER)
                            + delay * frame.getLocation().getBlock().getLightLevel();
                    if (evolutionStep > evolution.getDelay()) {
                        if (!evolution.bernoulliTest())
                            continue;
                        mechanic.remove(frame);
                        FurnitureMechanic nextMechanic = (FurnitureMechanic)
                                furnitureFactory.getMechanic(evolution.getNextStage());
                        nextMechanic.place(frame.getRotation(),
                                mechanic.getYaw(frame.getRotation()),
                                frame.getFacing(),
                                frame.getLocation(),
                                null
                        );
                    } else frame.getPersistentDataContainer().set(FurnitureMechanic.EVOLUTION_KEY,
                            PersistentDataType.INTEGER, evolutionStep);
                }
    }
}
