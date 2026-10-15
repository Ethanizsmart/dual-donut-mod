# Dual Donut Control 2.0.0 — Use & Sell

**YACL là thư viện bắt buộc ở cả hai phía. Nếu profile chưa có, tải và cài thêm
[YACL 3.8.1 cho Fabric / Minecraft 1.21.11](https://modrinth.com/mod/yacl/version/NWFsMIGn),
rồi chép `yet_another_config_lib_v3-3.8.1+1.21.11-fabric.jar` vào thư mục `mods`
của từng profile Use/Sell. Hai gói ZIP không bao gồm YACL hoặc Fabric API.**

## Chọn đúng gói theo profile

| Profile | Cài đặt | File tải |
| --- | --- | --- |
| Phía dùng farm / Use | AutoUseMod 1.0.1 + DualDonutControl Use 2.0.0 | `DualDonutControl-Use-2.0.0-install.zip` |
| Phía sell / Sell | AutoSell Shion 1.3.3-webhook.1 + DualDonutControl Sell 2.0.0 | `DualDonutControl-Sell-2.0.0-install.zip` |

Mỗi ZIP chứa đúng hai mod chính, source JAR và hướng dẫn. Nếu đã cài đúng mod gốc,
có thể chỉ tải JAR Control tương ứng. Chỉ cài một bản Control trong mỗi profile;
thay bản Control cũ nếu đang nâng cấp.

## Yêu cầu cài đặt

- Minecraft **1.21.11**, Java **21**.
- Fabric Loader **0.19.3+**.
- Fabric API **0.141.6+1.21.11**.
- **YACL 3.8.1+1.21.11-fabric**, chọn đúng Minecraft 1.21.11 / Fabric; cài thêm nếu
  chưa có. Không cài trùng khi profile đã có YACL tương thích.
- Mod Menu 17.0.1 là tùy chọn.

## Nội dung bản phát hành

- Hai mod ID, dependency, cấu hình và GUI riêng theo phía Use/Sell.
- **F8** bật/tắt master; **F9** mở menu. Master luôn OFF khi mở game/ngắt kết nối.
- Cả hai phía có **Auto Disconnect**, **AntiPlayer**, whitelist người chơi,
  lịch sử phát hiện và cảnh báo webhook.
- **Vex Recovery** và **giữ mốc/home** tích hợp ở cả hai phía; không cần cài AutoHome riêng.
- Use giữ Hold RMB/AutoSword và hỗ trợ webhook thiếu tiền mua kiếm.
- Sell giữ delay, packet size, whitelist/blacklist item, điều khiển tin nhắn và
  webhook lợi nhuận của AutoSell.

Hai công tắc phát hiện người chơi mặc định OFF: bật chế độ cần dùng trong GUI rồi
Save. Nếu hai tài khoản đứng gần nhau, thêm tên tài khoản còn lại vào whitelist
của mỗi profile. Auto Disconnect được ưu tiên khi bật cả hai chế độ.

## Kiểm tra

Đã build thành công, qua **121 kiểm tra offline Use**, **115 kiểm tra offline Sell**
và kiểm tra nạp mixin riêng cho từng phía. Đã kiểm tra mỗi gói chỉ chứa hai JAR
đúng phía, không yêu cầu mod phía đối diện hoặc AutoHome. `SHA256SUMS.txt` đi kèm
để đối chiếu bốn file JAR/ZIP.

Chưa kiểm thử giao dịch, teleport, Ender Pearl và GUI `/kill` trên server thực tế.
