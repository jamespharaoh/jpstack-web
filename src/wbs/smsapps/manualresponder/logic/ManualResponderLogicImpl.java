package wbs.smsapps.manualresponder.logic;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.sms.gsm.Gsm;
import wbs.smsapps.manualresponder.model.ManualResponderRequestRec;
import wbs.smsapps.manualresponder.model.ManualResponderTemplateRec;

@SingletonComponent ("manualResponderLogic")
public
class ManualResponderLogicImpl
	implements ManualResponderLogic {

	@Override
	public
	int maximumMessageLength (
			ManualResponderRequestRec request,
			ManualResponderTemplateRec template) {

		// work out real maximum

		int maximumMessageParts =
			template.getMaximumMessages ();

		boolean shortMessageParts =
			request
				.getNumber ()
				.getNetwork ()
				.getShortMultipartMessages ();

		int maxLengthPerMultipartMessage =
			shortMessageParts
				? 134
				: 153;

		int maxLength =
			maximumMessageParts > 1
				? maxLengthPerMultipartMessage * maximumMessageParts
				: 160;

		// removed length of fixed string

		if (
			template.getApplyTemplates ()
		) {

			String fixedText =
				template.getSingleTemplate ().replaceAll (
					"\\{message\\}",
					"");

			int fixedLength =
				Gsm.length (
					fixedText);

			maxLength -=
				fixedLength;

		}

		// and return

		return maxLength;

	}

	@Override
	public
	int minimumMessageLength (
			ManualResponderRequestRec request,
			ManualResponderTemplateRec template) {

		boolean shortMessageParts =
			request
				.getNumber ()
				.getNetwork ()
				.getShortMultipartMessages ();

		int maxLengthPerMultipartMessage =
			shortMessageParts
				? 134
				: 153;

		int minLength =
			maxLengthPerMultipartMessage * template.getMinimumMessageParts ();

		// added length of fixed string

		if (
			template.getApplyTemplates ()
		) {

			String fixedText =
				template.getSingleTemplate ().replaceAll (
					"\\{message\\}",
					"");

			int fixedLength =
				Gsm.length (
					fixedText);

			minLength +=
				fixedLength;

		}

		return minLength;

	}

}
