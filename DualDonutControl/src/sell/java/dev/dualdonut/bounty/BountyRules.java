package dev.dualdonut.bounty;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.regex.Pattern;

/** Protocol and amount rules ported from DonutUtility's BountyRuntime/BountyMoney. */
public final class BountyRules {
    private static final Pattern BALANCE = Pattern.compile("You\\s+have\\s+\\$\\s*([0-9,]+)(?![0-9,.kKmMbB])", Pattern.CASE_INSENSITIVE);
    private static final Pattern SUCCESS = Pattern.compile("You\\s+added\\s+\\$\\s*([^\\s]+)\\s+to\\s+([A-Za-z0-9_]{1,32})'s\\s+bounty", Pattern.CASE_INSENSITIVE);
    private BountyRules() {}
    public static String clean(String text) { return text == null ? "" : text.replaceAll("§.", "").strip(); }
    public static boolean validTarget(String target) { return target != null && target.matches("[A-Za-z0-9_]{1,32}"); }
    public static String amount(long balance) {
        if (balance < 1_000_000) return null;
        long tenths = balance / 100_000;
        return (tenths / 10) + "." + (tenths % 10) + "m";
    }
    public static Long balance(String text) {
        var match = BALANCE.matcher(clean(text));
        if (!match.find()) return null;
        try { return Long.parseLong(match.group(1).replace(",", "")); }
        catch (NumberFormatException ex) { return null; }
    }
    public static boolean success(String text, String target, String amount) {
        var match = SUCCESS.matcher(clean(text));
        if (!match.find() || !match.group(2).equalsIgnoreCase(target)) return false;
        var received = money(match.group(1));
        var expected = money(amount);
        return received != null && expected != null && received.compareTo(expected) == 0;
    }
    private static BigDecimal money(String text) {
        if (text == null) return null;
        text = text.replace(",", "").toLowerCase(Locale.ROOT);
        if (!text.matches("[0-9]+(?:\\.[0-9]+)?[kmb]?")) return null;
        int power = switch (text.charAt(text.length() - 1)) { case 'k' -> 3; case 'm' -> 6; case 'b' -> 9; default -> 0; };
        try { return new BigDecimal(power == 0 ? text : text.substring(0, text.length() - 1)).scaleByPowerOfTen(power); }
        catch (NumberFormatException ex) { return null; }
    }
    public static boolean confirmTitle(String title) { return clean(title).equalsIgnoreCase("Confirm Bounty"); }
    public static boolean confirmButton(String name, Iterable<String> tooltip) {
        if (!clean(name).equalsIgnoreCase("Confirm") || tooltip == null) return false;
        for (var line : tooltip) if (clean(line).toLowerCase(Locale.ROOT).contains("click to add bounty")) return true;
        return false;
    }
}
