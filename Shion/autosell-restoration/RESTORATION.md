# Báo cáo phục hồi AutoSell của Shion

Ngày thực hiện: 15/10/2026 (Asia/Bangkok).

## Đầu vào và bản lưu

- JAR đã chọn: `../autosell_donut-1.3.3.jar`, 93.620 byte.
- Bản lưu đã kiểm tra hash: `original/autosell_donut-1.3.3.jar`.
- SHA-256 gốc: `958f51e692039825c6f136a8143f22de34899fe461f17f2a1525e1a25956ec4a`.
- Không thay đổi JAR đầu vào. Dự án mới nằm trong `project/`.
- `baseline/` giữ nguyên kết quả Vineflower và resource của bản gốc để diff.

## Công cụ và cấu hình

Vineflower 1.11.2, lấy từ Maven Central chính thức:
https://repo.maven.apache.org/maven2/org/vineflower/vineflower/1.11.2/vineflower-1.11.2.jar

SHA-256 đã đối chiếu với file `.sha256` công bố:
`e1e2415e7f78b34960402c4beddfc88e033d7842a23ecd132a8ec2eadd54f6bf`.

Lệnh: `java -jar tools/vineflower-1.11.2.jar original/autosell_donut-1.3.3.jar baseline`.
Exit 0, không có WARN/ERROR hay marker phương thức decompile thất bại.
Không cần CFR. Log: `logs/decompile.log`.

| Thành phần | Bằng chứng / lựa chọn |
| --- | --- |
| Mod | `autosell_donut`, 1.3.3, package `hoshin.autosell` |
| Minecraft | 1.21.11 từ manifest, phù hợp `~1.21.11` trong metadata |
| Namespace | Intermediary; cột `named` ánh xạ đồng nhất để giữ nguyên source |
| Classfile | 65 = Java 21, cả 11 class gốc và phục hồi |
| JDK launcher/daemon/compiler | Eclipse Adoptium 21.0.7+6-LTS đã có trên máy |
| Gradle / Loom | 9.5.1 / 1.17.21, cùng phiên bản ghi trong manifest gốc |
| Loader | Pin 0.19.3, cùng manifest gốc và thỏa `>=0.19.3` |
| Fabric API | Pin 0.141.6+1.21.11; bản gốc chỉ khai báo `*` |
| YACL / Mod Menu | Pin 3.8.1+1.21.11-fabric / 17.0.1 từ import và metadata chính thức |

Đã kiểm tra Loom module yêu cầu Java 21 và Gradle plugin API 9.5.0;
đã kiểm tra metadata chính thức của các dependency và Minecraft mục tiêu.
Không suy ra Yarn/Mojang development mappings của tác giả từ tên Intermediary.

Wrapper đầy đủ lấy từ tag Gradle `v9.5.1`. SHA-256 wrapper JAR đã đối chiếu
với Gradle: `497c8c2a7e5031f6aa847f88104aa80a93532ec32ee17bdb8d1d2f67a194a9c7`.
Distribution checksum được pin và wrapper xác minh khi tải:
`bafc141b619ad6350fd975fc903156dd5c151998cc8b058e8c1044ab5f7b031f`.

## Sửa lỗi decompile

Chỉ sửa 2 file Java so với baseline:

1. `AutoSellGui.java`: khôi phục tham số generic cho 10 `Option` và 2
   `ListOption<String>`, dùng `ArrayList<>`. Đây là nguyên nhân của 26 lỗi
   compiler ở lần chạy đầu. Giữ nguyên mặc định, binding, giới hạn và action.
2. `ConfigManager.java`: bỏ cast raw `LiteralArgumentBuilder`, cast source
   thừa và import tương ứng, để giữ type inference của Brigadier và bỏ cảnh
   báo unchecked do decompiler. Không thay đổi lệnh hay side effect.

Không bỏ class, stub logic, thay quyền truy cập, tắt remapping hay bật thêm
feature. Các file Java còn lại giữ nguyên byte so với baseline.

## Kết quả xác minh

| Kiểm tra | Kết quả |
| --- | --- |
| Wrapper `--version` | Exit 0, Gradle/JVM đúng; `logs/gradle-version.log` |
| Compile lần 1 | Exit 1, 26 lỗi generic GUI; `logs/compile-1.log` |
| Compile lần 2 | Shell dừng vì xử lý stderr deprecation như lỗi; không dùng làm bằng chứng pass/fail Gradle |
| `compileJava --rerun-tasks` sau sửa | Exit 0, task thực sự chạy; `logs/compile-3.log` |
| `build` | Exit 0; `jar`, `sourcesJar`, `remapJar`, `remapSourcesJar` chạy; `logs/build-1.log` |
| Unit tests | `test NO-SOURCE`; không có suite để tuyên bố test pass |
| Minecraft / mod load / chức năng | NOT TESTED |
| IDE import/debug | NOT TESTED |

Còn cảnh báo deprecation trong `HsnAuthModule`; không chặn compile/build.
Không chạy entrypoint, không mở Minecraft, không dùng tài khoản hoặc gọi
endpoint xác thực, time API hay Discord webhook của mod.

## Đối chiếu artifact

JAR runtime: `project/build/libs/autosell_donut-1.3.3.jar`.
SHA-256: `cb8a3cd0208d606455e1ac8232306393d3dbd6d94f0653786f822b9c2fd3d23a`.

- Gốc và phục hồi: 2 package, 11 class, không thiếu/thừa class.
- 9 file Java: `AutoSellLogic.State` và `ConfigManager.ConfigData` là 2 class lồng.
- 5 resource, bao gồm manifest, khớp byte; không thiếu/thừa resource.
- `fabric.mod.json`, icon, license và mixin JSON được giữ nguyên.
- Không có JAR lồng, services hay access widener trong input.
- Không có entry trùng lặp hoặc class header hỏng.
- Cả 11 class có byte khác sau biên dịch; đây không phải JAR giống hệt binary.
- Helper inventory: skill `scripts/compare_jars.ps1`; JSON tại `logs/jar-comparison.json`.

Đã disassemble bản gốc bằng `javap -c -p -s`, và kiểm tra lại 2 class sửa.
Tool ASM đọc classfile, không class-load mod: `tools/CompareBytecode.java`.
Không thấy khác descriptor/quyền truy cập/giá trị constant của field,
header class hoặc phương thức ngoài tên lambda tổng hợp do compiler đánh số lại.
Enum giữ nguyên thứ tự, các static initializer có instruction text giống nhau.

Trong 85 phương thức đối chiếu, 74 có ASM instruction/annotation text giống
nhau sau bỏ debug và frame của ClassReader. 11 phương thức khác
đã đọc diff: nhánh early return so với khối if, thứ tự case, local slot và
try-with-resources, hoặc tham chiếu lambda tổng hợp đổi số. Hai lambda của
`ConfigManager` được ghép bằng descriptor; không mất phương thức thực.
Log tổng hợp: `logs/bytecode-comparison.txt`; chi tiết: `logs/bytecode-diffs/`.
Đây là đối chiếu tĩnh, không phải chứng minh tương đương mọi hành vi runtime.

## Điểm cần biết khi phát triển tiếp

- `Autosell` đăng ký keybind, tick và chat; gọi `HsnAuthModule.start`, load
  config và đăng ký command. Tick chỉ chạy bán/báo cáo khi state bằng 1 hoặc 2.
- `AutoSellLogic` quản lý chu trình mở `/sell`, chuyển đồ theo batch, xác nhận,
  chờ và tạm dừng khi nghi hết item. Lọc item đọc chung config của GUI/JSON.
- Settings đi từ binding GUI tới field tĩnh, DTO `ConfigData`, save/load rồi
  được automation và webhook đọc. Không có HUD riêng trong input.
- `HsnAuthModule` có luồng xác thực bên ngoài, payload tên/UUID/mã định danh
  do mod tạo, và nhánh `System.exit(0)` khi phản hồi chứa `BLACKLIST`.
  Giữ nguyên, chưa kiểm tra vận hành hoặc truy cập endpoint.
- `ModExpiration` có mốc 23/09/2026 nhưng không tìm thấy caller trong mod;
  không kết luận mod thực sự bị chặn hết hạn.
- `ExampleMixin` có hàm inject rỗng. Mixin JSON tồn tại nhưng không được
  `fabric.mod.json` đăng ký; giữ nguyên thay vì tự kích hoạt.
- GUI cần YACL dù bản gốc không khai báo dependency này trong `depends`.
  Cài dependency ngoài khi thử runtime; JAR phục hồi không shade thư viện.
- Nhận diện GUI/chat phụ thuộc server; compile không kiểm chứng được chu trình
  bán, delay, xác nhận hay báo cáo lợi nhuận trên server thật.

Kết luận: READY cho đọc/chỉnh sửa và build phát triển trong cấu hình đã pin;
chưa xác nhận sẵn sàng phát hành hoặc hoạt động trong game. Bước tiếp theo là
kiểm tra GUI, lưu cấu hình và state machine trong profile thử nghiệm được
cho phép riêng. Không thay đổi logic xác thực/hết hạn trong lần phục hồi này.
