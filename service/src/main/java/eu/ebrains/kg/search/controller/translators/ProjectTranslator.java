package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.openMINDSv1.ProjectV1;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Project;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.stream.Collectors;

public class ProjectTranslator implements Translator<ProjectV1, Project> {

    public Project translate(ProjectV1 projectSource, DataStage dataStage, boolean liveMode) {
        Project p = new Project();
        p.setFirstRelease(projectSource.getFirstReleaseAt());
        p.setDescription(projectSource.getDescription());
        p.setLastRelease(projectSource.getLastReleaseAt());
        if(!CollectionUtils.isEmpty(projectSource.getDatasets())) {
            p.setDataset(projectSource.getDatasets().stream()
                    .map(dataset ->
                            new TargetInternalReference(
                                    liveMode ? dataset.getRelativeUrl() : String.format("Dataset/%s", dataset.getIdentifier()),
                                    dataset.getName(), null))
                    .collect(Collectors.toList()));
        }
        p.setTitle(projectSource.getTitle());
        if(!CollectionUtils.isEmpty(projectSource.getPublications())) {
            p.setPublications(projectSource.getPublications().stream()
                    .map(publication -> {
                        String doi;
                        if(StringUtils.isNotBlank(publication.getDoi())) {
                            String url = URLEncoder.encode(publication.getDoi(), StandardCharsets.UTF_8);
                            doi = String.format("[DOI: %s]\n[DOI: %s]: https://doi.org/%s", publication.getDoi(), publication.getDoi(), url);
                        } else {
                            doi = "[DOI: null]\n[DOI: null]: https://doi.org/null";
                        }
                        if (StringUtils.isNotBlank(publication.getCitation())) {
                            return publication.getCitation() + "\n" + doi;
                        } else {
                            return doi;
                        }
                    }).collect(Collectors.toList()));
        }
        p.setIdentifier(Collections.singletonList(projectSource.getIdentifier()));
        if (dataStage == DataStage.IN_PROGRESS) {
            p.setEditorId(projectSource.getEditorId());
        }
        return p;
    }
}
