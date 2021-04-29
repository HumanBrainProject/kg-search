package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.openMINDSv3.ProjectV3;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Project;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.utils.IdUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class ProjectOfKGV3Translator  implements Translator<ProjectV3, Project>{

    public Project translate(ProjectV3 project, DataStage dataStage, boolean liveMode) {
        Project p = new Project();
        String uuid = IdUtils.getUUID(project.getId());
        p.setId(uuid);
        p.setIdentifier(project.getIdentifier());
        p.setDescription(project.getDescription());
        if(!CollectionUtils.isEmpty(project.getDatasets())) {
            p.setDataset(project.getDatasets().stream()
                    .map(dataset ->
                            new TargetInternalReference(
                                    IdUtils.getUUID(dataset.getId()),
                                    dataset.getFullName()))
                    .collect(Collectors.toList()));
        }
        p.setTitle(project.getTitle());
        if(!CollectionUtils.isEmpty(project.getPublications())) {
            p.setPublications(project.getPublications().stream()
                    .map(publication -> {
                        String doi;
                        if(StringUtils.isNotBlank(publication.getIdentifier())) {
                            String url = URLEncoder.encode(publication.getIdentifier(), StandardCharsets.UTF_8);
                            doi = String.format("[DOI: %s]\n[DOI: %s]: %s", publication.getIdentifier(), publication.getIdentifier(), url);
                        } else {
                            doi = "[DOI: null]\n[DOI: null]: https://doi.org/null";
                        }
                        if (StringUtils.isNotBlank(publication.getHowToCite())) {
                            return publication.getHowToCite() + "\n" + doi;
                        } else {
                            return doi;
                        }
                    }).collect(Collectors.toList()));
        }
        return p;
    }
}
