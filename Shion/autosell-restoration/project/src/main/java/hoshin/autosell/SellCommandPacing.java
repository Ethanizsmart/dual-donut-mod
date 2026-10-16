package hoshin.autosell;

/** One outstanding request, bounded retries, and a cooldown even after a menu closes. */
public final class SellCommandPacing {
   public static final long RESPONSE_TIMEOUT_MS = 5000;
   private static final int MAX_ATTEMPTS = 3;
   private long awaitingUntil;
   private long nextAttemptAt;
   private int attempts;

   public boolean trySend(long now) {
      if (attempts >= MAX_ATTEMPTS || now < nextAttemptAt || waiting(now)) return false;
      attempts++;
      awaitingUntil = now + RESPONSE_TIMEOUT_MS;
      nextAttemptAt = awaitingUntil + RESPONSE_TIMEOUT_MS * (1L << (attempts - 1));
      return true;
   }

   public boolean waiting(long now) { return now < awaitingUntil; }
   public boolean exhausted(long now) { return attempts >= MAX_ATTEMPTS && !waiting(now); }

   public void menuReceived(long now) {
      awaitingUntil = 0;
      attempts = 0;
      nextAttemptAt = now + RESPONSE_TIMEOUT_MS;
   }

   public void reset() { awaitingUntil = nextAttemptAt = 0; attempts = 0; }
}
