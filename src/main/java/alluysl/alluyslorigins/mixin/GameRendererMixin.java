package alluysl.alluyslorigins.mixin;

import alluysl.alluyslorigins.AlluyslOrigins;
import alluysl.alluyslorigins.power.OverlayPower;
import alluysl.alluyslorigins.util.OverlayInfo;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.GL_FUNC_ADD;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    private final int defaultBlendEquation = GL_FUNC_ADD;

    private float r = 0, g = 0, b = 0, a = 1.0F;
    private final double[][] vertices = new double[4][2];
    private Identifier texture = null;
    int textureId = -1;
    private int blendEquation = defaultBlendEquation;
    private int srcFactor = GL_ONE, dstFactor = GL_ONE, srcAlpha = GL_ONE, dstAlpha = GL_ONE;

    private void resetTexture(){
        texture = null;
        textureId = -1;
    }
    private void setTexture(Identifier id){
        texture = id;
        if (this.client.getTextureManager().getTexture(id) == null){
            textureId = -1;
            System.out.println("[Alluysl's Origins] Warning: couldn't get texture OpenGL id.");
        } else {
            textureId = this.client.getTextureManager().getTexture(id).getGlId();
        }
    }
    private void setTexture(String path){
        setTexture(new Identifier(path));
    }

    @Shadow
    @Final
    private static Identifier field_26730;

    // Original code for some of the following methods from Mojang, mapping from Yarn, research and some edits from me (splitting up, overloading, changing constants to arguments)

    private void setBlendFunc() { // edited blendFuncSeparate
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager.blendFuncSeparate(srcFactor, dstFactor, srcAlpha, dstAlpha);
    }

    private void drawTexture(){ // edited method_31136 (second part)
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        if (blendEquation != AlluyslOrigins.NO_BLEND){
            RenderSystem.enableBlend();
            if (blendEquation != GL_FUNC_ADD) // default
                RenderSystem.blendEquation(blendEquation);
            setBlendFunc();
        }
        RenderSystem.color4f(r, g, b, a);

        this.client.getTextureManager().bindTexture(texture == null ? field_26730 : texture);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE); // draw mode 7 is quads, we'll draw a single one
        // I presume the Z coordinates don't matter, and they don't seem to do, but why put -90 specifically then? It's weird
        bufferBuilder.vertex(vertices[0][0], vertices[0][1], -90.0D).texture(0.0F, 1.0F).next(); // bottom left
        bufferBuilder.vertex(vertices[1][0], vertices[1][1], -90.0D).texture(1.0F, 1.0F).next(); // bottom right
        bufferBuilder.vertex(vertices[2][0], vertices[2][1], -90.0D).texture(1.0F, 0.0F).next(); // top right
        bufferBuilder.vertex(vertices[3][0], vertices[3][1], -90.0D).texture(0.0F, 0.0F).next(); // top left
        tessellator.draw();

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        if (blendEquation != AlluyslOrigins.NO_BLEND){
            RenderSystem.defaultBlendFunc();
            if (blendEquation != GL_FUNC_ADD)
                RenderSystem.blendEquation(GL_FUNC_ADD);
            RenderSystem.disableBlend();
        }
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    // Sets the texture as a rectangle from (left, top) to (left + width, top + height)
    private void setTextureBoxed(double left, double top, double width, double height){
        vertices[0][0] = vertices[3][0] = left;
        vertices[0][1] = vertices[1][1] = top + height;
        vertices[1][0] = vertices[2][0] = left + width;
        vertices[2][1] = vertices[3][1] = top;
    }

    // Sets the texture as a rectangle of screen/window dimensions centered on the middle of it (Mojang + Yarn)
    private void setTextureCentered(double scale, String scalingX, String scalingY, int baseWidth, int baseHeight) {
        int clientWidth = this.client.getWindow().getScaledWidth();
        int clientHeight = this.client.getWindow().getScaledHeight();
        int textureWidth = baseWidth != 0 ? baseWidth : clientWidth;
        int textureHeight = baseHeight != 0 ? baseHeight : clientHeight;
        double left, top, width, height;

        if (textureId != -1){
            if (baseWidth != 0);
            if (baseHeight != 0);
            // TODO get texture dimensions
        }

        switch (scalingX){
            case "y":
                width = textureWidth * clientHeight / (double)textureHeight;
                break;
            case "min":
                if (clientWidth * textureHeight > clientHeight * textureWidth)
                    width = textureWidth * clientHeight / (double)textureHeight;
                else
                    width = clientWidth;
                break;
            case "max":
                if (clientWidth * textureHeight >= clientHeight * textureWidth)
                    width = clientWidth;
                else
                    width = textureWidth * clientHeight / (double)textureHeight;
                break;
            case "avg":
                width = (clientWidth + textureWidth * clientHeight / (double)textureHeight) / 2;
                break;
            case "fixed":
                width = textureWidth;
                break;
            default: // stretch (scale 1 is full width)
                width = clientWidth;
        }
        switch (scalingY){
            case "x":
                height = textureHeight * clientWidth / (double)textureWidth;
                break;
            case "min":
                if (clientHeight * textureWidth > clientWidth * textureHeight)
                    height = textureHeight * clientWidth / (double)textureWidth;
                else
                    height = clientHeight;
                break;
            case "max":
                if (clientHeight * textureWidth >= clientWidth * textureHeight)
                    height = clientHeight;
                else
                    height = textureHeight * clientWidth / (double)textureWidth;
                break;
            case "avg":
                height = (clientHeight + textureHeight * clientWidth / (double)textureWidth) / 2;
                break;
            case "fixed":
                height = textureHeight;
                break;
            default: // stretch (scale 1 is full height)
                height = clientHeight;
        }
        width *= scale;
        height *= scale;
        left = ((double)clientWidth - width) / 2.0D;
        top = ((double)clientHeight - height) / 2.0D;
        setTextureBoxed(left, top, width, height);
    }

    private float transformRatio(OverlayPower power, float ratio){
        if (power.mirror)
            ratio = 1.0F - Math.abs(1.0F - 2.0F * ratio);
        if (power.flipProfileTime)
            ratio = 1.0F - ratio;
        ratio = power.profile.apply(ratio);
        if (power.flipProfileValue)
            ratio = 1.0F - ratio;
        return ratio;
    }

    private void drawOverlay(OverlayPower power, float ratio){
        setTextureCentered(MathHelper.lerp(ratio, power.startScale, power.endScale),
                power.scalingX, power.scalingY, power.baseWidth, power.baseHeight);
        float colorRatio = power.ratioDrivesColor ? ratio : 1;
        r = colorRatio * power.r;
        g = colorRatio * power.g;
        b = colorRatio * power.b;
        a = (power.ratioDrivesAlpha ? ratio : 1) * power.a;
        setTexture(power.texture);
        blendEquation = power.blendEquation;
        srcFactor = power.srcFactor; dstFactor = power.dstFactor;
        srcAlpha = power.srcAlpha; dstAlpha = power.dstAlpha;
        drawTexture();
    }

    private int previousTick = 0;
    private int previousTickFrames = 0, currentTickFrames = 0;
    private boolean firstPass = true;

    private final Map<Integer, OverlayInfo> overlayInfoMap = new ConcurrentHashMap<>();

    @Shadow private int ticks;

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F"))
    private void drawOverlays(CallbackInfo ci) {
        int currentTick = ticks;

        if (previousTick != currentTick) {
            previousTickFrames = currentTickFrames;
            currentTickFrames = 1;
        } else
            ++currentTickFrames;

        if (this.client.player == null)
            return;

        for (OverlayPower power : ModComponents.ORIGIN.get(this.client.player).getPowers(OverlayPower.class, true)) {
            int id = power.getId();
            boolean active = power.isActive();
            OverlayInfo info = null;
            if (overlayInfoMap.containsKey(id))
                info = overlayInfoMap.get(id);
            else if (active)
                info = overlayInfoMap.put(id, new OverlayInfo(firstPass));

            if (info != null) {

                if (currentTick != previousTick) {

                    if (active && power.cyclic && power.upTicks != 0) { // if cyclic, version that doesn't clamp but switches to new cycle instead
                        info.setRatio(info.getRatio() + 1.0F / power.upTicks, true);
                        if (info.getRatio() > 1.0F) // can't use modulo else it's set to 0 when it's supposed to be 1 (supposing the IEEE 754 format allows that to happen)
                            info.setRatio(info.getRatio() - 1.0F, false);
                    } else
                        info.setRatio(MathHelper.clamp(
                            active ? (power.upTicks == 0 ? 1.0F : info.getRatio() + 1.0F / power.upTicks)
                                    : (power.downTicks == 0 ? 0.0F : info.getRatio() - 1.0F / power.downTicks),
                            0.0F, 1.0F
                        ), true);
                }

                // Ideally the interpolation would be decided by a config option on the client side, not by the power
                float ratio = power.interpolate && previousTickFrames > 0 ? MathHelper.lerp(MathHelper.clamp((float)currentTickFrames / previousTickFrames, 0.0F, 1.0F), info.getPreviousRatio(), info.getRatio())
                        : info.getRatio();

                if (power.cyclic && (active || ratio > 0.0F)
                    || (ratio > 0.0F || power.showOnZeroRatio) && (ratio < 1.0F || power.showOnOneRatio))
                    drawOverlay(power, transformRatio(power, ratio));
            }

        }

        previousTick = currentTick;
        firstPass = false;
    }
}