package io.th0rgal.oraxen.font;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.config.Settings;
import io.th0rgal.oraxen.utils.logs.Logs;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class Glyph {

    public static final Character WHITESPACE_GLYPH = '\ue000';

    private boolean fileChanged = false;

    private final String name;
    private final boolean isEmoji;
    private boolean tabcomplete;
    private final char character;
    private String texture;
    private final int ascent;
    private final int height;
    private String permission = null;
    private String[] placeholders;
    private int code;
    private final BitMapEntry bitmapEntry;

    public Glyph(final String glyphName, final ConfigurationSection glyphSection, int newCode) {
        name = glyphName;
        placeholders = new String[0];
        isEmoji = glyphSection.getBoolean("is_emoji", false);
        if (glyphSection.isConfigurationSection("chat")) {
            final ConfigurationSection chatSection = glyphSection.getConfigurationSection("chat");
            assert chatSection != null;
            placeholders = chatSection.getStringList("placeholders").toArray(new String[0]);
            if (chatSection.isString("permission"))
                permission = chatSection.getString("permission");
            tabcomplete = chatSection.getBoolean("tabcomplete", false);
        }

        this.code = newCode;
        if (glyphSection.getInt("code", -1) != newCode && !Settings.DISABLE_AUTOMATIC_GLYPH_CODE.toBool()) {
            glyphSection.set("code", code);
            fileChanged = true;
        }

        character = (char) code;

        ConfigurationSection bitmapSection = glyphSection.getConfigurationSection("bitmap");
        bitmapEntry = bitmapSection != null ? new BitMapEntry(bitmapSection.getString("id"), bitmapSection.getInt("row"), bitmapSection.getInt("column")) : null;
        ascent = getBitMap() != null ? getBitMap().ascent() : glyphSection.getInt("ascent", 8);
        height = getBitMap() != null ? getBitMap().height() : glyphSection.getInt("height", 8);
        texture = getBitMap() != null ? getBitMap().texture() : glyphSection.getString("texture", "required/exit_icon.png");
        if (!texture.endsWith(".png")) texture += ".png";
    }

    public record BitMapEntry(String id, int row, int column) {
    }

    public BitMapEntry getBitmapEntry() {
        return bitmapEntry;
    }

    public String getBitmapId() {
        return bitmapEntry != null ? bitmapEntry.id : null;
    }

    public boolean hasBitmap() {
        return getBitmapId() != null;
    }

    public boolean isBitMap() {
        return FontManager.getGlyphBitMap(getBitmapId()) != null;
    }

    public FontManager.GlyphBitMap getBitMap() {
        return FontManager.getGlyphBitMap(getBitmapId());
    }

    public boolean isFileChanged() {
        return fileChanged;
    }

    public String getName() {
        return name;
    }

    public char getCharacter() {
        return character;
    }

    public String getTexture() {
        return texture;
    }

    public void setTexture(String texture) {
        this.texture = (texture.endsWith(".png")) ? texture : texture + ".png";
    }

    public int getAscent() {
        return ascent;
    }

    public int getHeight() {
        return height;
    }

    public String getPermission() {
        return permission;
    }

    public String[] getPlaceholders() {
        return placeholders;
    }

    public boolean isEmoji() {
        return isEmoji;
    }

    public boolean hasTabCompletion() { return tabcomplete; }

    public JsonObject toJson() {
        final JsonObject output = new JsonObject();
        final JsonArray chars = new JsonArray();
        chars.add(getCharacter());
        output.add("chars", chars);
        output.addProperty("file", texture);
        output.addProperty("ascent", ascent);
        output.addProperty("height", height);
        output.addProperty("type", "bitmap");
        return output;
    }

    public boolean hasPermission(final Player player) {
        return permission == null || permission.isEmpty() || player.hasPermission(permission);
    }

    protected void setCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    private final Set<String> materialNames = Arrays.stream(Material.values()).map(Material::name).collect(Collectors.toSet());

    public void verifyGlyph(List<Glyph> glyphs) {
        // Return on first run as files aren't generated yet
        Path packFolder = Path.of(OraxenPlugin.get().getDataFolder().getAbsolutePath()).resolve("pack");
        if (!packFolder.toFile().exists()) return;

        String texturePath = getTexture().contains(":") ? "assets/" + StringUtils.substringBefore(getTexture(), ":") + "/textures/" : "textures/";
        texturePath = texturePath + (getTexture().contains(":") ? getTexture().split(":")[1] : getTexture());
        final File texture;
        // If using minecraft as a namespace, make sure it is in assets or root pack-dir
        if (!StringUtils.substringBefore(getTexture(), ":").equals("minecraft") || packFolder.resolve(texturePath).toFile().exists())
            texture = packFolder.resolve(texturePath).toFile();
        else texture = packFolder.resolve(texturePath.replace("assets/minecraft/", "")).toFile();

        Map<Glyph, Boolean> sameCodeMap = glyphs.stream().filter(g -> g != this && g.getCode() == this.getCode()).collect(Collectors.toMap(g -> g, g -> true));
        // Check if the texture is a vanilla item texture and therefore not in oraxen, but the vanilla pack
        boolean isMinecraftNamespace = !getTexture().contains(":") || getTexture().split(":")[0].equals("minecraft");
        boolean isVanillaTexture = isMinecraftNamespace && materialNames.stream().anyMatch(name -> texture.getName().split("\\.")[0].toUpperCase().contains(name));
        boolean hasUpperCase = false;
        BufferedImage image = null;
        for (char c : texturePath.toCharArray()) if (Character.isUpperCase(c)) hasUpperCase = true;
        try {
            image = ImageIO.read(texture);
        } catch (IOException ignored) {
        }

        if (height < ascent) {
            this.setTexture("required/exit_icon");
            Logs.logError("The ascent is bigger than the height for " + name + ". This will break all your glyphs.");
            Logs.logWarning("It has been temporarily set to a placeholder image. You should edit this in the glyph config.");
        } else if (!isVanillaTexture && (!texture.exists() || image == null)) {
            this.setTexture("required/exit_icon");
            Logs.logError("The texture specified for " + name + " does not exist. This will break all your glyphs.");
            Logs.logWarning("It has been temporarily set to a placeholder image. You should edit this in the glyph config.");
        } else if (hasUpperCase) {
            this.setTexture("required/exit_icon");
            Logs.logError("The filename specified for " + name + " contains capital letters.");
            Logs.logWarning("This will break all your glyphs. It has been temporarily set to a placeholder image.");
            Logs.logWarning("You should edit this in the glyph config and your textures filename.");
        } else if (texturePath.contains(" ")) {
            this.setTexture("required/exit_icon");
            Logs.logError("The filename specified for " + name + " contains spaces.");
            Logs.logWarning("This will break all your glyphs. It has been temporarily set to a placeholder image.");
            Logs.logWarning("You should replace spaces with _ in your filename and glyph config.");
        } else if (texturePath.contains("//")) {
            this.setTexture("required/exit_icon");
            Logs.logError("The filename specified for " + name + " contains double slashes.");
            Logs.logWarning("This will break all your glyphs. It has been temporarily set to a placeholder image.");
            Logs.logWarning("You should make sure that the texture-path you have specified is correct.");
        } else if (!isVanillaTexture && (image.getHeight() > 256 || image.getWidth() > 256)) {
            this.setTexture("required/exit_icon");
            Logs.logError("The texture specified for " + name + " is larger than the supported size.");
            Logs.logWarning("The maximum image size is 256x256. Anything bigger will break all your glyphs.");
            Logs.logWarning("It has been temporarily set to a placeholder-image. You should edit this in the glyph config.");
        } else if (Settings.DISABLE_AUTOMATIC_GLYPH_CODE.toBool() && !sameCodeMap.isEmpty()) {
            this.setTexture("required/exit_icon");
            Logs.logError(name + " code is the same as " + sameCodeMap.keySet().stream().map(Glyph::getName).collect(Collectors.joining(", ")) + ".");
            Logs.logWarning("This will break all your glyphs. It has been temporarily set to a placeholder image.");
            Logs.logWarning("You should edit the code of all these glyphs to be unique.");
        }
    }
}
