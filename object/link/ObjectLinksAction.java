package wbs.platform.object.link;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.BeanLogic;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.helper.ConsoleHelper;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.priv.console.PrivChecker;
import wbs.platform.updatelog.logic.UpdateManager;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

@Accessors (fluent = true)
@PrototypeComponent ("objectLinksAction")
public
class ObjectLinksAction
	extends ConsoleAction {

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	EventLogic eventLogic;

	@Inject
	PrivChecker privChecker;

	@Inject
	UpdateManager updateManager;

	@Inject
	UserObjectHelper userHelper;

	@Getter @Setter
	String responderName;

	@Getter @Setter
	ConsoleHelper<?> contextHelper;

	@Getter @Setter
	String contextLinkField;

	@Getter @Setter
	ConsoleHelper<?> targetHelper;

	@Getter @Setter
	String targetLinkField;

	@Getter @Setter
	String addEventName;

	@Getter @Setter
	String removeEventName;

	@Getter @Setter
	EventOrder eventOrder;

	@Getter @Setter
	String contextUpdateSignalName;

	@Getter @Setter
	String targetUpdateSignalName;

	@Getter @Setter
	String successNotice;

	public static
	enum EventOrder {
		contextThenTarget,
		targetThenContext;
	}

	static
	Pattern oldGroupPattern =
		Pattern.compile ("(\\d+),(true|false)");

	@Override
	public
	Responder goReal () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		Record<?> contextObject =
			contextHelper.lookupObject (
				requestContext.contextStuff ());

		@SuppressWarnings ("unchecked")
		Set<Record<?>> contextLinks =
			(Set<Record<?>>)
			BeanLogic.getProperty (
				contextObject,
				contextLinkField);

		List<String> params =
			requestContext.getParameterValues ("old_link");

		List<Record<?>> updatedTargetObjects =
			new ArrayList<Record<?>> ();

		for (String param
				: params) {

			Matcher matcher =
				oldGroupPattern.matcher (param);

			if (! matcher.matches ()) {

				requestContext.addError (
					"Internal error");

				return null;

			}

			int linkId =
				Integer.parseInt (
					matcher.group (1));

			boolean oldIsMember =
				matcher.group (2).equals ("true");

			boolean newIsMember =
				requestContext.parameter ("link_" + linkId) != null;

			if (oldIsMember == newIsMember)
				continue;

			Record<?> targetObject =
				targetHelper.find (linkId);

			//@SuppressWarnings ("unchecked")
			//Set<Record<?>> targetLinks =
			//	(Set<Record<?>>)
			//	BeanLogic.getProperty (
			//		targetObject,
			//		targetLinkField);

			if (! privChecker.can (
					targetObject,
					"manage"))
				continue;

			if (newIsMember
					&& ! contextLinks.contains (targetObject)) {

				contextLinks.add (
					targetObject);

				// TODO fix this properly
				//targetLinks.add (
				//	contextObject);

				if (eventOrder == EventOrder.contextThenTarget) {

					eventLogic.createEvent (
						addEventName,
						myUser,
						contextObject,
						targetObject);

				}

				if (eventOrder == EventOrder.targetThenContext) {

					eventLogic.createEvent (
						addEventName,
						myUser,
						targetObject,
						contextObject);

				}

			}

			if (oldIsMember
					&& contextLinks.contains (targetObject)) {

				contextLinks.remove (
					targetObject);

				// TODO fix this properly
				//targetLinks.remove (
				//	contextObject);

				if (eventOrder == EventOrder.contextThenTarget) {

					eventLogic.createEvent (
						removeEventName,
						myUser,
						contextObject,
						targetObject);

				}

				if (eventOrder == EventOrder.targetThenContext) {

					eventLogic.createEvent (
						removeEventName,
						myUser,
						targetObject,
						contextObject);

				}

			}

			updatedTargetObjects.add (
				targetObject);

		}

		if (! updatedTargetObjects.isEmpty ()) {

			if (contextUpdateSignalName != null) {

				updateManager.signalUpdate (
					contextUpdateSignalName,
					contextObject.getId ());

			}

			if (targetUpdateSignalName != null) {

				for (Record<?> targetObject
						: updatedTargetObjects) {

					updateManager.signalUpdate (
						targetUpdateSignalName,
						targetObject.getId ());

				}

			}

		}

		transaction.commit ();

		if (! updatedTargetObjects.isEmpty ()) {

			requestContext.addNotice (
				successNotice);

		}

		return null;

	}

	@Override
	protected
	Responder backupResponder () {
		return responder (responderName);
	}

}
