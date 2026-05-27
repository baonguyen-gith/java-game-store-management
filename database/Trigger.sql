-- ============================================================
-- TRIGGER
-- ============================================================

-- TRIGGER 1: TINH TIEN PHAT KHI TRA CD TRE HAN
-- phat = soNgayTre * 10.000
CREATE OR ALTER TRIGGER TRG_TINH_TIEN_PHAT
ON PHIEUTHUE
AFTER UPDATE
AS
BEGIN
    IF UPDATE(NgayTraThucTe)
    BEGIN
        SET NOCOUNT ON;
        UPDATE p
        SET p.TienPhat = CASE
            WHEN i.NgayTraThucTe > i.NgayTraDuKien
                THEN DATEDIFF(DAY, i.NgayTraDuKien, i.NgayTraThucTe) * 10000
            ELSE 0
        END
        FROM PHIEUTHUE p
        INNER JOIN inserted i ON p.MaPT = i.MaPT
        WHERE i.NgayTraThucTe IS NOT NULL;
    END
END;
GO

-- TRIGGER 2: CAP NHAT TRANG THAI PHIEUTHUE -> 'DaTra'
CREATE OR ALTER TRIGGER TRG_CAP_NHAT_TRANGTHAI_PHIEUTHUE
ON PHIEUTHUE
AFTER UPDATE
AS
BEGIN
    IF UPDATE(NgayTraThucTe)
    BEGIN
        SET NOCOUNT ON;
        UPDATE p
        SET p.TrangThai = N'DaTra'
        FROM PHIEUTHUE p
        INNER JOIN inserted i ON p.MaPT = i.MaPT
        WHERE i.NgayTraThucTe IS NOT NULL
          AND p.TrangThai = N'DangThue';
    END
END;
GO

-- TRIGGER 3: CAP NHAT TRANG THAI CD KHI TRA PHIEU THUE
CREATE OR ALTER TRIGGER TRG_CAP_NHAT_TRANGTHAI_CD_KHI_TRA
ON PHIEUTHUE
AFTER UPDATE
AS
BEGIN
    IF UPDATE(TrangThai)
    BEGIN
        SET NOCOUNT ON;
        UPDATE cd
        SET cd.TrangThai = N'SanSang'
        FROM CD cd
        INNER JOIN CTPHIEUTHUE ct ON cd.MaCD = ct.MaCD
        INNER JOIN inserted    i  ON ct.MaPT  = i.MaPT
        WHERE i.TrangThai = N'DaTra';
    END
END;
GO

-- TRIGGER 4: TANG SOLOIOTBAN ROM KHI CO HOADON THANH TOAN
CREATE OR ALTER TRIGGER TRG_TANG_SOLOIOTBAN_ROM
ON HOADON
AFTER UPDATE
AS
BEGIN
    IF UPDATE(TrangThai)
    BEGIN
        SET NOCOUNT ON;
        UPDATE r
        SET r.SoLuotBan = r.SoLuotBan + ct.SoLuong
        FROM ROM r
        INNER JOIN CTHOADON ct ON r.MaSP  = ct.MaSP
        INNER JOIN inserted  i  ON ct.MaHD = i.MaHD
        WHERE i.TrangThai = N'DaThanhToan';
    END
END;
GO

-- TRIGGER 5: CAP NHAT TONGTIEN HOADON
-- TongTien = SUM(SoLuong * DonGia) - TienGiam
CREATE OR ALTER TRIGGER TRG_CAP_NHAT_TONGTIEN_HOADON
ON CTHOADON
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @AffectedHD TABLE (MaHD INT);
    INSERT INTO @AffectedHD (MaHD)
    SELECT DISTINCT MaHD FROM inserted
    UNION
    SELECT DISTINCT MaHD FROM deleted;

    UPDATE h
    SET h.TongTien = ISNULL(
        (SELECT SUM(ct.SoLuong * ct.DonGia)
         FROM CTHOADON ct
         WHERE ct.MaHD = h.MaHD), 0
    ) - h.TienGiam
    FROM HOADON h
    INNER JOIN @AffectedHD a ON h.MaHD = a.MaHD;
END;
GO

PRINT N'Tao bang va trigger thanh cong.';
GO