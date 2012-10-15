
public interface ILocalStorage {
	public void set(String key, String value);
	public void remove(String key);
	public String get(String key);
	public String getType();
}
