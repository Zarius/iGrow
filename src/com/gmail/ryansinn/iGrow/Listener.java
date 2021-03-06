package com.gmail.ryansinn.iGrow;

import java.io.PrintStream;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.bukkit.DyeColor;
import org.bukkit.TreeSpecies;

public class Listener
{
  public static void onEvent(iGrow plugin)
  {
    for (Player p2 : plugin.getServer().getOnlinePlayers()) {
      Pattern pattern = Pattern.compile("\\s*"+p2.getWorld().getName()+"\\s*"); // compile our regex check for matching world here so only once per player
      plugin.sMdebug("MineCraft Time: " + p2.getWorld().getTime() + " | " + p2.getName() + "@" + p2.getWorld().getName());

      // As we are checking blocks in this loop, ensure we don't go out of Y axis bounds
      Integer minY = (int)p2.getLocation().getY() - plugin.AREA_;
      Integer maxY = (int)p2.getLocation().getY() + plugin.AREA_;
	  if (maxY > 128) maxY = 128;
	  if (minY < 0) minY = 0;

	  // Loop through all blocks in the given area
	  for (int x = (int)p2.getLocation().getX() - plugin.AREA_; x <= p2.getLocation().getX() + plugin.AREA_; x++)
		  for (int y = minY; y <= maxY; y++)
			  for (int z = (int)p2.getLocation().getZ() - plugin.AREA_; z <= p2.getLocation().getZ() + plugin.AREA_; z++)
			  {
				  // Check the lightlevel, if zero check all sides in case we got a non-exposed face 
				  /*int lightLevel = p2.getWorld().getBlockAt(x, y, z).getFace(BlockFace.DOWN).getLightLevel();
				  if (lightLevel == 0) {
					  lightLevel = p2.getWorld().getBlockAt(x, y, z).getFace(BlockFace.UP).getLightLevel();
				  }
				  if (lightLevel == 0) {
					  lightLevel = p2.getWorld().getBlockAt(x, y, z).getFace(BlockFace.EAST).getLightLevel();
				  }
				  if (lightLevel == 0) {
					  lightLevel = p2.getWorld().getBlockAt(x, y, z).getFace(BlockFace.WEST).getLightLevel();
				  }
				  if (lightLevel == 0) {
					  lightLevel = p2.getWorld().getBlockAt(x, y, z).getFace(BlockFace.NORTH).getLightLevel();
				  }
				  if (lightLevel == 0) {
					  lightLevel = p2.getWorld().getBlockAt(x, y, z).getFace(BlockFace.SOUTH).getLightLevel();
				  }*/				  
				  
				  String biome = p2.getWorld().getBlockAt(x, y, z).getBiome().name();
				  Byte thisBlockData = p2.getWorld().getBlockAt(x, y, z).getData();
				  Material thisBlockType = p2.getWorld().getBlockAt(x, y, z).getType();
				  
				  for (Recipe r : plugin.Recipes) {
					  Matcher matcher = pattern.matcher(r.world);
					  if (r.world == "" || matcher.matches()) {

						  // Check for ylevel
						  if (r.yLevel != "") {
							  if (r.yLevel.substring(0,1).equals("<")) {
								  if (y >= Integer.parseInt(r.yLevel.substring(1))) {
									  continue;
								  }
							  } else if (r.yLevel.substring(0,1).equals(">")) {
								  if (y <= Integer.parseInt(r.yLevel.substring(1))) {
									  continue;
								  }
							  } else if (r.yLevel.contains("-")) { // range
								  String[] range = r.yLevel.split("-");
								  if (y < Integer.parseInt(range[0]) || y > Integer.parseInt(range[1])) continue;
							  } else {
								  if (y != Integer.parseInt(r.yLevel)) {
									  continue;
								  }
							  }
						  }
						  // Check for lightlevel
						 /* if (r.lightLevel != "") {
							  if (r.lightLevel.substring(0,1).matches("<")) {
								  if (lightLevel >= Integer.parseInt(r.lightLevel.substring(1))) {
									  continue;
								  }
							  } else if (r.lightLevel.substring(0,1).matches(">")) {
								  if (lightLevel <= Integer.parseInt(r.lightLevel.substring(1))) {
									  continue;
								  }
							  } else {
								  if (lightLevel != Integer.parseInt(r.lightLevel)) {
									  continue;
								  }
							  }
						  }*/
						  //plugin.sMdebug("passed lightlevel, required:"+r.lightLevel+", lightlevel:"+lightLevel);
						  //plugin.sMdebug("passed lightlevel");
						  // Check for biome
						  if (r.biome != "") {
							  if (!biome.equals(r.biome))
							  {
								  //plugin.sMdebug("Biomecheck failed, biome: "+biome+" required:"+r.biome);
								  continue;
							  }            	  
						  }
						  //plugin.sMdebug("passed biome");

						  // If data exists for the needed block, check for a match - skip if no match.
						  if (r.needBlockData != "") {
							  //plugin.sMdebug("Checking data. - "+r.needBlock+"@"+r.needBlockData);
							  if (r.needBlock.matches("LEAVES")) {
								  //plugin.sMdebug("Leaves data found.");
								  if (thisBlockData != TreeSpecies.valueOf(r.needBlockData).getData()) continue;
							  }
							  if (r.needBlock.matches("LOG")) {
								  //plugin.sMdebug("Leaves data found.");
								  if (thisBlockData != TreeSpecies.valueOf(r.needBlockData).getData()) continue;
							  }
							  if (r.needBlock.matches("WOOL")) {
								  //plugin.sMdebug("Wool data found.");
								  if (thisBlockData != DyeColor.valueOf(r.needBlockData).getData()) continue;
							  }
						  }
						  if (thisBlockType == Material.valueOf(r.needBlock)) {
							  plugin.sMdebug("Recipe matched - found: " + r.needBlock + "@"+r.needBlockData+", " + r.oldBlock + "@" + r.oldBlockData + "->" + r.newBlock + "@"+r.newBlockData+":" + r.world);
							  ChangeBlocks(p2.getWorld(), plugin, p2.getWorld().getBlockAt(x, y, z), thisBlockType, r);
						  }
						  if ((!r.Near) || 
								  (thisBlockType != Material.valueOf(r.newBlock))) continue;

						  ChangeBlocks(p2.getWorld(), plugin, p2.getWorld().getBlockAt(x, y, z), thisBlockType, r);
					  }
				  }}
    }
  }

  public static void ChangeBlocks(World world, iGrow plugin, Block neededBlock, Material neededBlockType, Recipe r)
  {
	  String connections = getConnected(world, neededBlock.getLocation(), Material.valueOf(r.oldBlock), r.oldBlockData);

	  if ((neededBlockType == Material.valueOf(r.needBlock)) && connections != "") {
		  int random = new Random().nextInt(r.Chance[1]) + 1;
		  if (random <= r.Chance[0]) {
			  ChangeBlock(neededBlock, connections, r);
		  }
	  }
  }

  public static String getConnected(World world, Location here, Material blockMat, String data)
  {
	  String value = "";
	  if (CheckBlock(world.getBlockAt((int)here.getX() - 1, (int)here.getY(), (int)here.getZ()), blockMat, data)) {
		  value = value + "1, ";
	  }
	  if (CheckBlock(world.getBlockAt((int)here.getX() + 1, (int)here.getY(), (int)here.getZ()), blockMat, data)) {
		  value = value + "2, ";
	  }
	  if (CheckBlock(world.getBlockAt((int)here.getX(), (int)here.getY() - 1, (int)here.getZ()), blockMat, data)) {
		  value = value + "3, ";
	  }
	  if (CheckBlock(world.getBlockAt((int)here.getX(), (int)here.getY() + 1, (int)here.getZ()), blockMat, data)) {
		  value = value + "4, ";
	  }
	  if (CheckBlock(world.getBlockAt((int)here.getX(), (int)here.getY(), (int)here.getZ() - 1), blockMat, data)) {
		  value = value + "5, ";
	  }
	  if (CheckBlock(world.getBlockAt((int)here.getX(), (int)here.getY(), (int)here.getZ() + 1), blockMat, data)) {
		  value = value + "6, ";
	  }
	  return value;
  }

  public static boolean CheckBlock(Block checkBlock, Material originalBlockMat, String originalBlockData)
  {
	  //System.out.println("checkblock starts, blocktype: "+checkBlock.getType().name()+"@" + DyeColor.getByData(checkBlock.getData()).name()+ ", originalblockmat: "+originalBlockMat.name()+"@"+originalBlockData);
	  if (checkBlock.getType() == originalBlockMat)
	  {
		  //System.out.println("checking data");
		  if (originalBlockData != "")
		  {
			  if (originalBlockMat.name().matches("LEAVES")) {
				  //System.out.println("checking leaves data");
				  if (TreeSpecies.getByData(checkBlock.getData()).name().matches(originalBlockData)) {
					  return true;
				  } else {
					  return false;
				  }
			  } else if (originalBlockMat.name().matches("WOOL")) {
				  //System.out.println("checking wool data");
				  if (DyeColor.getByData(checkBlock.getData()).name().matches(originalBlockData)) {
					  //System.out.println("checking wool data -true");
					  return true;
				  } else {
					  return false;
				  }
			  } else if (originalBlockMat.name().matches("LOG")) {
				  //System.out.println("checking wool data");
				  if (TreeSpecies.getByData(checkBlock.getData()).name().matches(originalBlockData)) {
					  //System.out.println("checking wool data -true");
					  return true;
				  } else {
					  return false;
				  }
			  } else {
				  return false;
			  }
		  } else {
			  return true;
		  }

	  }
	  return false;
  }

  public static void ChangeBlock(Block b, String value, Recipe r) //, Material from, Material id)
  {
	  //System.out.println("Changing block: value="+value);
	  Material from = Material.valueOf(r.oldBlock);

	  String[] values = { "", "", "", "", "", "" };
	  values = value.split(", ");

	  // Make sure we don't go out of the worlds Y bounds (0 < Y < 128)
	  if (b.getWorld().getBlockAt((int)b.getLocation().getX(), (int)b.getLocation().getY() + 1, (int)b.getLocation().getZ()).getY() > 128)
		  return;
	  if (b.getWorld().getBlockAt((int)b.getLocation().getX(), (int)b.getLocation().getY() - 1, (int)b.getLocation().getZ()).getY() < 0) {
		  return;
	  }

	  //   if (values[0].equals("1")) {
	  //     Block block = b.getWorld().getBlockAt((int)b.getLocation().getX() - 1, (int)b.getLocation().getY(), (int)b.getLocation().getZ());
	  //        if (block.getType().equals(Material.getMaterial(from))) block.setType(Material.getMaterial(id));
	  //  }

	  if (values[0].equals("1")) {
		  Block block = b.getWorld().getBlockAt((int)b.getLocation().getX() - 1, (int)b.getLocation().getY(), (int)b.getLocation().getZ());
		  if (block.getType().equals(from)) SetBlock(block, r);
	  }
	  if (values[0].equals("2")) {
		  Block block = b.getWorld().getBlockAt((int)b.getLocation().getX() + 1, (int)b.getLocation().getY(), (int)b.getLocation().getZ());
		  if (block.getType().equals(from)) SetBlock(block, r);
	  }
	  if (values[0].equals("3")) {
		  Block block = b.getWorld().getBlockAt((int)b.getLocation().getX(), (int)b.getLocation().getY() - 1, (int)b.getLocation().getZ());
		  if (block.getType().equals(from)) SetBlock(block, r);
	  }
	  if (values[0].equals("4")) {
		  Block block = b.getWorld().getBlockAt((int)b.getLocation().getX(), (int)b.getLocation().getY() + 1, (int)b.getLocation().getZ());
		  //System.out.println("Changing block: values[0]="+values[0]+" block: "+block.getType().name()+"from="+from.name());
		  if (block.getType().equals(from)) SetBlock(block, r);
	  }
	  if (values[0].equals("5")) {
		  Block block = b.getWorld().getBlockAt((int)b.getLocation().getX(), (int)b.getLocation().getY(), (int)b.getLocation().getZ() - 1);
		  if (block.getType().equals(from)) SetBlock(block, r);
	  }
	  if (values[0].equals("6")) {
		  Block block = b.getWorld().getBlockAt((int)b.getLocation().getX(), (int)b.getLocation().getY(), (int)b.getLocation().getZ() + 1);
		  if (block.getType().equals(from)) SetBlock(block, r);
	  }
  }


  public static void SetBlock(Block b, Recipe r) // b = oldBlock, r is for newBlock and newBlockData
  {
	  b.setType(Material.valueOf(r.newBlock));
	  //System.out.println("setting value:" +b.getType().name()+"@"+DyeColor.getByData(b.getData()).name()+ " -> "+r.newBlock+"@"+r.newBlockData);
	  if (r.newBlockData != "")
	  {
		  if (r.newBlock.matches("LEAVES")) {
			  //System.out.println("setting leaves data");
			  b.setData(TreeSpecies.valueOf(r.newBlockData).getData());
		  }
		  if (r.newBlock.matches("LOG")) {
			  //System.out.println("setting logs data");
			  b.setData(TreeSpecies.valueOf(r.newBlockData).getData());
		  }
		  if (r.newBlock.matches("WOOL")) {
			  //System.out.println("setting wool data");
			  b.setData(DyeColor.valueOf(r.newBlockData).getData());
		  }
	  }

  }
  }

/* Location:           C:\Users\Robin\Downloads\iGrow.jar
 * Qualified Name:     com.bukkit.techguard.igrow.Listener
 * JD-Core Version:    0.6.0
 */