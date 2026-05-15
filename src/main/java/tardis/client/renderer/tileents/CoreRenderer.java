package tardis.client.renderer.tileents;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.opengl.GL11;

import io.darkcraft.darkcore.mod.abstracts.AbstractBlock;
import io.darkcraft.darkcore.mod.abstracts.AbstractBlockRenderer;
import tardis.client.renderer.model.RotorModel;
import tardis.common.TMRegistry;
import tardis.common.tileents.CoreTileEntity;

public class CoreRenderer extends AbstractBlockRenderer {

    RotorModel rotor;
    // OctagonModel oct;
    IModelCustom oct;
    IModelCustom cap;
    IModelCustom ang;
    IModelCustom scr;
    ResourceLocation rotorTex = new ResourceLocation("tardismod", "textures/models/TardisRotorA.png");
    ResourceLocation octTex = new ResourceLocation("tardismod", "textures/models/oct.png");
    ResourceLocation capTex = new ResourceLocation("tardismod", "textures/models/cap.png");
    ResourceLocation angTex = new ResourceLocation("tardismod", "textures/models/ang.png");
    ResourceLocation scrTex = new ResourceLocation("tardismod", "textures/models/screen.png");

    public CoreRenderer() {
        rotor = new RotorModel();
        // oct = new OctagonModel();
        oct = AdvancedModelLoader.loadModel(new ResourceLocation("tardismod", "models/oct.obj"));
        cap = AdvancedModelLoader.loadModel(new ResourceLocation("tardismod", "models/cap.obj"));
        ang = AdvancedModelLoader.loadModel(new ResourceLocation("tardismod", "models/ang.obj"));
        scr = AdvancedModelLoader.loadModel(new ResourceLocation("tardismod", "models/screen.obj"));
    }

    @Override
    public AbstractBlock getBlock() {
        return TMRegistry.tardisCoreBlock;
    }

    private void renderRotor(Tessellator tess, CoreTileEntity core) {
        GL11.glPushMatrix();
        GL11.glTranslatef(0.5F, 0, 0.5F);
        GL11.glRotatef(180F, 0F, 0, 1F);
        GL11.glTranslatef(0F, -0.7F, 0F);
        bindTexture(rotorTex);
        float proximity = core.getProximity();
        // GL11.glColor4f(1F, 1F, 1F, tte.getTransparency());
        GL11.glScaled(0.75, 1, 0.75);
        GL11.glPushMatrix();
        GL11.glTranslatef(0, 0.4F - proximity, 0);
        rotor.render(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glRotatef(180F, 0F, 0, 1F);
        GL11.glTranslatef(0, 0.4F - proximity, 0);
        rotor.render(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
        GL11.glPopMatrix();
        GL11.glPopMatrix();
    }

    private void renderOct(Tessellator tess) {
        GL11.glTranslated(0, 0.0675, 0);
        GL11.glScaled(0.76, 0.65, 0.76);
        GL11.glRotated(180, 0, 0, 1);
        oct.renderAll();
    }

    private void renderSpinner(Tessellator tess, CoreTileEntity core) {
        double spin = core.getSpin();
        GL11.glPushMatrix();
        bindTexture(octTex);
        // bindTexture(new ResourceLocation("tardismod","textures/models/Octagon.png"));
        GL11.glTranslated(0.5, 2.3, 0.5);
        GL11.glRotated(180, 1, 0, 0);
        GL11.glScaled(1.33, 1.6, 1.33);
        GL11.glPushMatrix();
        GL11.glTranslated(0, -0.33, 0);
        GL11.glScaled(1.15, 1.15, 1.15);
        GL11.glRotated(spin + (2 * 45), 0, 1, 0);
        renderOct(tess);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslated(0, -0.63, 0);
        GL11.glScaled(1.45, 1.2, 1.45);
        GL11.glRotated(spin + (3 * 45), 0, -1, 0);
        renderOct(tess);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslated(0, -0.98, 0);
        GL11.glScaled(1.8, 1.4, 1.8);
        GL11.glRotated(spin + (4 * 45), 0, 1, 0);
        renderOct(tess);
        GL11.glPopMatrix();
        GL11.glPopMatrix();
    }

    private void renderScreen(Tessellator tess, CoreTileEntity te) {
        GL11.glPushMatrix();
        int angle = te.getScreenAngle();
        GL11.glTranslated(0.5, -0.5, 0.5);
        GL11.glRotated(angle, 0, 1, 0);
        GL11.glTranslated(0.4, 0, 0);
        GL11.glScaled(0.5, 0.5, 0.5);
        bindTexture(scrTex);
        scr.renderAll();
        GL11.glPushMatrix();
        GL11.glRotated(180, 0, 0, 1);
        GL11.glRotated(90, 0, 1, 0);
        GL11.glTranslated(-0.75, -1.37, -0.583);
        double scale = 0.02;
        GL11.glScaled(scale, scale, scale);
        GL11.glDepthMask(false);
        // Ensure on-screen text is rendered bright/white regardless of lighting
        World w = te.getWorldObj();
        int sx = te.xCoord;
        int sy = te.yCoord;
        int sz = te.zCoord;
        int prevL1 = 0;
        int prevL2 = 0;
        if (w != null) {
            int actualLight = w.getLightBrightnessForSkyBlocks(sx, sy, sz, 0);
            prevL1 = actualLight % 65536;
            prevL2 = actualLight >> 16;
        }
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glColor3d(1.0, 1.0, 1.0);
        String[] strings = te.getScreenText();
        for (String s : strings) {
            fr.drawString(s, 0, 0, 0xFFFFFF);
            GL11.glTranslated(0, 10, 0);
        }
        GL11.glEnable(GL11.GL_LIGHTING);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, prevL1, prevL2);
        GL11.glDepthMask(true);
        GL11.glPopMatrix();
        GL11.glPopMatrix();

    }

    private void renderCaps(Tessellator tess, CoreTileEntity te) {
        GL11.glPushMatrix();
        bindTexture(capTex);
        double sc = 0.5;
        GL11.glTranslated(0.5, 0, 0.5);
        GL11.glScaled(sc, sc, sc);
        GL11.glPushMatrix();
        GL11.glTranslated(0, -0.55, 0);
        GL11.glRotated(180, 1, 0, 0);
        cap.renderAll();
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslated(0, 3.35, 0);
        cap.renderAll();
        GL11.glPopMatrix();
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glTranslated(0.5, 2.37, 0.5);
        GL11.glScaled(sc, 1, sc);
        bindTexture(angTex);
        ang.renderAll();
        GL11.glPopMatrix();
    }

    @Override
    public void renderBlock(Tessellator tess, TileEntity te, int x, int y, int z) {
        if (te instanceof CoreTileEntity) {
            CoreTileEntity core = (CoreTileEntity) te;
            renderRotor(tess, core);
            renderCaps(tess, core);
            renderSpinner(tess, core);
            renderScreen(tess, core);
        }
    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double d0, double d1, double d2, float f) {
        if (fr == null) fr = func_147498_b();
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glTranslatef((float) d0, (float) d1, (float) d2);

        World w = tileEntity.getWorldObj();
        int x = tileEntity.xCoord;
        int y = tileEntity.yCoord;
        int z = tileEntity.zCoord;

        Tessellator tessellator = Tessellator.instance;

        if (handleLighting() && (w != null)) {
            // Apply a small boost to the environment lightmap so the core glows white but still responds
            float actualBrightness = w.getBlockLightValue(x, y, z);
            int actualLight = w.getLightBrightnessForSkyBlocks(x, y, z, 0);
            float boostedBrightness = Math.min(1.0f, actualBrightness * 1.2f + 0.1f);
            if (boostedBrightness < 0.2f) boostedBrightness = 0.2f;
            int l1 = actualLight % 65536;
            int l2 = actualLight >> 16;
            l1 = Math.min(240, l1 + 20);
            l2 = Math.min(240, l2 + 10);

            tessellator.setColorOpaque_F(boostedBrightness, boostedBrightness, boostedBrightness);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, l1, l2);
            GL11.glEnable(GL11.GL_NORMALIZE);
        }

        renderBlock(tessellator, tileEntity, x, y, z);
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

}
