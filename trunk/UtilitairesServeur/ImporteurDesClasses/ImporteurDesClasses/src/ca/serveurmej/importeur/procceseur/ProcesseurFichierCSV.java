package ca.serveurmej.importeur.procceseur;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProcesseurFichierCSV {
	
	private String nomFichier = "";
	private String nomFichierReponse = "";
	
	public ProcesseurFichierCSV(String nomFichier) {
		this.nomFichier = nomFichier;
	}	
	
	public ProcesseurFichierCSV() {
		// exit
	}
	
	public void processe(){
		
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
	 
		try {
	 
			br = new BufferedReader(new FileReader(nomFichier));
			while ((line = br.readLine()) != null) {
	 
			    // use comma as separator
				String[] country = line.split(cvsSplitBy);
	 
				System.out.println("Country [code= " + country[4] 
	                                 + " , name=" + country[5] + "]");
	 
			}
	 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	 
		System.out.println("Done");
	}

}
