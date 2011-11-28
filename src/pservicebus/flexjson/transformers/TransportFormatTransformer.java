package pservicebus.flexjson.transformers;

import flexjson.transformer.*;
import pservicebus.runtime.*;

public class TransportFormatTransformer extends AbstractTransformer {
	public void transform(Object object) {
		getContext().write( Integer.toString(((TransportFormat)object).getCode() ));
	}
}
