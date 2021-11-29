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

package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.ResultsOfKG;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.ExternalRef;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.FullNameRef;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.FullNameRefForResearchProductVersion;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.*;
import eu.ebrains.kg.search.services.DOICitationFormatter;
import eu.ebrains.kg.search.utils.IdUtils;
import eu.ebrains.kg.search.utils.TranslationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class Translator<Source, Target, ListResult extends ResultsOfKG<Source>> {
    public static final String fileProxy = ""; //TODO: Should that be changed ?
//    public static final String fileProxy = "https://kg.ebrains.eu";

    public abstract Target translate(Source source, DataStage dataStage, boolean liveMode, DOICitationFormatter doiCitationFormatter) throws TranslationException;

    public abstract Class<Source> getSourceType();

    public abstract Class<Target> getTargetType();

    public abstract Class<ListResult> getResultType();

    public abstract List<String> getQueryIds();


    public String getQueryFileName(String semanticType){
        final String simpleName = getClass().getSimpleName();
        return StringUtils.uncapitalize(simpleName.substring(0, simpleName.indexOf("V3")));
    }


    protected Value<String> value(String v){
        if(StringUtils.isNotBlank(v)){
            return new Value<>(v.trim());
        }
        return null;
    }

    protected ISODateValue value(Date date){
        if(date != null){
            return new ISODateValue(date);
        }
        else{
            return null;
        }

    }

    protected <T> List<Children<T>> children(List<T> values){
        if(!CollectionUtils.isEmpty(values)){
            return values.stream().map(Children::new).collect(Collectors.toList());
        }
        return null;
    }

    protected List<Value<String>> value(List<String> values){
        if(!CollectionUtils.isEmpty(values)){
            return values.stream().map(this::value).filter(Objects::nonNull).collect(Collectors.toList());
        }
        return null;
    }


    protected TargetExternalReference link(String url){
        if(StringUtils.isNotBlank(url)){
            return new TargetExternalReference(url.trim(), url.trim());
        }
        return null;
    }

    protected TargetInternalReference ref(FullNameRef ref){
        if(ref!=null){
            final String uuid = IdUtils.getUUID(ref.getId());
            return new TargetInternalReference(uuid, StringUtils.defaultString(ref.getFullName(), uuid));
        }
        return null;
    }

    protected List<TargetInternalReference> ref(List<FullNameRef> refs){
        if(!CollectionUtils.isEmpty(refs)){
            return refs.stream().map(this::ref).filter(Objects::nonNull).collect(Collectors.toList());
        }
        return null;
    }

    protected List<TargetInternalReference> refVersion(List<FullNameRefForResearchProductVersion> refs){
        if(!CollectionUtils.isEmpty(refs)){
            return refs.stream().map(this::ref).filter(Objects::nonNull).collect(Collectors.toList());
        }
        return null;
    }


    protected TargetInternalReference ref(FullNameRefForResearchProductVersion ref){
        if(ref!=null){
            String name = StringUtils.defaultString(ref.getFullName(), ref.getFallbackName());
            String uuid = IdUtils.getUUID(ref.getId());
            if(name==null){
                name = uuid;
            }
            String versionedName = StringUtils.isNotBlank(ref.getVersionIdentifier()) ? String.format("%s %s", name, ref.getVersionIdentifier()) : name;
            return new TargetInternalReference(uuid, versionedName);
        }
        return null;
    }

    protected TargetExternalReference link(ExternalRef ref){
        if(ref!=null && StringUtils.isNotBlank(ref.getUrl())) {
            return new TargetExternalReference(ref.getUrl(), ref.getUrl() != null ? ref.getLabel() : ref.getUrl());
        }
        return null;
    }

}