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
        boolean mergeActive = false;
        if(child.getDataset()!=null){
            if(parent.getDataset()==null){
                parent.setDataset(new ArrayList<>());
            }
            final Set<String> datasetReferences = parent.getDataset().stream().map(TargetInternalReference::getReference).collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(datasetReferences)) {
                parent.getDataset().addAll(child.getDataset());
                mergeActive = true;
            }
        }
        if(child.getPublications()!=null){
            if(parent.getPublications()==null){
                parent.setPublications(new ArrayList<>());
            }
            final List<Value<String>> publications = child.getPublications().stream().filter(d -> !parent.getPublications().contains(d)).collect(Collectors.toList());
            if(!CollectionUtils.isEmpty(publications)) {
                mergeActive = true;
                parent.getPublications().addAll(publications);
            }
        }
        if (parent.getIdentifier() == null) {
            parent.setIdentifier(new ArrayList<>());
        }
        if(child.getIdentifier()!=null){
            final List<String> identifier = child.getIdentifier().stream().filter(d -> !parent.getIdentifier().contains(d)).collect(Collectors.toList());
            if(!CollectionUtils.isEmpty(identifier)) {
                mergeActive = true;
                parent.getIdentifier().addAll(identifier);
            }
        }
        if(!parent.getIdentifier().contains(child.getId())){
            mergeActive = true;
            parent.getIdentifier().add(child.getId());
        }
        if(mergeActive){
            logger.warn(String.format("Active merging of project %s", parent.getId()));
        }
    }

}
