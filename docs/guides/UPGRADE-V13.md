# V13 — hướng dẫn cập nhật

## Chạy localhost

1. Cài Docker Desktop, sao chép `.env.example` thành `.env`, thay các mật khẩu và JWT_SECRET.
2. Chạy `docker compose up -d --build` tại thư mục này.
3. Mở http://localhost:8080. Khách xem và chọn theme được, phải đăng ký/đăng nhập mới quay.
4. Creator đăng nhập bằng tài khoản trong `.env`, tạo Super Agent rồi Agent. Mỗi tài khoản có mã 6 số.
5. Player chọn nút Register màu vàng, nhập Username và Password (tối thiểu 6 ký tự).
   Không cần mã Agent hoặc tên hiển thị; xem SIMPLE-AUTH.md cho cấu hình MySQL local.

Chạy Java trực tiếp: Java 21, MySQL 8.4, cấu hình DB_URL/DB_USER/DB_PASSWORD và các biến
trong `.env`, chạy `start.bat` hoặc `./start.sh`. Để build: `mvn -f server/pom.xml verify`.

## Client và server

- `client/`: toàn bộ giao diện, ngôn ngữ, theme, hiệu ứng, âm thanh.
- `server/`: Spring Boot Java 21, MySQL, migrations và kiểm thử.
- `release/`: JAR chạy sẵn.
- `tools/`: mô phỏng RTP và kiểm tra giao diện.

Source đã tách hai folder. Bản JAR phục vụ cả client để chạy localhost bằng một địa chỉ.
Có thể host client riêng qua reverse proxy cùng origin với API.

## Phân cấp và chip

Creator chỉnh cấp và quản lý toàn hệ thống; Super Agent chỉnh Player/Agent thuộc nhánh
của mình; Agent quản lý Player thuộc nhánh. Trong Accounts có Edit, chọn cấp và tài khoản
cha mới. Phải chuyển hết cấp dưới trước khi đổi cấp của một tài khoản đang quản lý người khác.
Phải kết thúc Free Spin và dừng autoplay trước khi đổi Player sang cấp quản lý.
Không cho chỉnh chính tài khoản đang đăng nhập. Đổi cấp/di chuyển/khóa thu hồi phiên của tài khoản đó.

Give/Take trừ một ví và cộng ví còn lại trong một transaction, có kiểm tra số dư,
phạm vi và request ID. Chỉ Creator có Issue để phát hành chip ảo. Tài khoản quản lý mới có
0 chip; Player có 10.000 chip chào mừng như bản trước. Yêu cầu nạp do Agent/Super Agent
duyệt cũng phải có số dư tương ứng; rút được duyệt cộng về ví quản lý.

Chip history ghi nạp/rút và chuyển chip; win/loss chỉ tính cược và payout, không tính chuyển chip.
Mỗi lượt lưu Agent/Super Agent tại thời điểm quay; di chuyển Player không chuyển doanh số cũ.
Báo cáo tuần đóng từ 06:00 thứ Hai theo REPORT_ZONE, mặc định Asia/Manila.
Hoa hồng mặc định 30%, Creator chọn lại tỷ lệ trước duyệt. Nếu Agent đổi Super Agent giữa
tuần, bảng tổng hoa hồng Agent của tuần thuộc Super Agent của lượt cuối tuần đó;
báo cáo cược vẫn phân theo Super Agent tại thời điểm từng lượt.

## JWT

Dùng Auth0 java-jwt 4.5.0, HS256, issuer/audience cố định. Access JWT 15 phút,
refresh token ngẫu nhiên 7 ngày, xoay token sau mỗi lần refresh. Cả hai nằm trong cookie
HttpOnly/SameSite Strict; production bật Secure. Server kiểm tra phiên và quyền hiện tại
trong MySQL; không lấy quyền từ dữ liệu client. Mật khẩu BCrypt. Giữ JWT_SECRET ổn định
qua restart. Đổi secret sẽ buộc đăng nhập lại. Tham khảo: https://github.com/auth0/java-jwt

## RTP 97% → 96%

Mốc đầu là lần V13 khởi tạo server thành công, lưu trong MySQL. Sau đúng 7 × 24 giờ,
toàn server chuyển INTRO_97 sang STANDARD_96. Restart không bắt đầu lại tuần đầu.
Free Spin giữ profile từ lượt kích hoạt đến hết chuỗi; chuỗi cũ từ V12 giữ LEGACY.
Không thay đổi xác suất theo từng tài khoản, số dư hoặc lịch sử thua thắng.

Các mức là mục tiêu dài hạn được hiệu chỉnh bằng mô phỏng, không bảo đảm tổng payout
của từng tuần đúng 97%/96%. Trang game hiển thị mục tiêu, ngày chuyển và RTP quan sát
của profile hiện tại. Payout Free Spin được tính; chỉ cược trả phí vào mẫu số.
RTP quan sát có thể thay đổi khi các bonus chưa chơi hết. Xem RTP-REPORT.md để biết mẫu và sai số.

## Nâng từ V12

Sao lưu MySQL trước khi cập nhật, giữ nguyên volume và biến cấu hình, bổ sung JWT_SECRET.
Flyway chạy V2 tự động, giữ tài khoản/ví/lịch sử, cấp mã 6 số cho tài khoản cũ.
Phiên V12 không phải JWT nên người dùng cần đăng nhập lại một lần.
Không xóa volume MySQL khi thay bản. Không dùng JAR V12 sau khi đã nâng schema.
