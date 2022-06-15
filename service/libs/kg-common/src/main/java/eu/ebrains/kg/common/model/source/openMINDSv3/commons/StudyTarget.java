package eu.ebrains.kg.common.model.source.openMINDSv3.commons;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StudyTarget extends AnatomicalLocation {
    private List<String> studyTargetType;
}

