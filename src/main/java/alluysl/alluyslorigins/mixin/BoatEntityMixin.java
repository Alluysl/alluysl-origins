package alluysl.alluyslorigins.mixin;

import alluysl.alluyslorigins.power.ModifySlipperinessPower;
import io.github.apace100.origins.component.OriginComponent;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BoatEntity.class)
public abstract class BoatEntityMixin extends Entity {

    public BoatEntityMixin(EntityType<?> type, World world){
        super(type, world);
    }

    @Shadow @Nullable public abstract Entity getPrimaryPassenger();

    @Redirect(method = "method_7548", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getSlipperiness()F"))
    private float modifySlipperiness(Block block){
        float base = block.getSlipperiness();

        Entity passenger = getPrimaryPassenger();

        if (passenger instanceof LivingEntity)
            for (ModifySlipperinessPower power : OriginComponent.getPowers(passenger, ModifySlipperinessPower.class))
                if (power.doesModify(getVelocityAffectingPos(), true))
                    base = MathHelper.lerp(power.getValue(), base, 1.0F);

        return base;
    }
}
