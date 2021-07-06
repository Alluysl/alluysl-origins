package alluysl.alluyslorigins.power;

import io.github.apace100.origins.power.Power;
import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.power.factory.condition.ConditionFactory;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.function.Predicate;

public class ModifySlipperinessPower extends Power {

    private final float value;
    private final Predicate<CachedBlockPosition> blockCondition;
    private final boolean affectBoats;

    public ModifySlipperinessPower(PowerType<Power> type, PlayerEntity player, float value, ConditionFactory<CachedBlockPosition>.Instance blockCondition, boolean affectBoats){
        super(type, player);
        this.value = value;
        this.blockCondition = blockCondition;
        this.affectBoats = affectBoats;
    }

    public float getValue(){
        return value;
    }

    public boolean doesModify(BlockPos pos, boolean isBoat){
        return doesModify(new CachedBlockPosition(player.world, pos, true), isBoat);
    }

    public boolean doesModify(CachedBlockPosition pos, boolean isBoat){
        if (isBoat && !affectBoats)
            return false;
        if (blockCondition == null)
            return true;
        return blockCondition.test(pos);
    }
}
