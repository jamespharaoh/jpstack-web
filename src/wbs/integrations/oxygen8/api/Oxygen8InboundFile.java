package wbs.integrations.oxygen8.api;

import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalValueEqualSafe;

import java.io.IOException;

import javax.inject.Provider;
import javax.servlet.ServletException;

import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.web.AbstractWebFile;
import wbs.framework.web.Action;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;

@SingletonComponent ("oxygen8InboundFile")
public
class Oxygen8InboundFile
	extends AbstractWebFile {

	// singleton dependencies

	@SingletonDependency
	RequestContext requestContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <Oxygen8InboundMmsAction> oxygen8InboundMmsActionPrototype;

	@PrototypeDependency
	Provider <Oxygen8InboundSmsAction> oxygen8InboundSmsActionPrototype;

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
				oxygen8InboundMmsActionPrototype.get ();

		} else {

			action =
				oxygen8InboundSmsActionPrototype.get ();

		}

		Responder responder =
			action.handle ();

		responder.execute ();

	}

}
