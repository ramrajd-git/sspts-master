package dk.skat.ict.sspts.batch.enums;

/**
 * Created by mns on 02-03-2017.
 */
public enum DRHeader implements DREnum{
    INDV_NR(4,1,"0000"),
    FELT_LGD_KOD(4,5,"0096"),
    INDV_LGD_KOD(4,9,"0096"),
    LEV_NR(4,13,null),
    SYSTEM(8,17,null),
    KSL_DTO(8,47,null);

    private int pos;
    private int length;
    private String val;
    private boolean constant;

    private DRHeader(int l, int p, String v) {
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
