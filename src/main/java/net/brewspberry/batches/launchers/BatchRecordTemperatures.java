package net.brewspberry.batches.launchers;

import net.brewspberry.batches.tasks.RecordTemperatureFromFileTask;
import net.brewspberry.batches.tasks.Task;

public class BatchRecordTemperatures implements Batch {

	
	Task currentTask = null;
	
	private static void main (String[] args){
		
		
	}
	/**
	 * Params order :
	 * - 0 LaunchType : ONCE, SECOND, MINUTE, HOUR, INDEFINITE 
	 * - 1-n Specific argument 
	 */
	public void execute(String[] batchParams) {

		if (batchParams[0] != null){
			
			switch (batchParams[0]){

			case "ONCE" :
				
				currentTask = new RecordTemperatureFromFileTask();
				
				Thread t = new Thread((Runnable) currentTask);
				
				
				break;

			case "SECOND" :
				
				
				break;

			case "MINUTE" :
				
				
				break;


			case "HOUR" :
				
				
				break;


			case "INDEFINITE" :
				
				
				break;
			
			
			}
		}
		
		
		
	}

}
