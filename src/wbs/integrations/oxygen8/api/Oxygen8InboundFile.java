package wbs.integrations.oxygen8.api;

import static wbs.framework.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.framework.utils.etc.OptionalUtils.optionalValueEqualSafe;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.ServletException;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.web.AbstractWebFile;
import wbs.framework.web.Action;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;

@SingletonComponent ("oxygen8InboundFile")
public
class Oxygen8InboundFile
	extends AbstractWebFile {

	// dependencies

	@Inject
	Provider <Oxygen8InboundMmsAction> oxygen8InboundMmsAction;

	@Inject
	Provider <Oxygen8InboundSmsAction> oxygen8InboundSmsAction;

	@Inject
	RequestContext requestContext;

	// implementation

	@Override
	public
	void doPost ()
		throws
			IOException,
			ServletException {

		// detect request type

		Action action;

		if (
			optionalValueEqualSafe (
				optionalFromNullable (
					requestContext.header (
						"X-Mms-Message-Type")),
				"MO_MMS")
		) {

			action =
				oxygen8InboundMmsAction.get ();

		} else {

			action =
				oxygen8InboundSmsAction.get ();

		}

		Responder responder =
			action.handle ();

		responder.execute ();

	}

}
