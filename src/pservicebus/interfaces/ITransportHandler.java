package pservicebus.interfaces;

public interface ITransportHandler<TTransport extends ITransport> {
	public int getInterval();
	public void setInterval(int interval);
	public int getBatchSize();
	public void setBatchSize(int batchSize);
	public <TMessage> IMessageHandler<TMessage> createHandler(Class<TMessage> messageType, ITransport transport);
}
