package net.brewspberry.batches.main;

import net.brewspberry.batches.exceptions.NotTheGoodNumberOfArgumentsException;
import net.brewspberry.batches.launchers.Batch;
import net.brewspberry.batches.launchers.BatchRecordTemperatures;

public class MainExec {

	public static void main(String[] args) {

		if (args.length > 0) {

			switch (args[0]) {

			case "batchRecordTemperatures":

				String[] batchParams = new String[args.length - 1];

				System.arraycopy(args, 1, batchParams, 0, args.length - 1);

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
