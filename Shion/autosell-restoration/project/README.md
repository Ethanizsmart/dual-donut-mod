# AutoSell Donut 1.3.3-webhook.3 — mã nguồn phục hồi và tích hợp AutoUseMod

Phục hồi từ `Shion/autosell_donut-1.3.3.jar` bằng Vineflower 1.11.2.
Baseline phục hồi có 9 file Java chứa đủ 11 class của JAR gốc. Bản tích hợp
thêm một helper dựng JSON cảnh báo mua kiếm. Tên Minecraft Intermediary
(`class_*`, `method_*`, `field_*`) được giữ nguyên.

## Cập nhật cho Dual Donut Control 2.0.2

Bản `.3` giữ cùng menu khi bán xong, lúc scan rảnh và nghỉ `delay_sleep`, không
tự đóng rồi mở lại. Không bấm confirm nếu chưa chuyển item. Whitelist/blacklist
giữ nguyên; người dùng tự tắt whitelist nếu không muốn dùng bộ lọc.
`SellCommandPacing` chỉ cho một `/sell` đang chờ, timeout 5 giây, backoff 5/10 giây,
tối đa 3 lần thử rồi báo tắt/bật để thử lại. Menu nhận được đặt cooldown mở lại 5 giây.
API `AutoSellLogic.canYieldForHome()` giữ chặn lúc chuyển item/xác nhận;
`tickRelease()` cho Control dừng click mới và chờ đồng bộ trước khi nhường GUI,
kể cả khi engine đã OFF. Cursor vẫn phải trống trước khi Control đóng menu.
JAR `.1` và `.2` trong `SwordWebhookIntegration/` được giữ nguyên để đối chiếu.

## Build

Cài JDK 21 và trỏ `JAVA_HOME` tới thư mục JDK đó trong phiên terminal,
sau đó chạy tại thư mục chứa file này:

```powershell
.\gradlew.bat --version
.\gradlew.bat compileJava --rerun-tasks
.\gradlew.bat build
```

Trên Linux/macOS: `bash ./gradlew build` với JDK 21.

- JAR chạy: `build/libs/autosell_donut-1.3.3-webhook.3.jar`
- JAR mã nguồn: `build/libs/autosell_donut-1.3.3-webhook.3-sources.jar`
- Minecraft 1.21.11; Fabric Loader 0.19.3; Fabric API 0.141.6+1.21.11.
- YACL 3.8.1+1.21.11-fabric phục vụ GUI; Mod Menu 17.0.1 phục vụ tích hợp menu.
- Gradle 9.5.1, Loom 1.17.21, Java bytecode 21.

Các phiên bản dependency được pin để phục hồi build; không phải tất cả đều
chứng minh được là phiên bản tác giả đã dùng. YACL và Mod Menu là dependency
bên ngoài, không được nhúng vào JAR. Metadata runtime được giữ nguyên từ bản gốc
ngoại trừ version được đổi để phân biệt bản tích hợp;
đặc biệt YACL chưa được bản gốc khai báo trong `depends`.

## Cảnh báo thiếu tiền mua kiếm

Cài cùng AutoUseMod 1.0.1. Trong tab **Webhook & Thống kê**, nhập URL webhook
Discord rồi **Save**. Khi AutoSword nhận system message thiếu tiền trong lúc
chờ giao dịch, webhook gửi **Không đủ tiền mua kiếm**, tên người chơi và giá
kiếm đang mua. Không có số dư tài khoản được suy đoán hoặc gửi kèm.

Giới hạn một cảnh báo mỗi 5 phút trong cùng phiên game. Bỏ trống URL để không
gửi. Không cần bật engine bán đồ để dùng cảnh báo này. Cầu nối
`WebhookManager.notifySwordInsufficientFunds(String, long)` dùng config hiện tại;
không phụ thuộc khoảng báo cáo lợi nhuận 60 phút và không reset số liệu lợi nhuận.

`build` chạy 12 kiểm tra payload offline và 34 kiểm tra engine/coordinator bằng
menu trong bộ nhớ: 25 vòng cùng GUI, nghỉ, rảnh, response chậm, retry/backoff và
nhường home/stop/cursor/bounty. Source set `simulation` biên dịch cùng source engine và
Control Sell từ monorepo với game/config double; không đi vào runtime/source JAR.
Chưa gửi webhook thật hoặc chạy game để xác minh server.

Auto Bounty nằm riêng trong Control Sell 2.0.3, không thêm vào JAR Shion hoặc Farm.
Simulation dùng stub Bounty để cô lập coordinator bán; logic Bounty thật được
kiểm tra bởi `verifyBounty` tại project Control Sell.

## Điểm bắt đầu chỉnh sửa

| File | Vai trò |
| --- | --- |
| `Autosell.java` | Entrypoint client, phím F7/Right Shift, tick và chat |
| `AutoSellLogic.java` | Máy trạng thái bán đồ, delay, whitelist/blacklist, tạm dừng |
| `AutoSellGui.java` | Các tùy chọn YACL và nút thử webhook |
| `ConfigManager.java` | Đọc/ghi `config/hsnautosell.json`, lệnh `/atsell generate_code` |
| `WebhookManager.java` | Tính lợi nhuận và báo cáo Discord |
| `HsnAuthModule.java` | Luồng xác thực bên ngoài, định danh cục bộ |
| `ModExpiration.java` | Helper kiểm tra hết hạn; không tìm thấy caller trong mod |
| `ModMenuIntegration.java` | Mở GUI qua Mod Menu |

Build đã được kiểm tra thành công. Mixin smoke tại project Control đã qua cho cả
Use và Sell; chưa kiểm tra bán/teleport/xác thực/webhook trên server thực tế.
Code xác thực và các hành vi khác của bản gốc được giữ nguyên. Xem báo cáo
`../RESTORATION.md` trong thư mục phục hồi để biết phạm vi đối chiếu baseline
trước khi thêm cầu nối webhook ngày 16/10/2026.
