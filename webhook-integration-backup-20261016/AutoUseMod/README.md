# AutoUseMod

Mod Fabric client riêng cho **Minecraft 1.21.11 / Java 21+**, tách từ
`donututility-1.1.8-fix-hold-rmb.jar` và source trong `D:\DonutUtil\restored-donututility`.
Mod ID: `autousemod`. Package: `dev.autouse`. Có toàn bộ source Java và Gradle wrapper.

## Cài và dùng

1. Cài Fabric Loader **0.18.4+** và Fabric API **0.141.3+1.21.11** cho Minecraft 1.21.11.
2. Chép **`AutoUseMod-1.0.0.jar`** vào thư mục `mods` của profile đó.
3. Vào game, nhấn **F8** để bật/tắt; **F9** mở bảng cài đặt.
   Có thể đổi hai phím trong Options → Controls → Key Binds → AutoUseMod.

Master luôn **OFF khi khởi động game**; nhấn F8 để chạy. Các lựa chọn riêng được lưu tại
`config/autousemod.json`. Không cần MaLiLib. Dùng bản tách thay cho bản DonutUtility đầy đủ
để tránh hai bản cùng chạy các tính năng đã tách. JAR trong `reference` chỉ là bản gốc lưu đối chiếu.

## Tính năng

- **Hold RMB:** ưu tiên cố định **Ominous Bottle → Firework Rocket → kiếm vanilla**.
  Tự chọn trên hotbar. Khi món ưu tiên hơn chỉ có trong túi, chuyển nó lên hotbar
  bằng SWAP, giữ lại món bị đổi chỗ: kiếm vào ô 1, bình ô 2, pháo ô 3.
  Đợi ba tick sau đổi ô, kiểm tra lại vật phẩm tay chính rồi mới dùng.
  Chỉ giữ RMB và gửi thao tác dùng tay chính với đúng ba nhóm trên; không tự dùng
  banner, block, rìu, potion, chai thường hay vật phẩm tay phụ.
  Nhả RMB khi mở GUI, chết, mua kiếm, hồi phục, AutoSell báo đang bận hoặc tắt master.
- **AutoSword:** khi không có **kiếm kim cương có enchant Looting III thực sự**
  trong 36 ô túi/hotbar liên tục **10 giây**, mở `/ah sword looting 3`.
  Không tính tên/lore giả enchant. Bộ đếm độc lập với AutoRecovery; có kiếm lại,
  đổi player/world, disconnect hoặc tắt master sẽ hủy lần theo dõi cũ.
  Chọn bộ lọc **Lowest Price** theo màu tooltip của hopper (slot 47), rồi chọn
  kiếm hợp lệ rẻ nhất ở trang đầu đã sắp xếp. Giữ giới hạn **$5,000,000** của bản gốc.
  Xác nhận cả vật phẩm, enchant và giá ở màn hình xác nhận; chờ server cập nhật
  kiếm vào túi trước khi chuyển lên hotbar, không bấm Yes lặp khi đang chờ.
  Hỗ trợ cả GUI dạng rương và hộp thoại Yes/No. Không có hàng hợp lệ hoặc timeout
  thì chờ 2–3 giây trước lần tìm tiếp theo.
- **AutoRecovery + Vex entity check:** theo dõi Vex sống trong danh sách entity
  client đang tải. Nếu chết khi có Vex hoặc trong vòng ba giây từ lần thấy Vex,
  giữ màn hình chết, nhả RMB và báo tạm dừng AutoSell. Chỉ cho respawn khi có ít
  nhất năm lần quét thành công không còn Vex, ổn định tối thiểu một giây.
  Không quét được entity/world được coi là chưa biết, tiếp tục chờ.
  Vex xuất hiện lại trước respawn sẽ chặn lại. Đây là nhận diện nguy cơ theo entity
  như bản gốc, không khẳng định Vex là sát thủ dựa trên death message.
  Khi đã an toàn: respawn → chờ player/world sẵn sàng → `/home 1` → chờ ổn định
  rồi cho các automation tiếp tục. Chết tiếp sẽ khởi động lại quy trình.

Delay mặc định: respawn 2000 ms, sau respawn chờ 1500 ms để `/home 1`, sau home
chờ 8000 ms. Có thể sửa ba giá trị này trong JSON khi game đã đóng.
Các switch Hold RMB, AutoSword, AutoRecovery, Vex check chỉnh được bằng F9.

## AutoSell tách riêng

AutoUseMod cung cấp API **`dev.autouse.api.AutoUseApi`** để mod AutoSell được tách
tiếp theo phối hợp tạm dừng/tiếp tục và quyền dùng container.
Xem **[docs/AUTOSELL_INTEGRATION.md](docs/AUTOSELL_INTEGRATION.md)** để nối mod đó.

Trong lúc chờ Vex, chết, về home hoặc mua kiếm, API báo pause; có thêm chặn lệnh
`sell` qua đường gửi command thông thường. Chặn command không thay thế việc nối
API vào state machine AutoSell đang chạy. Khi túi đầy và cần mua kiếm, API nhường
AutoSell dọn chỗ, rồi khóa lại trước khi vào /ah. AutoUseMod không có engine bán
đồ; khi chưa có AutoSell đi kèm, cần tự dọn chỗ nếu túi đầy.

Không chứa AutoTPA, AutoBounty, AutoPay, webhook, Home Position Guard, thống kê,
AutoReconnect hoặc engine AutoSell của mod đầy đủ.

## Build và chỉnh source

Mở **thư mục chứa README này** bằng IntelliJ/VS Code và import Gradle project.
Dùng JDK 21 để build; Gradle wrapper tự tải Gradle 9.4.0 và kiểm tra SHA-256.

```powershell
# Trỏ JAVA_HOME tới JDK 21 trên máy nếu cần (chỉ có hiệu lực trong terminal hiện tại).
.\gradlew.bat --version
.\gradlew.bat compileJava --rerun-tasks
.\gradlew.bat build
```

Đầu ra:

- Runtime: `build/libs/AutoUseMod-1.0.0.jar`
- Source JAR: `build/libs/AutoUseMod-1.0.0-sources.jar` (không cài vào mods)
- Source chính: `src/main/java/dev/autouse`
- Kiểm tra offline: `src/checks/java/dev/autouse`

| Chỉnh nội dung | Source |
|---|---|
| Tick, hotkey, reset | `AutoUseMod.java`, `MasterAutomation.java` |
| Config/GUI | `AutoUseConfig.java`, `AutoUseScreen.java` |
| RMB, nhận diện món, ưu tiên | `HoldUseRuntime.java`, `UseItemRules.java` |
| Chờ 10 giây, tìm/mua kiếm | `MissingSwordTimer.java`, `SwordRecoveryRuntime.java`, `AuctionSwordRules.java` |
| Chết, chờ Vex, home | `DeathRecoveryRuntime.java`, `VexDeathGate.java` |
| Kết nối AutoSell | `api/AutoUseApi.java` |
| Hook game / dialog / chặn sell | `mixin/` |

Source giữ tên Minecraft **Intermediary** (`class_*`, `method_*`, `field_*`)
để tái dùng phần logic đã khôi phục; `build.gradle` tạo identity mappings tương ứng.
Chưa đổi sang Yarn/Mojang mappings. Loom 1.16.3, Loader 0.18.4 và Fabric API
0.141.3+1.21.11 được ghim trong build; compiler xuất bytecode Java 21.

## Xác minh

`build` có hai nhóm kiểm tra thực sự: `verifyAuctionSwordRules` (54 ca) và
`verifyAutomationRules` (42 ca). Chúng kiểm tra mốc 10 giây/reset, parse và giới
hạn giá, chọn filter, khớp giá xác nhận, allowlist/ưu tiên vật phẩm và chờ Vex
với scan lỗi, Vex quay lại, reset và thời gian quay lùi.

**Chưa chạy Minecraft, kết nối server, mua kiếm hoặc kiểm tra tích hợp với mod
AutoSell riêng.** Các kiểm tra offline không chứng minh server đã gửi kiếm hay
teleport thành công. `/home 1` hiện giữ delay như bản gốc, không có xác nhận chat
teleport. Entity check chỉ quan sát được Vex client đã tải; UI/tooltip /ah phụ
thuộc giao diện server giống mod gốc.
