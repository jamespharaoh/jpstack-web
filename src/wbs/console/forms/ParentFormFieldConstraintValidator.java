package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

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
			@NonNull Container container,
			@NonNull Optional<Native> nativeValue,
			@NonNull List<String> errors) {

		Record<?> privDelegate =
			createPrivDelegate != null
				? (Record<?>) objectManager.dereference (
					nativeValue.get (),
					createPrivDelegate)
				: nativeValue.get ();

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
