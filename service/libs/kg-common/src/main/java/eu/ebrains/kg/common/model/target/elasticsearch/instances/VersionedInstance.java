package eu.ebrains.kg.common.model.target.elasticsearch.instances;

import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.TargetInternalReference;

import java.util.List;

public interface VersionedInstance {

    /**
     * @param versions the versions are interpreted by the UI for displaying the selection box in the header section
     */
    void setVersions(List<TargetInternalReference> versions);

    /**
     * @param version the current version of the instance (to select the right entry in the populated selection box)
     */
    void setVersion(String version);

}
