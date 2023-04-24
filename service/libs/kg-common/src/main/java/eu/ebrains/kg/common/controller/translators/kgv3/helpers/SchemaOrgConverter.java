package eu.ebrains.kg.common.controller.translators.kgv3.helpers;

import eu.ebrains.kg.common.controller.translators.Helpers;
import eu.ebrains.kg.common.model.source.openMINDSv3.DatasetVersionV3;
import eu.ebrains.kg.common.model.source.openMINDSv3.commons.PersonOrOrganizationRef;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.schemaorg.SchemaOrgDataset;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SchemaOrgConverter {

    public static SchemaOrgDataset translateDatasetVersion(DatasetVersionV3 datasetVersion){
        SchemaOrgDataset schemaOrgDataset = new SchemaOrgDataset();
        List<PersonOrOrganizationRef> authors = datasetVersion.getAuthor();
        if(CollectionUtils.isEmpty(authors)){
            authors = datasetVersion.getDataset().getAuthor();
        }
        schemaOrgDataset.setName(StringUtils.isNotBlank(datasetVersion.getFullName()) ? datasetVersion.getFullName() : datasetVersion.getDataset().getFullName());
        schemaOrgDataset.setDescription(StringUtils.isNotBlank(datasetVersion.getDescription()) ? datasetVersion.getDescription() : datasetVersion.getDataset().getDescription());
            schemaOrgDataset.setVersion(datasetVersion.getVersion());
            List<String> identifiers = new ArrayList<>(datasetVersion.getIdentifier());
        if(StringUtils.isNotBlank(datasetVersion.getDoi())) {
            schemaOrgDataset.setIdentifier(identifiers);
            identifiers.add(datasetVersion.getDoi());
        }
        schemaOrgDataset.setCreator(authors.stream().map(a -> {
            if(a.getFamilyName()!=null){
                final SchemaOrgDataset.Person person = new SchemaOrgDataset.Person();
                person.setFamilyName(a.getFamilyName());
                person.setGivenName(a.getGivenName());
                person.setName(Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName()));
                return person;
            }
            else if(a.getFullName()!=null){
                SchemaOrgDataset.Organization organization = new SchemaOrgDataset.Organization();
                organization.setName(a.getFullName());
                return organization;
            }
            return null;
        }).collect(Collectors.toList()));
        return schemaOrgDataset;
    }

}
