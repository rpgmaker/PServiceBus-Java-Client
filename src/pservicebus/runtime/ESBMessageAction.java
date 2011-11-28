package pservicebus.runtime;


public interface ESBMessageAction<T> {
	public void handle(T message);
}
