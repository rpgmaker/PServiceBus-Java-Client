package pservicebus.flexjson.transformers;

import flexjson.transformer.*;
import pservicebus.runtime.mssql.*;

public class MSSQLCommandTypeTransformer extends AbstractTransformer {
	public void transform(Object object) {
		getContext().write( Integer.toString(((MSSQLCommandType)object).getCode() ));
	}
}
