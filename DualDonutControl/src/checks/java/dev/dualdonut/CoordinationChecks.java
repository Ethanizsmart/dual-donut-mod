package dev.dualdonut;

import dev.dualdonut.runtime.AutomationPolicy;
import dev.dualdonut.runtime.RecoveryState;
import dev.dualdonut.runtime.VexDeathGate;
import dev.dualdonut.runtime.SaleScreenRules;
import dev.dualdonut.runtime.PlayerDetectionRules;
import dev.dualdonut.runtime.AntiPlayerState;
import dev.dualdonut.runtime.Notifications;
import com.google.gson.JsonParser;
import java.util.List;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/** No game launch, network, credentials, or original mod initialization. */
public final class CoordinationChecks {
    private static int checks;
    private static final Object PLAYER = new Object(), WORLD = new Object();

    public static void main(String[] args) throws Exception {
        vexGate();
        recoveryFlow();
        recoveryResets();
        arbitration();
        saleTitles();
        protection();
        configuration();
        mixinContracts();
        System.out.println("Dual Donut coordination checks passed: " + checks);
    }

    private static void vexGate() {
        var gate = new VexDeathGate();
        check(gate.update(true, true, 0, 0), "ordinary death need not wait for nonexistent Vex");
        gate.reset();
        check(!gate.update(false, true, 1, 0), "record live Vex before death");
        check(!gate.update(true, true, 0, 100), "recent Vex holds death screen");
        check(!gate.update(true, true, 0, 400), "stable interval not reached");
        check(!gate.update(true, true, 0, 600), "still collecting clear scans");
        check(!gate.update(true, true, 0, 800), "four scans insufficient");
        check(gate.update(true, true, 0, 1100), "five scans and one second clear permits respawn");
        check(!gate.update(true, true, 1, 1200), "returning Vex closes gate again");
        check(!gate.update(true, false, 0, 2000), "failed scan is never clear evidence");
        for (int i = 0; i < 4; i++) check(!gate.update(true, true, 0, 3000 + i * 300), "rescan after failure");
        check(gate.update(true, true, 0, 4200), "fresh stable scans reopen gate");
        gate.reset();
        check(!gate.update(true, false, 0, 1000), "missing world holds gate even without seen Vex");
        check(!gate.update(true, true, 0, 1200), "first clear sample");
        check(!gate.update(true, true, 0, 100), "clock rollback restarts stable interval");
        check(!gate.update(true, true, 0, 1100), "two samples insufficient after rollback");
        gate.reset();
        check(gate.update(true, true, 0, 0), "reset clears prior Vex history");
    }

    private static RecoveryState.Action tick(RecoveryState state, boolean dead, boolean ready, long now) {
        return state.tick(dead, true, 0, ready, PLAYER, WORLD, now, true, 2000, 1500, 8000);
    }

    private static void recoveryFlow() {
        var state = new RecoveryState();
        check(tick(state, true, false, 0) == RecoveryState.Action.NONE && state.pending(), "death starts blocking");
        check(state.startedThisTick(), "death initialization is reported");
        check(tick(state, true, false, 1999) == RecoveryState.Action.NONE, "respawn delay enforced");
        check(tick(state, true, false, 2000) == RecoveryState.Action.RESPAWN, "one respawn request");
        check(tick(state, true, false, 2001) == RecoveryState.Action.NONE, "no duplicate respawn per tick");
        check(tick(state, true, false, 7000) == RecoveryState.Action.RESPAWN, "retry after five seconds");
        check(tick(state, false, false, 7500) == RecoveryState.Action.NONE, "missing world blocks home");
        check(tick(state, false, true, 8000) == RecoveryState.Action.NONE, "start world-ready delay");
        check(tick(state, false, true, 9499) == RecoveryState.Action.NONE, "home delay enforced");
        check(tick(state, false, true, 9500) == RecoveryState.Action.HOME, "send home once");
        check(tick(state, false, true, 9501) == RecoveryState.Action.NONE, "no duplicate home");
        check(tick(state, false, true, 17499) == RecoveryState.Action.NONE && state.pending(), "keep modules paused during settle");
        check(tick(state, false, true, 17500) == RecoveryState.Action.COMPLETE && !state.pending(), "resume after settle");
        check(tick(state, false, true, 20000) == RecoveryState.Action.NONE, "completed recovery stays idle");
    }

    private static void recoveryResets() {
        var state = new RecoveryState();
        tick(state, true, false, 0);
        tick(state, false, true, 2000);
        Object changedPlayer = new Object();
        check(state.tick(false, true, 0, true, changedPlayer, WORLD, 3400, true, 2000, 1500, 8000)
            == RecoveryState.Action.NONE, "player replacement restarts home delay");
        check(state.tick(false, true, 0, true, changedPlayer, new Object(), 4900, true, 2000, 1500, 8000)
            == RecoveryState.Action.NONE, "world replacement restarts home delay");
        check(tick(state, true, false, 5000) == RecoveryState.Action.NONE && state.startedThisTick(), "second death restarts sequence");
        check(tick(state, true, false, 6999) == RecoveryState.Action.NONE, "second death has its own delay");
        check(tick(state, true, false, 7000) == RecoveryState.Action.RESPAWN, "second death can respawn");
        state.reset();
        check(!state.pending() && !state.waitingForVex(), "disconnect/master off clears recovery");
        check(tick(state, false, true, 20000) == RecoveryState.Action.NONE, "no stale home after reconnect");
        check(state.tick(true, false, 0, false, PLAYER, WORLD, 0, true, 0, 0, 0) == RecoveryState.Action.NONE
            && state.waitingForVex(), "unknown scan blocks immediate respawn");
        check(state.tick(true, false, 0, false, PLAYER, WORLD, 1, false, 0, 0, 0) == RecoveryState.Action.RESPAWN,
            "Vex check switch is respected");
        state.reset();
        check(state.tick(true, true, 0, false, null, null, 0, true, 0, 0, 0) == RecoveryState.Action.NONE,
            "no player means no respawn call");
    }

    private static void arbitration() {
        var off = new AutomationPolicy(false, false, false, false, false);
        check(!off.use(true) && !off.sell(true, false, false) && !off.home(true, false, false, false), "master off stops all engines");
        var ready = new AutomationPolicy(true, false, false, false, false);
        check(ready.use(true) && ready.sell(true, false, false) && ready.home(true, false, false, false), "selected modules run when ready");
        check(!ready.use(false) && !ready.sell(false, false, false) && !ready.home(false, false, false, false), "independent module selections");
        for (var blocked : new AutomationPolicy[] {
            new AutomationPolicy(true, true, false, false, false),
            new AutomationPolicy(true, false, true, false, false),
            new AutomationPolicy(true, false, false, true, false),
            new AutomationPolicy(true, false, false, false, true)
        }) check(!blocked.use(true) && !blocked.sell(true, false, false) && !blocked.home(true, false, false, false), "shared pause stops every engine");
        check(!ready.sell(true, true, false), "auction owns container before sell");
        check(ready.sell(true, false, false), "sword FREE_SPACE phase may yield to sell");
        check(!ready.sell(true, false, true), "never move items in unrelated containers");
        check(!ready.home(true, true, false, false), "do not teleport during sword recovery");
        check(!ready.home(true, false, true, false), "do not teleport during sale");
        check(!ready.home(true, false, false, true), "do not teleport while GUI open");
        check(ready.homeTimer(true), "home countdown keeps its normal pace while waiting for container ownership");
        check(ready.sell(true, false, false) && !ready.use(false), "sell can run with AutoUse disabled");
    }

    private static void saleTitles() {
        check(SaleScreenRules.matches("Sell Items", "sell|bán"), "English sell title");
        check(SaleScreenRules.matches("§aBÁN VẬT PHẨM", "sell|bán"), "Vietnamese sale title ignores formatting, accents and case");
        check(SaleScreenRules.matches("BAN VAT PHAM", "sell|bán"), "unaccented Vietnamese title");
        check(!SaleScreenRules.matches("Chest", "sell|bán"), "never claim a chest just after /sell");
        check(!SaleScreenRules.matches("Auction (Page 1)", "sell|bán"), "never claim auction as sale");
        check(!SaleScreenRules.matches("Inventory", "sell|bán"), "never claim player inventory");
        check(!SaleScreenRules.matches("Chest", "| |"), "empty keywords match nothing");
        check(!SaleScreenRules.matches(null, "sell"), "null title matches nothing");
        check(!SaleScreenRules.matches("Sell", null), "null keywords match nothing");
        check(SaleScreenRules.matches("Trade All", "sell|trade"), "custom server title can be configured");
    }

    /** Validate every external mixin target and the command call against the actual linked JARs. */
    private static void mixinContracts() throws IOException {
        if (System.getProperty("dualdonut.side").equals("use")) {
        method("dev/autouse/AutoUseMod", "tick", "()V");
        field("dev/autouse/AutoUseMod", "toggleKey", "Lnet/minecraft/class_304;");
        field("dev/autouse/AutoUseMod", "settingsKey", "Lnet/minecraft/class_304;");
        field("dev/autouse/MasterAutomation", "enabled", "Z");
        method("dev/autouse/DeathRecoveryRuntime", "tick", "(Lnet/minecraft/class_310;J)V");
        method("dev/autouse/DeathRecoveryRuntime", "reset", "()V");
        method("dev/autouse/api/AutoUseApi", "isAutoSellPaused", "()Z");
        method("dev/autouse/api/AutoUseApi", "needsInventorySpace", "()Z");
        method("dev/autouse/api/AutoUseApi", "pauseReason", "()Ljava/lang/String;");
        method("dev/autouse/AutoSellWebhookBridge", "insufficientFunds", "(Ljava/lang/String;J)V");
        check(ClassLoader.getSystemResource("hoshin/autosell/Autosell.class") == null, "Use classpath contains no AutoSell");
        } else {
        method("hoshin/autosell/AutoSellLogic", "onTick", "(Lnet/minecraft/class_310;)V");
        method("hoshin/autosell/Autosell", "processChatMessage", "(Ljava/lang/String;)V");
        method("hoshin/autosell/AutoSellGui", "createGui", "(Lnet/minecraft/class_437;)Lnet/minecraft/class_437;");
        check(ClassLoader.getSystemResource("dev/autouse/AutoUseMod.class") == null, "Sell classpath contains no AutoUse");
        int[] calls = {0};
        new ClassReader("hoshin/autosell/AutoSellLogic").accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                if (!name.equals("onTick")) return null;
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                        if (owner.equals("net/minecraft/class_634") && name.equals("method_45730")
                            && descriptor.equals("(Ljava/lang/String;)V")) calls[0]++;
                    }
                };
            }
        }, 0);
        check(calls[0] == 1, "sale ownership hook matches exactly one command call");
        }
        check(ClassLoader.getSystemResource("net/autohomeafk/core/AutoHomeEngine.class") == null, "no AutoHome dependency on either side");
    }

    private static void protection() {
        var trusted = List.of("Friend", "  Ally ");
        check(PlayerDetectionRules.trusted("friend", trusted), "trusted names are case-insensitive");
        check(PlayerDetectionRules.trusted("ALLY", trusted), "trusted names trim whitespace");
        check(!PlayerDetectionRules.threat("Self", true, true, false, 0, 5, trusted), "ignore self");
        check(!PlayerDetectionRules.threat("Dead", false, false, false, 1, 5, trusted), "ignore dead players");
        check(!PlayerDetectionRules.threat("Spectator", false, true, true, 1, 5, trusted), "ignore spectators");
        check(!PlayerDetectionRules.threat("Friend", false, true, false, 1, 5, trusted), "ignore trusted player");
        check(PlayerDetectionRules.threat("Stranger", false, true, false, 25, 5, trusted), "include radius boundary");
        check(!PlayerDetectionRules.threat("Stranger", false, true, false, 25.01, 5, trusted), "exclude beyond radius");
        check(!PlayerDetectionRules.threat("Stranger", false, true, false, Double.NaN, 5, trusted), "invalid positions are not evidence");
        check(!PlayerDetectionRules.threat("Stranger", false, true, false, 1, Double.NaN, trusted), "invalid radius is rejected");
        check(!PlayerDetectionRules.threat("", false, true, false, 1, 5, trusted), "ignore empty player names");
        check(List.of("Friend", "Stranger").stream().anyMatch(name -> PlayerDetectionRules.threat(name, false, true, false, 1, 5, trusted)),
            "friend standing beside stranger must not mask stranger");
        var blocked = new AutomationPolicy(true, false, false, false, false, true);
        check(!blocked.use(true) && !blocked.sell(true, false, false) && !blocked.home(true, false, false, false), "protection pauses every automation");
        var state = new AntiPlayerState();
        state.start(0);
        check(state.blocking(), "AntiPlayer immediately blocks automation");
        check(state.tick(0, false, false, false, true, 1000) == AntiPlayerState.Action.THROW_PEARL, "first pearl on initial tick");
        state.pearlResult(true);
        check(state.tick(249, false, false, false, true, 1000) == AntiPlayerState.Action.NONE, "wait between pearls");
        check(state.tick(250, false, false, false, true, 1000) == AntiPlayerState.Action.THROW_PEARL, "second pearl once");
        state.pearlResult(true);
        check(state.tick(251, false, false, false, true, 1000) == AntiPlayerState.Action.NONE, "no third pearl");
        check(state.tick(2499, false, false, false, true, 1000) == AntiPlayerState.Action.NONE, "enforce pre-kill delay");
        check(state.tick(2500, false, false, false, true, 1000) == AntiPlayerState.Action.KILL, "send kill once");
        check(state.tick(2600, false, false, false, true, 1000) == AntiPlayerState.Action.NONE, "no repeated kill");
        check(!state.canConfirm(3499) && state.canConfirm(3500), "wait before GUI confirmation");
        check(state.tick(7500, false, false, false, true, 1000) == AntiPlayerState.Action.CONFIRM_TIMEOUT, "bounded confirmation timeout");
        check(state.phase() == AntiPlayerState.Phase.WAIT_CLEAR && state.blocking(), "timeout still waits for clear area");
        state.tick(8000, false, true, false, false, 1000);
        check(state.blocking(), "failed scan cannot resume automation");
        state.tick(8500, false, true, true, true, 1000);
        check(state.blocking(), "remaining stranger blocks resume");
        state.tick(9000, false, true, true, false, 1000);
        state.tick(11000, false, false, true, false, 1000);
        check(state.blocking(), "cached scan cannot prove stable clearance");
        state.tick(11000, false, true, true, false, 1000);
        check(!state.blocking(), "fresh stable clear scan resumes selected automation");
        state.start(0); state.tick(0, false, false, false, true, 1000); state.pearlResult(false);
        state.tick(250, false, false, false, true, 1000); state.pearlResult(false);
        check(state.phase() == AntiPlayerState.Phase.WAIT_CLEAR, "two missing pearls skip kill");
        check(state.tick(10000, false, false, false, true, 1000) != AntiPlayerState.Action.KILL, "never kill after two failed pearl attempts");
        state.start(0); state.tick(0, true, false, false, true, 1000);
        check(state.phase() == AntiPlayerState.Phase.WAIT_CLEAR, "death interrupts kill sequence for Vex recovery");
        state.reset();
        check(!state.blocking(), "master off/disconnect cancels AntiPlayer");
        state.start(0); state.tick(0, false, false, false, true, 1000); state.tick(250, false, false, false, true, 1000);
        state.tick(2500, false, false, false, true, 1000); state.confirmed();
        check(state.phase() == AntiPlayerState.Phase.WAIT_CLEAR, "GUI confirmation does not resume prematurely");
        var payload = JsonParser.parseString(Notifications.payload("Test", "Quote \" slash \\ newline\n @everyone")).getAsJsonObject();
        check(payload.getAsJsonObject("allowed_mentions").getAsJsonArray("parse").isEmpty(), "webhook mentions disabled");
        check(payload.getAsJsonArray("embeds").get(0).getAsJsonObject().get("description").getAsString().contains("\n @everyone"), "webhook JSON preserves and escapes message");
    }

    private static void configuration() throws Exception {
        var cfg = new ControlConfig();
        check(!cfg.autoDisconnect && !cfg.antiPlayer, "protection requires selecting a mode in GUI");
        check(cfg.automationEnabled && cfg.recoveryEnabled, "automation and recovery selected by default");
        cfg.disconnectRadius = Double.NaN; cfg.antiPlayerRadius = 999; cfg.respawnDelayMs = -10;
        cfg.trustedPlayers = null; cfg.detectionLog = null; cfg.homeTolerance = Double.POSITIVE_INFINITY;
        cfg.killConfirmTitles = ""; cfg.recoveryHomeCommand = "///home 2";
        var validate = ControlConfig.class.getDeclaredMethod("validate"); validate.setAccessible(true); validate.invoke(cfg);
        check(cfg.disconnectRadius == 30 && cfg.antiPlayerRadius == 128, "sanitize invalid detection ranges");
        check(cfg.respawnDelayMs == 0 && cfg.homeTolerance == 2, "sanitize recovery and home ranges");
        check(cfg.trustedPlayers != null && cfg.detectionLog != null, "null lists are repaired");
        check(cfg.killConfirmTitles.equals("kill|suicide|tự sát") && cfg.recoveryHomeCommand.equals("home 2"), "repair blank confirm titles and normalize commands");
    }

    private static void method(String owner, String name, String descriptor) throws IOException {
        check(members(owner, true).containsKey(name + descriptor), "method contract: " + owner + "." + name);
    }
    private static void field(String owner, String name, String descriptor) throws IOException {
        check(members(owner, false).containsKey(name + descriptor), "field contract: " + owner + "." + name);
    }
    private static Map<String, Integer> members(String owner, boolean methods) throws IOException {
        var members = new HashMap<String, Integer>();
        new ClassReader(owner).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                if (methods) members.put(name + descriptor, access);
                return null;
            }
            @Override public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                if (!methods) members.put(name + descriptor, access);
                return null;
            }
        }, ClassReader.SKIP_CODE);
        return members;
    }
    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
}
