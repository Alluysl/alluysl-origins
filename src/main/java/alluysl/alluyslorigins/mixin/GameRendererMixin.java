package alluysl.alluyslorigins.mixin;

import alluysl.alluyslorigins.power.AlluyslOriginsPowers;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow
    @Final
    private static Identifier field_26730;

    // Code from Mojang, mapping from Yarn, some edits from me, essentially a modified method_31136 (from GameRenderer)

    private void drawOverlay(float r, float g, float b, double left, double top, double width, double height, Identifier texture){
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE);
        RenderSystem.color4f(r, g, b, 1.0F); // alpha has no effect
        this.client.getTextureManager().bindTexture(texture);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE);
        // I presume the Z coordinate don't matter, and they don't seem to do, but why put -90 specifically then? It's weird
        bufferBuilder.vertex(left, top + height, -90.0D).texture(0.0F, 1.0F).next(); // bottom left
        bufferBuilder.vertex(left + width, top + height, -90.0D).texture(1.0F, 1.0F).next(); // bottom right
        bufferBuilder.vertex(left + width, top, -90.0D).texture(1.0F, 0.0F).next(); // top right
        bufferBuilder.vertex(left, top, -90.0D).texture(0.0F, 0.0F).next(); // top left
        tessellator.draw();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    private void drawOverlay(float r, float g, float b, double left, double top, double width, double height, String texturePath) {
        drawOverlay(r, g, b, left, top, width, height, new Identifier(texturePath));
    }

    private void drawOverlay(float ratio, float r, float g, float b, double startScale, double endScale, Identifier texture) {
        int clientWidth = this.client.getWindow().getScaledWidth();
        int clientHeight = this.client.getWindow().getScaledHeight();
        double scale = MathHelper.lerp((double)ratio, startScale, endScale);
        r *= ratio;
        g *= ratio;
        b *= ratio;
        double width = (double)clientWidth * scale;
        double height = (double)clientHeight * scale;
        double left = ((double)clientWidth - width) / 2.0D;
        double top = ((double)clientHeight - height) / 2.0D;
        drawOverlay(r, g, b, left, top, width, height, texture);
    }

    private void drawOverlay(float ratio, float r, float g, float b, double startScale, double endScale, String texturePath) {
        drawOverlay(ratio, r, g, b, startScale, endScale, new Identifier(texturePath));
    }

        private void drawOverlay(float ratio, float r, float g, float b){
        drawOverlay(ratio, r, g, b, 2.0D, 1.0D, field_26730);
    }

    private void drawOverlay(float r, float g, float b){
        drawOverlay(1.0F, r, g, b);
    }

    private int baseTick = 0;
    private int previousTick = 0;

    public boolean bypassNauseaCheck = true;

    @Shadow private int ticks;

    // Heavily based on Origin's game renderer phantomzied overlay mixin

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F"))
    private void drawBurrowOverlay(CallbackInfo ci) {
        int currentTick = ticks;
        int duration = 10;

        if (baseTick == 0){ // first time
            if (AlluyslOriginsPowers.BURROW_OVERLAY.isActive(this.client.player))
                baseTick = currentTick - duration;
            else
                baseTick = currentTick;
        }

        if(bypassNauseaCheck || !this.client.player.hasStatusEffect(StatusEffects.NAUSEA)) {

            if (AlluyslOriginsPowers.BURROW_OVERLAY.isActive(this.client.player)){
                if (currentTick > baseTick + duration)
                    baseTick = currentTick - duration; // avoid going over maximum activation
            } else
                baseTick += 2 * (currentTick - previousTick); // catch up with the current tick to deactivate

            if (currentTick > baseTick)
                this.drawOverlay(MathHelper.sqrt((float)(currentTick - baseTick) / duration), 0.2F, 0.1F, 0.05F);
            else
                baseTick = currentTick; // avoid going under minimum activation

        } else
            baseTick = currentTick;

        previousTick = currentTick;
    }
}