# Auto Home AFK — mã nguồn khôi phục

Khôi phục từ `Beatrix/autohomeafk-1.0.0.jar` bằng Vineflower 1.11.2.
Đây là source decompile, không phải source gốc do tác giả cung cấp.
Giữ nguyên 6 file Java sau decompile, ID `autohomeafk`, phiên bản `1.0.0`,
entrypoint và tài nguyên. Metadata gốc ghi tác giả `Antigravity`, license `MIT`;
JAR gốc không chứa văn bản LICENSE riêng.

## Build

Cần JDK 21 và Internet cho lần tải dependency đầu tiên. Mở **thư mục này**
làm Gradle project trong IDE và chọn JDK 21 cho Gradle JVM.

```powershell
# Nếu Java mặc định không phải JDK 21, đặt JAVA_HOME tới JDK 21 của bạn.
$env:JAVA_HOME = 'C:\path\to\jdk-21'
.\gradlew.bat --version
.\gradlew.bat compileJava --rerun-tasks
.\gradlew.bat build
```

Trên máy đã khôi phục dự án, JDK 21 có sẵn tại
`C:\Users\Ethan\.gradle\jdks\eclipse_adoptium-21-amd64-windows.2`.
Đường dẫn này chỉ là thông tin cục bộ, không được cố định trong build.

Linux/macOS: đặt `JAVA_HOME` tới JDK 21 rồi dùng `sh ./gradlew build`.

- JAR chạy trong game: `build/libs/autohomeafk-1.0.0.jar`.
- JAR source: `build/libs/autohomeafk-1.0.0-sources.jar`.
- Mục tiêu đã build: Minecraft 1.21.11, Fabric Loader 0.18.2,
  Fabric API 0.141.6+1.21.11.
- Công cụ: Gradle 9.5.1, Fabric Loom 1.17.21, Java 21.

Source giữ tên Intermediary của Minecraft (`class_*`, `method_*`, `field_*`).
`build.gradle` tạo mapping `named` giống `intermediary` từ artifact Fabric chính thức;
không cần sửa các import này khi mở IDE. JAR trong `build/devlibs` là artifact
phát triển; dùng JAR trong `build/libs` khi kiểm thử game.

## Các điểm chỉnh sửa

| File Java | Vai trò |
| --- | --- |
| `net/autohomeafk/AutoHomeAfkClient.java` | Entrypoint, load cấu hình, đăng ký phím và END_CLIENT_TICK |
| `net/autohomeafk/config/ModConfig.java` | Giá trị mặc định và các trường cấu hình |
| `net/autohomeafk/config/ConfigManager.java` | Đọc/ghi JSON UTF-8 tại thư mục config của Fabric |
| `net/autohomeafk/core/AutoHomeEngine.java` | Mốc X/Z, bán kính, đếm tick, gửi lệnh, thông báo actionbar |
| `net/autohomeafk/core/ModKeybinds.java` | H bật/tắt, O mở cấu hình |
| `net/autohomeafk/gui/AutoHomeConfigScreen.java` | Nhập lệnh, delay, chu kỳ, bán kính, đặt mốc và lưu |

Phím bật sẽ lấy tọa độ hiện tại làm mốc. Khi ra ngoài bán kính, engine đợi
`delayMinutes`, gửi lệnh, rồi lặp theo `repeatIntervalMinutes` tới khi trở về mốc.
Engine chỉ xét X/Z và chuyển phút thành tick theo 20 tick/giây.

## Phạm vi xác minh

`compileJava --rerun-tasks` và `build` đã thành công với JDK 21.0.7.
Đã đối chiếu class, member descriptor, tài nguyên và các khác biệt disassembly.
Không có bài test tự động (`test NO-SOURCE`); chưa khởi động Minecraft,
kiểm tra IDE thực tế hay kiểm thử hành vi trong game.

Metadata gốc cho phép `minecraft >=1.21.11`, nhưng bản khôi phục chỉ build cho
1.21.11. Cần kiểm thử load mod, phím, GUI, lưu/đọc config, timer và gửi lệnh
trong môi trường game riêng trước khi dùng bản build mới.
