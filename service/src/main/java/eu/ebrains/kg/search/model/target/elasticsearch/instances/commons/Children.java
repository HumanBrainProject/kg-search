package eu.ebrains.kg.search.model.target.elasticsearch.instances.commons;

import eu.ebrains.kg.search.model.target.elasticsearch.FieldInfo;

public class Children<T> {

        public Children() {
        }

        public Children(T children) {
            this.children = children;
        }

        @FieldInfo
        private T children;

        public T getChildren() {
            return children;
        }

}
