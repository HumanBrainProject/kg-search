package eu.ebrains.kg.search.model.source;

import eu.ebrains.kg.search.model.source.openMINDSv1.PersonV1;
import eu.ebrains.kg.search.model.source.openMINDSv2.PersonV2;
import eu.ebrains.kg.search.model.source.openMINDSv3.PersonV3;

public class PersonSources {

    private PersonV1 personV1;
    private PersonV2 personV2;
    private PersonV3 personV3;

    public void setPersonV1(PersonV1 personV1) {
        this.personV1 = personV1;
    }

    public void setPersonV2(PersonV2 personV2) {
        this.personV2 = personV2;
    }

    public void setPersonV3(PersonV3 personV3) {
        this.personV3 = personV3;
    }

    public PersonV1 getPersonV1() {
        return personV1;
    }

    public PersonV2 getPersonV2() {
        return personV2;
    }

    public PersonV3 getPersonV3() {
        return personV3;
    }
}
