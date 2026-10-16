# Auto Bounty cho Control Sell 2.0.3 — 16/10/2026

Gói cài `DualDonutControl-Sell-2.0.3-install.zip` chỉ có:

- `mods/DualDonutControl-Sell-2.0.3.jar`
- `mods/autosell_donut-1.3.3-webhook.3.jar`

Thay JAR Control Sell cũ bằng bản 2.0.3; giữ/cài AutoSell `.3` đi kèm.
Fabric API và YACL vẫn cài riêng. Không cần DonutUtility hoặc MaLiLib.
**Farm giữ nguyên 2.0.2, không thêm Auto Bounty vào Farm.**

Mở **F9 → Auto Bounty**: nhập tên người nhận, bật tính năng, chỉnh thời gian nếu
cần rồi **Save**. Bật Master F8 để chạy. Tính năng mặc định OFF và hoạt động độc
lập với switch AutoSell; cấu hình lưu ở `dualdonutcontrol-sell-bounty.json`.

Luồng giữ từ source DonutUtility ở `D:/DonutUtil/restored-donututility`:
`/balance` → số dư từ $1M làm tròn xuống theo 100k → `/bounty add <tên> <tiền>` →
GUI Confirm Bounty → nút Confirm với tooltip Click to add bounty → đợi thông báo
server đúng tên và số tiền. Ví dụ $1.299.999 đặt 1.2m. Không coi click là thành công.

GUI có bật/tắt, tên, chu kỳ 7 phút, retry 7 giây, chờ lệnh 0,5 giây, chờ xác nhận
1 giây, số dư/trạng thái/lần gần nhất/đếm ngược. Phản hồi timeout sau 5 giây.
Giữ các sửa menu Sell và pacing `/sell` từ 2.0.2. Chờ bán hoàn tất mới nhường GUI;
chỉ đóng đúng handler sở hữu khi cursor trống. Recovery/protection/Home vẫn được
tôn trọng; backoff cho Sell/Home chạy. Không thay whitelist hoặc config game.

Đã qua `check` Use (137), Sell coordination (143), Auto Bounty (69), Shion sale
simulation (34) và webhook payload (12); mixin smoke Use 8 lớp / Sell 7 lớp.
Các kiểm tra offline/mixin prelaunch không chạy auth, webhook hay tài khoản thật.
Build/remap và ZIP được xác minh metadata, dependency, thành phần và SHA-256 bằng:

```powershell
.\tools\Verify-InstallPackages.ps1 -Version 2.0.3 -Sides Sell
```

Chưa kiểm thử giao dịch/GUI trực tiếp trên server thật. Mô tả phát hành dễ hiểu
ở `RELEASE_NOTES.md`; bản phát hành GitHub v2.0.3 chỉ đính kèm hai ZIP cài Sell/Farm,
không đính kèm source. Nguồn hành vi được ghi trong `META-INF/BOUNTY_ORIGIN.md` ở JAR Sell.
