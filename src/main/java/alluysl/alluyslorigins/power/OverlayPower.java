package alluysl.alluyslorigins.power;

import alluysl.alluyslorigins.AlluyslOrigins;
import io.github.apace100.origins.power.Power;
import io.github.apace100.origins.power.PowerType;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import static org.lwjgl.opengl.GL14.*;

public class OverlayPower extends Power {

    private final int id;
    public float r, g, b, a;
    public int
            upTicks, /** amount of ticks for the overlay to fully appear */
            downTicks; /** amount of ticks for the overlay to fully disappear */
    public Identifier texture;
    public String preset, /** the type of overlay positioning */
            scalingX, scalingY;
    public int baseWidth, baseHeight; /** the base scale reference for constant scaling */
    public int blendEquation, srcFactor, dstFactor, srcAlpha, dstAlpha; /** OpenGL blending parameters */
    public boolean ratioDrivesColor, ratioDrivesAlpha; /** whether the progress ratio of the overlay should influence the color/alpha values */
    public float startScale, /** scale at ratio 0 */
            endScale; /** scale at ratio 1 */

    public OverlayPower(PowerType<?> type, PlayerEntity player, int id,
                        float r, float g, float b, float a,
                        int upTicks, int downTicks, Identifier texture, String preset,
                        String scalingX, String scalingY, int baseWidth, int baseHeight,
                        float startScale, float endScale, String blendEquation,
                        String srcFactor, String dstFactor, String srcAlpha, String dstAlpha,
                        boolean ratioDrivesColor, boolean ratioDrivesAlpha) {
        super(type, player);
        this.id = id;
        this.r = MathHelper.clamp(r, 0.0F, 1.0F);
        this.g = MathHelper.clamp(g, 0.0F, 1.0F);
        this.b = MathHelper.clamp(b, 0.0F, 1.0F);
        this.a = MathHelper.clamp(a, 0.0F, 1.0F);
        this.upTicks = Math.max(upTicks, 0);
        this.downTicks = Math.max(downTicks, 0);
        this.texture = texture;
        this.preset = preset;
        this.scalingX = getScaling(scalingX);
        this.scalingY = getScaling(scalingY);
        this.baseWidth = baseWidth;
        this.baseHeight = baseHeight;
        this.startScale = startScale;
        this.endScale = endScale;
        this.blendEquation = getBlendEquation(blendEquation, preset);
        this.srcFactor = getFactor(srcFactor, false);
        this.dstFactor = getFactor(dstFactor, true);
        this.srcAlpha = srcAlpha.equals("") ? this.srcFactor : getFactor(srcFactor, false);
        this.dstAlpha = dstAlpha.equals("") ? this.dstFactor : getFactor(dstFactor, true);
        this.ratioDrivesColor = ratioDrivesColor;
        this.ratioDrivesAlpha = ratioDrivesAlpha;
    }

    private String getScaling(String name){
        switch (name){
            case "x": case "width": return "x";
            case "y": case "height": return "y";
            case "min": case "minimum": return "min";
            case "max": case "maximum": return "max";
            case "avg": case "average": case "median": return "avg";
            case "fixed": case "constant": case "texture": return "fixed";
            case "stretch": case "stretched": return "stretch";
            default:
                System.out.println("[Alluysl's Origins] Warning: unrecognized scaling mode '" + name + "', defaulting to 'stretch'.");
                return "stretch";
        }
    }

    private int getBlendEquation(String name, String style){
        switch (name){
            case "add": case "additive": return GL_FUNC_ADD;
            case "sub": case "subtract": case "subtractive": return GL_FUNC_SUBTRACT;
            case "rsub": case "reverse_subtract": case "reverse_subtractive": return GL_FUNC_REVERSE_SUBTRACT;
            case "min": case "minimum": return GL_MIN;
            case "max": case "maximum": return GL_MAX;
            case "none": case "no_blend": case "no_blending": return AlluyslOrigins.NO_BLEND;
            default: return style.equals("classic") || style.equals("alpha") ? GL_FUNC_ADD : AlluyslOrigins.NO_BLEND;
        }
    }
    private int getFactor(String name, boolean destination){
        switch (name){
            case "zero": case "GL_ZERO": return GL_ZERO;
            case "one": case "GL_ONE": return GL_ONE;
            case "source_color": case "GL_SRC_COLOR": return GL_SRC_COLOR;
            case "one_minus_source_color": case "GL_ONE_MINUS_SRC_COLOR": return GL_ONE_MINUS_SRC_COLOR;
            case "destination_color": case "GL_DST_COLOR": return GL_DST_COLOR;
            case "one_minus_destination_color": case "GL_ONE_MINUS_DST_COLOR": return GL_ONE_MINUS_DST_COLOR;
            case "source_alpha": case "GL_SRC_ALPHA": return GL_SRC_ALPHA;
            case "one_minus_source_alpha": case "GL_ONE_MINUS_SRC_ALPHA": return GL_ONE_MINUS_SRC_ALPHA;
            case "destination_alpha": case "GL_DST_ALPHA": return GL_DST_ALPHA;
            case "one_minus_destination_alpha": case "GL_ONE_MINUS_DST_ALPHA": return GL_ONE_MINUS_DST_ALPHA;
            case "constant_color": case "GL_CONSTANT_COLOR": return GL_CONSTANT_COLOR;
            case "one_minus_constant_color": case "GL_ONE_MINUS_CONSTANT_COLOR": return GL_ONE_MINUS_CONSTANT_COLOR;
            case "constant_alpha": case "GL_CONSTANT_ALPHA": return GL_CONSTANT_ALPHA;
            case "one_minus_constant_alpha": case "GL_ONE_MINUS_CONSTANT_ALPHA": return GL_ONE_MINUS_CONSTANT_ALPHA;
            case "source_alpha_saturate": case "GL_SRC_ALPHA_SATURATE": return GL_SRC_ALPHA_SATURATE;
            // The four following modes aren't useful to my knowledge since afaik there is only one source buffer (constants are from the GL15 class)
//            case "second_source_color": case "GL_SRC1_COLOR": return GL_SRC1_COLOR;
//            case "one_minus_second_source_color": case "GL_ONE_MINUS_SRC1_COLOR": return GL_ONE_MINUS_SRC1_COLOR;
//            case "second_source_alpha": case "GL_SRC1_ALPHA": return GL_SRC1_ALPHA;
//            case "one_minus_second_source_alpha": case "GL_ONE_MINUS_SRC1_ALPHA": return GL_ONE_MINUS_SRC1_ALPHA;
            default: return preset.equals("classic") ? GL_ONE :
                destination ? GL_ONE_MINUS_SRC_ALPHA : GL_SRC_ALPHA;
        }
    }

    public int getId(){
        return id;
    }

    @Override
    public String toString(){
        return super.toString() + " | id " + id + " | rgba " + r + " " + g + " " + b + " " + a + " | ticks " + upTicks + " up " + downTicks + " down | " + texture + " | " + preset + " | " + startScale + " -> " + endScale;
    }
}
