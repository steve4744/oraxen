package io.th0rgal.oraxen.font;

import io.th0rgal.oraxen.OraxenPlugin;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.internal.serializer.Emitable;
import net.kyori.adventure.text.minimessage.internal.serializer.SerializableResolver;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import javax.annotation.Nullable;

public class GlyphTag {

    private static final String GLYPH = "glyph";

    public static final TagResolver RESOLVER = SerializableResolver.claimingComponent(GLYPH, GlyphTag::create, GlyphTag::emit);

    static Tag create(final ArgumentQueue args, final Context ctx) throws ParsingException {
        Glyph glyph = OraxenPlugin.get().getFontManager().getGlyphFromName(args.popOr("A glyph value is required").value());
        return Tag.inserting(Component.text(glyph.getCharacter()).color(NamedTextColor.WHITE).font(Key.key("default")));
    }

    static @Nullable Emitable emit(final Component component) {
        return null;
    }
}
