package pservicebus.runtime;

import java.io.*;

public class ExceptionHelper {
	public static void printStackTrace(Exception ex){
		StringWriter sw = new StringWriter();
		ex.printStackTrace(new PrintWriter(sw));
		System.out.println(sw.toString());
	}
}
