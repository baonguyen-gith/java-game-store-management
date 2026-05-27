-- ========================
-- DỮ LIỆU MẪU CỦA HỆ THỐNG QUẢN LÝ GAME STORE (QLGAME)
-- ========================
 
-- Tắt FK tạm thời để chèn mượt
EXEC sp_MSforeachtable 'ALTER TABLE ? NOCHECK CONSTRAINT ALL';
GO
 
-- ========================
-- 1. ROLE
-- ========================
SET IDENTITY_INSERT ROLE ON;
INSERT INTO ROLE (MaRole, TenRole) VALUES
(1, N'Admin'),
(2, N'Quản lý'),
(3, N'Nhân viên bán hàng'),
(4, N'Nhân viên kỹ thuật');
SET IDENTITY_INSERT ROLE OFF;
 
-- ========================
-- 2. NHANVIEN
-- ========================
SET IDENTITY_INSERT NHANVIEN ON;
INSERT INTO NHANVIEN (MaNV, HoTen, SDT, NgaySinh, CCCD, NgayVaoLam) VALUES
(1, N'Nguyễn Văn A', '0912345678', '1995-05-10', '001095001234', '2023-01-01'),
(2, N'Trần Thị B',   '0987654321', '1998-11-20', '001098005678', '2023-05-15'),
(3, N'Lê Văn C',     '0905123456', '2000-02-15', '001200009999', '2024-02-01'),
(4, N'Phạm Minh Đức', '0934888999', '1996-08-25', '001096009876', '2023-09-10'),
(5, N'Hoàng Thu Trang', '0977222333', '1999-04-12', '001099003421', '2024-01-15');
SET IDENTITY_INSERT NHANVIEN OFF;
 
-- ========================
-- 3. USERS
-- ========================
SET IDENTITY_INSERT USERS ON;
INSERT INTO USERS (MaUser, Username, Password, MaRole, MaNV) VALUES
(1, 'admin',       '12345678', 1, 1),
(2, 'nv_banhang',  '12345678',     3, 2),
(3, 'manager',     '12345678',2, 4),
(4, 'nv_banhang2', '12345678',     3, 5),
(5, 'nv_kythuat',  '12345678',   4, 3);
SET IDENTITY_INSERT USERS OFF;

-- ========================
-- 4. KHACHHANG
-- ========================
SET IDENTITY_INSERT KHACHHANG ON;
INSERT INTO KHACHHANG (MaKH, HoTen, SDT, CCCD, Email, DiaChi, DiemTichLuy) VALUES
(1, N'Lý Tiểu Long',         '0909111222', '079123456789', 'long.ly@gmail.com',      N'Quận 5, TP.HCM', 150),
(2, N'Phạm Nhật Vượng',      '0901000999', '001099111222', 'vuong.pham@vinhomes.vn', N'Hà Nội',         1000),
(3, N'Sơn Tùng MTP',         '0933555222', '034094001111', 'mtp@gmail.com',          N'Thái Bình',      50),
(4, N'Nguyễn Thúc Thùy Tiên','0988777666', '079098001234', 'tienguyen@gmail.com',     N'Quận 1, TP.HCM', 320),
(5, N'Trần Thành',           '0966555444', '079087005678', 'tranthanh@gmail.com',     N'Quận 3, TP.HCM', 450),
(6, N'Độ Mixi',              '0909999888', '001089001234', 'domixi@mixigaming.com',   N'Thanh Xuân, HN', 980),
(7, N'PewPew',               '0911222333', '031091005678', 'pewpew@pewpew.vn',       N'Bình Thạnh, HCM',210),
(8, N'ViruSs',               '0944555666', '001088009999', 'viruss@vrstudio.vn',     N'Tây Hồ, Hà Nội', 180),
(9, N'Ngô Kiến Huy',         '0977888999', '079085002222', 'huyngo@gmail.com',       N'Quận 10, TP.HCM',140),
(10,N'Midu',                 '0988222111', '079089004444', 'midu@gmail.com',          N'Quận 2, TP.HCM', 550);
SET IDENTITY_INSERT KHACHHANG OFF;
 
-- ========================
-- 5. GAME
-- ========================
SET IDENTITY_INSERT GAME ON;
INSERT INTO GAME (MaGame, TenGame, TheLoai, NenTang, GhiChu, HinhAnh) VALUES
(1, N'GTA VI',           N'Hành động', N'PS5/Xbox', N'Bom tấn 2026',                '/icons/gta6.png'),
(2, N'Skyrim',           N'Nhập vai',  N'PC',       N'Huyền thoại nhập vai',        '/icons/skyrim.png'),
(3, N'PUBG',             N'Bắn súng',   N'PC/PS5',   N'Sinh tồn đỉnh cao',          '/icons/pubg.png'),
(4, N'Minecraft',        N'Sinh tồn',  N'PC/PS5',   N'Thế giới khối vuông',         '/icons/mc.png'),
(5, N'Hollow Knight',    N'Souls-like',N'PC/PS5',   N'Metroidvania đỉnh cao',       '/icons/hollow.png'),
(6, N'Destiny 2',        N'Bắn súng',  N'PC/PS5',   N'Hành động MMO viễn tưởng',    '/icons/destiny.png'),
(7, N'Devil May Cry 5',  N'Hành động', N'PC/PS5',   N'Chặt chém cực đỉnh',          '/icons/devil.png'),
(8, N'Left 4 Dead 2',    N'Bắn súng',  N'PC',       N'Bắn zombie đồng đội',         '/icons/l4d2.png'),
(9, N'League of Legends',N'MOBA',      N'PC',       N'Đấu trường trực tuyến',       '/icons/lol.png'),
(10, N'Once Human',      N'Sinh tồn',  N'PC',       N'Sinh tồn kinh dị hậu tận thế','/icons/once.png'),
(11, N'Roblox',          N'Sandbox',   N'PC/Mobile',N'Vũ trụ ảo sáng tạo',          '/icons/roblox.png');
SET IDENTITY_INSERT GAME OFF;

-- ========================
-- 5.1 GAME_CHITIET
-- ========================
INSERT INTO GAME_CHITIET (MaGame, MoTa, Rating, Genre, DeliveryMethod, ReleaseDate, Region, Features, Language, Currency) VALUES
(1, N'Trò chơi thế giới mở rộng lớn nhất lịch sử', N'18+', N'Action/Open World', N'CD/Digital', '2026-01-01', N'Global', N'Multiplayer',   N'English/Vietnamese', N'VND'),
(2, N'Thế giới mở nhập vai huyền thoại của Bethesda', N'18+', N'RPG', N'CD/Digital', '2011-11-11', N'Global', N'Single Player', N'English', N'VND'),
(3, N'Game bắn súng sinh tồn Battle Royale hàng đầu thế giới', N'16+', N'Battle Royale', N'Digital', '2017-12-21', N'Global', N'Multiplayer', N'English/Vietnamese', N'VND'),
(4, N'Tự do sáng tạo và sinh tồn trong thế giới những khối vuông kỳ diệu', N'E', N'Sandbox', N'Digital', '2011-11-18', N'Global', N'Single/Multiplayer', N'English/Vietnamese', N'VND'),
(5, N'Hành trình phiêu lưu khám phá vương quốc côn trùng Hallownest đầy bí ẩn', N'12+', N'Metroidvania', N'CD/Digital', '2017-02-24', N'Global', N'Single Player', N'English', N'VND'),
(6, N'Khám phá những bí ẩn của hệ mặt trời và trải nghiệm chiến đấu góc nhìn thứ nhất đặc sắc', N'16+', N'Looter Shooter', N'Digital', '2017-09-06', N'Global', N'Co-op/PvP', N'English', N'VND'),
(7, N'Trận chiến chặt chém mãn nhãn của thợ săn quỷ Dante và Nero', N'18+', N'Hack and Slash', N'CD/Digital', '2019-03-08', N'Global', N'Single Player', N'English/Japanese', N'VND'),
(8, N'Cùng đồng đội chiến đấu sinh tồn qua các chiến dịch chống lại đại dịch xác sống zombie', N'18+', N'Co-op Shooter', N'Digital', '2009-11-17', N'Global', N'Multiplayer', N'English', N'VND'),
(9, N'Trò chơi đấu trường trực tuyến 5v5 phổ biến nhất thế giới', N'12+', N'MOBA', N'Digital', '2009-10-27', N'Global', N'Multiplayer', N'Vietnamese/English', N'VND'),
(10, N'Sinh tồn thế giới mở hậu tận thế nơi người chơi chiến đấu chống lại quái vật Stardust', N'16+', N'Survival RPG', N'Digital', '2024-07-09', N'Global', N'Multiplayer', N'English', N'VND'),
(11, N'Nền tảng vũ trụ ảo cho phép người chơi sáng tạo game và kết nối bạn bè toàn cầu', N'E', N'Sandbox', N'Digital', '2006-09-01', N'Global', N'Multiplayer', N'English/Vietnamese', N'VND');
 
-- ========================
-- 6. SANPHAM
-- ========================
SET IDENTITY_INSERT SANPHAM ON;
INSERT INTO SANPHAM (MaSP, MaGame, GiaBan, GiaThueNgay) VALUES
(1, 1, 1800000, 80000),
(2, 2,  800000, 30000),
(3, 3, 1450000, 50000),
(4, 4, 1200000, 40000),
(5, 5, 1600000, 60000),
(6, 6,  700000, 20000),
(7, 7,  900000, 25000),
(8, 8,  300000, 10000),
(9, 9,   50000,  5000),
(10,10, 450000, 15000),
(11,11, 150000,  5000);
SET IDENTITY_INSERT SANPHAM OFF;
 
-- ========================
-- 7. CD
-- ========================
SET IDENTITY_INSERT CD ON;
INSERT INTO CD (MaCD, MaSP, TinhTrang, TrangThai) VALUES
(1, 1, N'Mới',       N'SanSang'),   -- GTA VI CD1
(2, 1, N'99%',       N'DangThue'),  -- GTA VI CD2
(3, 1, N'Mới',       N'SanSang'),   -- GTA VI CD3
(4, 2, N'Mới',       N'SanSang'),   -- Skyrim CD1
(5, 2, N'Mới',       N'DaBan'),     -- Skyrim CD2
(6, 2, N'95%',       N'DangThue'),  -- Skyrim CD3
(7, 3, N'Trầy nhẹ',  N'SanSang'),   -- PUBG CD1
(8, 3, N'Mới',       N'SanSang'),   -- PUBG CD2
(9, 3, N'Cũ',        N'Hong'),      -- PUBG CD3
(10,4, N'Mới',       N'SanSang'),   -- Minecraft CD1
(11,4, N'99%',       N'SanSang'),   -- Minecraft CD2
(12,5, N'Mới',       N'SanSang'),   -- Hollow Knight CD1
(13,5, N'Mới',       N'DangThue'),  -- Hollow Knight CD2
(14,6, N'Mới',       N'SanSang'),   -- Destiny 2 CD1
(15,6, N'90%',       N'SanSang'),   -- Destiny 2 CD2
(16,7, N'Mới',       N'SanSang'),   -- Devil May Cry 5 CD1
(17,7, N'Mới',       N'DangThue'),  -- Devil May Cry 5 CD2
(18,8, N'Trầy nhẹ',  N'SanSang'),   -- Left 4 Dead 2 CD1
(19,9, N'Mới',       N'SanSang'),   -- League of Legends CD1
(20,10,N'Mới',       N'SanSang'),   -- Once Human CD1
(21,11,N'99%',       N'SanSang');   -- Roblox CD1
SET IDENTITY_INSERT CD OFF;
 
-- ========================
-- 8. ROM
-- ========================
INSERT INTO ROM (MaSP, DungLuong, LinkLuuTru, SoLuotBan) VALUES
(2, N'15GB', 'drive.google.com/skyrim', 120),
(4, N'1GB',  'fshare.vn/minecraft',    450),
(6, N'120GB','drive.google.com/destiny2', 320),
(7, N'35GB', 'drive.google.com/dmc5', 180),
(8, N'15GB', 'fshare.vn/l4d2', 640),
(9, N'20GB', 'riotgames.com/lol', 1500),
(10,N'50GB', 'drive.google.com/oncehuman', 280),
(11,N'200MB','roblox.com/download', 5000);
 
-- ========================
-- 9. HOADON
-- ========================
SET IDENTITY_INSERT HOADON ON;
INSERT INTO HOADON (MaHD, MaKH, MaNV, NgayLap, TongTien, DiemSuDung, TienGiam, TrangThai) VALUES
(1, 1,  2, '2026-04-01 10:00:00',  800000,  10, 10000, N'DaThanhToan'), -- Lý Tiểu Long
(2, 2,  2, '2026-04-05 14:30:00', 1200000,   0,     0, N'DaThanhToan'), -- Phạm Nhật Vượng
(3, 6,  2, '2026-04-15 16:20:00',  900000,   0,     0, N'DaThanhToan'), -- Độ Mixi
(4, 7,  5, '2026-04-20 11:15:00',  300000,   0,     0, N'DaThanhToan'), -- PewPew
(5, 4,  2, '2026-04-28 09:45:00', 1100000,  20, 20000, N'DaThanhToan'), -- Thùy Tiên
(6, 10, 5, '2026-05-02 15:30:00',  700000,   0,     0, N'DaThanhToan'), -- Midu
(7, 5,  2, '2026-05-10 13:00:00', 1450000,   0,     0, N'DaThanhToan'), -- Trần Thành
(8, 8,  5, '2026-05-15 18:25:00',  450000,   0,     0, N'DaThanhToan'), -- ViruSs
(9, 9,  2, '2026-05-18 10:10:00',  900000,  10, 10000, N'DaThanhToan'), -- Ngô Kiến Huy
(10,6,  5, '2026-05-22 17:40:00', 1800000,   0,     0, N'DaThanhToan'); -- Độ Mixi
SET IDENTITY_INSERT HOADON OFF;
 
-- ========================
-- 10. CTHOADON
-- ========================
INSERT INTO CTHOADON (MaHD, MaSP, SoLuong, DonGia) VALUES
(1,  2, 1,  800000),  -- HD1: Mua Skyrim ROM (800k)
(2,  4, 1, 1200000),  -- HD2: Mua Minecraft ROM (1200k)
(3,  7, 1,  900000),  -- HD3: Mua DMC5 ROM (900k)
(4,  8, 1,  300000),  -- HD4: Mua L4D2 ROM (300k)
(5,  2, 1,  800000),  -- HD5: Mua Skyrim CD (800k)
(5,  8, 1,  300000),  -- HD5: Mua L4D2 CD (300k)
(6,  6, 1,  700000),  -- HD6: Mua Destiny 2 ROM (700k)
(7,  3, 1, 1450000),  -- HD7: Mua PUBG CD (1450k)
(8, 10, 1,  450000),  -- HD8: Mua Once Human ROM (450k)
(9,  7, 1,  900000),  -- HD9: Mua DMC5 ROM (900k)
(10, 1, 1, 1800000);  -- HD10: Mua GTA VI CD (1800k)
 
-- ========================
-- 11. PHIEUTHUE
-- ========================
SET IDENTITY_INSERT PHIEUTHUE ON;
INSERT INTO PHIEUTHUE (MaPT, MaKH, NgayThue, NgayTraDuKien, NgayTraThucTe, TienCoc, TienPhat, TrangThai) VALUES
(1, 1, '2026-04-10 08:00:00', '2026-04-15 08:00:00', '2026-04-15 08:00:00', 200000,      0, N'DaTra'),     -- Lý Tiểu Long
(2, 3, '2026-04-20 09:00:00', '2026-04-25 09:00:00', NULL,                  300000,      0, N'DangThue'),  -- Sơn Tùng
(3, 6, '2026-05-01 11:30:00', '2026-05-08 11:30:00', NULL,                  150000,      0, N'DangThue'),  -- Độ Mixi
(4, 10,'2026-05-05 14:00:00', '2026-05-10 14:00:00', NULL,                  250000,      0, N'DangThue'),  -- Midu
(5, 7, '2026-05-12 10:00:00', '2026-05-17 10:00:00', NULL,                  200000,      0, N'DangThue'),  -- PewPew
(6, 5, '2026-05-01 09:00:00', '2026-05-05 09:00:00', '2026-05-08 16:30:00', 250000, 180000, N'DaTra');     -- Trần Thành (Trả trễ 3 ngày)
SET IDENTITY_INSERT PHIEUTHUE OFF;
 
-- ========================
-- 12. CTPHIEUTHUE
-- ========================
INSERT INTO CTPHIEUTHUE (MaPT, MaCD, MaNV, DonGiaThue) VALUES
(1, 1,  2, 80000),  -- PT1: Thuê GTA VI CD1
(2, 2,  2, 80000),  -- PT2: Thuê GTA VI CD2
(3, 6,  5, 30000),  -- PT3: Thuê Skyrim CD3
(4, 13, 5, 60000),  -- PT4: Thuê Hollow Knight CD2
(5, 17, 2, 25000),  -- PT5: Thuê DMC5 CD2
(6, 12, 2, 60000);  -- PT6: Thuê Hollow Knight CD1
 
-- Đồng bộ lại trạng thái CD tương ứng
UPDATE CD SET TrangThai = N'SanSang' WHERE MaCD IN (1, 12);
UPDATE CD SET TrangThai = N'DangThue' WHERE MaCD IN (2, 6, 13, 17);
 
-- ========================
-- 13. DIEM_LICHSU
-- ========================
SET IDENTITY_INSERT DIEM_LICHSU ON;
INSERT INTO DIEM_LICHSU (MaLS, MaKH, MaPT, Loai, SoDiem, Ngay, GhiChu) VALUES
(1,  1, NULL, N'CONG', 8,   '2026-04-01 10:00:00', N'Tích lũy mua hàng HĐ1'),
(2,  1, NULL, N'TRU',  10,  '2026-04-01 10:00:00', N'Đổi điểm giảm giá HĐ1'),
(3,  2, NULL, N'CONG', 12,  '2026-04-05 14:30:00', N'Tích lũy mua hàng HĐ2'),
(4,  6, NULL, N'CONG', 9,   '2026-04-15 16:20:00', N'Tích lũy mua hàng HĐ3'),
(5,  7, NULL, N'CONG', 3,   '2026-04-20 11:15:00', N'Tích lũy mua hàng HĐ4'),
(6,  4, NULL, N'CONG', 11,  '2026-04-28 09:45:00', N'Tích lũy mua hàng HĐ5'),
(7,  4, NULL, N'TRU',  20,  '2026-04-28 09:45:00', N'Đổi điểm giảm giá HĐ5'),
(8,  10,NULL, N'CONG', 7,   '2026-05-02 15:30:00', N'Tích lũy mua hàng HĐ6'),
(9,  5, NULL, N'CONG', 14,  '2026-05-10 13:00:00', N'Tích lũy mua hàng HĐ7'),
(10, 8, NULL, N'CONG', 4,   '2026-05-15 18:25:00', N'Tích lũy mua hàng HĐ8'),
(11, 9, NULL, N'CONG', 9,   '2026-05-18 10:10:00', N'Tích lũy mua hàng HĐ9'),
(12, 9, NULL, N'TRU',  10,  '2026-05-18 10:10:00', N'Đổi điểm giảm giá HĐ9'),
(13, 6, NULL, N'CONG', 18,  '2026-05-22 17:40:00', N'Tích lũy mua hàng HĐ10'),
(14, 1, 1,    N'CONG', 4,   '2026-04-15 08:00:00', N'Tích lũy thuê game PT1'),
(15, 5, 6,    N'CONG', 3,   '2026-05-08 16:30:00', N'Tích lũy thuê game PT6');
SET IDENTITY_INSERT DIEM_LICHSU OFF;
 
-- Bật lại FK
EXEC sp_MSforeachtable 'ALTER TABLE ? CHECK CONSTRAINT ALL';
GO
 
PRINT N'Tạo database và nạp dữ liệu mẫu thành công!';
GO