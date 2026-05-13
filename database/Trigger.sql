/* 1. TẠO SEQUENCE CHO CÁC BẢNG 
   (SQL Server dùng NEXT VALUE FOR thay vì .NEXTVAL)
*/
CREATE SEQUENCE SEQ_KHACHHANG START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE SEQ_GAME       START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE SEQ_SANPHAM    START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE SEQ_HOADON      START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE SEQ_PHIEUTHUE   START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE SEQ_USERS       START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE SEQ_ROLE        START WITH 1 INCREMENT BY 1;
GO

/* 2. CÁCH GÁN ID TỰ ĐỘNG (THAY THẾ CHO TRIGGER INSERT)
   Trong SQL Server, bạn nên dùng DEFAULT để gán Sequence. 
   Ví dụ khi bạn tạo bảng:
   MaKH INT PRIMARY KEY CONSTRAINT DF_MaKH DEFAULT (NEXT VALUE FOR SEQ_KHACHHANG),
*/

-- Nếu bảng đã tồn tại, bạn có thể chạy lệnh này để gán tự động tăng mà không cần Trigger:
-- ALTER TABLE KHACHHANG ADD CONSTRAINT DF_KH DEFAULT (NEXT VALUE FOR SEQ_KHACHHANG) FOR MaKH;
-- ALTER TABLE GAME ADD CONSTRAINT DF_GAME DEFAULT (NEXT VALUE FOR SEQ_GAME) FOR MaGame;
-- ... tương tự cho các bảng khác.

GO

/* 3. TRIGGER TỰ ĐỘNG TÍNH TIỀN PHẠT
   Lưu ý: SQL Server dùng bảng ảo 'inserted' và 'deleted' thay vì :NEW và :OLD
*/
CREATE OR ALTER TRIGGER TRG_TINH_TIEN_PHAT
ON PHIEUTHUE
AFTER UPDATE
AS
BEGIN
    -- Kiểm tra nếu cột NgayTraThucTe được cập nhật thì mới tính toán
    IF UPDATE(NgayTraThucTe)
    BEGIN
        SET NOCOUNT ON;

        UPDATE PHIEUTHUE
        SET TienPhat = CASE 
            -- Nếu ngày trả thực tế lớn hơn ngày trả dự kiến
            WHEN i.NgayTraThucTe > i.NgayTraDuKien 
            THEN DATEDIFF(DAY, i.NgayTraDuKien, i.NgayTraThucTe) * 20000
            -- Nếu trả đúng hạn hoặc sớm hơn
            ELSE 0 
        END
        FROM PHIEUTHUE p
        INNER JOIN inserted i ON p.MaPT = i.MaPT
        WHERE i.NgayTraThucTe IS NOT NULL;
    END
END;
GO