-- ========================
-- TRIGGER TÍNH TIỀN PHẠT
-- Đồng bộ với RentalService.tinhPhatTreHanOnly():
--   phat = soNgayTre * 10.000 (không phải 20.000)
-- Trigger chỉ ghi nhận phạt trễ hạn.
-- Phí hư hỏng CD được cộng thêm từ Java (returnCDFull).
-- ========================
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
            -- Khớp với Java: soNgayTre * 10.000
            THEN DATEDIFF(DAY, i.NgayTraDuKien, i.NgayTraThucTe) * 10000
            ELSE 0
        END
        FROM PHIEUTHUE p
        INNER JOIN inserted i ON p.MaPT = i.MaPT
        WHERE i.NgayTraThucTe IS NOT NULL;
    END
END;
GO