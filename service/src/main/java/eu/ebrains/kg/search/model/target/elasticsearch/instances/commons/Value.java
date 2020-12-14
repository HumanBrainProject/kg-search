package eu.ebrains.kg.search.model.target.elasticsearch.instances.commons;

public class Value<T>{

    public Value() {
    }

    public Value(T value) {
        this.value = value;
    }

    private T value;

    public T getValue() {
        return value;
    }
}
