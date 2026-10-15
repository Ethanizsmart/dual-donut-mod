# Báo cáo khôi phục Auto Home AFK

Ngày: 16/10/2026. Quy trình: skill `restore-fabric-mod`.

## Đầu vào và bản sao

- JAR đầu vào: `../autohomeafk-1.0.0.jar`, giữ nguyên.
- Bản sao đã kiểm tra hash: `original/autohomeafk-1.0.0.jar`.
- SHA-256 cả hai: `b19acb0cbcb5701b6d6da3e686c21bb4c90aa3df76a63560be2d4576cd0b1dd3`.
- Mod client-only, ID `autohomeafk`, phiên bản `1.0.0`, tác giả trong metadata
  là `Antigravity`; Beatrix là thư mục chứa JAR được người dùng chọn.
- Entrypoint: `net.autohomeafk.AutoHomeAfkClient`.
- Gốc: 6 class, 4 package, classfile major 65 (Java 21), 4 resource.
  Không có mixin, access widener, service hay dependency JAR lồng trong archive.

## Decompiler và provenance

Vineflower 1.11.2, không dùng fallback. Tái sử dụng tool cục bộ và đối chiếu
SHA-1 với artifact được công bố trên Maven Central trước khi chạy:

- Artifact: https://repo.maven.apache.org/maven2/org/vineflower/vineflower/1.11.2/vineflower-1.11.2.jar
- Checksum: https://repo.maven.apache.org/maven2/org/vineflower/vineflower/1.11.2/vineflower-1.11.2.jar.sha1
- SHA-1: `f02c1f554d5bd9cc0fd74d4977efba364d85e739`.
- SHA-256 cục bộ: `e1e2415e7f78b34960402c4beddfc88e033d7842a23ecd132a8ec2eadd54f6bf`.
- Lệnh: `java -jar tools/vineflower-1.11.2.jar original/autohomeafk-1.0.0.jar baseline`.
- Exit 0; 6 file Java, không class lồng/ẩn danh cần file bổ sung.
- Không thấy WARN/ERROR hoặc phương thức decompile thất bại trong log.
- `baseline/` giữ output nguyên trạng; cả 6 source production có hash bằng baseline.
  Không sửa logic, quyền truy cập hay hành vi của mod.

## Toolchain và mappings

| Thành phần | Bằng chứng từ JAR gốc | Bản khôi phục |
| --- | --- | --- |
| Minecraft | Manifest 1.21.11, metadata >=1.21.11 | 1.21.11 |
| Java | Metadata >=21, classfile 65 | JDK 21.0.7 Adoptium, target/release 21 |
| Gradle | Manifest 9.4.1 | 9.5.1 |
| Loom | Manifest 1.14.10 | 1.17.21 |
| Loader | Metadata >=0.18.0, manifest 0.18.2 | 0.18.2 |
| Fabric API | Metadata *, không biết pin của tác giả | 0.141.6+1.21.11 |
| Namespace | Intermediary | Intermediary |

Build dùng Intermediary v2 chính thức và tạo thêm cột `named` giống hệt
`intermediary`. Không migrate sang Yarn/Mojang. Dependency API được kiểm tra
metadata: yêu cầu Loader >=0.17.3, Java >=21, Minecraft >=1.21.11- <1.21.12-.
Gson lấy từ dependency Minecraft; không shade thêm thư viện vào mod.

Official Loom module metadata xác nhận JVM 21 và Gradle plugin API 9.5.0:
https://maven.fabricmc.net/net/fabricmc/fabric-loom/1.17.21/fabric-loom-1.17.21.module

Wrapper gồm 2 script và JAR chính thức từ tag Gradle v9.5.1:
https://github.com/gradle/gradle/tree/v9.5.1

- Wrapper JAR SHA-256 đã đối chiếu chính thức:
  `497c8c2a7e5031f6aa847f88104aa80a93532ec32ee17bdb8d1d2f67a194a9c7`.
- Distribution SHA-256 pin trong wrapper properties, đối chiếu chính thức:
  `bafc141b619ad6350fd975fc903156dd5c151998cc8b058e8c1044ab5f7b031f`.
- https://services.gradle.org/distributions/gradle-9.5.1-wrapper.jar.sha256
- https://services.gradle.org/distributions/gradle-9.5.1-bin.zip.sha256

## Build và diagnostics

| Lệnh | Exit | Kết quả |
| --- | --- | --- |
| `gradlew.bat --version` | 0 | Launcher và daemon chọn JDK 21.0.7 |
| `gradlew.bat compileJava --rerun-tasks --console=plain` | 0 | compileJava thực thi, BUILD SUCCESSFUL |
| `gradlew.bat build --console=plain` | 0 | 6 task thực thi, compileJava up-to-date, remapJar/remapSourcesJar thành công |

Không có lỗi javac hay source cần sửa. Lần cấu hình đầu có dòng
`Cannot remap modifiers because it does not exist in any of the targets [] or their parents.`
trước bước remap dependency source. Không sửa/tắt remapping để né dòng này;
compileJava và build hoàn tất, dòng này không lặp trong log build tiếp theo.
Đây vẫn là diagnostic công cụ đã ghi nhận, không phải bằng chứng runtime đã đạt.

`test NO-SOURCE`, `validateAccessWidener NO-SOURCE`: không có suite tự động
hoặc access widener để kiểm tra. Chưa chạy Minecraft, entrypoint, networking,
lệnh server, cấu hình tài khoản hay webhook.

## Đối chiếu artifact

- JAR runtime: `project/build/libs/autohomeafk-1.0.0.jar`.
- SHA-256: `f4d15e2adc5b16e0dca887c64d0f55291e14d5e3b8aad4bf1226a4d5d45c2238`.
- 6 class, 4 package, major 65: không thiếu/thừa class, không duplicate hoặc header lỗi.
- `fabric.mod.json`, icon PNG và `en_us.json` giữ nguyên byte.
- Chỉ `META-INF/MANIFEST.MF` thay đổi trong resource: Gradle 9.4.1 -> 9.5.1,
  Loom 1.14.10 -> 1.17.21, Tiny Remapper 0.12.2 -> 0.14.0.
  Giữ semantics client entrypoint, namespace intermediary, Jar-Type classes,
  mixin-remap-type static và các thuộc tính còn lại.
- Cả 6 class thay byte do recompilation; không tuyên bố JAR tương đương byte.
- Kiểm tra `javap -c -p -s` toàn bộ 6 class. Member descriptor và access declaration
  khớp sau khi quy đổi tên lambda synthetic `lambda$init$*` thành
  `lambda$method_25426$*` do tên method phát triển thay đổi.
- Sau bỏ index constant pool, 4 class có disassembly khớp.
  Các string literal hiện trong lệnh ldc của cả 6 class được so sánh Unicode
  phân biệt hoa/thường và khớp, bao gồm thông báo tiếng Việt.
  AutoHomeEngine có early return đổi sang nhảy tới return chung; đã kiểm tra
  đích nhảy và điều kiện null/enabled/world, vòng phím, delay, repeat, command.
  AutoHomeConfigScreen có hai nhánh input dùng goto tới ireturn chung;
  cùng kết quả và fallback super. Các callback giữ nội dung, đổi tên synthetic.
- Lưu disassembly gốc/mới và diff trong `logs/`. Diff đã bỏ offset chỉ là hỗ trợ
  đọc; việc bỏ đích nhảy không chứng minh tương đương control flow.
- Không kiểm thử hành vi động; đối chiếu này không chứng minh tương đương toàn bộ.

## Trạng thái phát triển và giới hạn

**READY cho chỉnh sửa/build**, chưa xác minh phát hành/runtime. Source đủ,
build thành công và README chỉ rõ các điểm mở rộng.

Luồng chính: entrypoint load cấu hình -> đăng ký H/O -> END_CLIENT_TICK ->
AutoHomeEngine. Setting nằm trong ModConfig, bind/validate tại GUI,
serialize toàn bộ config bằng Gson, consume tại engine. Thông báo dùng actionbar,
không có HUD renderer riêng. Lệnh gửi qua network handler của Minecraft.

Các hành vi có sẵn cần lưu ý khi phát triển tiếp, không thay đổi trong lần khôi phục:

- Metadata cho phép Minecraft mới hơn nhưng pin và xác minh chỉ cho 1.21.11.
- JSON load không áp dụng clamp giống GUI; file chỉnh tay có thể bỏ qua validation.
- Timer và isWaitingRepeat là static state trong engine, không có disconnect hook
  riêng. Nhánh bật trong GUI chỉ reset timer; phím tắt và GUI có đường cập nhật
  trạng thái riêng. Cần xem các đường này khi sửa tự động hóa.
- Tọa độ mốc chỉ X/Z, không lưu dimension; timer tính theo tick, không theo đồng hồ.

Bước xác minh tiếp theo: dùng profile game riêng với Minecraft 1.21.11,
Loader/API đúng pin; kiểm tra mod load, H/O, GUI/input/render, config save/load,
bán kính, delay/repeat, trạng thái bật/tắt/disconnect. Chỉ thử gửi lệnh trong
môi trường kiểm thử do người dùng cho phép.

## Bàn giao

- `project/`: Gradle project chỉnh sửa được, xem `project/README.md` để build.
- `baseline/`: source decompile nguyên trạng cùng tài nguyên gốc.
- `original/`: bản sao JAR gốc đã kiểm tra hash.
- `logs/`: lệnh decompile, version/compile/build, artifact comparison, disassembly.
- `autohomeafk-1.0.0-recovered-project.zip`: source project và wrapper;
  không chứa cache, tool, runtime profile hay build output.
- JAR `*-sources.jar` chứa source; không phải JAR chạy trong game.
