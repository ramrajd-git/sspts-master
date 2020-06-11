package dk.skat.ict.sspts.batch.enums;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Created by mns on 16-05-2017.
 */
@Converter(autoApply = true)
public class SourceSystemConverter implements AttributeConverter<SourceSystem, String> {
    @Override
    public String convertToDatabaseColumn(SourceSystem system) {
        return system.getIdentifier();
    }

    @Override
    public SourceSystem convertToEntityAttribute(String identifier) {
        return SourceSystem.getSystem(identifier);
    }
}
