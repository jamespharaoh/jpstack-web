package wbs.framework.object;

import java.util.List;

import wbs.framework.entity.record.EphemeralRecord;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.TaskLogger;

public
interface ObjectDatabaseHelper <RecordType extends Record <RecordType>> {

	// getters

	ObjectModel <RecordType> objectModel ();

	// setters

	ObjectDatabaseHelper <RecordType> objectModel (
			ObjectModel <RecordType> objectModel);

	// database

	RecordType find (
			Long id);

	List <RecordType> findMany (
			List <Long> ids);

	RecordType findByParentAndCode (
			GlobalId parentGlobalId,
			String code);

	List <RecordType> findManyByParentAndCode (
			GlobalId parentGlobalId,
			List <String> codes);

	RecordType findByParentAndIndex (
			GlobalId parentGlobalId,
			Long index);

	RecordType findByParentAndTypeAndCode (
			GlobalId parentGlobalId,
			String typeCode,
			String code);

	List <RecordType> findAll ();

	List <RecordType> findAllByParent (
			GlobalId parentGlobalId);

	List <RecordType> findByParentAndIndexRange (
			GlobalId parentGlobalId,
			Long indexStart,
			Long indexEnd);

	List <RecordType> findAllByParentAndType (
			GlobalId parentGlobalId,
			String typeCode);

	RecordType insert (
			TaskLogger taskLogger,
			RecordType object);

	RecordType insertSpecial (
			TaskLogger taskLogger,
			RecordType object);

	RecordType update (
			RecordType object);

	<ObjectType extends EphemeralRecord <RecordType>>
	ObjectType remove (
			ObjectType object);

	RecordType lock (
			RecordType object);

}
