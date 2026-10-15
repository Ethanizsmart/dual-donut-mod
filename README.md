# Dual Donut Mod

Monorepo chứa các project Fabric cho Minecraft 1.21.11 đã được khôi phục và
chỉnh sửa được. Các project dùng Java 21 và giữ tên Minecraft Intermediary
(`class_*`, `method_*`, `field_*`) trong source phục hồi.

## Project

| Đường dẫn | Mod | Mục đích |
| --- | --- | --- |
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
cd AutoUseMod
.\gradlew.bat build

cd ..\Shion\autosell-restoration\project
.\gradlew.bat build

cd ..\..\..\Beatrix\autohomeafk-restoration\project
.\gradlew.bat build
```

`AutoUseMod` và `Shion/autosell-restoration/project` có kiểm tra offline trong
task `check`. Không commit cache Gradle, output build, file config Minecraft,
log và dữ liệu runtime vào repository.

## Lưu ý

- Chỉ build được kiểm chứng cho Minecraft 1.21.11 và Java 21.
- Các mod là client-side automation. Chỉ sử dụng tại server/môi trường bạn được
  phép dùng mod.
- Webhook Discord được cấu hình lúc chạy trong file config; repository không
  chứa URL webhook hay credential.
