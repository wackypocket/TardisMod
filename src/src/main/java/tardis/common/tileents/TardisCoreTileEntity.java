package tardis.common.tileents;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import tardis.TardisMod;
import tardis.api.IActivatable;
import tardis.api.TardisFunction;
import tardis.common.core.Helper;
import tardis.common.core.TardisConfigFile;
import tardis.common.core.TardisOutput;
import tardis.common.core.store.SimpleCoordStore;
import tardis.common.items.TardisKeyItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.world.World;

public class TardisCoreTileEntity extends TardisAbstractTileEntity implements IActivatable
{
	private static TardisConfigFile config;
	private int exteriorWorld;
	private int exteriorX;
	private int exteriorY;
	private int exteriorZ;
	private float lastProximity = 0;
	
	private int tickCount = 0;
	
	private static int energyPerSecond = 0;
	
	private double speed = 4;
	private static double maxSpeed;
	
	private int energy;
	private static int maxEnergy;
	
	private boolean inFlight = false;
	private int flightTimer = 0;
	private int inFlightTimer = 0;
	private int totalFlightTimer = 0;
	private int buttonTime = 80;
	
	private int timeTillTakenOff;
	private int timeTillLanding;
	private int timeTillLandingInt;
	private int timeTillLanded;
	private int numButtons;
	
	private int numRooms = 0;
	private int maxNumRooms = 30;
	private double	tardisXP = 0;
	private int		tardisLevel = 0;
	
	private int shields;
	private int maxShields;
	private int hull;
	private int maxHull;
	
	private boolean deletingRooms = false;
	private static double explodeChance = 0.25;
	private boolean explode = false;
	
	private int instability = 0;
	private int desDim = 0;
	private int desX = 0;
	private int desY = 0;
	private int desZ = 0;
	private String[] desStrs = null;
	
	private enum LockState{Open,OwnerOnly,KeyOnly,Locked};
	private LockState lockState = LockState.Open; 
	
	private HashSet<SimpleCoordStore> roomSet = new HashSet<SimpleCoordStore>();
	
	private String ownerName;
	
	static
	{
		config = TardisMod.configHandler.getConfigFile("tardisCore");
		explodeChance = config.getDouble("Explosion chance (on control not pressed)", 0.25);
	}
	
	{
		maxSpeed = config.getDouble("maxSpeed", 8);
		maxEnergy = config.getInt("maxEnergy", 1000);
		maxNumRooms = config.getInt("maxRooms", 30);
		maxShields  = config.getInt("maxShields", 1000);
		maxHull		= config.getInt("maxHull", 1000);
		shields		= maxShields;
		hull		= maxHull;
		
		energy = 100;
		energyPerSecond = config.getInt("energy per second",1);
	}
	
	private void flightTick()
	{
		if(inFlightTimer == 0)
			worldObj.playSound(xCoord, yCoord, zCoord, "tardismod:takeoff", 0.75F, 1, true);
		totalFlightTimer++;
		
		if(inCoordinatedFlight())
			inFlightTimer++;
		if(inFlightTimer >= timeTillTakenOff)//Taken off
		{
			TardisConsoleTileEntity con = getConsole();
			int takenTime = inFlightTimer - timeTillTakenOff;
			int takenTimeTwo = flightTimer - timeTillTakenOff;
			if(!worldObj.isRemote && !con.isStable() && takenTimeTwo % buttonTime == 0 && (getButtonTime() * numButtons) >= takenTime)
			{
				if(takenTimeTwo > 0)
				{
					TardisOutput.print("TCTE", "Working out what to do!");
					if(con.unstableControlPressed())
					{
						instability -= 0.5;
						addXP(1);
					}
					else
					{
						instability++;
						if(shouldExplode())
							explode = true;
						worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
					}
					instability = Helper.clamp(instability, 0, 10);
				}
				if(con != null && con.unstableFlight() && (buttonTime * numButtons) >= takenTime)
					con.randomUnstableControl();
				else if((buttonTime * numButtons) == takenTime)
					con.clearUnstableControl();
			}
			if(takenTime == 0)// remove old tardis
			{
				World w = Helper.getWorld(exteriorWorld);
				if(w != null)
				{
					if(w.getBlockId(exteriorX,exteriorY,exteriorZ) == TardisMod.tardisBlock.blockID)
					{
						w.setBlockToAir(exteriorX, exteriorY, exteriorZ);
						w.setBlockToAir(exteriorX, exteriorY+1, exteriorZ);
						TardisOutput.print("TCTE", "Blanking exterior");
						exteriorWorld = 10000;
						exteriorX = 0;
						exteriorY = 0;
						exteriorZ = 0;
					} 	
				}
			}
			if(inFlightTimer < timeTillLanding)
			{
				if(flightTimer % 69 == 0 && inFlight)
					worldObj.playSound(xCoord, yCoord, zCoord, "tardismod:engines", 0.75F, 1, true);
				flightTimer++;
			}
			else
			{
				if(flightTimer % 69 == 0 && inFlightTimer < timeTillLandingInt)
					worldObj.playSound(xCoord, yCoord, zCoord, "tardismod:engines", 0.75F, 1, true);
				flightTimer++;
				
				if(inFlightTimer == timeTillLanding)
					placeBox();
				
				if(inFlightTimer == timeTillLandingInt)
					worldObj.playSound(xCoord, yCoord, zCoord, "tardismod:landingInt", 0.75F, 1, true);
				
				if(inFlightTimer >= timeTillLanded)
					land();
			}
		}
	}

	private void safetyTick()
	{
		List<Object> players = worldObj.playerEntities;
		for(Object o : players)
		{
			if(o instanceof EntityPlayer)
			{
				EntityPlayer pl = (EntityPlayer)o;
				if(pl.posY < -5 && !pl.capabilities.isFlying)
					Helper.teleportEntityToSafety(pl);
			}
		}
	}

	@Override
	public void updateEntity()
	{
		tickCount++;
		
		if(explode)
		{
			double xO = (rand.nextDouble()*3) - 1.5;
			double zO = (rand.nextDouble()*3) - 1.5;
			worldObj.playSound(xCoord, yCoord, zCoord, "random.explosion", 0.5F, 1, true);
			worldObj.createExplosion(null, xCoord+0.5+xO, yCoord-0.5, zCoord+0.5+zO, 1F, true);
			explode = false;
		}
		
		if(tickCount % 20 == 0)
		{
			addEnergy(energyPerSecond,false);
			safetyTick();
		}
		
		if(inFlight)
			flightTick();
		
		if(!worldObj.isRemote)
		{
			if(tickCount % 100 == 0 && TardisMod.plReg != null)
			{
				if(!TardisMod.plReg.hasTardis(ownerName))
					TardisMod.plReg.addPlayer(ownerName, worldObj.provider.dimensionId);
			}
			
			if(deletingRooms)
			{
				Iterator<SimpleCoordStore> i = roomSet.iterator();
				if(i.hasNext())
				{
					SimpleCoordStore coord = i.next();
					TileEntity te = worldObj.getBlockTileEntity(coord.x, coord.y, coord.z);
					if(te != null && te instanceof TardisSchemaCoreTileEntity)
					{
						TardisOutput.print("TCTE", "Removing room @ " + coord);
						TardisSchemaCoreTileEntity schemaCore = (TardisSchemaCoreTileEntity)te;
						schemaCore.remove();
					}
					i.remove();
				}
				else
				{
					deletingRooms = false;
					numRooms = 0;
				}
			}
			if(tickCount % 80 == 0)
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}

	public boolean activate(EntityPlayer player, int side)
	{
		/*if(ownerName != null)
			leaveTardis(player,false);*/
		return true;
	}
	
	public boolean hasValidExterior()
	{
		World w = Helper.getWorld(exteriorWorld);
		if(w != null)
		{
			if(w.getBlockId(exteriorX, exteriorY, exteriorZ) == TardisMod.tardisBlock.blockID)
				return true;
		}
		return false;
	}
	
	public void linkToExterior(TardisTileEntity exterior)
	{
		exteriorWorld = exterior.worldObj.provider.dimensionId;
		exteriorX = exterior.xCoord;
		exteriorY = exterior.yCoord;
		exteriorZ = exterior.zCoord;
	}
	
	public boolean hasKey(EntityPlayer player,boolean inHand)
	{
		if(inHand)
		{
			ItemStack held = player.getHeldItem();
			String on = TardisKeyItem.getOwnerName(held);
			if(on != null)
				TardisOutput.print("TCTE","Key owner = " + on);
			else
				TardisOutput.print("TCTE","Key owner = null");
			if(on != null && on.equals(ownerName))
				return true;
		}
		else
		{
			InventoryPlayer inv = player.inventory;
			if(inv == null)
				return false;
			for(ItemStack is: inv.mainInventory)
			{
				String on = TardisKeyItem.getOwnerName(is);
				if(on != null && on.equals(ownerName))
					return true;
			}
		}
		return false;
	}
	
	public boolean canOpenLock(EntityPlayer player, boolean isInside)
	{
		if(isInside && !lockState.equals(LockState.Locked))
			return true;
		else if(isInside)
			return false;
		
		if(player == null)
			return false;
		
		if(lockState.equals(LockState.Open))
			return true;
		else if(lockState.equals(LockState.Locked))
			return false;
		else if(lockState.equals(LockState.OwnerOnly))
			return player.username.equals(ownerName);
		else if(lockState.equals(LockState.KeyOnly))
			return hasKey(player,false);
		return false;
	}
	
	public void enterTardis(EntityPlayer player, boolean ignoreLock)
	{
		if(player.worldObj.isRemote)
			return;
		if(ignoreLock || canOpenLock(player,false))
			Helper.teleportEntity(player, worldObj.provider.dimensionId, 13.5, 28.5, 0, 90);
		else
			player.addChatMessage("[TARDIS]The door is locked");
	}
	
	public void leaveTardis(EntityPlayer player, boolean ignoreLock)
	{
		if(!inFlight)
		{
			if(ignoreLock || canOpenLock(player,true))
			{
				World ext = Helper.getWorld(exteriorWorld);
				if(ext != null)
				{
					if(ext.isRemote)
						return;
					int facing = ext.getBlockMetadata(exteriorX, exteriorY, exteriorZ);
					int dx = 0;
					int dz = 0;
					double rot = 0;
					switch(facing)
					{
						case 0:dz = -1;rot=180; break;
						case 1:dx =  1;rot=-90; break;
						case 2:dz =  1;rot=  0; break;
						case 3:dx = -1;rot= 90; break;
					}
					
					if(ext.isAirBlock(exteriorX+dx, exteriorY, exteriorZ+dz) && ext.isAirBlock(exteriorX+dx, exteriorY, exteriorZ+dz))
					{
						Helper.teleportEntity(player, exteriorWorld, exteriorX+0.5+(dx*1.3), exteriorY+1, exteriorZ+0.5+(dz*1.3),rot);
					}
					else
						player.addChatMessage("[TARDIS]The door is obstructed");
				}
				else
					player.addChatMessage("[TARDIS]The door refuses to open");
			}
			else
				player.addChatMessage("[TARDIS]The door is locked");
		}
		else
			player.addChatMessage("[TARDIS]The door won't open in flight");
	}
	
	public boolean changeLock(EntityPlayer pl,boolean inside)
	{
		if(pl.worldObj.isRemote)
			return false;
		
		TardisOutput.print("TCTE", "Changing lock");
		if(!hasKey(pl,true))
			return false;
		
		if(lockState.equals(LockState.Locked) && !inside)
			return false;
		
		int num = LockState.values().length;
		lockState = LockState.values()[((lockState.ordinal() + 1)%num)];
		if((!inside) && lockState.equals(LockState.Locked))
		{
			TardisOutput.print("TCTE", "Changing from locked because outside");
			lockState = LockState.values()[((lockState.ordinal() + 1)%num)];
		}
			
		TardisOutput.print("TTE", "Lockstate:"+lockState.toString());
		if(lockState.equals(LockState.KeyOnly))
			pl.addChatMessage("[TARDIS]The door will only open with the key");
		else if(lockState.equals(LockState.Locked))
			pl.addChatMessage("[TARDIS]The door will not open");
		else if(lockState.equals(LockState.Open))
			pl.addChatMessage("[TARDIS]The door will open for all");
		else if(lockState.equals(LockState.OwnerOnly))
			pl.addChatMessage("[TARDIS]The door will only open for its owner");
		return true;
	}
	
	public boolean takeOff(EntityPlayer pl)
	{
		if(!inFlight)
		{
			TardisConsoleTileEntity con = getConsole();
			int dDim = con.getDimFromControls();
			int dX = con.getXFromControls(exteriorX);
			int dY = con.getYFromControls(exteriorY);
			int dZ = con.getZFromControls(exteriorZ);
			
			int distance = Math.abs(dX - exteriorX) + Math.abs(dY - exteriorY) + Math.abs(dZ - exteriorZ) + (dDim != exteriorWorld ? 300 : 0);
			int enCost = (int) Helper.clamp(distance, 1, 500);
			if(takeEnergy(enCost,false))
			{
				instability = 0;
				inFlight = true;
				inFlightTimer = 0;
				flightTimer = 0;
				TardisTileEntity te = getExterior();
				
				timeTillTakenOff = (20 * 11);
				timeTillLanding = timeTillTakenOff +  (int) (((2+(2*getMaxSpeed())) - (2*getSpeed(true))) * 69);
				timeTillLandingInt = timeTillLanding + (20 * 6);
				timeTillLanded  = timeTillLanding + (20 * 11);
				numButtons = (timeTillLanding - timeTillTakenOff) / getButtonTime();
				
				if(te != null)
					te.takeoff();
				sendUpdate();
				return true;
			}
			else
				pl.addChatMessage("Not enough energy");
		}
		return false;
	}
	
	private boolean isValidPos(World w, int x, int y, int z)
	{
		return w.isAirBlock(x, y, z) && w.isAirBlock(x,y+1,z) && y > 0 && y < 254;
	}
	
	private void placeBox()
	{
		if(worldObj.isRemote)
			return;
		
		TardisConsoleTileEntity con = getConsole();
		int dX = con.getXFromControls(exteriorX);
		int dY = con.getYFromControls(exteriorY);
		int dZ = con.getZFromControls(exteriorZ);
		int facing = con.getFacingFromControls();
		World w = Helper.getWorld(con.getDimFromControls());
		if(!(isValidPos(w,dX,dY,dZ)))
		{
			boolean f = false;
			int[] check = {0,1,-1,2,-2};
			for(int i=0;i<5&&!f;i++)
			{
				int xO = check[i];
				for(int j=0;j<5&&!f;j++)
				{
					int zO = check[j];
					for(int k=0;k<5&&!f;k++)
					{
						int yO = check[k];
						if(isValidPos(w,dX+xO,dY+yO,dZ+zO))
						{
							dX += xO;
							dY += yO;
							dZ += zO;
							f = true;
						}
					}
				}
			}
		}
		boolean landOnGround = con.getLandFromControls();
		if(landOnGround)
		{
			int offset = 1;
			while(dY - offset > 0 && w.isAirBlock(dX, dY-offset, dZ))
				offset++;
			dY = dY + 1 - offset;
		}
		w.setBlock(dX, dY, dZ, TardisMod.tardisBlock.blockID, facing, 3);
		w.setBlock(dX, dY+1, dZ, TardisMod.tardisTopBlock.blockID, facing, 3);
		
		setExterior(w,dX,dY,dZ);
		TileEntity te = w.getBlockTileEntity(dX,dY,dZ);
		if(te != null && te instanceof TardisTileEntity)
		{
			TardisTileEntity tardis = (TardisTileEntity) te;
			tardis.linkedCore = this;
			tardis.linkedDimension = worldObj.provider.dimensionId;
			tardis.land();
		}
	}
	
	public void land()
	{
		if(inFlight)
		{
			addXP(30);
			inFlight = false;
			sendUpdate();
			worldObj.playSound(xCoord, yCoord, zCoord, "tardismod:engineDrum", 0.75F, 1, true);
			TardisTileEntity ext = getExterior();
			if(ext != null)
				ext.forceLand();
			TardisConsoleTileEntity con = getConsole();
			if(con != null)
				con.land();
		}
	}
	
	private int getButtonTime()
	{
		double mod = 1;
		if(getSpeed(false) != 0)
			mod = getMaxSpeed() / ((getSpeed(false)) * 2);
		else
			mod = 0;
		mod = Helper.clamp(mod, 0.5, 4);
		int buttonTimeMod = Helper.clamp((int)Math.round(buttonTime * mod),30,buttonTime*4);
		return buttonTimeMod;
	}
	
	private boolean shouldExplode()
	{
		double eC = explodeChance * ((getSpeed(false) + 1) / 3.0);
		eC *= Helper.clamp(3.0 / tardisLevel, 0.2, 1);
		return rand.nextDouble() < eC;
	}
	
	////////////////////////////////////////////////
	//////////////DATA STUFF////////////////////////
	////////////////////////////////////////////////
	
	public boolean inCoordinatedFlight()
	{
		if(inFlight)
		{
			TardisConsoleTileEntity con = getConsole();
			if(con == null)
				return true;
			return (inFlightTimer <= timeTillTakenOff) || con.shouldLand();
		}
		return false;
	}
	
	public boolean inFlight()
	{
		return inFlight;
	}
	
	public float getProximity()
	{
		if(inFlight)
		{
			int rate = 40;
			double val = Math.abs((tickCount % rate) - (rate / 2));
			double max = 0.4;
			lastProximity = (float) (max * 2 * (val / rate));
			return lastProximity;
		}
		else if(lastProximity > 0)
			return (lastProximity = lastProximity - (1/20.0F));
		else
		{
			return 0;
		}
	}
	
	public void setExterior(World w, int x, int y, int z)
	{
		exteriorWorld = w.provider.dimensionId;
		exteriorX = x;
		exteriorY = y;
		exteriorZ = z;
		TardisOutput.print("TCTE", "Exterior placed @ " + x + ","+ y +","+z+","+exteriorWorld +","+worldObj.isRemote);
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
	
	public TardisTileEntity getExterior()
	{
		World w = Helper.getWorld(exteriorWorld);
		if(w != null)
		{
			TileEntity te = w.getBlockTileEntity(exteriorX,exteriorY,exteriorZ);
			if(te != null && te instanceof TardisTileEntity)
				return (TardisTileEntity) te;
		}
		return null;
	}
	
	public boolean canModify(EntityPlayer player)
	{
		TardisOutput.print("TCTE","OwnerCheck:"+player.username+":"+ ownerName+"#"+worldObj.isRemote,TardisOutput.Priority.DEBUG);
		return player.username.equals(ownerName);
	}
	
	public String getOwner()
	{
		return ownerName;
	}
	
	public void setOwner(String name)
	{
		TardisOutput.print("TCTE", "Setting owner to " + name+"#"+worldObj.isRemote, TardisOutput.Priority.DEBUG);
		ownerName = name;
		if(!worldObj.isRemote && TardisMod.plReg != null && !TardisMod.plReg.hasTardis(ownerName))
			TardisMod.plReg.addPlayer(ownerName, worldObj.provider.dimensionId);
	}

	public TardisConsoleTileEntity getConsole()
	{
		TileEntity te = worldObj.getBlockTileEntity(xCoord, yCoord - 2, zCoord);
		if(te != null)
		{
			if(te instanceof TardisConsoleTileEntity)
				return (TardisConsoleTileEntity)te;
		}
		return null;
	}
	
	public boolean hasFunction(TardisFunction fun)
	{
		switch(fun)
		{
		case LOCATE:	return tardisLevel > 3;
		case SENSORS:	return tardisLevel > 5;
		case STABILISE:	return tardisLevel > 7;
		default:		return false;
		}
	}
	
	public double getMaxSpeed()
	{
		return maxSpeed;
	}
	
	public double getSpeed(boolean modified)
	{
		if(!modified)
			return speed;
		double mod = ((double)getNumRooms()) / getMaxNumRooms();
		return speed * (1-(mod / 2.0));
	}
	
	public double addSpeed(double a)
	{
		if(!inFlight)
			speed = speed + a;
		speed = Helper.clamp(speed, 0, getMaxSpeed());
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		return speed;
	}
	
	public int getLevel()
	{
		return tardisLevel;
	}
	
	public double getXP()
	{
		return tardisXP;
	}
	
	public double getXPNeeded()
	{
		return 100 * Math.max(1, tardisLevel);
	}
	
	public double addXP(double a)
	{
		tardisXP += Math.abs(a);
		if(tardisXP >= getXPNeeded())
		{
			tardisXP -= getXPNeeded();
			tardisLevel++;
			Helper.playSound(worldObj, xCoord, yCoord, zCoord, "tardismod:levelup", 1);
		}
		sendUpdate();
		return tardisXP;
	}
	
	public int getNumRooms()
	{
		return numRooms;
	}
	
	public int getMaxNumRooms()
	{
		return maxNumRooms;
	}
	
	public boolean addRoom(boolean sub, TardisSchemaCoreTileEntity te)
	{
		if(sub && numRooms > 0)
		{
			if(Helper.isServer() && te != null)
				roomSet.remove(new SimpleCoordStore(te));
			numRooms --;
			return true;
		}
		
		if(!sub && numRooms < maxNumRooms)
		{
			if(Helper.isServer() && te != null)
				roomSet.add(new SimpleCoordStore(te));
			numRooms++;
			return true;
		}
		
		return false;
	}
	
	public boolean addRoom(TardisSchemaCoreTileEntity te)
	{
		if(Helper.isServer() && te != null)
			return roomSet.add(new SimpleCoordStore(te));
		return false;
	}
	
	public void removeAllRooms(boolean force)
	{
		if(!force)
			removeAllRooms();
		else
		{
			for(SimpleCoordStore coord : roomSet)
			{
				TileEntity te = worldObj.getBlockTileEntity(coord.x, coord.y, coord.z);
				if(te != null && te instanceof TardisSchemaCoreTileEntity)
				{
					TardisOutput.print("TCTE", "Removing room @ " + coord);
					TardisSchemaCoreTileEntity schemaCore = (TardisSchemaCoreTileEntity)te;
					schemaCore.remove();
				}
			}
			roomSet.clear();
			numRooms = 0;
		}
	}
	
	public void removeAllRooms()
	{
		
		deletingRooms = true;
	}
	
	public int getMaxEnergy()
	{
		return maxEnergy;
	}
	
	public int getEnergy()
	{
		return energy;
	}
	
	public boolean addEnergy(int amount, boolean sim)
	{
		if(!sim)
			energy += amount;
		energy = Helper.clamp(energy,0,getMaxEnergy());
		return true;
	}
	
	public boolean takeEnergy(int amount, boolean sim)
	{
		if(energy >= amount)
		{
			if(!sim)
				energy -= amount;
			return true;
		}
		energy = Helper.clamp(energy,0,getMaxEnergy());
		return false;
	}
	
	public int getShields()
	{
		return shields;
	}
	
	public int getMaxShields()
	{
		return maxShields;
	}
	
	public int getHull()
	{
		return hull;
	}
	
	public int getMaxHull()
	{
		return maxHull;
	}
	
	public void sendDestinationStrings(EntityPlayer pl)
	{
		TardisConsoleTileEntity console = getConsole();
		if(console != null)
		{
			int dD = console.getDimFromControls();
			int dX = console.getXFromControls(exteriorX);
			int dY = console.getYFromControls(exteriorY);
			int dZ = console.getZFromControls(exteriorZ);
			TardisOutput.print("TCTE","Dest:" + dD +","+dX+","+dY+","+dZ);
			if(dD==desDim&&dX==desX&&dY==desY&&dZ==desZ&&desStrs!=null)
				for(String s:desStrs)
					pl.addChatMessage(s);
			else
			{
				int instability = Helper.clamp(20 - (2 * tardisLevel),3,20);
				desDim = dD;
				String[] send = new String[4];
				if(desStrs!= null && desStrs.length == 4)
					send = desStrs;
				
				send[0] = "The TARDIS will materialize in dimension " + dD + " near:";
				if(dX != desX || send[1] == null)
					send[1] = "x = " + (dX + (rand.nextInt(2 * instability) - instability));
				if(dY != desY || send[2] == null)
					send[2] = "y = " + (dY + (rand.nextInt(2 * instability) - instability)); 
				if(dZ != desZ || send[3] == null)
				send[3] = "z = " + (dZ + (rand.nextInt(2 * instability) - instability)); 
				desX = dX;
				desY = dY;
				desZ = dZ;
				desStrs = send;
				for(String s:desStrs)
					pl.addChatMessage(s);
			}
		}
	}
	
	private boolean[] getObstructData(World w, int x, int y, int z)
	{
		boolean[] data = new boolean[2];
		data[0] = data[1] = false;
		TardisOutput.print("TCTE", "Checking for air @ " + x + "," + y + "," + z,TardisOutput.Priority.DEBUG);
		if(w.isAirBlock(x, y, z) && w.isAirBlock(x, y+1, z))
		{
			data[0] = true;
			if(w.isAirBlock(x, y-1, z))
				data[1] = true;
		}
		return data;
	}
	
	public void sendScannerStrings(EntityPlayer pl)
	{
		if(inFlight())
		{
			pl.sendChatToPlayer(new ChatMessageComponent().addText("Cannot use temporal scanners while in flight"));
			return;
		}
		List<String> string = new ArrayList<String>();
		TardisTileEntity ext = getExterior();
		World w = ext.worldObj;
		int dx = 0;
		int dz = 0;
		string.add("Current position: Dimension " + exteriorWorld + " : " + exteriorX+","+exteriorY+","+exteriorZ);
		int facing = w.getBlockMetadata(exteriorX, exteriorY, exteriorZ);
		for(int i=0;i<4;i++)
		{
			switch(i)
			{
				case 0:dz = -1; dx = 0; break;
				case 1:dx =  1; dz = 0; break;
				case 2:dz =  1; dx = 0; break;
				case 3:dx = -1; dz = 0; break;
			}
			String s = (i == facing ? "Current facing " : "Facing ");
			boolean[] data = getObstructData(w,exteriorX+dx,exteriorY,exteriorZ+dz);
			if(!data[0])
				s += "obstructed";
			else if(!data[1])
				s += "safe";
			else
				s += "unsafe drop";
			string.add(s);
		}
		
		for(String s:string)
			pl.addChatMessage(s);
	}
	
	//////////////////////////////
	//////NBT DATA////////////////
	//////////////////////////////
	
	public void repair(String newO, int numRoom, int en)
	{
		energy = en;
		numRooms = numRoom;
		maxNumRooms = config.getInt("maxRooms", 30);
		setOwner(newO);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		lockState = LockState.values()[nbt.getInteger("lockState")];
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		
		nbt.setInteger("lockState", lockState.ordinal());
	}

	@Override
	public void writeTransmittable(NBTTagCompound nbt)
	{
		if(ownerName != null)
		{
			nbt.setDouble("tardisXP", tardisXP);
			nbt.setBoolean("explode",explode);
			nbt.setInteger("tardisLevel", tardisLevel);
			nbt.setString("ownerName", ownerName);
			nbt.setInteger("extWorld", exteriorWorld);
			nbt.setInteger("extX", exteriorX);
			nbt.setInteger("extY", exteriorY);
			nbt.setInteger("extZ", exteriorZ);
			
			nbt.setInteger("energy",energy);
			
			nbt.setBoolean("inFlight", inFlight);
			nbt.setInteger("flightTimer", flightTimer);
			nbt.setInteger("totalFlightTimer", totalFlightTimer);
			nbt.setInteger("inFlightTimer", inFlightTimer);
			nbt.setInteger("numRooms", numRooms);
			nbt.setDouble("speed", speed);
			
			nbt.setInteger("shields",shields);
			nbt.setInteger("hull",hull);
			if(inFlight())
			{
				nbt.setInteger("ttTO", timeTillTakenOff);
				nbt.setInteger("ttL" , timeTillLanding);
				nbt.setInteger("ttLI", timeTillLandingInt);
				nbt.setInteger("ttLa", timeTillLanded);
			}
		}
	}

	@Override
	public void readTransmittable(NBTTagCompound nbt)
	{
		if(nbt.hasKey("ownerName"))
		{
			tardisXP  = nbt.getDouble("tardisXP");
			explode = nbt.getBoolean("explode");
			tardisLevel = nbt.getInteger("tardisLevel");
			ownerName = nbt.getString("ownerName");
			exteriorWorld = nbt.getInteger("extWorld");
			exteriorX = nbt.getInteger("extX");
			exteriorY = nbt.getInteger("extY");
			exteriorZ = nbt.getInteger("extZ");
			
			energy = nbt.getInteger("energy");
			
			flightTimer = nbt.getInteger("flightTimer");
			inFlightTimer = nbt.getInteger("inFlightTimer");
			totalFlightTimer = nbt.getInteger("totalFlightTimer");
			inFlight = nbt.getBoolean("inFlight");
			numRooms = nbt.getInteger("numRooms");
			speed = nbt.getDouble("speed");
			
			shields  = nbt.getInteger("shields");
			hull     = nbt.getInteger("hull");
			
			if(nbt.hasKey("ttTO"))
			{
				timeTillTakenOff	= nbt.getInteger("ttTO");
				timeTillLanding		= nbt.getInteger("ttL");
				timeTillLandingInt	= nbt.getInteger("ttLI");
				timeTillLanded		= nbt.getInteger("ttLa");
			}
		}
	}

}
