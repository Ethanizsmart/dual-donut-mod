# Giao thức nối AutoSell vào AutoUseMod

Mod ID: `autousemod`. API: `dev.autouse.api.AutoUseApi`.
Mục tiêu: AutoSell tiếp tục là mod độc lập, có thể chạy khi không cài AutoUseMod.
Không chép lại class API vào AutoSell vì sẽ tạo hai bản trạng thái.

## Các hàm public

| Hàm static | Ý nghĩa |
|---|---|
| `boolean isAutoSellPaused()` | Phải dừng giao dịch bán ngay khi true. |
| `String pauseReason()` | `WAIT_VEX_CLEAR`, `DEATH_RECOVERY`, `BUYING_SWORD`, hoặc chuỗi rỗng. |
| `void setAutoSellBusy(boolean)` | AutoSell báo quyền sở hữu giao dịch/container mỗi client tick. |
| `boolean needsInventorySpace()` | Mua kiếm đang cần chỗ trống; cho phép một lượt AutoSell dọn túi. |
| `boolean isAutoSellBusy()` | Trạng thái busy hiện tại; heartbeat hết hiệu lực sau hai giây. |

Pause không thay đổi lựa chọn bật/tắt AutoSell của người dùng. Khi pause kết thúc,
reset về IDLE và chỉ chạy tiếp nếu AutoSell vốn được bật hoặc đang xử lý yêu cầu
`needsInventorySpace()`. Không tự bật AutoSell lâu dài khi dọn chỗ để mua kiếm.

## Vị trí cần nối vào state machine

1. Đầu mỗi client tick, trước mọi lệnh, click slot, confirm, pay hoặc recovery
   thuộc engine bán: đọc `isAutoSellPaused()`.
2. Nếu pause: hủy timer/giao dịch đang chờ đúng một lần, giải phóng phím do
   AutoSell giữ, báo `setAutoSellBusy(false)` và return. Không click, không gửi
   command và **không đóng/thay thế màn hình chết hay GUI đấu giá** của AutoUseMod.
   Nếu muốn đóng GUI bán cũ, chỉ đóng khi đã xác minh đó chính là container mình sở hữu.
3. Nếu vừa hết pause: reset state/handler cũ về IDLE, tránh dùng sync ID cũ.
4. Nếu AutoSell được bật hoặc có `needsInventorySpace()`, chạy engine bán như thường.
   Khi vào giao dịch, đặt busy true **trước** khi gửi `/sell`, và refresh busy mỗi tick.
   Sau khi giao dịch hoàn tất/hủy/disconnect/tắt mod, báo busy false.
5. Kiểm tra pause lại ngay trước các side effect nếu engine có callback bất đồng bộ;
   mọi thao tác Minecraft và API phối hợp nên chạy trên client thread.

Khi `needsInventorySpace()` true, AutoUseMod giữ RMB tắt nhưng cho AutoSell chạy.
Chỉ khi túi có ô trống thật, GUI bán đã đóng và player trở về inventory handler,
AutoUseMod mới khóa AutoSell trở lại và gửi `/ah sword looting 3`.

## Liên kết tùy chọn bằng reflection

Dùng `FabricLoader.getInstance().isModLoaded("autousemod")` trước khi tìm class.
Cache Method sau khi load; nếu không cài AutoUseMod, các giá trị mặc định là
pause=false, needsSpace=false và publishBusy không làm gì.

```java
Class<?> api = Class.forName("dev.autouse.api.AutoUseApi");
Method paused = api.getMethod("isAutoSellPaused");
Method busy = api.getMethod("setAutoSellBusy", boolean.class);
Method needsSpace = api.getMethod("needsInventorySpace");

// Trong tick của AutoSell, sau khi đã kiểm tra player/world/connection:
if ((boolean) paused.invoke(null)) {
    // cancelOwnTransactionWithoutClosingForeignScreens();
    busy.invoke(null, false);
    return;
}
// runOwnSellStateMachine(userEnabled || (boolean) needsSpace.invoke(null));
// busy.invoke(null, ownTransactionIsRunningOrOwnSellGuiIsOpen);
```

Đoạn trên mô tả điểm móc; cần nối vào engine AutoSell khi tách mod đó. Chưa có mod
AutoSell riêng để kiểm tra end-to-end ở lần bàn giao AutoUseMod này.

## Kiểm tra tích hợp khi có AutoSell

- Chết trong khi đang chờ `/sell` hoặc đang confirm: không click/gửi lệnh tiếp,
  giữ nguyên Death Screen, không ghi đè master/userEnabled của AutoSell.
- Vex còn sống hoặc scan chưa biết: pause xuyên suốt; chỉ tiếp tục sau respawn,
  `/home 1` và khoảng chờ ổn định.
- Thiếu kiếm 10 giây trong khi AutoSell đang chạy: đợi kết thúc giao dịch bán,
  không mở /ah chồng lên GUI bán.
- Túi đầy: AutoSell được nhường dọn chỗ; đóng GUI và trả busy=false trước khi mua.
- Disconnect, chết tiếp, F8 OFF hoặc mở F9: hủy handler/timer cũ, không tiếp tục
  xác nhận mua/bán từ phiên trước.
