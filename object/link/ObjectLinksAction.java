package wbs.platform.object.link;

import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.etc.Misc.contains;
import static wbs.utils.etc.Misc.doesNotContain;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Cleanup;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.action.ConsoleAction;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.TaskLogger;
import wbs.framework.web.Responder;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.updatelog.logic.UpdateManager;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.utils.etc.PropertyUtils;

@Accessors (fluent = true)
@PrototypeComponent ("objectLinksAction")
public
class ObjectLinksAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	Database database;

	@SingletonDependency
	EventLogic eventLogic;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	UpdateManager updateManager;

	@SingletonDependency
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
	Responder goReal (
			@NonNull TaskLogger taskLogger) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ObjectLinksAction.goReal ()",
				this);

		Record<?> contextObject =
			contextHelper.lookupObject (
				requestContext.contextStuff ());

		Set <Record <?>> contextLinks =
			genericCastUnchecked (
				PropertyUtils.getProperty (
					contextObject,
					contextLinkField));

		List <String> params =
			requestContext.getParameterValues (
				"old_link");

		List <Record <?>> updatedTargetObjects =
			new ArrayList<> ();

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

			Long linkId =
				Long.parseLong (
					matcher.group (
						1));

			boolean oldIsMember =
				matcher.group (2).equals ("true");

			boolean newIsMember =
				optionalIsPresent (
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
				PropertyUtils.getProperty (
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
			collectionIsNotEmpty (
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
			collectionIsNotEmpty (
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
