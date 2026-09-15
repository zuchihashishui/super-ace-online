# Đăng ký đơn giản và chạy với MySQL local

Form Register chỉ có Username và Password. Username gồm 3–40 chữ cái không dấu,
số hoặc dấu gạch dưới. Password tối thiểu 6 ký tự, tối đa 72 byte UTF-8.
Mật khẩu vẫn được băm BCrypt; không lưu dạng rõ.

Server tự sinh ID 6 số và đặt tên hiển thị bằng Username. Tài khoản tự đăng ký
được xếp vào nhóm Direct players thuộc Creator. Nhóm có hoa hồng mặc định 0%.
Creator chuyển Player sang Agent bất kỳ qua Accounts → Edit. Hai tài khoản nhóm
hệ thống được tạo với mật khẩu ngẫu nhiên không cung cấp cho người dùng; không
đổi cấp/khóa/di chuyển các nhóm này để tránh làm hỏng đường đăng ký.

## Windows, MySQL đã chạy sẵn

1. Cài Java 21; MySQL của bạn chạy tại localhost:3306.
2. Giải nén bản mới vào thư mục riêng rồi chạy `start-local.bat`.
3. Mở http://localhost:8080 và chọn Register.

Lần đầu script tạo `.env.local`: DB_USER=root, DB_PASSWORD=123456, database=ace.
Nếu database ace chưa có, kết nối yêu cầu tạo database; Flyway tạo/cập nhật bảng.
Nếu bạn dùng tên database hoặc port khác, chỉnh DB_URL trong `.env.local`.
Không chạy Docker database khi dùng cách này. Nếu cổng 8080 đang bận, dừng bản
server cũ trước khi mở bản mới.

Creator mặc định cho lần khởi tạo mới: username `creator`, password `creator123`.
Nếu database đã có Creator, mật khẩu cũ được giữ nguyên. JWT_SECRET được tạo
ngẫu nhiên và lưu trong `.env.local`; giữ file này qua các lần chạy.
Các thông tin mặc định này chỉ dành cho localhost, không đưa `.env.local` lên GitHub.

Đăng ký thành công sẽ đăng nhập ngay. Logout xóa cookie và thu hồi access/refresh
phiên hiện tại. Login lại dùng Username và Password đã tạo. Tài khoản cũ vẫn hoạt động.

Migration mới: V3__direct_registration.sql. Không cần xóa dữ liệu hay reset MySQL.
