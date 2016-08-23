package wbs.console.helper;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.context.ComponentFactory;

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
	Object makeComponent () {

		return new EnumConsoleHelper <EnumType> ()

			.enumClass (
				enumClass)

			.auto ();

	}

}
