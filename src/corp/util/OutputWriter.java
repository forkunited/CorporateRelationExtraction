package corp.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class OutputWriter {
	private BufferedWriter debugWriter;
	private BufferedWriter resultsWriter;
	private BufferedWriter dataWriter;
	
	public OutputWriter() {
		this.debugWriter = null;
		this.resultsWriter = null;
		this.dataWriter = null;
	}
	
	public OutputWriter(File debugFile, File resultsFile, File dataFile) {
		try {
			this.debugWriter = new BufferedWriter(new FileWriter(debugFile.getAbsolutePath()));
			this.resultsWriter = new BufferedWriter(new FileWriter(resultsFile.getAbsolutePath()));
			this.dataWriter = new BufferedWriter(new FileWriter(dataFile.getAbsolutePath()));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	public void debugWriteln(String str) {
		debugWrite(str + "\n");
	}
	
	public void debugWrite(String str) {
		System.out.print(str);
		if (this.debugWriter == null)
			return;
			
		try {
			synchronized (this.debugWriter) {
				this.debugWriter.write(str);
				this.debugWriter.flush();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
	
	public void resultsWriteln(String str) {
		resultsWrite(str + "\n");
	}
	
	public void resultsWrite(String str) {
		if (this.resultsWriter == null) {
			System.out.println(str);
			return;
		}
		
		try {
			synchronized (this.resultsWriter) {
				this.resultsWriter.write(str);
				this.resultsWriter.flush();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
	
	public void dataWriteln(String str) {
		dataWrite(str + "\n");
	}
	
	public void dataWrite(String str) {
		if (this.dataWriter == null) {
			System.out.println(str);
			return;
		}
		
		try {
			synchronized (this.dataWriter) {
				this.dataWriter.write(str);
				this.dataWriter.flush();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
	
	public void close() {
		try {
			if (this.debugWriter != null)
				this.debugWriter.close();
			if (this.dataWriter != null)
				this.dataWriter.close();
			if (this.resultsWriter != null)
				this.resultsWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
}
