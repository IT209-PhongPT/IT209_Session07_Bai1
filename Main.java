import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Học phần: IT209 - Phát triển ứng dụng Web / Cloud Infrastructure
 * Session 07 - Bài tập 1: Quản lý người dùng giới hạn và Truyền tải dữ liệu qua SFTP trên Windows
 * 
 * Sinh viên: Phạm Thanh Phong
 * MSSV: N24DTCN120
 * 
 * Chương trình Java mô phỏng và xác thực:
 * 1. Phân tích mô hình phân quyền bảo mật Least Privilege cho tài khoản `sftp-user`.
 * 2. Giả lập cơ chế truyền tải tệp tin qua giao thức SFTP (SSH File Transfer Protocol - Port 22).
 * 3. Kiểm định tính toàn vẹn dữ liệu (Data Integrity) bằng thuật toán mã băm SHA-256 giữa máy chủ Linux và máy trạm Windows.
 */
public class Main {

    public record UserSecurityProfile(
            String username,
            String primaryGroup,
            List<String> supplementaryGroups,
            boolean hasSudoPrivilege,
            String shell,
            String homeDirectory,
            String accessScope
    ) {}

    public record RemoteFileMetadata(
            String remotePath,
            String owner,
            String group,
            String octalPermissions,
            String symbolicPermissions,
            long sizeBytes,
            String simulatedContent,
            String sha256Checksum
    ) {}

    public static void main(String[] args) {
        printHeader();

        // 1. Phân tích hồ sơ an ninh của tài khoản sftp-user
        UserSecurityProfile sftpUser = new UserSecurityProfile(
                "sftp-user",
                "sftp-user",
                List.of("sftp-user"),
                false, // Không thuộc nhóm sudo/wheel
                "/bin/bash",
                "/home/sftp-user",
                "Chỉ được cấp quyền đọc thư mục /var/log/app-backup và file backup-check.log"
        );

        displayUserProfile(sftpUser);

        // 2. Thiết lập siêu dữ liệu tệp tin giả lập trên máy chủ Linux VPS
        String simulatedTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logContent = "Backup status: SUCCESS at " + simulatedTimestamp + "\n"
                + "[INFO] Database dump completed (app_db_prod.sql.gz)\n"
                + "[INFO] Static assets archived (/var/www/uploads)\n"
                + "[INFO] Checksum verification: PASSED\n";

        String logHash = calculateSHA256(logContent);

        RemoteFileMetadata remoteLog = new RemoteFileMetadata(
                "/var/log/app-backup/backup-check.log",
                "root",
                "sftp-user",
                "640",
                "-rw-r-----",
                logContent.getBytes(StandardCharsets.UTF_8).length,
                logContent,
                logHash
        );

        displayFilePermissions(remoteLog);

        // 3. Mô phỏng quy trình kết nối & truyền tải SFTP Client từ Windows
        simulateSftpTransferProcess(sftpUser, remoteLog);

        // 4. Kiểm định tính toàn vẹn dữ liệu (Integrity Verification)
        verifyDataIntegrity(remoteLog, logContent);

        // 5. Tổng kết quy tắc bảo mật thực chiến
        printSecuritySummary();
    }

    private static void printHeader() {
        System.out.println("==========================================================================================");
        System.out.println(" IT209 - SESSION 07 - BÀI TẬP 1: QUẢN LÝ USER GIỚI HẠN & TRUYỀN TẢI SFTP WINDOWS/LINUX   ");
        System.out.println(" Sinh viên: Phạm Thanh Phong | MSSV: N24DTCN120                                           ");
        System.out.println("==========================================================================================");
    }

    private static void displayUserProfile(UserSecurityProfile user) {
        System.out.println("\n[1] HỒ SƠ BẢO MẬT TÀI KHOẢN NGƯỜI DÙNG GIỚI HẠN (LEAST PRIVILEGE USER):");
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.printf(" %-22s: %s%n", "Tên người dùng (User)", user.username());
        System.out.printf(" %-22s: %s%n", "Nhóm chính (Primary GID)", user.primaryGroup());
        System.out.printf(" %-22s: %s%n", "Nhóm phụ (Groups)", user.supplementaryGroups());
        System.out.printf(" %-22s: %s%n", "Quyền sudo (Superuser)", user.hasSudoPrivilege() ? "CÓ (NGUY HIỂM)" : "KHÔNG (AN TOÀN)");
        System.out.printf(" %-22s: %s%n", "Thư mục Home", user.homeDirectory());
        System.out.printf(" %-22s: %s%n", "Phạm vi truy cập", user.accessScope());
        System.out.println("------------------------------------------------------------------------------------------");
    }

    private static void displayFilePermissions(RemoteFileMetadata file) {
        System.out.println("\n[2] CẤU HÌNH PHÂN QUYỀN TỆP TIN NHẬT KÝ TRÊN MÁY CHỦ LINUX (VPS):");
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.printf(" %-22s: %s%n", "Đường dẫn từ xa", file.remotePath());
        System.out.printf(" %-22s: %s:%s%n", "Quyền sở hữu (Owner:Group)", file.owner(), file.group());
        System.out.printf(" %-22s: %s (Ký hiệu: %s)%n", "Mã phân quyền", file.octalPermissions(), file.symbolicPermissions());
        System.out.printf(" %-22s: %d bytes%n", "Kích thước tệp tin", file.sizeBytes());
        System.out.printf(" %-22s: %s%n", "Mã băm SHA-256 (Gốc)", file.sha256Checksum());
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.println(" Phân tích quyền 640 (-rw-r-----):");
        System.out.println("   - Owner (root)       : Đọc + Ghi (rw-) -> Cho phép tiến trình backup ghi log");
        System.out.println("   - Group (sftp-user)  : Chỉ Đọc (r--)   -> Cho phép sftp-user đọc và tải về qua SFTP");
        System.out.println("   - Others             : Không quyền (---) -> Chặn toàn bộ người dùng khác trên VPS");
    }

    private static void simulateSftpTransferProcess(UserSecurityProfile user, RemoteFileMetadata file) {
        System.out.println("\n[3] MÔ PHỎNG QUY TRÌNH KẾT NỐI SFTP CLIENT TỪ HỆ ĐIỀU HÀNH WINDOWS:");
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.println(" [BƯỚC 1] Khởi tạo kết nối SSH Handshake tới Server (Port 22/TCP)...");
        System.out.println("          -> Client: Bitvise SSH Client / WinSCP trên Windows");
        System.out.println("          -> Trao đổi khóa bảo mật (Key Exchange: Curve25519-SHA256)");
        System.out.println(" [BƯỚC 2] Xác thực danh tính người dùng:");
        System.out.printf("          -> User: '%s' | Authentication: Password Authentication%n", user.username());
        System.out.println("          -> Kết quả xác thực: THÀNH CÔNG (Session Channel OPEN)");
        System.out.println(" [BƯỚC 3] Khởi chạy SFTP Subsystem (SSH File Transfer Protocol):");
        System.out.println("          -> Subsystem: /usr/lib/openssh/sftp-server");
        System.out.println(" [BƯỚC 4] Định hướng thư mục từ xa (Remote Browser):");
        System.out.printf("          -> Duyệt tới thư mục: /var/log/app-backup/%n");
        System.out.println(" [BƯỚC 5] Tải tệp tin về máy tính cục bộ (Download Operation):");
        System.out.printf("          -> Tải '%s' về 'C:\\Users\\ACER\\Downloads\\backup-check.log'...%n", file.remotePath());
        System.out.println("          -> Tiến trình: [========================================] 100% HOÀN TẤT");
    }

    private static void verifyDataIntegrity(RemoteFileMetadata remoteFile, String localContent) {
        System.out.println("\n[4] KIỂM ĐỊNH TÍNH TOÀN VẸN DỮ LIỆU (DATA INTEGRITY AUDIT):");
        System.out.println("------------------------------------------------------------------------------------------");
        String localHash = calculateSHA256(localContent);

        System.out.printf(" %-25s: %s%n", "Mã SHA-256 trên Linux VPS", remoteFile.sha256Checksum());
        System.out.printf(" %-25s: %s%n", "Mã SHA-256 trên Windows", localHash);

        boolean isMatched = remoteFile.sha256Checksum().equalsIgnoreCase(localHash);
        System.out.printf(" %-25s: %s%n", "Kết quả so khớp Checksum", isMatched ? "TRÙNG KHỚP TUYỆT ĐỐI (100% INTEGRITY)" : "SAI LỆCH (CORRUPTED)");

        if (isMatched) {
            System.out.println("\n Nội dung tệp tin đã tải về trên Windows:");
            System.out.println(" ----------------------------------------------------");
            for (String line : localContent.split("\n")) {
                System.out.println(" | " + line);
            }
            System.out.println(" ----------------------------------------------------");
        }
    }

    private static void printSecuritySummary() {
        System.out.println("\n[5] TỔNG KẾT BẢO MẬT & KIẾN NGHỊ VẬN HÀNH THỰC TẾ:");
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.println(" 1. Nguyên tắc Least Privilege: sftp-user không nằm trong nhóm sudo/wheel, chỉ có quyền");
        System.out.println("    đọc tại thư mục quy định, không thể thao tác sửa xóa cấu hình hệ thống.");
        System.out.println(" 2. Kiểm soát phân quyền thư mục cha: Thư mục /var/log/app-backup phải có quyền 750");
        System.out.println("    (hoặc 755) để user có thể thực thi (traverse) vào lấy danh sách tệp.");
        System.out.println(" 3. Phân quyền tệp tin: Tệp 640 (-rw-r-----) chỉ cho phép Group đọc, ngăn ngừa rò rỉ dữ liệu");
        System.out.println("    sang các tài khoản khác trên máy chủ.");
        System.out.println("==========================================================================================");
    }

    private static String calculateSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return "HASH_CALCULATION_ERROR";
        }
    }
}
