package net.brewspberry.batches.tasks;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
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

import javax.persistence.criteria.CriteriaBuilder.In;

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
	String[] filesToRead;
	Map<String, Integer> valuesMap = new HashMap<String, Integer>();

	IGenericService<Brassin> brassinService = new BrassinServiceImpl();
	IGenericService<Etape> etapeService = new EtapeServiceImpl();
	IGenericService<Actioner> actionerService = new ActionerServiceImpl();
	IGenericService<TemperatureMeasurement> tmesService = new TemperatureMeasurementServiceImpl();

	String entityToWrite = new String();

	String specificParameters = null;
	List<TemperatureMeasurement> temperatureMeasurement = new ArrayList<TemperatureMeasurement>();

	public RecordTemperatureFromFileTask(String specificParameters) {
		super();

		/*
		 * Specific parameters are : 
		 */
		
		

		this.specificParameters = specificParameters;
	}

	private Logger logger = LogManager
			.getInstance(DS18b20TemperatureMeasurementParser.class.getName());

	public static SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	public void run() {

		logger.info("Thread started");
		filesToRead = parser.findFilesToOpen();

		for (String file : filesToRead) {

			if (file != null) {

				Integer temperature = parser.parseTemperature(file);
				String uuid = parser.getProbeUUIDFromFileName(file);

				valuesMap.put(uuid, temperature);
			}
		}

		try {
			if (checkSpecificParameters(specificParameters)) {

				String[] specificParametersArray = specificParameters
						.split(" ");

				Brassin brassin = brassinService.getElementById(Long
						.parseLong(specificParametersArray[0]));
				Etape etape = etapeService.getElementById(Long
						.parseLong(specificParametersArray[1]));
				Actioner actioner = actionerService.getElementById(Long
						.parseLong(specificParametersArray[2]));

				Iterator<Entry<String, Integer>> entries = valuesMap.entrySet()
						.iterator();

				int i = 0;
				while (entries.hasNext()) {

					TemperatureMeasurement tmes = new TemperatureMeasurement();
					Entry<String, Integer> entry = entries.next();

					tmes.setTmes_brassin(brassin);
					tmes.setTmes_etape(etape);
					tmes.setTmes_actioner(actioner);
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
		
		System.exit(0);


	}

	/**
	 * Checks validity of specific parameters transmitted.
	 * 
	 * @throws NotTheGoodNumberOfArgumentsException
	 */
	public boolean checkSpecificParameters(String specs)
			throws NotTheGoodNumberOfArgumentsException {

		String[] params = specs.split(" ");

		if (params.length == 3) {

			logger.info("Parameters : Brew ID=" + params[0] + " Step ID="
					+ params[1] + " Actioner ID=" + params[2]);

			return true;

		} else {
			throw new NotTheGoodNumberOfArgumentsException();
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

		try {
			Writer writer = new BufferedWriter(new OutputStreamWriter(

					new FileOutputStream(ConfigLoader.getConfigByKey(
							Constants.CONFIG_PROPERTIES,
							"files.measurements.temperature")), "utf-8"));


			writer.write(str);

		} catch (Exception e) {
			logger.severe("Could not write line to file "
					+ Constants.DS18B20_CSV);
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
