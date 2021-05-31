package alluysl.alluyslorigins.power;

import alluysl.alluyslorigins.AlluyslOrigins;
import io.github.apace100.origins.power.Power;
import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.power.PowerTypeReference;
import net.minecraft.util.Identifier;

public class AlluyslOriginsPowers {

    public static final PowerType<Power> TEST = new PowerTypeReference<>(new Identifier(AlluyslOrigins.MODID, "test"));

    public static void init(){

    }
}