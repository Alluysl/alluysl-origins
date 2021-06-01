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

    // Code from Mojang, mapping from Yarn, few edits from me, essentially a modified method_31136 (from GameRenderer)
    private void drawBurrowOverlayOnScreen(float ratio, float r, float g, float b) {
        int clientWidth = this.client.getWindow().getScaledWidth();
        int clientHeight = this.client.getWindow().getScaledHeight();
        double scale = MathHelper.lerp((double)ratio, 2.0D, 1.0D);
        r *= ratio;
        g *= ratio;
        b *= ratio;
        double width = (double)clientWidth * scale;
        double height = (double)clientHeight * scale;
        double left = ((double)clientWidth - width) / 2.0D;
        double top = ((double)clientHeight - height) / 2.0D;
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE);
        RenderSystem.color4f(r, g, b, 1.0F); // alpha has no effect
        this.client.getTextureManager().bindTexture(field_26730);
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

    private int framesSinceStart = -1;

    // Heavily based on Origin's game renderer phantomzied overlay mixin

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F"))
    private void drawBurrowOverlay(CallbackInfo ci) {
        if(!this.client.player.hasStatusEffect(StatusEffects.NAUSEA)) {
            if (AlluyslOriginsPowers.BURROW_OVERLAY.isActive(this.client.player)){
                if (framesSinceStart < 30)
                    ++framesSinceStart;
            } else if (framesSinceStart > 0)
                --framesSinceStart;
            if (framesSinceStart > 0)
                this.drawBurrowOverlayOnScreen(MathHelper.sqrt(framesSinceStart / 30.0F), 0.2F, 0.1F, 0.05F);
        } else
            framesSinceStart = 0;
    }
}