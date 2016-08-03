package wbs.framework.utils;

import static wbs.framework.utils.etc.StringUtils.isNotEmptyString;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;

import lombok.NonNull;
import lombok.experimental.Accessors;

import com.sun.mail.smtp.SMTPMessage;

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

	// state

	Session mailSession;

	// life cycle

	@PostConstruct
	void setup () {

		Properties properties =
			new Properties (
				System.getProperties ());

		if (
			isNotEmptyString (
				wbsConfig.smtpHostname ())
		) {

			properties.setProperty (
				"mail.smtp.host",
				wbsConfig.smtpHostname ());

		}

		if (
			isNotEmptyString (
				wbsConfig.smtpPort ())
		) {

			properties.setProperty (
				"mail.smtp.port",
				wbsConfig.smtpPort ());

		}

		if (
			isNotEmptyString (
				wbsConfig.smtpUsername ())
		) {

			throw new RuntimeException ();

		}

		if (
			isNotEmptyString (
				wbsConfig.smtpPassword ())
		) {

			throw new RuntimeException ();

		}

		mailSession =
			Session.getInstance (
				properties);

	}

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

			SMTPMessage smtpMessage =
				new SMTPMessage (
					mailSession);

			smtpMessage.setEnvelopeFrom (
				wbsConfig.defaultEmailEnvelopeFrom ());

			try {

				smtpMessage.setFrom (
					new InternetAddress (
						fromAddress,
						fromName));

			} catch (UnsupportedEncodingException exception) {

				throw new RuntimeException (
					exception);

			}

			smtpMessage.setSubject (
				subjectText);

			for (
				String toAddress
					: toAddresses
			) {

				smtpMessage.addRecipient (
					Message.RecipientType.TO,
					new InternetAddress (
						toAddress,
						true));

			}

			smtpMessage.setText (
				messageText,
				"utf-8");

			Transport.send (
				smtpMessage);

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
