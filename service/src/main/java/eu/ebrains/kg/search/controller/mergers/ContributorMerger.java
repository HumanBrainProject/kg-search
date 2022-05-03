package eu.ebrains.kg.search.controller.mergers;

import eu.ebrains.kg.search.model.target.elasticsearch.instances.Contributor;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ContributorMerger extends Merger<Contributor> {

    protected void merge(Contributor parent, Contributor child) {
        if (parent.getIdentifier() == null) {
            parent.setIdentifier(new ArrayList<>());
        }
        if (child.getIdentifier() != null) {
            parent.getIdentifier().addAll(child.getIdentifier().stream().filter(d -> !parent.getIdentifier().contains(d)).collect(Collectors.toList()));
        }
        if (child.getFirstRelease() != null) {
            if (parent.getFirstRelease() == null || child.getFirstRelease().getValue().before(parent.getFirstRelease().getValue())) {
                parent.setFirstRelease(child.getFirstRelease());
            }
        }
        if (child.getLastRelease() != null) {
            if (parent.getLastRelease() == null || child.getLastRelease().getValue().after(parent.getLastRelease().getValue())) {
                parent.setLastRelease(child.getLastRelease());
            }
        }
        if (child.getDatasetContributions() != null) {
            if (parent.getDatasetContributions() == null) {
                parent.setDatasetContributions(new ArrayList<>());
            }
            final Set<String> parentContributions = parent.getDatasetContributions().stream().map(TargetInternalReference::getReference).collect(Collectors.toSet());
            final Set<String> parentContributionsWithOld = Stream.concat(parentContributions.stream(), parentContributions.stream().map(p -> String.format("Person/%s", p))).collect(Collectors.toSet());
            parent.getDatasetContributions().addAll(child.getDatasetContributions().stream().filter(d -> parentContributionsWithOld.contains(d.getReference())).collect(Collectors.toList()));
            sortByValue(parent.getDatasetContributions());
        }
        if (child.getCustodianOfDataset() != null) {
            if (parent.getCustodianOfDataset() == null) {
                parent.setCustodianOfDataset(new ArrayList<>());
            }
            final Set<String> parentCustodians = parent.getCustodianOfDataset().stream().map(TargetInternalReference::getReference).collect(Collectors.toSet());
            final Set<String> parentCustodiansWithOld = Stream.concat(parentCustodians.stream(), parentCustodians.stream().map(p -> String.format("Person/%s", p))).collect(Collectors.toSet());
            parent.getCustodianOfDataset().addAll(child.getCustodianOfDataset().stream().filter(d -> !parentCustodiansWithOld.contains(d.getReference())).collect(Collectors.toList()));
            sortByValue(parent.getCustodianOfDataset());
        }
        if (child.getCustodianOfModel() != null) {
            if (parent.getCustodianOfModel() == null) {
                parent.setCustodianOfModel(new ArrayList<>());
            }
            final Set<String> parentCustodians = parent.getCustodianOfModel().stream().map(TargetInternalReference::getReference).collect(Collectors.toSet());
            final Set<String> parentCustodiansWithOld = Stream.concat(parentCustodians.stream(), parentCustodians.stream().map(p -> String.format("Person/%s", p))).collect(Collectors.toSet());
            parent.getCustodianOfModel().addAll(child.getCustodianOfModel().stream().filter(d -> !parentCustodiansWithOld.contains(d.getReference())).collect(Collectors.toList()));
            sortByValue(parent.getCustodianOfModel());
        }
        if (child.getModelContributions() != null) {
            if (parent.getModelContributions() == null) {
                parent.setModelContributions(new ArrayList<>());
            }
            final Set<String> parentContributions = parent.getModelContributions().stream().map(TargetInternalReference::getReference).collect(Collectors.toSet());
            final Set<String> parentContributionsWithOld = Stream.concat(parentContributions.stream(), parentContributions.stream().map(p -> String.format("Person/%s", p))).collect(Collectors.toSet());
            parent.getModelContributions().addAll(child.getModelContributions().stream().filter(d -> !parentContributionsWithOld.contains(d.getReference())).collect(Collectors.toList()));
            sortByValue(parent.getModelContributions());
        }
    }

    private void sortByValue(List<TargetInternalReference> listTargetInternalReference) {
        listTargetInternalReference.sort(Comparator.comparing(TargetInternalReference::getValue));
    }
}
