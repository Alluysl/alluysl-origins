package alluysl.alluyslorigins.power;

import alluysl.alluyslorigins.AlluyslOrigins;
import io.github.apace100.origins.power.factory.PowerFactory;
import io.github.apace100.origins.registry.ModRegistries;
import io.github.apace100.origins.util.SerializableData;
import io.github.apace100.origins.util.SerializableDataType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Arrays;

public class AlluyslOriginsPowers {

    // As I heard, variadic arguments are just syntactic sugar in Java, so they (allegedly) aren't more performant than instancing array objects
    private static <T> T processAliases(T[] values, T defaultValue){
        return Arrays.stream(values).filter(value -> !(value.equals(defaultValue))).findFirst().orElse(defaultValue);
    }

    public static void register(){

        register(new PowerFactory<>(
                new Identifier(AlluyslOrigins.MODID, "overlay"),
                new SerializableData()
                        .add("r", SerializableDataType.FLOAT, 1.0F)
                        .add("red", SerializableDataType.FLOAT, 1.0F) // alias
                        .add("g", SerializableDataType.FLOAT, 1.0F)
                        .add("green", SerializableDataType.FLOAT, 1.0F) // alias
                        .add("b", SerializableDataType.FLOAT, 1.0F)
                        .add("blue", SerializableDataType.FLOAT, 1.0F) // alias
                        .add("a", SerializableDataType.FLOAT, 1.0F)
                        .add("alpha", SerializableDataType.FLOAT, 1.0F) // alias
                        .add("profile", SerializableDataType.STRING, "linear")
                        .add("flip_profile_time", SerializableDataType.BOOLEAN, false)
                        .add("flip_profile_value", SerializableDataType.BOOLEAN, false)
                        .add("interpolate", SerializableDataType.BOOLEAN, true)
                        .add("lerp", SerializableDataType.BOOLEAN, true)
                        .add("up_ticks", SerializableDataType.INT, 0)
                        .addFunctionedDefault("down_ticks", SerializableDataType.INT, data -> data.getInt("up_ticks"))
                        .add("cyclic", SerializableDataType.BOOLEAN, false)
                        .add("mirror_cycle", SerializableDataType.BOOLEAN, false)
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
                        .add("blend_mode", SerializableDataType.STRING, "") // alias
                        .add("source_factor", SerializableDataType.STRING, "")
                        .add("destination_factor", SerializableDataType.STRING, "")
                        .add("source_alpha_factor", SerializableDataType.STRING, "")
                        .add("destination_alpha_factor", SerializableDataType.STRING, "")
                        .add("show_on_zero_ratio", SerializableDataType.BOOLEAN, false)
                        .add("show_on_one_ratio", SerializableDataType.BOOLEAN, true)
                        .add("multiply_ratio_by_configured_strength", SerializableDataType.BOOLEAN, false)
                        .add("static_ratio", SerializableDataType.BOOLEAN, false)
                        .add("static_ratio_threshold", SerializableDataType.FLOAT, 0.0F)
                        .add("static_ratio_threshold_inclusive", SerializableDataType.BOOLEAN, false)
                        .addFunctionedDefault("ratio_drives_color", SerializableDataType.BOOLEAN,
                                data -> Arrays.asList("classic", "mask").contains(data.getString("preset")))
                        .addFunctionedDefault("ratio_drives_alpha", SerializableDataType.BOOLEAN,
                                data -> Arrays.asList("alpha", "transparent", "transparency").contains(data.getString("preset"))),
                data -> (type, player) -> new OverlayPower(
                            type, player,
                            data.hashCode(), // thankfully, the hash is consistent, and powers with identical JSON files even get a different hash!
                            processAliases(new Float[]{data.getFloat("r"), data.getFloat("red")}, 1.0F),
                            processAliases(new Float[]{data.getFloat("g"), data.getFloat("green")}, 1.0F),
                            processAliases(new Float[]{data.getFloat("b"), data.getFloat("blue")}, 1.0F),
                            processAliases(new Float[]{data.getFloat("a"), data.getFloat("alpha")}, 1.0F),
                            data.getString("profile"),
                            data.getBoolean("flip_profile_time"),
                            data.getBoolean("flip_profile_value"),
                            processAliases(new Boolean[]{data.getBoolean("interpolate"), data.getBoolean("lerp")}, true),
                            data.getInt("up_ticks"),
                            data.getInt("down_ticks"),
                            data.getBoolean("cyclic"),
                            data.getBoolean("mirror_cycle"),
                            data.getId("texture"),
                            data.getString("preset").equals("alpha") || data.getString("preset").equals("transparent") || data.getString("preset").equals("transparency") ?
                                    "alpha" :
                                    data.getString("preset").equals("mask") ?
                                    "classic" :
                                    data.getString("preset"),
                            data.getString("scaling_x").equals("")?
                                    data.getString("scaling") : data.getString("scaling_x"), // could use a functioned default
                            data.getString("scaling_y").equals("")?
                                    data.getString("scaling") : data.getString("scaling_y"), // could use a functioned default
                            data.getInt("base_scale_x"),
                            data.getInt("base_scale_y"),
                            data.getFloat("start_scale"),
                            data.getFloat("scale"),
                            processAliases(new String[]{data.getString("blend_equation"), data.getString("blend_mode")}, ""),
                            data.getString("source_factor"),
                            data.getString("destination_factor"),
                            data.getString("source_alpha_factor"),
                            data.getString("destination_alpha_factor"),
                            data.getBoolean("show_on_zero_ratio"),
                            data.getBoolean("show_on_one_ratio"),
                            data.getBoolean("ratio_drives_color"),
                            data.getBoolean("ratio_drives_alpha"),
                            data.getBoolean("multiply_ratio_by_configured_strength"),
                            data.getBoolean("static_ratio"),
                            data.getFloat("static_ratio_threshold"),
                            data.getBoolean("static_ratio_threshold_inclusive"))
        ).allowCondition());

        System.out.println("[Alluysl's Origins] Powers registered.");
    }

    private static void register(PowerFactory<?> factory) {
        Registry.register(ModRegistries.POWER_FACTORY, factory.getSerializerId(), factory);
    }
}