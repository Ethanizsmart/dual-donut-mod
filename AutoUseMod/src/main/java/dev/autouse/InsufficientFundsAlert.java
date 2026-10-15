package dev.autouse;

import java.text.Normalizer;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/** Server feedback only, while a particular sword purchase is pending. */
final class InsufficientFundsAlert {
    static final long COOLDOWN_NANOS = TimeUnit.MINUTES.toNanos(5);
    private static final Pattern FEEDBACK = Pattern.compile(
        "^(?:you (?:do not|don't|dont) have enough (?:money|funds)"
        + "|you have insufficient (?:money|funds)|insufficient (?:money|funds)"
        + "|not enough (?:money|funds)|you (?:cannot|can't|cant) afford (?:this|that)(?: item| sword| purchase)?(?=$|[.!])"
        + "|ban khong (?:co )?du tien|khong du tien)(?:[.! :].*)?$"
    );
    private boolean notified;
    private long lastNotification;

    boolean shouldNotify(String message, boolean purchasePending, long nowNanos) {
        if (!purchasePending || !matches(message)) return false;
        if (notified && nowNanos - lastNotification < COOLDOWN_NANOS) return false;
        notified = true;
        lastNotification = nowNanos;
        return true;
    }

    static boolean matches(String message) {
        if (message == null) return false;
        String text = message.replaceAll("(?i)§[0-9a-fk-or]", "").replace('’', '\'');
        text = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}+", "")
            .toLowerCase(Locale.ROOT).replace('đ', 'd').replaceAll("\\s+", " ").trim();
        text = text.replaceFirst("^(?:\\[(?:auction(?: house)?|ah|donutsmp|shop|error)\\]\\s*)+", "");
        text = text.replaceFirst("^[!⚠»›:•\\-\\s]+", "").trim();
        return FEEDBACK.matcher(text).matches();
    }
}
