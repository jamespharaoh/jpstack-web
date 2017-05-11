package wbs.console.helper.core;

import static wbs.utils.etc.OptionalUtils.optionalOrNull;

import java.util.List;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.context.ConsoleContextStuff;
import wbs.console.forms.object.EntityFinder;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.helper.provider.ConsoleHelperProvider;
import wbs.console.lookup.ObjectLookup;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectHelper;

import wbs.utils.string.FormatWriter;

import fj.data.Either;

@Accessors (fluent = true)
@PrototypeComponent ("consoleHelperImplementation")
public
class ConsoleHelperImplementation <
	RecordType extends Record <RecordType>
>
	implements
		ConsoleHelperMethods <RecordType>,
		EntityFinder <RecordType>,
		ObjectLookup <RecordType> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@WeakSingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// properties

	@Getter @Setter
	ObjectHelper <RecordType> objectHelper;

	@Getter @Setter
	ConsoleHelperProvider <RecordType> consoleHelperProvider;

	@Getter @Setter
	ConsoleHooks <RecordType> consoleHooks;

	// implementation

	@Override
	public
	String idKey () {

		return consoleHelperProvider.idKey ();

	}

	@Override
	public
	String getPathId (
			@NonNull Transaction parentTransaction,
			@NonNull RecordType object) {

		return consoleHelperProvider.getPathId (
			parentTransaction,
			object.getId ());

	}

	@Override
	public
	String getPathId (
			@NonNull Transaction parentTransaction,
			@NonNull Long objectId) {

		return consoleHelperProvider.getPathId (
			parentTransaction,
			objectId);

	}

	@Override
	public
	String getDefaultContextPath (
			@NonNull Transaction parentTransaction,
			@NonNull RecordType object) {

		return consoleHelperProvider.getDefaultContextPath (
			parentTransaction,
			object);

	}

	@Override
	public
	String getDefaultLocalPath (
			@NonNull Transaction parentTransaction,
			@NonNull RecordType object) {

		return consoleHelperProvider.localPath (
			parentTransaction,
			object);

	}

	@Override
	public
	boolean canView (
			@NonNull Transaction parentTransaction,
			@NonNull RecordType object) {

		return consoleHelperProvider.canView (
			parentTransaction,
			object);

	}

	@Override
	public
	Optional <RecordType> findFromContext (
			@NonNull Transaction parentTransaction) {

		return objectHelper.find (
			parentTransaction,
			requestContext.stuffIntegerRequired (
				idKey ()));

	}

	@Override
	public
	RecordType findFromContextRequired (
			@NonNull Transaction parentTransaction) {

		return objectHelper.findRequired (
			parentTransaction,
			requestContext.stuffIntegerRequired (
				idKey ()));

	}

	@Override
	public
	void writeHtml (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			@NonNull RecordType object,
			@NonNull Optional <Record <?>> assumedRoot,
			@NonNull Boolean mini) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"writeHtml");

		) {

			Optional <String> optionalHtml =
				consoleHooks.getHtml (
					transaction,
					object,
					mini);

			if (optionalHtml.isPresent ()) {

				formatWriter.writeLineFormat (
					"%s",
					optionalHtml.get ());

			} else {

				String path =
					objectManager.objectPath (
						transaction,
						object,
						assumedRoot,
						false,
						mini);

				formatWriter.writeLineFormat (
					"<a href=\"%h\">%h</a>",
					requestContext.resolveLocalUrl (
						getDefaultLocalPath (
							transaction,
							object)),
					path);

			}

		}

	}

	@Override
	public <ObjectTypeAgain extends Record <ObjectTypeAgain>>
	ConsoleHelper <ObjectTypeAgain> cast (
			@NonNull Class <?> objectClass) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	Class <RecordType> entityClass () {

		return objectHelper ().objectClass ();

	}

	@Override
	public
	RecordType findEntity (
			@NonNull Transaction parentTransaction,
			@NonNull Long id) {

		return optionalOrNull (
			objectHelper.find (
				parentTransaction,
				id));

	}

	@Override
	public
	List <RecordType> findAllEntities (
			@NonNull Transaction parentTransaction) {

		return objectHelper.findAll (
			parentTransaction);

	}

	@Override
	public
	Either <Boolean, String> getDeletedOrError (
			@NonNull Transaction parentTransaction,
			@NonNull RecordType entity,
			boolean checkParents) {

		return objectHelper.getDeletedOrError (
			parentTransaction,
			entity,
			checkParents);

	}

	@Override
	public
	Boolean getDeleted (
			@NonNull Transaction parentTransaction,
			@NonNull RecordType entity,
			boolean checkParents) {

		return objectHelper.getDeleted (
			parentTransaction,
			entity,
			checkParents);

	}

	@Override
	public
	RecordType lookupObject (
			@NonNull Transaction parentTransaction,
			@NonNull ConsoleContextStuff contextStuff) {

		return optionalOrNull (
			objectHelper.find (
				parentTransaction,
				(Long)
				contextStuff.get (
					consoleHelperProvider ().idKey ())));

	}

}
