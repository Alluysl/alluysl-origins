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

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.GL_FUNC_ADD;
import static org.lwjgl.opengl.GL14.GL_FUNC_SUBTRACT;
import static org.lwjgl.opengl.GL14.GL_FUNC_REVERSE_SUBTRACT;
import static org.lwjgl.opengl.GL14.GL_MIN;
import static org.lwjgl.opengl.GL14.GL_MAX;

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
        bufferBuilder.vertex(vertices[3][1], vertices[3][1], -90.0D).texture(0.0F, 0.0F).next(); // top left
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

    private void drawDefaultOverlay(float ratio, float r, float g, float b){
        setTextureCentered(MathHelper.lerp(ratio, 2.0F, 1.0F));
        this.r = ratio * r;
        this.g = ratio * g;
        this.b = ratio * b;
        a = 1.0F;
        resetTexture();
        blendEquation = defaultBlendEquation;
        srcFactor = dstFactor = srcAlpha = dstAlpha = GL_ONE;
        drawTexture();
    }

    private int baseTick = 0;
    private int previousTick = 0;

    public boolean bypassNauseaCheck = true;

    @Shadow private int ticks;

    // Heavily based on Origin's game renderer phantomzied overlay mixin

    @Shadow
    @Final
    private MinecraftClient client;

//    private int testFuncVal = GL_FUNC_ADD;
//    private String testFuncName = "add";
//    private int testTick = 0;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F"))
    private void drawBurrowOverlay(CallbackInfo ci) {
        int currentTick = ticks;
        int duration = 10;

        if (this.client.player == null)
            return;

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
                drawDefaultOverlay(MathHelper.sqrt((float)(currentTick - baseTick) / duration), 0.2F, 0.1F, 0.05F);
            else
                baseTick = currentTick; // avoid going under minimum activation

        } else
            baseTick = currentTick;

        previousTick = currentTick;

//        if (currentTick % 50 == 49 && currentTick != testTick){
//            switch (testFuncVal){
//                case GL_FUNC_ADD: testFuncVal = GL_FUNC_SUBTRACT; testFuncName = "sub "; break;
//                case GL_FUNC_SUBTRACT: testFuncVal = GL_FUNC_REVERSE_SUBTRACT; testFuncName = "subr"; break;
//                case GL_FUNC_REVERSE_SUBTRACT: testFuncVal = GL_MIN; testFuncName = "min "; break;
//                case GL_MIN: testFuncVal = GL_MAX; testFuncName = "max "; break;
//                case GL_MAX: testFuncVal = NO_BLEND; testFuncName = "no b"; break;
//                default: testFuncVal = GL_FUNC_ADD; testFuncName = "add ";
//            }
//        }
//
//        testTick = currentTick;
//
//        if (currentTick % 600 >= 299){
//            srcFactor = srcAlpha = GL_SRC_ALPHA;
//            dstFactor = dstAlpha = GL_ONE_MINUS_SRC_ALPHA;
//        } else
//            srcFactor = dstFactor = srcAlpha = dstAlpha = GL_ONE;
//
//        setTexture("textures/misc/forcefield.png");
//        setTextureBoxed(10, 10, 200, 200);
//        blendEquation = testFuncVal;
//        System.out.println(testFuncName);
//        r = g = b = a = 1.0F;
//        drawTexture();
    }
}