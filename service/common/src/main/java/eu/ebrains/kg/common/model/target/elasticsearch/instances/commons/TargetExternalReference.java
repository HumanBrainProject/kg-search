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

package eu.ebrains.kg.common.model.target.elasticsearch.instances.commons;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.ebrains.kg.common.model.target.elasticsearch.ElasticSearchInfo;

public class TargetExternalReference {

    public TargetExternalReference() {}

    public TargetExternalReference(String url, String value) {
        this.url = normalizeUrl(url);
        this.value = value;
    }

    @ElasticSearchInfo(ignoreAbove = 256)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String url;
    private String value;

    private String normalizeUrl(String url){
        // We want to ensure that all the links pointing to data-proxy are having the inline parameter set (e.g. for PDFs, etc.)
        if(url!=null && url.startsWith("https://data-proxy.ebrains.eu") && !(url.contains("&inline") || url.contains("?inline"))){
            url = String.format("%s%sinline=true", url, url.contains("?") ? "&" : "?");
        }
        return url;
    }


    public String getUrl() { return url; }

    public void setUrl(String url) { this.url = normalizeUrl(url); }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}