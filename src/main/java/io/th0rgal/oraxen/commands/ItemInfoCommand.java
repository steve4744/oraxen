package io.th0rgal.oraxen.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.items.ItemBuilder;
import io.th0rgal.oraxen.utils.AdventureUtils;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

import java.util.Map;

public class ItemInfoCommand {

    public CommandAPICommand getItemInfoCommand() {
        String[] itemNames = OraxenItems.getItemNames();
        if (itemNames == null) itemNames = new String[0];
        return new CommandAPICommand("iteminfo")
                .withPermission("oraxen.command.iteminfo")
                .withArguments(new StringArgument("itemid").replaceSuggestions(ArgumentSuggestions.strings(itemNames)))
                .executes((commandSender, args) -> {
                    String argument = (String) args[0];
                    Audience audience = OraxenPlugin.get().getAudience().sender(commandSender);
                    if (argument.equals("all")) {
                        for (Map.Entry<String, ItemBuilder> entry : OraxenItems.getEntries()) {
                            sendItemInfo(audience, entry.getValue(), entry.getKey());
                        }
                    } else {
                        ItemBuilder ib = OraxenItems.getItemById(argument);
                        if (ib == null)
                            audience.sendMessage(AdventureUtils.MINI_MESSAGE.deserialize("<red>No item found with ID</red> <dark_red>" + argument));
                        else sendItemInfo(audience, ib, argument);
                    }
                });
    }

    private void sendItemInfo(Audience sender, ItemBuilder builder, String itemId) {
        sender.sendMessage(AdventureUtils.MINI_MESSAGE.deserialize("<dark_aqua>ItemID: <aqua>" + itemId));
        sender.sendMessage(AdventureUtils.MINI_MESSAGE.deserialize("<dark_green>CustomModelData: <green>" + builder.getOraxenMeta().getCustomModelData()));
        sender.sendMessage(AdventureUtils.MINI_MESSAGE.deserialize("<dark_green>Material: <green>" + builder.getReferenceClone().getType()));
        sender.sendMessage(AdventureUtils.MINI_MESSAGE.deserialize("<dark_green>Model Name: <green>" + builder.getOraxenMeta().getModelName()));
        sender.sendMessage(Component.newline());
    }
}
