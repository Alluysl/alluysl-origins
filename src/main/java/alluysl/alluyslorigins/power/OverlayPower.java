package alluysl.alluyslorigins.power;

import io.github.apace100.origins.power.Power;
import io.github.apace100.origins.power.PowerType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class OverlayPower extends Power {

    private final int id;
    public float r, g, b, a;
    public int
            upTicks, /** amount of ticks for the overlay to fully appear */
            downTicks; /** amount of ticks for the overlay to fully disappear */
    public float startScale, /** scale at ratio 0 */
            endScale; /** scale at ratio 1 */
    public Identifier texture;
    public String style; /** the type of overlay positioning */

    public OverlayPower(PowerType<?> type, PlayerEntity player, int id, float r, float g, float b, float a, int upTicks, int downTicks, Identifier texture, String style, float startScale, float endScale) {
        super(type, player);
        this.id = id;
        this.r = MathHelper.clamp(r, 0.0F, 1.0F);
        this.g = MathHelper.clamp(g, 0.0F, 1.0F);
        this.b = MathHelper.clamp(b, 0.0F, 1.0F);
        this.a = MathHelper.clamp(a, 0.0F, 1.0F);
        this.upTicks = Math.max(upTicks, 0);
        this.downTicks = Math.max(downTicks, 0);
        this.texture = texture;
        this.style = style;
        this.startScale = startScale;
        this.endScale = endScale;
    }

    public int getId(){
        return id;
    }

    @Override
    public String toString(){
        return super.toString() + " | id " + id + " | rgba " + r + " " + g + " " + b + " " + a + " | ticks " + upTicks + " up " + downTicks + " down | " + texture + " | " + style + " | " + startScale + " -> " + endScale;
    }
}
