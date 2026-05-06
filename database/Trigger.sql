--SEQUENCE
CREATE SEQUENCE SEQ_KHACHHANG START WITH 26 INCREMENT BY 1; 
CREATE SEQUENCE SEQ_GAME       START WITH 11 INCREMENT BY 1; 
CREATE SEQUENCE SEQ_SANPHAM    START WITH 21 INCREMENT BY 1;
CREATE SEQUENCE SEQ_HOADON     START WITH 16 INCREMENT BY 1;
CREATE SEQUENCE SEQ_PHIEUTHUE  START WITH 9  INCREMENT BY 1;
CREATE SEQUENCE SEQ_USERS      START WITH 4  INCREMENT BY 1;
CREATE SEQUENCE SEQ_ROLE       START WITH 4  INCREMENT BY 1;

--TRIGGER
-- Tự động tăng cho KHACHHANG
CREATE OR REPLACE TRIGGER TRG_ID_KHACHHANG
BEFORE INSERT ON KHACHHANG FOR EACH ROW
BEGIN
    IF :NEW.MaKH IS NULL THEN
        SELECT SEQ_KHACHHANG.NEXTVAL INTO :NEW.MaKH FROM DUAL;
    END IF;
END;
/

-- Tự động tăng cho GAME
CREATE OR REPLACE TRIGGER TRG_ID_GAME
BEFORE INSERT ON GAME FOR EACH ROW
BEGIN
    IF :NEW.MaGame IS NULL THEN
        SELECT SEQ_GAME.NEXTVAL INTO :NEW.MaGame FROM DUAL;
    END IF;
END;
/

-- Tự động tăng cho HOADON
CREATE OR REPLACE TRIGGER TRG_ID_HOADON
BEFORE INSERT ON HOADON FOR EACH ROW
BEGIN
    IF :NEW.MaHD IS NULL THEN
        SELECT SEQ_HOADON.NEXTVAL INTO :NEW.MaHD FROM DUAL;
    END IF;
END;
/

-- Tự động tăng cho PHIEUTHUE
CREATE OR REPLACE TRIGGER TRG_ID_PHIEUTHUE
BEFORE INSERT ON PHIEUTHUE FOR EACH ROW
BEGIN
    IF :NEW.MaPT IS NULL THEN
        SELECT SEQ_PHIEUTHUE.NEXTVAL INTO :NEW.MaPT FROM DUAL;
    END IF;
END;
/

-- Tự động tăng cho USERS
CREATE OR REPLACE TRIGGER TRG_ID_USERS
BEFORE INSERT ON USERS FOR EACH ROW
BEGIN
    IF :NEW.MaUser IS NULL THEN
        SELECT SEQ_USERS.NEXTVAL INTO :NEW.MaUser FROM DUAL;
    END IF;
END;
/

-- TRIGGER NGHIỆP VỤ (TIỀN PHẠT & DIÊM LỊCH SỬ)
-- Trigger tự động tính tiền phạt dựa trên đơn giá thuê của từng đĩa khi trễ hạn
CREATE OR REPLACE TRIGGER TRG_TINH_TIEN_PHAT
BEFORE UPDATE OF NgayTraThucTe ON PHIEUTHUE
FOR EACH ROW
DECLARE
    v_so_ngay_tre     NUMBER := 0;
    v_tong_gia_thue   NUMBER(15,2) := 0;
BEGIN
    IF :NEW.NgayTraThucTe IS NOT NULL THEN
        IF :NEW.NgayTraThucTe > :OLD.NgayTraDuKien THEN
            -- Tính số ngày trễ (Làm tròn lên)
            v_so_ngay_tre := CEIL(CAST(:NEW.NgayTraThucTe AS DATE) - CAST(:OLD.NgayTraDuKien AS DATE));
            
            -- Lấy tổng tiền thuê/ngày của các đĩa trong phiếu này
            SELECT NVL(SUM(DonGiaThue), 0) INTO v_tong_gia_thue
            FROM CTPHIEUTHUE WHERE MaPT = :OLD.MaPT;
            
            -- Phạt gấp 1.5 lần tiền thuê gốc cho mỗi ngày trễ
            :NEW.TienPhat := v_so_ngay_tre * v_tong_gia_thue * 1.5;
            :NEW.TrangThai := 'QuaHan';
        ELSE
            :NEW.TienPhat := 0;
            :NEW.TrangThai := 'DaTra';
        END IF;
    END IF;
END;
/

-- Trigger tự động tích điểm và ghi vào DIEM_LICHSU khi hóa đơn chuyển sang 'DaThanhToan'
CREATE OR REPLACE TRIGGER TRG_TICH_DIEM_HOADON
AFTER UPDATE OF TrangThai ON HOADON
FOR EACH ROW
DECLARE
    v_diem_cong NUMBER := 0;
BEGIN
    IF :NEW.TrangThai = 'DaThanhToan' AND :OLD.TrangThai = 'ChuaThanhToan' THEN
        -- Cứ 100k tiền hóa đơn được cộng 1 điểm
        v_diem_cong := FLOOR(:NEW.TongTien / 100000);
        
        IF v_diem_cong > 0 THEN
            UPDATE KHACHHANG SET DiemTichLuy = DiemTichLuy + v_diem_cong WHERE MaKH = :NEW.MaKH;
            
            -- Ghi log bảng điểm mới
            INSERT INTO DIEM_LICHSU (MaKH, Loai, SoDiem, GhiChu)
            VALUES (:NEW.MaKH, 'Cong', v_diem_cong, 'Tich diem tu hoa don MaHD: ' || :NEW.MaHD);
        END IF;
        
        -- Nếu khách có dùng điểm để giảm giá
        IF :NEW.DiemSuDung > 0 THEN
            UPDATE KHACHHANG SET DiemTichLuy = DiemTichLuy - :NEW.DiemSuDung WHERE MaKH = :NEW.MaKH;
            
            INSERT INTO DIEM_LICHSU (MaKH, Loai, SoDiem, GhiChu)
            VALUES (:NEW.MaKH, 'Tru', :NEW.DiemSuDung, 'Tieu diem tai hoa don MaHD: ' || :NEW.MaHD);
        END IF;
    END IF;
END;
/
