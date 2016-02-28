package net.brewspberry.batches.tasks;

import net.brewspberry.batches.exceptions.NotTheGoodNumberOfArgumentsException;

public interface Task extends Runnable {

	public boolean checkSpecificParameters (Object[] specs) throws NotTheGoodNumberOfArgumentsException;
	public void buildSpecificParameters (String specs);
	public void setWriteParameters(String entityToWrite); 
}
