package eu.ebrains.kg.search.controller.indexing;

import eu.ebrains.kg.search.model.DataStage;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

@Component
public class IdRegistry {

    @Cacheable(value="internalIds",  unless="#result == null", key="#stage.name().concat(#type)")
    public Set<String> getIds(DataStage stage, String type){
        return Collections.emptySet();
    }

    @CachePut(value="internalIds",  unless="#result == null", key="#stage.name().concat(#type)")
    public Set<String> addIds(DataStage stage, String type, Set<String> ids){
        return ids;
    }




}
