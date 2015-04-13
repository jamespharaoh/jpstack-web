package wbs.framework.utils;

public
interface EmailLogic {

	void sendEmail (
			String fromAddress,
			String toAddresses,
			String subjectText,
			String messageText);

}
