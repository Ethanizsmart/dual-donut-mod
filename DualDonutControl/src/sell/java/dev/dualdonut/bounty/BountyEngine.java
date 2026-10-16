package dev.dualdonut.bounty;

/** Deterministic core; only the Sell runtime supplies Minecraft actions. */
public final class BountyEngine {
    public record Settings(boolean enabled, String target, double intervalMinutes, double retrySeconds,
                           double commandSeconds, double confirmSeconds) {
        public Settings {
            target = target == null ? "" : target.strip();
            intervalMinutes = bounded(intervalMinutes, 7, .1, 1440);
            retrySeconds = bounded(retrySeconds, 7, .1, 3600);
            commandSeconds = bounded(commandSeconds, .5, 0, 60);
            confirmSeconds = bounded(confirmSeconds, 1, 0, 60);
        }
        private static double bounded(double value, double fallback, double min, double max) {
            return Double.isFinite(value) ? Math.clamp(value, min, max) : fallback;
        }
    }
    public interface Port {
        boolean ready();
        boolean acquire();
        boolean findConfirm();
        boolean clickConfirm();
        boolean release();
        void command(String command);
    }
    public enum State { OFF, INTERVAL, BALANCE, COMMAND_DELAY, CONFIRM_GUI, CONFIRM_DELAY, SUCCESS, RETRY }
    private State state = State.OFF;
    private Settings settings;
    private long deadline, nextRun, balance = -1;
    private String amount, target = "", lastAdded = "-", status = "OFF";
    private boolean cleanup;

    public void tick(Settings selected, long now, Port port) {
        if (!selected.equals(settings)) {
            cleanup |= blocking();
            settings = selected;
            state = State.OFF;
            nextRun = now;
            amount = null;
        }
        if (!selected.enabled() || !port.ready()) {
            cleanup |= blocking();
            state = State.OFF;
            deadline = nextRun = 0;
            status = selected.enabled() ? "Tạm dừng theo Master / bảo vệ / Home" : "OFF";
            if (cleanup && port.release()) cleanup = false;
            return;
        }
        if (cleanup) {
            if (!port.release()) { status = "Đợi đồng bộ / cursor trống để nhường GUI"; return; }
            cleanup = false;
        }
        if (!BountyRules.validTarget(selected.target())) { state = State.OFF; status = "Cần tên người nhận hợp lệ"; return; }
        if (state == State.OFF) { state = State.INTERVAL; nextRun = now; }
        switch (state) {
            case INTERVAL -> {
                if (now < nextRun) return;
                if (!port.acquire()) { status = "Đợi AutoSell / GUI / AutoHome"; return; }
                target = selected.target(); amount = null;
                state = State.BALANCE; deadline = now + 5000;
                status = "Đang đọc /balance";
                port.command("balance");
            }
            case BALANCE -> { if (now >= deadline) retry("Không có phản hồi /balance", now); }
            case COMMAND_DELAY -> {
                if (now < deadline) return;
                if (!port.acquire()) { if (now >= deadline + 5000) retry("GUI khác đang mở", now); return; }
                state = State.CONFIRM_GUI; deadline = now + 5000;
                status = "Đợi Confirm Bounty";
                port.command("bounty add " + target + " " + amount);
            }
            case CONFIRM_GUI -> {
                if (now >= deadline) { retry("Không nhận được Confirm Bounty", now); return; }
                if (port.findConfirm()) {
                    state = State.CONFIRM_DELAY; deadline = now + ms(selected.confirmSeconds()); status = "Đợi trước khi xác nhận";
                }
            }
            case CONFIRM_DELAY -> {
                if (!port.findConfirm()) { retry("Menu / nút xác nhận đã thay đổi", now); return; }
                if (now >= deadline) {
                    if (port.clickConfirm()) { state = State.SUCCESS; deadline = now + 5000; status = "Đợi thông báo bounty thành công"; }
                    else retry("Không thể xác nhận an toàn", now);
                }
            }
            case SUCCESS -> { if (now >= deadline) retry("Chưa có thông báo bounty thành công", now); }
            case RETRY -> { if (now >= deadline) { state = State.INTERVAL; nextRun = now; } }
            default -> { }
        }
    }

    /** Only server game messages are passed here; a click never proves success. */
    public void message(String text, long now) {
        if (now >= deadline) return;
        if (state == State.BALANCE) {
            var value = BountyRules.balance(text);
            if (value == null) return;
            balance = value; amount = BountyRules.amount(value);
            if (amount == null) finish("Số dư dưới $1M; chờ chu kỳ sau", now);
            else { state = State.COMMAND_DELAY; deadline = now + ms(settings.commandSeconds()); status = "Đợi trước lệnh bounty"; }
        } else if (state == State.SUCCESS && BountyRules.success(text, target, amount)) {
            lastAdded = amount + " -> " + target;
            finish("Thành công: " + lastAdded, now);
        }
    }
    private void finish(String message, long now) {
        state = State.INTERVAL; nextRun = now + ms(settings.intervalMinutes() * 60); deadline = 0; cleanup = true; status = message;
    }
    public void retry(String reason, long now) {
        state = State.RETRY; deadline = now + ms(settings == null ? 7 : settings.retrySeconds());
        cleanup = true; status = reason + "; đợi thử lại";
    }
    private static long ms(double seconds) { return Math.round(seconds * 1000); }
    public boolean blocking() { return cleanup || switch (state) { case BALANCE, COMMAND_DELAY, CONFIRM_GUI, CONFIRM_DELAY, SUCCESS -> true; default -> false; }; }
    public State state() { return state; }
    public String status() { return status; }
    public String amount() { return amount == null ? "-" : amount; }
    public String target() { return target; }
    public long balance() { return balance; }
    public String lastAdded() { return lastAdded; }
    public long remainingSeconds(long now) { return Math.max(0, ((state == State.INTERVAL ? nextRun : deadline) - now + 999) / 1000); }
}
