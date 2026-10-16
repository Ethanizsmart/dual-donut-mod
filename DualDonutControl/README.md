# Dual Donut Control — Use 2.0.2 / Sell 2.0.3

Hai mod Fabric client riêng cho **Minecraft 1.21.11 / Java 21**. Mỗi bản có GUI
YACL đúng chức năng của phía mình, master **F8**, menu **F9**, giữ mốc/home tích hợp,
Vex Recovery, Auto Disconnect và AntiPlayer từ hành vi DonutUtility cũ.

## Cài đặt theo phía

| Profile | Cài hai mod chính | Nội dung GUI |
| --- | --- | --- |
| **Phía dùng farm / Use** | **AutoUseMod 1.0.1 + DualDonutControl Use 2.0.2** | Hold RMB, AutoSword, cảnh báo thiếu tiền, bảo vệ người chơi, Vex Recovery, giữ mốc/home |
| **Phía sell / Sell** | **AutoSell Shion 1.3.3-webhook.3 + DualDonutControl Sell 2.0.3** | AutoSell, Auto Bounty và GUI cấu hình, delay, packet size, whitelist/blacklist item, điều khiển tin nhắn, báo cáo lợi nhuận, bảo vệ người chơi, Vex Recovery, giữ mốc/home |

Phía farm cài:

```text
mods/AutoUseMod-1.0.1.jar
mods/DualDonutControl-Use-2.0.2.jar
```

Phía sell cài:

```text
mods/autosell_donut-1.3.3-webhook.3.jar
mods/DualDonutControl-Sell-2.0.3.jar
```

Cả hai profile cần **Fabric Loader 0.19.3+**, **Fabric API 0.141.6+1.21.11** và
**YACL 3.8.1+1.21.11-fabric**. Mod Menu 17.0.1 là tùy chọn. Hai file ZIP cài đặt
chứa đúng hai mod chính theo từng phía; Fabric API và YACL cài riêng nếu chưa có.

**YACL là thư viện bắt buộc ở cả hai profile Use và Sell. Nếu profile chưa có YACL,
tải [YACL 3.8.1 cho Fabric / Minecraft 1.21.11](https://modrinth.com/mod/yacl/version/NWFsMIGn)
và chép `yet_another_config_lib_v3-3.8.1+1.21.11-fabric.jar` vào thư mục `mods`
của profile đó.** Chọn đúng bản Fabric 1.21.11. Không cần chép thêm nếu profile
đã có bản tương thích; YACL không được đóng gói trong hai ZIP cài đặt.

Tải hai gói cài và xem hướng dẫn dễ hiểu tại
[GitHub Release v2.0.3](https://github.com/Ethanizsmart/dual-donut-mod/releases/tag/v2.0.3).

Dùng hai thư mục/profile Minecraft riêng. Chỉ cài bản Control tương ứng trong mỗi
profile; Loader từ chối cài Use và Sell cùng nhau hoặc cùng Control 1.0.0 cũ.
Nếu hai tài khoản farm/sell đứng gần nhau, thêm tên tài khoản còn lại vào danh
sách người tin cậy của mỗi profile trước khi bật phát hiện người chơi.
Thay gói Control cũ bằng gói mới. Không cần AutoHome AFK riêng: chức năng giữ mốc X/Z
đã nằm trong cả hai bản Control; bỏ AutoHome riêng khỏi hai profile để tránh chạy
hai engine home. Không chép file `-sources.jar` vào `mods`.

Gói Sell ở `releases/2.0.3/`; Farm giữ gói `releases/2.0.2/`. Hai profile
hoạt động độc lập, không gửi lệnh giữa các tài khoản/profile.

## Phím và lưu cấu hình

- **F8**: master bật/tắt automation và các tính năng Control được chọn. Master
  luôn OFF khi mở game, ngắt kết nối hoặc chuyển server.
- **F9**: menu Control của đúng phía. Nút master ON/OFF trong GUI áp dụng ngay;
  các tùy chọn khác áp dụng khi bấm **Save**.
- Phía Use nhận cả phím F8/F9 của AutoUse mà không bị bật/tắt hai lần.
- Phía Sell nhận F7 của AutoSell như phím master, Right Shift mở menu Sell;
  cấu hình AutoSell trong Mod Menu cũng mở menu Control Sell.
- Có thể đổi phím trong Options → Controls → Key Binds.

File Control riêng theo phía:

- `config/dualdonutcontrol-use.json`: tùy chọn Use, recovery, home, phát hiện người
  chơi, whitelist người chơi, lịch sử và URL webhook của phía farm.
- `config/dualdonutcontrol-sell.json`: tùy chọn Sell, recovery, home, phát hiện người
  chơi, whitelist người chơi và lịch sử của phía bán.
- Hold RMB và AutoSword vẫn lưu trong `autousemod.json` của AutoUse.
- Cấu hình engine bán, lọc item, mã tin nhắn, URL và chu kỳ báo cáo Sell vẫn lưu
  trong `hsnautosell.json`. Cảnh báo người chơi dùng cùng URL webhook Sell.

Cấu hình Control 2.0 theo phía là file mới; thiết lập lại recovery, home và mốc
cho từng tài khoản/profile sau khi nâng từ Control 1.0.0. Hai bản không đọc config
hay cần class của mod ở phía đối diện.

## Hai chế độ phát hiện người chơi — có ở cả Use và Sell

**Auto Disconnect** quét mỗi 10 client tick, chỉ xét người chơi đang được client
tải. Phạm vi mặc định **30 block**, chỉnh được **1–128**; đo khoảng cách 3D. Bỏ qua
bản thân, người chết, spectator và tên trong danh sách tin cậy. Phát hiện người lạ
thì dừng automation, ghi lịch sử tối đa 50 tên gần nhất, gửi webhook nếu có URL,
rồi ngắt kết nối. Màn hình ngắt kết nối có **Reconnect** và **Tin cậy người vừa
phát hiện & Reconnect**. Reconnect không tự bật master lại.

**AntiPlayer / Chống người lạ** mặc định vùng **5 block**, chỉnh được **1–128**.
Khi phát hiện người lạ: nhả RMB/dừng bán hoặc mua kiếm, thử ném Ender Pearl ở tay
trái hai lần cách nhau 250 ms, chờ rồi gửi `/kill` một lần. Nếu cả hai lần đều
không có pearl, bỏ bước `/kill` và vẫn chờ vùng an toàn. Nếu server có GUI xác
nhận, chờ ít nhất một giây và chỉ click vật phẩm xác nhận của luồng `/kill` trong
GUI có tên đúng từ khóa cấu hình. Sau năm giây không xác nhận được, kết thúc phần
thao tác và tiếp tục chờ người lạ rời đi.

Từ khóa GUI `/kill` mặc định `kill|suicide|tự sát`; thay trong menu nếu server dùng
tên khác. Không click fallback slot của GUI bán, rương hay auction. Khi chết, Vex
Recovery tiếp quản hồi sinh/home; automation tiếp tục sau recovery và sau khi
quét vùng an toàn ổn định (mặc định một giây). Scan lỗi/thiếu world không được coi
là bằng chứng vùng an toàn.

Hai công tắc phát hiện mặc định **OFF**, chọn chế độ trong GUI rồi Save. Nếu bật
cả hai, **Auto Disconnect được ưu tiên**, không chạy tiếp Ender Pearl hoặc `/kill`
sau quyết định thoát server. Danh sách người tin cậy áp dụng chung cho hai chế độ,
không phân biệt chữ hoa/thường. Người quen đứng cùng người lạ không làm che mất
người lạ như nhánh lọc bạn bè trong bản SeaAutoSell cũ.

## Recovery và home tích hợp

Recovery có công tắc, Vex check, delay hồi sinh, delay sau hồi sinh, lệnh home
(mặc định `/home 1`) và delay ổn định sau home trong GUI của cả hai phía. Giữ màn
hình chết đến khi quét thành công ít nhất năm lần không còn Vex và ổn định ít
nhất một giây, rồi hồi sinh → chờ world/player → home → chờ ổn định → tiếp tục.
Recovery hoạt động khi master ON dù switch riêng AutoUse/AutoSell OFF.

Module recovery gốc của AutoUse được giữ nguyên trong JAR gốc. Bản Use ngăn module
đó chạy trùng trong khi Control tự quản lý recovery. Bỏ Control Use thì AutoUse
vẫn dùng module gốc. Bản Sell có recovery riêng, hoàn toàn không cần AutoUse.

AutoHome có hai nút riêng trong **Giữ mốc / Home**: **Lưu mốc** chỉ lưu X/Z;
**Lưu hướng nhìn** chỉ lưu góc ngang (yaw) và dọc (pitch). Bật AutoHome rồi
chỉnh lệnh, thời gian chờ/lặp, bán kính vị trí và dung sai hướng nhìn (mặc định
5 độ cho mỗi góc). Lệch vị trí **hoặc** hướng nhìn sẽ đếm ngược về home. Yaw được
so theo góc ngắn nhất qua ranh giới −180°/180°. Hướng nhìn chỉ được kiểm tra sau
khi bấm nút lưu hướng; config cũ tiếp tục kiểm tra X/Z cho đến lúc đó.

Đếm chờ home vẫn tiến khi bán/mua. AutoSell `.3` giữ nguyên menu qua nhiều vòng
bán, lúc scan rảnh và khi nghỉ `delay_sleep`; không xác nhận vòng không chuyển item.
Chỉ có một yêu cầu `/sell` đang chờ, cửa sổ phản hồi 5 giây. Nếu không nhận menu:
chờ thêm 5 giây trước lần thử thứ hai, 10 giây trước lần thử thứ ba, sau đó dừng
gửi và báo tắt/bật để thử lại. Nhận menu sẽ xóa chuỗi retry và đặt cooldown mở lại
5 giây. Whitelist/blacklist và cấu hình người dùng được giữ nguyên.
Khi home đến hạn, Control Sell có thể đóng menu bán đang rảnh để nhường quyền;
không ngắt scan/chuyển item, `WAITING_MOVE`, xác nhận bán, chờ phản hồi `/sell`
hay khi con trỏ còn cầm item. GUI khác vẫn chặn home. Sau lệnh home, khoảng chờ
ổn định vẫn áp dụng. Lệnh home chưa có xác nhận teleport thành công từ server.
Khi tắt/nhường quyền, Control dừng gửi click mới, chờ hết delay đồng bộ đang có
và đợi cursor trống trước khi đóng đúng menu sở hữu. AutoHome vẫn dùng nguyên
interlock `AutomationPolicy.home`, chỉ chạy sau khi menu đã được đóng an toàn.

## Auto Bounty — chỉ phía Sell

Mở **F9 → Auto Bounty**, nhập tên người nhận, chọn **Bật Auto Bounty** rồi Save.
Master F8 phải ON; Auto Bounty hoạt động độc lập với công tắc AutoSell. Mặc định
OFF, tên trống nên không gửi lệnh ngoài ý muốn. Thiết lập riêng trong
`config/dualdonutcontrol-sell-bounty.json`; không đọc/sửa config DonutUtility cũ.
Farm không có runtime, thiết lập hay tab Auto Bounty.

Giữ hành vi từ source có sẵn ở `D:/DonutUtil/restored-donututility`: đọc `/balance`,
chỉ chạy từ $1M, dùng số dư làm tròn xuống theo 0,1 triệu, ví dụ $1.299.999 → 1.2m.
Gửi `/bounty add <tên> <số tiền>`, chờ đúng **Confirm Bounty**, nút **Confirm** có
tooltip **Click to add bounty**, đợi rồi kiểm tra lại handler/slot/cursor trước click.
Chỉ ghi nhận thành công từ thông báo server đúng người nhận và số tiền.

GUI có chu kỳ (mặc định 7 phút), chờ retry (7 giây), chờ trước lệnh (0,5 giây),
chờ trước xác nhận (1 giây), trạng thái, số dư, lần gần nhất và đếm ngược.
Mỗi phản hồi balance/GUI/success có timeout 5 giây. Retry luôn đọc số dư mới;
trong backoff cho Sell/Home chạy. Không sao chép MaLiLib, AutoTPA hay AutoPay.

Đợi AutoSell hoàn tất chuyển/xác nhận item mới nhường menu. Trong giao dịch
bounty thì chặn Sell/Home, giữ ưu tiên Master, recovery, bảo vệ và home settle.
Chỉ đóng GUI do yêu cầu bounty đang chạy sở hữu, sau đồng bộ và khi cursor trống.
Không đóng rương khác và không dùng số slot fallback khi nhận diện lỗi.

## Build và kiểm tra

Giữ project trong monorepo, chọn Gradle JVM **JDK 21**. Cùng source chung nhưng
biên dịch thành hai JAR có ID và dependency riêng; chỉ compile source/mixin của
phía đang build. Output tách thành `build/use/` và `build/sell/`.

```powershell
.\gradlew.bat packageRelease -Pside=use
.\gradlew.bat packageRelease -Pside=sell

.\gradlew.bat runMixinSmoke -Pside=use -PmixinSmoke -x downloadAssets
.\gradlew.bat runMixinSmoke -Pside=sell -PmixinSmoke -x downloadAssets
```

- Use: `build/use/libs/DualDonutControl-Use-2.0.2.jar` và gói cài trong
  `build/use/distributions/`.
- Sell: `build/sell/libs/DualDonutControl-Sell-2.0.3.jar` và gói cài trong
  `build/sell/distributions/`.
- Mỗi ZIP chỉ có `mods/` với hai runtime JAR: Control và mod của đúng phía.
  Không chứa source JAR, source code hoặc tài liệu rời. Release có đúng hai asset ZIP.
- `verifyCoordination`: **137 kiểm tra Use**, **143 kiểm tra Sell** về Vex gate,
  recovery, interlock, nhận diện GUI, whitelist người chơi, thời gian AntiPlayer,
  hai lần thiếu pearl, gửi kill một lần, timeout, chờ vùng sạch, cấu hình hỏng,
  payload webhook, góc nhìn/yaw wrap, config cũ, pha nhường quyền của AutoSell
  và hợp đồng bytecode của đúng mod gốc.
- `runMixinSmoke`: Fabric Loader áp dụng mixin trên **8 lớp ở Use**, **7 lớp ở
  Sell**, xác minh class phía đối diện và AutoHome không tồn tại trong runtime.
  Dừng trước khi chạy entrypoint của mod gốc/Minecraft; không gọi auth, kết nối
  server hay gửi webhook. Source smoke không đi vào JAR phát hành.
- Sell `verifyBounty` (tự chạy trong `check`): **69 kiểm tra** protocol, tiền làm
  tròn, config/GUI snapshot, timeout, retry, xác nhận, cursor, ngắt Master và nhường
  quyền. Mixin smoke còn xác minh GUI Bounty tồn tại ở Sell và không có ở Farm.
- Project Shion: `check` chạy thêm **34 kiểm tra mô phỏng** trên cùng source
  `AutoSellLogic` và `SideIntegration`, gồm 25 vòng bán cùng menu, nghỉ, menu rảnh,
  response chậm, retry/backoff, home, tắt/bật nhanh và cursor còn item. Double
  game/config chỉ nằm trong source set simulation, không vào runtime JAR.

Đã build và qua hai nhóm kiểm tra nêu trên. **Chưa kiểm thử trên server thực tế**:
khả năng server nhận projectile, GUI `/kill`, bán/mua kiếm, teleport và recovery
phải kiểm tra trong game. GUI bán được nhận theo tên phản hồi sau `/sell` trong
tối đa năm giây; server dùng tên khác cần sửa từ khóa trong tab Sell.
Không sửa case GUI chen ngang làm mất quyền sở hữu, theo phạm vi đã chốt.
