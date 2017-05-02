package wbs.console.helper.provider;

import static wbs.utils.collection.CollectionUtils.collectionHasOneElement;
import static wbs.utils.collection.CollectionUtils.collectionHasTwoElements;
import static wbs.utils.collection.CollectionUtils.listFirstElementRequired;
import static wbs.utils.collection.CollectionUtils.listSecondElementRequired;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.OptionalUtils.optionalCast;
import static wbs.utils.etc.OptionalUtils.optionalGetOrAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.etc.ResultUtils.resultValue;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.naivePluralise;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;
import static wbs.utils.string.StringUtils.stringSplitColon;
import static wbs.utils.string.StringUtils.underscoreToCamel;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.context.ConsoleContextStuff;
import wbs.console.context.ConsoleContextStuffSpec;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.helper.spec.ConsoleHelperProviderSpec;
import wbs.console.helper.spec.PrivKeySpec;
import wbs.console.helper.spec.RunPostProcessorSpec;
import wbs.console.module.ConsoleManager;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.request.Cryptor;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectHelper;
import wbs.framework.object.ObjectManager;

import wbs.utils.string.StringSubstituter;

@Accessors (fluent = true)
@PrototypeComponent ("genericConsoleHelperProvider")
public
class GenericConsoleHelperProvider <
	RecordType extends Record <RecordType>
>
	implements ConsoleHelperProvider <RecordType> {

	// singleton dependencies

	@SingletonDependency
	ComponentManager componentManager;

	@WeakSingletonDependency
	ConsoleManager consoleManager;

	@WeakSingletonDependency
	ConsoleObjectManager consoleObjectManager;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectManager objectManager;

	@WeakSingletonDependency
	ConsoleRequestContext requestContext;

	@WeakSingletonDependency
	UserPrivChecker privChecker;

	// required properties

	@Getter @Setter
	ConsoleHelperProviderSpec consoleHelperProviderSpec;

	@Getter @Setter
	ObjectHelper <RecordType> objectHelper;

	@Getter @Setter
	Class <ConsoleHelper <RecordType>> consoleHelperClass;

	// console helper properties

	@Getter @Setter
	Class <RecordType> objectClass;

	@Getter @Setter
	String objectName;

	@Getter @Setter
	String idKey;

	@Getter @Setter
	Cryptor cryptor;

	@Getter @Setter
	String defaultListContextName;

	@Getter @Setter
	String defaultObjectContextName;

	@Getter @Setter
	String viewDelegateField;

	@Getter @Setter
	String viewDelegatePrivCode;

	// state

	List <PrivKeySpec> viewPrivKeySpecs;

	// init

	public
	GenericConsoleHelperProvider <RecordType> init (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"init");

		) {

			// check required properties

			if (consoleHelperProviderSpec == null)
				throw new NullPointerException ("consoleHelperProviderSpec");

			if (objectHelper == null)
				throw new NullPointerException ("objectHelper");

			if (consoleHelperClass == null)
				throw new NullPointerException ("consoleHelperClass");

			// initialise other stuff

			objectClass (
				objectHelper.objectClass ());

			objectName (
				objectHelper.objectName ());

			idKey (
				consoleHelperProviderSpec.idKey ());

			defaultListContextName (
				ifNull (
					consoleHelperProviderSpec.defaultListContextName (),
					naivePluralise (
						objectName ())));

			defaultObjectContextName (
				ifNull (
					consoleHelperProviderSpec.defaultObjectContextName (),
					objectHelper.objectName ()));

			if (
				isNotNull (
					consoleHelperProviderSpec.viewPriv ())
			) {

				List <String> viewPrivParts =
					stringSplitColon (
						consoleHelperProviderSpec.viewPriv ());

				if (viewPrivParts.size () == 1) {

					viewDelegateField (
						viewPrivParts.get (0));

				} else if (viewPrivParts.size () == 2) {

					viewDelegateField (
						viewPrivParts.get (0));

					viewDelegatePrivCode (
						viewPrivParts.get (1));

				} else {

					throw new RuntimeException ();

				}

			}

			if (consoleHelperProviderSpec.cryptorBeanName () != null) {

				cryptor (
					componentManager.getComponentRequired (
						taskLogger,
						consoleHelperProviderSpec.cryptorBeanName (),
						Cryptor.class));

			}

			// collect view priv key specs

			String viewPrivKey =
				stringFormat (
					"%s.view",
					objectName ());

			for (
				PrivKeySpec privKeySpec
					: consoleHelperProviderSpec.privKeys ()
			) {

				if (
					stringNotEqualSafe (
						privKeySpec.name (),
						viewPrivKey)
				) {
					continue;
				}

				if (viewPrivKeySpecs == null) {

					viewPrivKeySpecs =
						new ArrayList<> ();

				}

				viewPrivKeySpecs.add (
					privKeySpec);

			}

			// and return

			return this;

		}

	}

	@Override
	public
	void postProcess (
			@NonNull Transaction parentTransaction,
			@NonNull ConsoleContextStuff contextStuff) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"postProcess");

		) {

			transaction.debugFormat (
				"Running post processor for %s",
				objectName ());

			// lookup object

			Long id =
				(Long)
				contextStuff.get (
					idKey ());

			Record <?> object =
				objectHelper.findRequired (
					transaction,
					id);

			// set context stuff

			for (
				ConsoleContextStuffSpec contextStuffSpec
					: consoleHelperProviderSpec.contextStuffs ()
			) {

				if (
					contextStuffSpec.fieldName () != null
					&& contextStuffSpec.template () != null
				) {
					throw new RuntimeException ();
				}

				if (contextStuffSpec.template () != null) {

					contextStuff.set (
						contextStuffSpec.name (),
						contextStuff.substitutePlaceholders (
							contextStuffSpec.template ()));

				} else {

					contextStuff.set (
						contextStuffSpec.name (),
						objectManager.dereferenceRequired (
							transaction,
							object,
							contextStuffSpec.fieldName ()));

				}

			}

			// set privs

			UserPrivChecker privChecker =
				(UserPrivChecker)
				this.privChecker;

			for (
				PrivKeySpec privKeySpec
					: consoleHelperProviderSpec.privKeys ()
			) {

				List <String> privCodeParts =
					stringSplitColon (
						privKeySpec.privCode ());

				Record <?> privObject;
				String privCode;

				if (
					collectionHasOneElement (
						privCodeParts)
				) {

					privObject =
						object;

					privCode =
						privKeySpec.privCode ();

				} else if (
					collectionHasTwoElements (
						privCodeParts)
				) {

					privObject =
						genericCastUnchecked (
							objectManager.dereferenceRequired (
								transaction,
								object,
								listFirstElementRequired (
									privCodeParts)));

					privCode =
						listSecondElementRequired (
							privCodeParts);

				} else {

					throw new RuntimeException ();

				}

				if (
					privChecker.canRecursive (
						transaction,
						privObject,
						privCode)
				) {

					contextStuff.grant (
						privKeySpec.name ());

				}

			}

			// run chained post processors

			for (
				RunPostProcessorSpec runPostProcessorSpec
					: consoleHelperProviderSpec.runPostProcessors ()
			) {

				consoleManager.runPostProcessors (
					transaction,
					runPostProcessorSpec.name (),
					contextStuff);

			}

		}

	}

	@Override
	public
	String getPathId (
			@NonNull Transaction parentTransaction,
			@NonNull Long objectId) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getPathId");

		) {

			if (cryptor != null) {

				return cryptor.encryptInteger (
					transaction,
					objectId);

			} else {

				return Long.toString (
					objectId);

			}

		}

	}

	@Override
	public
	String getDefaultContextPath (
			@NonNull Transaction parentTransaction,
			@NonNull RecordType object) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getDefaultContextPath");

		) {

			StringSubstituter stringSubstituter =
				new StringSubstituter ();

			if (objectHelper.typeCodeExists ()) {

				stringSubstituter

					.param (
						"typeCode",
						objectHelper.getTypeCode (
							object))

					.param (
						"typeCamel",
						underscoreToCamel (
							objectHelper.getTypeCode (
								object)));

			}

			String url =
				stringFormat (

					"/%s",
					defaultObjectContextName (),

					"/%s",
					getPathId (
						transaction,
						object.getId ()));

			return stringSubstituter.substitute (
				url);

		}

	}

	@Override
	public
	String localPath (
			@NonNull Transaction parentTransaction,
			@NonNull RecordType object) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"localPath");

		) {

			String urlTemplate =

				stringFormat (
					"/-/%s",
					defaultObjectContextName (),

					"/%s",
					getPathId (
						transaction,
						object.getId ()));

			StringSubstituter stringSubstituter =
				new StringSubstituter ();

			if (objectHelper.typeCodeExists ()) {

				stringSubstituter.param (
					"typeCode",
					objectHelper.getTypeCode (
						object));

			}

			return stringSubstituter.substitute (
				urlTemplate);

		}

	}

	@Override
	public
	boolean canView (
			@NonNull Transaction parentTransaction,
			@NonNull RecordType object) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"canView");

		) {

			// types are always visible

			if (objectHelper.type ()) {

				transaction.debugFormat (
					"Object types are visible to everyone");

				return true;

			}

			// objects with cryptors are always visible

			if (
				isNotNull (
					cryptor)
			) {

				transaction.debugFormat (
					"Object with encrypted IDs are visible to everyone");

				return true;

			}

			// view privs

			if (
				isNotNull (
					viewPrivKeySpecs)
			) {

				transaction.debugFormat (
					"Checking view priv keys");

				Boolean visible =
					false;

				for (
					PrivKeySpec privKeySpec
						: viewPrivKeySpecs
				) {

					List <String> privCodeParts =
						stringSplitColon (
							privKeySpec.privCode ());

					String privObjectPath;
					Optional <Record <?>> privObjectOptional;
					String privCode;

					if (
						collectionHasOneElement (
							privCodeParts)
					) {

						privObjectPath = null;

						privObjectOptional =
							optionalOf (
								object);

						privCode =
							privKeySpec.privCode ();

					} else if (
						collectionHasTwoElements (
							privCodeParts)
					) {

						privObjectPath =
							listFirstElementRequired (
								privCodeParts);

						privObjectOptional =
							genericCastUnchecked (
								objectManager.dereference (
									transaction,
									object,
									privObjectPath));

						privCode =
							listSecondElementRequired (
								privCodeParts);

					} else {

						throw new RuntimeException ();

					}

					if (
						optionalIsNotPresent (
							privObjectOptional)
					) {

						transaction.debugFormat (
							"Can't find delegate %s for view priv key",
							privObjectPath);

						continue;

					}

					Record <?> privObject =
						privObjectOptional.get ();

					if (
						privChecker.canRecursive (
							transaction,
							privObject,
							privCode)
					) {

						transaction.debugFormat (
							"Object is visible because of priv '%s' on: %s",
							privCode,
							objectManager.objectPathMini (
								transaction,
								privObject));

						visible = true;

					} else if (
						isNotNull (
							privObjectPath)
					) {

						transaction.debugFormat (
							"Not granted visibility because of priv '%s' ",
							privCode,
							"on: %s",
							privObjectPath);

					} else {

						transaction.debugFormat (
							"Not granted visibility because of priv '%s'",
							privCode);

					}

				}

				if (visible) {
					return true;
				}

			}

			// view delegate

			if (
				isNotNull (
					viewDelegateField)
			) {

				// special keyword 'public'

				if (
					stringEqualSafe (
						viewDelegateField,
						"public")
				) {
					return true;
				}

				// lookup delegate

				Optional <Record <?>> delegateOptional =
					genericCastUnchecked (
						optionalGetOrAbsent (
							resultValue (
								objectManager.dereferenceOrError (
									transaction,
									object,
									viewDelegateField))));

				if (
					optionalIsNotPresent (
						delegateOptional)
				) {

					transaction.debugFormat (
						"Object is not visible because view delegate %s ",
						viewDelegateField,
						"is not present");

					return false;

				}

				Record <?> delegate =
					optionalGetRequired (
						delegateOptional);

				// check priv

				if (
					isNotNull (
						viewDelegatePrivCode)
				) {

					transaction.debugFormat (
						"Delegating to %s priv %s",
						viewDelegateField,
						viewDelegatePrivCode);

					return privChecker.canRecursive (
						transaction,
						delegate,
						viewDelegatePrivCode);

				} else {

					ConsoleHelper <?> delegateHelper =
						consoleObjectManager.findConsoleHelperRequired (
							delegate);

					transaction.debugFormat (
						"Delegating to %s",
						viewDelegateField);

					return delegateHelper.canView (
						transaction,
						genericCastUnchecked (
							delegate));

				}

			}

			// default

			transaction.debugFormat (
				"Delegating to priv checker");

			return privChecker.canRecursive (
				transaction,
				object);

		}

	}

	@Override
	public
	RecordType lookupObject (
			@NonNull Transaction parentTransaction,
			@NonNull ConsoleContextStuff contextStuff) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"lookupObject");

		) {

			Long objectId =
				(Long)
				contextStuff.get (
					idKey);

			return objectClass ().cast (
				optionalOrNull (
					optionalCast (
						Record.class,
						objectHelper.find (
							transaction,
							objectId))));

		}

	}

}
