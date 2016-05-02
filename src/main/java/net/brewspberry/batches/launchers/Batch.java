package net.brewspberry.batches.launchers;

public interface Batch {

	
	
	public void execute (String[] batchParams);
	public void setBatchParams(String[] batchParams);
	
}
