package ro.cs.tao.builders;

import ro.cs.tao.component.StringIdentifiable;

public abstract class AggregatedBuilder<T extends StringIdentifiable, B extends AbstractBuilder> extends AbstractBuilder<T>{
    protected B parent;

    public AggregatedBuilder(Class<T> clazz, B parent) {
        super(clazz);
        this.parent = parent;
    }

    public B and() {
        build();
        return this.parent;
    }

    @Override
    public T build() {
        T builtEntity = super.build();
        return addToParent(builtEntity);
    }

    protected abstract T addToParent(T builtEntity);
}
