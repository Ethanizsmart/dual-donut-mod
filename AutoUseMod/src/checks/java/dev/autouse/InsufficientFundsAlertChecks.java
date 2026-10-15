package dev.autouse;

public final class InsufficientFundsAlertChecks {
    private static int checks;

    public static void main(String[] args) {
        for (String message : new String[]{
            "You do not have enough money to buy this item!", "You don't have enough money!",
            "§cYou don’t have enough money to purchase this.", "[Auction House] Insufficient funds!",
            "[AH] You cannot afford this item.", "Not enough money.", "Không đủ tiền mua kiếm",
            "Bạn không có đủ tiền để mua vật phẩm này!", "§c[Error] You have insufficient funds."
        }) check(InsufficientFundsAlert.matches(message), "Recognize server funds feedback: " + message);
        for (String message : new String[]{
            "", "Auction timed out", "Item already sold", "No swords found", "Price: $6M",
            "You have enough money", "Not enough inventory space", "<Steve> You don't have enough money!",
            "Steve: Không đủ tiền mua kiếm", "Someone said: Insufficient funds!", "You cannot afford this swordsmith",
            "You don't have enough moneybags", "This listing costs more than 5M"
        }) check(!InsufficientFundsAlert.matches(message), "Do not infer insufficient balance: " + message);
        check(!InsufficientFundsAlert.matches(null), "Null feedback is ignored");
        var gate = new InsufficientFundsAlert();
        String feedback = "You don't have enough money!";
        long start = 1000L;
        check(!gate.shouldNotify(feedback, false, start), "Unrelated server feedback does not notify or consume cooldown");
        check(!gate.shouldNotify("Item sold", true, start), "A failed purchase is not necessarily lack of money");
        check(gate.shouldNotify(feedback, true, start), "An actual pending purchase can notify immediately");
        check(!gate.shouldNotify(feedback, true, start), "Duplicate messages do not double-send");
        check(!gate.shouldNotify(feedback, true, start + InsufficientFundsAlert.COOLDOWN_NANOS - 1), "Retries remain throttled for five minutes");
        check(!gate.shouldNotify(feedback, false, start + InsufficientFundsAlert.COOLDOWN_NANOS), "Cooldown expiry alone cannot notify after cancellation");
        check(gate.shouldNotify(feedback, true, start + InsufficientFundsAlert.COOLDOWN_NANOS), "Later pending purchase can notify at cooldown boundary");
        System.out.println("Insufficient funds alerts: " + checks + " checks passed.");
    }

    private static void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }
}
