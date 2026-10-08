#!/usr/bin/env bash
# ==============================================================================
# Học phần: IT209 - Phát triển ứng dụng Web / Cloud Infrastructure
# Session 07 - Bài tập 1: Quản lý người dùng giới hạn và Truyền tải dữ liệu qua SFTP trên Windows
# Sinh viên: Phạm Thanh Phong - MSSV: N24DTCN120
#
# Mục đích: Tự động hóa khởi tạo tài khoản sftp-user giới hạn, tạo thư mục log và phân quyền 750/640
# ==============================================================================

set -euo pipefail

echo "=============================================================================="
echo " KHỞI TẠO CẤU HÌNH USER GIỚI HẠN & LOG SYSTEM CHO SFTP (IT209 - SESSION 07 - BT1)"
echo " Sinh viên: Phạm Thanh Phong | MSSV: N24DTCN120"
echo "=============================================================================="

# 1. Khởi tạo người dùng sftp-user (nếu chưa tồn tại)
if id "sftp-user" &>/dev/null; then
    echo "[+] Tài khoản 'sftp-user' đã tồn tại trong hệ thống."
else
    echo "[+] Đang khởi tạo tài khoản 'sftp-user'..."
    sudo adduser --gecos "" --disabled-password sftp-user
    echo "sftp-user:StrongPassword@2026!" | sudo chpasswd
    echo "[OK] Đã tạo tài khoản 'sftp-user' với mật khẩu bảo mật."
fi

# Đảm bảo user sftp-user không có quyền sudo
sudo deluser sftp-user sudo 2>/dev/null || true

# 2. Khởi tạo thư mục chứa log giả lập
LOG_DIR="/var/log/app-backup"
LOG_FILE="${LOG_DIR}/backup-check.log"

echo "[+] Khởi tạo cấu trúc thư mục lưu trữ: ${LOG_DIR}"
sudo mkdir -p "${LOG_DIR}"

# 3. Tạo tệp tin nhật ký giả lập với mốc thời gian thực
echo "[+] Tạo dữ liệu nhật ký hệ thống vào: ${LOG_FILE}"
sudo bash -c "cat <<EOF > ${LOG_FILE}
Backup status: SUCCESS at $(date '+%Y-%m-%d %H:%M:%S')
[INFO] Database dump completed successfully: app_db_prod.sql.gz
[INFO] Static web assets archived: /var/www/my-app/public
[INFO] Data size: 45.2 MB | Compression ratio: 72%
[INFO] Checksum MD5 generated: 9e107d9d372bb6826bd81d3542a419d6
[INFO] Backup integrity verified: PASSED
EOF"

# 4. Phân quyền và gán quyền sở hữu theo nguyên tắc Least Privilege (PoLP)
echo "[+] Gán quyền sở hữu: Owner = root, Group = sftp-user"
sudo chown -R root:sftp-user "${LOG_DIR}"

echo "[+] Phân quyền thư mục 750 (u=rwx, g=rx, o=) cho: ${LOG_DIR}"
sudo chmod 750 "${LOG_DIR}"

echo "[+] Phân quyền tệp tin 640 (u=rw, g=r, o=) cho: ${LOG_FILE}"
sudo chmod 640 "${LOG_FILE}"

echo ""
echo "=============================================================================="
echo " THIẾT LẬP HOÀN TẤT THÀNH CÔNG!"
echo "=============================================================================="
