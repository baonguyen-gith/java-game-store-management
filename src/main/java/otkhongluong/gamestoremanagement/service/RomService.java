package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.dao.RomDAO;
import otkhongluong.gamestoremanagement.model.ROM;

public class RomService {

    private final RomDAO romDAO;

    public RomService() {
        this.romDAO = new RomDAO();
    }

    public ROM getByMaSP(int maSP) {
        if (maSP <= 0) throw new IllegalArgumentException("Mã sản phẩm không hợp lệ");
        return romDAO.findByMaSP(maSP);
    }

    public boolean save(ROM rom) {
        validate(rom);
        return romDAO.existsByMaSP(rom.getMaSP())
                ? romDAO.update(rom)
                : romDAO.insert(rom);
    }

    public boolean delete(int maSP) {
        if (maSP <= 0) throw new IllegalArgumentException("Mã sản phẩm không hợp lệ");
        return romDAO.delete(maSP);
    }

    private void validate(ROM rom) {
        if (rom == null)
            throw new IllegalArgumentException("ROM không được null");
        if (rom.getMaSP() <= 0)
            throw new IllegalArgumentException("Mã sản phẩm không hợp lệ");
        if (rom.getDungLuong() == null || rom.getDungLuong().trim().isEmpty())
            throw new IllegalArgumentException("Dung lượng không được để trống");
        if (rom.getLinkLuuTru() == null || rom.getLinkLuuTru().trim().isEmpty())
            throw new IllegalArgumentException("Link lưu trữ không được để trống");
        if (rom.getSoLuotBan() < 0)
            throw new IllegalArgumentException("Số lượt bán không được âm");
    }
}