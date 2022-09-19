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

export const resetFacet = facet => {
  switch (facet.type) {
  case "list":
    facet.value = null;
    facet.size = (facet.isHierarchical || facet.isFilterable)?FACET_ALL_SIZE:FACET_DEFAULT_SIZE;
    break;
  case "exists":
  default:
    facet.value = null;
  }
};

export const constructFacet = facet => ({
  ...facet,
  count: 0,
  value: null,
  keywords: [],
  others: 0,
  size: facet.isFilterable?FACET_ALL_SIZE:FACET_DEFAULT_SIZE,
  defaultSize: facet.isFilterable?FACET_ALL_SIZE:FACET_DEFAULT_SIZE
});

export const getAggregation = facets => {
  if (!Array.isArray(facets)) {
    return {};
  }
  return facets.reduce((acc, facet) => {
    switch (facet.type) {
    case "list":
      //if (facet.isHierarchical) {
      if (Array.isArray(facet.value) && facet.value.length) {
        acc[facet.name] = {
          values: facet.value,
          size: facet.size
        };
      } else {
        acc[facet.name] = {
          size: facet.size
        };
      }
      break;
    case "exists":
      if (facet.value) {
        acc[facet.name] = {};
      }
      break;
    default:
      break;
    }
    return acc;
  }, {});
};