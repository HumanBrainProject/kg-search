package eu.ebrains.kg.search.controller.mergers;

public abstract class Merger<T> {

    public T merge(T v1, T v2, T v3){
        if(v3 != null){
            if(v1 != null){
                merge(v3, v1);
            }
            if(v2 != null){
                merge(v3, v2);
            }
            return v3;
        }
        if(v2!=null){
            if(v1 != null){
                merge(v2, v1);
            }
            return v2;
        }
        return v1;
    }

    protected abstract void merge(T parent, T child);


}
