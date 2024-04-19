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

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.messaging.Message;
import ro.cs.tao.persistence.MessageProvider;
import ro.cs.tao.persistence.repository.MessageRepository;

import java.util.List;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
@Component("notificationManager")
public class NotificationManager extends EntityManager<Message, Long, MessageRepository> implements MessageProvider {

    @Override
    public List<Message> getUserMessages(String userId, Integer pageNumber) {
        PageRequest pageRequest = PageRequest.of(pageNumber - 1, Constants.MESSAGES_PAGE_SIZE,
                                                  Sort.Direction.DESC, Constants.MESSAGE_TIMESTAMP_PROPERTY_NAME);
        return repository.findByUserId(userId, pageRequest).getContent();
    }

    @Override
    public List<Message> getUnreadMessages(String userId) {
        return repository.getUnreadMessages(userId);
    }

    @Override
    public Message get(String userId, long timestamp) {
        return repository.get(userId, timestamp);
    }

    @Override
    public void acknowledge(List<Long> messageIds, String userId) {
        repository.markAsRead(messageIds, userId);
    }

    @Override
    public void clear(String userId) {
        repository.deleteAll(userId);
    }

    @Override
    protected String identifier() { return "id"; }

    @Override
    protected boolean checkEntity(Message entity) {
        return entity != null && entity.getTimestamp() != 0 && entity.getData() != null;
    }

    @Override
    protected boolean checkEntity(Message entity, boolean existingEntity) {
        return checkEntity(entity);
    }

    @Override
    protected boolean checkId(Long entityId, boolean existingEntity) {
        return (!existingEntity && (entityId == null || entityId.equals(0L))) || (existingEntity && get(entityId) != null);
    }

}
