# Webhook khi không đủ tiền mua kiếm

Bản cập nhật ngày 16/10/2026, Minecraft Fabric 1.21.11 / Java 21.

## Cài đặt

1. Thay các bản AutoUseMod và AutoSell cũ trong thư mục `mods` bằng **cả hai**
   JAR trong gói này. Mỗi mod ID chỉ giữ một bản.
2. Dùng Fabric Loader >=0.19.3, Fabric API cho 1.21.11 và YACL
   3.8.1+1.21.11-fabric. Mod Menu 17.0.1 dùng nếu muốn mở GUI qua menu mod.
3. Mở cài đặt AutoSell (Right Shift mặc định), vào **Webhook & Thống kê**,
   nhập URL Discord webhook rồi **Save**.
4. Bật AutoUseMod bằng F8 và giữ AutoSword ON. Engine AutoSell có thể OFF;
   cầu nối vẫn dùng URL đã lưu.

Khi server báo thiếu tiền trong lúc AutoSword đang chờ mua kiếm, webhook hiện:

> **Không đủ tiền mua kiếm**
>
> Người chơi: tên nhân vật
>
> Kiếm: Diamond Sword — Looting III
>
> Giá kiếm đang mua: giá listing đã chọn

Không suy đoán số dư. Cảnh báo được giới hạn một lần mỗi 5 phút trong phiên
game, kể cả thử mua lại, bật/tắt automation hoặc reconnect. Sau một lần phát
hiện bị giới hạn, không xếp hàng gửi bù; cần phản hồi thiếu tiền mới khi hết
cooldown. Bỏ trống URL thì không gửi. URL được đọc từ config AutoSell đang chạy,
không cần sao chép vào AutoUseMod. Báo cáo lợi nhuận có chu kỳ riêng.

## Điều kiện nhận diện

AutoUseMod nhận `ClientReceiveMessageEvents.GAME`, loại action bar; không đăng
ký player chat. Cảnh báo chỉ xét khi master và AutoSword bật, còn sống, đúng
player/world/connection, có listing đang chờ ở WAIT_PURCHASE hoặc VERIFY_PURCHASE,
và chưa nhận được kiếm phù hợp.

Hỗ trợ các phản hồi tiếng Anh phổ biến như `You don't have enough money`,
`Insufficient funds`, `You cannot afford this item`, và câu thiếu tiền bằng
tiếng Việt. Bỏ mã màu và các prefix đã biết. Do chưa có nguyên văn thông báo
của server, mẫu tùy biến ngoài các dạng này có thể cần thêm vào
`AutoUseMod/src/main/java/dev/autouse/InsufficientFundsAlert.java`.
Không báo thiếu tiền từ timeout, hết hàng, lỗi GUI hoặc giá vượt trần 5M.

## Cách nối và giới hạn

- AutoUseMod 1.0.1 gọi tùy chọn qua reflection tới
  `hoshin.autosell.WebhookManager.notifySwordInsufficientFunds(String, long)`
  trong AutoSell `1.3.3-webhook.1`.
- Không nhúng class của mod kia. Thiếu AutoSell hoặc API cũ không chặn luồng mua.
- Gửi HTTP bất đồng bộ qua WebhookManager sẵn có. Kết quả enqueue không khẳng
  định Discord đã nhận thành công; không có retry HTTP tự động.
- Cầu nối không đổi trần giá 5M, điều kiện Looting III, trình tự mua/confirm,
  retry mua, state AutoSell, chu kỳ lợi nhuận hay cơ chế xác thực của Shion.
- Giao thức pause/container chung giữa hai engine được mô tả riêng ở
  `AutoUseMod/docs/AUTOSELL_INTEGRATION.md`; cập nhật này thêm thông báo webhook.

## Xác minh

Cả hai dự án đã chạy `compileJava --rerun-tasks` và `build`, exit 0.

| Nhóm | Kết quả |
| --- | --- |
| Auction sword rules | 54 ca qua |
| Automation rules | 42 ca qua |
| Nhận diện thiếu tiền, ngữ cảnh, cooldown | 30 ca qua |
| Payload tiếng Việt, giá, Unicode/JSON escaping | 12 ca qua |

Tổng 138 kiểm tra offline. Không gửi HTTP trong test. Chưa khởi chạy Minecraft,
kết nối server, mua kiếm hoặc gửi tới Discord. AutoSell còn cảnh báo deprecation
có sẵn ở HsnAuthModule; build vẫn thành công.

Log: `AutoUseMod/build-webhook-compile.log`,
`AutoUseMod/build-webhook-validation.log`,
`Shion/autosell-restoration/logs/webhook-compile.log`,
`Shion/autosell-restoration/logs/webhook-build.log`.

Bản lưu source/build trước thay đổi nằm ở
`webhook-integration-backup-20261016/`. JAR gốc của Shion và các gói bàn giao
trước được giữ nguyên. Source mới nằm tại `AutoUseMod/` và
`Shion/autosell-restoration/project/`; source JAR/ZIP trong gói này phục vụ xem
mã và build lại, không cài source JAR vào `mods`.
