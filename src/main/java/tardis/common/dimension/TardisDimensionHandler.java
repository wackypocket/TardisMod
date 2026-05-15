package tardis.common.dimension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.world.WorldEvent.Load;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import io.darkcraft.darkcore.mod.config.ConfigFile;
import io.darkcraft.darkcore.mod.helpers.MathHelper;
import io.darkcraft.darkcore.mod.helpers.ServerHelper;
import io.darkcraft.darkcore.mod.helpers.WorldHelper;
import tardis.TardisMod;
import tardis.common.TMRegistry;
import tardis.common.core.TardisOutput;
import tardis.common.core.flight.FlightConfiguration;
import tardis.common.core.helpers.Helper;
import tardis.common.tileents.extensions.upgrades.AbstractUpgrade;
import tardis.common.tileents.extensions.upgrades.DimensionUpgrade;

public class TardisDimensionHandler {

    private volatile ArrayList<Integer> dimensionIDs = new ArrayList<Integer>();
    private static ConfigFile config = null;
    private static ArrayList<Integer> blacklistedIDs;
    private static ArrayList<String> blacklistedNames;
    private static HashMap<Integer, Integer> maxHeights = new HashMap<Integer, Integer>();
    private static HashMap<Integer, Integer> minLevels = new HashMap<Integer, Integer>();
    private static HashMap<Integer, Integer> energyCosts = new HashMap<Integer, Integer>();

    static {
        if (config == null) refreshConfigs();
    }

    private Thread scanAfterDelayThread = null;
    private volatile boolean scanAfterDelayRunning = true;
    private Runnable scanAfterDelay = new Runnable() {

        @Override
        public void run() {
            while (scanAfterDelayRunning) {
                try {
                    Thread.sleep(30000);
                    internalFindDimensions();
                } catch (InterruptedException e) {
                    Thread.currentThread()
                        .interrupt();
                    break;
                } catch (Exception e) {}
            }
        }
    };

    private static void splitToTwoInts(String s, HashMap<Integer, Integer> toFill) {
        toFill.clear();
        String[] csvBlobs = s.split(",");
        for (String csvBlob : csvBlobs) {
            String[] intBlobs = csvBlob.split("\\|");
            if (intBlobs.length != 2) continue;
            try {
                int dim = Integer.parseInt(intBlobs[0]);
                int height = Integer.parseInt(intBlobs[1]);
                toFill.put(dim, height);
            } catch (NumberFormatException e) {}
        }
    }

    public static void refreshConfigs() {
        if (config == null) config = TardisMod.configHandler.registerConfigNeeder("dimensions");
        String ids = config.getString(
            "Blacklisted Dimension IDs",
            "",
            "A comma separated blacklist of dimension ids which no tardis should be able to reach");
        String[] splitIDs = ids.split(",");
        blacklistedIDs = new ArrayList<Integer>(splitIDs.length);
        for (String s : splitIDs) {
            int t = MathHelper.toInt(s, 0);
            if (t != 0) blacklistedIDs.add(t);
        }
        String names = config.getString(
            "Blacklisted Dimension Names",
            "",
            "A comma separated list of dimension names which no tardis should be able to reach");
        String[] splitNames = names.split(",");
        blacklistedNames = new ArrayList<String>(splitNames.length);
        for (String s : splitNames) blacklistedNames.add(s);

        String heightsStr = config.getString(
            "Maximum heights",
            "-1|127",
            "A comma separated list of maximum heights in the form dimID|height",
            "E.g. -1|127 which sets the max height of the nether (dim -1) to 127");
        splitToTwoInts(heightsStr, maxHeights);

        String minLevelStr = config.getString(
            "Minimum levels",
            "1|13,-1|5",
            "A comma separated list of minimum levels required to reach dimension",
            "In the form dimID|level");
        splitToTwoInts(minLevelStr, minLevels);

        String enCostStr = config.getString(
            "Energy costs",
            "1|5000,-1|3000",
            "A comma separated list of energy cost to reach dimension",
            "In the form dimID|cost");
        splitToTwoInts(enCostStr, energyCosts);
    }

    public static int getMaxHeight(int dimID) {
        if (maxHeights.containsKey(dimID)) return maxHeights.get(dimID);
        return 255;
    }

    public static int getEnergyCost(int dimID) {
        // Make TARDIS-owned dimensions extremely expensive to hop to/from
        try {
            if (Helper.isTardisWorld(dimID)) {
                return Math.max(FlightConfiguration.energyCostDimChange, 10000);
            }
        } catch (Throwable t) {}
        if (energyCosts.containsKey(dimID)) return energyCosts.get(dimID);
        return FlightConfiguration.energyCostDimChange;
    }

    private boolean isBlacklisted(int id) {
        if (blacklistedIDs.contains(id)) return true;
        World w = WorldHelper.getWorld(id);
        return isBlacklisted(id, w);
    }

    private boolean isBlacklisted(int id, World w) {
        if (blacklistedIDs.contains(id)) return true;
        if (Helper.isTardisWorld(w)) return true;
        String worldName = WorldHelper.getDimensionName(w);
        if (blacklistedNames.contains(worldName)) return true;
        for (String potential : blacklistedNames) {
            if (potential.endsWith("*")) {
                String subPotential = potential.substring(0, potential.length() - 1);
                if (worldName.startsWith(subPotential)) return true;
            }
        }
        return false;
    }

    private boolean addDimension(int id) {
        if (dimensionIDs.contains(id)) return true;
        World w = WorldHelper.getWorld(id);
        return addDimension(w, id);
    }

    private boolean addDimension(World w) {
        if (w == null) return false;
        String name = WorldHelper.getDimensionName(w);
        if ((name == null) || name.isEmpty()) {
            scanAfterDelay();
            return false;
        }
        return addDimension(w, WorldHelper.getWorldID(w));
    }

    private synchronized boolean addDimension(World w, int id) {
        if (w == null) return false;
        if (Helper.isTardisWorld(w)) return false;
        if (dimensionIDs.contains(id)) return true;
        try {
            if (!dimensionIDs.contains(id)) {
                if (isBlacklisted(id)) return false;
                dimensionIDs.add(id);
                TardisOutput.print("TDimH", "Adding dimension: " + id + ", " + WorldHelper.getDimensionName(id));
                cleanUp();
                return true;
            }
        } catch (Exception e) {
            TardisOutput.print("TDimH", "Failed to add dimension: " + id);
        }
        return false;
    }

    private synchronized void cleanUp() {
        HashSet<Integer> uniques = new HashSet<Integer>();
        Iterator<Integer> iter = dimensionIDs.iterator();
        while (iter.hasNext()) {
            Integer i = iter.next();
            if ((i == null) || uniques.contains(i)) iter.remove();
            else uniques.add(i);
        }
    }

    private synchronized void internalFindDimensions() {
        try {
            Field f = DimensionManager.class.getDeclaredField("dimensions");
            f.setAccessible(true);
            Object o = f.get(null);
            if (o instanceof Hashtable) {
                @SuppressWarnings("unchecked")
                Hashtable<Integer, Object> hm = (Hashtable<Integer, Object>) o;
                for (Integer in : hm.keySet()) {
                    addDimension(in);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void scanAfterDelay() {
        if ((scanAfterDelayThread == null) || !scanAfterDelayThread.isAlive()) {
            scanAfterDelayRunning = true;
            scanAfterDelayThread = new Thread(scanAfterDelay);
            scanAfterDelayThread.setDaemon(true);
            scanAfterDelayThread.setName("TardisDimScanner");
            scanAfterDelayThread.start();
        }
    }

    public synchronized void stopScanning() {
        scanAfterDelayRunning = false;
        if (scanAfterDelayThread != null) {
            try {
                scanAfterDelayThread.interrupt();
            } catch (Exception e) {}
            scanAfterDelayThread = null;
        }
    }

    public void findDimensions() {
        if (ServerHelper.isClient()) return;
        WorldServer[] loadedWorlds = DimensionManager.getWorlds();
        for (WorldServer w : loadedWorlds) addDimension(w, WorldHelper.getWorldID(w));
        internalFindDimensions();
    }

    @SubscribeEvent
    public void loadWorld(Load loadEvent) {
        World w = loadEvent.world;
        if (w != null) addDimension(w);
    }

    public int numDims() {
        return Math.max(1, dimensionIDs.size());
    }

    public List<Integer> getDims(int level, TardisDataStore ds) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        if (!dimensionIDs.contains(0)) dimensionIDs.add(0);
        Integer ownerDim = null;
        boolean hasOwnerDim = false;

        // If this TARDIS has reached level 50, determine the owner's personal TARDIS dim
        try {
            if ((ds != null) && (ds.getLevel() >= 50) && (TardisMod.dimReg != null)) {
                String owner = ds.getOwnerName();
                if (owner != null) {
                    ownerDim = TardisMod.plReg.getDimension(owner);
                    if (ownerDim != null) {
                        hasOwnerDim = true;
                        try {
                            World ow = io.darkcraft.darkcore.mod.helpers.WorldHelper.getWorld(ownerDim);
                            if (ow != null)
                                io.darkcraft.darkcore.mod.helpers.WorldHelper.setDimensionName(ow, "Interior");
                        } catch (Throwable t) {}
                    }
                }
            }
        } catch (Exception e) {
            TardisOutput.print("TDimH", "Failed to determine owner tardis dim: " + e.getMessage());
        }

        // Start list with owner's TARDIS if present
        if (hasOwnerDim && (ownerDim != null)) list.add(ownerDim);

        mainLoop: for (Integer dim : dimensionIDs) {
            // Skip the owner's own TARDIS if we've already added it
            if (hasOwnerDim && (ownerDim != null) && ownerDim.equals(dim)) continue mainLoop;
            if (TMRegistry.dimensionUpgradeItems.containsKey(dim)) {
                if (ds == null) continue;
                for (AbstractUpgrade up : ds.upgrades) {
                    if (up instanceof DimensionUpgrade) {
                        if (((DimensionUpgrade) up).getDimID() == dim) {
                            list.add(dim);
                            continue mainLoop;
                        }
                    }
                }
                continue mainLoop;
            }
            if (minLevels.containsKey(dim) && (minLevels.get(dim) > level)) continue;
            list.add(dim);
        }
        return list;
    }

    public int numDims(int level, TardisDataStore ds) {
        return getDims(level, ds).size();
    }

    public Integer getControlFromDim(int dim, int level, TardisDataStore ds) {
        if (ServerHelper.isClient()) return 0;
        cleanUp();
        List<Integer> dims = getDims(level, ds);
        if (dimensionIDs.contains(dim)) {
            if (dims.contains(dim)) return dims.indexOf(dim);
        } else {
            World w = WorldHelper.getWorldServer(dim);
            if ((w != null) && !Helper.isTardisWorld(w)
                && !blacklistedIDs.contains(WorldHelper.getWorldID(w))
                && !blacklistedNames.contains(WorldHelper.getDimensionName(w))) {
                if (addDimension(w)) return getControlFromDim(dim, level, ds);
            }
        }
        if (dimensionIDs.contains(0)) return dimensionIDs.indexOf(0);
        return 0;
    }

    public Integer getDimFromControl(int control, int level, TardisDataStore ds) {
        if (ServerHelper.isClient()) return 0;
        List<Integer> dims = getDims(level, ds);
        int index = MathHelper.clamp(control, 0, dims.size() - 1);
        Integer dim = dims.get(index);
        if (dim == null) return 0;
        return dim;
    }

    public static int getMaxHeight(World w) {
        return getMaxHeight(WorldHelper.getWorldID(w));
    }
}
