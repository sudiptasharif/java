package domain;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class FileProcessor {
	

	public ArrayList<String> read(String fileName) throws IOException {
		ArrayList<String> lineList = new ArrayList<String>();
		String line = "";
		
		FileReader fileReader = new FileReader(fileName);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		while((line=bufferedReader.readLine()) != null) {
			lineList.add(line);
		}
		bufferedReader.close();
		return lineList;
	}
	public void write(String load, String location, Boolean append) throws IOException {
		FileWriter fileWriter = new FileWriter(location, append); // appends to existing file if true is passed
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		bufferedWriter.write(load);
		bufferedWriter.close();
	}

	public void diffTwoXPathFiles(String inputFile1, String inputFile2, String outputFile) throws IOException {
		ArrayList<String> fileOneXPaths = null;
		ArrayList<String> fileTwoXPaths = null;
		
		ArrayList<String> biggerFileWithXPaths = null;
		ArrayList<String> smallerFileWithXPaths = null;
		
		String fileDiff = "", bigFileXPath = "";
		
		fileOneXPaths = read(inputFile1);
		fileTwoXPaths = read(inputFile2);
		
		int size = 0, fileOneSize = fileOneXPaths.size(), fileTwoSize = fileTwoXPaths.size();
		size = Math.max(fileOneSize, fileTwoSize);
		if(size == fileOneSize) {
			biggerFileWithXPaths = fileOneXPaths;
			smallerFileWithXPaths = fileTwoXPaths;
		}else {
			biggerFileWithXPaths = fileTwoXPaths;
			smallerFileWithXPaths = fileOneXPaths;
		}
		
		for(int i=0; i<size; i++) {
			bigFileXPath = biggerFileWithXPaths.get(i);
			if(!smallerFileWithXPaths.contains(bigFileXPath)) {
				fileDiff += bigFileXPath + System.lineSeparator();
			}
		}
		write(fileDiff, outputFile, false);
	}
	
	public void makeNotInSQLClause(String inputFile, String outputFile) throws IOException {
		ArrayList<String> xpaths = null;
		String result = "", xpath="";
		int size = 0;
		xpaths = read(inputFile);
		size = xpaths.size();
		
		for(int i=0; i<size; i++) {
			xpath = xpaths.get(i);
			
			if(i == size-1) {
				result += quoatedStr(xpath);
			}else {
				result += quoatedStr(xpath) + ","+System.lineSeparator(); 
			}
		}
		result = "NOT IN ("+result+")";
		
		write(result, outputFile, false);
	}
	
	private String quoatedStr(String str) {
		return "\'"+str+"\'";
	}
}
