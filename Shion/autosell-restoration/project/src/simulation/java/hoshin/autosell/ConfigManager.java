package hoshin.autosell;

import java.util.List;

/** In-memory config: never loads or writes the player's configuration. */
public final class ConfigManager {
    public static double delay_step, delay_sleep;
    public static int delay_move = 2, packet_size = 4, state = 1;
    public static boolean enable_whitelist, enable_blacklist;
    public static List<String> whitelist = List.of(), blacklist = List.of();
    public static String webhook_url = "", msg_code = "000000";
    public static boolean msgctrl;
    public static int webhook_interval = 60;
    public static void save() {}
}
