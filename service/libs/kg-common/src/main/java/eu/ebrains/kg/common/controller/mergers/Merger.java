package eu.ebrains.kg.common.controller.mergers;

import java.util.List;

public abstract class Merger<T> {

    public T merge(List<T> v1, List<T> v2, T v3){
        T simpleV1 = null;
        if(v1!=null){
            for (T v : v1) {
                if(simpleV1 == null){
                    simpleV1 = v;
                }
                else{
                    merge(simpleV1, v);
                }
            }
        }

        T simpleV2 = null;
        if(v2!=null){
            for (T v : v2) {
                if(simpleV2 == null){
                    simpleV2 = v;
                }
                else{
                    merge(simpleV2, v);
                }
            }
        }


        if(v3 != null){
            if(simpleV1 != null){
                merge(v3, simpleV1);
            }
            if(simpleV2 != null){
                merge(v3, simpleV2);
            }
            return v3;
        }
        if(simpleV2!=null){
            if(simpleV1 != null){
                merge(simpleV2, simpleV1);
            }
            return simpleV2;
        }
        return simpleV1;
    }

    protected abstract void merge(T parent, T child);


}
