package net.brewspberry.batches.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import net.brewspberry.batches.tasks.reqult;
import net.brewspberry.util.Constants;
import net.brewspberry.util.LogManager;

public class DS18b20TemperatureMeasurementParser {

	public static DS18b20TemperatureMeasurementParser instance = null;

	public static int threadSleepMillis = 100;

	private Logger logger = LogManager
			.getInstance(DS18b20TemperatureMeasurementParser.class.getName());

	public static DS18b20TemperatureMeasurementParser getInstance() {

		if (instance == null) {
			instance = new DS18b20TemperatureMeasurementParser();
		}

		return instance;
	}

	public File loadFile(String path) {
		File result = null;

		if (path != null) {

			try {
				result = new File(path);
			} catch (Exception e) {

				logger.severe("Could not open file : " + path);

			}

		}

		return result;
	}

	/**
	 * Searching and returning all files in /sys/bus/devices/28* /w1_slave
	 * 
	 * @return array of files
	 */
	public String[] findFilesToOpen() {
		String[] files;
		File dir = new File(Constants.DS18B20_DEVICES_FOLDER);

		files = dir.list(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				return name.matches(Constants.DS18B20_DIR_PATTERN);
			}
		});

		for (String file : files) {
			file = file + "/" + Constants.DS18B20_FILE_NAME;
		}

		return files;
	}

	public synchronized int parseTemperature(String file) {

		int result = 0;

		if (file != null) {

			File fileF = new File(file);
			if (fileF.exists() && !fileF.isDirectory()) {

				try {

					List<String> content = Files.readAllLines(Paths.get(file));

					if (content.size() == 2) {
						while (!content.get(0)
								.substring(content.get(0).length() - 3)
								.equals("YES")) {

							Thread.sleep(threadSleepMillis);
						}
						
						
						String resultLine = content.get(1).substring(
								content.get(1).length() - 5);

						result = Integer.parseInt(resultLine);

					}
				} catch (IOException e) {

					e.printStackTrace();
				}
			}

		}
		return result;

	}

	public String getProbeUUIDFromFileName(String file) {

		String result;
		String UUIDPattern = Pattern.compile("28-????????????").pattern();

		String[] fileSplit = file.split("/");

		for (String str : fileSplit) {

			if (str.matches(UUIDPattern)) {

				if (result == null) {
					result = str;
				} else {

					logger.severe("Not only one result found... Path incorrect !!");

				}
			}
		}
		return result;

	}
}
