package net.brewspberry.batches.tasks;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import net.brewspberry.util.LogManager;

public class RecordTemperatureFromFileTask implements Task {
	/**
	 * RecordTemperatureFromFileTask represents 1 temperature record.
	 *  
	 * It can be launched in a separate thread. 
	 * 
	 */
	
	
	
	DS18b20TemperatureMeasurementParser parser = DS18b20TemperatureMeasurementParser.getInstance();
	String[] filesToRead;
	Map<String, Integer> valuesMap = new HashMap<String, Integer>();
	
	IGenericService<Brassin> brassinService = new BrassinServiceImpl();
	IGenericService<Etape> etapeService = new EtapeServiceImpl();
	IGenericService<Actioner> actionerService = new ActionerServiceImpl();
	IGenericService<TemperatureMeasurement> tmesService = new TemperatureMeasurementServiceImpl();
	
	String specificParameters = null;
	List<TemperatureMeasurement> temperatureMeasurement = new ArrayList<TemperatureMeasurement>();	
	
	
	public RecordTemperatureFromFileTask(String specificParameters) {
		super();
		
		/*
		 * Specific parameters are  : 
		 * * 
		 * 
		 */
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
				
				Brassin brassin = brassinService.getElementById(specificParametersArray[0]);
				Etape etape = etapeService.getElementById(specificParametersArray[1]);
				Actioner actioner = actionenrService.getElementById(specificParametersArray[2]);
				

				Iterator<Entry> entries = valuesMap.entrySet().iterator(); 

				int i = 0;
				while (entries.hasNext()){
					
					TemperatureMeasurement tmes = new TemperatureMeasurement();
					Entry entry = (Entry) entries.next();
					

					tmes.setTmes_brassin(brassin);
					tmes.setTmes_etape(etape);
					tmes.setTmes_actioner(actioner);
					tmes.setTmes_date(new Date());
					

					tmes.setTmes_probeUI (entry.getKey());
					tmes.setTmes_value (entry.getValue());
					tmes.setTmes_probe_name ("PROBE"+i);
					
					i++;
					
					temperatureMeasurement.add(tmes);
					
				}
				
				if (temperatureMeasurement.size() > 0){
					
					
					String lineToAddToCSV = this.formatDataForCSVFile(temperatureMeasurement);
					
					
					Iterator<TemperatureMeasurement> it = temperatureMeasurement.iterator();
					
					
					while (it.hasNext()){
						
						TemperatureMeasurement tmesToRec = it.next();
						
						try {
							
							tmesService.save(tmesToRec);
							
						} catch(Exception e){
							logger.severe("Could not record this measurement : UUID="+tmesToRec.getTmes_probe_UI());
							
							e.printStackTrace();
						}
					}					
				}
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
	
	
	public List<String> formatDataForCSVFile (List<TemperatureMeasurement> tmes){
		
		String lineResult = new String();
		
		List<String> result = new ArrayList<String>();
		logger.fine(tmes.size()+" temperatures to write");
		
		Iterator<TemperatureMeasurement> it = tmes.iterator();
		
		
		while (it.hasNext()){
	
			TemperatureMeasurement tmesU = it.next();
			
			lineResult = sdf.format(new Date()) + ";"
					+ String.valueOf(tmesU.getTmes_brassin().getBra_id())+";"
					+ String.valueOf(tmesU.getTmes_etape().getEtp_id())+";"
					+ String.valueOf(tmesU.getTmes_actioner().getAct_id())+";"
					+ String.valueOf(tmesU.getTmes_probe_UI())+";"
					+ String.valueOf(tmesU.getTmes_value());
			
			result.add(lineResult);
		}
		
		return result;
			
		
	}
	
	
	
	private synchronized void writeCSV(String str){
		
		
		try {
			Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Constants.DS18B20), "utf-8"));
			
			
			writer.write(str);
			
			
		} catch (Exception e){
			
			logger.severe("Could not write line to file "+Constants.DS18B20);
			
		}
	}
	
	
}
