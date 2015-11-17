package wbs.framework.utils;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Properties;

import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import lombok.NonNull;
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
			@NonNull String fromName,
			@NonNull String fromAddress,
			@NonNull String replyToAddress,
			@NonNull Collection<String> toAddresses,
			@NonNull String subjectText,
			@NonNull String messageText) {

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
				new MimeMessage (
					session);

			try {

				mimeMessage.setFrom (
					new InternetAddress (
						fromAddress,
						fromName));

			} catch (UnsupportedEncodingException exception) {

				throw new RuntimeException (
					exception);

			}

			mimeMessage.setSubject (
				subjectText);

			for (
				String toAddress
					: toAddresses
			) {

				mimeMessage.addRecipient (
					Message.RecipientType.TO,
					new InternetAddress (
						toAddress,
						true));

			}

			mimeMessage.setText (
				messageText,
				"utf-8");

			Transport.send (
				mimeMessage);

		} catch (MessagingException exception) {

			throw new RuntimeException (exception);

		}

	}

	@Override
	public
	void sendSystemEmail (
			@NonNull Collection<String> toAddresses,
			@NonNull String subject,
			@NonNull String message) {

		sendEmail (
			wbsConfig.defaultEmailFromName (),
			wbsConfig.defaultEmailFromAddress (),
			wbsConfig.defaultEmailReplyToAddress (),
			toAddresses,
			subject,
			message);

	}

}
