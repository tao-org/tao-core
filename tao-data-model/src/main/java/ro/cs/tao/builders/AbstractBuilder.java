package ro.cs.tao.builders;

import ro.cs.tao.component.StringIdentifiable;

import java.util.UUID;

public abstract class AbstractBuilder<T extends StringIdentifiable> {
    protected final T entity;

    public AbstractBuilder(Class<T> clazz) {
        try {
            this.entity = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public T build() {
        if (this.entity.getId() == null) {
            this.entity.setId(UUID.randomUUID().toString());
        }
        return this.entity;
    }
}
