package eu.ebrains.kg.search.controller.mergers;

import eu.ebrains.kg.search.model.target.elasticsearch.instances.Contributor;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ContributorMerger extends Merger<Contributor> {

    protected void merge(Contributor parent, Contributor child) {
        if (parent.getIdentifier() == null) {
            parent.setIdentifier(new ArrayList<>());
        }
        if (child.getIdentifier() != null) {
            parent.getIdentifier().addAll(child.getIdentifier().stream().filter(d -> !parent.getIdentifier().contains(d)).collect(Collectors.toList()));
        }
        if (child.getFirstRelease() != null) {
            if (parent.getFirstRelease() == null) {
                parent.setFirstRelease(child.getFirstRelease());
            } else if (child.getFirstRelease().getValue().before(parent.getFirstRelease().getValue())) {
                parent.setFirstRelease(child.getFirstRelease());
            }
        }
        if (child.getLastRelease() != null) {
            if (parent.getLastRelease() == null) {
                parent.setLastRelease(child.getLastRelease());
            } else if (child.getLastRelease().getValue().after(parent.getLastRelease().getValue())) {
                parent.setLastRelease(child.getLastRelease());
            }
        }
        if (child.getContributions() != null) {
            if (parent.getContributions() == null) {
                parent.setContributions(new ArrayList<>());
            }
            parent.getContributions().addAll(child.getContributions().stream().filter(d -> !parent.getContributions().contains(d)).collect(Collectors.toList()));
        }
        if (child.getCustodianOf() != null) {
            if (parent.getCustodianOf() == null) {
                parent.setCustodianOf(new ArrayList<>());
            }
            parent.getCustodianOf().addAll(child.getCustodianOf().stream().filter(d -> !parent.getCustodianOf().contains(d)).collect(Collectors.toList()));
        }
        if (child.getCustodianOfModel() != null) {
            if (parent.getCustodianOfModel() == null) {
                parent.setCustodianOfModel(new ArrayList<>());
            }
            parent.getCustodianOfModel().addAll(child.getCustodianOfModel().stream().filter(d -> !parent.getCustodianOfModel().contains(d)).collect(Collectors.toList()));
        }
        if (child.getModelContributions() != null) {
            if (parent.getModelContributions() == null) {
                parent.setModelContributions(new ArrayList<>());
            }
            parent.getModelContributions().addAll(child.getModelContributions().stream().filter(d -> !parent.getModelContributions().contains(d)).collect(Collectors.toList()));
        }
        if (child.getPublications() != null) {
            if (parent.getPublications() == null) {
                parent.setPublications(new ArrayList<>());
            }
            parent.getPublications().addAll(child.getPublications().stream().filter(d -> !parent.getPublications().contains(d)).collect(Collectors.toList()));
        }
    }
}
