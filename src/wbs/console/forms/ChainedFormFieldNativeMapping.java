package wbs.console.forms;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@PrototypeComponent ("chainedFormFieldNativeMapping")
public
class ChainedFormFieldNativeMapping<Container,Generic,Temporary,Native>
	implements FormFieldNativeMapping<Container,Generic,Native> {

	// singleton components

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	@Getter @Setter
	FormFieldNativeMapping<Container,Generic,Temporary> previousMapping;

	@Getter @Setter
	FormFieldNativeMapping<Container,Temporary,Native> nextMapping;

	// implementation

	@Override
	public
	Optional<Generic> nativeToGeneric (
			@NonNull Container container,
			@NonNull Optional<Native> nativeValue) {

		Optional<Temporary> temporaryValue =
			nextMapping.nativeToGeneric (
				container,
				nativeValue);

		Optional<Generic> genericValue =
			previousMapping.nativeToGeneric (
				container,
				temporaryValue);

		return genericValue;

	}

	@Override
	public
	Optional <Native> genericToNative (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Container container,
			@NonNull Optional <Generic> genericValue) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"genericToNative");

		Optional <Temporary> temporaryValue =
			previousMapping.genericToNative (
				taskLogger,
				container,
				genericValue);

		Optional <Native> nativeValue =
			nextMapping.genericToNative (
				taskLogger,
				container,
				temporaryValue);

		return nativeValue;

	}

}
