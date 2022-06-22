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
