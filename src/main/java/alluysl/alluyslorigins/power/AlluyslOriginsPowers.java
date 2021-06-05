package alluysl.alluyslorigins.power;

import alluysl.alluyslorigins.AlluyslOrigins;
import io.github.apace100.origins.power.Power;
import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.power.PowerTypeReference;
import io.github.apace100.origins.power.factory.PowerFactory;
import io.github.apace100.origins.registry.ModRegistries;
import io.github.apace100.origins.util.SerializableData;
import io.github.apace100.origins.util.SerializableDataType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

// Credits to MoriyaShiine for the hash map trick that allows cleaner referencing setup

public class AlluyslOriginsPowers {

//    private static final Map<PowerFactory<?>, Identifier> POWER_FACTORIES = new LinkedHashMap<>();
//
//    public static final PowerFactory<Power> OVERLAY = create(new PowerFactory<>(
//            new Identifier(AlluyslOrigins.MODID, "overlay"),
//            new SerializableData()
//                    .add("r", SerializableDataType.FLOAT, 1.0F)
//                    .add("g", SerializableDataType.FLOAT, 1.0F)
//                    .add("b", SerializableDataType.FLOAT, 1.0F)
//                    .add("a", SerializableDataType.FLOAT, 1.0F)
//                    .add("up_ticks", SerializableDataType.INT, 0)
//                    .addFunctionedDefault("down_ticks", SerializableDataType.INT, data -> data.getInt("up_ticks"))
//                    .add("style", SerializableDataType.STRING, "classic"),
//            data -> (type, player) -> {
//                System.out.println("lambda called");
//                return new OverlayPower(
//                        type, player,
//                        data.getFloat("r"),
//                        data.getFloat("g"),
//                        data.getFloat("b"),
//                        data.getFloat("a"),
//                        data.getInt("up_ticks"),
//                        data.getInt("down_ticks"),
//                        data.getString("style"));
//            }
//
//    ));

    public static final PowerType<Power> BURROW_OVERLAY = new PowerTypeReference<>(new Identifier(AlluyslOrigins.MODID, "burrow_overlay"));

//    private static <T extends Power> PowerFactory<T> create(PowerFactory<T> factory) {
//        POWER_FACTORIES.put(factory, factory.getSerializerId());
//        return factory;
//    }
//
//    public static void init() {
//        POWER_FACTORIES.keySet().forEach(powerType -> Registry.register(ModRegistries.POWER_FACTORY, POWER_FACTORIES.get(powerType), powerType));
//    }

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
                        .add("texture", SerializableDataType.IDENTIFIER, new Identifier("textures/misc/nausea.png"))
                        .add("style", SerializableDataType.STRING, "classic")
                        .add("scale", SerializableDataType.FLOAT, 1.0F)
                        .addFunctionedDefault("start_scale", SerializableDataType.FLOAT, data -> data.getFloat("scale")),
                data -> (type, player) -> new OverlayPower(
                            type, player,
                            data.hashCode(), // thankfully, the hash is consistent, and powers with identical JSON files even get a different hash!
                            data.getFloat("r"),
                            data.getFloat("g"),
                            data.getFloat("b"),
                            data.getFloat("a"),
                            data.getInt("up_ticks"),
                            data.getInt("down_ticks"),
                            data.getId("texture"),
                            data.getString("style"),
                            data.getFloat("start_scale"),
                            data.getFloat("scale"))
        ).allowCondition());

        System.out.println("[Alluysl's Origins] Powers registered.");
    }

    private static void register(PowerFactory<?> factory) {
        Registry.register(ModRegistries.POWER_FACTORY, factory.getSerializerId(), factory);
    }
}