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

package eu.ebrains.kg.search.api;

import eu.ebrains.kg.common.services.DOICitationFormatter;
import eu.ebrains.kg.search.security.UserRoles;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RequestMapping(value = "/internal", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
@SuppressWarnings("java:S1452") // we keep the generics intentionally
public class Internal {
    private final DOICitationFormatter doiCitationFormatter;

    public Internal(DOICitationFormatter doiCitationFormatter) {
        this.doiCitationFormatter = doiCitationFormatter;
    }

    @PostMapping("/doiCitations")
    @UserRoles.MustBeAdmin
    public ResponseEntity<String> refreshDOICitation(@RequestParam("doi") String doi, @RequestParam(value = "style", defaultValue = "apa") String style, @RequestParam(value = "contentType", defaultValue = "text/x-bibliography") String contentType, Principal principal) {
        return ResponseEntity.ok(this.doiCitationFormatter.refreshDOICitation(doi, style, contentType));
    }

    @PostMapping("/evictDoiCitations")
    @UserRoles.MustBeAdmin
    public ResponseEntity<Void> evictDoiCitations() {
        this.doiCitationFormatter.evictAll();
        return ResponseEntity.ok().build();
    }
}
