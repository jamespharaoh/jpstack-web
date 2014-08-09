package wbs.framework.record;

public
interface MinorRecord<
	ConcreteType extends PermanentRecord<ConcreteType>
>
	extends PermanentRecord<ConcreteType> {

}
