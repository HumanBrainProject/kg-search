package eu.ebrains.kg.common.model.target.elasticsearch.instances;

import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.PreviewObject;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.TargetFile;

import java.util.List;

public interface HasPreviews {

    List<PreviewObject> getPreviewObjects();

}
