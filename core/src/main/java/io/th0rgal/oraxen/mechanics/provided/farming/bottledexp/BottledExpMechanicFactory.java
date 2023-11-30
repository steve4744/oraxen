package io.th0rgal.oraxen.mechanics.provided.farming.bottledexp;

import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.mechanics.Mechanic;
import io.th0rgal.oraxen.mechanics.MechanicFactory;
import io.th0rgal.oraxen.mechanics.MechanicsManager;
import org.bukkit.configuration.ConfigurationSection;

public class BottledExpMechanicFactory extends MechanicFactory {

    private final int durabilityCost;

    public BottledExpMechanicFactory(ConfigurationSection section) {
        super(section);
        durabilityCost = section.getInt("durability_cost");
        MechanicsManager.registerListeners(OraxenPlugin.get(), getMechanicID(), new BottledExpMechanicListener(this));
    }

    @Override
    public Mechanic parse(ConfigurationSection itemMechanicConfiguration) {
        Mechanic mechanic = new BottledExpMechanic(this, itemMechanicConfiguration);
        addToImplemented(mechanic);
        return mechanic;
    }

    public int getDurabilityCost() {
        return durabilityCost;
    }

}
