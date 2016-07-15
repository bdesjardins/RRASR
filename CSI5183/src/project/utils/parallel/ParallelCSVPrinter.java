package project.utils.parallel;

import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

/**
 * 
 * @author bdesjardins
 * 
 * CSV printer with synchronized methods to avoid deadlock when
 * writing from multiple threads
 */
public class ParallelCSVPrinter{

	CSVPrinter printer;

	public ParallelCSVPrinter(FileWriter writer){
		try {
			printer = new CSVPrinter(writer, CSVFormat.EXCEL);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	public synchronized void print(String toWrite){
		try {
			printer.print(toWrite);
			printer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized void println(){
		try {
			printer.println();
			printer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized void writeEntry(String[] entry){
		try {
			for (int i = 0; i < entry.length; i++) {
				printer.print(entry[i]);
			}
			printer.println();
			printer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close(){
		try {
			printer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
