package org.jeo.map.render;

public interface Labeller {

    static Labeller NULL = new Labeller() {
        @Override
        public void render(Label label) {
        }
        @Override
        public boolean layout(Label label, LabelIndex labels) {
            return false;
        }
    };

    boolean layout(Label label, LabelIndex labels);

    void render(Label label);
}
