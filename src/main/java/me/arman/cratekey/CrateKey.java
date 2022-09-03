package me.arman.cratekey;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import me.arman.cratekey.utils.CustomEntityFirework;
import me.arman.cratekey.utils.ParticleEffect;
import me.arman.cratekey.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class CrateKey extends JavaPlugin implements Listener {
  private static Plugin plugin;
  
  private String prefix;
  
  private List<String> remove = new ArrayList<>();
  
  private HashMap<String, String> add = new HashMap<>();
  
  public void onEnable() {
    plugin = (Plugin)this;
    checkParticles();
    checkConfig();
    Bukkit.getPluginManager().registerEvents(this, (Plugin)this);
    this.prefix = String.valueOf(ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.Prefix"))) + " ";
  }
  
  public void onDisable() {
    reloadConfig();
  }
  
  public void checkParticles() {
    for (String key : getConfig().getConfigurationSection("Rewards").getKeys(false)) {
      if (getConfig().getBoolean("Rewards." + key + ".ChestParticle.Enabled"))
        Bukkit.getScheduler().runTaskTimerAsynchronously((Plugin)this, new Runnable() {
              public void run() {
                for (String location : CrateKey.this.getConfig().getStringList("Rewards." + key + ".CrateLocations")) {
                  String[] split = location.split(",");
                  Location center = new Location(Bukkit.getWorld(split[3]), Double.parseDouble(split[0]) + 0.5D, Double.parseDouble(split[1]) + 1.0D, 
                      Double.parseDouble(split[2]) + 0.5D);
                  int amount = CrateKey.this.getConfig().getInt("Rewards." + key + ".ChestParticle.Amount");
                  int speed = CrateKey.this.getConfig().getInt("Rewards." + key + ".ChestParticle.Speed");
                  int offsetX = CrateKey.this.getConfig().getInt("Rewards." + key + ".ChestParticle.OffsetX");
                  int offsetY = CrateKey.this.getConfig().getInt("Rewards." + key + ".ChestParticle.OffsetY");
                  int offsetZ = CrateKey.this.getConfig().getInt("Rewards." + key + ".ChestParticle.OffsetZ");
                  ParticleEffect.ParticleType type = ParticleEffect.ParticleType.valueOf(CrateKey.this.getConfig().getString("Rewards." + key + ".ChestParticle.Particle"));
                  ParticleEffect particle = new ParticleEffect(type, speed, amount, offsetX, offsetY, offsetZ);
                  particle.sendToLocation(center);
                } 
              }
            }0L, getConfig().getInt("Rewards." + key + ".ChestParticle.Interval")); 
    } 
  }
  
  public void checkConfig() {
    File file = new File("/CrateKey/config.yml");
    if (file.exists() && !file.isDirectory())
      return; 
    saveDefaultConfig();
  }
  
  public static Plugin getPlugin() {
    return plugin;
  }
  
  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    try {
      for (String keys : getConfig().getConfigurationSection("Rewards").getKeys(false)) {
        if (ChatColor.translateAlternateColorCodes('&', getConfig().getString("Rewards." + keys + ".GUI.Title")).equals(event.getInventory().getTitle())) {
          event.setCancelled(true);
          break;
        } 
      } 
    } catch (Exception exception) {}
  }
  
  @EventHandler
  public void onInventoryMoveItem(InventoryMoveItemEvent event) {
    try {
      for (String keys : getConfig().getConfigurationSection("Rewards").getKeys(false)) {
        if (ChatColor.translateAlternateColorCodes('&', getConfig().getString("Rewards." + keys + ".GUI.Title")).equals(event.getSource().getTitle())) {
          event.setCancelled(true);
          break;
        } 
      } 
    } catch (Exception exception) {}
  }
  
  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    this.remove.remove(event.getPlayer().getName());
    this.add.remove(event.getPlayer().getName());
  }
  
  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (event.getAction().equals(Action.LEFT_CLICK_BLOCK) && 
      event.getClickedBlock().getType().equals(Material.CHEST)) {
      Player player = event.getPlayer();
      String location = String.valueOf(event.getClickedBlock().getLocation().getX()) + "," + event.getClickedBlock().getLocation().getY() + "," + 
        event.getClickedBlock().getLocation().getZ() + "," + event.getClickedBlock().getLocation().getWorld().getName();
      for (String keys : getConfig().getConfigurationSection("Rewards").getKeys(false)) {
        if (getConfig().getStringList("Rewards." + keys + ".CrateLocations").isEmpty())
          continue; 
        List<String> locations = getConfig().getStringList("Rewards." + keys + ".CrateLocations");
        if (locations.contains(location)) {
          if (plugin.getConfig().getBoolean("Rewards." + keys + ".GUI.Enabled")) {
            String inventoryName = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Rewards." + keys + ".GUI.Title"));
            Inventory inventory = Bukkit.getServer().createInventory(null, plugin.getConfig().getInt("Rewards." + keys + ".GUI.Slots"), inventoryName);
            for (String items : plugin.getConfig().getConfigurationSection("Rewards." + keys + ".GUI.Items").getKeys(false)) {
              Material material;
              if (getConfig().getString("Rewards." + keys + ".GUI.Items." + items + ".Item").contains(":")) {
                material = Material.getMaterial(Integer.parseInt(getConfig().getString("Rewards." + keys + ".GUI.Items." + items + ".Item").split(":")[0]));
              } else {
                material = Material.getMaterial(Integer.parseInt(getConfig().getString("Rewards." + keys + ".GUI.Items." + items + ".Item")));
              } 
              int amount = plugin.getConfig().getInt("Rewards." + keys + ".GUI.Items." + items + ".Amount");
              ItemStack item = new ItemStack(material, amount);
              ItemMeta itemMeta = item.getItemMeta();
              List<String> lores = new ArrayList<>();
              if (getConfig().getString("Rewards." + keys + ".GUI.Items." + items + ".Item").contains(":"))
                item = new ItemStack(material, amount, (short)Integer.parseInt(getConfig().getString("Rewards." + keys + ".GUI.Items." + items + ".Item").split(":")[1])); 
              if (plugin.getConfig().getStringList("Rewards." + keys + ".GUI.Items." + items + ".Enchantments") != null)
                for (String enchantments : plugin.getConfig().getStringList("Rewards." + keys + ".GUI.Items." + items + ".Enchantments")) {
                  String[] split = enchantments.split(":");
                  itemMeta.addEnchant(Enchantment.getByName(split[0].toUpperCase()), Integer.parseInt(split[1]), true);
                }  
              for (String lore : plugin.getConfig().getStringList("Rewards." + keys + ".GUI.Items." + items + ".Lore"))
                lores.add(ChatColor.translateAlternateColorCodes('&', lore)); 
              itemMeta.setLore(lores);
              itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Rewards." + keys + ".GUI.Items." + items + ".Name")));
              item.setItemMeta(itemMeta);
              inventory.setItem(Integer.parseInt(items) - 1, item);
            } 
            player.openInventory(inventory);
          } 
          event.setCancelled(true);
          break;
        } 
      } 
    } 
    if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
      if (this.remove.contains(event.getPlayer().getName())) {
        if (event.getClickedBlock().getType().equals(Material.CHEST)) {
          Player player = event.getPlayer();
          boolean removed = false;
          String location = String.valueOf(event.getClickedBlock().getLocation().getX()) + "," + event.getClickedBlock().getLocation().getY() + "," + 
            event.getClickedBlock().getLocation().getZ() + "," + event.getClickedBlock().getLocation().getWorld().getName();
          this.remove.remove(player.getName());
          for (String keys : getConfig().getConfigurationSection("Rewards").getKeys(false)) {
            List<String> locations = new ArrayList<>();
            if (!getConfig().getStringList("Rewards." + keys + ".CrateLocations").isEmpty() || 
              getConfig().getStringList("Rewards." + keys + ".CrateLocations") != null)
              for (String list : getConfig().getStringList("Rewards." + keys + ".CrateLocations"))
                locations.add(list);  
            if (locations.contains(location)) {
              locations.remove(location);
              getConfig().set("Rewards." + keys + ".CrateLocations", locations);
              saveConfig();
              player.sendMessage(String.valueOf(this.prefix) + ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.RemoveCrate")));
              removed = true;
              break;
            } 
          } 
          if (!removed)
            player.sendMessage(String.valueOf(this.prefix) + ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.InvalidCrate"))); 
        } else {
          event.getPlayer().sendMessage(String.valueOf(this.prefix) + ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.InvalidCrate")));
        } 
        event.setCancelled(true);
        return;
      } 
      if (this.add.containsKey(event.getPlayer().getName())) {
        Player player = event.getPlayer();
        if (event.getClickedBlock().getType().equals(Material.CHEST)) {
          String location = String.valueOf(event.getClickedBlock().getLocation().getX()) + "," + event.getClickedBlock().getLocation().getY() + "," + 
            event.getClickedBlock().getLocation().getZ() + "," + event.getClickedBlock().getLocation().getWorld().getName();
          List<String> currentList = new ArrayList<>();
          if (!getConfig().getStringList("Rewards." + (String)this.add.get(player.getName()) + ".CrateLocations").isEmpty())
            for (String list : getConfig().getStringList("Rewards." + (String)this.add.get(player.getName()) + ".CrateLocations"))
              currentList.add(list);  
          if (currentList.contains(location)) {
            player.sendMessage(String.valueOf(this.prefix) + ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.AlreadyCrate")));
          } else {
            player.sendMessage(String.valueOf(this.prefix) + ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.AddCrate")));
            currentList.add(location);
            getConfig().set("Rewards." + (String)this.add.get(player.getName()) + ".CrateLocations", currentList);
            saveConfig();
          } 
        } else {
          player.sendMessage(String.valueOf(this.prefix) + ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.NotAChest")));
        } 
        this.add.remove(player.getName());
        event.setCancelled(true);
        return;
      } 
      if (event.getClickedBlock().getType().equals(Material.CHEST)) {
        Player player = event.getPlayer();
        String location = String.valueOf(event.getClickedBlock().getLocation().getX()) + "," + event.getClickedBlock().getLocation().getY() + "," + 
          event.getClickedBlock().getLocation().getZ() + "," + event.getClickedBlock().getLocation().getWorld().getName();
        for (String keys : getConfig().getConfigurationSection("Rewards").getKeys(false)) {
          if (getConfig().getStringList("Rewards." + keys + ".CrateLocations").isEmpty())
            continue; 
          List<String> locations = getConfig().getStringList("Rewards." + keys + ".CrateLocations");
          if (locations.contains(location)) {
            Material material;
            if (getConfig().getString("Rewards." + keys + ".KeyMaterial").contains(":")) {
              material = Material.getMaterial(Integer.parseInt(getConfig().getString("Rewards." + keys + ".KeyMaterial").split(":")[0]));
            } else {
              material = Material.getMaterial(Integer.parseInt(getConfig().getString("Rewards." + keys + ".KeyMaterial")));
            } 
            String keyName = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Rewards." + keys + ".KeyName"));
            List<String> lores = new ArrayList<>();
            for (String lore : getConfig().getStringList("Rewards." + keys + ".KeyLore"))
              lores.add(ChatColor.translateAlternateColorCodes('&', lore)); 
            ItemStack item = new ItemStack(material, 1);
            if (getConfig().getString("Rewards." + keys + ".KeyMaterial").contains(":"))
              item = new ItemStack(material, 1, (short)Integer.parseInt(getConfig().getString("Rewards." + keys + ".KeyMaterial").split(":")[1])); 
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.setDisplayName(keyName);
            itemMeta.setLore(lores);
            if (!getConfig().getString("Rewards." + keys + ".KeyEnchantment").equals("")) {
              String[] enchantmentAndLevel = getConfig().getString("Rewards." + keys + ".KeyEnchantment").split(":");
              Enchantment enchantment = Enchantment.getByName(enchantmentAndLevel[0]);
              int enchantmentLevel = Integer.parseInt(enchantmentAndLevel[1]);
              itemMeta.addEnchant(enchantment, enchantmentLevel, true);
            } 
            item.setItemMeta(itemMeta);
            if (player.getItemInHand() == null || player.getItemInHand().getType().equals(Material.AIR)) {
              if (getConfig().getBoolean("Push-Back"))
                Utils.pushBack(event.getClickedBlock().getLocation(), player); 
              player.sendMessage(String.valueOf(this.prefix) + ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.InvalidKey")
                    .replace("%tier%", keys)));
              event.setCancelled(true);
              break;
            } 
            if (player.getItemInHand().getItemMeta().equals(itemMeta)) {
              if (player.getItemInHand().getType().equals(material)) {
                if (!getConfig().getString("Rewards." + keys + ".KeySound").equals("")) {
                  Sound sound = Sound.valueOf(getConfig().getString("Rewards." + keys + ".KeySound").toUpperCase());
                  player.playSound(player.getLocation(), sound, 1.0F, 0.0F);
                } 
                player.getInventory().removeItem(new ItemStack[] { item });
                player.updateInventory();
                if (!getConfig().getString("Rewards." + keys + ".Particle").equals(""))
                  if (getConfig().getString("Rewards." + keys + ".Particle").equalsIgnoreCase("FIREWORK")) {
                    CustomEntityFirework.spawn(event.getClickedBlock().getLocation().add(0.5D, 1.0D, 0.5D), Utils.getRandomEffect(), Bukkit.getOnlinePlayers());
                  } else {
                    ParticleEffect.ParticleType type = ParticleEffect.ParticleType.valueOf(getConfig().getString("Rewards." + keys + ".Particle"));
                    ParticleEffect particle = new ParticleEffect(type, 4.0D, 20, 3.0F, 3.0F, 3.0F);
                    particle.sendToLocation(event.getClickedBlock().getLocation().add(0.5D, 1.0D, 0.5D));
                  }  
                Utils.openCrate(player, keys);
                event.setCancelled(true);
                break;
              } 
              if (getConfig().getBoolean("Push-Back"))
                Utils.pushBack(event.getClickedBlock().getLocation(), player); 
              player.sendMessage(String.valueOf(this.prefix) + ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.InvalidKey")
                    .replace("%tier%", keys)));
              event.setCancelled(true);
              continue;
            } 
            if (getConfig().getBoolean("Push-Back"))
              Utils.pushBack(event.getClickedBlock().getLocation(), player); 
            player.sendMessage(String.valueOf(this.prefix) + ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.InvalidKey")
                  .replace("%tier%", keys)));
            event.setCancelled(true);
          } 
        } 
      } 
    } 
  }
  
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (cmd.getName().equalsIgnoreCase("cratekey")) {
      if (args.length == 0)
        for (String help : getConfig().getStringList("Messages.Help"))
          sender.sendMessage(String.valueOf(this.prefix) + ChatColor.translateAlternateColorCodes('&', help));  
      if (args.length == 1) {
        if (args[0].equalsIgnoreCase("reload") && 
          sender instanceof Player) {
          Player player = (Player)sender;
          if (player.hasPermission("cratekey.reload")) {
            player.sendMessage(String.valueOf(this.prefix) + ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.Reload")));
            reloadConfig();
            saveConfig();
            this.prefix = String.valueOf(ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.Prefix"))) + " ";
            Bukkit.getScheduler().cancelTasks((Plugin)this);
            checkParticles();
          } else {
            player.sendMessage(String.valueOf(this.prefix) + ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.NoPermission")));
          } 
        } 
        if (args[0].equalsIgnoreCase("removecrate") && 
          sender instanceof Player) {
          Player player = (Player)sender;
          if (player.hasPermission("cratekey.removecrate")) {
            if (this.add.containsKey(player.getName()))
              this.add.remove(player.getName()); 
            this.remove.add(player.getName());
            player.sendMessage(String.valueOf(this.prefix) + ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.Remove")));
          } else {
            player.sendMessage(String.valueOf(this.prefix) + ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.NoPermission")));
          } 
        } 
      } 
      if (args.length == 2 && 
        args[0].equalsIgnoreCase("addcrate") && 
        sender instanceof Player) {
        boolean found = false;
        Player player = (Player)sender;
        if (player.hasPermission("cratekey.addcrate")) {
          for (String keys : getConfig().getConfigurationSection("Rewards").getKeys(false)) {
            if (keys.equalsIgnoreCase(args[1])) {
              args[1] = keys;
              found = true;
              break;
            } 
          } 
          if (found) {
            if (this.remove.contains(player.getName()))
              this.remove.remove(player.getName()); 
            this.add.put(player.getName(), args[1]);
            player.sendMessage(String.valueOf(this.prefix) + ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.Add")));
          } else {
            player.sendMessage(String.valueOf(this.prefix) + ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.InvalidTier")));
          } 
        } else {
          player.sendMessage(String.valueOf(this.prefix) + ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.NoPermission")));
        } 
      } 
      if (args.length == 3 && 
        args[0].equalsIgnoreCase("giveall"))
        if (sender.hasPermission("cratekey.giveall")) {
          boolean found = false;
          for (String keys : getConfig().getConfigurationSection("Rewards").getKeys(false)) {
            if (keys.equalsIgnoreCase(args[1])) {
              found = true;
              args[1] = keys;
              break;
            } 
          } 
          if (found) {
            try {
              sender.sendMessage(String.valueOf(this.prefix) + ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.GiveAll")
                    .replace("%cratekeys%", args[2])
                    .replace("%tier%", args[1])));
              byte b;
              int i;
              Player[] arrayOfPlayer;
              for (i = (arrayOfPlayer = Bukkit.getOnlinePlayers()).length, b = 0; b < i; ) {
                Player target = arrayOfPlayer[b];
                if (!target.hasPermission("cratekey.giveall.exempt")) {
                  Material material;
                  if (sender instanceof Player) {
                    Player player = (Player)sender;
                    if (target == player)
                      continue; 
                  } 
                  int keyInt = Integer.parseInt(args[2]);
                  if (getConfig().getString("Rewards." + args[1] + ".KeyMaterial").contains(":")) {
                    material = Material.getMaterial(Integer.parseInt(getConfig().getString("Rewards." + args[1] + ".KeyMaterial").split(":")[0]));
                  } else {
                    material = Material.getMaterial(Integer.parseInt(getConfig().getString("Rewards." + args[1] + ".KeyMaterial")));
                  } 
                  String keyName = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Rewards." + args[1] + ".KeyName"));
                  List<String> lores = new ArrayList<>();
                  for (String lore : getConfig().getStringList("Rewards." + args[1] + ".KeyLore"))
                    lores.add(ChatColor.translateAlternateColorCodes('&', lore)); 
                  if (keyInt > 0) {
                    ItemStack item = new ItemStack(material, keyInt);
                    if (getConfig().getString("Rewards." + args[1] + ".KeyMaterial").contains(":"))
                      item = new ItemStack(material, keyInt, (short)Integer.parseInt(getConfig().getString("Rewards." + args[1] + ".KeyMaterial").split(":")[1])); 
                    ItemMeta itemMeta = item.getItemMeta();
                    itemMeta.setDisplayName(keyName);
                    itemMeta.setLore(lores);
                    if (!getConfig().getString("Rewards." + args[1] + ".KeyEnchantment").equals("")) {
                      String[] enchantmentAndLevel = getConfig().getString("Rewards." + args[1] + ".KeyEnchantment").split(":");
                      Enchantment enchantment = Enchantment.getByName(enchantmentAndLevel[0]);
                      int enchantmentLevel = Integer.parseInt(enchantmentAndLevel[1]);
                      itemMeta.addEnchant(enchantment, enchantmentLevel, true);
                    } 
                    item.setItemMeta(itemMeta);
                    target.getInventory().addItem(new ItemStack[] { item });
                    target.sendMessage(String.valueOf(this.prefix) + ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.Recieve")
                          .replace("%cratekeys%", args[2])
                          .replace("%tier%", args[1])));
                  } 
                } 
                continue;
                b++;
              } 
            } catch (NumberFormatException event) {
              sender.sendMessage(String.valueOf(this.prefix) + ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.InvalidNumber")));
            } 
          } else {
            sender.sendMessage(String.valueOf(this.prefix) + ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.InvalidTier")));
          } 
        } else {
          sender.sendMessage(String.valueOf(this.prefix) + ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.NoPermission")));
        }  
      if (args.length == 4 && 
        args[0].equalsIgnoreCase("give"))
        if (sender.hasPermission("cratekey.give")) {
          Player target = Bukkit.getPlayer(args[1]);
          boolean found = false;
          if (target != null) {
            for (String keys : getConfig().getConfigurationSection("Rewards").getKeys(false)) {
              if (keys.equalsIgnoreCase(args[2])) {
                found = true;
                args[2] = keys;
                break;
              } 
            } 
            if (found) {
              try {
                Material material;
                int keyInt = Integer.parseInt(args[3]);
                if (getConfig().getString("Rewards." + args[2] + ".KeyMaterial").contains(":")) {
                  material = Material.getMaterial(Integer.parseInt(getConfig().getString("Rewards." + args[2] + ".KeyMaterial").split(":")[0]));
                } else {
                  material = Material.getMaterial(Integer.parseInt(getConfig().getString("Rewards." + args[2] + ".KeyMaterial")));
                } 
                String keyName = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Rewards." + args[2] + ".KeyName"));
                List<String> lores = new ArrayList<>();
                for (String lore : getConfig().getStringList("Rewards." + args[2] + ".KeyLore"))
                  lores.add(ChatColor.translateAlternateColorCodes('&', lore)); 
                ItemStack item = new ItemStack(material, keyInt);
                if (getConfig().getString("Rewards." + args[2] + ".KeyMaterial").contains(":"))
                  item = new ItemStack(material, keyInt, (short)Integer.parseInt(getConfig().getString("Rewards." + args[2] + ".KeyMaterial").split(":")[1])); 
                if (keyInt > 0) {
                  ItemMeta itemMeta = item.getItemMeta();
                  itemMeta.setDisplayName(keyName);
                  itemMeta.setLore(lores);
                  if (!getConfig().getString("Rewards." + args[2] + ".KeyEnchantment").equals("")) {
                    String[] enchantmentAndLevel = getConfig().getString("Rewards." + args[2] + ".KeyEnchantment").split(":");
                    Enchantment enchantment = Enchantment.getByName(enchantmentAndLevel[0]);
                    int enchantmentLevel = Integer.parseInt(enchantmentAndLevel[1]);
                    itemMeta.addEnchant(enchantment, enchantmentLevel, true);
                  } 
                  item.setItemMeta(itemMeta);
                  HashMap<Integer, ItemStack> items = target.getInventory().addItem(new ItemStack[] { item });
                  if (items.size() > 0) {
                    for (ItemStack drop : items.values())
                      target.getWorld().dropItem(target.getLocation(), drop); 
                    target.sendMessage(String.valueOf(this.prefix) + ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.InventoryFull")));
                  } 
                  sender.sendMessage(String.valueOf(this.prefix) + ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.Give")
                        .replace("%cratekeys%", args[3])
                        .replace("%player%", target.getName())
                        .replace("%tier%", args[2])));
                  target.sendMessage(String.valueOf(this.prefix) + ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.Recieve")
                        .replace("%cratekeys%", args[3])
                        .replace("%tier%", args[2])));
                } else {
                  sender.sendMessage(String.valueOf(this.prefix) + ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.InvalidNumber")));
                } 
              } catch (NumberFormatException event) {
                event.printStackTrace();
                sender.sendMessage(String.valueOf(this.prefix) + ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.InvalidNumber")));
              } 
            } else {
              sender.sendMessage(String.valueOf(this.prefix) + ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.InvalidTier")));
            } 
          } else {
            sender.sendMessage(String.valueOf(this.prefix) + ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.InvalidPlayer")));
          } 
        } else {
          sender.sendMessage(String.valueOf(this.prefix) + ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.NoPermission")));
        }  
    } 
    return false;
  }
}
