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

package eu.ebrains.kg.common.controller.translators.kgv3;

import eu.ebrains.kg.common.model.DataStage;
import eu.ebrains.kg.common.model.source.ResultsOfKGv3;
import eu.ebrains.kg.common.model.source.openMINDSv3.BrainAtlasV3;
import eu.ebrains.kg.common.model.source.openMINDSv3.CoordinateSpaceV3;
import eu.ebrains.kg.common.model.source.openMINDSv3.commons.FullNameRef;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.BrainAtlas;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.CoordinateSpace;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.BasicHierarchyElement;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.Children;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.Value;
import eu.ebrains.kg.common.utils.IdUtils;
import eu.ebrains.kg.common.utils.TranslationException;
import eu.ebrains.kg.common.utils.TranslatorUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class CoordinateSpaceV3Translator extends TranslatorV3<CoordinateSpaceV3, CoordinateSpace, CoordinateSpaceV3Translator.Result> {
    private static final String QUERY_ID = "38bfc9cb-aba4-4e78-818d-5b3e536cfa99";

    @Override
    public List<String> getQueryIds() {
        return Collections.singletonList(QUERY_ID);
    }

    @Override
    public Class<CoordinateSpaceV3Translator.Result> getResultType() {
        return CoordinateSpaceV3Translator.Result.class;
    }

    @Override
    public Class<CoordinateSpaceV3> getSourceType() {
        return CoordinateSpaceV3.class;
    }

    @Override
    public Class<CoordinateSpace> getTargetType() {
        return CoordinateSpace.class;
    }

    @Override
    public List<String> semanticTypes() {
        return Collections.singletonList("https://openminds.ebrains.eu/sands/CommonCoordinateSpace");
    }

    public static class Result extends ResultsOfKGv3<CoordinateSpaceV3> {
    }

    public CoordinateSpace translate(CoordinateSpaceV3 coordinateSpaceV3, DataStage dataStage, boolean liveMode, TranslatorUtils translatorUtils) throws TranslationException {
        CoordinateSpace c = new CoordinateSpace();
        c.setCategory(new Value<>("Coordinate space"));
        c.setDisclaimer(new Value<>("Not correct? The openMINDS coordinate spaces are community-driven. Please get in touch with the openMINDS development team [openMINDS@ebrains.eu](mailto:openMINDS@ebrains.eu) or raise an issue on the openMINDS GitHub if you'd like to correct a coordinate space or want to add more information to an coordinate spaces."));
        c.setId(IdUtils.getUUID(coordinateSpaceV3.getId()));
        c.setAllIdentifiers(coordinateSpaceV3.getIdentifier());
        c.setIdentifier(IdUtils.getUUID(coordinateSpaceV3.getIdentifier()).stream().distinct().collect(Collectors.toList()));
        c.setTitle(value(coordinateSpaceV3.getFullName()));
        c.setVersionIdentifier(value(coordinateSpaceV3.getVersionIdentifier()));
        c.setDescription(value(coordinateSpaceV3.getDescription()));
        c.setHomepage(link(coordinateSpaceV3.getHomepage()));
        c.setAnatomicalAxesOritentation(ref(coordinateSpaceV3.getAnatomicalAxesOrientation()));
        c.setNativeUnit(ref(coordinateSpaceV3.getNativeUnit()));
        c.setOntologyIdentifiers(simpleLink(coordinateSpaceV3.getOntologyIdentifier()));
        c.setQueryBuilderText(value(TranslatorUtils.createQueryBuilderText(coordinateSpaceV3.getPrimaryType(), c.getId())));
        return c;
    }


}
