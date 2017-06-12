package wbs.platform.object.list;

import static wbs.utils.collection.MapUtils.mapIsNotEmpty;
import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Map;

import javax.inject.Provider;

import com.google.common.collect.ImmutableMap;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.core.ConsoleFormType;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.part.PagePart;
import wbs.console.part.PagePartFactory;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;

import wbs.platform.object.criteria.WhereDeletedCriteriaSpec;
import wbs.platform.object.criteria.WhereICanManageCriteriaSpec;
import wbs.platform.object.criteria.WhereNotDeletedCriteriaSpec;

@Accessors (fluent = true)
public
class ObjectListPartFactory <RecordType extends Record <RecordType>>
	implements PagePartFactory {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <ObjectListTabSpec> objectListTabSpecProvider;

	@PrototypeDependency
	Provider <ObjectListPart <RecordType, ?>> objectListPartProvider;

	@PrototypeDependency
	Provider <WhereDeletedCriteriaSpec> whereDeletedCriteriaSpecProvider;

	@PrototypeDependency
	Provider <WhereICanManageCriteriaSpec> whereICanManageCriteriaSpecProvider;

	@PrototypeDependency
	Provider <WhereNotDeletedCriteriaSpec> whereNotDeletedCriteriaSpecProvider;

	// properties

	@Getter @Setter
	ConsoleHelper <RecordType> consoleHelper;

	@Getter @Setter
	String typeCode;

	@Getter @Setter
	String localName;

	@Getter @Setter
	ConsoleFormType <RecordType> formType;

	@Getter @Setter
	Map <String, ObjectListTabSpec> listTabSpecs;

	@Getter @Setter
	Map <String, ObjectListBrowserSpec> listBrowserSpecs;

	@Getter @Setter
	String targetContextTypeName;

	// implementation

	@Override
	public
	PagePart buildPagePart (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"buildPagePart");

		) {

			return objectListPartProvider.get ()

				.consoleHelper (
					consoleHelper)

				.typeCode (
					typeCode)

				.localName (
					localName)

				.listTabSpecs (
					ifThenElse (
						mapIsNotEmpty (
							listTabSpecs),
						() -> listTabSpecs,
						() -> defaultListTabSpecs ()))

				.formType (
					formType)

				.listBrowserSpecs (
					listBrowserSpecs)

				.targetContextTypeName (
					targetContextTypeName)

			;

		}

	}

	private
	Map <String, ObjectListTabSpec> defaultListTabSpecs () {

		if (consoleHelper.deletedExists ()) {

			return ImmutableMap.<String,ObjectListTabSpec>builder ()

				.put (
					"all",
					objectListTabSpecProvider.get ()

						.name (
							"all")

						.label (
							stringFormat (
								"All %s",
								consoleHelper.shortNamePlural ()))

						.addCriteria (
							whereNotDeletedCriteriaSpecProvider.get ()))

				.put (
					"deleted",
					objectListTabSpecProvider.get ()

						.name (
							"deleted")

						.label (
							"Deleted")

						.addCriteria (
							whereDeletedCriteriaSpecProvider.get ())

						.addCriteria (
							whereICanManageCriteriaSpecProvider.get ()))

				.build ();

		} else {

			return ImmutableMap.<String,ObjectListTabSpec>builder ()

				.put (
					"all",
					objectListTabSpecProvider.get ()

						.name (
							"all")

						.label (
							stringFormat (
								"All %s",
								consoleHelper.shortNamePlural ())))

				.build ();

		}

	}

}
