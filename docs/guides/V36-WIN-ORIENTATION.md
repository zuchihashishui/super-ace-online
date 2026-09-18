# V36 — thông báo thắng xoay cùng Color Game

## Đã sửa

- YOU WIN / BIG WIN nằm trong khung game đã xoay; bỏ thao tác xoay lặp lại trên hiệu ứng con.
- Căn giữa bảng thưởng theo chiều ngang, giảm chiều cao để chữ và số tiền vừa màn hình điện thoại.
- Tia sáng và hiệu ứng hạt đi cùng hướng bảng thưởng. Animation phóng to không ghi đè hướng xoay.
- Toast bên ngoài khung game xoay một lần, hỗ trợ thông báo dài và xuống dòng.
- Popup nhận Gold, Chip và Jackpot tiếp tục xoay độc lập một lần, nút xác nhận hoạt động.
- Đổi chiều điện thoại khi đang hiện thưởng vẫn giữ đúng hướng. Rời Color Game trả lại giao diện bình thường.

## Kiểm thử bản đóng gói

Chạy `python3 tools/run_color_win_orientation.py` với Playwright và Chromium, trên tài nguyên lấy từ chính release JAR.

Đã qua 5 kích thước 390×844, 844×390, 360×800, 667×375, 320×568, ở cả chế độ animation thường và giảm chuyển động. Kiểm tra góc xoay thực tế qua chuỗi transform cha/con, giới hạn khung nhìn, YOU WIN/BIG WIN, toast dài, popup Jackpot/Gold/Chip và bấm nhận. Đã kiểm tra desktop, xoay khi đang hiện thưởng, tự đóng hiệu ứng, rời game và không ảnh hưởng hiệu ứng game khác. Không có lỗi JavaScript.

Kiểm thử giao diện dùng dữ liệu thưởng giả lập có kiểm soát để gọi các hàm thông báo thật; không phát sinh cược hoặc giao dịch database. Không chạy lại bộ kiểm thử backend hay thử trên điện thoại thật trong bản sửa CSS này.

## Cập nhật

Source và JAR đều có CSS mới; mã cache giao diện tăng lên 42-win-landscape. Toàn bộ Java classes, migration và thư viện backend được giữ nguyên từng byte so với V35. Schema vẫn V20, không cần chạy SQL hay reset database.

Dừng server cũ, dùng bản mới với cấu hình database hiện có rồi chạy start.bat. Tải lại trang trên điện thoại.
