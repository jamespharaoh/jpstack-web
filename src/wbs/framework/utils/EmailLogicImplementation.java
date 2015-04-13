package wbs.framework.utils;

import java.util.Properties;

import javax.inject.Inject;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import lombok.experimental.Accessors;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.config.WbsConfig;

@SingletonComponent ("emailLogic")
@Accessors (fluent = true)
public
class EmailLogicImplementation
	implements EmailLogic {

	// dependencies

	@Inject
	WbsConfig wbsConfig;

	// implementation

	@Override
	public
	void sendEmail (
			String fromAddress,
			String toAddresses,
			String subjectText,
			String messageText) {

		try {

			Properties properties =
				System.getProperties ();

			properties.setProperty (
				"mail.smtp.host",
				wbsConfig.smtpHostname ());

			Session session =
				Session.getDefaultInstance (
						properties);

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
