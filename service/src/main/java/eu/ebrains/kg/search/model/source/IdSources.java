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

package eu.ebrains.kg.search.model.source;

import eu.ebrains.kg.search.model.source.openMINDSv1.DatasetV1;
import eu.ebrains.kg.search.model.source.openMINDSv3.DatasetVersionV3;

public class IdSources {
    private String idV1;
    private String idV2;
    private String idV3;

    public String getIdV1() {
        return idV1;
    }

    public void setIdV1(String idV1) {
        this.idV1 = idV1;
    }

    public String getIdV2() {
        return idV2;
    }

    public void setIdV2(String idV2) {
        this.idV2 = idV2;
    }

    public String getIdV3() {
        return idV3;
    }

    public void setIdV3(String idV3) {
        this.idV3 = idV3;
    }
}
