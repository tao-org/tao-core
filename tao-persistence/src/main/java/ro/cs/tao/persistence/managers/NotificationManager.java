/*
 * Copyright (C) 2017 CS ROMANIA
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.messaging.Message;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.persistence.repository.MessageRepository;

import java.util.logging.Logger;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
@Component("notificationManager")
public class NotificationManager {

    private Logger logger = Logger.getLogger(NotificationManager.class.getName());

    /** CRUD Repository for Mesaage entities */
    @Autowired
    private MessageRepository messageRepository;

    @Transactional
    public Message saveMessage(Message message) throws PersistenceException {
        // check method parameters
        if(!checkMessage(message)) {
            throw new PersistenceException("Invalid parameters were provided for adding new message !");
        }

        // save the new Message entity and return it
        return messageRepository.save(message);
    }

    @Transactional
    public Page<Message> getUserMessages(String user, Integer pageNumber)
    {
        PageRequest pageRequest = new PageRequest(pageNumber - 1, Constants.MESSAGES_PAGE_SIZE,
                                                  Sort.Direction.DESC, Constants.MESSAGE_TIMESTAMP_PROPERTY_NAME);
        return messageRepository.findByUser(user, pageRequest);
    }

    private boolean checkMessage(Message message) {
        return message != null && message.getTimestamp() != 0 && message.getData() != null;
    }
}
