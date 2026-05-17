-- ========================
-- DỮ LIỆU MẪU
-- ========================
 
-- Tắt FK tạm thời để insert mượt
EXEC sp_MSforeachtable 'ALTER TABLE ? NOCHECK CONSTRAINT ALL';
GO
 
-- 1. ROLE
SET IDENTITY_INSERT ROLE ON;
INSERT INTO ROLE (MaRole, TenRole) VALUES
(1, N'Admin'),
(2, N'Quản lý'),
(3, N'Nhân viên bán hàng'),
(4, N'Nhân viên kỹ thuật');
SET IDENTITY_INSERT ROLE OFF;
 
-- 2. NHANVIEN
SET IDENTITY_INSERT NHANVIEN ON;
INSERT INTO NHANVIEN (MaNV, HoTen, SDT, NgaySinh, CCCD, NgayVaoLam) VALUES
(1, N'Nguyễn Văn A', '0912345678', '1995-05-10', '001095001234', '2023-01-01'),
(2, N'Trần Thị B',   '0987654321', '1998-11-20', '001098005678', '2023-05-15'),
(3, N'Lê Văn C',     '0905123456', '2000-02-15', '001200009999', '2024-02-01');
SET IDENTITY_INSERT NHANVIEN OFF;
 
-- 3. USERS
SET IDENTITY_INSERT USERS ON;
INSERT INTO USERS (MaUser, Username, Password, MaRole, MaNV) VALUES
(1, 'admin',      'pbkdf2_sha256_admin123', 1, 1),
(2, 'nv_banhang', 'pbkdf2_sha256_nv123',   3, 2);
SET IDENTITY_INSERT USERS OFF;
 
-- 4. KHACHHANG
SET IDENTITY_INSERT KHACHHANG ON;
INSERT INTO KHACHHANG (MaKH, HoTen, SDT, CCCD, Email, DiaChi, DiemTichLuy) VALUES
(1, N'Lý Tiểu Long',    '0909111222', '079123456789', 'long.ly@gmail.com',      N'Quận 5, TP.HCM', 150),
(2, N'Phạm Nhật Vượng', '0901000999', '001099111222', 'vuong.pham@vinhomes.vn', N'Hà Nội',         1000),
(3, N'Sơn Tùng MTP',    '0933555222', '034094001111', 'mtp@gmail.com',          N'Thái Bình',      50);
SET IDENTITY_INSERT KHACHHANG OFF;
 
-- 5. GAME
SET IDENTITY_INSERT GAME ON;
INSERT INTO GAME (MaGame, TenGame, TheLoai, NenTang, GhiChu, HinhAnh) VALUES
(1, N'GTA VI',           N'Hành động', N'PS5/Xbox', N'Bom tấn 2026',              'gta6.jpg'),
(2, N'Cyberpunk 2077',   N'Nhập vai',  N'PC',       N'Bản cập nhật Phantom Liberty','cyberpunk.png'),
(3, N'Resident Evil 9',  N'Kinh dị',   N'PC/PS5',   N'Sắp ra mắt',               're9.jpg'),
(4, N'Elden Ring',       N'Souls-like',N'PC/PS5',   N'Game của năm',              'elden.jpg'),
(5, N'FIFA 25',          N'Thể thao',  N'PS5/PC',   N'Bóng đá đỉnh cao',         'fifa25.jpg');
SET IDENTITY_INSERT GAME OFF;
 
-- 5.1 GAME_CHITIET
INSERT INTO GAME_CHITIET (MaGame, MoTa, Rating, Genre, DeliveryMethod, ReleaseDate, Region, Features, Language, Currency) VALUES
(1, N'Trò chơi thế giới mở rộng lớn nhất lịch sử', N'18+', N'Action/Open World', N'CD/Digital', '2026-01-01', N'Global', N'Multiplayer',   N'English/Vietnamese', N'VND'),
(2, N'Thành phố tương lai Night City',              N'18+', N'Sci-fi RPG',         N'Digital',    '2020-12-10', N'Global', N'Single Player', N'English',            N'VND');
 
-- 6. SANPHAM
-- GiaBan: giá bán CD/ROM
-- GiaThueNgay: giá thuê mỗi ngày (chỉ áp dụng cho CD)
SET IDENTITY_INSERT SANPHAM ON;
INSERT INTO SANPHAM (MaSP, MaGame, GiaBan, GiaThueNgay) VALUES
(1, 1, 1800000, 80000),
(2, 2,  800000, 30000),
(3, 3, 1450000, 50000),
(4, 4, 1200000, 40000),
(5, 5, 1600000, 60000);
SET IDENTITY_INSERT SANPHAM OFF;
 
-- 7. CD
-- TrangThai: SanSang | DangThue | DaBan | Hong
SET IDENTITY_INSERT CD ON;
INSERT INTO CD (MaCD, MaSP, TinhTrang, TrangThai) VALUES
(1, 1, N'Mới',       N'SanSang'),  -- GTA VI — sẵn sàng bán/cho thuê
(2, 1, N'99%',       N'DangThue'), -- GTA VI — đang được thuê PT2
(3, 3, N'Trầy nhẹ',  N'SanSang'),  -- RE9 — sẵn sàng
(4, 5, N'Mới',       N'SanSang'),  -- FIFA 25 — sẵn sàng
(5, 2, N'Mới',       N'DaBan');    -- Cyberpunk — đã bán qua HĐ1
SET IDENTITY_INSERT CD OFF;
 
-- 8. ROM (chỉ Cyberpunk và Elden Ring có ROM)
INSERT INTO ROM (MaSP, DungLuong, LinkLuuTru, SoLuotBan) VALUES
(2, N'80GB', 'drive.google.com/cyberpunk', 120),
(4, N'60GB', 'fshare.vn/eldenring',        450);
 
-- 9. HOADON
-- MaKH NULL = khách vãng lai (FK cho phép NULL)
SET IDENTITY_INSERT HOADON ON;
INSERT INTO HOADON (MaHD, MaKH, MaNV, NgayLap, TongTien, DiemSuDung, TienGiam, TrangThai) VALUES
(1, 1, 2, '2026-04-01 10:00:00',  800000, 10, 10000, N'DaThanhToan'),
(2, 2, 2, '2026-04-05 14:30:00', 1200000,  0,     0, N'DaThanhToan');
SET IDENTITY_INSERT HOADON OFF;
 
-- 10. CTHOADON
-- HĐ1: KH1 mua Cyberpunk ROM (MaSP=2)
-- HĐ2: KH2 mua Elden Ring ROM (MaSP=4)
INSERT INTO CTHOADON (MaHD, MaSP, SoLuong, DonGia) VALUES
(1, 2, 1,  800000),
(2, 4, 1, 1200000);
 
-- 11. PHIEUTHUE
SET IDENTITY_INSERT PHIEUTHUE ON;
INSERT INTO PHIEUTHUE (MaPT, MaKH, NgayThue, NgayTraDuKien, NgayTraThucTe, TienCoc, TienPhat, TrangThai) VALUES
(1, 1, '2026-04-10 08:00:00', '2026-04-15 08:00:00', '2026-04-15 08:00:00', 200000, 0, N'DaTra'),
(2, 3, '2026-04-20 09:00:00', '2026-04-25 09:00:00', NULL,                  300000, 0, N'DangThue');
SET IDENTITY_INSERT PHIEUTHUE OFF;
 
-- 12. CTPHIEUTHUE
-- PT1: KH1 thuê CD1 (GTA VI, MaCD=1) — đã trả, CD1 về SanSang
-- PT2: KH3 thuê CD2 (GTA VI, MaCD=2) — đang thuê
INSERT INTO CTPHIEUTHUE (MaPT, MaCD, MaNV, DonGiaThue) VALUES
(1, 1, 2, 80000),
(2, 2, 2, 80000);
 
-- Cập nhật CD1 về SanSang vì PT1 đã trả
UPDATE CD SET TrangThai = N'SanSang' WHERE MaCD = 1;
 
-- 13. DIEM_LICHSU
-- Loai: 'CONG' hoặc 'TRU' — khớp với PointDAO trong Java
-- MaPT NULL = từ hóa đơn
SET IDENTITY_INSERT DIEM_LICHSU ON;
INSERT INTO DIEM_LICHSU (MaLS, MaKH, MaPT, Loai, SoDiem, Ngay, GhiChu) VALUES
(1, 1, NULL, N'CONG', 8,  '2026-04-01 10:00:00', N'Mua game - HĐ1'),
(2, 1, NULL, N'TRU',  10, '2026-04-01 10:00:00', N'Dùng điểm mua game — HĐ1'),
(3, 2, NULL, N'CONG', 12, '2026-04-05 14:30:00', N'Mua game - HĐ2');
SET IDENTITY_INSERT DIEM_LICHSU OFF;
 
-- Bật lại FK
EXEC sp_MSforeachtable 'ALTER TABLE ? CHECK CONSTRAINT ALL';
GO
 
PRINT N'Tạo database và nạp dữ liệu mẫu thành công!';
GO