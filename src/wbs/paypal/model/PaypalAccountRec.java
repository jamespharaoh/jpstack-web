package wbs.paypal.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.DeletedField;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.NameField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;
import wbs.platform.scaffold.model.SliceRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id" )
@MajorEntity
public
class PaypalAccountRec
	implements MajorRecord<PaypalAccountRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	SliceRec slice;

	@CodeField
	String code;

	// details

	@NameField
	String name;

	@DescriptionField
	String description;

	@DeletedField
	Boolean deleted = false;
	
	@SimpleField
	String serviceEndpoint = "service endpoint"; 
	
	@SimpleField
	String clientId = "client id";
	
	@SimpleField
	String clientSecret = "client secret";

	// compare to

	@Override
	public
	int compareTo (
			Record<PaypalAccountRec> otherRecord) {

		PaypalAccountRec other =
			(PaypalAccountRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getSlice (),
				other.getSlice ())

			.append (
				getCode (),
				other.getCode ())
				
			.append (
				getCode (),
				other.getServiceEndpoint ())
				
			.append (
				getCode (),
				other.getClientId ())
				
			.append (
				getCode (),
				other.getClientSecret ())

			.toComparison ();

	}

}

