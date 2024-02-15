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

package eu.ebrains.kg.projects.ebrains.translators.utils;

import eu.ebrains.kg.projects.ebrains.source.commons.HasMetaBadges;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MetaBadgeUtils {
    private static List<String> COMMUNITY_STANDARDS = Arrays.asList("application/vnd.bids", "application/vnd.bids.electrodesformat", "application/vnd.g-node.nix.neo", "application/vnd.g-node.nix+hdf5", "application/vnd.nwb.nwbn+hdf", "application/vnd.g-node.odml");
    private static List<String> IMAGE_VIEWER_SERVICES = Arrays.asList("LocaliZoom", "Multi-Image-OSd");
    private static List<String> ATLAS_SERVICES = Arrays.asList("Neuroglancer", "siibra-explorer");
    public static List<String> evaluateMetaBadgeUtils(HasMetaBadges instance, boolean hasOutputResources, boolean hasInputResources) {
        List<String> metaBadges = new ArrayList<>();
        if(!CollectionUtils.isEmpty(instance.getLearningResource()) || (instance.getParentOfVersion()!=null && !CollectionUtils.isEmpty(instance.getParentOfVersion().getLearningResource()))){
            metaBadges.add("isLearningResourceAvailable");
        }
        if(!CollectionUtils.isEmpty(instance.getAllServiceLinks())){
            if(instance.getAllServiceLinks().stream().anyMatch(s -> IMAGE_VIEWER_SERVICES.contains(s.getService()))){
                metaBadges.add("isLinkedToImageViewer");
            }
            if(instance.getAllServiceLinks().stream().anyMatch(s -> ATLAS_SERVICES.contains(s.getService()))){
                metaBadges.add("isIntegratedWithAtlas");
            }
        }
        if(!CollectionUtils.isEmpty(instance.getAllContentTypes())) {
            if (instance.getAllContentTypes().stream().anyMatch(s -> COMMUNITY_STANDARDS.contains(s))) {
                metaBadges.add("isFollowingStandards");
            }
        }
        if(!CollectionUtils.isEmpty(instance.getLivePapers())){
            metaBadges.add("isUsedInLivePaper");
        }
        if(hasOutputResources){
            metaBadges.add("isUsedByOthers");
        }
        if(hasInputResources){
            metaBadges.add("isUsingOthers");
        }
        if(instance.hasProtocolExecutions()){
            metaBadges.add("hasInDepthMetaData");
        }
        // "isLinkedToTools": Waiting for input data
        // "isReplicable": Waiting for pipeline to be ready
        // "hasInDepthMetaData":
        return metaBadges;
    }




}
