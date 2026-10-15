# Dual Donut Mod

Monorepo chứa các project Fabric cho Minecraft 1.21.11 đã được khôi phục và
chỉnh sửa được. Các project dùng Java 21 và giữ tên Minecraft Intermediary
(`class_*`, `method_*`, `field_*`) trong source phục hồi.

## Project

| Đường dẫn | Mod | Mục đích |
| --- | --- | --- |
| `DualDonutControl/` | Dual Donut Control Use / Sell 2.0.0 | Hai profile riêng, GUI theo phía, master F8, Vex Recovery, giữ mốc/home và hai chế độ phát hiện người chơi. |
| `AutoUseMod/` | AutoUseMod 1.0.1 | Giữ RMB, AutoSword, AutoRecovery và tích hợp AutoSell. |
| `Shion/autosell-restoration/project/` | AutoSell Donut 1.3.3-webhook.1 | AutoSell phục hồi, cấu hình GUI và cảnh báo Discord webhook. |
| `Beatrix/autohomeafk-restoration/project/` | Auto Home AFK 1.0.0 | Giữ mốc X/Z, đếm AFK và gửi lệnh home theo chu kỳ. |
| `SwordWebhookIntegration/` | Gói bàn giao | Tài liệu và JAR cho tích hợp cảnh báo thiếu tiền mua kiếm. |

Mỗi project Gradle có README riêng kèm hướng dẫn build, dependency, phạm vi
xác minh và các điểm bắt đầu để chỉnh sửa. Các JAR gốc, source package, baseline
và báo cáo phục hồi được giữ trong các thư mục liên quan để đối chiếu provenance.

## Build

Cài JDK 21, sau đó chạy Gradle wrapper trong từng project:

```powershell
cd DualDonutControl
.\gradlew.bat packageRelease -Pside=use
.\gradlew.bat packageRelease -Pside=sell

cd ..
cd AutoUseMod
.\gradlew.bat build

cd ..\Shion\autosell-restoration\project
.\gradlew.bat build

cd ..\..\..\Beatrix\autohomeafk-restoration\project
.\gradlew.bat build
```

`DualDonutControl`, `AutoUseMod` và `Shion/autosell-restoration/project` có kiểm tra offline trong
task `check`. Không commit cache Gradle, output build, file config Minecraft,
log và dữ liệu runtime vào repository.

## Hai profile Donut Control

- **Phía dùng farm:** cài **AutoUseMod 1.0.1 + DualDonutControl Use 2.0.0** để giữ RMB
  và mua kiếm. GUI chỉ hiển thị chức năng Use cùng các bảo vệ chung.
- **Phía sell:** cài **AutoSell Shion 1.3.3-webhook.1 + DualDonutControl Sell 2.0.0**
  để tự bán đồ. GUI hiển thị delay, lọc item, tin nhắn và webhook Sell cùng các bảo vệ chung.
- Cả hai bản đều tích hợp Auto Disconnect, AntiPlayer, Vex Recovery và giữ mốc/home;
  không cần AutoHome riêng. Cài Fabric API và YACL trong mỗi profile.
- Gói cài và SHA-256: [DualDonutControl/releases/2.0.0](DualDonutControl/releases/2.0.0/).
  Chi tiết cài đặt và kiểm tra: [DualDonutControl/README.md](DualDonutControl/README.md).

## Lưu ý

- Chỉ build được kiểm chứng cho Minecraft 1.21.11 và Java 21.
- Các mod là client-side automation. Chỉ sử dụng tại server/môi trường bạn được
  phép dùng mod.
- Webhook Discord được cấu hình lúc chạy trong file config; repository không
  chứa URL webhook hay credential.
