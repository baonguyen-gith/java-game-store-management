-- 1. TABLE: ROLE
INSERT INTO ROLE (TenRole) VALUES ('Administrator');
INSERT INTO ROLE (TenRole) VALUES ('Manager');
INSERT INTO ROLE (TenRole) VALUES ('Staff');

-- 2. TABLE: NHANVIEN
INSERT INTO NHANVIEN (HoTen, SDT, NgaySinh, CCCD, NgayVaoLam) 
VALUES ('Nguyễn Văn A', '0912345678', TO_DATE('1995-05-15', 'YYYY-MM-DD'), '001095001234', TO_DATE('2025-01-01', 'YYYY-MM-DD'));
INSERT INTO NHANVIEN (HoTen, SDT, NgaySinh, CCCD, NgayVaoLam) 
VALUES ('Trần Thị B', '0987654321', TO_DATE('1998-09-20', 'YYYY-MM-DD'), '002098005678', TO_DATE('2025-06-15', 'YYYY-MM-DD'));
INSERT INTO NHANVIEN (HoTen, SDT, NgaySinh, CCCD, NgayVaoLam) 
VALUES ('Lê Hoàng C', '0933445566', TO_DATE('2000-02-10', 'YYYY-MM-DD'), '003000009999', TO_DATE('2026-02-01', 'YYYY-MM-DD'));

-- 3. TABLE: USERS
INSERT INTO USERS (Username, Password, MaRole, MaNV) VALUES ('admin', 'hashed_password_123', 1, 1);
INSERT INTO USERS (Username, Password, MaRole, MaNV) VALUES ('manager_thuy', 'hashed_password_456', 2, 2);
INSERT INTO USERS (Username, Password, MaRole, MaNV) VALUES ('staff_dung', 'hashed_password_789', 3, 3);

-- 4. TABLE: KHACHHANG
INSERT INTO KHACHHANG (HoTen, SDT, Email, DiaChi, DiemTichLuy) VALUES ('Lý Tiểu Long', '0909111222', 'long.ly@gmail.com', 'Quận 5, TP.HCM', 100);
INSERT INTO KHACHHANG (HoTen, SDT, Email, DiaChi, DiemTichLuy) VALUES ('Bùi Xuân Huấn', '0919333444', 'huan.rose@gmail.com', 'Sơn La', 50);
INSERT INTO KHACHHANG (HoTen, SDT, Email, DiaChi, DiemTichLuy) VALUES ('Nguyễn Thanh Tùng', '0988555666', 'sky@mtp.vn', 'Thái Bình', 120);
INSERT INTO KHACHHANG (HoTen, SDT, Email, DiaChi, DiemTichLuy) VALUES ('Trần Duy Sang', '0977222888', 'sang.tra@gmail.com', 'Cần Thơ', 0);
INSERT INTO KHACHHANG (HoTen, SDT, Email, DiaChi, DiemTichLuy) VALUES ('Lê Thẩm Dương', '0933999111', 'duong.le@edu.vn', 'Hải Phòng', 200);
INSERT INTO KHACHHANG (HoTen, SDT, Email, DiaChi, DiemTichLuy) VALUES ('Phan Mạnh Quỳnh', '0944000222', 'quynh.phan@music.com', 'Nghệ An', 10);
INSERT INTO KHACHHANG (HoTen, SDT, Email, DiaChi, DiemTichLuy) VALUES ('Đặng Lê Nguyên Vũ', '0966333444', 'qua@trungnguyen.com', 'Buôn Ma Thuột', 500);
INSERT INTO KHACHHANG (HoTen, SDT, Email, DiaChi, DiemTichLuy) VALUES ('Nguyễn Phương Hằng', '0922444555', 'hang.nguyen@dai-nam.vn', 'Bình Dương', 350);
INSERT INTO KHACHHANG (HoTen, SDT, Email, DiaChi, DiemTichLuy) VALUES ('Phạm Nhật Vượng', '0901000999', 'vuong.pham@vinhomes.vn', 'Hà Nội', 1000);
INSERT INTO KHACHHANG (HoTen, SDT, Email, DiaChi, DiemTichLuy) VALUES ('Trịnh Văn Quyết', '0911222333', 'quyet.trinh@flc.vn', 'Vĩnh Phúc', 0);
INSERT INTO KHACHHANG (HoTen, SDT, Email, DiaChi, DiemTichLuy) VALUES ('Nguyễn Hà Đông', '0988777666', 'dong.nguyen@flappy.com', 'Hà Nội', 80);
INSERT INTO KHACHHANG (HoTen, SDT, Email, DiaChi, DiemTichLuy) VALUES ('Võ Hoàng Yến', '0977444111', 'yen.vo@model.com', 'TP.HCM', 15);
INSERT INTO KHACHHANG (HoTen, SDT, Email, DiaChi, DiemTichLuy) VALUES ('Sơn Tùng MTP', '0933555222', 'mtp@gmail.com', 'Thái Bình', 90);
INSERT INTO KHACHHANG (HoTen, SDT, Email, DiaChi, DiemTichLuy) VALUES ('Đen Vâu', '0944666333', 'den.vau@gmail.com', 'Quảng Ninh', 45);
INSERT INTO KHACHHANG (HoTen, SDT, Email, DiaChi, DiemTichLuy) VALUES ('Binz', '0900777888', 'binz.da-poet@gmail.com', 'Gia Lai', 60);
INSERT INTO KHACHHANG (HoTen, SDT, Email, DiaChi, DiemTichLuy) VALUES ('Phan Sào Nam', '0901111000', 'nam.phan@gmail.com', 'Phú Thọ', 0);
INSERT INTO KHACHHANG (HoTen, SDT, Email, DiaChi, DiemTichLuy) VALUES ('Nguyễn Đức Chung', '0902222000', 'chung.nguyen@hanoi.gov', 'Hà Nội', 0);
INSERT INTO KHACHHANG (HoTen, SDT, Email, DiaChi, DiemTichLuy) VALUES ('Trương Mỹ Lan', '0903333000', 'lan.truong@vanthinhphat.vn', 'TP.HCM', 0);
INSERT INTO KHACHHANG (HoTen, SDT, Email, DiaChi, DiemTichLuy) VALUES ('Trần Quí Thanh', '0904444000', 'thanh.tran@thanhhiep.com', 'Bình Dương', 0);
INSERT INTO KHACHHANG (HoTen, SDT, Email, DiaChi, DiemTichLuy) VALUES ('Nguyễn Cao Trí', '0905555000', 'tri.nguyen@capella.vn', 'TP.HCM', 0);
INSERT INTO KHACHHANG (HoTen, SDT, Email, DiaChi, DiemTichLuy) VALUES ('Đỗ Anh Dũng', '0906666000', 'dung.do@tanhoangminh.com', 'Hà Nội', 0);
INSERT INTO KHACHHANG (HoTen, SDT, Email, DiaChi, DiemTichLuy) VALUES ('Nguyễn Bắc Son', '0907777000', 'son.nguyen@mobifone.vn', 'Hà Nội', 0);
INSERT INTO KHACHHANG (HoTen, SDT, Email, DiaChi, DiemTichLuy) VALUES ('Trương Minh Tuấn', '0908888000', 'tuan.truong@vn.com', 'Đà Nẵng', 0);
INSERT INTO KHACHHANG (HoTen, SDT, Email, DiaChi, DiemTichLuy) VALUES ('Đinh La Thăng', '0909999000', 'thang.dinh@petro.vn', 'TP.HCM', 0);
INSERT INTO KHACHHANG (HoTen, SDT, Email, DiaChi, DiemTichLuy) VALUES ('Vũ Nhôm', '0900000111', 'nhom.vu@danang.com', 'Đà Nẵng', 0);

-- 5. TABLE: GAME
INSERT INTO GAME (TenGame, TheLoai, NenTang, GhiChu, HinhAnh) VALUES ('GTA VI', 'Open World', 'PS5/Xbox', 'Bom tan mong cho nhat', 'gtavi.jpg');
INSERT INTO GAME (TenGame, TheLoai, NenTang, GhiChu, HinhAnh) VALUES ('Cyberpunk 2077: Phantom Liberty', 'Sci-fi RPG', 'PC', 'Ban mo rong dinh cao', 'cyberpunk.jpg');
INSERT INTO GAME (TenGame, TheLoai, NenTang, GhiChu, HinhAnh) VALUES ('Resident Evil 9', 'Horror', 'PC/PS5', 'Kinh di song con', 're9.jpg');
INSERT INTO GAME (TenGame, TheLoai, NenTang, GhiChu, HinhAnh) VALUES ('Hades II', 'Roguelike', 'PC', 'Hanh dong nhip do nhanh', 'hades2.jpg');
INSERT INTO GAME (TenGame, TheLoai, NenTang, GhiChu, HinhAnh) VALUES ('Marvel Spider-Man 3', 'Action', 'PS5', 'Sieu anh hung doc quyen', 'spiderman3.jpg');
INSERT INTO GAME (TenGame, TheLoai, NenTang, GhiChu, HinhAnh) VALUES ('Genshin Impact Offline', 'Adventure', 'PC', 'The gioi mo phong cach anime', 'genshin.jpg');
INSERT INTO GAME (TenGame, TheLoai, NenTang, GhiChu, HinhAnh) VALUES ('Tekken 8', 'Fighting', 'PS5', 'Doi khang do hoa dinh cao', 'tekken8.jpg');
INSERT INTO GAME (TenGame, TheLoai, NenTang, GhiChu, HinhAnh) VALUES ('Final Fantasy VII Rebirth', 'RPG', 'PS5', 'Phan tiep theo hanh trinh Remake', 'ff7rebirth.jpg');
INSERT INTO GAME (TenGame, TheLoai, NenTang, GhiChu, HinhAnh) VALUES ('Valorant Story Mode', 'FPS', 'PC', 'Che do cot truyen ban sung', 'valorant.jpg');
INSERT INTO GAME (TenGame, TheLoai, NenTang, GhiChu, HinhAnh) VALUES ('Stardew Valley 2', 'Simulation', 'Nintendo Switch', 'Nong trai phien ban moi', 'stardew2.jpg');

-- 6. TABLE: GAME_CHITIET
INSERT INTO GAME_CHITIET (MaGame, MoTa, Rating, Genre, DeliveryMethod, ReleaseDate, Region, Features, Language, Currency)
VALUES (1, 'Kham pha tieu bang Leonida kieu moi', 'M (17+)', 'Action-Adventure', 'Physical Disc/Digital', TO_DATE('2025-11-01', 'YYYY-MM-DD'), 'Global', 'Single-player, Multiplayer', 'English, Vietnamese', 'VND');
INSERT INTO GAME_CHITIET (MaGame, MoTa, Rating, Genre, DeliveryMethod, ReleaseDate, Region, Features, Language, Currency)
VALUES (2, 'Phan mo rong cot truyen tai Dogtown', 'M (17+)', 'RPG', 'Digital Download', TO_DATE('2023-09-26', 'YYYY-MM-DD'), 'Global', 'Single-player', 'English, Chinese', 'VND');
INSERT INTO GAME_CHITIET (MaGame, MoTa, Rating, Genre, DeliveryMethod, ReleaseDate, Region, Features, Language, Currency)
VALUES (3, 'Chuong tiep theo cua dong game kinh di', 'M (17+)', 'Survival Horror', 'Physical Disc', TO_DATE('2026-01-15', 'YYYY-MM-DD'), 'Asia', 'Single-player', 'English, Japanese', 'VND');
INSERT INTO GAME_CHITIET (MaGame, MoTa, Rating, Genre, DeliveryMethod, ReleaseDate, Region, Features, Language, Currency)
VALUES (4, 'Chong lai cac vi than vung underworld', 'T (13+)', 'Roguelike', 'Digital Download', TO_DATE('2024-05-01', 'YYYY-MM-DD'), 'Global', 'Single-player', 'English', 'VND');
INSERT INTO GAME_CHITIET (MaGame, MoTa, Rating, Genre, DeliveryMethod, ReleaseDate, Region, Features, Language, Currency)
VALUES (5, 'Hanh trinh moi cua Peter va Miles', 'T (13+)', 'Action', 'Physical Disc', TO_DATE('2026-03-20', 'YYYY-MM-DD'), 'US', 'Single-player', 'English', 'VND');
INSERT INTO GAME_CHITIET (MaGame, MoTa, Rating, Genre, DeliveryMethod, ReleaseDate, Region, Features, Language, Currency)
VALUES (6, 'Genshin phien ban choi offline rieng tu', 'T (13+)', 'Action-RPG', 'Digital Download', TO_DATE('2024-12-01', 'YYYY-MM-DD'), 'Asia', 'Single-player', 'Vietnamese, English', 'VND');
INSERT INTO GAME_CHITIET (MaGame, MoTa, Rating, Genre, DeliveryMethod, ReleaseDate, Region, Features, Language, Currency)
VALUES (7, 'Tran chien dinh menh dong ho Mishima', 'T (13+)', 'Fighting', 'Physical/Digital', TO_DATE('2024-01-26', 'YYYY-MM-DD'), 'Global', 'Multiplayer, Co-op', 'English', 'VND');
INSERT INTO GAME_CHITIET (MaGame, MoTa, Rating, Genre, DeliveryMethod, ReleaseDate, Region, Features, Language, Currency)
VALUES (8, 'Roi khoi Midgar va vao the gioi mo vao', 'T (13+)', 'RPG', 'Physical Disc', TO_DATE('2024-02-29', 'YYYY-MM-DD'), 'Japan', 'Single-player', 'Japanese, English', 'VND');
INSERT INTO GAME_CHITIET (MaGame, MoTa, Rating, Genre, DeliveryMethod, ReleaseDate, Region, Features, Language, Currency)
VALUES (9, 'Che do chien dich tuyen tinh dac dac sac', 'T (13+)', 'FPS', 'Digital Download', TO_DATE('2025-08-12', 'YYYY-MM-DD'), 'Global', 'Single-player', 'English', 'VND');
INSERT INTO GAME_CHITIET (MaGame, MoTa, Rating, Genre, DeliveryMethod, ReleaseDate, Region, Features, Language, Currency)
VALUES (10, 'Xay dung lai nong trai cua ong noi p2', 'E (Everyone)', 'Simulation', 'Digital Download', TO_DATE('2026-04-01', 'YYYY-MM-DD'), 'Global', 'Single-player, Co-op', 'English, Vietnamese', 'VND');

-- 7. TABLE: SANPHAM
INSERT INTO SANPHAM (MaGame, GiaBan, GiaThueNgay) VALUES (1, 1800000, 80000);  
INSERT INTO SANPHAM (MaGame, GiaBan, GiaThueNgay) VALUES (2, 800000, 30000);    
INSERT INTO SANPHAM (MaGame, GiaBan, GiaThueNgay) VALUES (3, 1450000, 55000);  
INSERT INTO SANPHAM (MaGame, GiaBan, GiaThueNgay) VALUES (4, 450000, 15000);   
INSERT INTO SANPHAM (MaGame, GiaBan, GiaThueNgay) VALUES (5, 1600000, 70000);   
INSERT INTO SANPHAM (MaGame, GiaBan, GiaThueNgay) VALUES (6, 200000, NULL);     
INSERT INTO SANPHAM (MaGame, GiaBan, GiaThueNgay) VALUES (7, 1200000, NULL);    
INSERT INTO SANPHAM (MaGame, GiaBan, GiaThueNgay) VALUES (8, 1550000, NULL);  
INSERT INTO SANPHAM (MaGame, GiaBan, GiaThueNgay) VALUES (9, 350000, NULL);    
INSERT INTO SANPHAM (MaGame, GiaBan, GiaThueNgay) VALUES (10, 400000, NULL);   
INSERT INTO SANPHAM (MaGame, GiaBan, GiaThueNgay) VALUES (1, 1800000, 80000);  
INSERT INTO SANPHAM (MaGame, GiaBan, GiaThueNgay) VALUES (2, 800000, 30000);    
INSERT INTO SANPHAM (MaGame, GiaBan, GiaThueNgay) VALUES (3, 1450000, 55000);  
INSERT INTO SANPHAM (MaGame, GiaBan, GiaThueNgay) VALUES (4, 450000, 15000);    
INSERT INTO SANPHAM (MaGame, GiaBan, GiaThueNgay) VALUES (5, 1600000, 70000);   
INSERT INTO SANPHAM (MaGame, GiaBan, GiaThueNgay) VALUES (6, 200000, NULL);     
INSERT INTO SANPHAM (MaGame, GiaBan, GiaThueNgay) VALUES (7, 1200000, NULL);   
INSERT INTO SANPHAM (MaGame, GiaBan, GiaThueNgay) VALUES (8, 1550000, NULL);   
INSERT INTO SANPHAM (MaGame, GiaBan, GiaThueNgay) VALUES (9, 350000, NULL);    
INSERT INTO SANPHAM (MaGame, GiaBan, GiaThueNgay) VALUES (10, 400000, NULL);    

-- 8. TABLE: CD
INSERT INTO CD (MaSP, TinhTrang, TrangThai) VALUES (1, 'Mới', 'SanSang');      
INSERT INTO CD (MaSP, TinhTrang, TrangThai) VALUES (2, '99%', 'SanSang');      
INSERT INTO CD (MaSP, TinhTrang, TrangThai) VALUES (3, 'Mới', 'DangChoThue');   
INSERT INTO CD (MaSP, TinhTrang, TrangThai) VALUES (4, 'Cũ', 'SanSang');         
INSERT INTO CD (MaSP, TinhTrang, TrangThai) VALUES (5, 'Mới', 'SanSang');       
INSERT INTO CD (MaSP, TinhTrang, TrangThai) VALUES (11, 'Like New', 'SanSang'); 
INSERT INTO CD (MaSP, TinhTrang, TrangThai) VALUES (12, 'Mới', 'DangChoThue');   
INSERT INTO CD (MaSP, TinhTrang, TrangThai) VALUES (13, 'Mới', 'SanSang');     
INSERT INTO CD (MaSP, TinhTrang, TrangThai) VALUES (14, 'Cũ', 'SanSang');       
INSERT INTO CD (MaSP, TinhTrang, TrangThai) VALUES (15, 'Trầy nhẹ', 'SanSang');  

-- 8. TABLE: ROM
INSERT INTO ROM (MaSP, DungLuong, LinkLuuTru, SoLuotBan) VALUES (6, '60GB', 'link.com/genshin', 100);
INSERT INTO ROM (MaSP, DungLuong, LinkLuuTru, SoLuotBan) VALUES (7, '40GB', 'link.com/tekken8', 50);
INSERT INTO ROM (MaSP, DungLuong, LinkLuuTru, SoLuotBan) VALUES (8, '80GB', 'link.com/ff7', 80);
INSERT INTO ROM (MaSP, DungLuong, LinkLuuTru, SoLuotBan) VALUES (9, '30GB', 'link.com/valorant', 200);
INSERT INTO ROM (MaSP, DungLuong, LinkLuuTru, SoLuotBan) VALUES (10, '5GB', 'link.com/stardew', 300);
INSERT INTO ROM (MaSP, DungLuong, LinkLuuTru, SoLuotBan) VALUES (16, '60GB', 'mirror.link/genshin', 1500);
INSERT INTO ROM (MaSP, DungLuong, LinkLuuTru, SoLuotBan) VALUES (17, '40GB', 'mirror.link/tekken', 300);
INSERT INTO ROM (MaSP, DungLuong, LinkLuuTru, SoLuotBan) VALUES (18, '85GB', 'mirror.link/ff7', 450);
INSERT INTO ROM (MaSP, DungLuong, LinkLuuTru, SoLuotBan) VALUES (19, '35GB', 'mirror.link/valorant', 120);
INSERT INTO ROM (MaSP, DungLuong, LinkLuuTru, SoLuotBan) VALUES (20, '6GB', 'mirror.link/stardew', 900);

-- 9. TABLE: HOADON
INSERT INTO HOADON (MaKH, MaNV, NgayLap, TongTien, DiemSuDung, TienGiam, TrangThai) VALUES (1, 2, TIMESTAMP '2026-04-01 10:00:00', 1800000, 0, 0, 'DaThanhToan');  
INSERT INTO HOADON (MaKH, MaNV, NgayLap, TongTien, DiemSuDung, TienGiam, TrangThai) VALUES (2, 2, TIMESTAMP '2026-04-02 11:00:00', 800000, 0, 0, 'DaThanhToan');   
INSERT INTO HOADON (MaKH, MaNV, NgayLap, TongTien, DiemSuDung, TienGiam, TrangThai) VALUES (3, 3, TIMESTAMP '2026-04-03 12:00:00', 1450000, 0, 0, 'DaThanhToan'); 
INSERT INTO HOADON (MaKH, MaNV, NgayLap, TongTien, DiemSuDung, TienGiam, TrangThai) VALUES (4, 3, TIMESTAMP '2026-04-04 13:00:00', 450000, 0, 0, 'DaThanhToan');   
INSERT INTO HOADON (MaKH, MaNV, NgayLap, TongTien, DiemSuDung, TienGiam, TrangThai) VALUES (5, 2, TIMESTAMP '2026-04-05 14:00:00', 1600000, 0, 0, 'ChuaThanhToan');
INSERT INTO HOADON (MaKH, MaNV, NgayLap, TongTien, DiemSuDung, TienGiam, TrangThai) VALUES (16, 3, TIMESTAMP '2026-04-16 09:00:00', 350000, 0, 0, 'DaThanhToan'); 
INSERT INTO HOADON (MaKH, MaNV, NgayLap, TongTien, DiemSuDung, TienGiam, TrangThai) VALUES (17, 3, TIMESTAMP '2026-04-16 10:00:00', 400000, 0, 0, 'DaThanhToan'); 
INSERT INTO HOADON (MaKH, MaNV, NgayLap, TongTien, DiemSuDung, TienGiam, TrangThai) VALUES (18, 2, TIMESTAMP '2026-04-17 11:00:00', 1200000, 0, 0, 'DaThanhToan'); 
INSERT INTO HOADON (MaKH, MaNV, NgayLap, TongTien, DiemSuDung, TienGiam, TrangThai) VALUES (19, 2, TIMESTAMP '2026-04-17 14:00:00', 1550000, 0, 0, 'DaThanhToan'); 
INSERT INTO HOADON (MaKH, MaNV, NgayLap, TongTien, DiemSuDung, TienGiam, TrangThai) VALUES (20, 3, TIMESTAMP '2026-04-18 15:30:00', 200000, 0, 0, 'DaThanhToan');  
INSERT INTO HOADON (MaKH, MaNV, NgayLap, TongTien, DiemSuDung, TienGiam, TrangThai) VALUES (21, 2, TIMESTAMP '2026-04-18 16:00:00', 1800000, 0, 0, 'DaThanhToan'); 
INSERT INTO HOADON (MaKH, MaNV, NgayLap, TongTien, DiemSuDung, TienGiam, TrangThai) VALUES (22, 2, TIMESTAMP '2026-04-19 08:00:00', 800000, 0, 0, 'DaThanhToan'); 
INSERT INTO HOADON (MaKH, MaNV, NgayLap, TongTien, DiemSuDung, TienGiam, TrangThai) VALUES (23, 3, TIMESTAMP '2026-04-19 10:00:00', 1450000, 0, 0, 'Huy');         
INSERT INTO HOADON (MaKH, MaNV, NgayLap, TongTien, DiemSuDung, TienGiam, TrangThai) VALUES (24, 3, TIMESTAMP '2026-04-20 12:00:00', 450000, 10, 10000, 'DaThanhToan');
INSERT INTO HOADON (MaKH, MaNV, NgayLap, TongTien, DiemSuDung, TienGiam, TrangThai) VALUES (25, 2, TIMESTAMP '2026-04-20 14:00:00', 1600000, 0, 0, 'DaThanhToan');


-- 10. TABLE: CTHOADON

INSERT INTO CTHOADON (MaHD, MaSP, SoLuong, DonGia) VALUES (1, 1, 1, 1800000);
INSERT INTO CTHOADON (MaHD, MaSP, SoLuong, DonGia) VALUES (2, 2, 1, 800000);
INSERT INTO CTHOADON (MaHD, MaSP, SoLuong, DonGia) VALUES (3, 3, 1, 1450000);
INSERT INTO CTHOADON (MaHD, MaSP, SoLuong, DonGia) VALUES (4, 4, 1, 450000);
INSERT INTO CTHOADON (MaHD, MaSP, SoLuong, DonGia) VALUES (5, 5, 1, 1600000);
INSERT INTO CTHOADON (MaHD, MaSP, SoLuong, DonGia) VALUES (6, 19, 1, 350000);
INSERT INTO CTHOADON (MaHD, MaSP, SoLuong, DonGia) VALUES (7, 20, 1, 400000);
INSERT INTO CTHOADON (MaHD, MaSP, SoLuong, DonGia) VALUES (8, 17, 1, 1200000);
INSERT INTO CTHOADON (MaHD, MaSP, SoLuong, DonGia) VALUES (9, 18, 1, 1550000);
INSERT INTO CTHOADON (MaHD, MaSP, SoLuong, DonGia) VALUES (10, 16, 1, 200000);
INSERT INTO CTHOADON (MaHD, MaSP, SoLuong, DonGia) VALUES (11, 11, 1, 1800000);
INSERT INTO CTHOADON (MaHD, MaSP, SoLuong, DonGia) VALUES (12, 12, 1, 800000);
INSERT INTO CTHOADON (MaHD, MaSP, SoLuong, DonGia) VALUES (13, 13, 1, 1450000);
INSERT INTO CTHOADON (MaHD, MaSP, SoLuong, DonGia) VALUES (14, 14, 1, 450000);
INSERT INTO CTHOADON (MaHD, MaSP, SoLuong, DonGia) VALUES (15, 15, 1, 1600000);

-- 11. TABLE: PHIEUTHUE
INSERT INTO PHIEUTHUE (MaKH, NgayThue, NgayTraDuKien, NgayTraThucTe, TienCoc, TienPhat, TrangThai) 
VALUES (6, TIMESTAMP '2026-04-10 08:00:00', TIMESTAMP '2026-04-15 08:00:00', TIMESTAMP '2026-04-15 08:00:00', 200000, 0, 'DaTra');       
INSERT INTO PHIEUTHUE (MaKH, NgayThue, NgayTraDuKien, NgayTraThucTe, TienCoc, TienPhat, TrangThai) 
VALUES (7, TIMESTAMP '2026-04-10 08:00:00', TIMESTAMP '2026-04-15 08:00:00', TIMESTAMP '2026-04-16 08:00:00', 200000, 20000, 'DaTra');   
INSERT INTO PHIEUTHUE (MaKH, NgayThue, NgayTraDuKien, NgayTraThucTe, TienCoc, TienPhat, TrangThai) 
VALUES (8, TIMESTAMP '2026-04-20 08:00:00', TIMESTAMP '2026-04-25 08:00:00', NULL, 200000, 0, 'DangThue');                                
INSERT INTO PHIEUTHUE (MaKH, NgayThue, NgayTraDuKien, NgayTraThucTe, TienCoc, TienPhat, TrangThai) 
VALUES (11, TIMESTAMP '2026-04-10 08:00:00', TIMESTAMP '2026-04-15 08:00:00', TIMESTAMP '2026-04-15 08:00:00', 200000, 0, 'DaTra');    
INSERT INTO PHIEUTHUE (MaKH, NgayThue, NgayTraDuKien, NgayTraThucTe, TienCoc, TienPhat, TrangThai) 
VALUES (12, TIMESTAMP '2026-04-01 08:00:00', TIMESTAMP '2026-04-05 08:00:00', TIMESTAMP '2026-04-20 08:00:00', 500000, 300000, 'DaTra'); 
INSERT INTO PHIEUTHUE (MaKH, NgayThue, NgayTraDuKien, NgayTraThucTe, TienCoc, TienPhat, TrangThai) 
VALUES (13, TIMESTAMP '2026-04-21 08:00:00', TIMESTAMP '2026-04-26 08:00:00', NULL, 200000, 0, 'DangThue');                              
INSERT INTO PHIEUTHUE (MaKH, NgayThue, NgayTraDuKien, NgayTraThucTe, TienCoc, TienPhat, TrangThai) 
VALUES (14, TIMESTAMP '2026-04-21 09:00:00', TIMESTAMP '2026-04-26 09:00:00', NULL, 200000, 0, 'DangThue');                              
INSERT INTO PHIEUTHUE (MaKH, NgayThue, NgayTraDuKien, NgayTraThucTe, TienCoc, TienPhat, TrangThai) 
VALUES (15, TIMESTAMP '2026-04-21 10:00:00', TIMESTAMP '2026-04-26 10:00:00', NULL, 200000, 0, 'DangThue');                              

-- 12. TABLE: CTPHIEUTHUE
INSERT INTO CTPHIEUTHUE (MaPT, MaCD, MaNV, DonGiaThue) VALUES (1, 1, 2, 80000);
INSERT INTO CTPHIEUTHUE (MaPT, MaCD, MaNV, DonGiaThue) VALUES (2, 2, 2, 30000);
INSERT INTO CTPHIEUTHUE (MaPT, MaCD, MaNV, DonGiaThue) VALUES (3, 3, 3, 55000);
INSERT INTO CTPHIEUTHUE (MaPT, MaCD, MaNV, DonGiaThue) VALUES (4, 6, 3, 80000);
INSERT INTO CTPHIEUTHUE (MaPT, MaCD, MaNV, DonGiaThue) VALUES (5, 7, 2, 30000);
INSERT INTO CTPHIEUTHUE (MaPT, MaCD, MaNV, DonGiaThue) VALUES (6, 8, 3, 55000);
INSERT INTO CTPHIEUTHUE (MaPT, MaCD, MaNV, DonGiaThue) VALUES (7, 9, 2, 15000);
INSERT INTO CTPHIEUTHUE (MaPT, MaCD, MaNV, DonGiaThue) VALUES (8, 10, 3, 70000);

-- 13. TABLE: DIEM_LICHSU
INSERT INTO DIEM_LICHSU (MaKH, Loai, SoDiem, Ngay, GhiChu) 
VALUES (1, 'Cong', 100, TIMESTAMP '2026-04-01 10:05:00', 'Tich diem tu don hang MaHD 1');
INSERT INTO DIEM_LICHSU (MaKH, Loai, SoDiem, Ngay, GhiChu) 
VALUES (5, 'Cong', 200, TIMESTAMP '2026-04-05 14:02:00', 'Tich diem tu don hang MaHD 5');
INSERT INTO DIEM_LICHSU (MaKH, Loai, SoDiem, Ngay, GhiChu) 
VALUES (24, 'Tru', 10, TIMESTAMP '2026-04-20 12:00:00', 'Dung diem tru vao don hang MaHD 14');

