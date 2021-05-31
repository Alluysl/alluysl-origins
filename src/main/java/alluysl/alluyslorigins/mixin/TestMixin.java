package alluysl.alluyslorigins.mixin;

import alluysl.alluyslorigins.power.AlluyslOriginsPowers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
abstract class TestMixin extends Entity {

    private int testticks = 0;

    public TestMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(at = @At("HEAD"), method = "tick()V")
    private void init(CallbackInfo info) {
        if (++testticks % 20 == 0){
            System.out.println("Active: " + AlluyslOriginsPowers.TEST.isActive(this));
        }
    }
}