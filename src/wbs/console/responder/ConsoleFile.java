package wbs.console.responder;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;
import javax.servlet.ServletException;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.context.ConsoleContextPrivLookup;
import wbs.console.lookup.BooleanLookup;
import wbs.console.module.ConsoleManager;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.web.action.Action;
import wbs.web.action.ActionRequestHandler;
import wbs.web.exceptions.HttpForbiddenException;
import wbs.web.file.AbstractFile;
import wbs.web.handler.RequestHandler;
import wbs.web.responder.Responder;

@Accessors (fluent = true)
@PrototypeComponent ("consoleFile")
@DataClass ("console-file")
public
class ConsoleFile
	extends AbstractFile {

	// singleton dependencies

	@WeakSingletonDependency
	ConsoleManager consoleManager;

	@SingletonDependency
	ConsoleRequestContext consoleRequestContext;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <ActionRequestHandler> actionRequestHandlerProvider;

	@PrototypeDependency
	Provider <ConsoleContextPrivLookup> contextPrivLookupProvider;

	// properties

	@DataAttribute
	@Getter @Setter
	RequestHandler getHandler;

	@DataAttribute
	@Getter @Setter
	RequestHandler postHandler;

	@DataAttribute
	@Getter @Setter
	BooleanLookup privLookup;

	@DataChildren
	@Getter @Setter
	Map <String, Object> requestParams =
		new LinkedHashMap<String,Object> ();

	// utility methods

	public
	ConsoleFile getHandlerName (
			String handlerName) {

		if (handlerName == null)
			return this;

		return getHandler (
			handlerNameToRequestHandler (
				handlerName));

	}

	public
	ConsoleFile getResponder (
			Provider <Responder> responder) {

		return getHandler (
			responderToRequestHandler (
				responder));

	}

	public
	ConsoleFile getResponderName (
			String responderName) {

		if (responderName == null)
			return this;

		return getHandler (
			responderToRequestHandler (
				responder (responderName)));

	}

	public
	Provider <Responder> responder (
			final String responderName) {

		return new Provider<Responder> () {

			@Override
			public
			Responder get () {

				Provider <Responder> responderProvider =
					consoleManager.responder (
						responderName,
						true);

				return responderProvider.get ();

			}

		};

	}

	public
	ConsoleFile getActionProvider (
			Provider <Action> actionProvider) {

		return getHandler (

			actionRequestHandlerProvider.get ()
				.actionProvider (
					actionProvider)

		);

	}

	public
	ConsoleFile getActionName (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String actionName) {

		return getHandler (
			actionRequestHandlerProvider.get ()

			.actionName (
				parentTaskLogger,
				actionName));

	}

	public
	ConsoleFile getActionName (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Optional <String> actionName) {

		if (
			optionalIsNotPresent (
				actionName)
		) {
			return this;
		}

		return getHandler (
			actionRequestHandlerProvider.get ()

			.actionName (
				parentTaskLogger,
				actionName.get ()));

	}

	public
	ConsoleFile postHandlerName (
			String handlerName) {

		return postHandler (
			handlerNameToRequestHandler (
				handlerName));

	}

	public
	ConsoleFile postActionProvider (
			@NonNull Provider <Action> actionProvider) {

		return postHandler (
			actionRequestHandlerProvider.get ()

			.actionProvider (
				actionProvider)

		);

	}

	public
	ConsoleFile postActionName (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String actionName) {

		return postHandler (
			actionRequestHandlerProvider.get ()

			.actionName (
				parentTaskLogger,
				actionName));

	}

	public
	ConsoleFile postActionName (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Optional <String> actionName) {

		if (
			optionalIsNotPresent (
				actionName)
		) {
			return this;
		}

		return postHandler (
			actionRequestHandlerProvider.get ()

			.actionName (
				parentTaskLogger,
				optionalGetRequired (
					actionName)));

	}

	public
	ConsoleFile privKeys (
			List <String> privKeys) {

		return privLookup (
			contextPrivLookupProvider.get ()
				.privKeys (privKeys));

	}

	public
	ConsoleFile privName (
			String privName) {

		return privLookup (
			contextPrivLookupProvider.get ()
				.addPrivKey (privName));

	}

	public
	ConsoleFile requestParam (
			String key,
			Object value) {

		requestParams.put (
			key,
			value);

		return this;

	}

	// implementation

	@Override
	public
	void doGet (
			@NonNull TaskLogger parentTaskLogger)
		throws
			ServletException,
			IOException {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"doGet");

		if (

			privLookup != null

			&& ! privLookup.lookup (
				consoleRequestContext.consoleContextStuffRequired ())

		) {

			throw new HttpForbiddenException (
				optionalOf (
					privLookup.describe ()),
				emptyList ());

		}

		super.doGet (
			taskLogger);

	}

	@Override
	public
	void doPost (
			@NonNull TaskLogger parentTaskLogger)
		throws
			ServletException,
			IOException {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"doPost");

		if (

			privLookup != null

			&& ! privLookup.lookup (
				consoleRequestContext.consoleContextStuffRequired ())

		) {

			throw new HttpForbiddenException (
				optionalAbsent (),
				emptyList ());

		}

		super.doPost (
			taskLogger);

	}

}
