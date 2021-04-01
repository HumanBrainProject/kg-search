package eu.ebrains.kg.search.controller.translators;

import org.apache.commons.lang3.StringUtils;

public class Helpers {
    public static String getFullName(String familyName, String givenName) {
        if(familyName == null && givenName == null) {
            return  null;
        }
        if(familyName != null && givenName == null) {
            return familyName;
        }
        if(familyName == null) {
            return givenName;
        }
        return String.format("%s, %s", familyName, givenName);
    }

    public static String getFullName(String fullName, String familyName, String givenName) {
        if (StringUtils.isNotBlank(fullName)) {
            return  fullName;
        }
        return getFullName(familyName, givenName);
    }
}
