# Current update: Lobby / Club, schema V6

Read [UPDATE-LOBBY-CLUB.md](UPDATE-LOBBY-CLUB.md) for current behavior and [database/README.md](database/README.md) for SQL installation/upgrades. Full SQL: `database/super_ace.sql`. Fresh-install Creator: `zuchiha` / `112357`.

Registration now has three fields. New Players receive 10,000 Lobby Gold and zero Club chips. Player chip requests are disabled; management transfers produce receipt notifications. The notes below document earlier releases and are superseded by this update where they differ.

# Super Ace Online V13

**Cập nhật đăng ký:** chỉ Username + Password (tối thiểu 6 ký tự), không cần mã Agent.
MySQL đã chạy sẵn trên Windows: chạy `start-local.bat`. Xem [SIMPLE-AUTH.md](SIMPLE-AUTH.md).

Bắt đầu với [UPGRADE-V13.md](UPGRADE-V13.md): cấu hình localhost, MySQL, JWT, đăng ký, phân cấp, chuyển chip và lịch RTP.

- Source giao diện: `client/`
- Spring Boot Java 21: `server/`
- JAR: `release/super-ace-online-13.0.0.jar`
- Kết quả mô phỏng: [RTP-REPORT.md](RTP-REPORT.md)
- Kiểm thử: [VERIFICATION.md](VERIFICATION.md)

Game độc lập dùng chip ảo, lấy cảm hứng từ Super Ace. Không phải sản phẩm của JILI.
