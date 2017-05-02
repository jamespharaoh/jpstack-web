package wbs.framework.object;

import java.util.List;

import wbs.framework.database.Transaction;
import wbs.framework.entity.record.EphemeralRecord;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;

public
interface ObjectDatabaseHelper <RecordType extends Record <RecordType>> {

	// getters

	ObjectModel <RecordType> objectModel ();

	// setters

	ObjectDatabaseHelper <RecordType> objectModel (
			ObjectModel <RecordType> objectModel);

	// database

	RecordType find (
			Transaction parentTransaction,
			Long id);

	List <RecordType> findMany (
			Transaction parentTransaction,
			List <Long> ids);

	RecordType findByParentAndCode (
			Transaction parentTransaction,
			GlobalId parentGlobalId,
			String code);

	List <RecordType> findManyByParentAndCode (
			Transaction parentTransaction,
			GlobalId parentGlobalId,
			List <String> codes);

	RecordType findByParentAndIndex (
			Transaction parentTransaction,
			GlobalId parentGlobalId,
			Long index);

	RecordType findByParentAndTypeAndCode (
			Transaction parentTransaction,
			GlobalId parentGlobalId,
			String typeCode,
			String code);

	List <RecordType> findAll (
			Transaction parentTransaction);

	List <RecordType> findNotDeleted (
			Transaction parentTransaction);

	List <RecordType> findAllByParent (
			Transaction parentTransaction,
			GlobalId parentGlobalId);

	List <RecordType> findByParentAndIndexRange (
			Transaction parentTransaction,
			GlobalId parentGlobalId,
			Long indexStart,
			Long indexEnd);

	List <RecordType> findAllByParentAndType (
			Transaction parentTransaction,
			GlobalId parentGlobalId,
			String typeCode);

	RecordType insert (
			Transaction parentTransaction,
			RecordType object);

	RecordType insertSpecial (
			Transaction parentTransaction,
			RecordType object);

	RecordType update (
			Transaction parentTransaction,
			RecordType object);

	<ObjectType extends EphemeralRecord <RecordType>>
	ObjectType remove (
			Transaction parentTransaction,
			ObjectType object);

	RecordType lock (
			Transaction parentTransaction,
			RecordType object);

}
