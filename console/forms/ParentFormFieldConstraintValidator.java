package wbs.platform.console.forms;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.Record;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.priv.console.PrivChecker;

@Accessors (fluent = true)
@PrototypeComponent ("parentFormFieldConstraintValidator")
public
class ParentFormFieldConstraintValidator<
	Container extends Record<?>,
	Native extends Record<?>
>
	implements FormFieldConstraintValidator<Container,Native> {

	// dependencies

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	PrivChecker privChecker;

	// properties

	@Getter @Setter
	String createPrivDelegate;

	@Getter @Setter
	String createPrivCode;

	// implementation

	@Override
	public
	void validate (
			Container container,
			Native nativeValue,
			List<String> errors) {

System.out.println ("ABOUT TO RESOLVE DELEGATE FROM " + nativeValue);
System.out.println ("DELEGATE PATH IS " + createPrivDelegate);
		Record<?> privDelegate =
			createPrivDelegate != null
				? objectManager.dereference (
					nativeValue,
					createPrivDelegate)
				: nativeValue;
System.out.println ("RESOLVED TO " + nativeValue);

		if (! privChecker.can (
				privDelegate,
				createPrivCode)) {

			errors.add (
				stringFormat (
					"Permission denied"));

		}

	}

}
