package eu.ebrains.kg.search.utils;

public class AmbiguousDataException extends TranslationException{
    public AmbiguousDataException(String message, String identifier) {
        super(message, identifier);
    }
}
