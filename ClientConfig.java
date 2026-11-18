package com.helperclient;
import net.fabricmc.loader.api.FabricLoader;
import java.io.*; import java.nio.file.*; import java.util.*;
public final class ClientConfig {
 public enum Feature {
  FULLBRIGHT("Fullbright"), MINI_ITEMS("Mini Items"), LOW_FIRE("Low Fire"), CUSTOM_CROSSHAIR("Custom Crosshair"), TIER_TAGGER("TierTagger"), HITBOX("Hitbox (visual only)"), ARMOR_STATUS("Armor Status"), POTION_EFFECTS("Potion Effects"), HURTCAM("HurtCam"), FREELOOK("Freelook"), ZOOM("Zoom"), TOGGLE_SPRINT("Toggle Sprint"), HUD_EDITOR("HUD Editor"), CPS_COUNTER("CPS Counter"), FPS_DISPLAY("FPS Display"), COMBO_COUNTER("Combo Counter"), KEYSTROKES("Keystrokes"), COORDINATES("Coordinates"), DAMAGE_TINT("Damage Tint"), SOUND_HIT("Sound Hit"), BLOCK_OVERLAY("Block Overlay"), OPTIMIZATION("Performance Mode");
  public final String label; Feature(String label){this.label=label;}
 }
 private static final EnumSet<Feature> ON=EnumSet.of(Feature.ARMOR_STATUS,Feature.POTION_EFFECTS,Feature.ZOOM,Feature.CPS_COUNTER,Feature.FPS_DISPLAY,Feature.KEYSTROKES,Feature.COORDINATES,Feature.HUD_EDITOR);
 private static boolean loaded; private static float scale=1f, opacity=.82f;
 private static Path file(){return FabricLoader.getInstance().getConfigDir().resolve("helper-client.properties");}
 public static void load(){if(loaded)return;loaded=true;Properties p=new Properties();try(InputStream in=Files.newInputStream(file())){p.load(in);for(Feature f:Feature.values())if(p.containsKey(f.name())){if(Boolean.parseBoolean(p.getProperty(f.name())))ON.add(f);else ON.remove(f);}scale=Float.parseFloat(p.getProperty("scale","1"));opacity=Float.parseFloat(p.getProperty("opacity","0.82"));}catch(Exception ignored){}}
 public static boolean enabled(Feature f){load();return ON.contains(f);} public static void setEnabled(Feature f,boolean v){load();if(v)ON.add(f);else ON.remove(f);save();}
 public static float scale(){load();return scale;} public static void scale(float v){load();scale=Math.max(.5f,Math.min(1.5f,v));save();}
 public static float opacity(){load();return opacity;} public static void opacity(float v){load();opacity=Math.max(.25f,Math.min(1f,v));save();}
 public static void save(){Properties p=new Properties();for(Feature f:Feature.values())p.setProperty(f.name(),Boolean.toString(ON.contains(f)));p.setProperty("scale",Float.toString(scale));p.setProperty("opacity",Float.toString(opacity));try{Files.createDirectories(file().getParent());try(OutputStream out=Files.newOutputStream(file())){p.store(out,"Helper Client settings");}}catch(IOException ignored){}}
}
