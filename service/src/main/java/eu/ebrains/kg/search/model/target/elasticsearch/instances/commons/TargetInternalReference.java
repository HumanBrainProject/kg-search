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

package eu.ebrains.kg.search.model.target.elasticsearch.instances.commons;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.*;

public class TargetInternalReference implements Comparable<TargetInternalReference> {
    private final static Logger logger = LoggerFactory.getLogger(TargetInternalReference.class);
    private static ThreadLocal<Set<TargetInternalReference>> TARGET_INTERNAL_REFERENCES = new ThreadLocal<>();

    private static void addToRegistry(TargetInternalReference ref){
        final Set<TargetInternalReference> targetInternalReferences = TARGET_INTERNAL_REFERENCES.get();
        if(targetInternalReferences==null){
            TARGET_INTERNAL_REFERENCES.set(new HashSet<>());
        }
        TARGET_INTERNAL_REFERENCES.get().add(ref);
    }

    public static Set<TargetInternalReference> getRegistry(){
        return TARGET_INTERNAL_REFERENCES.get();
    }

    public static void clearRegistry(){
       TARGET_INTERNAL_REFERENCES.set(null);
    }

    public static void clearNonExistingReferences(Set<String> ids){
        final Set<TargetInternalReference> targetInternalReferences = TARGET_INTERNAL_REFERENCES.get();
        if(!CollectionUtils.isEmpty(targetInternalReferences)){
            targetInternalReferences.stream().filter(t -> t.getReference()!=null && !ids.contains(t.getReference())).forEach(t -> {
                logger.warn(String.format("The reference %s for the internal reference was not found - deactivating the link", t.getReference()));
                t.setReference(null);
            });
        }
        clearRegistry();
    }

    public TargetInternalReference() {
        addToRegistry(this);
    }

    public TargetInternalReference(String reference, String value) {
        this.reference = reference;
        this.value = value;
        addToRegistry(this);
    }

    public TargetInternalReference(String reference, String value, String uuid) {
        this.reference = reference;
        this.value = value;
        this.uuid = uuid;
        addToRegistry(this);
    }

    @ElasticSearchInfo(ignoreAbove = 256)
    private String reference;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String value;

    @ElasticSearchInfo(ignoreAbove = 256)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String uuid;

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TargetInternalReference that = (TargetInternalReference) o;
        return Objects.equals(reference, that.reference) && Objects.equals(value, that.value) && Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reference, value, uuid);
    }

    @Override
    public int compareTo(TargetInternalReference targetInternalReference) {
        return targetInternalReference==null ? -1 : Comparator.comparing(TargetInternalReference::getValue).compare(this, targetInternalReference);
    }
}
