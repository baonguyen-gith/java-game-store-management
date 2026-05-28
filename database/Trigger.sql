-- ============================================================
-- TRIGGER
-- ============================================================

-- TRIGGER 1: Tinh tien phat khi tra CD tre han (10.000/ngay)
CREATE OR ALTER TRIGGER TRG_TINH_TIEN_PHAT
ON PHIEUTHUE AFTER UPDATE
AS
BEGIN
    IF UPDATE(NgayTraThucTe)
    BEGIN
        SET NOCOUNT ON;

        UPDATE p
        SET p.TienPhat = CASE
            WHEN i.NgayTraThucTe > i.NgayTraDuKien THEN
                -- Tối thiểu 1 ngày (giống: if (days <= 0) days = 1)
                CASE
                    WHEN DATEDIFF(DAY, i.NgayTraDuKien, i.NgayTraThucTe) <= 0 THEN 1
                    ELSE DATEDIFF(DAY, i.NgayTraDuKien, i.NgayTraThucTe)
                END
                *
                -- SUM(GiaThueNgay) từ SANPHAM, không phải DonGiaThue
                (
                    SELECT ISNULL(SUM(sp.GiaThueNgay), 0)
                    FROM   CTPHIEUTHUE ct
                    JOIN   CD      cd ON cd.MaCD = ct.MaCD
                    JOIN   SANPHAM sp ON sp.MaSP = cd.MaSP
                    WHERE  ct.MaPT = i.MaPT
                )
                * 1.5
            ELSE 0
        END
        FROM PHIEUTHUE p
        INNER JOIN inserted i ON p.MaPT = i.MaPT
        WHERE i.NgayTraThucTe IS NOT NULL;
    END
END;
GO

-- TRIGGER 2: Chuyen PhieuThue sang 'DaTra' khi co NgayTraThucTe
CREATE OR ALTER TRIGGER TRG_CAP_NHAT_TRANGTHAI_PHIEUTHUE
ON PHIEUTHUE AFTER UPDATE
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

-- TRIGGER 3: CD ve 'SanSang' khi PhieuThue chuyen 'DaTra'
CREATE OR ALTER TRIGGER TRG_CAP_NHAT_TRANGTHAI_CD_KHI_TRA
ON PHIEUTHUE AFTER UPDATE
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

-- TRIGGER 4: Tang SoLuotBan ROM khi HoaDon chuyen 'DaThanhToan'
CREATE OR ALTER TRIGGER TRG_TANG_SOLOIOTBAN_ROM
ON HOADON AFTER UPDATE
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

-- TRIGGER 5: Tinh lai TongTien khi CTHOADON thay doi
-- TongTien = SUM(SoLuong * DonGia) - TienGiam
CREATE OR ALTER TRIGGER TRG_CAP_NHAT_TONGTIEN_HOADON
ON CTHOADON AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @AffectedHD TABLE (MaHD INT);
    INSERT INTO @AffectedHD
    SELECT DISTINCT MaHD FROM inserted
    UNION
    SELECT DISTINCT MaHD FROM deleted;

    UPDATE h
    SET h.TongTien = ISNULL(
        (SELECT SUM(ct.SoLuong * ct.DonGia) FROM CTHOADON ct WHERE ct.MaHD = h.MaHD), 0
    ) - h.TienGiam
    FROM HOADON h
    INNER JOIN @AffectedHD a ON h.MaHD = a.MaHD;
END;
GO

PRINT N'Tao bang va trigger thanh cong.';
GO