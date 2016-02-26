package net.brewspberry.batches.launchers;

import net.brewspberry.batches.exceptions.NotTheGoodNumberOfArgumentsException;
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
			
			this.execute(args);
			
		}
		
	}

	/**
	 * Params order : - 0 LaunchType : ONCE, SECOND, MINUTE, HOUR, INDEFINITE -
	 * 2-n Specific argument
	 */
	public void execute(String[] batchParams) {

		String taskParams = this.getTaskParameters(batchParams, 2);

		long timeLength;
		long startTime;

		if (batchParams[0] != null) {

			switch (batchParams[0]) {

			case "ONCE":

				currentTask = new RecordTemperatureFromFileTask(taskParams);

				currentTask.setWriteParameters(String.join("ALL"));

				Thread t = new Thread((Runnable) currentTask);

				t.start();

				break;

			case "SECOND":

				timeLength = Long.parseLong(batchParams[1]) * 1000;
				startTime = System.currentTimeMillis();

				currentTask = new RecordTemperatureFromFileTask(taskParams);
				currentTask.setWriteParameters(String.join("ALL"));

				while ((System.currentTimeMillis() - startTime) < timeLength) {

					new Thread((Runnable) currentTask).start();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				break;

			case "MINUTE":

				timeLength = Long.parseLong(batchParams[1]) * 1000 * 60;
				startTime = System.currentTimeMillis();

				currentTask = new RecordTemperatureFromFileTask(taskParams);
				currentTask.setWriteParameters(String.join("ALL"));

				while ((System.currentTimeMillis() - startTime) < timeLength) {

					new Thread((Runnable) currentTask).start();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				break;

			case "HOUR":

				timeLength = Long.parseLong(batchParams[1]) * 1000 * 3600;
				startTime = System.currentTimeMillis();

				currentTask = new RecordTemperatureFromFileTask(taskParams);
				currentTask.setWriteParameters(String.join("ALL"));

				while ((System.currentTimeMillis() - startTime) < timeLength) {

					new Thread((Runnable) currentTask).start();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				break;

			case "INDEFINITE":

				break;

			}
		}

	}

	private String getTaskParameters(String[] batchParams, int firstElement) {

		String result = new String();
		if (batchParams.length > 0 && batchParams.length > firstElement) {

			for (int i = firstElement; i < batchParams.length; i++) {

				result = result + " " + batchParams[i];

			}

		}

		return result;
	}

}
