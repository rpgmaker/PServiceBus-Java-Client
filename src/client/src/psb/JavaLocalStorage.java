package psb;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

class JavaLocalStorage implements ILocalStorage {

	private Preferences pref;
	
	public JavaLocalStorage(){
		pref = Preferences.userRoot();
		pref = pref.node(System.getProperty("user.dir"));
	}
	
	private void flush(){
		try {
			pref.flush();
		} catch (BackingStoreException e) {}
	}
	
	@Override
	public void set(String key, String value) {
		pref.put(key, value);
		flush();
	}

	@Override
	public String get(String key) {
		return pref.get(key, null);
	}
	
	@Override
	public void remove(String key){
		pref.remove(key);
		flush();
	}

	@Override
	public String getType(){
		return "Java";
	}
}