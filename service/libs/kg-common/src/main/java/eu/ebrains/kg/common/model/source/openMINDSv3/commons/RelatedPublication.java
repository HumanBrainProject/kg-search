package eu.ebrains.kg.common.model.source.openMINDSv3.commons;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RelatedPublication {
    private String identifier;
    private List<String> type;

    public PublicationType resolvedType() {
        if (type == null || type.isEmpty()) {
            return PublicationType.UNDEFINED;
        }
        switch (type.get(0)) {
            case "https://openminds.ebrains.eu/core/DOI":
                return PublicationType.DOI;
            case "https://openminds.ebrains.eu/core/HANDLE":
                return PublicationType.HANDLE;
            case "https://openminds.ebrains.eu/core/ISBN":
                return PublicationType.ISBN;
            default: return PublicationType.UNDEFINED;
        }
    }
    public enum PublicationType {
        UNDEFINED, DOI, HANDLE, ISBN
    }
}
