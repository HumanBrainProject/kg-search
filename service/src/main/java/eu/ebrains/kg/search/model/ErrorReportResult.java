package eu.ebrains.kg.search.model;

import java.util.HashMap;
import java.util.List;

public class ErrorReportResult {

    private String type = "https://search.kg.ebrains.eu/ErrorReport";
    private List<ErrorReportResultByTargetType> errorsByTarget;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<ErrorReportResultByTargetType> getErrorsByTarget() {
        return errorsByTarget;
    }

    public void setErrorsByTarget(List<ErrorReportResultByTargetType> errorsByTarget) {
        this.errorsByTarget = errorsByTarget;
    }

    public static class ErrorReportResultByTargetType{
        private String targetType;
        private List<ErrorReportResultBySourceType> errorsBySource;

        public String getTargetType() {
            return targetType;
        }

        public void setTargetType(String targetType) {
            this.targetType = targetType;
        }

        public List<ErrorReportResultBySourceType> getErrorsBySource() {
            return errorsBySource;
        }

        public void setErrorsBySource(List<ErrorReportResultBySourceType> errorsBySource) {
            this.errorsBySource = errorsBySource;
        }
    }

    public static class ErrorReportResultBySourceType{
        private String sourceType;
        private ErrorReport errors;

        public String getSourceType() {
            return sourceType;
        }

        public void setSourceType(String sourceType) {
            this.sourceType = sourceType;
        }

        public ErrorReport getErrors() {
            return errors;
        }

        public void setErrors(ErrorReport errors) {
            this.errors = errors;
        }
    }



}
