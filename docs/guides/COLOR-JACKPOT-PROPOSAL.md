> Historical V34 proposal. V35 implements the Lobby pilot; see [current rules](V35-COLOR-JACKPOT.md).

# Đề xuất Jackpot cho Color Game — chưa kích hoạt

## Những gì đọc được trực tiếp từ ảnh người dùng gửi

- Ván phải ra ba mặt Xanh Dương, là màu Jackpot hiển thị trong ảnh.
- Tổng cược trong ván phải **lớn hơn** 10.000 Gold. Ảnh không nói rõ cược phải đặt vào màu Xanh Dương; không nên tự thêm điều kiện này.
- Khi cùng đáp ứng hai điều kiện, người chơi được chọn ngẫu nhiên một trong bốn hạng.

| Hạng | Mức trả ghi trong ảnh |
| --- | --- |
| Grand | 50% toàn bộ quỹ Jackpot |
| Major | 20% toàn bộ quỹ Jackpot |
| Minor | 5% toàn bộ quỹ Jackpot |
| Mini | 30.000 Gold cố định |

50%, 20%, 5% là tỷ lệ tiền thưởng trên quỹ, **không phải xác suất chọn hạng**. Không có căn cứ để cho rằng bốn hạng có xác suất bằng nhau. Ảnh cũng không cho biết nguồn quỹ, mức quỹ khởi tạo, tỷ lệ trích, màu mục tiêu có đổi hay không, hay cách giải quyết nhiều người đủ điều kiện cùng một ván.

Tra cứu ngày 17/09/2026 chưa tìm được tài liệu chính thức công khai xác nhận đầy đủ cơ chế của đúng app trong ảnh. Trang nhà phát hành Tongits Go và trang ứng dụng có giới thiệu Color Game, nhưng không xác nhận bảng xác suất Jackpot này. Không kết luận ảnh chắc chắn thuộc Tongits Go chỉ từ giao diện tương tự.

- Trang nhà phát hành: https://tongitsgo.com/
- Trang ứng dụng: https://play.google.com/store/apps/details?id=com.tongitsgo.play&hl=en_US
- Tham khảo thiết kế hệ thống: https://www.gamblingcommission.gov.uk/standards/remote-gambling-and-software-technical-standards/rts-9-progressive-jackpot-systems

RTS 9 đề cập công khai nguồn quỹ, giá trị khởi tạo/trần, cách trả khi có nhiều người trúng, cập nhật quỹ và ghi nhật ký thay đổi cấu hình. Đây là tài liệu tham khảo kỹ thuật, không phải luật của app mẫu và không phải kết luận pháp lý cho Philippines.

## Đề xuất phù hợp với hệ thống hiện tại

Đây là thiết kế mới đề xuất, không phải cơ chế đã xác minh của app mẫu; bản V34 chưa thực thi các giá trị dưới đây.

1. Thử nghiệm trên **Lobby Gold** trước. Club có quỹ, cấu hình và nhật ký hoàn toàn riêng; mặc định chưa bật.
2. Quỹ thử ban đầu 30.000 Gold, do Creator cấp và có ghi sổ. Bổ sung quỹ bằng 1% tổng cược đã được chấp nhận, từ phần phân bổ của hệ thống; không trừ thêm tiền người chơi và không giảm khoản trả cược thường. Tổng cược nhỏ được cộng dồn phần lẻ trước khi quy đổi đơn vị nhỏ nhất, tránh mất phần đóng góp do làm tròn mỗi click.
3. Mọi người có cược hợp lệ trong ván được tham gia, không bắt cược hơn 10.000. Điều này phù hợp mệnh giá 5–100 và Gold tặng mỗi ngày hiện tại. Cược ở màu nào cũng được; số lượt click không làm tăng quyền lợi nếu tổng cược không đổi.
4. Kết quả ba Xanh Dương kích hoạt một lần chọn hạng **chung cho cả ván**. Đề xuất xác suất hạng có điều kiện: Grand 0,1%; Major 0,9%; Minor 9%; Mini 90%. Công khai trong luật; không thay đổi theo tài khoản, thời gian chơi hay lịch sử thắng/thua.
5. Đề xuất quỹ chi theo hạng: Grand 50%, Major 20%, Minor 5%, Mini **1% quỹ**. Mini theo phần trăm khác ảnh 30.000 cố định: giúp tổng giải không vượt quỹ khi có nhiều người. Nếu giữ Mini cố định thì cần quỹ dự phòng và quy tắc bảo đảm đủ tiền riêng trước khi bật.
6. Chụp giá trị quỹ sau khi cộng đóng góp của ván và trước khi phát giải. Chia khoản giải chung cho những người có cược hợp lệ, theo tỷ trọng tổng cược của họ trong ván. Chỉ trừ quỹ đúng tổng giải một lần. Phần làm tròn được chia theo phần dư lớn nhất, hòa thì theo ID ổn định; không phụ thuộc thứ tự API đến server.
7. Nếu không có người cược hợp lệ thì không rút quỹ. Sau trúng, phần chưa chi giữ nguyên; không tự bịa số dư mới. Creator có thể bổ sung quỹ bằng giao dịch ghi sổ.

Ví dụ: quỹ 100.000 Gold, trúng Minor thì khoản giải chung là 5.000. A cược 100, B cược 400, không ai khác cược: A nhận 1.000 và B nhận 4.000; quỹ còn 95.000. Khoản trả cược thường được tính riêng. Ví dụ này không phải kết quả thật và không phải tính năng đã bật.

## Tần suất và tác động lên RTP

Với ba xúc xắc công bằng, độc lập, xác suất ba Xanh Dương là (1/6)^3 = 1/216 ≈ 0,463% mỗi ván. Ba màu giống nhau bất kỳ là 6/216 = 1/36, là điều kiện khác. 1/216 không có nghĩa ván thứ 216 chắc chắn xảy ra.

Nếu dùng trọng số đề xuất 0,1% Grand, xác suất một ván kích hoạt hạng Grand (khi có người đủ điều kiện) là 1/216.000. Người trong ván chia giải theo quy tắc trên; không quay riêng từng người từ cùng một quỹ.

Color Game hiện trả tổng 2× / 3× / 4× khi màu xuất hiện 1 / 2 / 3 lần. Với xúc xắc công bằng, RTP cược thường = (75×2 + 15×3 + 1×4)/216 = 199/216 ≈ 92,13%. Mục RTP của **Super Ace không điều khiển Color Game**. Quỹ Jackpot bổ sung có thể tăng tổng tiền trả, nhưng không được tự tuyên bố Color Game đạt 97,5%. 1% cược đưa vào quỹ, nếu cuối cùng trả hết cho người chơi, tương ứng thêm xấp xỉ 1 điểm phần trăm dài hạn, chưa kể quỹ do hệ thống cấp và phần còn chưa phát. Cần mô phỏng ngân sách/quỹ theo số người và mức cược trước khi chốt.

## Cách triển khai khi chốt cơ chế

Server Spring Boot quyết định đủ điều kiện, hạng và tiền thưởng. MySQL lưu quỹ theo mode, cấu hình có phiên bản, đóng góp, giải theo ván, phần nhận của mỗi người và receipt. Đóng góp và giải có khóa duy nhất để tải lại/retry không cộng hoặc phát hai lần. Khóa dòng quỹ trong transaction và thứ tự khóa ổn định; chỉ một lần settlement cho mỗi ván/mode. Cấu hình mới chỉ áp dụng từ ván kế tiếp. Mất mạng vẫn ghi có phía server và hiện thông báo khi người chơi quay lại.

UI hiển thị quỹ thật, luật và xác suất đã chốt, phần thưởng của mình, hiệu ứng Jackpot sau xác nhận server, cùng lịch sử trả thưởng. Kiểm thử phải bao gồm biên đủ điều kiện, cả bốn hạng, nhiều người cùng trúng, phần lẻ, quỹ thấp, retry, restart, thay đổi cấu hình và cách ly Lobby/Club. Không thay đổi kết quả xúc xắc để điều tiết quỹ.
