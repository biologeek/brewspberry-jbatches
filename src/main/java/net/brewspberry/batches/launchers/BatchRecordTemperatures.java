package net.brewspberry.batches.launchers;

import java.util.logging.Logger;

import javax.naming.spi.DirStateFactory.Result;

import net.brewspberry.batches.tasks.RecordTemperatureFromFileTask;
import net.brewspberry.batches.tasks.Task;
import net.brewspberry.business.IGenericService;
import net.brewspberry.business.beans.Actioner;
import net.brewspberry.business.beans.Brassin;
import net.brewspberry.business.beans.Etape;
import net.brewspberry.business.service.ActionerServiceImpl;
import net.brewspberry.business.service.BrassinServiceImpl;
import net.brewspberry.business.service.EtapeServiceImpl;
import net.brewspberry.util.ConfigLoader;
import net.brewspberry.util.Constants;
import net.brewspberry.util.LogManager;

public class BatchRecordTemperatures implements Batch, Runnable {

	Logger logger = LogManager.getInstance(BatchRecordTemperatures.class
			.getName());
	Task currentTask = null;

	IGenericService<Brassin> brassinService = new BrassinServiceImpl();
	IGenericService<Etape> etapeService = new EtapeServiceImpl();
	IGenericService<Actioner> actionerService = new ActionerServiceImpl();
	String[] batchParams;

	public BatchRecordTemperatures() {
		/**
		 * Parameters to pass for this batch : 0- 1- 2- 3- 4- 5-
		 */

		logger.fine("Dont forget to set batch params !");

	}

	public BatchRecordTemperatures(String[] args) {
		/**
		 * Parameters to pass for this batch : 0- 1- 2- 3- 4- 5-
		 */
		if (args.length != 5) {

			logger.severe("Could not initialize batch, number of arguments wrong : "
					+ args.length + " (5 expected)");
			System.exit(1);
		}
		this.batchParams = args;

	}

	/**
	 * Params order : - 0 LaunchType : ONCE, SECOND, MINUTE, HOUR, INDEFINITE -
	 * 1 Launch length 2-n Specific argument
	 */
	public void run() {
		this.execute(batchParams);
	}

	private Object[] getTaskParameters(String[] batchParams, int firstElement) {
		String[] specParams = new String[3];
		Object[] result = new Object[3];


		logger.info("Params : "+String.valueOf(String.join(";",batchParams)+" | "+String.join(";",specParams)));

		if (batchParams.length > 0 && batchParams.length > firstElement) {

			System.arraycopy(batchParams, firstElement, specParams, 0,
					result.length);

			Brassin brassin = brassinService.getElementById(Long
					.parseLong(specParams[0]));
			Etape etape = etapeService.getElementById(Long
					.parseLong(specParams[1]));
			Actioner actioner = actionerService.getElementById(Long
					.parseLong(specParams[2]));

			result[0] = brassin;
			result[1] = etape;
			result[2] = actioner;
			logger.fine("Transmitting " + result[0]+ result[1]+ result[2]+ " to task");

		}

		return result;
	}

	public void setBatchParams(String[] batchParams) {

		this.batchParams = batchParams;

	}

	@Override
	public void execute(String[] batchParams) {

		Object[] taskParams = this.getTaskParameters(batchParams, 2);

		Integer threadSleep = Integer.parseInt(ConfigLoader.getConfigByKey(
				Constants.PROJECT_ROOT_PATH + "/" + Constants.BREW_CONF
						+ "/batches.properties",
				"brewspberry.batches.threads.delay"));
		
		
		double timeLength;
		long startTime;

		
		logger.fine("Thread sleep param : " + threadSleep);
		if (batchParams[0] != null) {

			
			switch (batchParams[0]) {

			case "ONCE":
				try {
					currentTask = new RecordTemperatureFromFileTask(taskParams);

					currentTask.setWriteParameters("ALL");

					Thread t = new Thread((Runnable) currentTask);

					t.start();
				} catch (Throwable t) {
					t.printStackTrace();
				}
				break;

			case "SECOND":

				timeLength = Double.parseDouble(batchParams[1]) * 1000;

				startTime = System.currentTimeMillis();

				while ((System.currentTimeMillis() - startTime) < timeLength) {

					logger.info("--BEGIN : Time length : " + timeLength
							+ " | Real : "
							+ (System.currentTimeMillis() - startTime));

					try {
						new Thread(
								(Runnable) new RecordTemperatureFromFileTask(
										taskParams)).start();
						Thread.sleep(threadSleep);
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

				break;

			case "MINUTE":
				try {

					logger.info("Recording temperatures for "+batchParams[1]+" minutes");
					timeLength = Double.parseDouble(batchParams[1]) * 1000.0 * 60.0;
					startTime = System.currentTimeMillis();

					currentTask = new RecordTemperatureFromFileTask(taskParams);
					currentTask.setWriteParameters("ALL");

					while ((System.currentTimeMillis() - startTime) < timeLength) {

						try {
							Thread t = new Thread((Runnable) currentTask);
							t.start();
							Thread.sleep(threadSleep);
						} catch (Throwable e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} catch (Throwable t) {
					t.printStackTrace();
				}

				break;

			case "HOUR":
				try {
					logger.info("Recording temperatures for "+batchParams[1]+" hours");

					timeLength = Double.parseDouble(batchParams[1]) * 1000.0 * 3600.0;
					startTime = System.currentTimeMillis();

					currentTask = new RecordTemperatureFromFileTask(taskParams);
					currentTask.setWriteParameters(String.join("ALL"));

					while ((System.currentTimeMillis() - startTime) < timeLength) {

						try {
							new Thread((Runnable) currentTask).start();
							Thread.sleep(threadSleep);
						} catch (Throwable e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				} catch (Throwable t) {
					t.printStackTrace();
				}
				break;

			case "INDEFINITE":

				break;

			}
		}

		logger.info("Exiting program");

	}

}
