package dev.dualdonut.bounty;
import dev.isxander.yacl3.api.ConfigCategory;
/** Auto Bounty is tested by Control's Sell checks; these checks isolate the sale coordinator. */
public final class BountyRuntime {
    public static boolean busy() { return false; }
    public static boolean blocksSell() { return false; }
    public static void save() {}
    public static ConfigCategory category() { return null; }
}
