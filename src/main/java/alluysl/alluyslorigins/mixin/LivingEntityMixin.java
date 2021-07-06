package alluysl.alluyslorigins.mixin;

import alluysl.alluyslorigins.power.ModifySlipperinessPower;
import io.github.apace100.origins.component.OriginComponent;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> type, World world){
        super(type, world);
    }

    @Redirect(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getSlipperiness()F"))
    private float modifySlipperiness(Block block){
        float base = block.getSlipperiness();

        for (ModifySlipperinessPower power : OriginComponent.getPowers(this, ModifySlipperinessPower.class))
            if (power.doesModify(getVelocityAffectingPos(), false))
                base = MathHelper.lerp(power.getValue(), base, 1.0F);

        return base;
    }
}
