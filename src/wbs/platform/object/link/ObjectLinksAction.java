package wbs.platform.object.link;

import static wbs.framework.utils.etc.Misc.contains;
import static wbs.framework.utils.etc.Misc.doesNotContain;
import static wbs.framework.utils.etc.Misc.isNotEmpty;
import static wbs.framework.utils.etc.Misc.isNotNull;

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

import wbs.console.action.ConsoleAction;
import wbs.console.helper.ConsoleHelper;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.BeanLogic;
import static wbs.framework.utils.etc.OptionalUtils.isPresent;
import wbs.framework.web.Responder;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.updatelog.logic.UpdateManager;
import wbs.platform.user.console.UserConsoleLogic;

@Accessors (fluent = true)
@PrototypeComponent ("objectLinksAction")
public
class ObjectLinksAction
	extends ConsoleAction {

	// dependencies

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	EventLogic eventLogic;

	@Inject
	UserPrivChecker privChecker;

	@Inject
	UpdateManager updateManager;

	@Inject
	UserConsoleLogic userConsoleLogic;

	// properties

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

	// details

	public static
	enum EventOrder {
		contextThenTarget,
		targetThenContext;
	}

	static
	Pattern oldGroupPattern =
		Pattern.compile ("(\\d+),(true|false)");

	// implementation

	@Override
	public
	Responder goReal () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ObjectLinksAction.goReal ()",
				this);

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

		for (
			String param
				: params
		) {

			Matcher matcher =
				oldGroupPattern.matcher (
					param);

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
				isPresent (
					requestContext.parameter (
						"link_" + linkId));

			if (oldIsMember == newIsMember)
				continue;

			Record<?> targetObject =
				targetHelper.findRequired (
					linkId);

			@SuppressWarnings ("unchecked")
			Set<Record<?>> targetLinks =
				(Set<Record<?>>)
				BeanLogic.getProperty (
					targetObject,
					targetLinkField);

			if (
				! privChecker.canRecursive (
					targetObject,
					"manage")
			) {
				continue;
			}

			if (

				newIsMember

				&& doesNotContain (
					contextLinks,
					targetObject)

			) {

				contextLinks.add (
					targetObject);

				targetLinks.add (
					contextObject);

				if (eventOrder == EventOrder.contextThenTarget) {

					eventLogic.createEvent (
						addEventName,
						userConsoleLogic.userRequired (),
						contextObject,
						targetObject);

				}

				if (eventOrder == EventOrder.targetThenContext) {

					eventLogic.createEvent (
						addEventName,
						userConsoleLogic.userRequired (),
						targetObject,
						contextObject);

				}

			}

			if (

				oldIsMember

				&& contains (
					contextLinks,
					targetObject)

			) {

				contextLinks.remove (
					targetObject);

				targetLinks.remove (
					contextObject);

				if (eventOrder == EventOrder.contextThenTarget) {

					eventLogic.createEvent (
						removeEventName,
						userConsoleLogic.userRequired (),
						contextObject,
						targetObject);

				}

				if (eventOrder == EventOrder.targetThenContext) {

					eventLogic.createEvent (
						removeEventName,
						userConsoleLogic.userRequired (),
						targetObject,
						contextObject);

				}

			}

			updatedTargetObjects.add (
				targetObject);

		}

		if (
			isNotEmpty (
				updatedTargetObjects)
		) {

			if (
				isNotNull (
					contextUpdateSignalName)
			) {

				updateManager.signalUpdate (
					contextUpdateSignalName,
					contextObject.getId ());

			}

			if (
				isNotNull (
					targetUpdateSignalName)
			) {

				for (
					Record<?> targetObject
						: updatedTargetObjects
				) {

					updateManager.signalUpdate (
						targetUpdateSignalName,
						targetObject.getId ());

				}

			}

		}

		transaction.commit ();

		if (
			isNotEmpty (
				updatedTargetObjects)
		) {

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
