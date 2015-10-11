package wbs.framework.hibernate;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import org.hibernate.LockOptions;
import org.hibernate.Session;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.entity.model.Model;
import wbs.framework.object.ObjectHelper;
import wbs.framework.object.ObjectHelperProvider;
import wbs.framework.object.ObjectHooks;
import wbs.framework.object.ObjectTypeRegistry;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.GlobalId;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.BeanLogic;

@Accessors (fluent = true)
@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("hibernateHelperProviderBuilder")
@Log4j
public
class HibernateHelperProviderBuilder {

	// dependencies

	@Inject
	HibernateDatabase hibernateDatabase;

	@Inject
	ObjectTypeRegistry objectTypeRegistry;

	// properties

	@Getter @Setter
	Model model;

	@Getter @Setter
	ObjectHooks objectHooks;

	// state

	boolean built = false;

	// implementation

	public
	ObjectHelperProvider build () {

		// only allow a single invocation

		if (built)
			throw new IllegalStateException ();

		built = true;

		// sanity check

		if (hibernateDatabase == null) {

			throw new NullPointerException (
				"hibernateDatabase");

		}

		if (model == null) {

			throw new NullPointerException (
				"model");

		}

		if (model.objectName () == null) {

			log.error (
				stringFormat (
					"Model helper %s of type %s for class %s has no object name",
					model.toString (),
					model.objectClass (),
					model.objectName ()));

		}

		return new Implementation ();

	}

	@Accessors (fluent = true)
	class Implementation
		implements ObjectHelperProvider {

		@Override
		public
		Class<? extends Record<?>> objectClass () {

			return
				(Class<? extends Record<?>>)
				model.objectClass ();

		}

		@Override
		public
		String objectTypeCode () {

			return model
				.objectTypeCode ();

		}

		@Override
		public
		Class<? extends ObjectHelper<?>> helperClass () {

			return
				(Class<? extends ObjectHelper<?>>)
				model.helperClass ();

		}

		@Override
		public
		Class<?> parentClass () {

			if (model.isRooted ()) {

				return objectTypeRegistry.rootRecordClass ();

			} else {

				return model.parentClass ();

			}

		}

		@Override
		public
		String parentFieldName () {
			return model.parentField ().name ();
		}

		@Override
		public
		String parentLabel () {
			return model.parentField ().label ();
		}

		@Override
		public
		Boolean parentExists () {
			return model.parentField () != null;
		}

		@Override
		public
		String codeFieldName () {

			if (model.codeField () == null) {

				throw new UnsupportedOperationException (
					stringFormat (
						"Object type %s has no code field",
						model.objectName ()));

			}

			return model.codeField ().name ();

		}

		@Override
		public
		String typeCodeLabel () {

			if (model.typeCodeField () == null) {

				throw new UnsupportedOperationException (
					stringFormat (
						"Object type %s has no type code field",
						model.objectName ()));

			}

			return model.typeCodeField ().label ();

		}

		@Override
		public
		Boolean typeCodeExists () {
			return model.typeCodeField () != null;
		}

		@Override
		public
		String typeCodeFieldName () {

			if (model.typeCodeField () == null) {

				throw new UnsupportedOperationException (
					stringFormat (
						"Object type %s has no type code field",
						model.objectName ()));

			}

			return model.typeCodeField ().name ();

		}

		@Override
		public
		String codeLabel () {

			if (model.codeField () == null) {

				throw new UnsupportedOperationException (
					stringFormat (
						"Object type %s has no code field",
						model.objectName ()));

			}

			return model.codeField ().label ();

		}

		@Override
		public
		Boolean codeExists () {
			return model.codeField () != null;
		}

		@Override
		public
		String indexFieldName () {

			if (model.indexField () == null) {

				throw new UnsupportedOperationException (
					stringFormat (
						"Object type %s has no index field",
						model.objectName ()));

			}

			return model.indexField ().name ();

		}

		@Override
		public
		String indexLabel () {

			if (model.indexField () == null) {

				throw new UnsupportedOperationException (
					stringFormat (
						"Object type %s has no index field",
						model.objectName ()));

			}

			return model.indexField ().label ();

		}

		@Override
		public
		Boolean indexExists () {
			return model.indexField () != null;
		}

		@Override
		public
		String indexCounterFieldName () {
			return model.indexField ().counter ();
		}

		@Override
		public
		String nameFieldName () {

			if (model.nameField () == null) {

				throw new UnsupportedOperationException (
					stringFormat (
						"Object type %s has no name field",
						model.objectName ()));

			}

			return model.nameField ().name ();

		}

		@Override
		public
		String nameLabel () {

			if (model.nameField () == null) {

				throw new UnsupportedOperationException (
					stringFormat (
						"Object type %s has no name field",
						model.objectName ()));

			}

			return model.nameField ().label ();
		}

		@Override
		public
		Boolean nameExists () {
			return model.nameField () != null;
		}

		@Override
		public
		Boolean nameIsCode () {

			return model.nameField () == null
					&& model.codeField () != null;

		}

		@Override
		public
		Boolean deletedExists () {

			return model.deletedField () != null;

		}

		@Override
		public
		String descriptionFieldName () {

			if (model.descriptionField () == null) {

				throw new UnsupportedOperationException (
					stringFormat (
						"Object type %s has no description field",
						model.objectName ()));

			}

			return model.descriptionField ().name ();

		}

		@Override
		public
		String deletedFieldName () {

			if (model.deletedField () == null) {

				throw new UnsupportedOperationException (
					stringFormat (
						"Object type %s has no deleted field",
						model.objectName ()));

			}

			return model.deletedField ().name ();

		}

		@Override
		public
		String deletedLabel () {

			if (model.deletedField () == null) {

				throw new UnsupportedOperationException (
					stringFormat (
						"Object type %s has no deleted field",
						model.objectName ()));

			}

			return model.deletedField ().label ();

		}

		@Override
		public
		String descriptionLabel () {

			if (model.descriptionField () == null) {

				throw new UnsupportedOperationException (
					stringFormat (
						"Object type %s has no description field",
						model.objectName ()));

			}

			return model.descriptionField ().label ();

		}

		@Override
		public
		Boolean descriptionExists () {
			return model.descriptionField () != null;
		}

		@Override
		public
		Record<?> find (
				long id) {

			Session session =
				hibernateDatabase.currentSession ();

			return (Record<?>)
				session.get (
					objectClass (),
					(int) id);

		}

		@Override
		public
		Record<?> findByParentAndCode (
				GlobalId parentGlobalId,
				String code) {

			Session session =
				hibernateDatabase.currentSession ();

			if (typeCodeExists ()) {

				throw new UnsupportedOperationException (
					stringFormat (
						"Object type %s must be looked up by type code",
						getClass ().getSimpleName ()));

			}

			if (model.isRooted ()) {

				if (
					! equal (
						parentGlobalId,
						GlobalId.root)
				) {

					throw new IllegalArgumentException (
						stringFormat (
							"Invalid parent global id %s for rooted object in %s.%s",
							parentGlobalId,
							getClass ().getSimpleName (),
							"findChildByCode"));

				}

				List<?> list =

					session.createQuery (
						"FROM " + objectClass ().getName () + " ob " +
						"WHERE ob."	+ codeFieldName () + " = :code")

					.setString (
						"code",
						code)

					.list ();

				if (list.isEmpty ())
					return null;

				return (Record<?>) list.get (0);

			} else if (model.parentTypeIsFixed ()) {

				/*
				if (parentGlobalId.getTypeId ()
						!= parentObjectHelperProvider ().objectTypeId ()) {

					throw new IllegalArgumentException (sf (
						"Invalid parent type id %s for %s (should be %s)",
						parentGlobalId.getTypeId (),
						objectClass ().getSimpleName (),
						parentObjectHelperProvider ().objectTypeId ()));

				}
				*/

				List<Record<?>> list =
					session.createQuery (

						stringFormat (

							"FROM %s _%s ",
							objectClass ().getSimpleName (),
							objectName (),

							"WHERE _%s.%s.id = :parentId ",
							objectName (),
							parentFieldName (),

							"AND _%s.%s = :code",
							objectName (),
							codeFieldName ()))

						.setInteger (
							"parentId",
							parentGlobalId.objectId ())

						.setString (
							"code",
							code)

						.list ();

				if (list.isEmpty ())
					return null;

				return (Record<?>)
					list.get (0);

			} else {

				List<?> list =

					session.createQuery (
						stringFormat (

							"FROM %s _%s ",
							objectClass ().getSimpleName (),
							objectName (),

							"WHERE _%s.%s.id = :parentTypeId ",
							objectName (),
							model.parentTypeField ().name (),

							"AND _%s.%s = :parentId ",
							objectName (),
							model.parentIdField ().name (),

							"AND _%s.%s = :code",
							objectName (),
							codeFieldName ()))

					.setInteger (
						"parentTypeId",
						parentGlobalId.typeId ())

					.setInteger (
						"parentId",
						parentGlobalId.objectId ())

					.setString (
						"code",
						code)

					.list ();

				if (list.isEmpty ())
					return null;

				return (Record<?>)
					list.get (0);

			}

		}

		@Override
		public
		Record<?> findByParentAndIndex (
				GlobalId parentGlobalId,
				Integer index) {

			Session session =
				hibernateDatabase.currentSession ();

			if (typeCodeExists ()) {

				throw new UnsupportedOperationException (
					stringFormat (
						"Object type %s must be looked up by type code",
						getClass ().getSimpleName ()));

			}

			if (model.isRooted ()) {

				if (
					! equal (
						parentGlobalId,
						GlobalId.root)
				) {

					throw new IllegalArgumentException (
						stringFormat (
							"Invalid parent global id %s for rooted object in %s.%s",
							parentGlobalId,
							getClass ().getSimpleName (),
							"findChildByCode"));

				}

				List<?> list =

					session.createQuery (
						"FROM " + objectClass ().getName () + " ob " +
						"WHERE ob."	+ indexFieldName () + " = :index")

					.setInteger (
						"index",
						index)

					.list ();

				if (list.isEmpty ())
					return null;

				return (Record<?>) list.get (0);

			} else if (model.parentTypeIsFixed ()) {

				/*
				if (parentGlobalId.getTypeId ()
						!= parentObjectHelperProvider ().objectTypeId ()) {

					throw new IllegalArgumentException (sf (
						"Invalid parent type id %s for %s (should be %s)",
						parentGlobalId.getTypeId (),
						objectClass ().getSimpleName (),
						parentObjectHelperProvider ().objectTypeId ()));

				}
				*/

				List<Record<?>> list =
					session.createQuery (

						stringFormat (

							"FROM %s _%s ",
							objectClass ().getSimpleName (),
							objectName (),

							"WHERE _%s.%s.id = :parentId ",
							objectName (),
							parentFieldName (),

							"AND _%s.%s = :index",
							objectName (),
							indexFieldName ()))

						.setInteger (
							"parentId",
							parentGlobalId.objectId ())

						.setInteger (
							"index",
							index)

						.list ();

				if (list.isEmpty ())
					return null;

				return (Record<?>)
					list.get (0);

			} else {

				List<?> list =

					session.createQuery (
						stringFormat (

							"FROM %s _%s ",
							objectClass ().getSimpleName (),
							objectName (),

							"WHERE _%s.%s.id = :parentTypeId ",
							objectName (),
							model.parentTypeField ().name (),

							"AND _%s.%s = :parentId ",
							objectName (),
							model.parentIdField ().name (),

							"AND _%s.%s = :index",
							objectName (),
							indexFieldName ()))

					.setInteger (
						"parentTypeId",
						parentGlobalId.typeId ())

					.setInteger (
						"parentId",
						parentGlobalId.objectId ())

					.setInteger (
						"index",
						index)

					.list ();

				if (list.isEmpty ())
					return null;

				return (Record<?>)
					list.get (0);

			}

		}

		@Override
		public
		Record<?> findByParentAndTypeAndCode (
				GlobalId parentGlobalId,
				String typeCode,
				String code) {

			Session session =
				hibernateDatabase.currentSession ();

			if (! typeCodeExists ()) {

				throw new UnsupportedOperationException (
					stringFormat (
						"Object type %s has no type code",
						getClass ().getSimpleName ()));

			}

			if (model.isRooted ()) {

				if (
					notEqual (
						parentGlobalId,
						GlobalId.root)
				) {

					throw new IllegalArgumentException (
						stringFormat (
							"Invalid parent global id %s for rooted object in %s.%s",
							parentGlobalId,
							getClass ().getSimpleName (),
							"findByParentAndCode"));

				}

				List<?> list =

					session.createQuery (
						stringFormat (

							"FROM %s _%s ",
							objectClass ().getSimpleName (),
							objectName (),

							"WHERE _%s.%s = :%s ",
							objectName (),
							typeCodeFieldName (),
							typeCodeFieldName (),

							"AND _%s.%s = :%s",
							objectName (),
							codeFieldName (),
							codeFieldName ()))

					.setString (
						typeCodeFieldName (),
						typeCode)

					.setString (
						codeFieldName (),
						code)

					.list ();

				if (list.isEmpty ())
					return null;

				return (Record<?>) list.get (0);

			} else if (model.parentTypeIsFixed ()) {

				/*
				if (parentGlobalId.getTypeId ()
						!= parentObjectHelperProvider ().objectTypeId ()) {

					throw new IllegalArgumentException (sf (
						"Invalid parent type id %s for %s (should be %s)",
						parentGlobalId.getTypeId (),
						objectClass ().getSimpleName (),
						parentObjectHelperProvider ().objectTypeId ()));

				}
				*/

				List<?> list =

					session.createQuery (
						stringFormat (

							"FROM %s _%s ",
							objectClass ().getSimpleName (),
							objectName (),

							"WHERE _%s.%s.id = :parentId ",
							objectName (),
							parentFieldName (),

							"AND _%s.%s = :%s ",
							objectName (),
							typeCodeFieldName (),
							typeCodeFieldName (),

							"AND _%s.%s = :%s",
							objectName (),
							codeFieldName (),
							codeFieldName ()))

					.setInteger (
						"parentId",
						parentGlobalId.objectId ())

					.setString (
						typeCodeFieldName (),
						typeCode)

					.setString (
						codeFieldName (),
						code)

					.list ();

				if (list.isEmpty ())
					return null;

				return (Record<?>)
					list.get (0);

			} else {

				List<?> list =

					session.createQuery (
						stringFormat (

							"FROM %s _%s ",
							objectClass ().getSimpleName (),
							objectName (),

							"WHERE _%s.%s.id = :parentTypeId ",
							objectName (),
							model.parentTypeField ().name (),

							"AND _%s.%s = :parentId ",
							objectName (),
							model.parentIdField ().name (),

							"AND _%s.%s = :%s ",
							objectName (),
							typeCodeFieldName (),
							typeCodeFieldName (),

							"AND _%s.%s = :code",
							objectName (),
							codeFieldName ()))

					.setInteger (
						"parentTypeId",
						parentGlobalId.typeId ())

					.setInteger (
						"parentId",
						parentGlobalId.objectId ())

					.setString (
						typeCodeFieldName (),
						typeCode)

					.setString (
						codeFieldName (),
						code)

					.list ();

				if (list.isEmpty ())
					return null;

				return (Record<?>) list.get (0);

			}

		}

		@Override
		public
		List<Record<?>> findAll () {

			Session session =
				hibernateDatabase.currentSession ();

			return session.createQuery (
				"FROM " + objectClass ().getName ())

				.list ();

		}

		@Override
		public
		List<Record<?>> findAllByParent (
				GlobalId parentGlobalId) {

			Session session =
				hibernateDatabase.currentSession ();

			if (model.isRooted ()) {

				if (
					notEqual (
						parentGlobalId,
						GlobalId.root)
				) {

					throw new IllegalArgumentException (
						stringFormat (
							"Invalid parent global id %s for rooted object in %s.%s",
							parentGlobalId,
							getClass ().getSimpleName (),
							"findChildren"));

				}

				return session.createQuery (

					"FROM " + objectClass ().getName ())

					.list ();

			} else if (model.canGetParent ()) {

				/*

				TODO enable this again

				if (parentGlobalId.getTypeId ()
						!= parentObjectHelperProvider ().objectTypeId ()) {

					throw new IllegalArgumentException (sf (
						"Invalid parent type id %s for %s (should be %s)",
						parentGlobalId.getTypeId (),
						objectClass ().getSimpleName (),
						parentObjectHelperProvider ().objectTypeId ()));

				}

				*/

				return session.createQuery (

					stringFormat (
						"FROM %s _%s ",
						objectClass ().getSimpleName (),
						objectName (),

						"WHERE _%s.%s.id = :parentId",
						objectName (),
						parentFieldName ()))

					.setInteger (
						"parentId",
						parentGlobalId.objectId ())

					.list ();

			} else {

				return session.createQuery (

					stringFormat (

						"FROM %s _%s ",
						objectClass ().getSimpleName (),
						objectName (),

						"WHERE _%s.%s.id = :parentTypeId ",
						objectName (),
						model.parentTypeField ().name (),

						"AND _%s.%s = :parentId",
						objectName (),
						model.parentIdField ().name ()))

					.setInteger (
						"parentTypeId",
						parentGlobalId.typeId ())

					.setInteger (
						"parentId",
						parentGlobalId.objectId ())

					.list ();

			}

		}

		@Override
		public
		Record insert (
				Record object) {

			objectHooks.beforeInsert (
				object);

			Session session =
				hibernateDatabase.currentSession ();

			session.save (
				object);

			objectHooks.afterInsert (
				object);

			return object;

		}

		@Override
		public
		Record update (
				Record object) {

			objectHooks.beforeUpdate (
				object);

			return object;

		}

		@Override
		public
		List<Integer> searchIds (
				Object search) {

			return objectHooks.searchIds (
				search);

		}

		@Override
		public <ObjectType extends EphemeralRecord<?>>
		ObjectType remove (
				ObjectType object) {

			Session session =
				hibernateDatabase.currentSession ();

			session.delete (
				object);

			return object;

		}

		@Override
		public
		String getName (
				Record<?> object) {

			return model.getName (object);

		}

		@Override
		public
		String getCode (
				Record<?> object) {

			return model.getCode (
				object);

		}

		@Override
		public
		String getTypeCode (
				Record<?> object) {

			return model.getTypeCode (
				object);

		}

		@Override
		public
		String getDescription (
				Record<?> object) {

			return model.getDescription (
				object);

		}

		@Override
		public
		Record<?> getParentType (
				Record<?> object) {

			return
				model.getParentType (
					object);

		}

		@Override
		public
		Integer getParentId (
				Record<?> object) {

			return model.getParentId (
				object);

		}

		@Override
		public
		Record<?> getParent (
				Record<?> object) {

			return model.getParent (
				object);

		}

		@Override
		public
		void createSingletons (
				ObjectHelper<?> objectHelper,
				ObjectHelper<?> parentHelper,
				Record<?> parentObject) {

			objectHooks.createSingletons (
				objectHelper,
				parentHelper,
				parentObject);

		}

		@Override
		public
		void setParent (
				Record<?> object,
				Record<?> parent) {

			BeanLogic.setProperty (
				object,
				model.parentField ().name (),
				parent);

			// TODO grand parent etc

		}

		@Override
		public
		String objectName () {

			return model.objectName ();

		}

		@Override
		public
		boolean getDeleted (
				Record<?> object) {

			try {

				return (Boolean)
					BeanLogic.getProperty (
						object,
						"deleted");

			} catch (Exception exception) {

				return false;

			}

		}

		@Override
		public
		Model model () {
			return model;
		}

		@Override
		public
		List<Record<?>> findAllByParentAndType (
				GlobalId parentGlobalId,
				String typeCode) {

			Session session =
				hibernateDatabase.currentSession ();

			if (model.isRooted ()) {

				if (
					notEqual (
						parentGlobalId,
						GlobalId.root)
				) {

					throw new IllegalArgumentException (
						stringFormat (
							"Invalid parent global id %s for rooted object in %s.%s",
							parentGlobalId,
							getClass ().getSimpleName (),
							"findChildren"));

				}

				return session.createQuery (

					stringFormat (
						"FROM %s",
						objectClass ().getName ()))

					.list ();

			}

			if (! typeCodeExists ()) {
				throw new RuntimeException ();
			}

			if (model.canGetParent ()) {

				/*
				if (parentGlobalId.getTypeId ()
						!= parentObjectHelperProvider ().objectTypeId ()) {

					throw new IllegalArgumentException (sf (
						"Invalid parent type id %s for %s (should be %s)",
						parentGlobalId.getTypeId (),
						objectClass ().getSimpleName (),
						parentObjectHelperProvider ().objectTypeId ()));

				}
				*/

				return session.createQuery (

					stringFormat (

						"FROM %s _%s ",
						objectClass ().getSimpleName (),
						objectName (),

						"WHERE _%s.%s.id = :parentId ",
						objectName (),
						parentFieldName (),

						"AND _%s.%s = :%s",
						objectName (),
						typeCodeFieldName (),
						typeCodeFieldName ()))

					.setInteger (
						"parentId",
						parentGlobalId.objectId ())

					.setString (
						typeCodeFieldName (),
						typeCode)

					.list ();

			} else {


				return session.createQuery (

					stringFormat (

						"FROM %s _%s ",
						objectClass ().getSimpleName (),
						objectName (),

						"WHERE _%s.%s.id = :parentTypeId ",
						objectName (),
						model.parentTypeField ().name (),

						"AND _%s.%s = :parentId",
						objectName (),
						model.parentIdField ().name (),

						"AND _%s.%s = :%s",
						objectName (),
						typeCodeFieldName (),
						typeCodeFieldName ()))

					.setInteger (
						"parentTypeId",
						parentGlobalId.typeId ())

					.setInteger (
						"parentId",
						parentGlobalId.objectId ())

					.setString (
						typeCodeFieldName (),
						typeCode)

					.list ();

			}

		}

		@Override
		public <RecordType extends Record<?>>
		RecordType lock (
				RecordType object) {

			Session session =
				hibernateDatabase.currentSession ();

			session.flush ();

			session.refresh (
				object,
				LockOptions.UPGRADE);

			return object;

		}

		@Override
		public
		Object getDynamic (
				Record<?> object,
				String name) {

			 return objectHooks.getDynamic (
				 object,
				 name);

		}

		@Override
		public
		void setDynamic (
				Record<?> object,
				String name,
				Object value) {

			objectHooks.setDynamic (
				object,
				name,
				value);

		}

	}

}
