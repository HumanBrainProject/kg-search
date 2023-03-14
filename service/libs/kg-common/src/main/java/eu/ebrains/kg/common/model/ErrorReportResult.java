package eu.ebrains.kg.common.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ErrorReportResult {

    private String type = "https://search.kg.ebrains.eu/ErrorReport";
    private List<ErrorReportResultByTargetType> errorsByTarget;

    @Getter
    @Setter
    public static class ErrorReportResultByTargetType{
        private String targetType;
        private ErrorReport errors;
    }

}
