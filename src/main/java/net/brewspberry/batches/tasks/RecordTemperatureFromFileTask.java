package net.brewspberry.batches.tasks;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import net.brewspberry.batches.exceptions.NotTheGoodNumberOfArgumentsException;
import net.brewspberry.batches.util.DS18b20TemperatureMeasurementParser;
import net.brewspberry.business.IGenericService;
import net.brewspberry.business.beans.Actioner;
import net.brewspberry.business.beans.Brassin;
import net.brewspberry.business.beans.Etape;
import net.brewspberry.business.beans.TemperatureMeasurement;
import net.brewspberry.business.service.ActionerServiceImpl;
import net.brewspberry.business.service.BrassinServiceImpl;
import net.brewspberry.business.service.EtapeServiceImpl;
import net.brewspberry.business.service.TemperatureMeasurementServiceImpl;
import net.brewspberry.util.ConfigLoader;
import net.brewspberry.util.Constants;
import net.brewspberry.util.LogManager;

public class RecordTemperatureFromFileTask implements Task {
	/**
	 * RecordTemperatureFromFileTask represents 1 temperature record.
	 * 
	 * It can be launched in a separate thread.
	 * 
	 */

	DS18b20TemperatureMeasurementParser parser = DS18b20TemperatureMeasurementParser
			.getInstance();
	List<Path> filesToRead;
	Map<String, Integer> valuesMap = new HashMap<String, Integer>();

	IGenericService<TemperatureMeasurement> tmesService = new TemperatureMeasurementServiceImpl();

	String entityToWrite = "ALL";

	Object[] specificParameters = null;
	List<TemperatureMeasurement> temperatureMeasurement = new ArrayList<TemperatureMeasurement>();


	private Logger logger = LogManager
			.getInstance(DS18b20TemperatureMeasurementParser.class.getName());

	public static SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	
	

	public RecordTemperatureFromFileTask(Object[] specificParameters) {
		super();

		/*
		 * Specific parameters are :
		 */

		this.specificParameters = specificParameters;
	}
	public void run() {

		logger.info("Thread started");
		try {
			filesToRead = parser.findFilesToOpen();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		logger.info("Found "+filesToRead.size()+" files :");
		Iterator<Path> itP = filesToRead.iterator();

		while (itP.hasNext()) {
			String file = Paths.get(itP.next().toUri()).toString();
			logger.info(file);

			if (file != null) {

				Integer temperature = parser.parseTemperature(file);
				String uuid = parser.getProbeUUIDFromFileName(file);

				valuesMap.put(uuid, temperature);
			}
		}

		logger.info("Could find " + valuesMap.size() + " devices");

		try {
			if (checkSpecificParameters(specificParameters)) {

				Iterator<Entry<String, Integer>> entries = valuesMap.entrySet()
						.iterator();

				int i = 0;
				while (entries.hasNext()) {

					TemperatureMeasurement tmes = new TemperatureMeasurement();
					Entry<String, Integer> entry = entries.next();

					tmes.setTmes_brassin((Brassin) specificParameters[0]);
					tmes.setTmes_etape((Etape) specificParameters[1]);
					tmes.setTmes_actioner((Actioner) specificParameters[2]);
					tmes.setTmes_date(new Date());

					tmes.setTmes_probeUI(entry.getKey());
					tmes.setTmes_value(Float.valueOf(entry.getValue()));
					tmes.setTmes_probe_name("PROBE" + i);

					i++;

					temperatureMeasurement.add(tmes);

				}

				if (temperatureMeasurement.size() > 0) {

					if (entityToWrite.equals("ALL")
							|| entityToWrite.equals("FILE")) {
						logger.info("Saving in File");

						List<String> linesToAddToCSV = this
								.formatDataForCSVFile(temperatureMeasurement);

						Iterator<String> it2 = linesToAddToCSV.iterator();
						while (it2.hasNext()) {

							this.writeCSV(it2.next());

						}

					}
					if (entityToWrite.equals("ALL")
							|| entityToWrite.equals("SQL")) {
						Iterator<TemperatureMeasurement> it = temperatureMeasurement
								.iterator();

						while (it.hasNext()) {
							TemperatureMeasurement tmesToRec = it.next();

							try {
								logger.info("Saving in DB");
								tmesService.save(tmesToRec);

							} catch (Exception e) {
								logger.severe("Could not record this measurement : UUID="
										+ tmesToRec.getTmes_probeUI());

								e.printStackTrace();
							}

						}
					}
				}
			}
		} catch (NotTheGoodNumberOfArgumentsException e) {

			e.printStackTrace();
		}
	}

	/**
	 * Checks validity of specific parameters transmitted.
	 * 
	 * @throws NotTheGoodNumberOfArgumentsException
	 */
	public boolean checkSpecificParameters(Object[] specs)
			throws NotTheGoodNumberOfArgumentsException {

		logger.fine("Got this : ");

		if (specs[0] instanceof Brassin && specs[1] instanceof Etape
				&& specs[2] instanceof Actioner) {
			for (Object param : specs) {

				logger.fine("| " + param);

			}

			if (specs.length == 3) {

				logger.info("Parameters : Brew=" + specs[0] + " Step="
						+ specs[1] + " Actioner=" + specs[2]);

				return true;

			} else {
				throw new NotTheGoodNumberOfArgumentsException();
			}
		}
		else {
			return false;
		}
	}

	public void buildSpecificParameters(String specs) {
		// TODO Auto-generated method stub

	}

	public List<String> formatDataForCSVFile(List<TemperatureMeasurement> tmes) {

		String lineResult = new String();

		List<String> result = new ArrayList<String>();
		logger.fine(tmes.size() + " temperatures to write");

		if (tmes.size() > 0) {
			Iterator<TemperatureMeasurement> it = tmes.iterator();

			while (it.hasNext()) {

				TemperatureMeasurement tmesU = it.next();

				lineResult = sdf.format(new Date()) + ";"
						+ String.valueOf(tmesU.getTmes_brassin().getBra_id())
						+ ";"
						+ String.valueOf(tmesU.getTmes_etape().getEtp_id())
						+ ";"
						+ String.valueOf(tmesU.getTmes_actioner().getAct_id())
						+ ";" + String.valueOf(tmesU.getTmes_probe_name())
						+ ";" + String.valueOf(tmesU.getTmes_value());

				result.add(lineResult);
			}
		}
		return result;
	}

	/**
	 * Writes str to Constants.DS18B20_CSV file
	 * 
	 * @param str
	 *            string to write
	 */

	private synchronized void writeCSV(String str) {
		Writer writer = null;

		try {
			writer = new BufferedWriter(new OutputStreamWriter(

			new FileOutputStream(ConfigLoader.getConfigByKey(
					Constants.CONFIG_PROPERTIES,
					"files.measurements.temperature")), "utf-8"));

			writer.write(str);

		} catch (Exception e) {
			logger.severe("Could not write line to file "
					+ ConfigLoader.getConfigByKey(Constants.CONFIG_PROPERTIES,
							"files.measurements.temperature"));
		} finally {

			try {
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Sets where to write temperatures.
	 * 
	 * Can be for example FILE for file writing, SQL for DB record, ALL for both
	 * 
	 * @param entityToWrite
	 */
	public void setWriteParameters(String entityToWrite) {

		if (entityToWrite != null) {

			if (Arrays.asList(Constants.WRITABLE_ENTITIES).contains(
					entityToWrite)) {

				this.entityToWrite = entityToWrite;
			}
		}

	}
}
