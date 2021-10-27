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

import eu.ebrains.kg.search.model.source.openMINDSv1.PersonV1;
import eu.ebrains.kg.search.model.source.openMINDSv2.PersonV2;
import eu.ebrains.kg.search.model.source.openMINDSv3.PersonOrOrganizationV3;

public class PersonSources {

    private PersonV1 personV1;
    private PersonV2 personV2;
    private PersonOrOrganizationV3 personOrOrganizationV3;

    public void setPersonV1(PersonV1 personV1) {
        this.personV1 = personV1;
    }

    public void setPersonV2(PersonV2 personV2) {
        this.personV2 = personV2;
    }

    public void setPersonV3(PersonOrOrganizationV3 personOrOrganizationV3) {
        this.personOrOrganizationV3 = personOrOrganizationV3;
    }

    public PersonV1 getPersonV1() {
        return personV1;
    }

    public PersonV2 getPersonV2() {
        return personV2;
    }

    public PersonOrOrganizationV3 getPersonV3() {
        return personOrOrganizationV3;
    }
}
