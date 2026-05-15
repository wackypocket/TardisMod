package tardis.common.dimension;

import net.minecraft.entity.Entity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.common.DimensionManager;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.darkcraft.darkcore.mod.helpers.MathHelper;
import tardis.common.core.helpers.Helper;

public class TardisWorldProvider extends WorldProvider {

    public final ChunkCoordinates spawnPoint = new ChunkCoordinates(9, Helper.tardisCoreY, 0);
    private int cachedMode = Integer.MIN_VALUE;
    private int cachedAmbientBucket = Integer.MIN_VALUE;

    public TardisWorldProvider() {}

    @Override
    public String getDimensionName() {
        return "Tardis Interior";
    }

    @Override
    public String getSaveFolder() {
        return (dimensionId == 0 ? null : "tardis/DIM" + dimensionId);
    }

    @Override
    public boolean canRespawnHere() {
        return true;
    }

    @Override
    protected void registerWorldChunkManager() {
        worldChunkMgr = new TardisChunkManager(worldObj);
    }

    @Override
    public IChunkProvider createChunkGenerator() {
        return new TardisChunkProvider(worldObj);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IRenderHandler getSkyRenderer() {
        if (dimensionId != 0) {
            TardisDataStore ds = Helper.getDataStore(worldObj);
            if (ds == null) return super.getSkyRenderer();
            if (ds.getSpaceProjection()) return new TardisWorldSkyRenderer();
            else {
                return super.getSkyRenderer();
            }
        }
        return super.getSkyRenderer();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IRenderHandler getCloudRenderer() {
        if (dimensionId != 0) {
            TardisDataStore ds = Helper.getDataStore(worldObj);
            if (ds == null) return super.getCloudRenderer();
            if (ds.getSpaceProjection()) return new TardisWorldSkyRenderer();
            else return super.getCloudRenderer();
        }
        return super.getCloudRenderer();
    }

    @Override
    public void updateWeather() {
        worldObj.prevRainingStrength = 0;
        worldObj.rainingStrength = 0;
        worldObj.prevRainingStrength = 0;
        worldObj.thunderingStrength = 0;
        worldObj.updateWeatherBody();
        if (worldObj.isRaining()) {
            worldObj.setRainStrength(0);
            WorldInfo wi = worldObj.getWorldInfo();
            if (wi != null) {
                wi.setRaining(false);
                wi.setRainTime(Integer.MAX_VALUE);
            }
        }
    }

    @Override
    public ChunkCoordinates getSpawnPoint() {
        return spawnPoint;
    }

    @Override
    public ChunkCoordinates getRandomizedSpawnPoint() {
        return getSpawnPoint();
    }

    @Override
    public long getWorldTime() {
        ensureBrightnessTableFresh();
        return getProjectedWorldTime();
    }

    public float calculateRealCelestialAngle(float var) {
        ensureBrightnessTableFresh();
        return projectedCelestialAngle(var);
    }

    @Override
    public float calculateCelestialAngle(long worldTime, float partialTicks) {
        ensureBrightnessTableFresh();
        return projectedCelestialAngle(partialTicks);
    }

    public int getWorldVariance() {
        return MathHelper.round((Math.random() * 10) - 5);
    }

    @Override
    protected void generateLightBrightnessTable() {
        float ambient = getProjectedAmbientBrightness();
        for (int i = 0; i <= 15; ++i) {
            float vanillaLevel = 1.0F - (i / 15.0F);
            vanillaLevel = (1.0F - vanillaLevel) / (vanillaLevel * 3.0F + 1.0F);
            // Maintain vanilla light shaping but enforce outdoor-like ambient floor.
            this.lightBrightnessTable[i] = Math.max(ambient, vanillaLevel);
        }
    }

    private long getProjectedWorldTime() {
        if ((dimensionId == 0) || !canUseTemporalControls()) return super.getWorldTime();
        TardisDataStore ds = getClientDataStore();
        if (ds == null) return super.getWorldTime();
        int mode = ds.getDaytimeSetting();
        switch (mode) {
            case 0:
                return 18000L;
            case 2:
                return 6000L;
            default:
                // Do not force-load dimensions from here; this method can run during world bootstrap.
                World overworld = DimensionManager.getWorld(0);
                if (overworld != null) return overworld.getWorldTime();
                return super.getWorldTime();
        }
    }

    private float projectedCelestialAngle(float partialTicks) {
        long projectedTime = getProjectedWorldTime();
        int j = (int) (projectedTime % 24000L);
        float f1 = ((j + partialTicks) / 24000.0F) - 0.25F;
        if (f1 < 0.0F) ++f1;
        if (f1 > 1.0F) --f1;
        float f2 = f1;
        f1 = 1.0F - (float) ((Math.cos(f1 * Math.PI) + 1.0D) / 2.0D);
        return f2 + ((f1 - f2) / 3.0F);
    }

    private float getProjectedAmbientBrightness() {
        float angle = projectedCelestialAngle(1.0F);
        float sunlight = 1.0F - ((float) Math.cos(angle * ((float) Math.PI * 2.0F)) * 2.0F + 0.2F);
        if (sunlight < 0.0F) sunlight = 0.0F;
        if (sunlight > 1.0F) sunlight = 1.0F;
        sunlight = 1.0F - sunlight;
        return sunlight;
    }

    private void ensureBrightnessTableFresh() {
        if ((dimensionId == 0) || !canUseTemporalControls()) return;
        TardisDataStore ds = getClientDataStore();
        int mode = (ds == null) ? 1 : ds.getDaytimeSetting();
        float ambient = getProjectedAmbientBrightness();
        int ambientBucket = MathHelper.round(ambient * 1000);
        if ((mode != cachedMode) || (ambientBucket != cachedAmbientBucket)) {
            cachedMode = mode;
            cachedAmbientBucket = ambientBucket;
            generateLightBrightnessTable();
        }
    }

    private boolean canUseTemporalControls() {
        // Keep temporal overrides client-side only; server-side access can recurse during dimension bootstrap.
        if (worldObj == null) return false;
        if (!worldObj.isRemote) return false;
        if (worldObj.getWorldInfo() == null) return false;
        if (worldObj.getChunkProvider() == null) return false;
        return true;
    }

    private TardisDataStore getClientDataStore() {
        if (!canUseTemporalControls()) return null;
        try {
            return Helper.getDataStore(worldObj);
        } catch (Throwable t) {
            return null;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Vec3 getSkyColor(Entity cameraEntity, float partialTicks) {
        if (dimensionId != 0) {
            TardisDataStore ds = Helper.getDataStore(worldObj);
            if (ds == null) return super.getSkyColor(cameraEntity, partialTicks);
            if (ds.getSpaceProjection()) return Vec3.createVectorHelper(0, 0, 0);
            else return super.getSkyColor(cameraEntity, partialTicks);
        }
        return super.getSkyColor(cameraEntity, partialTicks);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getStarBrightness(float par1) {
        if (dimensionId != 0) {
            TardisDataStore ds = Helper.getDataStore(worldObj);
            if (ds == null) return super.getStarBrightness(par1);
            if (ds.getSpaceProjection()) return 1f;
            else return super.getStarBrightness(par1);
        }
        return super.getStarBrightness(par1);
    }

    @Override
    public double getHorizon() {
        if (dimensionId != 0) {
            TardisDataStore ds = Helper.getDataStore(worldObj);
            if (ds == null) return super.getHorizon();
            if (ds.getSpaceProjection()) return 0;
            else return super.getHorizon();
        }
        return super.getHorizon();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float[] calcSunriseSunsetColors(float p_76560_1_, float p_76560_2_) {
        if (dimensionId != 0) {
            TardisDataStore ds = Helper.getDataStore(worldObj);
            if (ds == null) return super.calcSunriseSunsetColors(p_76560_1_, p_76560_2_);
            if (ds.getSpaceProjection()) return null;
            else return super.calcSunriseSunsetColors(p_76560_1_, p_76560_2_);
        }
        return super.calcSunriseSunsetColors(p_76560_1_, p_76560_2_);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getCloudHeight() {
        if (dimensionId != 0) {
            TardisDataStore ds = Helper.getDataStore(worldObj);
            if (ds == null) return super.getCloudHeight();
            if (ds.getSpaceProjection()) return 1f;
            else return super.getCloudHeight();
        }
        return super.getCloudHeight();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Vec3 getFogColor(float p_76562_1_, float p_76562_2_) {
        if (dimensionId != 0) {
            TardisDataStore ds = Helper.getDataStore(worldObj);
            if (ds == null) return super.getFogColor(p_76562_1_, p_76562_2_);
            if (ds.getSpaceProjection()) return Vec3.createVectorHelper(0, 0, 0);
            else return super.getFogColor(p_76562_1_, p_76562_2_);
        }
        return super.getFogColor(p_76562_1_, p_76562_2_);
    }
}
