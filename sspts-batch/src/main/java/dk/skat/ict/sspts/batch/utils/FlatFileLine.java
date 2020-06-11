package dk.skat.ict.sspts.batch.utils;


import dk.skat.ict.sspts.batch.enums.DREnum;
import dk.skat.ict.sspts.batch.enums.DRHeader;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mns on 02-03-2017.
 * Inspired by http://stackoverflow.com/a/5904633
 */
public class FlatFileLine{

    final StringBuilder flatFileLine;

    public FlatFileLine(int recordSize) {
        flatFileLine = new StringBuilder(recordSize);

        for(int i = 0; i < recordSize; i++) {
            flatFileLine.append(" ");
        }
    }

    private void setField(int pos,int length,String s) {
        int p = pos - 1;
        flatFileLine.replace(p, p + length, rightpad(s, length, false));
    }

    private void setField(int pos,int length,String s, boolean spaces) {
        int p = pos - 1;
        flatFileLine.replace(p, p + length, rightpad(s, length, spaces));
    }

    private void setField(int pos,int length,Date d) {
        String formattedDate = new SimpleDateFormat("yyyyMMdd").format(d);

        setField(pos,length, formattedDate);
    }

    private void setField(int pos,int length, long l, boolean signed) {
        if(signed) {
            if(l < 0) {
                setField(pos,length, String.format("%0" + (length - 1) + "d-",Math.abs(l)));
            } else {
                setField(pos,length, String.format("%0" + (length - 1) + "d+",l));
            }
        } else {
                setField(pos,length, String.format("%0" + length + "d",Math.abs(l)));
        }
    }

    public String toString() { return flatFileLine.toString(); }

    private String leftpad(String s, int length) {
        return String.format("%1$-" + length + "s", s).replace(" ","0");
    }

    private String rightpad(String s, int length, boolean spaces) {
        if(spaces) {
            return String.format("%1$" + length + "s", s);
        } else {
            return String.format("%1$" + length + "s", s).replace(" ","0");
        }
    }

    public void addField(DREnum f, Object value, boolean signed) {
        if(f.isConstant()) {
            setField(f.pos(),f.length(),f.val());
        } else if (value == null) {
            //TODO: Exception instead?
            setField(f.pos(), f.length(), " ");
        } else if (value instanceof Date){
            setField(f.pos(), f.length(), (Date) value);
        } else if (value instanceof Long){
            setField(f.pos(), f.length(), (long) value, signed);
        } else if (f.equals(DRHeader.SYSTEM)) {
            setField(f.pos(), f.length(), value.toString(), true);
        } else {
            setField(f.pos(), f.length(), value.toString(), false);
        }
    }

    public void addField(DREnum f, Object value) {
        addField(f, value, false);
    }


    public void addField(DREnum f) {
        addField(f, null);
    }
}
