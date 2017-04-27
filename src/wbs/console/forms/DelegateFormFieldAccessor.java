package wbs.console.forms;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.helper.manager.ConsoleObjectManager;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@PrototypeComponent ("delegateFormFieldacessor")
public
class DelegateFormFieldAccessor <PrincipalContainer, DelegateContainer, Native>
	implements FormFieldAccessor <PrincipalContainer, Native> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	// properties

	@Getter @Setter
	String path;

	@Getter @Setter
	FormFieldAccessor <DelegateContainer, Native> delegateFormFieldAccessor;

	// implementation

	@Override
	public
	Optional <Native> read (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull PrincipalContainer principalContainer) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"read");

		) {

			Optional <DelegateContainer> delegateContainerOptional =
				genericCastUnchecked (
					objectManager.dereference (
						principalContainer,
						path));

			if (
				optionalIsNotPresent (
					delegateContainerOptional)
			) {
				return optionalAbsent ();
			}

			DelegateContainer delegateContainer =
				optionalGetRequired (
					delegateContainerOptional);

			return delegateFormFieldAccessor.read (
				taskLogger,
				delegateContainer);

		}

	}

	@Override
	public
	void write (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull PrincipalContainer principalContainer,
			@NonNull Optional <Native> nativeValue) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"write");

		) {

			DelegateContainer delegateContainer =
				genericCastUnchecked (
					objectManager.dereferenceRequired (
						principalContainer,
						path));

			delegateFormFieldAccessor.write (
				taskLogger,
				delegateContainer,
				nativeValue);

		}

	}

}
