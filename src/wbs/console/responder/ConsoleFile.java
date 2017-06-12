package wbs.console.responder;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

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
import wbs.framework.component.annotations.StrongPrototypeDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.exceptions.HttpForbiddenException;
import wbs.web.file.AbstractFile;
import wbs.web.mvc.WebAction;
import wbs.web.mvc.WebActionRequestHandler;
import wbs.web.mvc.WebRequestHandler;
import wbs.web.mvc.WebResponderRequestHandler;
import wbs.web.responder.WebResponder;

@Accessors (fluent = true)
@PrototypeComponent ("consoleFile")
@DataClass ("console-file")
public
class ConsoleFile
	extends AbstractFile {

	// singleton dependencies

	@SingletonDependency
	ComponentManager componentManager;

	@WeakSingletonDependency
	ConsoleManager consoleManager;

	@SingletonDependency
	ConsoleRequestContext consoleRequestContext;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@StrongPrototypeDependency
	Provider <WebActionRequestHandler> actionRequestHandlerProvider;

	@PrototypeDependency
	Provider <ConsoleContextPrivLookup> contextPrivLookupProvider;

	@StrongPrototypeDependency
	Provider <WebResponderRequestHandler> responderRequestHandlerProvider;

	// properties

	@DataAttribute
	@Getter @Setter
	Provider <? extends WebRequestHandler> getHandlerProvider;

	@DataAttribute
	@Getter @Setter
	Provider <? extends WebRequestHandler> postHandlerProvider;

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
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String handlerName) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"getHandlerName");

		) {

			return getHandlerProvider (
				componentManager.getComponentProviderRequired (
					taskLogger,
					handlerName,
					WebRequestHandler.class));

		}

	}

	public
	ConsoleFile getResponderProvider (
			@NonNull Provider <WebResponder> responder) {

		return getHandlerProvider (
			() -> responderRequestHandlerProvider.get ()

			.responderProvider (
				responder)

		);

	}

	public
	ConsoleFile getResponderName (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String responderName) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"getResponderName");

		) {

			return getResponderProvider (
				componentManager.getComponentProviderRequired (
					taskLogger,
					responderName,
					WebResponder.class));

		}

	}

	public
	ConsoleFile getResponderName (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Optional <String> responderName) {

		if (
			optionalIsNotPresent (
				responderName)
		) {
			return this;
		}

		return getResponderName (
			parentTaskLogger,
			optionalGetRequired (
				responderName));

	}

	public
	ConsoleFile getActionProvider (
			@NonNull Provider <WebAction> actionProvider) {

		return getHandlerProvider (
			() -> actionRequestHandlerProvider.get ()

			.actionProvider (
				actionProvider)

		);

	}

	public
	ConsoleFile getActionName (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String actionName) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"getActionName");

		) {

			return getActionProvider (
				componentManager.getComponentProviderRequired (
					taskLogger,
					actionName,
					WebAction.class));

		}

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

		return getActionName (
			parentTaskLogger,
			optionalGetRequired (
				actionName));

	}

	public
	ConsoleFile postHandlerName (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String handlerName) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"postHandlerName");

		) {

			return postHandlerProvider (
				componentManager.getComponentProviderRequired (
					taskLogger,
					handlerName,
					WebRequestHandler.class));

		}

	}

	public
	ConsoleFile postActionProvider (
			@NonNull Provider <WebAction> actionProvider) {

		return postHandlerProvider (
			() -> actionRequestHandlerProvider.get ()

			.actionProvider (
				actionProvider)

		);

	}

	public
	ConsoleFile postActionName (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String actionName) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"postActionName");

		) {

			return postActionProvider (
				componentManager.getComponentProviderRequired (
					taskLogger,
					actionName,
					WebAction.class));

		}

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

		return postActionName (
			parentTaskLogger,
			optionalGetRequired (
				actionName));

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
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"doGet");

		) {

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

	}

	@Override
	public
	void doPost (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"doPost");

		) {

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

}
