package eu.ebrains.kg.common.controller.mergers;

import eu.ebrains.kg.common.model.target.elasticsearch.instances.Project;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ProjectMerger extends Merger<Project>{

    private final Logger logger = LoggerFactory.getLogger(getClass());
    protected void merge(Project parent, Project child){
        if(child.getDataset()!=null){
            if(parent.getDataset()==null){
                parent.setDataset(new ArrayList<>());
            }
            final Set<String> datasetReferences = parent.getDataset().stream().map(TargetInternalReference::getReference).collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(datasetReferences)) {
                parent.getDataset().addAll(child.getDataset());
                logger.warn(String.format("Choosing old datasets for project %s", parent.getId()));
                //Since we're choosing the "old" datasets, we also want the "old" publications to show.
                parent.setPublications(child.getPublications());
            }
        }
        if (parent.getIdentifier() == null) {
            parent.setIdentifier(new ArrayList<>());
        }
        if(child.getIdentifier()!=null){
            final List<String> identifier = child.getIdentifier().stream().filter(d -> !parent.getIdentifier().contains(d)).collect(Collectors.toList());
            if(!CollectionUtils.isEmpty(identifier)) {
                logger.error(String.format("We found at least one identifier in the old representation of instance %s which is not part of the new instance!", parent.getId()));
                parent.getIdentifier().addAll(identifier);
            }
        }
        if(!parent.getIdentifier().contains(child.getId())){
            logger.error(String.format("We found that the id of the old representation of instance %s is not part of the new instance!", parent.getId()));
            parent.getIdentifier().add(child.getId());
        }
    }

}
