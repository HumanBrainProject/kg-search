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

package eu.ebrains.kg.search.model.source.commonsV1andV2;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ListOrSingleStringAsListDeserializer extends JsonDeserializer<List> {

    @Override
    public List<?> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        final TreeNode treeNode = jsonParser.readValueAsTree();
        if(treeNode.isArray()){
            List<?> list = jsonParser.getCodec().treeToValue(treeNode, List.class);
            if(CollectionUtils.isEmpty(list)){
                return null;
            }
            else {
                return list;
            }
        }
        else{
            String s = jsonParser.getCodec().treeToValue(treeNode, String.class);
            if(StringUtils.isNotBlank(s)) {
                return Collections.singletonList(s);
            }
            return null;
        }
    }
}