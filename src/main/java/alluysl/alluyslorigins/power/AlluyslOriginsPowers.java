package alluysl.alluyslorigins.power;

import alluysl.alluyslorigins.AlluyslOrigins;
import io.github.apace100.origins.power.factory.PowerFactory;
import io.github.apace100.origins.registry.ModRegistries;
import io.github.apace100.origins.util.SerializableData;
import io.github.apace100.origins.util.SerializableDataType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class AlluyslOriginsPowers {

    public static void register(){

        register(new PowerFactory<>(
                new Identifier(AlluyslOrigins.MODID, "overlay"),
                new SerializableData()
                        .add("r", SerializableDataType.FLOAT, 1.0F)
                        .add("g", SerializableDataType.FLOAT, 1.0F)
                        .add("b", SerializableDataType.FLOAT, 1.0F)
                        .add("a", SerializableDataType.FLOAT, 1.0F)
                        .add("up_ticks", SerializableDataType.INT, 0)
                        .addFunctionedDefault("down_ticks", SerializableDataType.INT, data -> data.getInt("up_ticks"))
                        .add("cyclic", SerializableDataType.BOOLEAN, false)
                        .add("cycle_type", SerializableDataType.STRING, "saw")
                        .add("texture", SerializableDataType.IDENTIFIER, new Identifier("textures/misc/nausea.png"))
                        .add("preset", SerializableDataType.STRING, "classic")
                        .add("scaling", SerializableDataType.STRING, "stretch")
                        .add("scaling_x", SerializableDataType.STRING, "")
                        .add("scaling_y", SerializableDataType.STRING, "")
                        .add("base_scale_x", SerializableDataType.INT, 0)
                        .add("base_scale_y", SerializableDataType.INT, 0)
                        .add("scale", SerializableDataType.FLOAT, 1.0F)
                        .addFunctionedDefault("start_scale", SerializableDataType.FLOAT, data -> data.getFloat("scale"))
                        .add("blend_equation", SerializableDataType.STRING, "")
                        .add("blend_mode", SerializableDataType.STRING, "")
                        .add("source_factor", SerializableDataType.STRING, "")
                        .add("destination_factor", SerializableDataType.STRING, "")
                        .add("source_alpha_factor", SerializableDataType.STRING, "")
                        .add("destination_alpha_factor", SerializableDataType.STRING, "")
                        .addFunctionedDefault("ratio_drives_color", SerializableDataType.BOOLEAN, data -> data.getString("preset").equals("classic") || data.getString("preset").equals("mask"))
                        .addFunctionedDefault("ratio_drives_alpha", SerializableDataType.BOOLEAN, data -> data.getString("preset").equals("alpha") || data.getString("preset").equals("transparent") || data.getString("preset").equals("transparency")),
                data -> (type, player) -> new OverlayPower(
                            type, player,
                            data.hashCode(), // thankfully, the hash is consistent, and powers with identical JSON files even get a different hash!
                            data.getFloat("r"),
                            data.getFloat("g"),
                            data.getFloat("b"),
                            data.getFloat("a"),
                            data.getInt("up_ticks"),
                            data.getInt("down_ticks"),
                            data.getBoolean("cyclic"),
                            data.getString("cycle_type"),
                            data.getId("texture"),
                            data.getString("preset").equals("alpha") || data.getString("preset").equals("transparent") || data.getString("preset").equals("transparency") ?
                                    "alpha" :
                            data.getString("preset").equals("mask") ?
                                    "classic" :
                                    data.getString("preset"),
                            data.getString("scaling_x").equals("")?
                                    data.getString("scaling") : data.getString("scaling_x"),
                            data.getString("scaling_y").equals("")?
                                    data.getString("scaling") : data.getString("scaling_y"),
                            data.getInt("base_scale_x"),
                            data.getInt("base_scale_y"),
                            data.getFloat("start_scale"),
                            data.getFloat("scale"),
                            data.getString("blend_equation").equals("") ?
                                    data.getString("blend_mode") : data.getString("blend_equation"),
                            data.getString("source_factor"),
                            data.getString("destination_factor"),
                            data.getString("source_alpha_factor"),
                            data.getString("destination_alpha_factor"),
                            data.getBoolean("ratio_drives_color"),
                            data.getBoolean("ratio_drives_alpha"))
        ).allowCondition());

        System.out.println("[Alluysl's Origins] Powers registered.");
    }

    private static void register(PowerFactory<?> factory) {
        Registry.register(ModRegistries.POWER_FACTORY, factory.getSerializerId(), factory);
    }
}