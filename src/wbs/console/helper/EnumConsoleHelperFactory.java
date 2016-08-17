package wbs.console.helper;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.context.UninitializedComponentFactory;

@Accessors (fluent = true)
public
class EnumConsoleHelperFactory <EnumType extends Enum <EnumType>>
	implements UninitializedComponentFactory {

	// properties

	@Getter @Setter
	Class <EnumType> enumClass;

	// implementation

	@Override
	public
	Object makeComponent () {

		return new EnumConsoleHelper<EnumType> ()

			.enumClass (
				enumClass)

			.auto ();

	}

}
