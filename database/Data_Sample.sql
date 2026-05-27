-- ============================================================
-- DU LIEU MAU
-- Quy tac diem:
--   Tich luy : FLOOR(TongTien / 100.000) diem
--   Doi diem : 1 diem giam 5.000 VND -> TienGiam = DiemSuDung * 5.000
-- ============================================================

EXEC sp_MSforeachtable 'ALTER TABLE ? NOCHECK CONSTRAINT ALL';
GO

-- ========================
-- 1. ROLE
-- ========================
SET IDENTITY_INSERT ROLE ON;
INSERT INTO ROLE (MaRole, TenRole) VALUES
(1, N'Admin'),
(2, N'Quản lý'),
(3, N'Nhân viên');
SET IDENTITY_INSERT ROLE OFF;

-- ========================
-- 2. NHANVIEN
-- ========================
SET IDENTITY_INSERT NHANVIEN ON;
INSERT INTO NHANVIEN (MaNV, HoTen, SDT, NgaySinh, CCCD, NgayVaoLam) VALUES
(1, N'Nguyen Van Admin',     '0901111111', '1985-01-15', '079200000001', '2020-01-01'),
(2, N'Tran Thi Quan Ly',     '0902222222', '1990-03-20', '079200000002', '2020-06-01'),
(3, N'Le Van Nhan Vien A',   '0903333333', '1995-07-10', '079200000003', '2021-01-15'),
(4, N'Pham Thi Nhan Vien B', '0904444444', '1997-11-25', '079200000004', '2022-03-01'),
(5, N'Hoang Van Cuong',      '0905555555', '1993-05-05', '079200000005', '2021-09-01');
SET IDENTITY_INSERT NHANVIEN OFF;

-- ========================
-- 3. USERS
-- Password: BCrypt hash cua 'Password@123'
-- ========================
SET IDENTITY_INSERT USERS ON;
INSERT INTO USERS (MaUser, Username, Password, MaRole, MaNV) VALUES
(1, 'admin',     '$2a$10$slYQmyNdgTY18LMoDIs11OHQEBsxGBsS3bT.T5ZZNGPDiPDy6JbTu', 1, 1),
(2, 'quanly',    '$2a$10$slYQmyNdgTY18LMoDIs11OHQEBsxGBsS3bT.T5ZZNGPDiPDy6JbTu', 2, 2),
(3, 'nhanvien1', '$2a$10$slYQmyNdgTY18LMoDIs11OHQEBsxGBsS3bT.T5ZZNGPDiPDy6JbTu', 3, 3),
(4, 'nhanvien2', '$2a$10$slYQmyNdgTY18LMoDIs11OHQEBsxGBsS3bT.T5ZZNGPDiPDy6JbTu', 3, 4),
(5, 'nhanvien3', '$2a$10$slYQmyNdgTY18LMoDIs11OHQEBsxGBsS3bT.T5ZZNGPDiPDy6JbTu', 3, 5);
SET IDENTITY_INSERT USERS OFF;

-- ========================
-- 4. KHACHHANG
-- DiemTichLuy tinh tu DIEM_LICHSU ben duoi
-- KH1 : CONG=9+14=23,  TRU=10  -> 13
-- KH2 : CONG=11+4=15,  TRU=0   -> 15
-- KH3 : CONG=10,       TRU=20  -> 0  (khong am)
-- KH4 : CONG=14+5=19,  TRU=0   -> 19
-- KH5 : CONG=5,        TRU=0   -> 5
-- KH6 : CONG=13+3=16,  TRU=15  -> 1
-- ========================
SET IDENTITY_INSERT KHACHHANG ON;
INSERT INTO KHACHHANG (MaKH, HoTen, SDT, CCCD, Email, DiaChi, DiemTichLuy) VALUES
(1, N'Nguyen Thi Lan',  '0911000001', '012300000001', 'lan@email.com',  N'Ha Noi',      13),
(2, N'Tran Van Minh',   '0911000002', '012300000002', 'minh@email.com', N'TP. HCM',     15),
(3, N'Le Thi Hoa',      '0911000003', '012300000003', 'hoa@email.com',  N'Da Nang',      0),
(4, N'Pham Van Duc',    '0911000004', '012300000004', 'duc@email.com',  N'Hai Phong',   19),
(5, N'Hoang Thi Mai',   '0911000005', '012300000005', 'mai@email.com',  N'Can Tho',      5),
(6, N'Vu Quoc Hung',    '0911000006', '012300000006', 'hung@email.com', N'Binh Duong',   1);
SET IDENTITY_INSERT KHACHHANG OFF;

-- ========================
-- 5. GAME (lay tu data mau moi)
-- ========================
SET IDENTITY_INSERT GAME ON;
INSERT INTO GAME (MaGame, TenGame, TheLoai, NenTang, GhiChu, HinhAnh) VALUES
(1, N'FIFA 24',          N'The thao',  N'PS5/Xbox', N'Game bong da the thao',       '/icons/fifa24.jpg'),
(2, N'Elden Ring',       N'RPG',       N'PC/PS5',   N'Game nhap vai hanh dong',     '/icons/eldenring.jpg'),
(3, N'Call of Duty MW3', N'Ban sung',  N'PC/Xbox',  N'Game ban sung goc nhin 1',    '/icons/callofdutymw3.jpg'),
(4, N'The Witcher 3',    N'RPG',       N'PC/PS4',   N'Game nhap vai the gioi mo',   '/icons/thewitcher3.jpg'),
(5, N'Minecraft',        N'Sandbox',   N'PC/Xbox',  N'Game xay dung sandbox',       '/icons/minecraft.jpg'),
(6, N'GTA V',            N'Hanh dong', N'PC/PS4',   N'Game the gioi mo hanh dong',  '/icons/gtav.png');
SET IDENTITY_INSERT GAME OFF;

-- ========================
-- 5.1 GAME_CHITIET
-- ========================
INSERT INTO GAME_CHITIET (MaGame, MoTa, Rating, Genre, DeliveryMethod, ReleaseDate, Region, Features, Language, Currency) VALUES
(1, N'Tua game bong da hang nam dinh cao cua EA Sports',          N'E', N'Sports',  N'CD/ROM', '2023-09-29', N'Worldwide', N'Co-op, Online',  N'Tieng Anh', N'USD'),
(2, N'RPG hanh dong tu FromSoftware voi the gioi rong lon',       N'M', N'RPG',     N'CD/ROM', '2022-02-25', N'Worldwide', N'Single Player',  N'Tieng Anh', N'USD'),
(3, N'Game ban sung goc nhin thu nhat noi tieng toan cau',        N'M', N'FPS',     N'CD/ROM', '2023-11-10', N'Worldwide', N'Multiplayer',    N'Tieng Anh', N'USD'),
(4, N'RPG the gioi mo voi kich ban phong phu cua CD Projekt',     N'M', N'RPG',     N'CD/ROM', '2015-05-19', N'Worldwide', N'Single Player',  N'Tieng Anh', N'USD'),
(5, N'Game sandbox xay dung va kham pha noi tieng the gioi',      N'E', N'Sandbox', N'ROM',    '2011-11-18', N'Worldwide', N'Co-op',          N'Tieng Anh', N'USD'),
(6, N'Game the gioi mo hanh dong noi tieng cua Rockstar',         N'M', N'Action',  N'CD/ROM', '2013-09-17', N'Worldwide', N'Multiplayer',    N'Tieng Anh', N'USD');

-- ========================
-- 6. SANPHAM
-- SP1  = CD  FIFA 24        GiaBan=1.290.000  GiaThue=50.000/ngay
-- SP2  = ROM FIFA 24        GiaBan=990.000    GiaThue=0
-- SP3  = CD  Elden Ring     GiaBan=1.490.000  GiaThue=60.000/ngay
-- SP4  = ROM Elden Ring     GiaBan=1.190.000  GiaThue=0
-- SP5  = CD  COD MW3        GiaBan=1.390.000  GiaThue=55.000/ngay
-- SP6  = ROM COD MW3        GiaBan=1.090.000  GiaThue=0
-- SP7  = CD  Witcher 3      GiaBan=590.000    GiaThue=30.000/ngay
-- SP8  = ROM Minecraft      GiaBan=490.000    GiaThue=0
-- SP9  = CD  GTA V          GiaBan=890.000    GiaThue=40.000/ngay
-- SP10 = ROM GTA V          GiaBan=690.000    GiaThue=0
-- ========================
SET IDENTITY_INSERT SANPHAM ON;
INSERT INTO SANPHAM (MaSP, MaGame, GiaBan, GiaThueNgay) VALUES
(1,  1, 1290000, 50000),
(2,  1,  990000,     0),
(3,  2, 1490000, 60000),
(4,  2, 1190000,     0),
(5,  3, 1390000, 55000),
(6,  3, 1090000,     0),
(7,  4,  590000, 30000),
(8,  5,  490000,     0),
(9,  6,  890000, 40000),
(10, 6,  690000,     0);
SET IDENTITY_INSERT SANPHAM OFF;

-- ========================
-- 7. CD (12 dia)
-- CD1,CD2  = FIFA 24 (SP1)
-- CD3,CD4  = Elden Ring (SP3)
-- CD5,CD6  = COD MW3 (SP5)
-- CD7,CD8,CD9 = Witcher 3 (SP7)
-- CD10,CD11   = GTA V (SP9)
-- CD12        = GTA V (SP9) - Hong
-- ========================
SET IDENTITY_INSERT CD ON;
INSERT INTO CD (MaCD, MaSP, TinhTrang, TrangThai) VALUES
(1,  1, N'Moi',     N'SanSang'),   -- FIFA 24 CD1
(2,  1, N'LikeNew', N'SanSang'),   -- FIFA 24 CD2
(3,  3, N'Moi',     N'SanSang'),   -- Elden Ring CD1
(4,  3, N'99%',     N'SanSang'),   -- Elden Ring CD2
(5,  5, N'Moi',     N'SanSang'),   -- COD MW3 CD1
(6,  5, N'Cu',      N'SanSang'),   -- COD MW3 CD2
(7,  7, N'Moi',     N'SanSang'),   -- Witcher 3 CD1
(8,  7, N'LikeNew', N'SanSang'),   -- Witcher 3 CD2
(9,  7, N'TrayNhe', N'SanSang'),   -- Witcher 3 CD3
(10, 9, N'Moi',     N'SanSang'),   -- GTA V CD1
(11, 9, N'99%',     N'DaBan'),     -- GTA V CD2 (da ban)
(12, 9, N'Cu',      N'Hong');      -- GTA V CD3 (hong)
SET IDENTITY_INSERT CD OFF;

-- ========================
-- 8. ROM (6 rom)
-- SoLuotBan se duoc cap nhat boi trigger khi HoaDon DaThanhToan
-- ========================
INSERT INTO ROM (MaSP, DungLuong, LinkLuuTru, SoLuotBan) VALUES
(2,  N'50GB',  'https://storage.example.com/fifa24.zip',    0),
(4,  N'45GB',  'https://storage.example.com/eldenring.zip', 0),
(6,  N'100GB', 'https://storage.example.com/codmw3.zip',    0),
(7,  N'30GB',  'https://storage.example.com/witcher3.zip',  0),
(8,  N'1GB',   'https://storage.example.com/minecraft.zip', 0),
(10, N'95GB',  'https://storage.example.com/gtav.zip',      0);

-- ========================
-- 9. HOADON + CTHOADON
--
-- Cac truong hop can test:
--   HD1 : Mua 1 ROM, khong dung diem                      (KH1)
--   HD2 : Mua 1 ROM, co dung diem giam gia                (KH3)
--   HD3 : Mua 1 CD,  khong dung diem                      (KH2)
--   HD4 : Mua nhieu SP (1 CD + 1 ROM), co dung diem       (KH4)
--   HD5 : Mua 1 ROM, khong dung diem                      (KH4)
--   HD6 : Mua 1 CD,  khong dung diem                      (KH6)
--
-- TienGiam = DiemSuDung * 5.000
-- TongTien = SUM(CTHOADON) - TienGiam
--
-- HD1 : 990.000 - 0       = 990.000   -> diem CONG = FLOOR(990000/100000)  = 9
-- HD2 : 1.090.000-100.000 = 990.000   -> diem CONG = FLOOR(990000/100000)  = 9 (dung 20 diem, giam 100k)
--       KH3 bi tru 20 diem truoc
-- HD3 : 1.490.000 - 0     = 1.490.000 -> diem CONG = FLOOR(1490000/100000) = 14  (KH2 - mua CD Elden Ring)
--       TrangThai CD3 -> DaBan
-- HD4 : (1.290.000+990.000) - 50.000 = 2.230.000 -> diem CONG = FLOOR(2230000/100000) = 22  (KH4 - dung 10 diem, giam 50k)
--       TrangThai CD1 -> DaBan
-- HD5 : 490.000 - 0       = 490.000   -> diem CONG = FLOOR(490000/100000)  = 4   (KH2 - mua ROM Minecraft)
-- HD6 : 1.290.000 - 0     = 1.290.000 -> diem CONG = FLOOR(1290000/100000) = 12  (KH6 - mua CD FIFA)
--       TrangThai CD2 -> DaBan
-- ========================
SET IDENTITY_INSERT HOADON ON;
INSERT INTO HOADON (MaHD, MaKH, MaNV, NgayLap, TongTien, DiemSuDung, TienGiam, TrangThai) VALUES
(1, 1, 3, '2026-01-10 10:00:00',  990000,  0,      0, N'DaThanhToan'),  -- KH1 mua ROM FIFA
(2, 3, 3, '2026-01-15 14:00:00',  990000, 20, 100000, N'DaThanhToan'),  -- KH3 mua ROM COD, dung 20 diem
(3, 2, 4, '2026-02-01 09:00:00', 1490000,  0,      0, N'DaThanhToan'),  -- KH2 mua CD Elden Ring
(4, 4, 3, '2026-02-10 11:00:00', 2230000, 10,  50000, N'DaThanhToan'),  -- KH4 mua CD FIFA + ROM FIFA, dung 10 diem
(5, 2, 4, '2026-03-05 15:00:00',  490000,  0,      0, N'DaThanhToan'),  -- KH2 mua ROM Minecraft
(6, 6, 5, '2026-03-20 16:00:00', 1290000,  0,      0, N'DaThanhToan');  -- KH6 mua CD FIFA
SET IDENTITY_INSERT HOADON OFF;

INSERT INTO CTHOADON (MaHD, MaSP, SoLuong, DonGia) VALUES
(1, 2,  1,  990000),   -- HD1: ROM FIFA
(2, 6,  1, 1090000),   -- HD2: ROM COD MW3
(3, 3,  1, 1490000),   -- HD3: CD Elden Ring (CD3)
(4, 1,  1, 1290000),   -- HD4: CD FIFA (CD1)
(4, 2,  1,  990000),   -- HD4: ROM FIFA
(5, 8,  1,  490000),   -- HD5: ROM Minecraft
(6, 1,  1, 1290000);   -- HD6: CD FIFA (CD2)

-- CD da ban qua hoa don
UPDATE CD SET TrangThai = N'DaBan' WHERE MaCD IN (1, 2, 3);

-- Kich trigger TRG_TANG_SOLOIOTBAN_ROM
-- (HoaDon insert thang DaThanhToan nen trigger chua chay, update lai de chay)
UPDATE HOADON SET TrangThai = N'DaThanhToan' WHERE MaHD IN (1, 2, 3, 4, 5, 6);
-- Ket qua SoLuotBan sau trigger:
-- ROM FIFA (SP2)  : HD1(1) + HD4(1)          = 2
-- ROM COD  (SP6)  : HD2(1)                   = 1
-- ROM Minecraft (SP8): HD5(1)                = 1

-- ========================
-- 10. PHIEUTHUE + CTPHIEUTHUE
--
-- Mỗi phiếu thuê chỉ có 1 CD.
-- PT5 (cũ: 2 CD) được tách thành PT5 + PT6:
--   PT5 : KH4 thuê CD6 (COD MW3)   22/05 - 29/05
--   PT6 : KH4 thuê CD7 (Witcher 3) 22/05 - 29/05
-- ========================
SET IDENTITY_INSERT PHIEUTHUE ON;
INSERT INTO PHIEUTHUE (MaPT, MaKH, NgayThue, NgayTraDuKien, NgayTraThucTe, TienCoc, TienPhat, TrangThai) VALUES
(1, 1, '2026-05-20 09:00:00', '2026-05-27 09:00:00', NULL,                  200000,     0, N'DangThue'),
(2, 5, '2026-05-10 10:00:00', '2026-05-15 10:00:00', NULL,                  200000,     0, N'DangThue'),
(3, 2, '2026-03-29 08:00:00', '2026-04-05 08:00:00', '2026-04-05 07:30:00', 150000,     0, N'DaTra'),
(4, 6, '2026-04-24 09:00:00', '2026-05-01 09:00:00', '2026-05-05 14:00:00', 200000, 40000, N'DaTra'),
-- PT5: KH4 thuê CD6 (COD MW3)
(5, 4, '2026-05-22 10:00:00', '2026-05-29 10:00:00', NULL,                  200000,     0, N'DangThue'),
-- PT6: KH4 thuê CD7 (Witcher 3) - cùng đợt, tách riêng phiếu
(6, 4, '2026-05-22 10:00:00', '2026-05-29 10:00:00', NULL,                  150000,     0, N'DangThue');
SET IDENTITY_INSERT PHIEUTHUE OFF;

INSERT INTO CTPHIEUTHUE (MaPT, MaCD, MaNV, DonGiaThue) VALUES
(1, 4, 3, 60000),   -- PT1: CD4 Elden Ring
(2, 5, 4, 55000),   -- PT2: CD5 COD MW3
(3, 8, 3, 30000),   -- PT3: CD8 Witcher 3
(4, 9, 4, 30000),   -- PT4: CD9 Witcher 3
(5, 6, 3, 55000),   -- PT5: CD6 COD MW3
(6, 7, 3, 30000);   -- PT6: CD7 Witcher 3

-- Cập nhật trạng thái CD
UPDATE CD SET TrangThai = N'DangThue' WHERE MaCD IN (4, 5, 6, 7);
UPDATE CD SET TrangThai = N'SanSang'  WHERE MaCD IN (8, 9);

-- ========================
-- 11. DIEM_LICHSU
--
-- Diem CONG tu HoaDon = FLOOR(TongTien / 100.000):
--   HD1 KH1: 990k   -> 9
--   HD2 KH3: 990k   -> 9   (sau khi giam)
--   HD3 KH2: 1490k  -> 14
--   HD4 KH4: 2230k  -> 22  (sau khi giam)
--   HD5 KH2: 490k   -> 4
--   HD6 KH6: 1290k  -> 12  (sau khi giam)
--
-- Diem TRU:
--   HD2 KH3: dung 20 diem
--   HD4 KH4: dung 10 diem
--
-- Diem CONG tu PhieuThue = FLOOR(TienThue / 100.000):
--   PT3 KH2: CD8 Witcher3, 30k/ngay x 7 ngay = 210k -> 2
--   PT4 KH6: CD9 Witcher3, 30k/ngay x 7 ngay = 210k -> 2 (tinh theo so ngay thue du kien)
--     (hoac theo thuc te: 30k x 11 ngay = 330k -> 3, tuy nghiep vu)
--     -> chon tinh theo tong tien thuc tra (330k -> 3)
--
-- Ket qua DiemTichLuy:
--   KH1: 9         -> 9   ... nhung da khai la 13 o tren -> them diem cu truoc khi co data
--        -> them 4 diem tu giao dich cu (truoc 2026)
--   KH2: 14+4+2    -> 20  ... nhung da khai la 15 -> cap nhat lai KHACHHANG phai dung
-- => De don gian va chinh xac, DiemTichLuy = tong thuc tu DIEM_LICHSU:
--   KH1: CONG=9+4=13,   TRU=0  -> 13  ✓
--   KH2: CONG=14+4+2=20,TRU=9  -> 15  ✓  (KH2 da dung 9 diem truoc do (giao dich cu))
-- Ghi chu: De du lieu khop voi DiemTichLuy khai bao, them 1 so ban ghi
-- bieu dien giao dich diem cu cua KH2 va KH4.
--
-- KH1: 9 (HD1) + 4 (giao dich cu) = 13                TRU=0       -> 13
-- KH2: 14(HD3)+4(HD5)+2(PT3) = 20, TRU=9(cu)+1(=10?) -> can tinh lai
-- => Thay doi cach tiep can: DiemTichLuy chinh xac theo DIEM_LICHSU la chuan.
--    Khai DiemTichLuy o KHACHHANG theo dung tong nay.
--
-- FINAL DiemTichLuy:
--   KH1: CONG 9+4=13,  TRU 0  -> 13
--   KH2: CONG 14+4+2=20, TRU 5 -> 15
--   KH3: CONG 9,        TRU 20 -> 0  (khong am)
--   KH4: CONG 22+5=27,  TRU 10 -> 17 (*)
--   KH5: CONG 5,        TRU 0  -> 5
--   KH6: CONG 12+3=15,  TRU 15 -> 0  (*)
-- (*) Chinh lai DiemTichLuy KHACHHANG cho dung
-- ========================

-- Chinh lai DiemTichLuy cho dung truoc khi insert DIEM_LICHSU
UPDATE KHACHHANG SET DiemTichLuy =  13 WHERE MaKH = 1;
UPDATE KHACHHANG SET DiemTichLuy =  15 WHERE MaKH = 2;
UPDATE KHACHHANG SET DiemTichLuy =   0 WHERE MaKH = 3;
UPDATE KHACHHANG SET DiemTichLuy =  17 WHERE MaKH = 4;
UPDATE KHACHHANG SET DiemTichLuy =   5 WHERE MaKH = 5;
UPDATE KHACHHANG SET DiemTichLuy =   0 WHERE MaKH = 6;

SET IDENTITY_INSERT DIEM_LICHSU ON;
INSERT INTO DIEM_LICHSU (MaLS, MaKH, MaPT, Loai, SoDiem, Ngay, GhiChu) VALUES
-- Giao dich cu cua KH1 (truoc 2026)
(1,  1, NULL, N'CONG',  4, '2025-12-01 10:00:00', N'Tich luy giao dich cu KH1'),
-- Giao dich cu cua KH2 (da dung 5 diem truoc)
(2,  2, NULL, N'TRU',   5, '2025-11-01 09:00:00', N'Su dung diem giao dich cu KH2'),
-- Giao dich cu cua KH4 (co 5 diem truoc)
(3,  4, NULL, N'CONG',  5, '2025-10-15 11:00:00', N'Tich luy giao dich cu KH4'),
-- Giao dich cu cua KH5 (co 5 diem truoc)
(4,  5, NULL, N'CONG',  5, '2025-09-20 14:00:00', N'Tich luy giao dich cu KH5'),
-- Giao dich cu cua KH6 (da dung 15 diem truoc)
(5,  6, NULL, N'TRU',  15, '2025-08-10 16:00:00', N'Su dung diem giao dich cu KH6'),
-- HD1: KH1 mua ROM FIFA (990k -> 9 diem)
(6,  1, NULL, N'CONG',  9, '2026-01-10 10:05:00', N'Tich luy mua hang HD1 - ROM FIFA 24'),
-- HD2: KH3 dung 20 diem roi tich 9 diem
(7,  3, NULL, N'TRU',  20, '2026-01-15 14:00:00', N'Su dung diem giam gia HD2 - ROM COD MW3'),
(8,  3, NULL, N'CONG',  9, '2026-01-15 14:05:00', N'Tich luy mua hang HD2 - ROM COD MW3'),
-- HD3: KH2 mua CD Elden Ring (1490k -> 14 diem)
(9,  2, NULL, N'CONG', 14, '2026-02-01 09:05:00', N'Tich luy mua hang HD3 - CD Elden Ring'),
-- HD4: KH4 dung 10 diem roi tich 22 diem
(10, 4, NULL, N'TRU',  10, '2026-02-10 11:00:00', N'Su dung diem giam gia HD4 - CD FIFA + ROM FIFA'),
(11, 4, NULL, N'CONG', 22, '2026-02-10 11:05:00', N'Tich luy mua hang HD4 - CD FIFA + ROM FIFA'),
-- HD5: KH2 mua ROM Minecraft (490k -> 4 diem)
(12, 2, NULL, N'CONG',  4, '2026-03-05 15:05:00', N'Tich luy mua hang HD5 - ROM Minecraft'),
-- HD6: KH6 mua CD FIFA (1290k -> 12 diem)
(13, 6, NULL, N'CONG', 12, '2026-03-20 16:05:00', N'Tich luy mua hang HD6 - CD FIFA 24'),
-- PT3: KH2 tra CD Witcher3 dung han (210k -> 2 diem)
(14, 2, 3,    N'CONG',  2, '2026-04-05 07:35:00', N'Tich luy thue game PT3 - CD Witcher 3'),
-- PT4: KH6 tra CD Witcher3 tre han (tong thuc tra 30k x 11 ngay = 330k -> 3 diem)
(15, 6, 4,    N'CONG',  3, '2026-05-05 14:05:00', N'Tich luy thue game PT4 - CD Witcher 3');
SET IDENTITY_INSERT DIEM_LICHSU OFF;

EXEC sp_MSforeachtable 'ALTER TABLE ? CHECK CONSTRAINT ALL';
GO