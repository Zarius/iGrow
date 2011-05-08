package com.gmail.ryansinn.iGrow;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;

public class iGrow extends JavaPlugin
{
  private String dirs = "plugins/iGrow/";
  private String file = this.dirs + "config.properties";
  private int TIMER_ = 33;
  public int AREA_ = 100;
  public boolean DEBUGMESSAGES_ = false;
  private final HashMap<Player, Boolean> debugees = new HashMap();
  public ArrayList<Recipe> Recipes = new ArrayList();
  Thread Event = new onEvent(this);

  public iGrow() {
  }
  public iGrow(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) { super(pluginLoader, noNag(instance, true), desc, folder, plugin, cLoader);
    noNag(instance, false); }

  private static Server noNag(Server instance, boolean enable) {
    try {
      Method getlogger = Server.class.getMethod("getLogger", null);
      Logger thelogger = (Logger)getlogger.invoke(instance, null);

      if (enable) thelogger.setLevel(Level.SEVERE); else
        thelogger.setLevel(null);
    } catch (NoSuchMethodException localNoSuchMethodException) {
    } catch (IllegalArgumentException localIllegalArgumentException) {
    } catch (IllegalAccessException localIllegalAccessException) {
    } catch (InvocationTargetException localInvocationTargetException) {
    }
    return instance;
  }

  public void onDisable() {
    this.Event.interrupt();
    sM("Disabled!");
  }

  public void onEnable() {
    sM("Enabled!");
    ini();
    loadRecipes();
  }

  public void ini() {
    try {
      File conf = new File(this.file);
      if (!conf.exists()) {
        File dirs = new File(this.dirs);
        File File = new File(this.file);
        dirs.mkdirs();
        File.createNewFile();

        FileWriter fstream = new FileWriter(this.file);
        BufferedWriter out = new BufferedWriter(fstream);
        out.write("#Properties file for iGrow"); out.newLine();
        out.write("#The timer-interval is in SECONDS!"); out.newLine();
        out.write("#block-area is the area around the player to check."); out.newLine();
        out.write("#debug-messages is for debugging only - this _will_ spam your server console and log."); out.newLine();
        out.write(""); out.newLine();
        out.write("timer-interval = 33"); out.newLine();
        out.write("check-block-area = 100");
        out.write("debug-messages = false");
        out.close();
      }
      FileInputStream fstream = new FileInputStream(this.file);
      DataInputStream in = new DataInputStream(fstream);
      BufferedReader br = new BufferedReader(new InputStreamReader(in));
      String strLine;
      while ((strLine = br.readLine()) != null)
      {
        if (strLine.startsWith("timer-interval = ")) {
          this.TIMER_ = Integer.parseInt(strLine.substring(17));
        }
        if (strLine.startsWith("check-block-area = ")) {
          this.AREA_ = Integer.parseInt(strLine.substring(19));
        }
        if (strLine.startsWith("debug-messages = ")) {
          this.DEBUGMESSAGES_ = true;
	    }
      }
	  sM("Timer: "+this.TIMER_ + ", Area: " +this.AREA_+", Debug messages: "+DEBUGMESSAGES_);
      in.close();
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
    }
    onEvent.set(this.TIMER_);
    this.Event.start();
  }

  public void loadRecipes() {
    try {
      String recipes = this.dirs + "recipes.dat";
      File conf = new File(recipes);
      if (!conf.exists()) {
        File dirs = new File(this.dirs);
        File File = new File(recipes);
        dirs.mkdirs();
        File.createNewFile();

        FileWriter fstream = new FileWriter(recipes);
        BufferedWriter out = new BufferedWriter(fstream);
        out.write("#Recipes file for iGrow"); out.newLine();
        out.write("#Lines that starts with the '#' sign, will be ignored!!"); out.newLine();
        out.write("#Usage:"); out.newLine();
        out.write("#  oldBlock,newBlock,needBlock,Chance:OutOf,enableNear,world"); out.newLine();
        out.write("#    - oldBlock = The block it was before"); out.newLine();
        out.write("#    - newBlock = The block it will change to"); out.newLine();
        out.write("#    - needBlock = The oldBlock needs to be connected to this block"); out.newLine();
        out.write("#    - Chance:OutOf = The chance to let the block change"); out.newLine();
        out.write("#    - enableNear = Spread newBlocks over the oldBlocks"); out.newLine();
        out.write("#    - world = name of the world this applies to - leave blank for all worlds"); out.newLine();
        out.write("#The first recipe (mossycobble) is enabled below."); out.newLine();
        out.write(""); out.newLine();
        out.write("#Mossy Cobblestone:"); out.newLine();
        out.write("4,48,8,30:100,false"); out.newLine();
        out.write("4,48,9,30:100,false"); out.newLine();
        out.write(""); out.newLine();
        out.write("#Ice Block:"); out.newLine();
        out.write("8,79,80,4:15,false"); out.newLine();
        out.write("9,79,80,4:15,false"); out.newLine();
        out.write(""); out.newLine();
        out.write("#Other recipes:");
        out.close();
      }
      FileInputStream fstream = new FileInputStream(recipes);
      DataInputStream in = new DataInputStream(fstream);
      BufferedReader br = new BufferedReader(new InputStreamReader(in));
      String strLine;
      sM("Reading recipes...");
      while ((strLine = br.readLine()) != null)
      {
        if (strLine.startsWith("#") || strLine.length() < 2) {
          continue;
        }
        String[] removeComments = strLine.split("#");
	    String[] donees = removeComments[0].split(",");
        Recipe recipe = new Recipe();
        if (strLine.contains("old:") && strLine.contains("new:")) {
        	recipe = scanRecipeLine_2_2(donees, recipe);
        } else {
        	try {
        		Integer.parseInt(strLine.substring(0, 1));
        		//sMdebug("int parsed ("+strLine.substring(0,1)+") v2.0 scan running...");
        		recipe = scanRecipeLine_2_0(donees, recipe);
        	}
        	catch(NumberFormatException nfe)
        	{
        		recipe = scanRecipeLine_2_1(donees, recipe);
        	}
        } 

        if (recipe.oldBlock != null) {
        	sMdebug("Recipe: oldblock:"+recipe.oldBlock+"@"+recipe.oldBlockData+", newblock: "+recipe.newBlock+"@"+recipe.newBlockData+", need: "+
        			recipe.needBlock+"@"+recipe.needBlockData+", chance: "+recipe.Chance[0]+"/"+(recipe.Chance[1]+1)+", world:"+recipe.world+", biome: "+
        			recipe.biome+", lightlevel: "+recipe.lightLevel+", ylevel: "+recipe.yLevel);
        
        	this.Recipes.add(recipe);
        } else {
        	sMdebug("Invalid recipe: strLine was - "+strLine);
        }
      }
      sM("Loaded " + this.Recipes.size() + " recipes!");
      in.close();
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
    }
  }
    
public Recipe scanRecipeLine_2_2(String[] donees, Recipe recipe) {
	HashMap<String, String> recipeBlock = new HashMap();
    for (int x = 0; x < donees.length; x++)
    {
        String[] recipeKeys = donees[x].split(":");
        if (recipeKeys.length == 2) {
        	recipeBlock.put(recipeKeys[0], recipeKeys[1]);
        }
    }

    if (recipeBlock.get("old") != null) {
    	recipe.oldBlock = recipeBlock.get("old").split("@")[0];
    	if (recipeBlock.get("old").split("@").length > 1) recipe.oldBlockData = recipeBlock.get("old").split("@")[1];
    }
    if (recipeBlock.get("new") != null) {
    	recipe.newBlock = recipeBlock.get("new").split("@")[0];    	
    	if (recipeBlock.get("new").split("@").length > 1) recipe.newBlockData = recipeBlock.get("new").split("@")[1];
    }
    if (recipeBlock.get("need") != null) {
    	recipe.needBlock = recipeBlock.get("need").split("@")[0];    	
    	if (recipeBlock.get("need").split("@").length > 1) recipe.needBlockData = recipeBlock.get("need").split("@")[1];
    }
    if (recipeBlock.get("world") != null) recipe.world = recipeBlock.get("world");
    if (recipeBlock.get("lightlevel") != null) recipe.lightLevel = recipeBlock.get("lightlevel");
    if (recipeBlock.get("ylevel") != null) recipe.yLevel = recipeBlock.get("ylevel");
    if (recipeBlock.get("biome") != null) recipe.biome = recipeBlock.get("biome");


    if (recipeBlock.get("near") != null) {
    	if (recipeBlock.get("near") == "true") {
    		recipe.Near = true;
    	}
    }
    if (recipeBlock.get("chance") != null) {
    	recipe.Chance[0] = Integer.parseInt(recipeBlock.get("chance").split("/")[0]);
    	recipe.Chance[1] = Integer.parseInt(recipeBlock.get("chance").split("/")[1]) - 1;
    }

    if (recipe.oldBlockData == null) recipe.oldBlockData = "";
    if (recipe.newBlockData == null) recipe.newBlockData = "";
    if (recipe.needBlockData == null) recipe.needBlockData = "";

    return recipe;
}
        
public Recipe scanRecipeLine_2_1(String[] donees, Recipe recipe) {
        
	    if ((donees.length < 4) || (donees.length > 6)) {
	    	sMdebug("Invalid line, not enough values found.  Values found: "+donees.length+".  Should be 5 or 6.");
	    	return null;
        }
        if (donees[0].contains("@")) {
        	String[] needBlockDataArray = donees[0].split("@");
        	recipe.oldBlock = String.valueOf(needBlockDataArray[0]);
        	recipe.oldBlockData = String.valueOf(needBlockDataArray[1]);
        } else {
        	recipe.oldBlock = String.valueOf(donees[0]);
        	recipe.oldBlockData = "";					  
        }
        if (donees[1].contains("@")) {
        	String[] needBlockDataArray = donees[1].split("@");
        	recipe.newBlock = String.valueOf(needBlockDataArray[0]);
        	recipe.newBlockData = String.valueOf(needBlockDataArray[1]);
        } else {
        	recipe.newBlock = String.valueOf(donees[1]);
        	recipe.newBlockData = "";				  
        }
        if (donees[2].contains("@")) {
        	String[] needBlockDataArray = donees[2].split("@");
        	recipe.needBlock = String.valueOf(needBlockDataArray[0]);
        	recipe.needBlockData = String.valueOf(needBlockDataArray[1]);
        } else {
        	recipe.needBlock = String.valueOf(donees[2]);
        	recipe.needBlockData = "";					  
        }
        if (donees[3].contains(":")) {
          String[] donees3 = donees[3].split(":");
          recipe.Chance[0] = Integer.parseInt(donees3[0]);
          recipe.Chance[1] = (Integer.parseInt(donees3[1]) - 1);
        }
        else {
          System.err.println("Error loading recipe for iGrow!");
          return null;
        }
        if (donees[4].contains("true")) {
          recipe.Near = true;
        }
        else if (donees[4].contains("false")) {
          recipe.Near = false;
        }
		if (donees.length == 6) {
		  recipe.world = donees[5]; 
        }
		return recipe;
  }


public Recipe scanRecipeLine_2_0(String[] donees, Recipe recipe) {
	if ((donees.length < 4) || (donees.length > 6)) {
    	sMdebug("Invalid line, not enough values found.  Values found: "+donees.length+".  Should be 5 or 6.");
		return null;
	}
	recipe.oldBlock = Material.getMaterial(Integer.parseInt(donees[0])).name();
	recipe.newBlock = Material.getMaterial(Integer.parseInt(donees[1])).name();
	recipe.needBlock = Material.getMaterial(Integer.parseInt(donees[2])).name();
	if (donees[3].contains(":")) {
		String[] donees3 = donees[3].split(":");
		recipe.Chance[0] = Integer.parseInt(donees3[0]);
		recipe.Chance[1] = (Integer.parseInt(donees3[1]) - 1);
	}
	else {
		System.err.println("Error loading recipe for iGrow!");
		return null;
	}
	if (donees[4].contains("true")) {
		recipe.Near = true;
	}
	else if (donees[4].contains("false")) {
		recipe.Near = false;
	}
	if (donees.length == 6) {
		recipe.world = donees[5];
	}
	return recipe;
}
  public boolean isDebugging(Player player) {
    if (this.debugees.containsKey(player)) {
      return ((Boolean)this.debugees.get(player)).booleanValue();
    }
    return false;
  }

  public void setDebugging(Player player, boolean value)
  {
    this.debugees.put(player, Boolean.valueOf(value));
  }

  public String getName() {
    PluginDescriptionFile pdfFile = getDescription();
    return pdfFile.getName();
  }
  public String getVersion() {
    PluginDescriptionFile pdfFile = getDescription();
    return pdfFile.getVersion();
  }
  public void sM(String message) {
    System.out.println("[" + getName() + ":" + getVersion() + "] " + message);
  }
  public void sMdebug(String message) {
	if (DEBUGMESSAGES_ == true) {    
	  System.out.println("[" + getName() + ":" + getVersion() + "] " + message);
	}
  }
  public Player getPlayer(String name) {
    for (Player pl : getServer().getOnlinePlayers()) {
      if (pl.getName().toLowerCase().startsWith(name.toLowerCase())) {
        return pl;
      }
    }
    return null;
  }
}

/* Location:           C:\Users\Robin\Downloads\iGrow.jar
 * Qualified Name:     com.bukkit.techguard.igrow.iGrow
 * JD-Core Version:    0.6.0
 */