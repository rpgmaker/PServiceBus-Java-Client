package psb;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


class AndroidLocalStorage implements ILocalStorage {

	private static Object context, preferences, editor;
	private static Method putStringMethod, getStringMethod,
		removeKeyMethod, applyMethod;
	static {
		try {
		    context = LocalStorage.getContext();
		    Method prefMethod = context.getClass()
		    		.getDeclaredMethod("getSharedPreferences");
		    preferences = prefMethod.invoke(context,
		    				"PServiceBus_Settings", 0);
		    editor = preferences.getClass()
		    		.getDeclaredMethod("edit").invoke(preferences);
		    Class<?> editorType = editor.getClass(),
		    		preferenceType = preferences.getClass();
		    putStringMethod = editorType.getDeclaredMethod("putString");
		    removeKeyMethod = editorType.getDeclaredMethod("remove");
		    applyMethod = editorType.getDeclaredMethod("apply");
		    getStringMethod = preferenceType.getDeclaredMethod("getString");
		}
		catch (NoSuchMethodException e) {} 
		catch (SecurityException e) {} 
		catch (IllegalAccessException e) {} 
		catch (IllegalArgumentException e) {} 
		catch (InvocationTargetException e) {}
	}
	
	@Override
	public void set(String key, String value) {
		return;
		/*
		try {
			putStringMethod.invoke(editor, key, value);
		    applyMethod.invoke(editor);
		} catch (IllegalAccessException e) {} 
		catch (IllegalArgumentException e) {} 
		catch (InvocationTargetException e) {}
		*/
	}

	@Override
	public String get(String key) {
		return null;
		/*
		String value = null;
		try {
			value = (String)getStringMethod.invoke(preferences, key);
		} catch (IllegalAccessException e) {} 
		catch (IllegalArgumentException e) {} 
		catch (InvocationTargetException e) {}
		return value;
		*/
	}
	
	@Override
	public void remove(String key){
		return;
		/*
		try {
			removeKeyMethod.invoke(editor, key);
		    applyMethod.invoke(editor);
		} catch (IllegalAccessException e) {} 
		catch (IllegalArgumentException e) {} 
		catch (InvocationTargetException e) {}
		*/
	}

	@Override
	public String getType(){
		return "Android";
	}
}
