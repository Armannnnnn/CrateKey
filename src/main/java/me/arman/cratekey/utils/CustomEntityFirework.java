package me.arman.cratekey.utils;

import net.minecraft.server.v1_8_R1.Entity;
import net.minecraft.server.v1_8_R1.EntityFireworks;
import net.minecraft.server.v1_8_R1.Packet;
import net.minecraft.server.v1_8_R1.PacketPlayOutEntityStatus;
import net.minecraft.server.v1_8_R1.World;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

public class CustomEntityFirework extends EntityFireworks {
  Player[] players = null;
  
  boolean gone;
  
  public CustomEntityFirework(World world, Player... p) {
    super(world);
    this.gone = false;
    this.players = p;
    a(0.25F, 0.25F);
  }
  
  public void h() {
    if (this.gone)
      return; 
    if (!this.world.isStatic) {
      this.gone = true;
      if (this.players != null && 
        this.players.length > 0) {
        byte b;
        int i;
        Player[] arrayOfPlayer;
        for (i = (arrayOfPlayer = this.players).length, b = 0; b < i; ) {
          Player player = arrayOfPlayer[b];
          (((CraftPlayer)player).getHandle()).playerConnection.sendPacket((Packet)new PacketPlayOutEntityStatus((Entity)this, (byte)17));
          b++;
        } 
        die();
        return;
      } 
      this.world.broadcastEntityEffect((Entity)this, (byte)17);
      die();
    } 
  }
  
  public static void spawn(Location location, FireworkEffect effect, Player... players) {
    try {
      CustomEntityFirework firework = new CustomEntityFirework((World)((CraftWorld)location.getWorld()).getHandle(), players);
      FireworkMeta meta = ((Firework)firework.getBukkitEntity()).getFireworkMeta();
      meta.addEffect(effect);
      ((Firework)firework.getBukkitEntity()).setFireworkMeta(meta);
      firework.setPosition(location.getX(), location.getY(), location.getZ());
      if (((CraftWorld)location.getWorld()).getHandle().addEntity((Entity)firework))
        firework.setInvisible(true); 
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }
}
