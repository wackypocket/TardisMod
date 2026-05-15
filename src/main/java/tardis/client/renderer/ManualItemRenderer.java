package tardis.client.renderer;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.opengl.GL11;

public class ManualItemRenderer implements IItemRenderer {

    private static IModelCustom scr;
    private static ResourceLocation scrTex = new ResourceLocation("tardismod", "textures/models/screen.png");

    public ManualItemRenderer() {
        if (scr == null)
            scr = AdvancedModelLoader.loadModel(new ResourceLocation("tardismod", "models/handscreen.obj"));
    }

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return true;
    }

    private void renderText() {
        List<String> data = null;

        EntityPlayer pl = Minecraft.getMinecraft().thePlayer;
        if (pl == null) return;

        MovingObjectPosition mop = pl.rayTrace(3, 1);
        if (mop == null) return;

        Block b = pl.worldObj.getBlock(mop.blockX, mop.blockY, mop.blockZ);
        if (b == null) return;

        int meta = pl.worldObj.getBlockMetadata(mop.blockX, mop.blockY, mop.blockZ);

        // Use our comprehensive fallback system to provide control information
        data = getTardisControlInfo(b, meta, pl.worldObj, mop.blockX, mop.blockY, mop.blockZ, pl, mop);

        if (data != null && data.size() > 0) {
            double sx = 0.01;
            int maxSize = 0;
            for (String s : data) maxSize = Math.max(maxSize, s.length());
            // sx = 0.25 / maxSize;
            FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
            GL11.glDepthMask(false);
            GL11.glRotated(180, 0, 0, 1);
            GL11.glRotated(90, 0, 1, 0);
            GL11.glTranslated(-0.775, -0.5, -0.1407);
            GL11.glScaled(sx, sx, sx);
            int y = 0;
            for (String s : data) {
                List<String> inData = fr.listFormattedStringToWidth(s, 155);
                for (String text : inData) {
                    fr.drawString(text, 0, y, 16579836);
                    y += fr.FONT_HEIGHT;
                }
            }
            GL11.glDepthMask(true);
        }
    }

    private void renderScreen() {
        scr.renderAll();
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        GL11.glPushMatrix();
        GL11.glScaled(0.5, 0.5, 0.5);
        if (type.equals(ItemRenderType.EQUIPPED)) {
            GL11.glRotated(45, 0, 1, 0);
            GL11.glRotated(-65, 1, 0, 0);
            GL11.glRotated(-90, 0, 1, 0);
            GL11.glTranslated(1.45, -0.8, 1);
            GL11.glScaled(1.65, 1.65, 1.65);
        } else if (type.equals(ItemRenderType.EQUIPPED_FIRST_PERSON)) {
            GL11.glRotated(220, 0, 1, 0);
            GL11.glTranslated(1, 2.2, 0);
            GL11.glScaled(1.65, 1.65, 1.65);
        } else if (type.equals(ItemRenderType.INVENTORY)) {
            GL11.glScaled(1.75, 1.75, 1.75);
        }
        GL11.glPushMatrix();
        Minecraft.getMinecraft().renderEngine.bindTexture(scrTex);
        renderScreen();
        GL11.glPopMatrix();
        if (type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
            GL11.glPushMatrix();
            renderText();
            GL11.glPopMatrix();
        }
        GL11.glPopMatrix();
    }

    private List<String> getTardisControlInfo(Block block, int meta, World world, int x, int y, int z,
        EntityPlayer player, MovingObjectPosition mop) {
        List<String> info = new ArrayList<String>();

        try {
            // Check if this is a TARDIS-related block that we should show control info for
            if (block == net.minecraft.init.Blocks.air) {
                return null; // Don't show info for air blocks
            }

            // Check if it's a TARDIS console or component block
            if (block == tardis.common.TMRegistry.schemaComponentBlock) {
                // This is a console component - try to get control info
                if (meta == 3 || meta == 6) { // Console controls
                    tardis.common.tileents.ConsoleTileEntity console = tardis.common.core.helpers.Helper
                        .getTardisConsole(world);

                    if (console != null) {
                        int controlId = console.getControlFromHit(x, y, z, mop.hitVec, player);
                        if (controlId != -1) {
                            List<String> controlInfo = getConsoleControlInfo(controlId);
                            // Add control name first
                            if (!controlInfo.isEmpty()) {
                                info.add(controlInfo.get(0)); // "Control: [Name]"
                            }

                            // Add extra info before description
                            String[] extraInfo = console.getExtraInfo(controlId);
                            if (extraInfo != null) {
                                for (String extra : extraInfo) {
                                    info.add(extra);
                                }
                            }

                            // Add blank line and description last (skip first item as it's already added)
                            for (int i = 1; i < controlInfo.size(); i++) {
                                info.add(controlInfo.get(i));
                            }
                        }
                    }
                } else if (meta == 7) { // Engine controls
                    tardis.common.tileents.EngineTileEntity engine = tardis.common.core.helpers.Helper
                        .getTardisEngine(world);

                    if (engine != null) {
                        int controlId = engine.getControlFromHit(x, y, z, mop.hitVec, player);
                        if (controlId != -1) {
                            List<String> controlInfo = getEngineControlInfo(controlId);
                            // Add control name first
                            if (!controlInfo.isEmpty()) {
                                info.add(controlInfo.get(0)); // "Control: [Name]"
                            }

                            // Add extra info (like "Level: 0(0)") before description
                            String[] extraInfo = engine.getExtraInfo(controlId);
                            if (extraInfo != null) {
                                for (String extra : extraInfo) {
                                    info.add(extra);
                                }
                            }

                            // Add blank line and description last (skip first item as it's already added)
                            for (int i = 1; i < controlInfo.size(); i++) {
                                info.add(controlInfo.get(i));
                            }
                        }
                    }
                }
            } else if (block == tardis.common.TMRegistry.tardisConsoleBlock) {
                // Direct console block
                tardis.common.tileents.ConsoleTileEntity console = tardis.common.core.helpers.Helper
                    .getTardisConsole(world);

                if (console != null) {
                    int controlId = console.getControlFromHit(x, y, z, mop.hitVec, player);
                    if (controlId != -1) {
                        List<String> controlInfo = getConsoleControlInfo(controlId);
                        // Add control name first
                        if (!controlInfo.isEmpty()) {
                            info.add(controlInfo.get(0)); // "Control: [Name]"
                        }

                        // Add extra info before description
                        String[] extraInfo = console.getExtraInfo(controlId);
                        if (extraInfo != null) {
                            for (String extra : extraInfo) {
                                info.add(extra);
                            }
                        }

                        // Add blank line and description last (skip first item as it's already added)
                        for (int i = 1; i < controlInfo.size(); i++) {
                            info.add(controlInfo.get(i));
                        }
                    }
                }
            } else if (block == tardis.common.TMRegistry.tardisEngineBlock) {
                // Direct engine block
                tardis.common.tileents.EngineTileEntity engine = tardis.common.core.helpers.Helper
                    .getTardisEngine(world);

                if (engine != null) {
                    int controlId = engine.getControlFromHit(x, y, z, mop.hitVec, player);
                    if (controlId != -1) {
                        List<String> controlInfo = getEngineControlInfo(controlId);
                        // Add control name first
                        if (!controlInfo.isEmpty()) {
                            info.add(controlInfo.get(0)); // "Control: [Name]"
                        }

                        // Add extra info (like "Level: 0(0)") before description
                        String[] extraInfo = engine.getExtraInfo(controlId);
                        if (extraInfo != null) {
                            for (String extra : extraInfo) {
                                info.add(extra);
                            }
                        }

                        // Add blank line and description last (skip first item as it's already added)
                        for (int i = 1; i < controlInfo.size(); i++) {
                            info.add(controlInfo.get(i));
                        }
                    }
                }
            } else if (block == tardis.common.TMRegistry.battery) {
                // Battery block
                TileEntity te = world.getTileEntity(x, y, z);
                if (te instanceof tardis.common.tileents.BatteryTileEntity) {
                    tardis.common.tileents.BatteryTileEntity battery = (tardis.common.tileents.BatteryTileEntity) te;
                    info.add("Artron Battery");
                    info.add("Energy: " + battery.getArtronEnergy());
                    info.add("Max Energy: " + battery.getMaxArtronEnergy());

                    // Add mode information to match WAILA display
                    int mode = battery.getMode();
                    switch (mode) {
                        case 1:
                            info.add("Mode: Uncoordinated Flight");
                            break;
                        case 2:
                            info.add("Mode: Coordinated Flight");
                            break;
                        default:
                            info.add("Mode: Landed");
                            break;
                    }
                }
            } else
                if (block == tardis.common.TMRegistry.tardisBlock || block == tardis.common.TMRegistry.tardisTopBlock) {
                    // TARDIS exterior block
                    TileEntity te = world.getTileEntity(x, y, z);
                    if (block == tardis.common.TMRegistry.tardisTopBlock) {
                        // If it's the top block, check the block below
                        te = world.getTileEntity(x, y - 1, z);
                    }
                    if (te instanceof tardis.common.tileents.TardisTileEntity) {
                        tardis.common.tileents.TardisTileEntity tardis = (tardis.common.tileents.TardisTileEntity) te;
                        info.add("TARDIS");
                        if (tardis.owner != null) {
                            info.add("Owner: " + tardis.owner);
                        }
                    }
                }

            // If we don't have any specific control info, don't show anything
            if (info.isEmpty()) {
                return null;
            }

        } catch (Exception e) {
            // If something goes wrong, return null to show no info
            return null;
        }

        return info;
    }

    private List<String> getConsoleControlInfo(int controlId) {
        List<String> info = new ArrayList<String>();

        // Mirror the control information from WailaConsoleProvider
        switch (controlId) {
            case 0:
                info.add("Control: Energy Gauge");
                info.add("");
                info.add("- Displays how much Artron Energy is available.");
                break;
            case 1:
                info.add("Control: Rooms Counter");
                info.add("");
                info.add("- Displays how many rooms you have.");
                break;
            case 2:
                info.add("Control: Speedometer");
                info.add("");
                info.add("- Displays the current speed after rooms have been taken into account.");
                break;
            case 3:
                info.add("Control: Facing Wheel");
                info.add("");
                info.add("- Controls which way the TARDIS will face when landing.");
                break;
            case 4:
                info.add("Control: Speed Lever");
                info.add("");
                info.add("- Controls the max speed at which you can fly");
                break;
            case 5:
                info.add("Control: Screwdriver Button");
                info.add("");
                info.add("- Generates a new screwdriver or absorbs an old screwdriver");
                break;
            case 6:
            case 7:
                info.add("Control: Screwdriver Slot");
                break;
            case 8:
                info.add("Control: XP Gauge");
                info.add("");
                info.add("- Displays how much XP remaining until the next TARDIS level.");
                break;
            case 9:
                info.add("Control: Shield Gauge");
                info.add("");
                info.add("- Displays how strong the shields are");
                break;
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
                info.add("Control: X Control");
                break;
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
                info.add("Control: Z Control");
                break;
            case 30:
            case 31:
            case 32:
            case 33:
                info.add("Control: Y Control");
                break;
            case 34:
                info.add("Control: Land Ground Control");
                info.add("");
                info.add("- Controls whether the TARDIS can land in mid air");
                break;
            case 40:
                info.add("Control: Temporal Primer");
                info.add("");
                info.add("- The first part of the Takeoff sequence");
                break;
            case 41:
                info.add("Control: Helmic Regulator");
                info.add("");
                info.add("- The second part of the Takeoff sequence");
                break;
            case 42:
                info.add("Control: Quantum Handbrake");
                info.add("");
                info.add("- The third and final part of the Takeoff sequence");
                break;
            case 43:
                info.add("Control: Zigzag Plotter");
                info.add("");
                info.add("- Randomizes the coordinate levers. Adventure button");
                break;
            case 50:
                info.add("Control: Prev Schema Button");
                info.add("");
                info.add("- Selects the previous schema in the current category");
                break;
            case 51:
                info.add("Control: Next Schema Button");
                info.add("");
                info.add("- Selects the next schema in the current category");
                break;
            case 52:
                info.add("Control: Interior Temporal Control");
                info.add("");
                info.add("- Controls the internal projected time");
                break;
            case 53:
                info.add("Control: Relative Coords Switch");
                info.add("");
                info.add("- Switches between using absolute or relative controls");
                break;
            case 54:
                info.add("Control: External Scanner");
                info.add("");
                info.add("- Gives a basic idea of what is outside the TARDIS");
                break;
            case 55:
                info.add("Control: Uncoordinated Flight Control");
                info.add("");
                info.add("- Switches between drifting (never landing) and coordinated (going to a destination) flight");
                break;
            case 56:
                info.add("Control: Flight Stabilizer");
                info.add("");
                info.add("- Prevents buttons from becoming unstable, allowing unsupervised (low xp) flights");
                break;
            case 57:
                info.add("Control: Prev Category Button");
                info.add("");
                info.add("- Selects the previous schema category");
                break;
            case 58:
                info.add("Control: Next Category Button");
                info.add("");
                info.add("- Selects the next schema category");
                break;
            case 60:
                info.add("Control: Dimension Lever");
                info.add("");
                info.add("- Controls which dimension the TARDIS will land in");
                break;
            case 100:
                info.add("Control: Coordinate Guesser");
                info.add("");
                info.add("- Attempts to calculate where the TARDIS will land");
                break;
            case 900:
                info.add("Control: Coordinate Save/Load Mode Control");
                info.add("");
                info.add("- Controls whether pressing a save slot will save to or load that slot");
                break;
            case 901:
                info.add("Control: Room Deletion Control");
                info.add("");
                info.add("- Deletes all non-console rooms in the TARDIS");
                break;
            case 902:
                info.add("Control: Last Coordinates Store");
                info.add("");
                info.add("- Sets the coordinates to the last place you landed");
                break;
            case 903:
                info.add("Control: Current Coordinates Store");
                info.add("");
                info.add("- Sets the coordinates to the current location");
                break;
            case 904:
                info.add("Control: Landing Pad Control");
                info.add("");
                info.add("- Sets whether the TARDIS will attempt to find a landing pad to land on or not");
                break;
            default:
                // Handle ranges that don't fit in simple case statements
                if (controlId >= 1000 && controlId <= 1019) {
                    info.add("Control: Save Slots");
                    info.add("");
                    info.add("- Save/load coordinate presets");
                } else if (controlId >= 1020 && controlId <= 1032) {
                    info.add("Control: Flight Controls");
                    info.add("");
                    info.add(
                        "- Controls which may need to be pressed during flight. Increased speed increases difficulty");
                } else {
                    info.add("Control: Unknown Control");
                }
                break;
        }

        return info;
    }

    private List<String> getEngineControlInfo(int controlId) {
        List<String> info = new ArrayList<String>();

        // Mirror the control information from WailaEngineProvider
        switch (controlId) {
            case 0:
                info.add("Control: Owner display");
                info.add("");
                info.add("- Shows who the owner of this TARDIS is");
                break;
            case 1:
                info.add("Control: Hull gauge");
                info.add("");
                info.add("- Shows the level of the hull");
                break;
            case 2:
                info.add("Control: Shields gauge");
                info.add("");
                info.add("- Shows the level of the shields");
                break;
            case 3:
                info.add("Control: Currently selected player");
                info.add("");
                info.add("- The player whose permissions are being modified");
                break;
            case 4:
                info.add("Control: Next player");
                info.add("");
                info.add("- Select the next online player");
                break;
            case 5:
                info.add("Control: Previous player");
                info.add("");
                info.add("- Select the previous online player");
                break;
            case 10:
                info.add("Control: Upgrade energy level");
                info.add("");
                info.add("- Upgrades the Maximum Artron Energy of the TARDIS");
                break;
            case 11:
                info.add("Control: Upgrade energy regeneration level");
                info.add("");
                info.add("- Upgrades the rate at which Artron Energy is generated");
                break;
            case 12:
                info.add("Control: Upgrade shields level");
                info.add("");
                info.add("- Increases the maximum Shield points");
                break;
            case 13:
                info.add("Control: Upgrade max rooms level");
                info.add("");
                info.add("- Increases the maximum number of rooms");
                break;
            case 20:
                info.add("Control: Energy level gauge");
                info.add("");
                info.add("- Shows the current Energy upgrade level");
                break;
            case 21:
                info.add("Control: Energy regen level gauge");
                info.add("");
                info.add("- Shows the current Energy Regen upgrade level");
                break;
            case 22:
                info.add("Control: Shields level gauge");
                info.add("");
                info.add("- Shows the current Shields upgrade level");
                break;
            case 23:
                info.add("Control: Max rooms level gauge");
                info.add("");
                info.add("- Shows the current Max Rooms upgrade level");
                break;
            case 30:
                info.add("Control: Unspent points gauge");
                info.add("");
                info.add("- Shows how many upgrade points are left to spend");
                break;
            case 39:
                info.add("Control: Screwdriver slot");
                break;
            case 60:
                info.add("Control: Landing Pad lockdown");
                break;
            case 70:
                info.add("Control: Console Room setting");
                break;
            case 71:
                info.add("Control: Prev Console Room");
                break;
            case 72:
                info.add("Control: Next Console Room");
                break;
            case 73:
                info.add("Control: Switch Console Room");
                info.add("");
                info.add("- Right click then sneak right click to change console room");
                break;
            case 100:
                info.add("Control: Engine Panel Release");
                info.add("");
                info.add("- Opens or closes the engine panel");
                break;
            case 110:
                info.add("Control: Master Damage Repair Unit");
                break;
            case 130:
                info.add("Control: Spawn Protection Lever");
                info.add("");
                info.add("- Allows you to vary the radius of spawn prevention");
                break;
            case 131:
                info.add("Control: Screwdriver Style Button");
                info.add("");
                info.add("- Allows you to change your Screwdriver's style");
                break;
            case 132:
                info.add("Control: Interior Temporal Style");
                info.add("");
                info.add("- Allows you to switch the sky between overworld and space");
                break;
            default:
                // Handle ranges that don't fit in simple case statements
                if (controlId >= 40 && controlId <= 49) {
                    info.add("Control: Screwdriver permissions buttons");
                    info.add("");
                    info.add("- Allows you to toggle permissions on a screwdriver");
                } else if (controlId >= 50 && controlId <= 59) {
                    info.add("Control: Screwdriver permissions lights");
                    info.add("");
                    info.add("- Displays which permissions a screwdriver has");
                } else if (controlId >= 80 && controlId <= 89) {
                    info.add("Control: Toggle permission");
                    info.add("");
                    info.add("- Toggle a specific permission");
                } else if (controlId >= 90 && controlId <= 99) {
                    info.add("Control: Permission light");
                    info.add("");
                    info.add("- Shows if a permission is enabled");
                } else if (controlId >= 101 && controlId <= 108) {
                    info.add("Control: Upgrade Slot");
                    info.add("");
                    info.add("- Allows you to insert upgrades");
                } else if (controlId >= 111 && controlId <= 119) {
                    info.add("Control: Damage Repair Unit");
                    info.add("");
                    info.add("- Repairs specific TARDIS systems");
                } else {
                    info.add("Control: Engine Component");
                    info.add("");
                    info.add("- Controls TARDIS engine functions");
                }
                break;
        }

        return info;
    }

}
