package eu.ebrains.kg.search.model.target.elasticsearch.instances.commons;

public class Children<T> {

        public Children() {
        }

        public Children(T children) {
            this.children = children;
        }

        private T children;

        public T getChildren() {
            return children;
        }

}
