package net.brewspberry.batches.launchers;

import java.util.logging.Logger;

import net.brewspberry.batches.tasks.RecordTemperatureFromFileTask;
import net.brewspberry.batches.tasks.Task;
import net.brewspberry.util.LogManager;

public class BatchRecordTemperatures implements Batch {

	Logger logger = LogManager.getInstance(BatchRecordTemperatures.class.getName());
	Task currentTask = null;
	
	public BatchRecordTemperatures (String[] args){
		/**
		 * Parameters to pass for this batch : 
		 * 0- 
		 * 1- 
		 * 2- 
		 * 3- 
		 * 4- 
		 * 5- 
		 */
		
		if (args.length != 5){
			
			logger.severe("Could not initialize batch, number of arguments wrong : "+args.length+" (5 expected)");
			System.exit(1);
		} else {
			
			
			
		}
		
		
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
				
				currentTask = new RecordTemperatureFromFileTask(String.join(" ",batchParams));
				
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
