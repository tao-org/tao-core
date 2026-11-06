/*
 * Copyright (C) 2018 CS ROMANIA
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package ro.cs.tao.persistence.managers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ro.cs.tao.SortDirection;
import ro.cs.tao.Tag;
import ro.cs.tao.component.ParameterDescriptor;
import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.persistence.repository.TaggableEntityRepository;
import ro.cs.tao.utils.StringUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class TaoComponentManager<T extends TaoComponent, R extends TaggableEntityRepository<T, String>>
        extends EntityManager<T, String, R> {

    @Override
    public List<T> list(int pageNumber, int pageSize, ro.cs.tao.Sort sort) {
        if (pageNumber < 1 || pageSize < 1) return List.of();
        Page<T> page = repository.findAll(PageRequest.of(pageNumber - 1, pageSize, jpaSort(sort)));
        return page.hasContent() ? page.getContent() : List.of();
    }

    @Override
    public List<T> list(String label, int pageNumber, int pageSize, ro.cs.tao.Sort sort) {
        if (StringUtilities.isNullOrEmpty(label)) return list(pageNumber, pageSize, sort);
        if (pageNumber < 1 || pageSize < 1) return List.of();
        return repository.getByLabelLike(label, PageRequest.of(pageNumber - 1, pageSize, jpaSort(sort))).getContent();
    }

    @Override
    public List<T> list(List<Tag> tags, int pageNumber, int pageSize, ro.cs.tao.Sort sort) {
        List<String> values = tagTexts(tags);
        if (pageNumber < 1 || pageSize < 1 || values.isEmpty()) return List.of();
        return repository.getByTags(values, PageRequest.of(pageNumber - 1, pageSize, jpaSort(sort))).getContent();
    }

    @Override
    public List<T> list(String label, List<Tag> tags, int pageNumber, int pageSize, ro.cs.tao.Sort sort) {
        if (StringUtilities.isNullOrEmpty(label)) return list(tags, pageNumber, pageSize, sort);
        List<String> values = tagTexts(tags);
        if (values.isEmpty()) return list(label, pageNumber, pageSize, sort);
        if (pageNumber < 1 || pageSize < 1) return List.of();
        return repository.getByLabelLikeAndTags(label, values, PageRequest.of(pageNumber - 1, pageSize, jpaSort(sort))).getContent();
    }

    @Override
    public long count() { return repository.count(); }

    @Override
    public long count(String label, List<Tag> tags) {
        boolean hasLabel = !StringUtilities.isNullOrEmpty(label);
        List<String> values = tagTexts(tags);
        if (hasLabel && !values.isEmpty()) return repository.countByLabelLikeAndTags(label, values);
        if (hasLabel) return repository.countByLabelLike(label);
        if (!values.isEmpty()) return repository.countByTags(values);
        return count();
    }

    private List<String> tagTexts(List<Tag> tags) {
        return tags == null ? List.of() : tags.stream().filter(Objects::nonNull).map(Tag::getText)
                .filter(t -> !StringUtilities.isNullOrEmpty(t)).map(String::trim).distinct().toList();
    }

    private Sort jpaSort(ro.cs.tao.Sort sort) {
        if (sort == null || sort.getFieldsForSort() == null || sort.getFieldsForSort().isEmpty()) {
            return Sort.by(Sort.Direction.ASC, identifier());
        }
        List<Sort.Order> orders = new ArrayList<>();
        for (Map.Entry<String, SortDirection> entry : sort.getFieldsForSort().entrySet()) {
            orders.add(new Sort.Order(SortDirection.ASC.equals(entry.getValue()) ? Sort.Direction.ASC : Sort.Direction.DESC, entry.getKey()));
        }
        return Sort.by(orders);
    }

    public void updateModified(T component) throws PersistenceException {
        // To be overwritten in the managers that should update the modified column
    }

    @Override
    protected String identifier() { return "id"; }

    @Override
    protected boolean checkId(String entityId, boolean existingEntity) {
        return entityId != null && !entityId.isEmpty();
    }

    @Override
    protected boolean checkEntity(T entity, boolean existingEntity) {
        return entity != null && checkEntity(entity);
    }

    protected boolean checkComponent(TaoComponent component) {
        return component.getLabel() != null && component.getVersion() != null &&
                component.getDescription() != null && component.getAuthors() != null &&
                component.getCopyright() != null;
    }

    protected boolean checkParameterDescriptor(ParameterDescriptor parameterDesc) {
        return parameterDesc != null &&
                !StringUtilities.isNullOrEmpty(parameterDesc.getName()) &&
                parameterDesc.getType() != null && parameterDesc.getDataType() != null &&
                !StringUtilities.isNullOrEmpty(parameterDesc.getLabel());
    }
}
