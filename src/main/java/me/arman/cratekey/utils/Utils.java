package me.arman.cratekey.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import me.arman.cratekey.CrateKey;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class Utils {
  private static Plugin plugin = CrateKey.getPlugin();
  
  private static String prefix = String.valueOf(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.Prefix"))) + " ";
  
  private static Random rnd = new Random();
  
  public static void pushBack(Location source, Player player) {
    Location loc2 = player.getLocation();
    double deltaX = loc2.getX() - source.getX();
    double deltaZ = loc2.getZ() - source.getZ();
    Vector vec = new Vector(deltaX, 0.0D, deltaZ);
    vec.normalize();
    player.setVelocity(vec.multiply(plugin.getConfig().getDouble("Force") / Math.sqrt(Math.pow(deltaX, 2.0D) + Math.pow(deltaZ, 2.0D))));
  }
  
  public static int randInt(int min, int max) {
    int randomNum = rnd.nextInt(max - min + 1) + min;
    return randomNum;
  }
  
  public static void shuffleList(List<String> a) {
    int n = a.size();
    for (int i = 0; i < n; i++) {
      int change = i + rnd.nextInt(n - i);
      swap(a, i, change);
    } 
  }
  
  private static void swap(List<String> a, int i, int change) {
    String helper = a.get(i);
    a.set(i, a.get(change));
    a.set(change, helper);
  }
  
  public static void openCrate(Player player, String tier) {
    boolean lucky = false;
    int leastChance = 10000000;
    String chanceKey = null;
    for (String key : plugin.getConfig().getConfigurationSection("Rewards." + tier + ".PrizePackages").getKeys(false)) {
      int LuckyNumber = rnd.nextInt(plugin.getConfig().getInt("Rewards." + tier + ".PrizePackages." + key + ".Chance") + 1);
      if (LuckyNumber == 1) {
        List<String> cmds = plugin.getConfig().getStringList("Rewards." + tier + ".PrizePackages." + key + ".Commands");
        for (String cmd : cmds) {
          cmd = cmd.replaceAll("%player%", player.getName());
          Bukkit.getServer().dispatchCommand((CommandSender)Bukkit.getConsoleSender(), cmd);
        } 
        if (!plugin.getConfig().getString("Rewards." + tier + ".PrizePackages." + key + ".PlayerMessage").equals(""))
          player.sendMessage(String.valueOf(Utils.prefix) + ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Rewards." + tier + ".PrizePackages." + key + ".PlayerMessage"))
              .replace("%player%", player.getName())); 
        if (plugin.getConfig().getConfigurationSection("Rewards." + tier + ".PrizePackages." + key + ".Items") != null) {
          boolean inventoryFull = false;
          for (String items : plugin.getConfig().getConfigurationSection("Rewards." + tier + ".PrizePackages." + key + ".Items").getKeys(false)) {
            Material material;
            if (plugin.getConfig().getString("Rewards." + tier + ".PrizePackages." + key + ".Items." + items + ".Item").contains(":")) {
              material = Material.getMaterial(Integer.parseInt(plugin.getConfig().getString("Rewards." + tier + ".PrizePackages." + key + ".Items." + items + ".Item").split(":")[0]));
            } else {
              material = Material.getMaterial(Integer.parseInt(plugin.getConfig().getString("Rewards." + tier + ".PrizePackages." + key + ".Items." + items + ".Item")));
            } 
            int amount = plugin.getConfig().getInt("Rewards." + tier + ".PrizePackages." + key + ".Items." + items + ".Amount");
            ItemStack item = new ItemStack(material, amount);
            if (plugin.getConfig().getString("Rewards." + tier + ".PrizePackages." + key + ".Items." + items + ".Item").contains(":"))
              item = new ItemStack(material, amount, (short)Integer.parseInt(plugin.getConfig().getString("Rewards." + tier + ".PrizePackages." + key + ".Items." + items + ".Item").split(":")[1])); 
            ItemMeta itemMeta = item.getItemMeta();
            List<String> lores = new ArrayList<>();
            for (String enchantments : plugin.getConfig().getStringList("Rewards." + tier + ".PrizePackages." + key + ".Items." + items + ".Enchantments")) {
              String[] split = enchantments.split(":");
              itemMeta.addEnchant(Enchantment.getByName(split[0].toUpperCase()), Integer.parseInt(split[1]), true);
            } 
            for (String lore : plugin.getConfig().getStringList("Rewards." + tier + ".PrizePackages." + key + ".Items." + items + ".Lore"))
              lores.add(ChatColor.translateAlternateColorCodes('&', lore)); 
            itemMeta.setLore(lores);
            itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Rewards." + tier + ".PrizePackages." + key + ".Items." + items + ".Name")));
            item.setItemMeta(itemMeta);
            HashMap<Integer, ItemStack> drop = player.getInventory().addItem(new ItemStack[] { item });
            player.updateInventory();
            for (ItemStack dropItem : drop.values()) {
              player.getWorld().dropItem(player.getLocation(), dropItem);
              if (!inventoryFull) {
                player.sendMessage(String.valueOf(Utils.prefix) + ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.InventoryFull")));
                inventoryFull = true;
              } 
            } 
          } 
        } 
        if (plugin.getConfig().getBoolean("Rewards." + tier + ".PrizePackages." + key + ".Inventory.Enabled"))
          if (!plugin.getConfig().getBoolean("Rewards." + tier + ".Random.Enabled")) {
            String inventoryName = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Rewards." + tier + ".PrizePackages." + key + ".Inventory.Title"));
            Inventory inventory = Bukkit.getServer().createInventory(null, plugin.getConfig().getInt("Rewards." + tier + ".PrizePackages." + key + ".Inventory.Slots"), inventoryName);
            for (String items : plugin.getConfig().getConfigurationSection("Rewards." + tier + ".PrizePackages." + key + ".Inventory.Items").getKeys(false)) {
              Material material;
              if (plugin.getConfig().getString("Rewards." + tier + ".PrizePackages." + key + ".Inventory.Items." + items + ".Item").contains(":")) {
                material = Material.getMaterial(Integer.parseInt(plugin.getConfig().getString("Rewards." + tier + ".PrizePackages." + key + ".Inventory.Items." + items + ".Item").split(":")[0]));
              } else {
                material = Material.getMaterial(Integer.parseInt(plugin.getConfig().getString("Rewards." + tier + ".PrizePackages." + key + ".Inventory.Items." + items + ".Item")));
              } 
              int amount = plugin.getConfig().getInt("Rewards." + tier + ".PrizePackages." + key + ".Inventory.Items." + items + ".Amount");
              ItemStack item = new ItemStack(material, amount);
              if (plugin.getConfig().getString("Rewards." + tier + ".PrizePackages." + key + ".Inventory.Items." + items + ".Item").contains(":"))
                item = new ItemStack(material, amount, (short)Integer.parseInt(plugin.getConfig().getString("Rewards." + tier + ".PrizePackages." + key + ".Inventory.Items." + items + ".Item").split(":")[1])); 
              ItemMeta itemMeta = item.getItemMeta();
              List<String> lores = new ArrayList<>();
              for (String enchantments : plugin.getConfig().getStringList("Rewards." + tier + ".PrizePackages." + key + ".Inventory.Items." + items + ".Enchantments")) {
                String[] split = enchantments.split(":");
                itemMeta.addEnchant(Enchantment.getByName(split[0].toUpperCase()), Integer.parseInt(split[1]), true);
              } 
              for (String lore : plugin.getConfig().getStringList("Rewards." + tier + ".PrizePackages." + key + ".Inventory.Items." + items + ".Lore"))
                lores.add(ChatColor.translateAlternateColorCodes('&', lore)); 
              itemMeta.setLore(lores);
              itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Rewards." + tier + ".PrizePackages." + key + ".Inventory.Items." + items + ".Name")));
              item.setItemMeta(itemMeta);
              inventory.setItem(Integer.parseInt(items) - 1, item);
            } 
            player.openInventory(inventory);
          } else {
            String inventoryName = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Rewards." + tier + ".PrizePackages." + key + ".Inventory.Title"));
            Inventory inventory = Bukkit.getServer().createInventory(null, plugin.getConfig().getInt("Rewards." + tier + ".PrizePackages." + key + ".Inventory.Slots"), inventoryName);
            Set<String> itemSet = plugin.getConfig().getConfigurationSection("Rewards." + tier + ".PrizePackages." + key + ".Inventory.Items").getKeys(false);
            List<String> itemList = new ArrayList<>(itemSet);
            int maximumRandom = plugin.getConfig().getInt("Rewards." + tier + ".Random.Max");
            int minimumRandom = plugin.getConfig().getInt("Rewards." + tier + ".Random.Min");
            int amountOfItems = randInt(minimumRandom, maximumRandom);
            int count = 0;
            for (String items : itemList) {
              Material material;
              if (plugin.getConfig().getString("Rewards." + tier + ".PrizePackages." + key + ".Inventory.Items." + items + ".Item").contains(":")) {
                material = Material.getMaterial(Integer.parseInt(plugin.getConfig().getString("Rewards." + tier + ".PrizePackages." + key + ".Inventory.Items." + items + ".Item").split(":")[0]));
              } else {
                material = Material.getMaterial(Integer.parseInt(plugin.getConfig().getString("Rewards." + tier + ".PrizePackages." + key + ".Inventory.Items." + items + ".Item")));
              } 
              int amount = plugin.getConfig().getInt("Rewards." + tier + ".PrizePackages." + key + ".Inventory.Items." + items + ".Amount");
              ItemStack item = new ItemStack(material, amount);
              if (plugin.getConfig().getString("Rewards." + tier + ".PrizePackages." + key + ".Inventory.Items." + items + ".Item").contains(":"))
                item = new ItemStack(material, amount, (short)Integer.parseInt(plugin.getConfig().getString("Rewards." + tier + ".PrizePackages." + key + ".Inventory.Items." + items + ".Item").split(":")[1])); 
              ItemMeta itemMeta = item.getItemMeta();
              List<String> lores = new ArrayList<>();
              for (String enchantments : plugin.getConfig().getStringList("Rewards." + tier + ".PrizePackages." + key + ".Inventory.Items." + items + ".Enchantments")) {
                String[] split = enchantments.split(":");
                itemMeta.addEnchant(Enchantment.getByName(split[0].toUpperCase()), Integer.parseInt(split[1]), true);
              } 
              for (String lore : plugin.getConfig().getStringList("Rewards." + tier + ".PrizePackages." + key + ".Inventory.Items." + items + ".Lore"))
                lores.add(ChatColor.translateAlternateColorCodes('&', lore)); 
              itemMeta.setLore(lores);
              itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Rewards." + tier + ".PrizePackages." + key + ".Inventory.Items." + items + ".Name")));
              item.setItemMeta(itemMeta);
              inventory.setItem(Integer.parseInt(items) - 1, item);
              count++;
              if (count == amountOfItems)
                break; 
            } 
            player.openInventory(inventory);
          }  
        for (String message : plugin.getConfig().getStringList("Rewards." + tier + ".PrizePackages." + key + ".BroadcastMessage")) {
          String prefix = ChatColor.translateAlternateColorCodes('&', String.valueOf(plugin.getConfig().getString("Rewards." + tier + ".BroadcastPrefix")) + " ");
          Bukkit.broadcastMessage(String.valueOf(prefix) + ChatColor.translateAlternateColorCodes('&', message.replace("%player%", player.getName())));
        } 
        lucky = true;
        break;
      } 
    } 
    if (!lucky) {
      List<String> keys = new ArrayList<>();
      for (String key : plugin.getConfig().getConfigurationSection("Rewards." + tier + ".PrizePackages").getKeys(false)) {
        if (plugin.getConfig().getInt("Rewards." + tier + ".PrizePackages." + key + ".Chance") < leastChance)
          leastChance = plugin.getConfig().getInt("Rewards." + tier + ".PrizePackages." + key + ".Chance"); 
      } 
      for (String key : plugin.getConfig().getConfigurationSection("Rewards." + tier + ".PrizePackages.").getKeys(false)) {
        if (plugin.getConfig().getInt("Rewards." + tier + ".PrizePackages." + key + ".Chance") == leastChance)
          keys.add(key); 
      } 
      chanceKey = keys.get(rnd.nextInt(keys.size()));
      List<String> cmds = plugin.getConfig().getStringList("Rewards." + tier + ".PrizePackages." + chanceKey + ".Commands");
      for (String cmd : cmds) {
        cmd = cmd.replaceAll("%player%", player.getName());
        Bukkit.getServer().dispatchCommand((CommandSender)Bukkit.getConsoleSender(), cmd);
      } 
      if (!plugin.getConfig().getString("Rewards." + tier + ".PrizePackages." + chanceKey + ".PlayerMessage").equals(""))
        player.sendMessage(String.valueOf(Utils.prefix) + ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Rewards." + tier + ".PrizePackages." + chanceKey + ".PlayerMessage"))
            .replace("%player%", player.getName())); 
      if (plugin.getConfig().getConfigurationSection("Rewards." + tier + ".PrizePackages." + chanceKey + ".Items") != null) {
        boolean inventoryFull = false;
        for (String items : plugin.getConfig().getConfigurationSection("Rewards." + tier + ".PrizePackages." + chanceKey + ".Items").getKeys(false)) {
          Material material;
          if (plugin.getConfig().getString("Rewards." + tier + ".PrizePackages." + chanceKey + ".Items." + items + ".Item").contains(":")) {
            material = Material.getMaterial(Integer.parseInt(plugin.getConfig().getString("Rewards." + tier + ".PrizePackages." + chanceKey + ".Items." + items + ".Item").split(":")[0]));
          } else {
            material = Material.getMaterial(Integer.parseInt(plugin.getConfig().getString("Rewards." + tier + ".PrizePackages." + chanceKey + ".Items." + items + ".Item")));
          } 
          int amount = plugin.getConfig().getInt("Rewards." + tier + ".PrizePackages." + chanceKey + ".Items." + items + ".Amount");
          ItemStack item = new ItemStack(material, amount);
          if (plugin.getConfig().getString("Rewards." + tier + ".PrizePackages." + chanceKey + ".Items." + items + ".Item").contains(":"))
            item = new ItemStack(material, amount, (short)Integer.parseInt(plugin.getConfig().getString("Rewards." + tier + ".PrizePackages." + chanceKey + ".Items." + items + ".Item").split(":")[1])); 
          ItemMeta itemMeta = item.getItemMeta();
          List<String> lores = new ArrayList<>();
          for (String enchantments : plugin.getConfig().getStringList("Rewards." + tier + ".PrizePackages." + chanceKey + ".Items." + items + ".Enchantments")) {
            String[] split = enchantments.split(":");
            itemMeta.addEnchant(Enchantment.getByName(split[0].toUpperCase()), Integer.parseInt(split[1]), true);
          } 
          for (String lore : plugin.getConfig().getStringList("Rewards." + tier + ".PrizePackages." + chanceKey + ".Items." + items + ".Lore"))
            lores.add(ChatColor.translateAlternateColorCodes('&', lore)); 
          itemMeta.setLore(lores);
          itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Rewards." + tier + ".PrizePackages." + chanceKey + ".Items." + items + ".Name")));
          item.setItemMeta(itemMeta);
          HashMap<Integer, ItemStack> drop = player.getInventory().addItem(new ItemStack[] { item });
          player.updateInventory();
          for (ItemStack dropItem : drop.values()) {
            player.getWorld().dropItem(player.getLocation(), dropItem);
            if (!inventoryFull) {
              player.sendMessage(String.valueOf(Utils.prefix) + ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.InventoryFull")));
              inventoryFull = true;
            } 
          } 
        } 
      } 
      if (plugin.getConfig().getBoolean("Rewards." + tier + ".PrizePackages." + chanceKey + ".Inventory.Enabled")) {
        String inventoryName = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Rewards." + tier + ".PrizePackages." + chanceKey + ".Inventory.Title"));
        Inventory inventory = Bukkit.getServer().createInventory(null, plugin.getConfig().getInt("Rewards." + tier + ".PrizePackages." + chanceKey + ".Inventory.Slots"), inventoryName);
        for (String items : plugin.getConfig().getConfigurationSection("Rewards." + tier + ".PrizePackages." + chanceKey + ".Inventory.Items").getKeys(false)) {
          Material material;
          if (plugin.getConfig().getString("Rewards." + tier + ".PrizePackages." + chanceKey + ".Inventory.Items." + items + ".Item").contains(":")) {
            material = Material.getMaterial(Integer.parseInt(plugin.getConfig().getString("Rewards." + tier + ".PrizePackages." + chanceKey + ".Inventory.Items." + items + ".Item").split(":")[0]));
          } else {
            material = Material.getMaterial(Integer.parseInt(plugin.getConfig().getString("Rewards." + tier + ".PrizePackages." + chanceKey + ".Inventory.Items." + items + ".Item")));
          } 
          int amount = plugin.getConfig().getInt("Rewards." + tier + ".PrizePackages." + chanceKey + ".Inventory.Items." + items + ".Amount");
          ItemStack item = new ItemStack(material, amount);
          if (plugin.getConfig().getString("Rewards." + tier + ".PrizePackages." + chanceKey + ".Inventory.Items." + items + ".Item").contains(":"))
            item = new ItemStack(material, amount, (short)Integer.parseInt(plugin.getConfig().getString("Rewards." + tier + ".PrizePackages." + chanceKey + ".Inventory.Items." + items + ".Item").split(":")[1])); 
          ItemMeta itemMeta = item.getItemMeta();
          List<String> lores = new ArrayList<>();
          for (String enchantments : plugin.getConfig().getStringList("Rewards." + tier + ".PrizePackages." + chanceKey + ".Inventory.Items." + items + ".Enchantments")) {
            String[] split = enchantments.split(":");
            itemMeta.addEnchant(Enchantment.getByName(split[0].toUpperCase()), Integer.parseInt(split[1]), true);
          } 
          for (String lore : plugin.getConfig().getStringList("Rewards." + tier + ".PrizePackages." + chanceKey + ".Inventory.Items." + items + ".Lore"))
            lores.add(ChatColor.translateAlternateColorCodes('&', lore)); 
          itemMeta.setLore(lores);
          itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Rewards." + tier + ".PrizePackages." + chanceKey + ".Inventory.Items." + items + ".Name")));
          item.setItemMeta(itemMeta);
          inventory.setItem(Integer.parseInt(items) - 1, item);
        } 
        player.openInventory(inventory);
      } 
      for (String message : plugin.getConfig().getStringList("Rewards." + tier + ".PrizePackages." + chanceKey + ".BroadcastMessage")) {
        String prefix = ChatColor.translateAlternateColorCodes('&', String.valueOf(plugin.getConfig().getString("Rewards." + tier + ".BroadcastPrefix")) + " ");
        Bukkit.broadcastMessage(String.valueOf(prefix) + ChatColor.translateAlternateColorCodes('&', message.replace("%player%", player.getName())));
      } 
    } 
  }
  
  private static Color getColor(int i) {
    Color c = null;
    if (i == 1)
      c = Color.AQUA; 
    if (i == 2)
      c = Color.BLACK; 
    if (i == 3)
      c = Color.BLUE; 
    if (i == 4)
      c = Color.FUCHSIA; 
    if (i == 5)
      c = Color.GRAY; 
    if (i == 6)
      c = Color.GREEN; 
    if (i == 7)
      c = Color.LIME; 
    if (i == 8)
      c = Color.MAROON; 
    if (i == 9)
      c = Color.NAVY; 
    if (i == 10)
      c = Color.OLIVE; 
    if (i == 11)
      c = Color.ORANGE; 
    if (i == 12)
      c = Color.PURPLE; 
    if (i == 13)
      c = Color.RED; 
    if (i == 14)
      c = Color.SILVER; 
    if (i == 15)
      c = Color.TEAL; 
    if (i == 16)
      c = Color.WHITE; 
    if (i == 17)
      c = Color.YELLOW; 
    return c;
  }
  
  public static FireworkEffect getRandomEffect() {
    int rt = rnd.nextInt(4) + 1;
    FireworkEffect.Type type = FireworkEffect.Type.BALL;
    if (rt == 1)
      type = FireworkEffect.Type.BALL; 
    if (rt == 2)
      type = FireworkEffect.Type.BALL_LARGE; 
    if (rt == 3)
      type = FireworkEffect.Type.BURST; 
    if (rt == 4)
      type = FireworkEffect.Type.CREEPER; 
    if (rt == 5)
      type = FireworkEffect.Type.STAR; 
    int r1i = rnd.nextInt(17) + 1;
    int r2i = rnd.nextInt(17) + 1;
    Color c1 = getColor(r1i);
    Color c2 = getColor(r2i);
    FireworkEffect effect = FireworkEffect.builder().flicker(rnd.nextBoolean()).withColor(c1).withFade(c2).with(type).trail(rnd.nextBoolean()).build();
    return effect;
  }
}
