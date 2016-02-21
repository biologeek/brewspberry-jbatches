package net.brewspberry.batches.tasks;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
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
import net.brewspberry.util.LogManager;

public class RecordTemperatureFromFileTask implements Task {

	
	DS18b20TemperatureMeasurementParser parser = DS18b20TemperatureMeasurementParser.getInstance();
	String[] filesToRead;
	Map<String, Integer> valuesMap = new HashMap<String, Integer>();
	
	IGenericService<Brassin> brassinService = new BrassinServiceImpl();
	IGenericService<Etape> etapeService = new EtapeServiceImpl();
	IGenericService<Actioner> actionenrService = new ActionerServiceImpl();
	
	String specificParameters = null;
	TemperatureMeasurement temperatureMeasurement = new TemperatureMeasurement();	
	
	
	public RecordTemperatureFromFileTask(String specificParameters) {
		super();
		this.specificParameters = specificParameters;
	}

	private Logger logger = LogManager.getInstance(DS18b20TemperatureMeasurementParser.class.getName());
	
	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public void run() {
		

		filesToRead = parser.findFilesToOpen();
		
		
		for (String file : filesToRead){
			
			if (file != null){
				
				Integer temperature = parser.parseTemperature(file);
				String uuid = parser.getProbeUUIDFromFileName(file);
				
				valuesMap.put(uuid, temperature);
			}
		}
		
		try {
			if (checkSpecificParameters(specificParameters)){
				
				String[] specificParametersArray = specificParameters.split(" ");
				
				Brassin brassin = brassinService.getElementById()
				temperatureMeasurement.setTmes_brassin();
				
				
				
			}
		} catch (NotTheGoodNumberOfArgumentsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		
	}
	/**
	 * Checks validity of specific parameters transmitted.
	 * @throws NotTheGoodNumberOfArgumentsException 
	 */
	public boolean checkSpecificParameters(String specs) throws NotTheGoodNumberOfArgumentsException {

		
		String[] params = specs.split(" ");
		
		if (params.length == 3){
			
			logger.info ("Parameters : Brew ID="+params[0]+" Step ID="+params[1]+" Actioner ID="+params[2]);
			
			return true;
			
		}
		else {			
			throw new NotTheGoodNumberOfArgumentsException ();
		}
			
	}

	public void buildSpecificParameters(String specs) {
		// TODO Auto-generated method stub
		
	}
	
	
	
}
