package io.th0rgal.oraxen.mechanics.provided.misc.food;

import io.lumine.mythiccrucible.MythicCrucible;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.mechanics.Mechanic;
import io.th0rgal.oraxen.mechanics.MechanicFactory;
import io.th0rgal.oraxen.utils.logs.Logs;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class FoodMechanic extends Mechanic {

    private final Set<PotionEffect> effects = new HashSet<>();
    private final double effectProbability;
    private final int hunger;
    private final int saturation;
    private ItemStack replacementItem;

    public FoodMechanic(MechanicFactory mechanicFactory, ConfigurationSection section) {
        super(mechanicFactory, section);
        hunger = section.getInt("hunger");
        saturation = section.getInt("saturation");
        effectProbability = Math.min(section.getDouble("effect_probability", 1.0), 1.0);

        if (section.isConfigurationSection("replacement")) {
            ConfigurationSection replacementSection = section.getConfigurationSection("replacement");
            assert replacementSection != null;
            registerReplacement(replacementSection);
        } else replacementItem = null;

        if (section.isConfigurationSection("effects")) {
            ConfigurationSection effectsSection = section.getConfigurationSection("effects");
            assert effectsSection != null;
            for (String effectSection : effectsSection.getKeys(false))
                if (section.isConfigurationSection(effectSection)) {
                    ConfigurationSection effect = section.getConfigurationSection("effectSection");
                    assert effect != null;
                    registerEffects(effect);
                }
        }
    }

    private void registerEffects(ConfigurationSection section) {
        PotionEffectType effectType = PotionEffectType.getByName(section.getName());
        if (effectType == null) {
            Logs.logError("Invalid effect type: " + section.getName() + " in " + getItemID());
            return;
        }
        effects.add(new PotionEffect(effectType,
                section.getInt("duration", 1) * 20,
                section.getInt("amplifier", 0),
                section.getBoolean("is_ambient", true),
                section.getBoolean("has_particles", true),
                section.getBoolean("has_icon", true)));
    }

    private void registerReplacement(ConfigurationSection section) {
        if (section.isString("minecraft_type")) {
            Material material = Material.getMaterial(Objects.requireNonNull(section.getString("minecraft_type")));
            if (material == null) {
                Logs.logError("Invalid material: " + section.getString("minecraft_type"));
                replacementItem = null;
            }
            else replacementItem = new ItemStack(material);
        } else if (section.isString("oraxen_item"))
            replacementItem = OraxenItems.getItemById(section.getString("oraxen_item")).build();
        else if (section.isString("crucible_item"))
            replacementItem = MythicCrucible.core().getItemManager().getItemStack(section.getString("crucible_item"));
        else replacementItem = null;
    }

    public int getHunger() {
        return hunger;
    }

    public int getSaturation() {
        return saturation;
    }

    public boolean hasReplacement() {
        return replacementItem != null;
    }

    public ItemStack getReplacement() {
        return replacementItem;
    }

    public boolean hasEffects() { return !effects.isEmpty(); }
    public Set<PotionEffect> getEffects() { return effects; }

    public double getEffectProbability() {
        return effectProbability;
    }
}
