package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import domain.FileProcessor;

public class XPathFinderApp {

	public static void main(String[] args) throws IOException {
		// All conn string are stripped away for security reasons
		final String RETAILNET_EXPDB_CONN_STRING = ""; 
		final String HDDB_CONN_STRING = "";
		final String LOCAL_EXPDB_CONN_STRING = "";
		
//		String fileExt = getFileExtention();
//		if(fileExt.length() == 0) {
//			System.out.println("No File Type Selected.\nExiting Application.");
//			System.exit(0);
//		}
//		ArrayList<File> files = getFilesWithTargetExtension(fileExt);
//		if(files.size() == 0) {
//			System.out.println("No Files To Process.\nExiting Application.");
//			System.exit(0);
//		}
		
/*		XPathLoader loadedXPath = new XPathLoader(LOCAL_EXPDB_CONN_STRING,HDDB_CONN_STRING,RETAILNET_EXPDB_CONN_STRING);
		try {
			loadedXPath.loadXPathFromDB();
		} catch (SQLException e) {
			System.out.println("EXCEPTION THROWN: "+e.getMessage());
			System.exit(0);
		}*/
		
//		XPathFinder xpathFinder = null;
//		File file = null;
//		for(int i=0; i<files.size(); i++) {
//			file = files.get(i);
//			xpathFinder = new XPathFinder(file, loadedXPath);
//			try {
//				xpathFinder.processFile();
//			} catch (SQLException e) {
//				System.out.println("EXCEPTION THROWN: "+e.getMessage());
//			}
//		}
/*		try {
			loadedXPath.dumpXPathNotFoundWithHostInfoByXPath();
		} catch (SQLException e) {
			System.out.println("EXCEPTION THROWN: "+e.getMessage());
			System.exit(0);
		}*/
		String inputFile1 = "fileOne.txt", inputFile2 = "fileTwo.txt", outputFile = "diff.txt";
		FileProcessor fp = new FileProcessor();
		//fp.diffTwoXPathFiles(inputFile1, inputFile2, outputFile);
		//System.out.println("Done With Diff");
		inputFile1 = "input.txt";
		outputFile = "output.txt";
		fp.makeNotInSQLClause(inputFile1, outputFile);
		System.out.println("Done With Making the Not In SQL Clause.");
		
	}
	
	public static String getFileExtention() {
		final int FILETYPE_PASCAL = 0;
		final int FILETYPE_HTM = 1;
		final String FILEEXT_PASCAL = ".PAS";
		final String FILEEXT_HTM = ".HTM";	
		
		String msg;
		int selOpt;
		msg = "File Type:\nYes: .pas\nNo: .htm";
		selOpt = JOptionPane.showConfirmDialog(null, msg, "Input File Type Selection", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
		if(selOpt == FILETYPE_PASCAL) {
			return FILEEXT_PASCAL;
		}else if(selOpt == FILETYPE_HTM) {
			return FILEEXT_HTM;
		}else {
			return "";
		}
		
	}
	public static ArrayList<File> getFilesWithTargetExtension(String fileExtention){
		ArrayList<File> files = new ArrayList<File>();
		File selectedDir = null;
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setAcceptAllFileFilterUsed(false);
		int status = fileChooser.showOpenDialog(null);
		if(status == fileChooser.APPROVE_OPTION) {
			selectedDir = fileChooser.getSelectedFile();
			recursiveFileSearch(selectedDir, files, fileExtention);
		}
		return files;
	}
	
	public static void recursiveFileSearch(File dir, ArrayList<File> fileList, String targetFileExt) {
		File[] files = dir.listFiles();
		String fileNameUpCase = "";
		
		for(File file:files) {
			if(file.isDirectory() && !file.isHidden()) {
				recursiveFileSearch(file, fileList, targetFileExt);
			}else {
				fileNameUpCase = file.getAbsolutePath().toUpperCase();
				if(fileNameUpCase.endsWith(targetFileExt)) {
					fileList.add(file);
				}
			}
		}
	}
	public static void printFileNames(ArrayList<File> fileList) throws IOException {
		String fileName = "";
		File file;
		int size = fileList.size();
		System.out.println("TOTAL FILES FOUND = "+Integer.toString(size));
		for(int i = 0; i < size; i++) {
			file = fileList.get(i);
			fileName = file.getCanonicalPath();
			System.out.println(fileName);
		}
	}
	

	

}
