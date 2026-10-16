# Auto Bounty origin (Sell only)

Behavior ported from the existing source under
`D:/DonutUtil/restored-donututility/src/main/java/dev/autoominous/`:
`BountyRuntime.java`, `BountyMoney.java`, `BountySettings.java`, and
`BountyMinecraftBridge.java`. No decompilation was performed for this port.

The matching DonutUtility 1.1.8 reference JAR is preserved at
`AutoUseMod/reference/donututility-1.1.8-fix-hold-rmb.jar`, SHA-256
`5e4d7eb193f06c0c316746a240e0ecc850d6a5c676407ff31afc83c1312d8c40`.
Original third-party code retains its own license/ownership.

Preserved behavior: balance request, minimum $1M, round down in 100k units,
`bounty add <target> <amount>`, Confirm Bounty/Confirm/lore recognition,
success-message acknowledgement, interval/retry/command/confirmation delays.

Adaptations: Sell-only YACL GUI/JSON, direct Intermediary calls instead of
reflection/MaLiLib, safe handoff from Shion AutoSell, shared protection/recovery/
home interlocks, only close an owned handler with an empty cursor, reject stale
confirmation candidates and require matching target AND amount in success replies.
Tooltip failures do not authorize a click. Retry backoff releases Sell/Home.

No full DonutUtility, AutoTPA, AutoPay, statistics or MaLiLib is bundled.
Farm/Use contains none of this feature's runtime, settings or GUI classes.
