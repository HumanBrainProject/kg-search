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

import eu.ebrains.kg.search.model.ErrorReport;
import eu.ebrains.kg.search.model.target.elasticsearch.TargetInstance;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class TargetInstancesResult<Target> {
    private List<Target> targetInstances;
    private ErrorReport errors;
    private int from = 0;
    private int size = 0;
    private int total = 0;

    public ErrorReport getErrors() {
        return errors;
    }

    public void setErrors(ErrorReport errors) {
        this.errors = errors;
    }

    public List<Target> getTargetInstances() {
        return targetInstances;
    }

    public void setTargetInstances(List<Target> targetInstances) {
        this.targetInstances = targetInstances;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
