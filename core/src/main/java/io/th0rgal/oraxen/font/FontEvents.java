package io.th0rgal.oraxen.font;

import io.papermc.paper.event.player.AsyncChatDecorateEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.compatibilities.provided.placeholderapi.PapiAliases;
import io.th0rgal.oraxen.config.Message;
import io.th0rgal.oraxen.config.Settings;
import io.th0rgal.oraxen.utils.AdventureUtils;
import io.th0rgal.oraxen.utils.Utils;
import io.th0rgal.oraxen.utils.VersionUtil;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;

import static io.th0rgal.oraxen.items.ItemBuilder.ORIGINAL_NAME_KEY;
import static io.th0rgal.oraxen.utils.AdventureUtils.MINI_MESSAGE;
import static io.th0rgal.oraxen.utils.AdventureUtils.MINI_MESSAGE_EMPTY;

public class FontEvents implements Listener {

    private final FontManager manager;

    public FontEvents(FontManager manager) {
        this.manager = manager;
        Bukkit.getPluginManager().registerEvents(
                VersionUtil.isPaperServer() && VersionUtil.isSupportedVersionOrNewer("1.19.1")
                        ? new PaperChatHandler() : new SpigotChatHandler(), OraxenPlugin.get());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBookGlyph(final PlayerEditBookEvent event) {
        if (!Settings.FORMAT_BOOKS.toBool() || manager.useNmsGlyphs()) return;

        BookMeta meta = event.getNewBookMeta();
        for (String page : meta.getPages()) {
            int i = meta.getPages().indexOf(page) + 1;
            if (i == 0) continue;
            for (Character character : manager.getReverseMap().keySet()) {
                if (!page.contains(String.valueOf(character))) continue;

                Glyph glyph = manager.getGlyphFromName(manager.getReverseMap().get(character));
                if (!glyph.hasPermission(event.getPlayer())) {
                    Message.NO_PERMISSION.send(event.getPlayer(), AdventureUtils.tagResolver("permission", glyph.getPermission()));
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBookGlyph(final PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!Settings.FORMAT_BOOKS.toBool()) return;

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getItem() == null || !(event.getItem().getItemMeta() instanceof BookMeta meta)) return;
        if (event.getItem().getType() != Material.WRITTEN_BOOK) return;
        if (event.useInteractedBlock() == Event.Result.ALLOW) return;

        for (String page : meta.getPages()) {
            int i = meta.getPages().indexOf(page) + 1;
            if (i == 0) continue;

            for (Map.Entry<String, Glyph> entry : manager.getGlyphByPlaceholderMap().entrySet()) {
                String unicode = String.valueOf(entry.getValue().getCharacter());
                if (entry.getValue().hasPermission(player))
                    page = (manager.permsChatcolor == null)
                            ? page.replace(entry.getKey(), ChatColor.WHITE + unicode + ChatColor.BLACK)
                            .replace(unicode, ChatColor.WHITE + unicode + ChatColor.BLACK)
                            : page.replace(entry.getKey(), ChatColor.WHITE + unicode + PapiAliases.setPlaceholders(player, manager.permsChatcolor))
                            .replace(unicode, ChatColor.WHITE + unicode + ChatColor.BLACK);
                meta.setPage(i, AdventureUtils.parseLegacy(page));
            }
        }

        Book book = Book.builder()
                .title(MINI_MESSAGE.deserialize(meta.getTitle() != null ? meta.getTitle() : ""))
                .author(MINI_MESSAGE.deserialize(meta.getAuthor() != null ? meta.getAuthor() : ""))
                .pages(meta.getPages().stream().map(p -> MINI_MESSAGE_EMPTY.deserialize(p, GlyphTag.getResolverForPlayer(player))).toList())
                .build();

        // Open fake book and deny opening of original book to avoid needing to format the original book
        event.setUseItemInHand(Event.Result.DENY);
        OraxenPlugin.get().getAudience().player(player).openBook(book);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSignGlyph(final SignChangeEvent event) {
        if (!Settings.FORMAT_SIGNS.toBool() || manager.useNmsGlyphs()) return;

        Player player = event.getPlayer();
        for (String line : event.getLines()) {
            line = AdventureUtils.parseLegacyThroughMiniMessage(line);
            int i = Arrays.stream(event.getLines()).toList().indexOf(line);
            if (i == -1) continue;
            for (Character character : manager.getReverseMap().keySet()) {
                if (!line.contains(String.valueOf(character))) continue;

                Glyph glyph = manager.getGlyphFromName(manager.getReverseMap().get(character));
                if (!glyph.hasPermission(player)) {
                    Message.NO_PERMISSION.send(player, AdventureUtils.tagResolver("permission", glyph.getPermission()));
                    event.setCancelled(true);
                }
            }

            for (Map.Entry<String, Glyph> entry : manager.getGlyphByPlaceholderMap().entrySet()) {
                String unicode = String.valueOf(entry.getValue().getCharacter());
                if (entry.getValue().hasPermission(player))
                    line = (manager.permsChatcolor == null)
                            ? line.replace(entry.getKey(), ChatColor.WHITE + unicode + ChatColor.BLACK)
                            .replace(unicode, ChatColor.WHITE + unicode + ChatColor.BLACK)
                            : line.replace(entry.getKey(), ChatColor.WHITE + unicode + PapiAliases.setPlaceholders(player, manager.permsChatcolor))
                            .replace(unicode, ChatColor.WHITE + unicode + ChatColor.BLACK);
            }
            event.setLine(i, AdventureUtils.parseLegacy(line));
        }
    }

    @EventHandler
    public void onPlayerRename(final InventoryClickEvent event) {
        if (!(event.getClickedInventory() instanceof AnvilInventory clickedInv)) return;
        if (!Settings.FORMAT_ANVIL.toBool() || manager.useNmsGlyphs() || event.getSlot() != 2) return;

        Player player = (Player) event.getWhoClicked();
        String displayName = clickedInv.getRenameText();
        ItemStack inputItem = clickedInv.getItem(0);
        ItemStack resultItem = clickedInv.getItem(2);
        if (resultItem == null || !OraxenItems.exists(inputItem)) return;

        if (displayName != null) {
            displayName = AdventureUtils.parseLegacyThroughMiniMessage(displayName);
            for (Character character : manager.getReverseMap().keySet()) {
                if (!displayName.contains(String.valueOf(character))) continue;
                Glyph glyph = manager.getGlyphFromName(manager.getReverseMap().get(character));
                if (!glyph.hasPermission(player)) {
                    Glyph required = manager.getGlyphFromName("required");
                    String replacement = required.hasPermission(player) ? String.valueOf(required.getCharacter()) : "";
                    Message.NO_PERMISSION.send(player, AdventureUtils.tagResolver("permission", glyph.getPermission()));
                    displayName = displayName.replace(String.valueOf(character), replacement);
                }
            }

            for (Map.Entry<String, Glyph> entry : manager.getGlyphByPlaceholderMap().entrySet()) {
                if (entry.getValue().hasPermission(player))
                    displayName = (manager.permsChatcolor == null)
                            ? displayName.replace(entry.getKey(),
                            String.valueOf(entry.getValue().getCharacter()))
                            : displayName.replace(entry.getKey(),
                            ChatColor.WHITE + String.valueOf(entry.getValue().getCharacter())
                                    + PapiAliases.setPlaceholders(player, manager.permsChatcolor));
            }
        }

        // Since getRenameText is in PlainText, check if the displayName is the same as the rename text with all tags stripped
        // If so retain the displayName of inputItem. This also fixes enchantments breaking names
        // If the displayName is null, reset it to the "original" name
        String strippedDownInputDisplay = MINI_MESSAGE.stripTags(AdventureUtils.parseLegacy(inputItem.getItemMeta().getDisplayName()));
        if (((displayName == null || displayName.isEmpty()) && OraxenItems.exists(inputItem)) || strippedDownInputDisplay.equals(displayName)) {
            displayName = inputItem.getItemMeta().getPersistentDataContainer().get(ORIGINAL_NAME_KEY, PersistentDataType.STRING);
        }

        String finalDisplayName = displayName;
        Utils.editItemMeta(resultItem, meta -> meta.setDisplayName(finalDisplayName));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        manager.sendGlyphTabCompletion(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        manager.clearGlyphTabCompletions(event.getPlayer());
    }

    public class SpigotChatHandler implements Listener {
        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        public void onPlayerChat(AsyncPlayerChatEvent event) {
            if (!Settings.FORMAT_CHAT.toBool()) return;

            String format = format(event.getFormat(), null);
            String message = format(event.getMessage(), event.getPlayer());
            if (format == null || message == null) event.setCancelled(true);
            else {
                event.setFormat(format);
                event.setMessage(message);
            }
        }

        /**
         * Formats a string with glyphs and placeholders
         * @param string The string to format
         * @param player The player to check permissions for, if null it parses the string without checking permissions
         * @return The formatted string, or null if the player doesn't have permission for a glyph
         */
        private String format(String string, @Nullable Player player) {
            string = AdventureUtils.parseLegacyThroughMiniMessage(string, player);
            if (player != null) for (Character character : manager.getReverseMap().keySet()) {
                if (!string.contains(String.valueOf(character))) continue;
                Glyph glyph = manager.getGlyphFromName(manager.getReverseMap().get(character));
                if (!glyph.hasPermission(player)) {
                    Message.NO_PERMISSION.send(player, AdventureUtils.tagResolver("permission", glyph.getPermission()));
                    return null;
                }
            }


            for (Map.Entry<String, Glyph> entry : manager.getGlyphByPlaceholderMap().entrySet()) {
                String unicode = ChatColor.WHITE + String.valueOf(entry.getValue().getCharacter());
                if (player == null || entry.getValue().hasPermission(player))
                    string = (manager.permsChatcolor == null)
                            ? string.replace(entry.getKey(), unicode)
                            : string.replace(entry.getKey(), unicode + PapiAliases.setPlaceholders(player, manager.permsChatcolor));
            }

            return string;
        }
    }


    @SuppressWarnings("UnstableApiUsage")
    public class PaperChatHandler implements Listener {

        @EventHandler(priority = EventPriority.LOWEST)
        public void onPlayerChat(AsyncChatDecorateEvent event) {
            if (!Settings.FORMAT_CHAT.toBool()) return;

            Component result = format(event.originalMessage(), event.player());
            event.result(result != null ? result : Component.empty());
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onPlayerChat2(AsyncChatDecorateEvent event) {
            if (!Settings.FORMAT_CHAT.toBool()) return;

            Component result = format(event.result(), null);
            event.result(result != null ? result : Component.empty());
        }

    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncChatEvent event) {
        if (!Settings.FORMAT_CHAT.toBool() || !VersionUtil.isPaperServer()) return;
        // AsyncChatDecorateEvent has formatted the component if server is 1.19.1+
        Component message = VersionUtil.isSupportedVersionOrNewer("1.19.1") ? event.message() : format(event.message(), event.getPlayer());
        message = message != null ? message : Component.empty();
        if (!message.equals(Component.empty())) return;

        event.viewers().clear();
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat2(AsyncChatEvent event) {
        if (!Settings.FORMAT_CHAT.toBool() || !VersionUtil.isPaperServer()) return;
        // AsyncChatDecorateEvent has formatted the component if server is 1.19.1+
        Component message = VersionUtil.isSupportedVersionOrNewer("1.19.1") ? event.message() : format(event.originalMessage(), event.getPlayer());
        message = message != null ? message : Component.empty();
        if (!message.equals(Component.empty())) return;

        event.viewers().clear();
        event.setCancelled(true);
    }

    private Component format(Component message, Player player) {
        for (Character character : manager.getReverseMap().keySet()) {
            if (!MINI_MESSAGE.serialize(message).contains(character.toString())) continue;

            Glyph glyph = manager.getGlyphFromName(manager.getReverseMap().get(character));
            if (!glyph.hasPermission(player)) {
                Message.NO_PERMISSION.send(player, AdventureUtils.tagResolver("permission", glyph.getPermission()));
                return null;
            }
        }

        for (Glyph glyph : manager.getGlyphs()) {
            for (String placeholder : glyph.getPlaceholders()) {
                if (!glyph.hasPermission(player)) continue;
                message = message.replaceText(TextReplacementConfig.builder().matchLiteral(placeholder)
                        .replacement(Component.text(glyph.getCharacter()).color(NamedTextColor.WHITE)).build());
            }
        }

        return AdventureUtils.parseMiniMessage(message, player);
    }

}
