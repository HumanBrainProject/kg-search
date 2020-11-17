package eu.ebrains.kg.search.controller.mapping;

import eu.ebrains.kg.search.utils.MetaModelUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MappingControllerTest {

    @Test
    void generateMapping() {
        MappingController mappingController = new MappingController(new MetaModelUtils());
        mappingController.generateMapping();


    }
}