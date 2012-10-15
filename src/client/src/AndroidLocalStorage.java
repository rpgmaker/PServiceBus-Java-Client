import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


class AndroidLocalStorage implements ILocalStorage {

	private static Object context, preferences, editor;
	private static Method putStringMethod, getStringMethod,
		removeKeyMethod, applyMethod;
	static {
		Class<?> activityThreadClass;
		try {
			activityThreadClass = Class.forName("android.app.ActivityThread");
		    Method method = activityThreadClass.getMethod("currentApplication");
		    context = method.invoke(null, (Object[]) null);		
		    preferences = context.getClass()
		    		.getMethod("getSharedPreferences").invoke(context,
		    				"PServiceBus_Settings", 0);
		    editor = preferences.getClass()
		    		.getMethod("edit").invoke(preferences);
		    Class<?> editorType = editor.getClass(),
		    		preferenceType = preferences.getClass();
		    putStringMethod = editorType.getMethod("putString");
		    removeKeyMethod = editorType.getMethod("remove");
		    applyMethod = editorType.getMethod("apply");
		    getStringMethod = preferenceType.getMethod("getString");
		} catch (ClassNotFoundException e) {} 
		catch (NoSuchMethodException e) {} 
		catch (SecurityException e) {} 
		catch (IllegalAccessException e) {} 
		catch (IllegalArgumentException e) {} 
		catch (InvocationTargetException e) {}
	}
	
	@Override
	public void set(String key, String value) {
		try {
			putStringMethod.invoke(editor, key, value);
		    applyMethod.invoke(editor);
		} catch (IllegalAccessException e) {} 
		catch (IllegalArgumentException e) {} 
		catch (InvocationTargetException e) {}
	}

	@Override
	public String get(String key) {
		String value = null;
		try {
			value = (String)getStringMethod.invoke(preferences, key);
		} catch (IllegalAccessException e) {} 
		catch (IllegalArgumentException e) {} 
		catch (InvocationTargetException e) {}
		return value;
	}
	
	@Override
	public void remove(String key){
		try {
			removeKeyMethod.invoke(editor, key);
		    applyMethod.invoke(editor);
		} catch (IllegalAccessException e) {} 
		catch (IllegalArgumentException e) {} 
		catch (InvocationTargetException e) {}
	}

	@Override
	public String getType(){
		return "Android";
	}
}
