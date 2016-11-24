package wbs.console.forms;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormat;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;

@Accessors (fluent = true)
@PrototypeComponent ("parentFormFieldConstraintValidator")
public
class ParentFormFieldConstraintValidator <
	Container extends Record <?>,
	Native extends Record <?>
>
	implements FormFieldConstraintValidator <Container, Native> {

	// singleton dependencies

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	UserPrivChecker privChecker;

	// properties

	@Getter @Setter
	String createPrivDelegate;

	@Getter @Setter
	String createPrivCode;

	// implementation

	@Override
	public
	Optional <String> validate (
			@NonNull Container container,
			@NonNull Optional <Native> nativeValue) {

		Record <?> privDelegate =
			createPrivDelegate != null
				? (Record <?>)
					objectManager.dereferenceObsolete (
						nativeValue.get (),
						createPrivDelegate)
				: nativeValue.get ();

		if (
			! privChecker.canRecursive (
				privDelegate,
				createPrivCode)
		) {

			return optionalOf (
				stringFormat (
					"Permission denied"));

		}

		return optionalAbsent ();

	}

}
