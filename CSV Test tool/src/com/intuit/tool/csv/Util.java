/**
 * 
 */
package com.intuit.tool.csv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

/**
 *  * @author sumit_choudhary  * @since Sep 3, 2014  *  *  
 */
public class Util {

	private static String CONFIG_FILES = "config.properties";
	private static String REPORT_FILE = "match.report";

	private static Map<String, List<Object>> primaryFile = null;
	private static Map<String, List<Object>> secondaryFile = null;

	private static Map<String, String> config = new HashMap<String, String>();
	private static Map<Integer, Integer> mapping = new HashMap<Integer, Integer>();
	private static Properties properties;
	private static final String PRIMARY_COLUMN_INDEX_KEY = "primary.column";
	private static final String MAPPING_COLUMN_KEY = "column.mapping";
	private static final String LINE_IGNORE_CHAR_KEY = "line.ingnore.char";
	private static final String INGNORE_CHAR_KEY = "ingnore.char";

	private void loadConfiguration() {
		InputStream in = this.getClass().getClassLoader()
				.getResourceAsStream(CONFIG_FILES);

		properties = new Properties();
		loadMappingConfiguraution();
		REPORT_FILE = REPORT_FILE+"_"+new Date().getTime();
		
		try {
			properties.load(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void loadMappingConfiguraution() {
		String mappings = properties.getProperty(MAPPING_COLUMN_KEY);

		String[] maps = mappings.split(",");

		for (String map : maps) {

			map = map.trim();

			String[] token = map.split("->");

			if (token.length == 2) {

				mapping.put(Integer.valueOf(token[0].trim()),
						Integer.valueOf(token[2].trim()));

			} else {
				System.out.println("[CONFIG]bad map:" + map);
			}

		}

	}

	public void compare(String csv1, String csv2) {

		Map<String, List<String>> primaryData = loadCSV(csv1,
				Integer.valueOf(properties
						.getProperty(PRIMARY_COLUMN_INDEX_KEY)));

		Map<String, List<String>> secondaryData = loadCSV(csv1,
				Integer.valueOf(properties
						.getProperty(PRIMARY_COLUMN_INDEX_KEY)));

		for(String primarykey : primaryData.keySet() ){
			List<String> pData = primaryData.get(primarykey);
			List<String> sData = secondaryData.get(primarykey);
			
			if(pData==null || sData==null){
				logInreportFile("Key:"+ primarykey+", incorrect data");
			}else{
				compareData(primarykey, pData, sData);
			}
		}
		
	}

	private boolean compareData(String key, List<String> primaryData,
			List<String> secondaryData) {

		String ignoreChar = properties.getProperty(INGNORE_CHAR_KEY);

		for (Integer primaryKey : mapping.keySet()) {
			Integer secondKey = mapping.get(primaryKey);

			String primaryValue = primaryData.get(primaryKey - 1);
			String secondValue = secondaryData.get(secondKey - 1);

			if (ignoreChar != null && ignoreChar.length() > 0) {
				primaryValue = primaryValue.replace(ignoreChar, "");
				secondValue = secondValue.replace(ignoreChar, "");
			}

			if (!primaryValue.equals(secondValue)) {
				String log = "Key:" + key + " data mismatch at column ("
						+ primaryKey + "," + secondKey + ")" + " value("
						+ primaryValue + "," + secondValue + ")";
				
				logInreportFile(log);
			}

		}

		return false;
	}

	private void logInreportFile(String data) {

		BufferedWriter writer = null;

		try {
			writer = new BufferedWriter(new FileWriter(REPORT_FILE));

			writer.append(data);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private Map<String, List<String>> loadCSV(String csvFilePath,
			int primaryColumnIndex) {
		// Get scanner instance
		Scanner scanner = null;
		Map<String, List<String>> retu = null;
		try {
			scanner = new Scanner(new File(csvFilePath));
			retu = new HashMap<String, List<String>>();
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (line == null
						|| line.length() == 0
						|| line.startsWith(properties
								.getProperty(LINE_IGNORE_CHAR_KEY))) {
					continue;
				}
				String[] fields = line.split(",");
				if (fields.length < primaryColumnIndex) {
					System.out
							.println("[CONFIG] invalid input, primary column not matching number of columns");
					return null;
				} else {

					List<String> data = Arrays.asList(fields);
					String primary = fields[primaryColumnIndex + 1];

					if (primary != null && primary.length() > 0) {
						retu.put(primary, data);
					}

				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			scanner.close();
		}

		return retu;

	}

}
