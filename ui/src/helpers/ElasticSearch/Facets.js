/*
 * Copyright 2018 - 2021 Swiss Federal Institute of Technology Lausanne (EPFL)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This open source software code was developed in part or in whole in the
 * Human Brain Project, funded from the European Union's Horizon 2020
 * Framework Programme for Research and Innovation under
 * Specific Grant Agreements No. 720270, No. 785907, and No. 945539
 * (Human Brain Project SGA1, SGA2 and SGA3).
 *
 */
const FACET_DEFAULT_SIZE = 10;
const FACET_ALL_SIZE = 1000000000;

export const getResetFacets = facets => {
  return facets.map(f => {
    switch (f.filterType) {
    case "list":
      return {
        ...f,
        value: null,
        size: (f.isHierarchical || f.isFilterable)?FACET_ALL_SIZE:FACET_DEFAULT_SIZE
      };
    case "exists":
    default:
      return {
        ...f,
        value: null
      };
    }
  });
};

export const constructFacets = definition => {
  const facets = [];
  Object.entries(definition).forEach(([type, typeDefinition]) => {
    Object.entries(typeDefinition.fields).forEach(([name, field]) => {
      if (field.facet) {
        facets.push({
          id: "facet_" + type + "_" + name,
          name: name,
          type: type,
          filterType: field.facet,
          filterOrder: field.facetOrder,
          exclusiveSelection: field.facetExclusiveSelection,
          fieldType: field.type,
          fieldLabel: field.label,
          isChild: false,
          isFilterable: field.isFilterableFacet,
          count: 0,
          value: null,
          keywords: [],
          size: field.isFilterableFacet?FACET_ALL_SIZE:FACET_DEFAULT_SIZE,
          defaultSize: field.isFilterableFacet?FACET_ALL_SIZE:FACET_DEFAULT_SIZE
        });
      }
      if (field.children) {
        Object.entries(field.children).forEach(([childName, child]) => {
          if (child.facet) {
            if (child.isHierarchicalFacet) {
              facets.push({
                id: "facet_" + type + "_" + name + ".children." + childName,
                name: name,
                type: type,
                filterType: child.facet,
                filterOrder: child.facetOrder,
                exclusiveSelection: field.facetExclusiveSelection,
                fieldType: child.type,
                fieldLabel: field.label,
                isChild: true,
                isHierarchical: true,
                isFilterable: false,
                path: name + ".children",
                childName: childName,
                count: 0,
                value: null,
                keywords: [],
                size: FACET_ALL_SIZE,
                defaultSize: FACET_ALL_SIZE,
                missingTerm: field.facetMissingTerm?field.facetMissingTerm:"Others"
              });
            } else {
              facets.push({
                id: "facet_" + type + "_" + name + ".children." + childName,
                name: name,
                type: type,
                filterType: child.facet,
                filterOrder: child.facetOrder,
                exclusiveSelection: field.facetExclusiveSelection,
                fieldType: child.type,
                fieldLabel: child.label,
                isChild: true,
                isHierarchical: false,
                isFilterable: false,
                path: name + ".children",
                childName: childName,
                count: 0,
                value: null,
                keywords: [],
                size: FACET_DEFAULT_SIZE,
                defaultSize: FACET_DEFAULT_SIZE
              });
            }
          }
        });
      }
    });
  });
  return facets;
};
