# IT209 - Session 07 - Bài tập 1: Quản lý người dùng giới hạn và Truyền tải dữ liệu qua SFTP trên Windows

- **Sinh viên:** Phạm Thanh Phong
- **MSSV:** N24DTCN120
- **Đường dẫn nộp bài:** `homework/session_07/ex1/`

---

## Danh mục tệp tin trong dự án

| Tên tệp tin | Mô tả chức năng |
| :--- | :--- |
| [IT209_Session07_BT1_PhamThanhPhong_N24DTCN120.md](./IT209_Session07_BT1_PhamThanhPhong_N24DTCN120.md) | Báo cáo chuyên sâu đầy đủ các bước thực hiện, phân tích bảo mật và đối chiếu Checksum |
| [Main.java](./Main.java) | Ứng dụng Java mô phỏng phân tích Least Privilege, mô phỏng SFTP transfer và xác thực SHA-256 |
| [setup_sftp_user.sh](./setup_sftp_user.sh) | Script tự động hóa khởi tạo user `sftp-user`, tạo folder log và phân quyền 750/640 |
| [verify.sh](./verify.sh) | Script kiểm tra và xác thực các quyền POSIX, quyền sudo và mã băm SHA-256 |

---

## Tóm tắt quy trình thực hiện

1. **Khởi tạo và cấu hình người dùng trên VPS:**
   ```bash
   sudo chmod +x setup_sftp_user.sh verify.sh
   ./setup_sftp_user.sh
   ./verify.sh
   ```
2. **Thực thi mô phỏng và kiểm tra bằng Java:**
   ```bash
   javac Main.java
   java Main
   ```
3. **Kết nối SFTP từ Windows:**
   - Dùng **Bitvise SSH Client** hoặc **WinSCP**.
   - Host: `<IP_VPS>`, Port: `22`, User: `sftp-user`.
   - Điều hướng tới `/var/log/app-backup/` và tải file `backup-check.log` về máy cục bộ.
4. **Kiểm tra Checksum trên PowerShell:**
   ```powershell
   Get-FileHash -Algorithm SHA256 .\backup-check.log
   ```
