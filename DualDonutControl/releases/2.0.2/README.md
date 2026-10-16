# Bản sửa local 2.0.2 — 16/10/2026

- Phía bán: `DualDonutControl-Sell-2.0.2-install.zip` chứa Control Sell 2.0.2 và AutoSell 1.3.3-webhook.3.
- Phía farm: `DualDonutControl-Use-2.0.2-install.zip` chứa Control Use 2.0.2 và AutoUseMod 1.0.1.

Giải nén gói đúng phía và thay hai JAR tương ứng trong profile, bỏ bản cũ cùng mod
khỏi `mods`. Fabric API và YACL vẫn cài riêng như trước. Không cài cả hai phía
vào cùng profile. JAR `.2` cũ vẫn được giữ nguyên trong workspace để đối chiếu.

Menu Sell được giữ mở qua nhiều vòng bán, scan rảnh và nghỉ. Chờ phản hồi `/sell`
5 giây; nếu timeout thì backoff 5/10 giây, tối đa ba lần thử rồi dừng gửi và báo
tắt/bật để thử lại. Menu đang mở được tái sử dụng. Khi cần nhường home/stop,
Control chỉ đóng menu sở hữu sau delay đồng bộ và khi cursor trống.

Giữ nguyên bộ lọc whitelist/blacklist và cấu hình game; người dùng tự tắt whitelist.
Không sửa case GUI chen ngang làm menu mất quyền sở hữu, theo yêu cầu.
GUI hiện có, hai nút lưu X/Z/hướng nhìn và `AutomationPolicy.home` không đổi.

Đã qua:

- Shion `build`/`check`: 12 payload checks và 31 engine/coordinator simulation checks.
- Control `check -Pside=use`: 136 checks; `check -Pside=sell`: 142 checks.
- Mixin smoke Use: 8 lớp; Sell: 7 lớp. Dừng ở prelaunch, không chạy entrypoint/auth/webhook.
- `packageRelease` cả hai phía; metadata, dependency, SHA-256 và thành phần ZIP
  xác minh bằng `tools/Verify-InstallPackages.ps1`. Chi tiết ở `PACKAGE_VERIFICATION.json`.

Chưa cài vào game đang chạy hoặc xác minh giao dịch/teleport trên server thật.
Mô phỏng dùng cùng source engine/coordinator với menu/config trong bộ nhớ;
không coi click là bằng chứng server đã bán thành công.
Không commit, push hoặc phát hành GitHub. Hai ZIP là gói cài local để kiểm thử.
