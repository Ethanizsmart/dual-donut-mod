# Dual Donut Control 2.0.0

- **Phía dùng farm:** cài đặt **AutoUseMod 1.0.1** và **DualDonutControl-Use-2.0.0.jar**.
  Có thể dùng gói `DualDonutControl-Use-2.0.0-install.zip` chứa đúng hai mod.
- **Phía sell:** cài đặt **AutoSell Shion 1.3.3-webhook.1** và **DualDonutControl-Sell-2.0.0.jar**.
  Có thể dùng gói `DualDonutControl-Sell-2.0.0-install.zip` chứa đúng hai mod.

Minecraft 1.21.11, Java 21, Fabric Loader 0.19.3+, Fabric API 0.141.6+1.21.11 và
YACL 3.8.1+1.21.11-fabric. Fabric API/YACL không nằm trong hai gói cài chính.

**Nếu profile chưa có YACL, bắt buộc tải và cài thêm
[YACL 3.8.1 cho Fabric / Minecraft 1.21.11](https://modrinth.com/mod/yacl/version/NWFsMIGn).**
Chép `yet_another_config_lib_v3-3.8.1+1.21.11-fabric.jar` vào `mods` của cả profile
Use và profile Sell. Nếu đã có YACL tương thích thì không cài trùng.

Tải JAR/ZIP từ [GitHub Release v2.0.0](https://github.com/Ethanizsmart/dual-donut-mod/releases/tag/v2.0.0).

F8 master, F9 GUI. Chọn Auto Disconnect và/hoặc AntiPlayer trong menu Phát hiện
người chơi rồi Save; cả hai mặc định OFF. Thêm tài khoản farm/sell còn lại vào
whitelist nếu đứng gần nhau. Home và Vex Recovery tích hợp ở cả hai phía,
không cần AutoHome riêng. Chỉ cài một bản Control trong từng profile.

Đã qua 121 kiểm tra Use, 115 kiểm tra Sell và kiểm tra nạp mixin riêng từng phía.
Chưa kiểm thử giao dịch, teleport, Ender Pearl và `/kill` trên server thực tế.
Xem [hướng dẫn đầy đủ](../../README.md) và `SHA256SUMS.txt` trong thư mục này.
