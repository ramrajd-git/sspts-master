package dk.skat.ict.sspts.batch.enums;

/**
 * Created by mns on 02-03-2017.
 */
public enum DRFooter implements DREnum{
    INDV_NR(4,1,"9999"),
    FELT_LGD_KOD(4,5,"0096"),
    INDV_LGD_KOD(4,9,"0096"),
    TRANS_BLB_TOT(16,19,null),
    INDV_ANT(7,48,null);

    private int pos;
    private int length;
    private String val;
    private boolean constant;

    private DRFooter(int l, int p, String v) {
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
