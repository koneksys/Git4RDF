package mainPackage;

import java.util.Arrays;
import java.util.List;

public class Settings {

	public static String FILEFORMAT = "N-TRIPLES";
	public static final int GRAPHLAYOUT = 5;
	public static boolean QVF = true;//QuarterVisualizationFormat
	static List<String> formats = Arrays.asList("RDF/XML","RDF/XML-ABBREV","RDF/JSON","JSON-LD","N-TRIPLES","N-TRIPLE","NT","TTL","TURTLE","Turtle","N3");
	public static String authorID = "DefaultAuthor";
	public static String printFormat = "%1$-60s";
	
	public static void printSettings(){
		System.out.println("File format: " + FILEFORMAT);
		System.out.println("Author ID: " + authorID);
		System.out.println("Quarter Visualization: " + QVF);
		System.out.println("Print format: " + printFormat);
	}
}
