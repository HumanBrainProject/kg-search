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

package eu.ebrains.kg.search.model.source.openMINDSv3;

import eu.ebrains.kg.search.model.source.openMINDSv3.commons.HasEmbargo;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.SourceInternalReference;

import java.util.List;

public class FileRepositoryV3 extends SourceInstanceV3 {
    private String IRI;
    private FileRepositoryOfReference fileRepositoryOf;

    public String getIRI() { return IRI; }

    public void setIRI(String IRI) { this.IRI = IRI; }

    public FileRepositoryOfReference getFileRepositoryOf() {
        return fileRepositoryOf;
    }

    public void setFileRepositoryOf(FileRepositoryOfReference fileRepositoryOf) {
        this.fileRepositoryOf = fileRepositoryOf;
    }

    public static class FileRepositoryOfReference extends SourceInternalReference implements HasEmbargo {
        private List<String> type;
        private List<String> embargo;
        private boolean useHDG;

        public List<String> getType() {
            return type;
        }

        public void setType(List<String> type) {
            this.type = type;
        }

        public List<String> getEmbargo() {
            return embargo;
        }

        public void setEmbargo(List<String> embargo) {
            this.embargo = embargo;
        }

        public boolean isUseHDG() {
            return useHDG;
        }

        public void setUseHDG(boolean useHDG) {
            this.useHDG = useHDG;
        }

    }
}
