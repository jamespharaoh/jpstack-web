package wbs.framework.utils;

import java.util.Collection;

public
interface EmailLogic {

	void sendEmail (
			String fromName,
			String fromAddress,
			String replyToAddress,
			Collection<String> toAddresses,
			String subject,
			String message);

	void sendSystemEmail (
			Collection<String> toAddresses,
			String subject,
			String message);

}
