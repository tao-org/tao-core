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
package ro.cs.tao.utils.mail;

import ro.cs.tao.configuration.ConfigurationManager;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class MailSender {

    private static final Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$");
    private static final Logger logger = Logger.getLogger(MailSender.class.getName());
    private String mailSmtpAuth;
    private String mailSmtpStartTlsEnable;
    private String mailSmptpHost;
    private String mailSmtpPort;
    private String mailFrom;
    private String mailTo;
    private String mailUsername;
    private String mailPassword;

    public static void main (String[] args) {
        MailSender sender = new MailSender();
        sender.sendMail("Test", "Test");
    }

    public MailSender() {
        ConfigurationManager configManager = ConfigurationManager.getInstance();
        this.mailSmtpAuth = configManager.getValue("mail.smtp.auth");
        this.mailSmtpStartTlsEnable = configManager.getValue("mail.smtp.starttls.enable");
        this.mailSmptpHost = configManager.getValue("mail.smtp.host");
        this.mailSmtpPort = configManager.getValue("mail.smtp.port");
        this.mailFrom = configManager.getValue("mail.from");
        this.mailTo = configManager.getValue("mail.to");
        this.mailUsername = configManager.getValue("mail.smtp.username");
        this.mailPassword = configManager.getValue("mail.smtp.password");
    }

    public MailSender(String host, int port, boolean auth, String user, String password,
                      boolean startTLS, String sender, String recipient) {
        this.mailSmtpAuth = String.valueOf(auth);
        this.mailSmtpStartTlsEnable = String.valueOf(startTLS);
        if (host == null || host.isEmpty()) {
            throw new IllegalArgumentException("Invalid mail host");
        }
        this.mailSmptpHost = host;
        if (port == 0) {
            throw new IllegalArgumentException("Invalid mail port");
        }
        this.mailSmtpPort = String.valueOf(port);
        if (auth && (user == null || user.isEmpty() || password == null || password.isEmpty())) {
            throw new IllegalArgumentException("Mail authentication is required, but the provided credentials are invalid");
        }
        this.mailUsername = user;
        this.mailPassword = password;
        if (sender == null) {
            String appName = ConfigurationManager.getInstance().getValue("spring.application.name",
                                                                         "TAO Services");
            sender = "sysadm@" + appName.toLowerCase().replaceAll(" ", "_") + ".org";
        }
        this.mailFrom = sender;
        if (recipient == null || !emailPattern.matcher(recipient).matches()) {
            throw new IllegalArgumentException(String.format("Invalid email recipient [%s]",
                                                             recipient == null ? "null" : recipient));
        }
        this.mailTo = recipient;
    }

    public void sendMail(String subject, String message) {
        if (this.mailTo == null || this.mailTo.isEmpty()) {
            throw new RuntimeException("Mail recipient not configured");
        }
        sendMail(mailTo, subject, message, null);
    }

    public void sendMail(String subject, String message, Path attachment) {
        if (this.mailTo == null || this.mailTo.isEmpty()) {
            throw new RuntimeException("Mail recipient not configured");
        }
        sendMail(mailTo, subject, message, attachment);
    }

    public void sendMail(String toAddress, String subject, String content, Path file) {
        if (!canSend()) {
            throw new RuntimeException("Mail not configured");
        }
        Properties props = new Properties();
        props.put("mail.smtp.auth", mailSmtpAuth);
        props.put("mail.smtp.starttls.enable", mailSmtpStartTlsEnable);
        props.put("mail.smtp.host", mailSmptpHost);
        props.put("mail.smtp.port", mailSmtpPort);

        Session session = Session.getInstance(props,
          new javax.mail.Authenticator() {
              protected PasswordAuthentication getPasswordAuthentication() {
                  return new PasswordAuthentication(mailUsername, mailPassword);
              }
          });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mailFrom));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
            message.setSubject(subject);
            BodyPart bodyPart = new MimeBodyPart();
            bodyPart.setText(content);
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(bodyPart);
            if (file != null && Files.exists(file)) {
                bodyPart = new MimeBodyPart();
                DataSource dataSource = new FileDataSource(file.toFile());
                bodyPart.setDataHandler(new DataHandler(dataSource));
                bodyPart.setFileName(file.getFileName().toString());
                multipart.addBodyPart(bodyPart);
            }
            message.setContent(multipart);
            Transport.send(message);
            logger.finest("Email sent to " + toAddress);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean canSend() {
        return this.mailSmtpAuth != null && !this.mailSmtpAuth.isEmpty() &&
                this.mailSmtpStartTlsEnable != null && !this.mailSmtpStartTlsEnable.isEmpty() &&
                this.mailSmptpHost != null && !this.mailSmptpHost.isEmpty() &&
                this.mailSmtpPort != null && !this.mailSmtpPort.isEmpty() &&
                this.mailFrom != null && !this.mailFrom.isEmpty() &&
                this.mailUsername != null && !this.mailUsername.isEmpty() &&
                this.mailPassword != null && !this.mailPassword.isEmpty();
    }
}
