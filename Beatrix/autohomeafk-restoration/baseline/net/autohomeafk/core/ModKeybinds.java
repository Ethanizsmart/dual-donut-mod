package net.autohomeafk.core;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_304.class_11900;
import net.minecraft.class_3675.class_307;

public class ModKeybinds {
   public static class_304 toggleKey;
   public static class_304 configScreenKey;
   private static final class_11900 CATEGORY = class_11900.method_74698(class_2960.method_60656("autohomeafk"));

   public static void register() {
      toggleKey = KeyBindingHelper.registerKeyBinding(new class_304("key.autohomeafk.toggle", class_307.field_1668, 72, CATEGORY));
      configScreenKey = KeyBindingHelper.registerKeyBinding(new class_304("key.autohomeafk.config", class_307.field_1668, 79, CATEGORY));
   }
}
