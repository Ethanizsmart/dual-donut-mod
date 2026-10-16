# Nguồn hành vi và phạm vi tích hợp

- `AutoUseMod` 1.0.1: JAR/source có sẵn trong monorepo, metadata MIT. Vex gate và
  recovery Control phát triển từ hành vi của module này. JAR gốc được giữ nguyên;
  chỉ bản Use nối API/mixin vào AutoUse.
- AutoSell Shion `1.3.3-webhook.2`: build từ source có sẵn trong monorepo, giữ
  license và metadata gốc; bổ sung đóng menu sau đợt bán và API nhường quyền home.
  Bản `.1` được giữ để đối chiếu. Chỉ bản Sell nối vào engine và cấu hình gốc;
  không copy class AutoSell vào JAR Control.
- AutoHome AFK Beatrix `1.0.0`: metadata MIT, tác giả `Antigravity`. Control triển
  khai giữ mốc X/Z, delay và gửi lại lệnh trực tiếp để không cần JAR AutoHome.
- DonutUtility `donututility-1.1.8-fix-hold-rmb.jar`: tham chiếu các hành vi
  `dev.autoominous.DisconnectRuntime`, `DisconnectSettings`, hai mixin reconnect,
  và `com.seastore.auto.seaats.client.AutoSellLogic` nhánh AntiPlayer. Bản tham
  chiếu trong `AutoUseMod/reference/` có SHA-256:
  `5e4d7eb193f06c0c316746a240e0ecc850d6a5c676407ff31afc83c1312d8c40`.

Hai chế độ phát hiện được viết lại thành bộ quét/state machine chung của Control,
thay cấu hình MaLiLib bằng YACL và JSON riêng từng phía. Không đóng gói DonutUtility,
SeaAutoSell hay MaLiLib. Sửa lỗi bạn bè che người lạ, hai lần ném pearl và hạn chế
click xác nhận theo đúng tên GUI `/kill`. Các dependency và JAR gốc giữ license riêng.
