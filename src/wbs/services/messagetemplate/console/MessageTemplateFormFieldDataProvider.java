package wbs.services.messagetemplate.console;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.record.Record;
import wbs.services.messagetemplate.model.MessageTemplateParameterRec;
import wbs.services.messagetemplate.model.MessageTemplateTypeRec;

@PrototypeComponent ("messageTemplateFormFieldDataProvider")
public 
class MessageTemplateFormFieldDataProvider 
	implements FormFieldDataProvider {
	
	String mode;

	@Override
	public String getFormFieldData(Record<?> parent) {
		String formFieldData = "";
		
		MessageTemplateTypeRec messageTemplateType =
			(MessageTemplateTypeRec) parent;
		 
		for (MessageTemplateParameterRec messageTemplateParameter : messageTemplateType.getMessageTemplateParameters()) {
			
			if (messageTemplateParameter.getLength() != null) {
			
				formFieldData += messageTemplateParameter.getName()+"="+messageTemplateParameter.getLength().toString()+"&";
			
			}
			else {
				
				formFieldData += messageTemplateParameter.getName()+"="+0+"&";
				
			}
		}
		 
		formFieldData += "minimumTemplateLength="+messageTemplateType.getMinLength()+"&";
		formFieldData += "maximumTemplateLength="+messageTemplateType.getMaxLength();
		
		return formFieldData;
	}
	
	@Override
	public FormFieldDataProvider setMode (String modeSet) {
		
		mode = modeSet;	
		return this;
		
	}

	@SingletonComponent("messageTemplateFormFieldDataProviderConfig")
	public static
	class Config {

		@Inject
		Provider<MessageTemplateFormFieldDataProvider> messageTemplateFormFieldDataProvider;
		
		@PrototypeComponent ("messageTemplateSettingsFormFieldDataProvider")
		
		public
		FormFieldDataProvider messageTemplateSettingsFormFieldDataProvider () {

			return messageTemplateFormFieldDataProvider.get ()
				.setMode ("settings");

		}
		

	}

}
