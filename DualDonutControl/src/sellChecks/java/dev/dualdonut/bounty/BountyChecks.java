package dev.dualdonut.bounty;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;

/** Deterministic Sell-only checks. No client entrypoints, game, account or network. */
public final class BountyChecks {
    private static int checks;
    private static final BountyEngine.Settings ON = new BountyEngine.Settings(true, "Receiver_1", 7, 7, .5, 1);
    private static final class Port implements BountyEngine.Port {
        boolean ready = true, saleBusy, foreign, cursor, confirm, replaced, owned, settled = true;
        int closes, clicks, acquireCalls;
        final List<String> commands = new ArrayList<>();
        @Override public boolean ready() { return ready; }
        @Override public boolean acquire() { acquireCalls++; return !saleBusy && !foreign && !cursor; }
        @Override public void command(String command) { commands.add(command); }
        @Override public boolean findConfirm() {
            if (confirm && !foreign && !replaced && !cursor) { owned = true; return true; }
            return false;
        }
        @Override public boolean clickConfirm() { if (!findConfirm()) return false; clicks++; return true; }
        @Override public boolean release() {
            if (!owned || foreign || replaced) { owned = false; return true; }
            if (cursor || !settled) return false;
            closes++; owned = false; return true;
        }
    }
    public static void main(String[] args) {
        rules(); defaults(); saleAndHomePriority(); successFlow(); responses(); stops(); retries();
        System.out.println("Auto Bounty checks passed: " + checks);
    }
    private static void rules() {
        check(BountyRules.amount(999999) == null && BountyRules.amount(-1) == null, "never bounty less than one million");
        check(BountyRules.amount(1000000).equals("1.0m"), "one million boundary");
        check(BountyRules.amount(1299999).equals("1.2m"), "round down in 100k units, preserve original behavior");
        check(BountyRules.amount(Long.MAX_VALUE).equals("9223372036854.7m"), "no overflow for a large balance");
        check(BountyRules.balance("§aYou have $1,299,999") == 1299999, "server balance with formatting");
        check(BountyRules.balance("You have $1.2m") == null && BountyRules.balance("You have $1,000,000.50") == null, "never parse a partial abbreviated or decimal balance");
        check(BountyRules.balance("You have $99999999999999999999999") == null, "overflow is not a balance");
        check(BountyRules.success("You added $1,200,000 to receiver_1's bounty", "Receiver_1", "1.2m"), "success money and case normalize");
        check(!BountyRules.success("You added $1.3m to Receiver_1's bounty", "Receiver_1", "1.2m"), "wrong success amount ignored");
        check(!BountyRules.success("You added $1.2m to Other's bounty", "Receiver_1", "1.2m"), "wrong success target ignored");
        check(BountyRules.confirmTitle("§aConfirm Bounty") && !BountyRules.confirmTitle("Sell") && !BountyRules.confirmTitle("Chest Confirm Bounty"), "exact confirm GUI only");
        check(BountyRules.confirmButton("§aConfirm", List.of("§7Click to add bounty")), "confirm button name and lore");
        check(!BountyRules.confirmButton("Confirm", List.of("Click to sell")) && !BountyRules.confirmButton("Confirm", null), "fail closed on wrong or absent lore");
        for (String invalid : new String[] {"", "a b", "a;kill", "x\nbounty", "@everyone", "a".repeat(33)})
            check(!BountyRules.validTarget(invalid), "invalid target: " + invalid.replace('\n', ' '));
    }
    private static void defaults() {
        var config = new BountyConfig();
        check(!config.enabled && config.target.isEmpty(), "new feature starts OFF with no target");
        check(config.intervalMinutes == 7 && config.retrySeconds == 7 && config.commandSeconds == .5 && config.confirmSeconds == 1, "DonutUtility timing defaults preserved");
        config.target = null; config.intervalMinutes = Double.NaN; config.retrySeconds = -10;
        config.commandSeconds = 999; config.confirmSeconds = Double.POSITIVE_INFINITY; config.validate();
        check(config.target.isEmpty() && config.intervalMinutes == 7 && config.retrySeconds == .1 && config.commandSeconds == 60 && config.confirmSeconds == 1, "sanitize invalid settings");
        var restored = new Gson().fromJson(new Gson().toJson(config), BountyConfig.class);
        check(restored.snapshot().equals(config.snapshot()), "GUI settings round-trip through JSON");
        var engine = new BountyEngine(); var port = new Port();
        engine.tick(new BountyEngine.Settings(false, "Receiver_1", 7, 7, .5, 1), 0, port);
        check(port.commands.isEmpty() && !engine.blocking(), "default OFF sends nothing");
        engine.tick(new BountyEngine.Settings(true, "invalid target", 7, 7, .5, 1), 1, port);
        check(port.commands.isEmpty() && !engine.blocking(), "invalid configured target cannot send a command");
    }
    private static void saleAndHomePriority() {
        var engine = new BountyEngine(); var port = new Port(); port.saleBusy = true;
        engine.tick(ON, 0, port);
        check(port.commands.isEmpty() && !engine.blocking(), "bounty waits without freezing an in-progress sale");
        port.saleBusy = false; port.foreign = true; engine.tick(ON, 100, port);
        check(port.commands.isEmpty() && port.closes == 0, "foreign GUI is left alone");
        port.foreign = false; port.ready = false; engine.tick(ON, 200, port);
        check(port.commands.isEmpty(), "home settle/recovery/protection has priority");
        port.ready = true; port.cursor = true; engine.tick(ON, 300, port);
        check(port.commands.isEmpty(), "cursor-held item blocks acquisition");
        port.cursor = false; engine.tick(ON, 400, port);
        check(port.commands.equals(List.of("balance")) && engine.blocking(), "acquire only after sale and interlocks release");
        for (int t = 401; t < 500; t++) engine.tick(ON, t, port);
        check(port.commands.size() == 1, "no duplicated balance request while waiting");
    }
    private static void beginConfirm(BountyEngine engine, Port port) {
        engine.tick(ON, 0, port);
        engine.message("You have $1,299,999", 100);
        engine.tick(ON, 599, port);
        check(port.commands.size() == 1, "command delay respected");
        engine.tick(ON, 600, port);
        check(port.commands.equals(List.of("balance", "bounty add Receiver_1 1.2m")), "correct target and rounded amount command");
        port.confirm = true; engine.tick(ON, 650, port);
    }
    private static void successFlow() {
        var engine = new BountyEngine(); var port = new Port(); beginConfirm(engine, port);
        engine.tick(ON, 1649, port); check(port.clicks == 0, "confirm delay respected");
        engine.tick(ON, 1650, port);
        check(port.clicks == 1 && engine.state() == BountyEngine.State.SUCCESS && engine.lastAdded().equals("-"), "click is only an attempt");
        engine.message("You added $1.2m to Other's bounty", 1700);
        engine.message("You added $9.0m to Receiver_1's bounty", 1701);
        check(engine.state() == BountyEngine.State.SUCCESS, "unrelated success messages cannot complete transaction");
        port.settled = false; engine.message("You added $1.2M to receiver_1's bounty", 1800); engine.tick(ON, 1801, port);
        check(engine.blocking() && port.closes == 0, "success waits for GUI synchronization before home/sell resume");
        port.settled = true; port.cursor = true; engine.tick(ON, 1900, port);
        check(engine.blocking() && port.closes == 0, "cursor must also be empty");
        port.cursor = false; engine.tick(ON, 2000, port);
        check(!engine.blocking() && port.closes == 1 && engine.lastAdded().equals("1.2m -> Receiver_1"), "release once after verified success");
        engine.tick(ON, 421799, port); check(port.commands.size() == 2, "seven minute interval enforced");
        engine.tick(ON, 421800, port); check(port.commands.size() == 3 && port.commands.get(2).equals("balance"), "next cycle rereads current balance");
    }
    private static void responses() {
        var engine = new BountyEngine(); var port = new Port();
        engine.message("You have $2,000,000", 0);
        engine.tick(ON, 0, port); check(engine.state() == BountyEngine.State.BALANCE, "unsolicited balance before request ignored");
        engine.message("You have $999,999", 100); engine.tick(ON, 101, port);
        check(port.commands.size() == 1 && !engine.blocking(), "low balance schedules interval without bounty/confirmation");
        engine = new BountyEngine(); port = new Port(); engine.tick(ON, 0, port);
        engine.message("You have $2,000,000", 5000);
        check(engine.state() == BountyEngine.State.BALANCE && engine.amount().equals("-"), "response outside deadline ignored");
        engine.tick(ON, 5000, port); check(engine.state() == BountyEngine.State.RETRY, "balance request has bounded timeout");
        engine = new BountyEngine(); port = new Port(); beginConfirm(engine, port);
        port.replaced = true; engine.tick(ON, 1650, port);
        check(port.clicks == 0 && engine.state() == BountyEngine.State.RETRY, "replaced confirmation handler cannot be clicked");
        engine.tick(ON, 1651, port); check(port.closes == 0 && !engine.blocking(), "cleanup leaves replacement GUI alone");
    }
    private static void stops() {
        for (boolean disable : new boolean[] { false, true }) {
            var engine = new BountyEngine(); var port = new Port(); beginConfirm(engine, port);
            port.cursor = true;
            var selected = disable ? new BountyEngine.Settings(false, "Receiver_1", 7, 7, .5, 1) : ON;
            port.ready = disable;
            engine.tick(selected, 1600, port);
            check(engine.blocking() && port.clicks == 0 && port.closes == 0, "stop waits for cursor, sends no new side effects");
            engine.message("You added $1.2m to Receiver_1's bounty", 1601);
            check(engine.lastAdded().equals("-"), "cancelled transaction ignores later success");
            port.cursor = false; engine.tick(selected, 1700, port);
            check(!engine.blocking() && port.closes == 1, "stop drains owned GUI even with master disabled");
        }
        var engine = new BountyEngine(); var port = new Port(); beginConfirm(engine, port);
        engine.tick(new BountyEngine.Settings(true, "Different", 7, 7, .5, 1), 700, port);
        check(port.clicks == 0 && port.closes == 1 && port.commands.get(port.commands.size() - 1).equals("balance"), "changing settings cancels old candidate and starts with fresh balance");
    }
    private static void retries() {
        var engine = new BountyEngine(); var port = new Port(); engine.tick(ON, 0, port);
        engine.tick(ON, 5000, port); engine.tick(ON, 5001, port);
        check(!engine.blocking() && engine.state() == BountyEngine.State.RETRY, "sale/home may run during retry backoff");
        engine.tick(ON, 11999, port); check(port.commands.size() == 1, "retry delay enforced");
        engine.tick(ON, 12000, port); engine.tick(ON, 12001, port);
        check(port.commands.equals(List.of("balance", "balance")), "retry rereads balance rather than repeating stale bounty");
        engine = new BountyEngine(); port = new Port(); beginConfirm(engine, port); engine.tick(ON, 1650, port);
        for (int t = 1651; t < 6650; t++) engine.tick(ON, t, port);
        check(port.clicks == 1, "never click repeatedly while waiting for success");
        engine.tick(ON, 6650, port); check(engine.state() == BountyEngine.State.RETRY && engine.lastAdded().equals("-"), "missing success does not count as a completed bounty");
    }
    private static void check(boolean condition, String message) { checks++; if (!condition) throw new AssertionError(message); }
}
