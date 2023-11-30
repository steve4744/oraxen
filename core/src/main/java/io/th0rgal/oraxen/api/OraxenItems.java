package io.th0rgal.oraxen.api;

import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.config.Message;
import io.th0rgal.oraxen.items.ItemBuilder;
import io.th0rgal.oraxen.items.ItemParser;
import io.th0rgal.oraxen.items.ModelData;
import io.th0rgal.oraxen.mechanics.MechanicFactory;
import io.th0rgal.oraxen.mechanics.MechanicsManager;
import io.th0rgal.oraxen.pack.generation.DuplicationHandler;
import io.th0rgal.oraxen.utils.AdventureUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OraxenItems {

    public static final NamespacedKey ITEM_ID = new NamespacedKey(OraxenPlugin.get(), "id");
    // configuration sections : their OraxenItem wrapper
    private static Map<File, Map<String, ItemBuilder>> map;
    private static Set<String> items;

    public static void loadItems() {
        ItemParser.MODEL_DATAS_BY_ID.clear();
        ModelData.DATAS.clear();
        OraxenPlugin.get().getConfigsManager().assignAllUsedModelDatas();
        OraxenPlugin.get().getConfigsManager().parseAllItemTemplates();
        DuplicationHandler.convertOldMigrateItemConfig();
        map = OraxenPlugin.get().getConfigsManager().parseItemConfig();
        items = new HashSet<>();
        for (final Map<String, ItemBuilder> subMap : map.values())
            items.addAll(subMap.keySet());
    }

    public static String getIdByItem(final ItemBuilder item) {
        return item.getCustomTag(ITEM_ID, PersistentDataType.STRING);
    }

    public static String getIdByItem(final ItemStack item) {
        return (item == null || item.getItemMeta() == null || item.getItemMeta().getPersistentDataContainer().isEmpty()) ? null
                : item.getItemMeta().getPersistentDataContainer().get(ITEM_ID, PersistentDataType.STRING);
    }

    public static boolean exists(final String itemId) {
        return items.contains(itemId);
    }

    public static boolean exists(final ItemStack itemStack) {
        return items.contains(OraxenItems.getIdByItem(itemStack));
    }

    public static Optional<ItemBuilder> getOptionalItemById(final String id) {
        return entryStream().filter(entry -> entry.getKey().equals(id)).findFirst().map(Entry::getValue);
    }

    public static ItemBuilder getItemById(final String id) {
        return getOptionalItemById(id).orElse(null);
    }

    public static ItemBuilder getBuilderByItem(ItemStack item) {
        return getItemById(getIdByItem(item));
    }

    public static List<ItemBuilder> getUnexcludedItems() {
        return itemStream().filter(item -> !item.getOraxenMeta().isExcludedFromInventory())
                .toList();
    }

    public static List<ItemBuilder> getUnexcludedItems(final File file) {
        return map.get(file).values().stream().filter(item -> !item.getOraxenMeta().isExcludedFromInventory()).toList();
    }

    public static List<ItemStack> getItemStacksByName(final List<List<String>> lists) {
        return lists.stream().flatMap(list -> {
            final ItemStack[] itemStack = new ItemStack[]{new ItemStack(Material.AIR)};
            list.stream().map(line -> line.split(":")).forEach(param -> {
                switch (param[0].toLowerCase(Locale.ENGLISH)) {
                    case "type" -> {
                        if (exists(param[1])) itemStack[0] = getItemById(param[1]).build().clone();
                        else Message.ITEM_NOT_FOUND.log(AdventureUtils.tagResolver("item", param[1]));
                    }
                    case "amount" -> itemStack[0].setAmount(NumberUtils.toInt(param[1], 1));
                }
            });
            return Stream.of(itemStack[0]);
        }).toList();
    }

    public static boolean hasMechanic(String itemID, String mechanicID) {
        MechanicFactory factory = MechanicsManager.getMechanicFactory(mechanicID);
        return factory != null && factory.getMechanic(itemID) != null;
    }

    public static Map<File, Map<String, ItemBuilder>> getMap() {
        return map != null ? map : new HashMap<>();
    }

    public static Map<String, ItemBuilder> getEntriesAsMap() {
        return entryStream().collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    public static Set<Entry<String, ItemBuilder>> getEntries() {
        return entryStream().collect(Collectors.toSet());
    }

    public static Collection<ItemBuilder> getItems() {
        return itemStream().toList();
    }

    @Deprecated
    public static Set<String> getSectionsNames() {
        return getNames();
    }

    public static Set<String> getNames() {
        return nameStream().collect(Collectors.toSet());
    }

    public static String[] nameArray() {
        return nameStream().toArray(String[]::new);
    }

    public static Stream<String> nameStream() {
        return entryStream().map(Entry::getKey);
    }

    public static Stream<ItemBuilder> itemStream() {
        return entryStream().map(Entry::getValue);
    }

    public static Stream<Entry<String, ItemBuilder>> entryStream() {
        return map.values().stream().flatMap(map -> map.entrySet().stream());
    }

    public static String[] getItemNames() {
        return items.stream().filter(item -> {
            ItemBuilder builder = OraxenItems.getItemById(item);
            return builder != null && builder.hasOraxenMeta() && !builder.getOraxenMeta().isExcludedFromCommands();
        }).toArray(String[]::new);
    }

}
