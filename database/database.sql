/* =====================================
    DATABASE: QLGAME (SQL SERVER)
    Đã bỏ SEQUENCE (dùng IDENTITY thay thế)
    Đã đồng bộ với code Java
===================================== */
 
USE master;
GO
IF EXISTS (SELECT name FROM sys.databases WHERE name = N'qlgamee')
    DROP DATABASE qlgamee;
GO
CREATE DATABASE qlgamee;
GO
USE qlgamee;
GO
 
-- ========================
-- 1. ROLE
-- ========================
CREATE TABLE ROLE (
    MaRole INT IDENTITY(1,1) PRIMARY KEY,
    TenRole NVARCHAR(50) NOT NULL
);
 
-- ========================
-- 2. NHANVIEN
-- ========================
CREATE TABLE NHANVIEN (
    MaNV       INT IDENTITY(1,1) PRIMARY KEY,
    HoTen      NVARCHAR(100) NOT NULL,
    SDT        VARCHAR(15),
    NgaySinh   DATE,
    CCCD       VARCHAR(20) UNIQUE,
    NgayVaoLam DATE DEFAULT GETDATE()
);
 
-- ========================
-- 3. USERS
-- ========================
CREATE TABLE USERS (
    MaUser   INT IDENTITY(1,1) PRIMARY KEY,
    Username VARCHAR(50)  UNIQUE NOT NULL,
    Password VARCHAR(255) NOT NULL,
    MaRole   INT,
    MaNV     INT
);
 
-- ========================
-- 4. KHACHHANG
-- ========================
CREATE TABLE KHACHHANG (
    MaKH        INT IDENTITY(1,1) PRIMARY KEY,
    HoTen       NVARCHAR(100),
    SDT         VARCHAR(15),
    CCCD        VARCHAR(20),
    Email       VARCHAR(100),
    DiaChi      NVARCHAR(200),
    DiemTichLuy INT DEFAULT 0
);
-- Filtered unique index cho SDT (NULL không bị unique constraint)
CREATE UNIQUE INDEX UQ_KHACHHANG_SDT
ON KHACHHANG (SDT)
WHERE SDT IS NOT NULL;
 
-- ========================
-- 5. GAME
-- ========================
CREATE TABLE GAME (
    MaGame  INT IDENTITY(1,1) PRIMARY KEY,
    TenGame NVARCHAR(200) NOT NULL,
    TheLoai NVARCHAR(50),
    NenTang NVARCHAR(50),
    GhiChu  NVARCHAR(500),
    HinhAnh VARCHAR(255)
);
 
-- ========================
-- 5.1 GAME_CHITIET
-- ========================
CREATE TABLE GAME_CHITIET (
    MaGame         INT PRIMARY KEY,
    MoTa           NVARCHAR(MAX),
    Rating         NVARCHAR(50),
    Genre          NVARCHAR(100),
    DeliveryMethod NVARCHAR(50),
    ReleaseDate    DATE,
    Region         NVARCHAR(50),
    Features       NVARCHAR(200),
    Language       NVARCHAR(200),
    Currency       NVARCHAR(10)
);
 
-- ========================
-- 6. SANPHAM
-- ========================
CREATE TABLE SANPHAM (
    MaSP        INT IDENTITY(1,1) PRIMARY KEY,
    MaGame      INT,
    GiaBan      DECIMAL(15,2),
    GiaThueNgay DECIMAL(15,2)
);
 
-- ========================
-- 7. CD
-- ========================
CREATE TABLE CD (
    MaCD      INT IDENTITY(1,1) PRIMARY KEY,
    MaSP      INT,
    TinhTrang NVARCHAR(50),
    TrangThai NVARCHAR(20) DEFAULT N'SanSang'
    -- TrangThai: SanSang | DangThue | DaBan | Hong
);
 
-- ========================
-- 8. ROM
-- ========================
CREATE TABLE ROM (
    MaSP       INT PRIMARY KEY,
    DungLuong  NVARCHAR(20),
    LinkLuuTru VARCHAR(500),
    SoLuotBan  INT DEFAULT 0
);
 
-- ========================
-- 9. HOADON
-- ========================
CREATE TABLE HOADON (
    MaHD       INT IDENTITY(1,1) PRIMARY KEY,
    MaKH       INT,               -- NULL = khách vãng lai
    MaNV       INT,
    NgayLap    DATETIME DEFAULT GETDATE(),
    TongTien   DECIMAL(15,2) DEFAULT 0,
    DiemSuDung INT DEFAULT 0,
    TienGiam   DECIMAL(15,2) DEFAULT 0,
    TrangThai  NVARCHAR(20) DEFAULT N'ChuaThanhToan'
    -- TrangThai: ChuaThanhToan | DaThanhToan
);
 
-- ========================
-- 10. CTHOADON
-- ========================
CREATE TABLE CTHOADON (
    MaHD    INT,
    MaSP    INT,
    SoLuong INT,
    DonGia  DECIMAL(15,2),
    PRIMARY KEY (MaHD, MaSP)
);
 
-- ========================
-- 11. PHIEUTHUE
-- ========================
CREATE TABLE PHIEUTHUE (
    MaPT          INT IDENTITY(1,1) PRIMARY KEY,
    MaKH          INT,
    NgayThue      DATETIME DEFAULT GETDATE(),
    NgayTraDuKien DATETIME,
    NgayTraThucTe DATETIME,
    TienCoc       DECIMAL(15,2),
    TienPhat      DECIMAL(15,2) DEFAULT 0,
    TrangThai     NVARCHAR(20) DEFAULT N'DangThue'
    -- TrangThai: DangThue | DaTra
);
 
-- ========================
-- 12. CTPHIEUTHUE
-- ========================
CREATE TABLE CTPHIEUTHUE (
    MaPT      INT,
    MaCD      INT,
    MaNV      INT,
    DonGiaThue DECIMAL(15,2),
    PRIMARY KEY (MaPT, MaCD)
);
 
-- ========================
-- 13. DIEM_LICHSU
-- ========================
CREATE TABLE DIEM_LICHSU (
    MaLS   INT IDENTITY(1,1) PRIMARY KEY,
    MaKH   INT,
    MaPT   INT,            -- NULL nếu từ hóa đơn
    Loai   NVARCHAR(10),   -- 'CONG' hoặc 'TRU'
    SoDiem INT,
    Ngay   DATETIME DEFAULT GETDATE(),
    GhiChu NVARCHAR(200)
);