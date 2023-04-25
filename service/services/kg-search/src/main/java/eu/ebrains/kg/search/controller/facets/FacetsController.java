/*
 *  Copyright 2018 - 2021 Swiss Federal Institute of Technology Lausanne (EPFL)
 *  Copyright 2021 - 2023 EBRAINS AISBL
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0.
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  This open source software code was developed in part or in whole in the
 *  Human Brain Project, funded from the European Union's Horizon 2020
 *  Framework Programme for Research and Innovation under
 *  Specific Grant Agreements No. 720270, No. 785907, and No. 945539
 *  (Human Brain Project SGA1, SGA2 and SGA3).
 */

package eu.ebrains.kg.search.controller.facets;

import eu.ebrains.kg.common.model.TranslatorModel;
import eu.ebrains.kg.common.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.common.utils.MetaModelUtils;
import eu.ebrains.kg.search.model.Facet;
import eu.ebrains.kg.search.utils.FacetsUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.*;

@Component
public class FacetsController {

    private final MetaModelUtils utils;

    public FacetsController(MetaModelUtils utils) {
        this.utils = utils;
    }

    @Cacheable(value = "facets", unless = "#type == null", key = "#type")
    public List<Facet> getFacets(String type) {
        List<Facet> facets  = new ArrayList<>();
        if (StringUtils.isNotBlank(type)) {
            TranslatorModel.MODELS.stream().filter(m -> MetaModelUtils.getNameForClass(m.getTargetClass()).equals(type)).forEach(m -> {
                Class<?> targetModel = m.getTargetClass();
                handleChildren(targetModel, type, facets, "", "");
            });
        }
        Set<String> names = new HashSet<>();
        facets.forEach(facet -> {
            String name = FacetsUtils.getUniqueFacetName(facet, names);
            names.add(name);
            facet.setName(name);
        });
        return facets;
    }

    private void handleChildren(Type type, String rootType, List<Facet> facets, String parentPath, String parentPropertyName) {
        List<MetaModelUtils.FieldWithGenericTypeInfo> allFields = utils.getAllFields(type);
        String path = FacetsUtils.getPath(parentPath, parentPropertyName);
        String childPath = FacetsUtils.getChildPath(parentPath, parentPropertyName);
        allFields.forEach(field -> {
            try {
                handleField(field, rootType, facets, path, childPath);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void handleField(MetaModelUtils.FieldWithGenericTypeInfo f, String rootType, List<Facet> facets, String parentPath, String path) throws ClassNotFoundException {
        FieldInfo info = f.getField().getAnnotation(FieldInfo.class);
        if (info != null) {
            FieldInfo defaultFieldInfo = utils.defaultFieldInfo();
            String propertyName = utils.getPropertyName(f.getField());
            if (info.facet() != defaultFieldInfo.facet()) {
                Facet facet = new Facet(parentPath, path, propertyName);
                facets.add(facet);
                if (!info.label().equals(defaultFieldInfo.label())) {
                    facet.setLabel(info.label());
                }
                if (info.facet() != defaultFieldInfo.facet()) {
                    facet.setType(info.facet());
                }
                if (info.facetOrder() != defaultFieldInfo.facetOrder()) {
                    facet.setOrder(info.facetOrder());
                }
                if (info.isFilterableFacet() != defaultFieldInfo.isFilterableFacet()) {
                    facet.setIsFilterable(info.isFilterableFacet());
                }
                //facetExclusiveSelection
                //facetMissingTerm

            }
            Type topTypeToHandle = f.getGenericType() != null ? f.getGenericType() : MetaModelUtils.getTopTypeToHandle(f.getField().getGenericType());
            handleChildren(topTypeToHandle, rootType, facets, path, propertyName);
        }
    }
}
