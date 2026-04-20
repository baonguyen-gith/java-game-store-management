-- T?o Database
CREATE DATABASE GameStoreManager;
USE GameStoreManager;
GO

CREATE TABLE ROLE (
    MaRole INT PRIMARY KEY IDENTITY(1,1),
    TenRole NVARCHAR(50) NOT NULL
);

CREATE TABLE [USER] ( 
    Username VARCHAR(50) PRIMARY KEY,
    Password VARCHAR(255) NOT NULL,
    MaRole INT,
    HoTen NVARCHAR(100),
    CONSTRAINT FK_User_Role FOREIGN KEY (MaRole) REFERENCES ROLE(MaRole)
);

CREATE TABLE KHACHHANG (
    MaKH CHAR(10) PRIMARY KEY,
    HoTen NVARCHAR(100) NOT NULL,
    SDT VARCHAR(15),
    Email VARCHAR(100),
    DiaChi NVARCHAR(255)
);

CREATE TABLE GAME (
    MaGame CHAR(10) PRIMARY KEY,
    TenGame NVARCHAR(200) NOT NULL,
    TheLoai NVARCHAR(50),
    NenTang NVARCHAR(50),
    GiaBan DECIMAL(18, 2),
    GiaThueNgay DECIMAL(18, 2)
);

CREATE TABLE SANPHAM (
    MaSP CHAR(15) PRIMARY KEY,
    MaGame CHAR(10),
    GiaBan DECIMAL(18, 2),
    LoaiSP VARCHAR(10) CHECK (LoaiSP IN ('CD', 'ROM')), 
    CONSTRAINT FK_SP_Game FOREIGN KEY (MaGame) REFERENCES GAME(MaGame)
);

CREATE TABLE CD (
    MaSP CHAR(15) PRIMARY KEY,
    TinhTrang NVARCHAR(50),
    CONSTRAINT FK_CD_SP FOREIGN KEY (MaSP) REFERENCES SANPHAM(MaSP)
);

CREATE TABLE ROM (
    MaSP CHAR(15) PRIMARY KEY,
    DungLuong VARCHAR(20),
    LinkLuuTru VARCHAR(255),
    SoLuotBan INT DEFAULT 0,
    CONSTRAINT FK_ROM_SP FOREIGN KEY (MaSP) REFERENCES SANPHAM(MaSP)
);

CREATE TABLE HOADON (
    MaHD CHAR(15) PRIMARY KEY,
    MaKH CHAR(10),
    NgayLap DATETIME DEFAULT GETDATE(),
    TongTien DECIMAL(18, 2) DEFAULT 0,
    CONSTRAINT FK_HD_KH FOREIGN KEY (MaKH) REFERENCES KHACHHANG(MaKH)
);

CREATE TABLE CTHOADON (
    MaHD CHAR(15),
    MaSP CHAR(15),
    SoLuong INT,
    DonGia DECIMAL(18, 2),
    PRIMARY KEY (MaHD, MaSP),
    CONSTRAINT FK_CTHD_HD FOREIGN KEY (MaHD) REFERENCES HOADON(MaHD),
    CONSTRAINT FK_CTHD_SP FOREIGN KEY (MaSP) REFERENCES SANPHAM(MaSP)
);

CREATE TABLE PHIEUTHUE (
    MaPT CHAR(15) PRIMARY KEY,
    MaKH CHAR(10),
    NgayThue DATETIME,
    NgayTraDuKien DATETIME,
    NgayTraThucTe DATETIME,
    TienCoc DECIMAL(18, 2),
    TienPhat DECIMAL(18, 2) DEFAULT 0,
    CONSTRAINT FK_PT_KH FOREIGN KEY (MaKH) REFERENCES KHACHHANG(MaKH)
);

CREATE TABLE CTPHIEUTHUE (
    MaPT CHAR(15),
    MaSP CHAR(15),
    PRIMARY KEY (MaPT, MaSP),
    CONSTRAINT FK_CTPT_PT FOREIGN KEY (MaPT) REFERENCES PHIEUTHUE(MaPT),
    CONSTRAINT FK_CTPT_CD FOREIGN KEY (MaSP) REFERENCES CD(MaSP)
);


CREATE TRIGGER trg_UpdateTongTien
ON CTHOADON
AFTER INSERT
AS
BEGIN
    UPDATE HOADON
    SET TongTien = TongTien + (SELECT SUM(SoLuong * DonGia) FROM inserted WHERE MaHD = HOADON.MaHD)
    FROM HOADON
    INNER JOIN inserted ON HOADON.MaHD = inserted.MaHD;
END;
-- 1. D? LI?U ROLE & USER (Phân quy?n & Tài kho?n)
INSERT INTO ROLE (TenRole) VALUES (N'Qu?n tr? viên'), (N'Nhân viên bán hàng'), (N'Th? kho');

INSERT INTO [USER] (Username, Password, MaRole, HoTen) VALUES 
('admin', 'password_secure_123', 1, N'Nguy?n Hoàng Admin'),
('nv_banhang1', 'sales123', 2, N'Lê Th? Thu'),
('nv_kho', 'kho123', 3, N'Ph?m V?n Kho');

-- 2. D? LI?U KHACHHANG (Khách hàng)
INSERT INTO KHACHHANG (MaKH, HoTen, SDT, Email, DiaChi) VALUES 
('KH001', N'Tr?n Thanh Tâm', '0905123456', 'tam.tt@gmail.com', N'123 Lê L?i, ?à N?ng'),
('KH002', N'Nguy?n Gia B?o', '0914223344', 'baogia99@yahoo.com', N'456 Nguy?n Hu?, Qu?n 1, TP.HCM'),
('KH003', N'Hoàng Thùy Linh', '0988556677', 'linhht@hotmail.com', N'789 C?u Gi?y, Hà N?i'),
('KH004', N'Lý Ti?u Long', '0977112233', 'longly@gmail.com', N'321 Tr?n H?ng ??o, C?n Th?');

-- 3. D? LI?U GAME (Kho game g?c)
INSERT INTO GAME (MaGame, TenGame, TheLoai, NenTang, GiaBan, GiaThueNgay) VALUES 
('G001', N'Elden Ring', 'Action RPG', 'PC/PS5', 1200000, 40000),
('G002', N'Spider-Man 2', 'Action-Adventure', 'PS5', 1600000, 50000),
('G003', N'The Witcher 3', 'RPG', 'PC/Xbox/PS4', 600000, 20000),
('G004', N'FIFA 24 (FC24)', 'Sports', 'Multi-platform', 1400000, 45000),
('G005', N'Resident Evil 4 Remake', 'Horror', 'PC/PS5', 1100000, 35000);

-- 4. D? LI?U SANPHAM (Phân lo?i CD/ROM)
-- Gi? s? m?i game ??u có c? b?n ??a (CD) và b?n s? (ROM)
INSERT INTO SANPHAM (MaSP, MaGame, GiaBan, LoaiSP) VALUES 
('SP_CD_001', 'G001', 1200000, 'CD'),
('SP_ROM_001', 'G001', 1000000, 'ROM'),
('SP_CD_002', 'G002', 1600000, 'CD'),
('SP_ROM_002', 'G002', 1450000, 'ROM'),
('SP_CD_003', 'G003', 600000, 'CD'),
('SP_ROM_003', 'G003', 450000, 'ROM'),
('SP_CD_004', 'G004', 1400000, 'CD'),
('SP_CD_005', 'G005', 1100000, 'CD');

-- 5. CHI TI?T B?NG CD (Tình tr?ng v?t lý)
INSERT INTO CD (MaSP, TinhTrang) VALUES 
('SP_CD_001', N'M?i 99%'),
('SP_CD_002', N'Nguyên Seal'),
('SP_CD_003', N'Tr?y x??c nh?'),
('SP_CD_004', N'S?n sàng'),
('SP_CD_005', N'M?i 100%');

-- 6. CHI TI?T B?NG ROM (Link t?i & L??t t?i)
INSERT INTO ROM (MaSP, DungLuong, LinkLuuTru, SoLuotBan) VALUES 
('SP_ROM_001', '60GB', 'https://game.com/dl/elden-ring', 120),
('SP_ROM_002', '85GB', 'https://game.com/dl/spiderman2', 45),
('SP_ROM_003', '40GB', 'https://game.com/dl/witcher3', 300);

-- 7. D? LI?U HOADON (Giao d?ch mua ??t)
INSERT INTO HOADON (MaHD, MaKH, NgayLap, TongTien) VALUES 
('HD001', 'KH001', '2024-03-01 10:30:00', 0), -- T?ng ti?n s? t? update nh? Trigger
('HD002', 'KH002', '2024-03-05 15:45:00', 0),
('HD003', 'KH003', '2024-03-10 09:00:00', 0);

-- Chi ti?t hóa ??n
INSERT INTO CTHOADON (MaHD, MaSP, SoLuong, DonGia) VALUES 
('HD001', 'SP_ROM_001', 1, 1000000), -- Khách 1 mua ROM Elden Ring
('HD001', 'SP_CD_003', 1, 600000),   -- Khách 1 mua thêm CD Witcher 3
('HD002', 'SP_ROM_002', 1, 1450000), -- Khách 2 mua ROM Spider-Man
('HD003', 'SP_CD_005', 2, 1100000);  -- Khách 3 mua 2 ??a Resident Evil 4

-- 8. D? LI?U PHIEUTHUE (D?ch v? thuê ??a)
-- Phi?u 1: Thuê và ?ã tr? ?úng h?n
INSERT INTO PHIEUTHUE (MaPT, MaKH, NgayThue, NgayTraDuKien, NgayTraThucTe, TienCoc, TienPhat) VALUES 
('PT001', 'KH001', '2024-02-01', '2024-02-05', '2024-02-05', 500000, 0);

-- Phi?u 2: Thuê và tr? tr? h?n (S? kích ho?t trigger tính ti?n ph?t n?u b?n ?ã cài)
INSERT INTO PHIEUTHUE (MaPT, MaKH, NgayThue, NgayTraDuKien, NgayTraThucTe, TienCoc, TienPhat) VALUES 
('PT002', 'KH004', '2024-03-01', '2024-03-05', '2024-03-08', 300000, 45000);

-- Phi?u 3: ?ang thuê (Ch?a tr?)
INSERT INTO PHIEUTHUE (MaPT, MaKH, NgayThue, NgayTraDuKien, NgayTraThucTe, TienCoc, TienPhat) VALUES 
('PT003', 'KH002', '2024-03-15', '2024-03-20', NULL, 400000, 0);

-- Chi ti?t phi?u thuê
INSERT INTO CTPHIEUTHUE (MaPT, MaSP) VALUES 
('PT001', 'SP_CD_001'),
('PT002', 'SP_CD_004'),
('PT003', 'SP_CD_002');