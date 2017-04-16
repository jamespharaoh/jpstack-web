package wbs.console.helper.core;

import static wbs.utils.etc.OptionalUtils.optionalOrNull;

import java.util.List;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.context.ConsoleContextStuff;
import wbs.console.forms.EntityFinder;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.helper.provider.ConsoleHelperProvider;
import wbs.console.lookup.ObjectLookup;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
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
			@NonNull TaskLogger parentTaskLogger,
			@NonNull RecordType object) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"getPathId");

		return consoleHelperProvider.getPathId (
			taskLogger,
			object.getId ());

	}

	@Override
	public
	String getPathId (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long objectId) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"getPathId");

		return consoleHelperProvider.getPathId (
			taskLogger,
			objectId);

	}

	@Override
	public
	String getDefaultContextPath (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull RecordType object) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"getDefaultContextPath");

		return consoleHelperProvider.getDefaultContextPath (
			taskLogger,
			object);

	}

	@Override
	public
	String getDefaultLocalPath (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull RecordType object) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"getDefaultLocalPath");

		return consoleHelperProvider.localPath (
			taskLogger,
			object);

	}

	@Override
	public
	boolean canView (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull RecordType object) {

		return consoleHelperProvider.canView (
			parentTaskLogger,
			object);

	}

	@Override
	public
	Optional <RecordType> findFromContext () {

		return objectHelper.find (
			requestContext.stuffIntegerRequired (
				idKey ()));

	}

	@Override
	public
	RecordType findFromContextRequired () {

		return objectHelper.findRequired (
			requestContext.stuffIntegerRequired (
				idKey ()));

	}

	@Override
	public
	void writeHtml (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter formatWriter,
			@NonNull RecordType object,
			@NonNull Optional <Record <?>> assumedRoot,
			@NonNull Boolean mini) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"writeHtml");

		Optional <String> optionalHtml =
			consoleHooks.getHtml (
				object,
				mini);

		if (optionalHtml.isPresent ()) {

			formatWriter.writeLineFormat (
				"%s",
				optionalHtml.get ());

		} else {

			String path =
				objectManager.objectPath (
					object,
					assumedRoot,
					false,
					mini);

			formatWriter.writeLineFormat (
				"<a href=\"%h\">%h</a>",
				requestContext.resolveLocalUrl (
					getDefaultLocalPath (
						taskLogger,
						object)),
				path);

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
			@NonNull Long id) {

		return optionalOrNull (
			objectHelper.find (
				id));

	}

	@Override
	public
	List <RecordType> findAllEntities () {

		return objectHelper.findAll ();

	}

	@Override
	public
	Either <Boolean, String> getDeletedOrError (
			@NonNull RecordType entity,
			boolean checkParents) {

		return objectHelper.getDeletedOrError (
			entity,
			checkParents);

	}

	@Override
	public
	Boolean getDeleted (
			RecordType entity,
			boolean checkParents) {

		return objectHelper.getDeleted (
			entity,
			checkParents);

	}

	@Override
	public
	RecordType lookupObject (
			@NonNull ConsoleContextStuff contextStuff) {

		return optionalOrNull (
			objectHelper.find (
				(Long)
				contextStuff.get (
					consoleHelperProvider ().idKey ())));

	}

}
