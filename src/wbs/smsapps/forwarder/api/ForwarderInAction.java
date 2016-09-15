package wbs.smsapps.forwarder.api;

import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;

import wbs.api.mvc.ApiAction;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.platform.text.web.TextResponder;
import wbs.smsapps.forwarder.logic.ForwarderNotFoundException;
import wbs.smsapps.forwarder.logic.IncorrectPasswordException;
import wbs.smsapps.forwarder.logic.ReportableException;
import wbs.smsapps.forwarder.model.ForwarderRec;

@Log4j
@PrototypeComponent ("forwarderInAction")
public
class ForwarderInAction
	extends ApiAction {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ForwarderApiLogic forwarderApiLogic;

	@SingletonDependency
	RequestContext requestContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <TextResponder> textResponderProvider;

	// implementation

	@Override
	protected
	Responder goApi () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ForwarderInAction.goApi ()",
				this);

		try {

			String slice =
				requestContext.parameterOrNull ("slice");

			String code =
				requestContext.parameterOrNull ("code");

			String password =
				requestContext.parameterOrNull ("password");

			String action =
				requestContext.parameterOrNull ("action");

			if (
				code == null
				|| password == null
				|| action == null
			) {

				throw new ReportableException (
					"Invalid parameters supplied");

			}

			ForwarderRec forwarder;

			try {

				forwarder =
					forwarderApiLogic.lookupForwarder (
						requestContext,
						slice,
						code,
						password);

			} catch (ForwarderNotFoundException exception) {

				throw new ReportableException (
					"Forwarder not found: " + code);

			} catch (IncorrectPasswordException exception) {

				throw new ReportableException (
					"Password incorrect");

			}

			transaction.commit ();

			if (action.equalsIgnoreCase ("get")) {

				return forwarderApiLogic.controlActionGet (
					requestContext,
					forwarder);

			} else if (action.equalsIgnoreCase ("borrow")) {

				return forwarderApiLogic.controlActionBorrow (
					requestContext,
					forwarder);

			} else if (action.equalsIgnoreCase ("unqueue")) {

				return forwarderApiLogic.controlActionUnqueue (
					requestContext,
					forwarder);

			} else {

				throw new ReportableException (
					"Unknown action: " + action);

			}

		} catch (ReportableException exception) {

			log.error ("Error doing 'in': " + exception.getMessage ());

			for (Map.Entry<String,List<String>> entry
					: requestContext.parameterMap ().entrySet ()) {

				String name =
					entry.getKey ();

				List<String> values =
					entry.getValue ();

				for (String value : values)
					log.error ("Param " + name + ": " + value);

			}

			return textResponderProvider.get ()
				.text ("ERROR\n" + exception.getMessage () + "\n");

		}

	}

}
