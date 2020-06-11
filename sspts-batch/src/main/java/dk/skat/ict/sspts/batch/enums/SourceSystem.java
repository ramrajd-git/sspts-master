package dk.skat.ict.sspts.batch.enums;

/**
 * Created by mns on 08-03-2017.
 */
public enum SourceSystem {
    IMPORT("IMPORT","I","DABETT.FTP.TOLD.AD091"),
    MANIFEST("MANIFEST","M","DABETT.FTP.TOLD.AD090");

    private String system;
    private String identifier;
    private String filename;

    SourceSystem(String system, String indentifier, String filename) {
        this.system = system;
        this.identifier = indentifier;
        this.filename = filename;
    }

    @Override
    public String toString(){
        return system;
    }

    public static SourceSystem getSystem(String identifier) {
        for(SourceSystem sourceSystem : SourceSystem.values()) {
            if(sourceSystem.identifier.equals(identifier)){
                return sourceSystem;
            }
        }
        return null;
    }

    public String getFilename() {
        return filename;
    }

    public String getIdentifier() {
        return identifier;
    }
}
