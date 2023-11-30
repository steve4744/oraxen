package io.th0rgal.oraxen.mechanics.provided.gameplay.furniture;

import com.jeff_media.morepersistentdatatypes.datatypes.serializable.ConfigurationSerializableDataType;
import org.bukkit.Location;
import org.bukkit.Utility;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class BlockLocation implements ConfigurationSerializable {
    public static PersistentDataType<byte[],BlockLocation> dataType = new ConfigurationSerializableDataType<>(BlockLocation.class);

    private int x;
    private int y;
    private int z;

    public BlockLocation(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockLocation(Location location) {
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
    }

    public BlockLocation(String serializedBlockLocation) {
        String[] values = serializedBlockLocation.split(",");
        this.x = Integer.parseInt(values[0]);
        this.y = Integer.parseInt(values[1]);
        this.z = Integer.parseInt(values[2]);
    }

    public BlockLocation(Map<String, Object> coordinatesMap) {
        this.x = (Integer) coordinatesMap.get("x");
        this.y = (Integer) coordinatesMap.get("y");
        this.z = (Integer) coordinatesMap.get("z");
    }

    @Override
    public String toString() {
        return x + "," + y + "," + z;
    }

    public BlockLocation add(BlockLocation blockLocation) {
        BlockLocation output = new BlockLocation(x, y, z);
        output.x += blockLocation.x;
        output.y += blockLocation.y;
        output.z += blockLocation.z;
        return output;
    }

    public Location add(Location location) {
        return location.clone().add(x, y, z);
    }

    public Location toLocation(World world) {
        return new Location(world, x, y, z);
    }

    public BlockLocation groundRotate(float angle) {
        BlockLocation output = new BlockLocation(x, y, z);
        float fixedAngle = (360 - angle);
        double radians = Math.toRadians(fixedAngle);
        output.x = ((int) Math.round(Math.cos(radians) * x - Math.sin(radians) * z));
        output.z = ((int) Math.round(Math.sin(radians) * x - Math.cos(radians) * z));
        if (fixedAngle % 180 > 1)
            output.z = -output.z;
        return output;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BlockLocation blockLocation && blockLocation.x == x && blockLocation.y == y && blockLocation.z == z;
    }

    @Override
    @Utility
    @NotNull
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("x", this.x);
        data.put("y", this.y);
        data.put("z", this.z);

        return data;
}

    /**
     * Required method for deserialization
     *
     * @param args map to deserialize
     * @return deserialized location
     * @throws IllegalArgumentException if the world don't exists
     * @see ConfigurationSerializable
     */
    @NotNull
    public static BlockLocation deserialize(@NotNull Map<String, Object> args) {
        return new BlockLocation((int) args.get("x"), (int) args.get("y"), (int) args.get("z"));
    }
}
