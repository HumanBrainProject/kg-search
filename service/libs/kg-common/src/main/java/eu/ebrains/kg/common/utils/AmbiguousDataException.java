package eu.ebrains.kg.common.utils;

public class AmbiguousDataException extends TranslationException{
    public AmbiguousDataException(String message, String identifier) {
        super(message, identifier);
    }
}
