package alluysl.alluyslorigins.mixin;

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

    private final int NO_BLEND = 0;
    private final int defaultBlendEquation = GL_FUNC_ADD;

    private float r = 0, g = 0, b = 0, a = 1.0F;
    private final double[][] vertices = new double[4][2];
    private Identifier texture = null;
    private int blendEquation = defaultBlendEquation;
    private int srcFactor, dstFactor, srcAlpha, dstAlpha;

    private void resetTexture(){ texture = null; }
    private void setTexture(Identifier id){ texture = id; }
    private void setTexture(String path){ texture = new Identifier(path); }

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
        if (blendEquation != NO_BLEND){
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
        if (blendEquation != NO_BLEND){
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
    private void setTextureCentered(double scale) {
        int clientWidth = this.client.getWindow().getScaledWidth();
        int clientHeight = this.client.getWindow().getScaledHeight();
        double width = (double)clientWidth * scale;
        double height = (double)clientHeight * scale;
        double left = ((double)clientWidth - width) / 2.0D;
        double top = ((double)clientHeight - height) / 2.0D;
        setTextureBoxed(left, top, width, height);
    }

    private void drawClassicOverlay(float ratio, float r, float g, float b, Identifier texture, float startScale, float endScale){
        setTextureCentered(MathHelper.lerp(ratio, startScale, endScale));
        this.r = ratio * r;
        this.g = ratio * g;
        this.b = ratio * b;
        a = 1.0F;
        setTexture(texture);
        blendEquation = defaultBlendEquation;
        srcFactor = dstFactor = srcAlpha = dstAlpha = GL_ONE;
        drawTexture();
    }

    private int previousTick = 0;
    private boolean firstPass = true;

    private final Map<Integer, OverlayInfo> overlayInfoMap = new ConcurrentHashMap<>();

    @Shadow private int ticks;

    @Shadow
    @Final
    private MinecraftClient client;


    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F"))
    private void drawBurrowOverlay(CallbackInfo ci) {
        int currentTick = ticks;

        if (this.client.player == null)
            return;
        
        for (OverlayPower power : ModComponents.ORIGIN.get(this.client.player).getPowers(OverlayPower.class, true)){
            int id = power.getId();
            boolean active = power.isActive();
            OverlayInfo info = null;
            if (overlayInfoMap.containsKey(id))
                info = overlayInfoMap.get(id);
            else if (active)
                info = overlayInfoMap.put(id, new OverlayInfo(firstPass));

            if (info != null){

                if (currentTick != previousTick){

                    info.ratio = MathHelper.clamp(
                            active ? (power.upTicks == 0 ? 1.0F : info.ratio + 1.0F / power.upTicks)
                                    : (power.downTicks == 0 ? 0.0F : info.ratio - 1.0F / power.downTicks),
                            0.0F, 1.0F
                        );
                }

                if (info.ratio > 0.0F)
                    drawClassicOverlay(info.ratio, power.r, power.g, power.b, power.texture, power.startScale, power.endScale);
            }

        previousTick = currentTick;
        firstPass = false;
    }
}