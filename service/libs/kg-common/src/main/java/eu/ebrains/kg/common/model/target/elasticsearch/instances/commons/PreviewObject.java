package eu.ebrains.kg.common.model.target.elasticsearch.instances.commons;

import lombok.Getter;
import lombok.Setter;

import java.util.Comparator;

@Getter
@Setter
public class PreviewObject implements Comparable<PreviewObject> {
    private String imageUrl;
    private String videoUrl;
    private String description;
    private TargetExternalReference link;

    @Override
    public int compareTo(PreviewObject previewObject) {
        return Comparator.comparing(PreviewObject::getDescription).compare(this, previewObject);
    }
}
