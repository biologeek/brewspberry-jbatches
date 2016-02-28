package net.brewspberry.batches.main;

import java.util.logging.Logger;

import org.jboss.jandex.Main;

import net.brewspberry.batches.launchers.Batch;
import net.brewspberry.batches.launchers.BatchRecordTemperatures;
import net.brewspberry.util.LogManager;

public class MainExec {

	private static final Logger logger = LogManager.getInstance(MainExec.class.getName());

	public static void main(String[] args) {

		if (args.length > 0) {

			switch (args[0]) {

			case "batchRecordTemperatures":

				String[] batchParams = new String[args.length - 1];

				System.arraycopy(args, 1, batchParams, 0, args.length - 1);
				logger.info ("Launching batchRecordTemperatures batch");

				try {
					Batch recordTemperature = new BatchRecordTemperatures(
							batchParams);
					System.exit(0);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.exit(1);
				}

				break;
			}

		}

	}

}
