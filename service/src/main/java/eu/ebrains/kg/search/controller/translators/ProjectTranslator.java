package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.DatabaseScope;
import eu.ebrains.kg.search.model.source.openMINDSv1.ProjectV1;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Project;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class ProjectTranslator implements Translator<ProjectV1, Project> {

    public Project translate(ProjectV1 projectSource, DatabaseScope databaseScope, boolean liveMode) {
        Project p = new Project();
        p.setFirstRelease(projectSource.getFirstReleaseAt());
        p.setDescription(projectSource.getDescription());
        p.setLastRelease(projectSource.getLastReleaseAt());
        p.setDataset(projectSource.getDatasets().stream()
                .map(dataset ->
                        new TargetInternalReference(
                                liveMode ? dataset.getRelativeUrl() : String.format("Dataset/%s", dataset.getIdentifier()),
                                dataset.getName(), null))
                .collect(Collectors.toList()));
        p.setTitle(projectSource.getTitle());
        p.setPublications(projectSource.getPublications().stream()
                .map(publication -> {
                    String publicationResult = "";
                    if (publication.getCitation() != null && publication.getDoi() != null) {
                        String url = URLEncoder.encode(publication.getDoi(), StandardCharsets.UTF_8);
                        publicationResult = publication.getCitation() + "\n" + String.format("[DOI: %s]\\n[DOI: %s]: https://doi.org/%s\"", publication.getDoi(), publication.getDoi(), url);
                    } else if (publication.getCitation() != null && publication.getDoi() == null) {
                        publicationResult = publication.getCitation() + "\n" + "[DOI: null]\\n[DOI: null]: https://doi.org/null\"";
                    } else {
                        publicationResult = publication.getDoi();
                    }
                    return publicationResult;
                }).collect(Collectors.toList()));
        p.setIdentifier(projectSource.getIdentifier());
        if (databaseScope == DatabaseScope.INFERRED) {
            p.setEditorId(projectSource.getEditorId());
        }
        return p;
    }
}
