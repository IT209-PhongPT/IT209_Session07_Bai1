#!/usr/bin/env bash
# ==============================================================================
# Script kiểm tra và xác thực cấu hình SFTP, tài khoản và phân quyền tệp tin
# Học phần: IT209 - Session 07 - BT1
# Sinh viên: Phạm Thanh Phong - MSSV: N24DTCN120
# ==============================================================================

LOG_DIR="/var/log/app-backup"
LOG_FILE="${LOG_DIR}/backup-check.log"

echo "=============================================================================="
echo " KIỂM TRA ĐỘ CHÍNH XÁC CẤU HÌNH SFTP (IT209 - SESSION 07 - BT1)"
echo "=============================================================================="

# 1. Kiểm tra danh tính và quyền hạn của sftp-user
echo ""
echo "[1] KIỂM TRA THÔNG TIN USER VÀ GROUP:"
id sftp-user

echo ""
echo "[2] KIỂM TRA QUYỀN SUDO CỦA USER (Mong muốn: Không có quyền sudo):"
if sudo -l -U sftp-user 2>&1 | grep -q "not allowed to run sudo"; then
    echo "[PASS] sftp-user KHÔNG có quyền sudo (Tuân thủ nguyên tắc Least Privilege)."
else
    echo "[WARNING] Kiểm tra lại quyền sudo của sftp-user!"
fi

# 2. Kiểm tra phân quyền thư mục và tệp tin
echo ""
echo "[3] KIỂM TRA PHÂN QUYỀN THƯ MỤC VÀ TỆP TIN:"
echo "--- Danh sách tệp tin dạng dài (ls -la): ---"
ls -ld "${LOG_DIR}"
ls -l "${LOG_FILE}"

echo ""
echo "--- Thông số Octal & Quyền chi tiết (stat): ---"
stat -c "Thư mục: %n | Octal: %a | Ký hiệu: %A | Sở hữu: %U:%G" "${LOG_DIR}"
stat -c "Tệp tin: %n | Octal: %a | Ký hiệu: %A | Sở hữu: %U:%G" "${LOG_FILE}"

# 3. Kiểm tra khả năng đọc tệp log của sftp-user
echo ""
echo "[4] KIỂM TRA ĐỌC NỘI DUNG TỆP LOG DƯỚI TƯ CÁCH sftp-user:"
sudo -u sftp-user cat "${LOG_FILE}"

# 4. Kiểm tra checksum để đối chiếu khi tải về Windows
echo ""
echo "[5] MÃ BĂM CHECKSUM GỐC TRÊN LINUX VPS ĐỂ ĐỐI CHIẾU VỚI WINDOWS:"
echo -n "SHA-256: " && sha256sum "${LOG_FILE}"
echo -n "MD5    : " && md5sum "${LOG_FILE}"

echo ""
echo "=============================================================================="
echo " HOÀN TẤT KIỂM ĐỊNH HỆ THỐNG!"
echo "=============================================================================="
