# AutoSell Donut 1.3.3 — mã nguồn phục hồi

Phục hồi từ `Shion/autosell_donut-1.3.3.jar` bằng Vineflower 1.11.2.
9 file Java chứa đủ 11 class của JAR gốc. Tên Minecraft Intermediary
(`class_*`, `method_*`, `field_*`) được giữ nguyên.

## Build

Cài JDK 21 và trỏ `JAVA_HOME` tới thư mục JDK đó trong phiên terminal,
sau đó chạy tại thư mục chứa file này:

```powershell
.\gradlew.bat --version
.\gradlew.bat compileJava --rerun-tasks
.\gradlew.bat build
```

Trên Linux/macOS: `bash ./gradlew build` với JDK 21.

- JAR chạy: `build/libs/autosell_donut-1.3.3.jar`
- JAR mã nguồn: `build/libs/autosell_donut-1.3.3-sources.jar`
- Minecraft 1.21.11; Fabric Loader 0.19.3; Fabric API 0.141.6+1.21.11.
- YACL 3.8.1+1.21.11-fabric phục vụ GUI; Mod Menu 17.0.1 phục vụ tích hợp menu.
- Gradle 9.5.1, Loom 1.17.21, Java bytecode 21.

Các phiên bản dependency được pin để phục hồi build; không phải tất cả đều
chứng minh được là phiên bản tác giả đã dùng. YACL và Mod Menu là dependency
bên ngoài, không được nhúng vào JAR. Metadata runtime được giữ nguyên từ bản gốc;
đặc biệt YACL chưa được bản gốc khai báo trong `depends`.

## Điểm bắt đầu chỉnh sửa

| File | Vai trò |
| --- | --- |
| `Autosell.java` | Entrypoint client, phím F7/Right Shift, tick và chat |
| `AutoSellLogic.java` | Máy trạng thái bán đồ, delay, whitelist/blacklist, tạm dừng |
| `AutoSellGui.java` | Các tùy chọn YACL và nút thử webhook |
| `ConfigManager.java` | Đọc/ghi `config/hsnautosell.json`, lệnh `/atsell generate_code` |
| `WebhookManager.java` | Tính lợi nhuận và báo cáo Discord |
| `HsnAuthModule.java` | Luồng xác thực bên ngoài, định danh cục bộ |
| `ModExpiration.java` | Helper kiểm tra hết hạn; không tìm thấy caller trong mod |
| `ModMenuIntegration.java` | Mở GUI qua Mod Menu |

Build đã được kiểm tra thành công. Chưa chạy Minecraft, xác nhận mod load,
thao tác bán đồ, xác thực hoặc webhook. Không có bộ unit test sẵn có.
Code xác thực và các hành vi khác của bản gốc được giữ nguyên. Xem báo cáo
`../RESTORATION.md` trong thư mục phục hồi để biết phạm vi đối chiếu.
