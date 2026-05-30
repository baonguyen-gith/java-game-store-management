-- ============================================================
-- DATA SAMPLE - qlgamee (SQL Server)
-- Ngay cap nhat: 2026-05-30
--
-- MUC DICH:
--   Chay file nay SAU KHI da chay schema.sql + trigger.sql
--   De tao du lieu demo day du:
--     - Du lieu nen: role, nhan vien, users, khach hang,
--                    game, san pham, CD, ROM
--     - Hoa don + phieu thue: nam 2025 (xen ke 12 thang)
--     - Hoa don + phieu thue: thang 5/2026 (moi ngay)
--     - Hoa don + phieu thue: thang 1-4/2026 (tu data goc)
--
-- QUY TAC DIEM:
--   TICH : FLOOR(TongTien / 100.000) = 1 diem
--   TIEU : 1 diem giam 5.000 VND
--
-- TRANG THAI CD CUOI FILE:
--   CD1  (FIFA 24)      -> DaBan   (HD4)
--   CD2  (FIFA 24)      -> DaBan   (HD6)
--   CD3  (Elden Ring)   -> DaBan   (HD3)
--   CD4  (Elden Ring)   -> DangThue (PT1 - con han)
--   CD5  (COD MW3)      -> DangThue (PT2 - qua han)
--   CD6  (COD MW3)      -> DangThue (PT5 - con han)
--   CD7  (Witcher 3)    -> DangThue (PT6 - con han)
--   CD8  (Witcher 3)    -> SanSang  (PT23 da tra)
--   CD9  (Witcher 3)    -> DangThue (PT24 - dang thue T5/2026)
--   CD10 (GTA V)        -> SanSang
--   CD11 (GTA V)        -> Hong
-- ============================================================

USE qlgamee;
GO

-- Tat constraint de insert du lieu mau (tranh loi FK)
ALTER TABLE USERS         NOCHECK CONSTRAINT ALL;
ALTER TABLE KHACHHANG     NOCHECK CONSTRAINT ALL;
ALTER TABLE SANPHAM       NOCHECK CONSTRAINT ALL;
ALTER TABLE CD            NOCHECK CONSTRAINT ALL;
ALTER TABLE ROM           NOCHECK CONSTRAINT ALL;
ALTER TABLE HOADON        NOCHECK CONSTRAINT ALL;
ALTER TABLE CTHOADON      NOCHECK CONSTRAINT ALL;
ALTER TABLE PHIEUTHUE     NOCHECK CONSTRAINT ALL;
ALTER TABLE CTPHIEUTHUE   NOCHECK CONSTRAINT ALL;
ALTER TABLE DIEM_LICHSU   NOCHECK CONSTRAINT ALL;
ALTER TABLE GAME_CHITIET  NOCHECK CONSTRAINT ALL;
GO

-- ============================================================
-- 1. ROLE
-- ============================================================
SET IDENTITY_INSERT ROLE ON;
INSERT INTO ROLE (MaRole, TenRole) VALUES
(1, N'Admin'),
(2, N'Quan ly'),
(3, N'Nhan vien');
SET IDENTITY_INSERT ROLE OFF;

-- ============================================================
-- 2. NHAN VIEN
-- ============================================================
SET IDENTITY_INSERT NHANVIEN ON;
INSERT INTO NHANVIEN (MaNV, HoTen, SDT, NgaySinh, CCCD, NgayVaoLam) VALUES
(1, N'Nguyen Van An',  '0901111111', '1985-03-10', '079201000001', '2020-01-01'),
(2, N'Tran Thi Bich',  '0902222222', '1990-07-22', '079201000002', '2020-06-01'),
(3, N'Le Van Cuong',   '0903333333', '1995-11-05', '079201000003', '2021-02-01'),
(4, N'Pham Thi Dung',  '0904444444', '1997-04-18', '079201000004', '2022-05-01'),
(5, N'Hoang Van Em',   '0905555555', '1993-09-30', '079201000005', '2021-10-01');
SET IDENTITY_INSERT NHANVIEN OFF;

-- ============================================================
-- 3. USERS
-- ============================================================
SET IDENTITY_INSERT USERS ON;
INSERT INTO USERS (MaUser, Username, Password, MaRole, MaNV) VALUES
(1, 'admin',     'Password@123', 1, 1),
(2, 'quanly',    'Password@123', 2, 2),
(3, 'nhanvien1', 'Password@123', 3, 3),
(4, 'nhanvien2', 'Password@123', 3, 4),
(5, 'nhanvien3', 'Password@123', 3, 5);
SET IDENTITY_INSERT USERS OFF;

-- ============================================================
-- 4. KHACH HANG
-- DiemTichLuy se duoc UPDATE lai chinh xac o cuoi file
-- ============================================================
SET IDENTITY_INSERT KHACHHANG ON;
INSERT INTO KHACHHANG (MaKH, HoTen, SDT, CCCD, Email, DiaChi, DiemTichLuy) VALUES
(1, N'Nguyen Thi Lan', '0911000001', '012300000001', 'lan@email.com',  N'Ha Noi',      0),
(2, N'Tran Van Minh',  '0911000002', '012300000002', 'minh@email.com', N'TP. HCM',     0),
(3, N'Le Thi Hoa',     '0911000003', '012300000003', 'hoa@email.com',  N'Da Nang',     0),
(4, N'Pham Van Duc',   '0911000004', '012300000004', 'duc@email.com',  N'Hai Phong',   0),
(5, N'Hoang Thi Mai',  '0911000005', '012300000005', 'mai@email.com',  N'Can Tho',     0),
(6, N'Vu Quoc Hung',   '0911000006', '012300000006', 'hung@email.com', N'Binh Duong',  0);
SET IDENTITY_INSERT KHACHHANG OFF;

-- ============================================================
-- 5. GAME
-- ============================================================
SET IDENTITY_INSERT GAME ON;
INSERT INTO GAME (MaGame, TenGame, TheLoai, NenTang, GhiChu, HinhAnh) VALUES
(1, N'FIFA 24',          N'The thao',  N'PS5/Xbox', N'Game bong da the thao dinh cao',  '/icons/fifa24.jpg'),
(2, N'Elden Ring',       N'RPG',       N'PC/PS5',   N'RPG hanh dong the gioi mo rong',  '/icons/eldenring.jpg'),
(3, N'Call of Duty MW3', N'Ban sung',  N'PC/Xbox',  N'FPS ban sung goc nhin thu nhat',  '/icons/callofdutymw3.jpg'),
(4, N'The Witcher 3',    N'RPG',       N'PC/PS4',   N'RPG kich ban phong phu sau sac',  '/icons/thewitcher3.jpg'),
(5, N'Minecraft',        N'Sandbox',   N'PC/Xbox',  N'Sandbox xay dung va kham pha',    '/icons/minecraft.jpg'),
(6, N'GTA V',            N'Hanh dong', N'PC/PS4',   N'The gioi mo hanh dong Rockstar',  '/icons/gtav.png');
SET IDENTITY_INSERT GAME OFF;

-- ============================================================
-- 5.1 GAME_CHITIET
-- ============================================================
INSERT INTO GAME_CHITIET
    (MaGame, MoTa, Rating, Genre, DeliveryMethod, ReleaseDate, Region, Features, Language, Currency)
VALUES
(1, N'Tua game bong da hang nam dinh cao cua EA Sports',      N'E', N'Sports',  N'CD/ROM', '2023-09-29', N'Worldwide', N'Co-op, Online', N'Tieng Anh', N'USD'),
(2, N'RPG hanh dong tu FromSoftware voi the gioi rong lon',   N'M', N'RPG',     N'CD/ROM', '2022-02-25', N'Worldwide', N'Single Player', N'Tieng Anh', N'USD'),
(3, N'FPS noi tieng toan cau voi multiplayer soi dong',       N'M', N'FPS',     N'CD/ROM', '2023-11-10', N'Worldwide', N'Multiplayer',   N'Tieng Anh', N'USD'),
(4, N'RPG the gioi mo kich ban phong phu cua CD Projekt',     N'M', N'RPG',     N'CD/ROM', '2015-05-19', N'Worldwide', N'Single Player', N'Tieng Anh', N'USD'),
(5, N'Sandbox xay dung va kham pha noi tieng the gioi',       N'E', N'Sandbox', N'ROM',    '2011-11-18', N'Worldwide', N'Co-op',         N'Tieng Anh', N'USD'),
(6, N'The gioi mo hanh dong noi tieng cua Rockstar Games',    N'M', N'Action',  N'CD/ROM', '2013-09-17', N'Worldwide', N'Multiplayer',   N'Tieng Anh', N'USD');

-- ============================================================
-- 6. SAN PHAM
--   SP1  = CD  FIFA 24         GiaBan=1.290.000  GiaThue=50.000/ngay
--   SP2  = ROM FIFA 24         GiaBan=  990.000
--   SP3  = CD  Elden Ring      GiaBan=1.490.000  GiaThue=60.000/ngay
--   SP4  = ROM Elden Ring      GiaBan=1.190.000
--   SP5  = CD  COD MW3         GiaBan=1.390.000  GiaThue=55.000/ngay
--   SP6  = ROM COD MW3         GiaBan=1.090.000
--   SP7  = CD  Witcher 3       GiaBan=  590.000  GiaThue=30.000/ngay
--   SP8  = ROM Witcher 3       GiaBan=  390.000
--   SP9  = ROM Minecraft       GiaBan=  490.000
--   SP10 = CD  GTA V           GiaBan=  890.000  GiaThue=40.000/ngay
--   SP11 = ROM GTA V           GiaBan=  690.000
-- ============================================================
SET IDENTITY_INSERT SANPHAM ON;
INSERT INTO SANPHAM (MaSP, MaGame, GiaBan, GiaThueNgay) VALUES
(1,  1, 1290000,  50000),
(2,  1,  990000,      0),
(3,  2, 1490000,  60000),
(4,  2, 1190000,      0),
(5,  3, 1390000,  55000),
(6,  3, 1090000,      0),
(7,  4,  590000,  30000),
(8,  4,  390000,      0),
(9,  5,  490000,      0),
(10, 6,  890000,  40000),
(11, 6,  690000,      0);
SET IDENTITY_INSERT SANPHAM OFF;

-- ============================================================
-- 7. CD (11 dia)
-- ============================================================
SET IDENTITY_INSERT CD ON;
INSERT INTO CD (MaCD, MaSP, TinhTrang, TrangThai) VALUES
(1,   1, N'Moi',     N'SanSang'),
(2,   1, N'LikeNew', N'SanSang'),
(3,   3, N'Moi',     N'SanSang'),
(4,   3, N'99%',     N'SanSang'),
(5,   5, N'Moi',     N'SanSang'),
(6,   5, N'Cu',      N'SanSang'),
(7,   7, N'Moi',     N'SanSang'),
(8,   7, N'LikeNew', N'SanSang'),
(9,   7, N'TrayNhe', N'SanSang'),
(10, 10, N'Moi',     N'SanSang'),
(11, 10, N'Cu',      N'SanSang');
SET IDENTITY_INSERT CD OFF;

-- ============================================================
-- 8. ROM (6 rom)
-- ============================================================
INSERT INTO ROM (MaSP, DungLuong, LinkLuuTru, SoLuotBan) VALUES
(2,  N'50GB',  'https://storage.example.com/fifa24.zip',    0),
(4,  N'45GB',  'https://storage.example.com/eldenring.zip', 0),
(6,  N'100GB', 'https://storage.example.com/codmw3.zip',    0),
(8,  N'30GB',  'https://storage.example.com/witcher3.zip',  0),
(9,  N'1GB',   'https://storage.example.com/minecraft.zip', 0),
(11, N'95GB',  'https://storage.example.com/gtav.zip',      0);

-- ============================================================
-- 9. HOA DON + CHI TIET
--
-- ── DATA GOC (MaHD 1..7) ─────────────────────────────────────
--   HD1: KH1 mua ROM FIFA 24         -> 990k    ->  9 diem
--   HD2: KH3 mua ROM COD MW3         -> 990k    ->  9 diem  (tieu 20 diem)
--   HD3: KH2 mua CD Elden Ring (CD3) -> 1490k   -> 14 diem
--   HD4: KH1 mua CD FIFA(CD1)+ROM    -> 2230k   -> 22 diem  (tieu 10 diem)
--   HD5: KH2 mua ROM Minecraft       -> 490k    ->  4 diem
--   HD6: KH6 mua CD FIFA (CD2)       -> 1290k   -> 12 diem
--   HD7: Khach vang lai, ROM GTA V   -> 690k    ->  0 diem
--
-- ── NAM 2025 (MaHD 8..31): 2 HD/thang, xen ke KH va SP ──────
-- ── THANG 5/2026 (MaHD 32..62): 1 HD/ngay (01/05-31/05) ─────
-- ============================================================

SET IDENTITY_INSERT HOADON ON;

-- ── DATA GOC 2026 (T1-T4) ─────────────────────────────────────
INSERT INTO HOADON (MaHD,MaKH,MaNV,NgayLap,TongTien,DiemSuDung,TienGiam,TrangThai) VALUES
(1, 1,    3, '2026-01-10 10:00:00',  990000,  0,      0, N'DaThanhToan'),
(2, 3,    3, '2026-01-15 14:00:00',  990000, 20, 100000, N'DaThanhToan'),
(3, 2,    4, '2026-02-01 09:00:00', 1490000,  0,      0, N'DaThanhToan'),
(4, 1,    3, '2026-02-10 11:00:00', 2230000, 10,  50000, N'DaThanhToan'),
(5, 2,    4, '2026-03-05 15:00:00',  490000,  0,      0, N'DaThanhToan'),
(6, 6,    5, '2026-03-20 16:00:00', 1290000,  0,      0, N'DaThanhToan'),
(7, NULL, 3, '2026-04-12 13:00:00',  690000,  0,      0, N'DaThanhToan');

-- ── NAM 2025: T1 ──────────────────────────────────────────────
INSERT INTO HOADON (MaHD,MaKH,MaNV,NgayLap,TongTien,DiemSuDung,TienGiam,TrangThai) VALUES
(8,  2, 3, '2025-01-05 10:00:00', 0, 0, 0, N'DaThanhToan'),
(9,  4, 4, '2025-01-18 14:30:00', 0, 0, 0, N'DaThanhToan');

-- T2
INSERT INTO HOADON (MaHD,MaKH,MaNV,NgayLap,TongTien,DiemSuDung,TienGiam,TrangThai) VALUES
(10, 5, 5, '2025-02-08 09:00:00', 0, 0, 0, N'DaThanhToan'),
(11, 1, 3, '2025-02-20 16:00:00', 0, 0, 0, N'DaThanhToan');

-- T3
INSERT INTO HOADON (MaHD,MaKH,MaNV,NgayLap,TongTien,DiemSuDung,TienGiam,TrangThai) VALUES
(12, 3, 4, '2025-03-03 11:00:00', 0, 0, 0, N'DaThanhToan'),
(13, 6, 5, '2025-03-22 13:00:00', 0, 0, 0, N'DaThanhToan');

-- T4
INSERT INTO HOADON (MaHD,MaKH,MaNV,NgayLap,TongTien,DiemSuDung,TienGiam,TrangThai) VALUES
(14, 2, 3, '2025-04-10 10:00:00', 0, 0, 0, N'DaThanhToan'),
(15, 5, 4, '2025-04-25 15:00:00', 0, 0, 0, N'DaThanhToan');

-- T5
INSERT INTO HOADON (MaHD,MaKH,MaNV,NgayLap,TongTien,DiemSuDung,TienGiam,TrangThai) VALUES
(16, 1, 5, '2025-05-07 08:30:00', 0, 0, 0, N'DaThanhToan'),
(17, 4, 3, '2025-05-19 17:00:00', 0, 0, 0, N'DaThanhToan');

-- T6
INSERT INTO HOADON (MaHD,MaKH,MaNV,NgayLap,TongTien,DiemSuDung,TienGiam,TrangThai) VALUES
(18, 3, 4, '2025-06-02 10:00:00', 0, 0, 0, N'DaThanhToan'),
(19, 6, 5, '2025-06-28 14:00:00', 0, 0, 0, N'DaThanhToan');

-- T7
INSERT INTO HOADON (MaHD,MaKH,MaNV,NgayLap,TongTien,DiemSuDung,TienGiam,TrangThai) VALUES
(20, 2, 3, '2025-07-04 09:00:00', 0, 0, 0, N'DaThanhToan'),
(21, 5, 4, '2025-07-20 11:00:00', 0, 0, 0, N'DaThanhToan');

-- T8
INSERT INTO HOADON (MaHD,MaKH,MaNV,NgayLap,TongTien,DiemSuDung,TienGiam,TrangThai) VALUES
(22, 4, 5, '2025-08-11 13:30:00', 0, 0, 0, N'DaThanhToan'),
(23, 1, 3, '2025-08-27 16:00:00', 0, 0, 0, N'DaThanhToan');

-- T9
INSERT INTO HOADON (MaHD,MaKH,MaNV,NgayLap,TongTien,DiemSuDung,TienGiam,TrangThai) VALUES
(24, 6, 4, '2025-09-05 10:00:00', 0, 0, 0, N'DaThanhToan'),
(25, 3, 5, '2025-09-19 12:00:00', 0, 0, 0, N'DaThanhToan');

-- T10
INSERT INTO HOADON (MaHD,MaKH,MaNV,NgayLap,TongTien,DiemSuDung,TienGiam,TrangThai) VALUES
(26, 2, 3, '2025-10-08 09:00:00', 0, 0, 0, N'DaThanhToan'),
(27, 5, 4, '2025-10-23 14:00:00', 0, 0, 0, N'DaThanhToan');

-- T11
INSERT INTO HOADON (MaHD,MaKH,MaNV,NgayLap,TongTien,DiemSuDung,TienGiam,TrangThai) VALUES
(28, 1, 5, '2025-11-03 10:30:00', 0, 0, 0, N'DaThanhToan'),
(29, 4, 3, '2025-11-17 15:00:00', 0, 0, 0, N'DaThanhToan');

-- T12
INSERT INTO HOADON (MaHD,MaKH,MaNV,NgayLap,TongTien,DiemSuDung,TienGiam,TrangThai) VALUES
(30, 6, 4, '2025-12-10 11:00:00', 0, 0, 0, N'DaThanhToan'),
(31, 3, 5, '2025-12-26 13:00:00', 0, 0, 0, N'DaThanhToan');

-- ── THANG 5/2026: 1 HD/ngay ───────────────────────────────────
INSERT INTO HOADON (MaHD,MaKH,MaNV,NgayLap,TongTien,DiemSuDung,TienGiam,TrangThai) VALUES
(32, 1, 3, '2026-05-01 09:15:00', 0, 0, 0, N'DaThanhToan'),
(33, 2, 4, '2026-05-02 10:30:00', 0, 0, 0, N'DaThanhToan'),
(34, 3, 5, '2026-05-03 11:00:00', 0, 0, 0, N'DaThanhToan'),
(35, 4, 3, '2026-05-04 14:00:00', 0, 0, 0, N'DaThanhToan'),
(36, 5, 4, '2026-05-05 08:45:00', 0, 0, 0, N'DaThanhToan'),
(37, 6, 5, '2026-05-06 16:00:00', 0, 0, 0, N'DaThanhToan'),
(38, 1, 3, '2026-05-07 09:00:00', 0, 0, 0, N'DaThanhToan'),
(39, 2, 4, '2026-05-08 13:30:00', 0, 0, 0, N'DaThanhToan'),
(40, 3, 5, '2026-05-09 10:00:00', 0, 0, 0, N'DaThanhToan'),
(41, 4, 3, '2026-05-10 11:30:00', 0, 0, 0, N'DaThanhToan'),
(42, 5, 4, '2026-05-11 15:00:00', 0, 0, 0, N'DaThanhToan'),
(43, 6, 5, '2026-05-12 09:30:00', 0, 0, 0, N'DaThanhToan'),
(44, 1, 3, '2026-05-13 14:00:00', 0, 0, 0, N'DaThanhToan'),
(45, 2, 4, '2026-05-14 10:15:00', 0, 0, 0, N'DaThanhToan'),
(46, 3, 5, '2026-05-15 16:30:00', 0, 0, 0, N'DaThanhToan'),
(47, 4, 3, '2026-05-16 08:00:00', 0, 0, 0, N'DaThanhToan'),
(48, 5, 4, '2026-05-17 12:00:00', 0, 0, 0, N'DaThanhToan'),
(49, 6, 5, '2026-05-18 17:00:00', 0, 0, 0, N'DaThanhToan'),
(50, 1, 3, '2026-05-19 09:45:00', 0, 0, 0, N'DaThanhToan'),
(51, 2, 4, '2026-05-20 11:00:00', 0, 0, 0, N'DaThanhToan'),
(52, 3, 5, '2026-05-21 14:30:00', 0, 0, 0, N'DaThanhToan'),
(53, 4, 3, '2026-05-22 10:00:00', 0, 0, 0, N'DaThanhToan'),
(54, 5, 4, '2026-05-23 13:00:00', 0, 0, 0, N'DaThanhToan'),
(55, 6, 5, '2026-05-24 09:00:00', 0, 0, 0, N'DaThanhToan'),
(56, 1, 3, '2026-05-25 15:30:00', 0, 0, 0, N'DaThanhToan'),
(57, 2, 4, '2026-05-26 10:30:00', 0, 0, 0, N'DaThanhToan'),
(58, 3, 5, '2026-05-27 11:45:00', 0, 0, 0, N'DaThanhToan'),
(59, 4, 3, '2026-05-28 08:30:00', 0, 0, 0, N'DaThanhToan'),
(60, 5, 4, '2026-05-29 14:00:00', 0, 0, 0, N'DaThanhToan'),
(61, 6, 5, '2026-05-30 10:00:00', 0, 0, 0, N'DaThanhToan'),
(62, 1, 3, '2026-05-31 16:00:00', 0, 0, 0, N'DaThanhToan');

SET IDENTITY_INSERT HOADON OFF;

-- ============================================================
-- CHI TIET HOA DON
-- ============================================================

-- ── DATA GOC 2026 ─────────────────────────────────────────────
INSERT INTO CTHOADON (MaHD, MaSP, SoLuong, DonGia) VALUES
(1,  2, 1,  990000),   -- HD1: ROM FIFA 24
(2,  6, 1, 1090000),   -- HD2: ROM COD MW3 (gia goc 1090k, giam 100k -> 990k)
(3,  3, 1, 1490000),   -- HD3: CD Elden Ring (CD3 -> DaBan)
(4,  1, 1, 1290000),   -- HD4: CD FIFA 24   (CD1 -> DaBan)
(4,  2, 1,  990000),   -- HD4: ROM FIFA 24
(5,  9, 1,  490000),   -- HD5: ROM Minecraft
(6,  1, 1, 1290000),   -- HD6: CD FIFA 24   (CD2 -> DaBan)
(7, 11, 1,  690000);   -- HD7: ROM GTA V

-- ── NAM 2025 ──────────────────────────────────────────────────
INSERT INTO CTHOADON (MaHD, MaSP, SoLuong, DonGia) VALUES
-- T1
(8,   2, 1,  990000),   -- ROM FIFA 24
(9,   4, 1, 1190000),   -- ROM Elden Ring
-- T2
(10,  6, 1, 1090000),   -- ROM COD MW3
(11,  9, 1,  490000),   -- ROM Minecraft
-- T3
(12, 11, 1,  690000),   -- ROM GTA V
(13,  8, 1,  390000),   -- ROM Witcher 3
-- T4
(14, 11, 1,  690000),   -- ROM GTA V
(15,  6, 1, 1090000),   -- ROM COD MW3
-- T5
(16,  4, 1, 1190000),   -- ROM Elden Ring
(17,  2, 1,  990000),   -- ROM FIFA 24
-- T6
(18,  9, 1,  490000),   -- ROM Minecraft
(19, 11, 1,  690000),   -- ROM GTA V
-- T7
(20,  6, 1, 1090000),   -- ROM COD MW3
(21,  8, 1,  390000),   -- ROM Witcher 3
-- T8 (HD22 mua 2 SP -> TongTien 2180k)
(22,  4, 1, 1190000),   -- ROM Elden Ring
(22,  2, 1,  990000),   -- ROM FIFA 24
(23, 11, 1,  690000),   -- ROM GTA V
-- T9
(24,  6, 1, 1090000),   -- ROM COD MW3
(25,  2, 1,  990000),   -- ROM FIFA 24
-- T10 (HD26 mua 2 SP -> TongTien 1180k)
(26,  9, 1,  490000),   -- ROM Minecraft
(26, 11, 1,  690000),   -- ROM GTA V
(27,  4, 1, 1190000),   -- ROM Elden Ring
-- T11 (HD29 mua 2 SP -> TongTien 1380k)
(28,  6, 1, 1090000),   -- ROM COD MW3
(29,  8, 1,  390000),   -- ROM Witcher 3
(29,  2, 1,  990000),   -- ROM FIFA 24
-- T12 (HD30 mua 2 SP -> TongTien 1180k)
(30, 11, 1,  690000),   -- ROM GTA V
(30,  9, 1,  490000),   -- ROM Minecraft
(31,  4, 1, 1190000);   -- ROM Elden Ring

-- ── THANG 5/2026: moi ngay 1 HD, xen ke SP ───────────────────
INSERT INTO CTHOADON (MaHD, MaSP, SoLuong, DonGia) VALUES
(32,  2, 1,  990000),   -- D1:  ROM FIFA 24
(33,  4, 1, 1190000),   -- D2:  ROM Elden Ring
(34,  6, 1, 1090000),   -- D3:  ROM COD MW3
(35,  8, 1,  390000),   -- D4:  ROM Witcher 3
(36,  9, 1,  490000),   -- D5:  ROM Minecraft
(37, 11, 1,  690000),   -- D6:  ROM GTA V
(38,  2, 1,  990000),   -- D7:  ROM FIFA 24
(39,  6, 1, 1090000),   -- D8:  ROM COD MW3
(40,  4, 1, 1190000),   -- D9:  ROM Elden Ring
(41, 11, 1,  690000),   -- D10: ROM GTA V
(42,  8, 1,  390000),   -- D11: ROM Witcher 3
(43,  9, 1,  490000),   -- D12: ROM Minecraft
(44,  4, 1, 1190000),   -- D13: ROM Elden Ring
(44,  2, 1,  990000),   -- D13: + ROM FIFA 24 -> 2180k
(45,  6, 1, 1090000),   -- D14: ROM COD MW3
(46, 11, 1,  690000),   -- D15: ROM GTA V
(47,  9, 1,  490000),   -- D16: ROM Minecraft
(48,  8, 1,  390000),   -- D17: ROM Witcher 3
(49,  4, 1, 1190000),   -- D18: ROM Elden Ring
(50,  2, 1,  990000),   -- D19: ROM FIFA 24
(51,  6, 1, 1090000),   -- D20: ROM COD MW3
(52, 11, 1,  690000),   -- D21: ROM GTA V
(52,  9, 1,  490000),   -- D21: + ROM Minecraft -> 1180k
(53,  4, 1, 1190000),   -- D22: ROM Elden Ring
(54,  2, 1,  990000),   -- D23: ROM FIFA 24
(55,  6, 1, 1090000),   -- D24: ROM COD MW3
(56,  8, 1,  390000),   -- D25: ROM Witcher 3
(57,  9, 1,  490000),   -- D26: ROM Minecraft
(58, 11, 1,  690000),   -- D27: ROM GTA V
(59,  4, 1, 1190000),   -- D28: ROM Elden Ring
(60,  2, 1,  990000),   -- D29: ROM FIFA 24
(61,  6, 1, 1090000),   -- D30: ROM COD MW3
(62, 11, 1,  690000);   -- D31: ROM GTA V

-- Cap nhat CD da ban qua hoa don goc
UPDATE CD SET TrangThai = N'DaBan' WHERE MaCD IN (1, 2, 3);

-- Kich trigger TRG_CAP_NHAT_TONGTIEN_HOADON (da chay qua CTHOADON INSERT)
-- Kich them trigger TRG_TANG_SOLOIOTBAN_ROM
UPDATE HOADON SET TrangThai = N'DaThanhToan'
WHERE MaHD BETWEEN 1 AND 62;

-- ============================================================
-- 10. PHIEU THUE + CHI TIET
--
-- ── DATA GOC 2026 (MaPT 1..6) ────────────────────────────────
--   PT1: KH1 CD4 Elden Ring   20/05-30/05  DangThue  con han
--   PT2: KH5 CD5 COD MW3      10/05-15/05  DangThue  QUA HAN 13 ngay
--   PT3: KH2 CD8 Witcher3     29/03-05/04  DaTra     dung han
--   PT4: KH6 CD9 Witcher3     24/04-01/05  DaTra     tre 4 ngay
--   PT5: KH4 CD6 COD MW3      22/05-29/05  DangThue  con han
--   PT6: KH4 CD7 Witcher3     22/05-29/05  DangThue  con han
--
-- ── NAM 2025 (MaPT 7..18): 1 phieu/thang ─────────────────────
--   Luan phien CD8/CD9, co mix dung han / tre han
--
-- ── THANG 5/2026 (MaPT 19..24) ───────────────────────────────
--   PT19-23: DaTra  |  PT24: DangThue (con han)
-- ============================================================

SET IDENTITY_INSERT PHIEUTHUE ON;

-- ── DATA GOC 2026 ─────────────────────────────────────────────
INSERT INTO PHIEUTHUE (MaPT,MaKH,NgayThue,NgayTraDuKien,NgayTraThucTe,TienCoc,TienPhat,TrangThai) VALUES
(1, 1, '2026-05-20 09:00:00','2026-05-30 09:00:00', NULL,                  1200000,      0, N'DangThue'),
(2, 5, '2026-05-10 10:00:00','2026-05-15 10:00:00', NULL,                   550000,      0, N'DangThue'),
(3, 2, '2026-03-29 08:00:00','2026-04-05 08:00:00','2026-04-05 07:30:00',  420000,      0, N'DaTra'),
(4, 6, '2026-04-24 09:00:00','2026-05-01 09:00:00','2026-05-05 14:00:00',  420000, 180000, N'DaTra'),
(5, 4, '2026-05-22 10:00:00','2026-05-29 10:00:00', NULL,                   770000,      0, N'DangThue'),
(6, 4, '2026-05-22 10:00:00','2026-05-29 10:00:00', NULL,                   420000,      0, N'DangThue');

-- ── NAM 2025 ──────────────────────────────────────────────────
-- T1: KH3 CD8 Witcher3  5 ngay  DaTra dung han
INSERT INTO PHIEUTHUE (MaPT,MaKH,NgayThue,NgayTraDuKien,NgayTraThucTe,TienCoc,TienPhat,TrangThai) VALUES
(7,  3, '2025-01-08 10:00:00','2025-01-13 10:00:00','2025-01-13 09:00:00',  300000,      0, N'DaTra');

-- T2: KH5 CD9 Witcher3  7 ngay  DaTra dung han
INSERT INTO PHIEUTHUE (MaPT,MaKH,NgayThue,NgayTraDuKien,NgayTraThucTe,TienCoc,TienPhat,TrangThai) VALUES
(8,  5, '2025-02-10 09:00:00','2025-02-17 09:00:00','2025-02-17 08:30:00',  420000,      0, N'DaTra');

-- T3: KH2 CD8 Witcher3  5 ngay  DaTra TRE 2 NGAY  (phat = 2*30k*1.5 = 90k)
INSERT INTO PHIEUTHUE (MaPT,MaKH,NgayThue,NgayTraDuKien,NgayTraThucTe,TienCoc,TienPhat,TrangThai) VALUES
(9,  2, '2025-03-05 10:00:00','2025-03-10 10:00:00','2025-03-12 15:00:00',  300000,  90000, N'DaTra');

-- T4: KH6 CD9 Witcher3  7 ngay  DaTra dung han
INSERT INTO PHIEUTHUE (MaPT,MaKH,NgayThue,NgayTraDuKien,NgayTraThucTe,TienCoc,TienPhat,TrangThai) VALUES
(10, 6, '2025-04-01 09:00:00','2025-04-08 09:00:00','2025-04-08 08:00:00',  420000,      0, N'DaTra');

-- T5: KH1 CD8 Witcher3  10 ngay  DaTra dung han
INSERT INTO PHIEUTHUE (MaPT,MaKH,NgayThue,NgayTraDuKien,NgayTraThucTe,TienCoc,TienPhat,TrangThai) VALUES
(11, 1, '2025-05-03 10:00:00','2025-05-13 10:00:00','2025-05-13 10:00:00',  600000,      0, N'DaTra');

-- T6: KH4 CD9 Witcher3  5 ngay  DaTra TRE 3 NGAY  (phat = 3*30k*1.5 = 135k)
INSERT INTO PHIEUTHUE (MaPT,MaKH,NgayThue,NgayTraDuKien,NgayTraThucTe,TienCoc,TienPhat,TrangThai) VALUES
(12, 4, '2025-06-10 10:00:00','2025-06-15 10:00:00','2025-06-18 14:00:00',  300000, 135000, N'DaTra');

-- T7: KH3 CD8 Witcher3  7 ngay  DaTra dung han
INSERT INTO PHIEUTHUE (MaPT,MaKH,NgayThue,NgayTraDuKien,NgayTraThucTe,TienCoc,TienPhat,TrangThai) VALUES
(13, 3, '2025-07-14 09:00:00','2025-07-21 09:00:00','2025-07-21 08:00:00',  420000,      0, N'DaTra');

-- T8: KH5 CD9 Witcher3  5 ngay  DaTra dung han
INSERT INTO PHIEUTHUE (MaPT,MaKH,NgayThue,NgayTraDuKien,NgayTraThucTe,TienCoc,TienPhat,TrangThai) VALUES
(14, 5, '2025-08-05 10:00:00','2025-08-10 10:00:00','2025-08-10 09:30:00',  300000,      0, N'DaTra');

-- T9: KH2 CD8 Witcher3  7 ngay  DaTra TRE 1 NGAY  (phat = 1*30k*1.5 = 45k)
INSERT INTO PHIEUTHUE (MaPT,MaKH,NgayThue,NgayTraDuKien,NgayTraThucTe,TienCoc,TienPhat,TrangThai) VALUES
(15, 2, '2025-09-08 09:00:00','2025-09-15 09:00:00','2025-09-16 11:00:00',  420000,  45000, N'DaTra');

-- T10: KH6 CD9 Witcher3  10 ngay  DaTra dung han
INSERT INTO PHIEUTHUE (MaPT,MaKH,NgayThue,NgayTraDuKien,NgayTraThucTe,TienCoc,TienPhat,TrangThai) VALUES
(16, 6, '2025-10-05 10:00:00','2025-10-15 10:00:00','2025-10-15 09:00:00',  600000,      0, N'DaTra');

-- T11: KH1 CD8 Witcher3  5 ngay  DaTra dung han
INSERT INTO PHIEUTHUE (MaPT,MaKH,NgayThue,NgayTraDuKien,NgayTraThucTe,TienCoc,TienPhat,TrangThai) VALUES
(17, 1, '2025-11-10 10:00:00','2025-11-15 10:00:00','2025-11-15 09:30:00',  300000,      0, N'DaTra');

-- T12: KH4 CD9 Witcher3  7 ngay  DaTra TRE 2 NGAY  (phat = 2*30k*1.5 = 90k)
INSERT INTO PHIEUTHUE (MaPT,MaKH,NgayThue,NgayTraDuKien,NgayTraThucTe,TienCoc,TienPhat,TrangThai) VALUES
(18, 4, '2025-12-15 09:00:00','2025-12-22 09:00:00','2025-12-24 14:00:00',  420000,  90000, N'DaTra');

-- ── THANG 5/2026 ──────────────────────────────────────────────
-- PT19: KH3 CD8  03/05-10/05  DaTra dung han
INSERT INTO PHIEUTHUE (MaPT,MaKH,NgayThue,NgayTraDuKien,NgayTraThucTe,TienCoc,TienPhat,TrangThai) VALUES
(19, 3, '2026-05-03 09:00:00','2026-05-10 09:00:00','2026-05-10 08:30:00',  420000,      0, N'DaTra');

-- PT20: KH2 CD9  08/05-15/05  DaTra TRE 2 NGAY  (phat = 2*30k*1.5 = 90k)
INSERT INTO PHIEUTHUE (MaPT,MaKH,NgayThue,NgayTraDuKien,NgayTraThucTe,TienCoc,TienPhat,TrangThai) VALUES
(20, 2, '2026-05-08 10:00:00','2026-05-15 10:00:00','2026-05-17 14:00:00',  420000,  90000, N'DaTra');

-- PT21: KH1 CD8  13/05-20/05  DaTra dung han
INSERT INTO PHIEUTHUE (MaPT,MaKH,NgayThue,NgayTraDuKien,NgayTraThucTe,TienCoc,TienPhat,TrangThai) VALUES
(21, 1, '2026-05-13 10:00:00','2026-05-20 10:00:00','2026-05-20 09:00:00',  420000,      0, N'DaTra');

-- PT22: KH6 CD9  18/05-25/05  DaTra dung han
INSERT INTO PHIEUTHUE (MaPT,MaKH,NgayThue,NgayTraDuKien,NgayTraThucTe,TienCoc,TienPhat,TrangThai) VALUES
(22, 6, '2026-05-18 09:00:00','2026-05-25 09:00:00','2026-05-25 08:00:00',  420000,      0, N'DaTra');

-- PT23: KH3 CD8  24/05-31/05  DaTra dung han
INSERT INTO PHIEUTHUE (MaPT,MaKH,NgayThue,NgayTraDuKien,NgayTraThucTe,TienCoc,TienPhat,TrangThai) VALUES
(23, 3, '2026-05-24 10:00:00','2026-05-31 10:00:00','2026-05-31 09:30:00',  420000,      0, N'DaTra');

-- PT24: KH2 CD9  27/05-03/06  DangThue (con han tinh den 30/05)
INSERT INTO PHIEUTHUE (MaPT,MaKH,NgayThue,NgayTraDuKien,NgayTraThucTe,TienCoc,TienPhat,TrangThai) VALUES
(24, 2, '2026-05-27 10:00:00','2026-06-03 10:00:00', NULL,                   420000,      0, N'DangThue');

SET IDENTITY_INSERT PHIEUTHUE OFF;

-- ============================================================
-- CHI TIET PHIEU THUE
-- ============================================================

-- ── DATA GOC 2026 ─────────────────────────────────────────────
INSERT INTO CTPHIEUTHUE (MaPT, MaCD, MaNV, DonGiaThue) VALUES
(1, 4, 3, 600000),   -- PT1:  CD4 Elden Ring  60k*10 ngay
(2, 5, 4, 275000),   -- PT2:  CD5 COD MW3     55k*5 ngay
(3, 8, 3, 210000),   -- PT3:  CD8 Witcher3    30k*7 ngay
(4, 9, 4, 210000),   -- PT4:  CD9 Witcher3    30k*7 ngay
(5, 6, 3, 385000),   -- PT5:  CD6 COD MW3     55k*7 ngay
(6, 7, 3, 210000);   -- PT6:  CD7 Witcher3    30k*7 ngay

-- ── NAM 2025 ──────────────────────────────────────────────────
INSERT INTO CTPHIEUTHUE (MaPT, MaCD, MaNV, DonGiaThue) VALUES
(7,  8, 3, 150000),   -- PT7:  CD8  5 ngay*30k
(8,  9, 4, 210000),   -- PT8:  CD9  7 ngay*30k
(9,  8, 3, 150000),   -- PT9:  CD8  5 ngay*30k
(10, 9, 4, 210000),   -- PT10: CD9  7 ngay*30k
(11, 8, 3, 300000),   -- PT11: CD8 10 ngay*30k
(12, 9, 4, 150000),   -- PT12: CD9  5 ngay*30k
(13, 8, 3, 210000),   -- PT13: CD8  7 ngay*30k
(14, 9, 4, 150000),   -- PT14: CD9  5 ngay*30k
(15, 8, 3, 210000),   -- PT15: CD8  7 ngay*30k
(16, 9, 4, 300000),   -- PT16: CD9 10 ngay*30k
(17, 8, 3, 150000),   -- PT17: CD8  5 ngay*30k
(18, 9, 4, 210000);   -- PT18: CD9  7 ngay*30k

-- ── THANG 5/2026 ──────────────────────────────────────────────
INSERT INTO CTPHIEUTHUE (MaPT, MaCD, MaNV, DonGiaThue) VALUES
(19, 8, 3, 210000),   -- PT19: CD8  7 ngay*30k
(20, 9, 4, 210000),   -- PT20: CD9  7 ngay*30k
(21, 8, 3, 210000),   -- PT21: CD8  7 ngay*30k
(22, 9, 4, 210000),   -- PT22: CD9  7 ngay*30k
(23, 8, 3, 210000),   -- PT23: CD8  7 ngay*30k
(24, 9, 4, 210000);   -- PT24: CD9  7 ngay*30k (dang thue)

-- Cap nhat TrangThai CD theo trang thai hien tai
UPDATE CD SET TrangThai = N'DaBan'    WHERE MaCD IN (1, 2, 3);
UPDATE CD SET TrangThai = N'DangThue' WHERE MaCD IN (4, 5, 6, 7);  -- PT1/2/5/6
UPDATE CD SET TrangThai = N'SanSang'  WHERE MaCD IN (8, 10);        -- PT23 da tra; CD10 chua dung
UPDATE CD SET TrangThai = N'DangThue' WHERE MaCD = 9;               -- PT24 dang thue
UPDATE CD SET TrangThai = N'Hong'     WHERE MaCD = 11;

-- ============================================================
-- 11. DIEM LICH SU
--
-- Tat ca giao dich theo thu tu thoi gian, bao gom:
--   - Giao dich cu (truoc 2025)
--   - Hoa don 2026 goc (HD1-6 su dung/tich luy diem)
--   - Phieu thue 2025 da tra (PT7-18)
--   - Phieu thue T5/2026 da tra (PT19-23)
-- ============================================================
SET IDENTITY_INSERT DIEM_LICHSU ON;

INSERT INTO DIEM_LICHSU (MaLS,MaKH,MaPT,Loai,SoDiem,Ngay,GhiChu) VALUES

-- ── Giao dich cu (truoc 2025) ─────────────────────────────────
(1,  3, NULL, N'CONG', 20, '2025-06-01 09:00:00', N'Tich luy giao dich cu KH3'),
(2,  2, NULL, N'TRU',   5, '2025-10-15 11:00:00', N'Su dung diem giao dich cu KH2'),
(3,  4, NULL, N'CONG',  5, '2025-11-20 14:00:00', N'Tich luy giao dich cu KH4'),
(4,  5, NULL, N'CONG',  5, '2025-12-05 10:00:00', N'Tich luy giao dich cu KH5'),

-- ── Hoa don goc 2026 ──────────────────────────────────────────
-- HD1: KH1 mua ROM FIFA 24 (990k -> 9 diem)
(5,  1, NULL, N'CONG',  9, '2026-01-10 10:05:00', N'Tich luy mua hang HD0001 - ROM FIFA 24'),
-- HD2: KH3 tieu 20 diem, roi tich 9 diem (990k -> 9 diem)
(6,  3, NULL, N'TRU',  20, '2026-01-15 14:00:00', N'Su dung diem giam gia HD0002 - ROM COD MW3'),
(7,  3, NULL, N'CONG',  9, '2026-01-15 14:05:00', N'Tich luy mua hang HD0002 - ROM COD MW3'),
-- HD3: KH2 mua CD Elden Ring (1490k -> 14 diem)
(8,  2, NULL, N'CONG', 14, '2026-02-01 09:05:00', N'Tich luy mua hang HD0003 - CD Elden Ring'),
-- HD4: KH1 tieu 10 diem, roi tich 22 diem (2230k -> 22 diem)
(9,  1, NULL, N'TRU',  10, '2026-02-10 11:00:00', N'Su dung diem giam gia HD0004 - CD FIFA + ROM FIFA'),
(10, 1, NULL, N'CONG', 22, '2026-02-10 11:05:00', N'Tich luy mua hang HD0004 - CD FIFA + ROM FIFA'),
-- HD5: KH2 mua ROM Minecraft (490k -> 4 diem)
(11, 2, NULL, N'CONG',  4, '2026-03-05 15:05:00', N'Tich luy mua hang HD0005 - ROM Minecraft'),
-- HD6: KH6 mua CD FIFA 24 (1290k -> 12 diem)
(12, 6, NULL, N'CONG', 12, '2026-03-20 16:05:00', N'Tich luy mua hang HD0006 - CD FIFA 24'),
-- PT3: KH2 tra Witcher3 dung han (210k -> 2 diem)
(13, 2, 3,    N'CONG',  2, '2026-04-05 07:35:00', N'Tich luy thue game PT0003 - CD Witcher 3'),
-- PT4: KH6 tra Witcher3 tre 4 ngay (210k+180k=390k -> 3 diem)
(14, 6, 4,    N'CONG',  3, '2026-05-05 14:05:00', N'Tich luy thue game PT0004 - CD Witcher 3'),

-- ── Phieu thue nam 2025 (da tra) ──────────────────────────────
-- PT7:  KH3 150k -> 1 diem
(15, 3, 7,    N'CONG',  1, '2025-01-13 09:05:00', N'Tich luy thue game PT0007 - Witcher 3 (150k)'),
-- PT8:  KH5 210k -> 2 diem
(16, 5, 8,    N'CONG',  2, '2025-02-17 08:35:00', N'Tich luy thue game PT0008 - Witcher 3 (210k)'),
-- PT9:  KH2 150k+90k=240k -> 2 diem
(17, 2, 9,    N'CONG',  2, '2025-03-12 15:05:00', N'Tich luy thue game PT0009 - Witcher 3 (150k+phat 90k=240k)'),
-- PT10: KH6 210k -> 2 diem
(18, 6, 10,   N'CONG',  2, '2025-04-08 08:05:00', N'Tich luy thue game PT0010 - Witcher 3 (210k)'),
-- PT11: KH1 300k -> 3 diem
(19, 1, 11,   N'CONG',  3, '2025-05-13 10:05:00', N'Tich luy thue game PT0011 - Witcher 3 (300k)'),
-- PT12: KH4 150k+135k=285k -> 2 diem
(20, 4, 12,   N'CONG',  2, '2025-06-18 14:05:00', N'Tich luy thue game PT0012 - Witcher 3 (150k+phat 135k=285k)'),
-- PT13: KH3 210k -> 2 diem
(21, 3, 13,   N'CONG',  2, '2025-07-21 08:05:00', N'Tich luy thue game PT0013 - Witcher 3 (210k)'),
-- PT14: KH5 150k -> 1 diem
(22, 5, 14,   N'CONG',  1, '2025-08-10 09:35:00', N'Tich luy thue game PT0014 - Witcher 3 (150k)'),
-- PT15: KH2 210k+45k=255k -> 2 diem
(23, 2, 15,   N'CONG',  2, '2025-09-16 11:05:00', N'Tich luy thue game PT0015 - Witcher 3 (210k+phat 45k=255k)'),
-- PT16: KH6 300k -> 3 diem
(24, 6, 16,   N'CONG',  3, '2025-10-15 09:05:00', N'Tich luy thue game PT0016 - Witcher 3 (300k)'),
-- PT17: KH1 150k -> 1 diem
(25, 1, 17,   N'CONG',  1, '2025-11-15 09:35:00', N'Tich luy thue game PT0017 - Witcher 3 (150k)'),
-- PT18: KH4 210k+90k=300k -> 3 diem
(26, 4, 18,   N'CONG',  3, '2025-12-24 14:05:00', N'Tich luy thue game PT0018 - Witcher 3 (210k+phat 90k=300k)'),

-- ── Phieu thue thang 5/2026 (da tra) ─────────────────────────
-- PT19: KH3 210k -> 2 diem
(27, 3, 19,   N'CONG',  2, '2026-05-10 08:35:00', N'Tich luy thue game PT0019 - Witcher 3 (210k)'),
-- PT20: KH2 210k+90k=300k -> 3 diem
(28, 2, 20,   N'CONG',  3, '2026-05-17 14:05:00', N'Tich luy thue game PT0020 - Witcher 3 (210k+phat 90k=300k)'),
-- PT21: KH1 210k -> 2 diem
(29, 1, 21,   N'CONG',  2, '2026-05-20 09:05:00', N'Tich luy thue game PT0021 - Witcher 3 (210k)'),
-- PT22: KH6 210k -> 2 diem
(30, 6, 22,   N'CONG',  2, '2026-05-25 08:05:00', N'Tich luy thue game PT0022 - Witcher 3 (210k)'),
-- PT23: KH3 210k -> 2 diem
(31, 3, 23,   N'CONG',  2, '2026-05-31 09:35:00', N'Tich luy thue game PT0023 - Witcher 3 (210k)');

SET IDENTITY_INSERT DIEM_LICHSU OFF;

-- ============================================================
-- CAP NHAT DIEM TICH LUY CHINH XAC CHO TUNG KHACH HANG
-- Tinh lai tu DIEM_LICHSU de dam bao khop 100%
-- ============================================================
UPDATE KHACHHANG
SET DiemTichLuy = (
    SELECT ISNULL(SUM(
        CASE WHEN Loai = N'CONG' THEN SoDiem
             ELSE -SoDiem
        END), 0)
    FROM DIEM_LICHSU
    WHERE MaKH = KHACHHANG.MaKH
)
WHERE MaKH IN (1, 2, 3, 4, 5, 6);

-- Bat lai constraint
ALTER TABLE USERS         CHECK CONSTRAINT ALL;
ALTER TABLE KHACHHANG     CHECK CONSTRAINT ALL;
ALTER TABLE SANPHAM       CHECK CONSTRAINT ALL;
ALTER TABLE CD            CHECK CONSTRAINT ALL;
ALTER TABLE ROM           CHECK CONSTRAINT ALL;
ALTER TABLE HOADON        CHECK CONSTRAINT ALL;
ALTER TABLE CTHOADON      CHECK CONSTRAINT ALL;
ALTER TABLE PHIEUTHUE     CHECK CONSTRAINT ALL;
ALTER TABLE CTPHIEUTHUE   CHECK CONSTRAINT ALL;
ALTER TABLE DIEM_LICHSU   CHECK CONSTRAINT ALL;
ALTER TABLE GAME_CHITIET  CHECK CONSTRAINT ALL;
GO

-- ============================================================
-- KIEM TRA NHANH
-- ============================================================

-- Tong so ban ghi
SELECT 'HOADON'      AS Bang, COUNT(*) AS SoLuong FROM HOADON
UNION ALL SELECT 'CTHOADON',     COUNT(*) FROM CTHOADON
UNION ALL SELECT 'PHIEUTHUE',    COUNT(*) FROM PHIEUTHUE
UNION ALL SELECT 'CTPHIEUTHUE',  COUNT(*) FROM CTPHIEUTHUE
UNION ALL SELECT 'DIEM_LICHSU',  COUNT(*) FROM DIEM_LICHSU;

-- Doanh thu ban theo thang (2025 + 2026)
SELECT
    YEAR(NgayLap)  AS Nam,
    MONTH(NgayLap) AS Thang,
    COUNT(*)       AS SoHoaDon,
    SUM(TongTien)  AS DoanhThuBan
FROM HOADON
WHERE TrangThai = N'DaThanhToan'
GROUP BY YEAR(NgayLap), MONTH(NgayLap)
ORDER BY Nam, Thang;

-- Doanh thu thue theo thang (2025 + 2026)
SELECT
    YEAR(NgayThue)  AS Nam,
    MONTH(NgayThue) AS Thang,
    COUNT(*)        AS SoPhieu,
    SUM(ct.DonGiaThue + ISNULL(pt.TienPhat, 0)) AS DoanhThuThue
FROM PHIEUTHUE pt
JOIN CTPHIEUTHUE ct ON pt.MaPT = ct.MaPT
WHERE pt.TrangThai = N'DaTra'
GROUP BY YEAR(NgayThue), MONTH(NgayThue)
ORDER BY Nam, Thang;

-- Diem tich luy hien tai cua tung khach hang
SELECT MaKH, HoTen, DiemTichLuy
FROM KHACHHANG
ORDER BY MaKH;

GO
PRINT N'Insert du lieu mau hoan tat.';