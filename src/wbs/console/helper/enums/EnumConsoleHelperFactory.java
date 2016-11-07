package wbs.console.helper.enums;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.logging.TaskLogger;

@Log4j
@Accessors (fluent = true)
public
class EnumConsoleHelperFactory <EnumType extends Enum <EnumType>>
	implements ComponentFactory {

	// properties

	@Getter @Setter
	Class <EnumType> enumClass;

	// implementation

	@Override
	public
	Object makeComponent (
			@NonNull TaskLogger taskLogger) {

		return new EnumConsoleHelper <EnumType> ()

			.enumClass (
				enumClass)

			.auto ();

	}

}
