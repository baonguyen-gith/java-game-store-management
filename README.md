# Hệ Thống Quản Lý Cửa Hàng Game (Java Swing & SQL Server)

Hệ thống quản lý cửa hàng game (**QLGAME**) được phát triển bằng ngôn ngữ **Java**, sử dụng bộ thư viện giao diện **Java Swing**, quản lý dự án bằng **Maven** và thiết kế theo kiến trúc **MVC** (Model-View-Controller) kết hợp mô hình DAO (Data Access Object) chuyên nghiệp.

---

## 1. Hướng Dẫn Cài Đặt (Installation)

Để chạy dự án này trên máy tính của bạn, vui lòng đảm bảo đã cài đặt đầy đủ các thành phần môi trường sau:

### Yêu Cầu Phần Mềm
1. **Java Development Kit (JDK)**: Phiên bản **17** trở lên.
2. **Microsoft SQL Server**: Phiên bản 2016 trở lên (bao gồm công cụ **SQL Server Management Studio - SSMS**).
3. **Apache Maven**: Đảm bảo lệnh `mvn` có thể chạy từ Terminal/Cmd.
4. **IDE đề xuất**: VS Code (cài thêm Extension Pack for Java), IntelliJ IDEA hoặc NetBeans.

---

## 2. Hướng Dẫn Import Database

Hệ thống sử dụng cơ sở dữ liệu Microsoft SQL Server. Vui lòng thực hiện các bước sau để thiết lập cơ sở dữ liệu:

1. Mở phần mềm **SQL Server Management Studio (SSMS)** và kết nối với máy chủ SQL Server của bạn.
2. Tạo một cơ sở dữ liệu mới có tên là `QLGAME` (hoặc tên tùy chọn của bạn):
   ```sql
   CREATE DATABASE QLGAME;
   ```
3. Mở và thực thi toàn bộ nội dung tệp tin khởi tạo cấu trúc bảng:
   * Đường dẫn: [database/database.sql]
4. Mở và thực thi tiếp nội dung tệp tin chứa dữ liệu mẫu để chạy thử hệ thống:
   * Đường dẫn: [database/Data_Sample.sql]
5. **Cấu hình kết nối**:
   * Mở tệp tin kết nối cơ sở dữ liệu tại dự án: [src/main/java/otkhongluong/gamestoremanagement/util/DBConnection.java]
   * Chỉnh sửa cấu hình `URL`, `username` và `password` cho khớp với tài khoản SQL Server trên máy tính cá nhân của bạn.

---

## 3. Hướng Dẫn Chạy Chương Trình (Execution)

### Chạy từ Terminal / Command Prompt
Di chuyển terminal vào thư mục gốc của dự án (`java-game-store-management`) và thực hiện lần lượt các lệnh sau:

1. Dọn dẹp và biên dịch dự án:
   ```bash
   mvn clean compile
   ```
2. Chạy ứng dụng:
   ```bash
   mvn exec:java -Dexec.mainClass="otkhongluong.gamestoremanagement.Main"
   ```

### Chạy trực tiếp trên IDE
* **VS Code / IntelliJ**: Tìm đến lớp `App.java` (đường dẫn: [src/main/java/otkhongluong/gamestoremanagement/Main.java]), click chuột phải chọn **Run** hoặc bấm biểu tượng **Run** (mũi tên xanh).

---

## 4. Tài Khoản Mẫu Hệ Thống (Sample Credentials)

Dưới đây là danh sách các tài khoản mẫu đã được cấu hình sẵn trong dữ liệu mẫu của hệ thống để bạn dễ dàng chạy và thử nghiệm các tính năng phân quyền:

| STT | Tài Khoản (Username) | Mật Khẩu (Password) | Vai Trò (Role) | Chức năng chính được phép |
| :---: | :--- | :--- | :--- | :--- |
| **1** | `admin` | `Password@123` | **Admin** | Toàn quyền kiểm soát hệ thống, quản lý tài khoản, nhân viên, cấu hình game, hóa đơn, và báo cáo doanh thu. |
| **2** | `quanly` | `Password@123` | **Quản lý** | Quản lý kho game, khách hàng, sản phẩm (CD/ROM), xem thống kê doanh thu hệ thống. Không có quyền quản lý nhân viên và tài khoản. |
| **3** | `nhanvien1` | `Password@123` | **Nhân viên bán hàng** | Giao diện thu ngân: Tạo hóa đơn mua game, thuê đĩa game, trả đĩa game, quản lý tích điểm và thông tin khách hàng. |
| **4** | `nhanvien2` | `Password@123` | **Nhân viên bán hàng** | Tương tự nhân viên bán hàng 1. |
| **5** | `nhanvien3` | `Password@123` | **Nhân viên bán hàng** | Tương tự nhân viên bán hàng 1. |

> [!NOTE]  
> Các mật khẩu mẫu trên được lưu dưới dạng plaintext để dễ dàng phát triển. Khi đăng nhập lần đầu tiên thành công, hệ thống sẽ **tự động hash mật khẩu** bằng thuật toán mã hóa **BCrypt** theo chuẩn bảo mật production và lưu đè lên mật khẩu cũ trong database để đảm bảo an toàn tuyệt đối.
