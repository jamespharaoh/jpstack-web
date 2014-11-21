package wbs.platform.console.helper;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.context.BeanFactory;

@Accessors (fluent = true)
public
class EnumConsoleHelperFactory<EnumType extends Enum<EnumType>>
	implements BeanFactory {

	@Getter @Setter
	Class<EnumType> enumClass;

	public
	Object instantiate () {

		return new EnumConsoleHelper<EnumType> ()

			.enumClass (
				enumClass)

			.auto ();

	}

}
