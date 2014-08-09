package wbs.platform.email.logic;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
public
class EmailLogic {

	@Getter @Setter
	String smtpHostname;

	@Getter @Setter
	int smtpPort = 25;

	@Getter @Setter
	String smtpUsername;

	@Getter @Setter
	String smtpPassword;

	@Getter @Setter
	String fromAddress;

	public
	void sendEmail (
			String toAddresses,
			String subjectText,
			String messageText) {

		try {

			Session session =
				Session.getDefaultInstance (
					System.getProperties (),
					null);

			Transport transport =
				session.getTransport ("smtp");

			transport.connect (
				smtpHostname,
				smtpPort,
				smtpUsername,
				smtpPassword);

			MimeMessage mimeMessage =
				new MimeMessage (session);

			mimeMessage.setFrom (
				new InternetAddress (fromAddress));

			mimeMessage.setSubject (
				subjectText);

			mimeMessage.setRecipients (
				Message.RecipientType.TO,
				new Address [] {
					new InternetAddress (toAddresses)
				});

			mimeMessage.setText (
				messageText,
				"utf-8");

			Transport.send (
				mimeMessage);

		} catch (MessagingException exception) {

			throw new RuntimeException (exception);

		}

	}

}
