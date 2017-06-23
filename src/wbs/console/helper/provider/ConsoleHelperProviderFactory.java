package wbs.console.helper.provider;

import static wbs.utils.etc.TypeUtils.classForNameRequired;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.joinWithFullStop;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringSplitFullStop;

import java.util.List;

import javax.inject.Provider;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.helper.core.ConsoleHelper;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectHelper;
import wbs.framework.object.ObjectManager;

@Accessors (fluent = true)
public
class ConsoleHelperProviderFactory <
	RecordType extends Record <RecordType>
>
	implements ComponentFactory <ConsoleHelperProvider <RecordType>> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectManager objectManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <GenericConsoleHelperProvider <RecordType>>
		genericConsoleHelperProviderProvider;

	// properties

	@Getter @Setter
	ConsoleHelperProviderSpec spec;

	// implementation

	@Override
	public
	ConsoleHelperProvider <RecordType> makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeComponent");
		) {

			ObjectHelper <RecordType> objectHelper =
				genericCastUnchecked (
					objectManager.objectHelperForObjectNameRequired (
						spec.objectName ()));

			List <String> packageNameParts =
				stringSplitFullStop (
					objectHelper.objectClass ().getPackage ().getName ());

			String consoleHelperClassName =
				stringFormat (
					"%s.console.%sConsoleHelper",
					joinWithFullStop (
						packageNameParts.subList (
							0,
							packageNameParts.size () - 1)),
					capitalise (
						objectHelper.objectName ()));

			Class <ConsoleHelper <RecordType>> consoleHelperClass =
				genericCastUnchecked (
					classForNameRequired (
						consoleHelperClassName));

			return genericConsoleHelperProviderProvider.get ()

				.spec (
					spec)

				.objectHelper (
					objectHelper)

				.consoleHelperClass (
					consoleHelperClass)

				.init (
					taskLogger);

		}

	}

}
