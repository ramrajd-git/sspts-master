package dk.skat.ict.sspts.batch.enums;

/**
 * Created by mns on 02-03-2017.
 */
public enum DRBody implements DREnum{
    INDV_NR(4,1,"0629"),
    FELT_LGD_KOD(4,5,"0096"),
    INDV_LGD_KOD(4,9,"0096"),
    VIRK_NR(8,13,null),
    TRANS_ART(5,21,null),
    UKTO_ART(3,26,null),
    PER_DTO(8,29,null),
    HJEM_DT(2,37,null),
    TXT_KOD(3,39,null),
    KTO_NR(4,42,null),
    ANTAG_DTO(8,46,null),
    KØRSELS_DTO(8,54,null),
    TRANS_BLB(16,62,null),
    TOLD_REF_NR(11,78,null),
    TOLD_REG_SE_NR(8,89,null);

    private int pos;
    private int length;
    private String val;
    private boolean constant;

    private DRBody(int l, int p, String v) {
        pos = p;
        length = l;
        val = v;
        constant = val != null;
    }

    public int pos() {
        return pos;
    }
    public int length() {
        return length;
    }
    public String val() {
        return val;
    }

    public boolean isConstant() {
        return constant;
    }
}
