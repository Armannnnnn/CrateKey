package me.arman.cratekey.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class ParticleEffect {
  private static Class<?> packetClass = null;
  
  private static Constructor<?> packetConstructor = null;
  
  private static Field[] fields = null;
  
  private static boolean netty = true;
  
  private static Field player_connection = null;
  
  private static Method player_sendPacket = null;
  
  private static HashMap<Class<? extends Entity>, Method> handles = new HashMap<>();
  
  private static boolean newParticlePacketConstructor = false;
  
  private static Class<Enum> enumParticle = null;
  
  private ParticleType type;
  
  private double speed;
  
  private int count;
  
  private float offsetX;
  
  private float offsetY;
  
  private float offsetZ;
  
  private static boolean compatible = true;
  
  static {
    String vString = getVersion().replace("v", "");
    double v = 0.0D;
    if (!vString.isEmpty()) {
      String[] array = vString.split("_");
      v = Double.parseDouble(String.valueOf(array[0]) + "." + array[1]);
    } 
    try {
      Bukkit.getLogger().info("[CrateKey] Server major/minor version: " + v);
      if (v < 1.7D) {
        Bukkit.getLogger().info("[CrateKey] Hooking into pre-Netty NMS classes");
        netty = false;
        packetClass = getNmsClass("Packet63WorldParticles");
        packetConstructor = packetClass.getConstructor(new Class[0]);
        fields = packetClass.getDeclaredFields();
      } else {
        Bukkit.getLogger().info("[CrateKey] Hooking into Netty NMS classes");
        packetClass = getNmsClass("PacketPlayOutWorldParticles");
        if (v < 1.8D) {
          Bukkit.getLogger().info("[CrateKey] Version is < 1.8 - using old packet constructor");
          packetConstructor = packetClass.getConstructor(new Class[] { String.class, float.class, float.class, float.class, 
                float.class, float.class, float.class, float.class, int.class });
        } else {
          Bukkit.getLogger().info("[CrateKey] Version is >= 1.8 - using new packet constructor");
          newParticlePacketConstructor = true;
          enumParticle = (Class)getNmsClass("EnumParticle");
          packetConstructor = packetClass.getDeclaredConstructor(new Class[] { 
                enumParticle, boolean.class, float.class, 
                float.class, float.class, float.class, float.class, float.class, float.class, int.class, 
                int[].class });
        } 
      } 
    } catch (Exception ex) {
      ex.printStackTrace();
      Bukkit.getLogger().severe("[CrateKey] Failed to initialize NMS components!");
      compatible = false;
    } 
  }
  
  public ParticleEffect(ParticleType type, double speed, int count, float offsetX, float offsetY, float offsetZ) {
    this.type = type;
    this.speed = speed;
    this.count = count;
    this.offsetX = offsetX;
    this.offsetY = offsetY;
    this.offsetZ = offsetZ;
  }
  
  public double getSpeed() {
    return this.speed;
  }
  
  public int getCount() {
    return this.count;
  }
  
  public float getOffsetX() {
    return this.offsetX;
  }
  
  public float getOffsetY() {
    return this.offsetY;
  }
  
  public float getOffsetZ() {
    return this.offsetZ;
  }
  
  public void sendToLocation(Location location) {
    try {
      Object packet = createPacket(location);
      byte b;
      int i;
      Player[] arrayOfPlayer;
      for (i = (arrayOfPlayer = Bukkit.getOnlinePlayers()).length, b = 0; b < i; ) {
        Player player = arrayOfPlayer[b];
        sendPacket(player, packet);
        b++;
      } 
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }
  
  private Object createPacket(Location location) {
    try {
      Object packet;
      if (this.count <= 0)
        this.count = 1; 
      if (netty) {
        if (newParticlePacketConstructor) {
          Object particleType = ((Enum[])enumParticle.getEnumConstants())[this.type.getId()];
          packet = packetConstructor.newInstance(new Object[] { 
                particleType, 
                Boolean.valueOf(true), Float.valueOf((float)location.getX()), Float.valueOf((float)location.getY()), Float.valueOf((float)location.getZ()), 
                Float.valueOf(this.offsetX), Float.valueOf(this.offsetY), Float.valueOf(this.offsetZ), 
                Float.valueOf((float)this.speed), Integer.valueOf(this.count), 
                new int[0] });
        } else {
          packet = packetConstructor.newInstance(new Object[] { this.type.getName(), 
                Float.valueOf((float)location.getX()), Float.valueOf((float)location.getY()), Float.valueOf((float)location.getZ()), 
                Float.valueOf(this.offsetX), Float.valueOf(this.offsetY), Float.valueOf(this.offsetZ), 
                Float.valueOf((float)this.speed), Integer.valueOf(this.count) });
        } 
      } else {
        packet = packetConstructor.newInstance(new Object[0]);
        byte b;
        int i;
        Field[] arrayOfField;
        for (i = (arrayOfField = fields).length, b = 0; b < i; ) {
          Field f = arrayOfField[b];
          f.setAccessible(true);
          if (f.getName().equals("a")) {
            f.set(packet, this.type.getName());
          } else if (f.getName().equals("b")) {
            f.set(packet, Float.valueOf((float)location.getX()));
          } else if (f.getName().equals("c")) {
            f.set(packet, Float.valueOf((float)location.getY()));
          } else if (f.getName().equals("d")) {
            f.set(packet, Float.valueOf((float)location.getZ()));
          } else if (f.getName().equals("e")) {
            f.set(packet, Float.valueOf(this.offsetX));
          } else if (f.getName().equals("f")) {
            f.set(packet, Float.valueOf(this.offsetY));
          } else if (f.getName().equals("g")) {
            f.set(packet, Float.valueOf(this.offsetZ));
          } else if (f.getName().equals("h")) {
            f.set(packet, Double.valueOf(this.speed));
          } else if (f.getName().equals("i")) {
            f.set(packet, Integer.valueOf(this.count));
          } 
          b++;
        } 
      } 
      return packet;
    } catch (IllegalAccessException ex) {
      ex.printStackTrace();
      Bukkit.getLogger().severe("{ParticleLib] Failed to construct particle effect packet!");
    } catch (InstantiationException ex) {
      ex.printStackTrace();
      Bukkit.getLogger().severe("{ParticleLib] Failed to construct particle effect packet!");
    } catch (InvocationTargetException ex) {
      ex.printStackTrace();
      Bukkit.getLogger().severe("{ParticleLib] Failed to construct particle effect packet!");
    } 
    return null;
  }
  
  private static void sendPacket(Player p, Object packet) throws IllegalArgumentException {
    try {
      if (player_connection == null) {
        player_connection = getHandle((Entity)p).getClass().getField("playerConnection");
        byte b;
        int i;
        Method[] arrayOfMethod;
        for (i = (arrayOfMethod = player_connection.get(getHandle((Entity)p)).getClass().getMethods()).length, b = 0; b < i; ) {
          Method m = arrayOfMethod[b];
          if (m.getName().equalsIgnoreCase("sendPacket"))
            player_sendPacket = m; 
          b++;
        } 
      } 
      player_sendPacket.invoke(player_connection.get(getHandle((Entity)p)), new Object[] { packet });
    } catch (IllegalAccessException ex) {
      ex.printStackTrace();
      Bukkit.getLogger().severe("[CrateKey] Failed to send packet!");
    } catch (InvocationTargetException ex) {
      ex.printStackTrace();
      Bukkit.getLogger().severe("[CrateKey] Failed to send packet!");
    } catch (NoSuchFieldException ex) {
      ex.printStackTrace();
      Bukkit.getLogger().severe("[CrateKey] Failed to send packet!");
    } 
  }
  
  private static Object getHandle(Entity entity) {
    try {
      if (handles.get(entity.getClass()) != null)
        return ((Method)handles.get(entity.getClass())).invoke(entity, new Object[0]); 
      Method entity_getHandle = entity.getClass().getMethod("getHandle", new Class[0]);
      handles.put(entity.getClass(), entity_getHandle);
      return entity_getHandle.invoke(entity, new Object[0]);
    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    } 
  }
  
  private static Class<?> getNmsClass(String name) {
    String version = getVersion();
    String className = "net.minecraft.server." + version + name;
    Class<?> clazz = null;
    try {
      clazz = Class.forName(className);
    } catch (ClassNotFoundException ex) {
      ex.printStackTrace();
      Bukkit.getLogger().severe("[CrateKey] Failed to load NMS class " + name + "!");
    } 
    return clazz;
  }
  
  private static String getVersion() {
    String[] array = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",");
    if (array.length == 4)
      return String.valueOf(array[3]) + "."; 
    return "";
  }
  
  public static boolean isCompatible() {
    return compatible;
  }
  
  public enum ParticleType {
    EXPLOSION_NORMAL("explode", 0, 17),
    EXPLOSION_LARGE("largeexplode", 1, 1),
    EXPLOSION_HUGE("hugeexplosion", 2, 0),
    FIREWORKS_SPARK("fireworksSpark", 3, 2),
    WATER_BUBBLE("bubble", 4, 3),
    WATER_SPLASH("splash", 5, 21),
    WATER_WAKE("wake", 6, -1),
    SUSPENDED("suspended", 7, 4),
    SUSPENDED_DEPTH("depthsuspend", 8, 5),
    CRIT("crit", 9, 7),
    CRIT_MAGIC("magicCrit", 10, 8),
    SMOKE_NORMAL("smoke", 11, -1),
    SMOKE_LARGE("largesmoke", 12, 22),
    SPELL("spell", 13, 11),
    SPELL_INSTANT("instantSpell", 14, 12),
    SPELL_MOB("mobSpell", 15, 9),
    SPELL_MOB_AMBIENT("mobSpellAmbient", 16, 10),
    SPELL_WITCH("witchMagic", 17, 13),
    DRIP_WATER("dripWater", 18, 27),
    DRIP_LAVA("dripLava", 19, 28),
    VILLAGER_ANGRY("angryVillager", 20, 31),
    VILLAGER_HAPPY("happyVillager", 21, 32),
    TOWN_AURA("townaura", 22, 6),
    NOTE("note", 23, 24),
    PORTAL("portal", 24, 15),
    ENCHANTMENT_TABLE("enchantmenttable", 25, 16),
    FLAME("flame", 26, 18),
    LAVA("lava", 27, 19),
    FOOTSTEP("footstep", 28, 20),
    CLOUD("cloud", 29, 23),
    REDSTONE("reddust", 30, 24),
    SNOWBALL("snowballpoof", 31, 25),
    SNOW_SHOVEL("snowshovel", 32, 28),
    SLIME("slime", 33, 29),
    HEART("heart", 34, 30),
    BARRIER("barrier", 35, -1),
    ITEM_CRACK("iconcrack_", 36, 33),
    BLOCK_CRACK("tilecrack_", 37, 34),
    BLOCK_DUST("blockdust_", 38, -1),
    WATER_DROP("droplet", 39, -1),
    ITEM_TAKE("take", 40, -1),
    MOB_APPEARANCE("mobappearance", 41, -1);
    
    private String name;
    
    private int id;
    
    private int legacyId;
    
    ParticleType(String name, int id, int legacyId) {
      this.name = name;
      this.id = id;
      this.legacyId = legacyId;
    }
    
    String getName() {
      return this.name;
    }
    
    int getId() {
      return this.id;
    }
    
    int getLegacyId() {
      return this.legacyId;
    }
  }
}
