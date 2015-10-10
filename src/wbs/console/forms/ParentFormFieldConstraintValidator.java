package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.priv.PrivChecker;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.Record;

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

		Record<?> privDelegate =
			createPrivDelegate != null
				? (Record<?>) objectManager.dereference (
					nativeValue,
					createPrivDelegate)
				: nativeValue;

		if (
			! privChecker.can (
				privDelegate,
				createPrivCode)
		) {

			errors.add (
				stringFormat (
					"Permission denied"));

		}

	}

}
