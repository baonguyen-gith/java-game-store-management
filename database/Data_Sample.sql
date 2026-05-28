-- ============================================================
-- DU LIEU MAU - qlgamee (SQL Server)
-- Ngay cap nhat: 2026-05-28
--
-- QUY TAC DIEM:
--   TICH : FLOOR(TongTien / 100.000) = 1 diem
--   TIEU : 1 diem giam 5.000 VND
--
-- LICH SU BIEN DONG CD:
--   CD1  (FIFA 24)     -> DaBan  (qua HD4)
--   CD2  (FIFA 24)     -> DaBan  (qua HD6)
--   CD3  (Elden Ring)  -> DaBan  (qua HD3)
--   CD4  (Elden Ring)  -> DangThue (PT1 - KH1, dang thue, con han)
--   CD5  (COD MW3)     -> DangThue (PT2 - KH5, QUHAN 13 ngay)
--   CD6  (COD MW3)     -> DangThue (PT5 - KH4, con han)
--   CD7  (Witcher 3)   -> DangThue (PT6 - KH4, con han)
--   CD8  (Witcher 3)   -> SanSang  (da tra - PT3 DaTra dung han)
--   CD9  (Witcher 3)   -> SanSang  (da tra - PT4 DaTra tre han)
--   CD10 (GTA V)       -> SanSang
--   CD11 (GTA V)       -> Hong
--
-- PHIEU THUE:
--   PT1: KH1, CD4 Elden Ring,  20/05-30/05, DangThue, con han (10 ngay, 600k)  [TienCoc=1.200.000]
--   PT2: KH5, CD5 COD MW3,     10/05-15/05, DangThue, QUA HAN 13 ngay (phat TT = 13*55k*1.5)
--   PT3: KH2, CD8 Witcher 3,   29/03-05/04, DaTra dung han (7 ngay, 210k, 2 diem)
--   PT4: KH6, CD9 Witcher 3,   24/04-01/05, DaTra tre han 4 ngay (TienPhat da chot)
--   PT5: KH4, CD6 COD MW3,     22/05-29/05, DangThue, con han (7 ngay, 385k)
--   PT6: KH4, CD7 Witcher 3,   22/05-29/05, DangThue, con han (7 ngay, 210k)
--
-- DIEM TICH LUY CUOI CUNG (da tinh lai chinh xac):
--   KH1 : CONG=9(HD1)+22(HD4)=31,            TRU=10(HD4)  -> 21  [co diem dang thue PT1 chua tinh]
--   KH2 : CONG=14(HD3)+4(HD5)+2(PT3)=20,    TRU=5(cu)    -> 15
--   KH3 : CONG=20(cu)+9(HD2)=29,             TRU=20(HD2)  ->  9
--   KH4 : CONG=5(cu)=5,                       TRU=0        ->  5  [dang thue PT5+PT6 chua tinh]
--   KH5 : CONG=5(cu)=5, TRU=0                       ->  5  [dang thue PT2 chua tinh]
--   KH6 : CONG=12(HD6)+3(PT4)=15, TRU=0                 -> 15
-- ============================================================

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
(1, N'Nguyen Van An',    '0901111111', '1985-03-10', '079201000001', '2020-01-01'),
(2, N'Tran Thi Bich',    '0902222222', '1990-07-22', '079201000002', '2020-06-01'),
(3, N'Le Van Cuong',     '0903333333', '1995-11-05', '079201000003', '2021-02-01'),
(4, N'Pham Thi Dung',    '0904444444', '1997-04-18', '079201000004', '2022-05-01'),
(5, N'Hoang Van Em',     '0905555555', '1993-09-30', '079201000005', '2021-10-01');
SET IDENTITY_INSERT NHANVIEN OFF;

-- ============================================================
-- 3. USERS  (Password: BCrypt hash cua 'Password@123')
-- ============================================================
SET IDENTITY_INSERT USERS ON;
INSERT INTO USERS (MaUser, Username, Password, MaRole, MaNV) VALUES
(1, 'admin',     '$2a$10$slYQmyNdgTY18LMoDIs11OHQEBsxGBsS3bT.T5ZZNGPDiPDy6JbTu', 1, 1),
(2, 'quanly',    '$2a$10$slYQmyNdgTY18LMoDIs11OHQEBsxGBsS3bT.T5ZZNGPDiPDy6JbTu', 2, 2),
(3, 'nhanvien1', '$2a$10$slYQmyNdgTY18LMoDIs11OHQEBsxGBsS3bT.T5ZZNGPDiPDy6JbTu', 3, 3),
(4, 'nhanvien2', '$2a$10$slYQmyNdgTY18LMoDIs11OHQEBsxGBsS3bT.T5ZZNGPDiPDy6JbTu', 3, 4),
(5, 'nhanvien3', '$2a$10$slYQmyNdgTY18LMoDIs11OHQEBsxGBsS3bT.T5ZZNGPDiPDy6JbTu', 3, 5);
SET IDENTITY_INSERT USERS OFF;

-- ============================================================
-- 4. KHACH HANG
-- DiemTichLuy khai bao khop chinh xac voi DIEM_LICHSU o cuoi file
-- ============================================================
SET IDENTITY_INSERT KHACHHANG ON;
INSERT INTO KHACHHANG (MaKH, HoTen, SDT, CCCD, Email, DiaChi, DiemTichLuy) VALUES
(1, N'Nguyen Thi Lan',  '0911000001', '012300000001', 'lan@email.com',  N'Ha Noi',     21),
(2, N'Tran Van Minh',   '0911000002', '012300000002', 'minh@email.com', N'TP. HCM',    15),
(3, N'Le Thi Hoa',      '0911000003', '012300000003', 'hoa@email.com',  N'Da Nang',     9),
(4, N'Pham Van Duc',    '0911000004', '012300000004', 'duc@email.com',  N'Hai Phong',   5),
(5, N'Hoang Thi Mai',   '0911000005', '012300000005', 'mai@email.com',  N'Can Tho',     5),
(6, N'Vu Quoc Hung',    '0911000006', '012300000006', 'hung@email.com', N'Binh Duong', 15);
SET IDENTITY_INSERT KHACHHANG OFF;

-- ============================================================
-- 5. GAME
-- ============================================================
SET IDENTITY_INSERT GAME ON;
INSERT INTO GAME (MaGame, TenGame, TheLoai, NenTang, GhiChu, HinhAnh) VALUES
(1, N'FIFA 24',          N'The thao',  N'PS5/Xbox', N'Game bong da the thao dinh cao',   '/icons/fifa24.jpg'),
(2, N'Elden Ring',       N'RPG',       N'PC/PS5',   N'RPG hanh dong the gioi mo rong',   '/icons/eldenring.jpg'),
(3, N'Call of Duty MW3', N'Ban sung',  N'PC/Xbox',  N'FPS ban sung goc nhin thu nhat',   '/icons/codmw3.jpg'),
(4, N'The Witcher 3',    N'RPG',       N'PC/PS4',   N'RPG kich ban phong phu sau sac',   '/icons/witcher3.jpg'),
(5, N'Minecraft',        N'Sandbox',   N'PC/Xbox',  N'Sandbox xay dung va kham pha',     '/icons/minecraft.jpg'),
(6, N'GTA V',            N'Hanh dong', N'PC/PS4',   N'The gioi mo hanh dong Rockstar',   '/icons/gtav.png');
SET IDENTITY_INSERT GAME OFF;

-- ============================================================
-- 5.1 GAME_CHITIET
-- ============================================================
INSERT INTO GAME_CHITIET
    (MaGame, MoTa, Rating, Genre, DeliveryMethod, ReleaseDate, Region, Features, Language, Currency)
VALUES
(1, N'Tua game bong da hang nam dinh cao cua EA Sports',       N'E', N'Sports',  N'CD/ROM', '2023-09-29', N'Worldwide', N'Co-op, Online',  N'Tieng Anh', N'USD'),
(2, N'RPG hanh dong tu FromSoftware voi the gioi rong lon',    N'M', N'RPG',     N'CD/ROM', '2022-02-25', N'Worldwide', N'Single Player',  N'Tieng Anh', N'USD'),
(3, N'FPS noi tieng toan cau voi multiplayer soi dong',        N'M', N'FPS',     N'CD/ROM', '2023-11-10', N'Worldwide', N'Multiplayer',    N'Tieng Anh', N'USD'),
(4, N'RPG the gioi mo kich ban phong phu cua CD Projekt',      N'M', N'RPG',     N'CD/ROM', '2015-05-19', N'Worldwide', N'Single Player',  N'Tieng Anh', N'USD'),
(5, N'Sandbox xay dung va kham pha noi tieng the gioi',        N'E', N'Sandbox', N'ROM',    '2011-11-18', N'Worldwide', N'Co-op',          N'Tieng Anh', N'USD'),
(6, N'The gioi mo hanh dong noi tieng cua Rockstar Games',     N'M', N'Action',  N'CD/ROM', '2013-09-17', N'Worldwide', N'Multiplayer',    N'Tieng Anh', N'USD');

-- ============================================================
-- 6. SAN PHAM
--   SP1  = CD  FIFA 24         GiaBan=1.290.000  GiaThue= 50.000/ngay
--   SP2  = ROM FIFA 24         GiaBan=  990.000  GiaThue=0
--   SP3  = CD  Elden Ring      GiaBan=1.490.000  GiaThue= 60.000/ngay
--   SP4  = ROM Elden Ring      GiaBan=1.190.000  GiaThue=0
--   SP5  = CD  COD MW3         GiaBan=1.390.000  GiaThue= 55.000/ngay
--   SP6  = ROM COD MW3         GiaBan=1.090.000  GiaThue=0
--   SP7  = CD  Witcher 3       GiaBan=  590.000  GiaThue= 30.000/ngay
--   SP8  = ROM Witcher 3       GiaBan=  390.000  GiaThue=0        <- FIX: ROM rieng
--   SP9  = ROM Minecraft       GiaBan=  490.000  GiaThue=0
--   SP10 = CD  GTA V           GiaBan=  890.000  GiaThue= 40.000/ngay
--   SP11 = ROM GTA V           GiaBan=  690.000  GiaThue=0
-- ============================================================
SET IDENTITY_INSERT SANPHAM ON;
INSERT INTO SANPHAM (MaSP, MaGame, GiaBan, GiaThueNgay) VALUES
(1,  1, 1290000,  50000),   -- CD  FIFA 24
(2,  1,  990000,      0),   -- ROM FIFA 24
(3,  2, 1490000,  60000),   -- CD  Elden Ring
(4,  2, 1190000,      0),   -- ROM Elden Ring
(5,  3, 1390000,  55000),   -- CD  COD MW3
(6,  3, 1090000,      0),   -- ROM COD MW3
(7,  4,  590000,  30000),   -- CD  Witcher 3
(8,  4,  390000,      0),   -- ROM Witcher 3  <- FIX: MaGame=4, khong phai 5
(9,  5,  490000,      0),   -- ROM Minecraft  <- FIX: SP9 la Minecraft
(10, 6,  890000,  40000),   -- CD  GTA V
(11, 6,  690000,      0);   -- ROM GTA V
SET IDENTITY_INSERT SANPHAM OFF;

-- ============================================================
-- 7. CD  (11 dia)
--   FIFA 24 (SP1)   : CD1, CD2
--   Elden Ring (SP3): CD3, CD4
--   COD MW3 (SP5)   : CD5, CD6
--   Witcher 3 (SP7) : CD7, CD8, CD9
--   GTA V (SP10)    : CD10, CD11
-- TrangThai se duoc cap nhat dung sau khi insert PHIEUTHUE / HOADON
-- ============================================================
SET IDENTITY_INSERT CD ON;
INSERT INTO CD (MaCD, MaSP, TinhTrang, TrangThai) VALUES
(1,   1, N'Moi',     N'SanSang'),   -- FIFA 24 #1
(2,   1, N'LikeNew', N'SanSang'),   -- FIFA 24 #2
(3,   3, N'Moi',     N'SanSang'),   -- Elden Ring #1
(4,   3, N'99%',     N'SanSang'),   -- Elden Ring #2
(5,   5, N'Moi',     N'SanSang'),   -- COD MW3 #1
(6,   5, N'Cu',      N'SanSang'),   -- COD MW3 #2
(7,   7, N'Moi',     N'SanSang'),   -- Witcher 3 #1
(8,   7, N'LikeNew', N'SanSang'),   -- Witcher 3 #2
(9,   7, N'TrayNhe', N'SanSang'),   -- Witcher 3 #3
(10, 10, N'Moi',     N'SanSang'),   -- GTA V #1
(11, 10, N'Cu',      N'SanSang');   -- GTA V #2
SET IDENTITY_INSERT CD OFF;

-- ============================================================
-- 8. ROM  (6 rom, MaSP khop voi bang SANPHAM da sua)
--   ROM FIFA 24    = SP2
--   ROM Elden Ring = SP4
--   ROM COD MW3    = SP6
--   ROM Witcher 3  = SP8   <- FIX: truoc la SP7 (nhầm voi CD)
--   ROM Minecraft  = SP9   <- FIX: truoc la SP8
--   ROM GTA V      = SP11  <- FIX: truoc la SP10
-- SoLuotBan se duoc trigger cap nhat khi HoaDon chuyen DaThanhToan
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
-- Cac truong hop test:
--   HD1: KH1 mua ROM FIFA 24        - khong dung diem          -> 990k   -> 9  diem CONG
--   HD2: KH3 mua ROM COD MW3        - DUNG 20 DIEM giam 100k   -> 990k   -> 9  diem CONG
--   HD3: KH2 mua CD Elden Ring      - khong dung diem          -> 1490k  -> 14 diem CONG
--   HD4: KH1 mua CD FIFA + ROM FIFA - DUNG 10 DIEM giam 50k    -> 2230k  -> 22 diem CONG
--                                      (CD1 -> DaBan)
--   HD5: KH2 mua ROM Minecraft      - khong dung diem          -> 490k   -> 4  diem CONG
--   HD6: KH6 mua CD FIFA            - khong dung diem          -> 1290k  -> 12 diem CONG
--                                      (CD2 -> DaBan)
--   HD7: KHACH VANG LAI mua ROM GTA - khong co KH, khong diem  -> 690k   (khong cong diem)
--
-- TienGiam = DiemSuDung * 5.000
-- TongTien = SUM(CTHOADON.SoLuong * DonGia) - TienGiam
-- ============================================================
SET IDENTITY_INSERT HOADON ON;
INSERT INTO HOADON (MaHD, MaKH, MaNV, NgayLap, TongTien, DiemSuDung, TienGiam, TrangThai) VALUES
(1, 1,    3, '2026-01-10 10:00:00',  990000,  0,      0, N'DaThanhToan'), -- KH1 ROM FIFA
(2, 3,    3, '2026-01-15 14:00:00',  990000, 20, 100000, N'DaThanhToan'), -- KH3 ROM COD, dung 20 diem
(3, 2,    4, '2026-02-01 09:00:00', 1490000,  0,      0, N'DaThanhToan'), -- KH2 CD Elden Ring (CD3)
(4, 1,    3, '2026-02-10 11:00:00', 2230000, 10,  50000, N'DaThanhToan'), -- KH1 CD FIFA(CD1)+ROM FIFA, dung 10 diem
(5, 2,    4, '2026-03-05 15:00:00',  490000,  0,      0, N'DaThanhToan'), -- KH2 ROM Minecraft
(6, 6,    5, '2026-03-20 16:00:00', 1290000,  0,      0, N'DaThanhToan'), -- KH6 CD FIFA (CD2)
(7, NULL, 3, '2026-04-12 13:00:00',  690000,  0,      0, N'DaThanhToan'); -- Khach vang lai, ROM GTA V
SET IDENTITY_INSERT HOADON OFF;

INSERT INTO CTHOADON (MaHD, MaSP, SoLuong, DonGia) VALUES
(1,  2, 1,  990000),   -- HD1: ROM FIFA 24
(2,  6, 1, 1090000),   -- HD2: ROM COD MW3  (gia goc 1090k, giam 100k -> TongTien 990k)
(3,  3, 1, 1490000),   -- HD3: CD Elden Ring (CD3 -> DaBan)
(4,  1, 1, 1290000),   -- HD4: CD FIFA 24   (CD1 -> DaBan)
(4,  2, 1,  990000),   -- HD4: ROM FIFA 24
(5,  9, 1,  490000),   -- HD5: ROM Minecraft
(6,  1, 1, 1290000),   -- HD6: CD FIFA 24   (CD2 -> DaBan)
(7, 11, 1,  690000);   -- HD7: ROM GTA V

-- Cap nhat CD da ban qua hoa don
UPDATE CD SET TrangThai = N'DaBan' WHERE MaCD IN (1, 2, 3);

-- Kich trigger TRG_TANG_SOLOIOTBAN_ROM
-- (INSERT thang DaThanhToan -> trigger chua chay, can UPDATE lai)
UPDATE HOADON SET TrangThai = N'DaThanhToan' WHERE MaHD IN (1, 2, 3, 4, 5, 6, 7);
-- SoLuotBan sau trigger:
--   ROM FIFA    (SP2)  : HD1(1) + HD4(1) = 2
--   ROM COD MW3 (SP6)  : HD2(1)          = 1
--   ROM Minecraft (SP9): HD5(1)          = 1
--   ROM GTA V   (SP11) : HD7(1)          = 1

-- ============================================================
-- 10. PHIEU THUE + CHI TIET
--
-- Moi phieu chi co 1 CD (thiet ke 1-1 de don gian khi demo).
--
-- PT1: KH1, CD4 Elden Ring  (60k/ngay), 20/05-30/05, DangThue, CON HAN
--      -> Hom nay 28/05 -> con han, phatTreTamTinh = 0
--      -> DonGiaThue = 60k * 10 ngay = 600.000
--      -> TienCoc = MAX(250.000, 600.000 * 2) = 1.200.000
--
-- PT2: KH5, CD5 COD MW3     (55k/ngay), 10/05-15/05, DangThue, QUA HAN 13 NGAY
--      -> Hom nay 28/05 -> tre 13 ngay -> phatTreTamTinh = 13 * 55k * 1.5 = 1.072.500
--      -> DonGiaThue = 55k * 5 ngay = 275.000
--      -> TienCoc = MAX(250.000, 275.000 * 2) = 550.000
--
-- PT3: KH2, CD8 Witcher 3   (30k/ngay), 29/03-05/04, DaTra DUNG HAN
--      -> Tra dung gio, TienPhat = 0
--      -> DonGiaThue = 30k * 7 ngay = 210.000
--      -> Diem tich luy = FLOOR(210000/100000) = 2 diem
--
-- PT4: KH6, CD9 Witcher 3   (30k/ngay), 24/04-01/05, DaTra TRE HAN 4 NGAY
--      -> Tra ngay 05/05, tre 4 ngay
--      -> TienPhat (da chot) = 4 * 30k * 1.5 = 180.000
--      -> DonGiaThue = 30k * 7 ngay = 210.000
--      -> Diem tich luy = FLOOR(210000/100000) = 2 diem (tinh tren tien thue goc)
--         (tren thuc te co the tinh them ca phat, nhung o day tinh tren tien thue goc)
--         -> chinh xac hon: tinh FLOOR((210k+180k)/100k) = 3 diem
--
-- PT5: KH4, CD6 COD MW3     (55k/ngay), 22/05-29/05, DangThue, CON HAN
--      -> DonGiaThue = 55k * 7 ngay = 385.000
--
-- PT6: KH4, CD7 Witcher 3   (30k/ngay), 22/05-29/05, DangThue, CON HAN
--      -> DonGiaThue = 30k * 7 ngay = 210.000
-- ============================================================
SET IDENTITY_INSERT PHIEUTHUE ON;
INSERT INTO PHIEUTHUE
    (MaPT, MaKH, NgayThue, NgayTraDuKien, NgayTraThucTe, TienCoc, TienPhat, TrangThai)
VALUES
-- PT1: KH1, Elden Ring, con han (deadline 30/05 -> hom nay 28/05 -> con han 2 ngay)
(1, 1, '2026-05-20 09:00:00', '2026-05-30 09:00:00', NULL,                   1200000,      0, N'DangThue'),
-- PT2: KH5, COD MW3, QUA HAN 13 NGAY (deadline 15/05, hom nay 28/05)
(2, 5, '2026-05-10 10:00:00', '2026-05-15 10:00:00', NULL,                    550000,      0, N'DangThue'),
-- PT3: KH2, Witcher 3, DaTra dung han
(3, 2, '2026-03-29 08:00:00', '2026-04-05 08:00:00', '2026-04-05 07:30:00',  420000,      0, N'DaTra'),
-- PT4: KH6, Witcher 3, DaTra tre han 4 ngay (01/05 -> tra 05/05)
(4, 6, '2026-04-24 09:00:00', '2026-05-01 09:00:00', '2026-05-05 14:00:00',  420000, 180000, N'DaTra'),
-- PT5: KH4, COD MW3, con han
(5, 4, '2026-05-22 10:00:00', '2026-05-29 10:00:00', NULL,                    770000,      0, N'DangThue'),
-- PT6: KH4, Witcher 3, con han
(6, 4, '2026-05-22 10:00:00', '2026-05-29 10:00:00', NULL,                    420000,      0, N'DangThue');
SET IDENTITY_INSERT PHIEUTHUE OFF;

-- DonGiaThue = GiaThueNgay * SoNgayThue
INSERT INTO CTPHIEUTHUE (MaPT, MaCD, MaNV, DonGiaThue) VALUES
(1, 4, 3, 600000),   -- PT1: CD4 Elden Ring  60k * 10 ngay (20/05 -> 30/05)
(2, 5, 4, 275000),   -- PT2: CD5 COD MW3     55k * 5 ngay
(3, 8, 3, 210000),   -- PT3: CD8 Witcher 3   30k * 7 ngay
(4, 9, 4, 210000),   -- PT4: CD9 Witcher 3   30k * 7 ngay
(5, 6, 3, 385000),   -- PT5: CD6 COD MW3     55k * 7 ngay
(6, 7, 3, 210000);   -- PT6: CD7 Witcher 3   30k * 7 ngay

-- Cap nhat TrangThai CD theo phieu thue
UPDATE CD SET TrangThai = N'DangThue' WHERE MaCD IN (4, 5, 6, 7);  -- dang duoc thue
UPDATE CD SET TrangThai = N'SanSang'  WHERE MaCD IN (8, 9);         -- da tra xong
UPDATE CD SET TrangThai = N'Hong'     WHERE MaCD = 11;              -- hong, khong dung duoc

-- ============================================================
-- 11. DIEM LICH SU
--
-- Nguon tinh diem CONG tu hoa don (FLOOR(TongTien / 100.000)):
--   HD1 KH1 : 990k         ->  9 diem
--   HD2 KH3 : 990k         ->  9 diem  (KH3 truoc do TRU 20 diem)
--   HD3 KH2 : 1490k        -> 14 diem
--   HD4 KH1 : 2230k        -> 22 diem  (KH1 truoc do TRU 10 diem)
--   HD5 KH2 : 490k         ->  4 diem
--   HD6 KH6 : 1290k        -> 12 diem
--   HD7 KH NULL            ->  0 diem  (khach vang lai, khong ghi)
--
-- Nguon tinh diem CONG tu phieu thue (FLOOR(TongTienThue / 100.000)):
--   PT3 KH2 : 210k         ->  2 diem  (tra dung han)
--   PT4 KH6 : 210k + 180k = 390k -> 3 diem (tinh ca tien phat da dong)
--
-- Giao dich cu (truoc 2026):
--   KH2: -5 diem (da tieu truoc)
--   KH3: +20 diem cu (de giai thich nguon 20 diem da tieu khi mua HD2)
--   KH4: +5 diem cu
--   KH5: +5 diem cu
--
-- KIEM TRA TONG KET:
--   KH1: CONG=9(HD1)+22(HD4)=31,  TRU=10(HD4)  -> 21 ✓
--   KH2: CONG=14(HD3)+4(HD5)+2(PT3)=20, TRU=5(cu) -> 15 ✓
--   KH3: CONG=20(cu)+9(HD2)=29,   TRU=20(HD2)  ->  9 ✓
--   KH4: CONG=5(cu)=5,             TRU=0        ->  5 ✓
--   KH5: CONG=5(cu)=5,             TRU=0        ->  5 ✓
--   KH6: CONG=12(HD6)+3(PT4)=15,  TRU=0        -> 15 ✓
-- ============================================================

-- DiemTichLuy da duoc INSERT chinh xac o buoc 4, khong can UPDATE lai.
-- Giu lai de kiem tra khop voi DIEM_LICHSU:
--   KH1: CONG 9+22=31, TRU 10 -> 21  ✓
--   KH2: CONG 14+4+2=20, TRU 5 -> 15 ✓
--   KH3: CONG 20+9=29, TRU 20  ->  9 ✓
--   KH4: CONG 5=5,  TRU 0      ->  5 ✓
--   KH5: CONG 5=5,  TRU 0      ->  5 ✓
--   KH6: CONG 12+3=15, TRU 0   -> 15 ✓

SET IDENTITY_INSERT DIEM_LICHSU ON;
INSERT INTO DIEM_LICHSU (MaLS, MaKH, MaPT, Loai, SoDiem, Ngay, GhiChu) VALUES

-- ── Giao dich cu cua KH3 (co 20 diem truoc khi mua HD2) ──────────────
(1,  3, NULL, N'CONG', 20, '2025-06-01 09:00:00', N'Tich luy giao dich cu KH3'),

-- ── Giao dich cu cua KH2 (da tieu 5 diem truoc 2026) ─────────────────
(2,  2, NULL, N'TRU',   5, '2025-10-15 11:00:00', N'Su dung diem giao dich cu KH2'),

-- ── Giao dich cu cua KH4 (+5 diem truoc 2026) ────────────────────────
(3,  4, NULL, N'CONG',  5, '2025-11-20 14:00:00', N'Tich luy giao dich cu KH4'),

-- ── Giao dich cu cua KH5 (+5 diem truoc 2026) ────────────────────────
(4,  5, NULL, N'CONG',  5, '2025-12-05 10:00:00', N'Tich luy giao dich cu KH5'),

-- ── HD1: KH1 mua ROM FIFA 24 (990k -> 9 diem) ────────────────────────
(5,  1, NULL, N'CONG',  9, '2026-01-10 10:05:00', N'Tich luy mua hang HD0001 - ROM FIFA 24'),

-- ── HD2: KH3 tieu 20 diem roi tich 9 diem ────────────────────────────
(6,  3, NULL, N'TRU',  20, '2026-01-15 14:00:00', N'Su dung diem giam gia HD0002 - ROM COD MW3'),
(7,  3, NULL, N'CONG',  9, '2026-01-15 14:05:00', N'Tich luy mua hang HD0002 - ROM COD MW3'),

-- ── HD3: KH2 mua CD Elden Ring (1490k -> 14 diem) ────────────────────
(8,  2, NULL, N'CONG', 14, '2026-02-01 09:05:00', N'Tich luy mua hang HD0003 - CD Elden Ring'),

-- ── HD4: KH1 tieu 10 diem roi tich 22 diem ───────────────────────────
(9,  1, NULL, N'TRU',  10, '2026-02-10 11:00:00', N'Su dung diem giam gia HD0004 - CD FIFA + ROM FIFA'),
(10, 1, NULL, N'CONG', 22, '2026-02-10 11:05:00', N'Tich luy mua hang HD0004 - CD FIFA + ROM FIFA'),

-- ── HD5: KH2 mua ROM Minecraft (490k -> 4 diem) ──────────────────────
(11, 2, NULL, N'CONG',  4, '2026-03-05 15:05:00', N'Tich luy mua hang HD0005 - ROM Minecraft'),

-- ── HD6: KH6 mua CD FIFA 24 (1290k -> 12 diem) ───────────────────────
(12, 6, NULL, N'CONG', 12, '2026-03-20 16:05:00', N'Tich luy mua hang HD0006 - CD FIFA 24'),

-- ── PT3: KH2 tra CD Witcher 3 dung han (210k -> 2 diem) ──────────────
(13, 2, 3,    N'CONG',  2, '2026-04-05 07:35:00', N'Tich luy thue game PT0003 - CD Witcher 3'),

-- ── PT4: KH6 tra CD Witcher 3 tre han (210k+180k=390k -> 3 diem) ─────
(14, 6, 4,    N'CONG',  3, '2026-05-05 14:05:00', N'Tich luy thue game PT0004 - CD Witcher 3');

SET IDENTITY_INSERT DIEM_LICHSU OFF;

-- Bat lai constraint sau khi insert xong
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