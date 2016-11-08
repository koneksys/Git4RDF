package mainPackage;


import org.apache.jena.rdf.model.*;

public class VCOnt {
	static String dirVCOnt = "http://example.com/VCOnt-directory/";
	
	static Model model = ModelFactory.createDefaultModel();
	
	static public Property contains			= model.createProperty("VCOnt:contains");
	static public Property isAbout			= model.createProperty("VCOnt:isAbout");
	static public Property backwardStream	= model.createProperty("VCOnt:backwardStream");
	static public Property Commit			= model.createProperty("VCOnt:Commit");
	static public Property Delta			= model.createProperty("VCOnt:Delta");
	static public Property Model			= model.createProperty("VCOnt:Model");
	static public Property Script			= model.createProperty("VCOnt:Script");
	static public Property Deletion			= model.createProperty("VCOnt:Deletion");
	static public Property Insertion		= model.createProperty("VCOnt:Insertion");
	static public Property Modification		= model.createProperty("VCOnt:Modification");
	static public Property Subject			= model.createProperty("VCOnt:Subject");
	static public Property Predicate		= model.createProperty("VCOnt:Predicate");
	static public Property Object			= model.createProperty("VCOnt:Object");
	static public Property Tag				= model.createProperty("VCOnt:Tag");
	static public Property Baseline			= model.createProperty("VCOnt:Baseline");
	static public Property authorID			= model.createProperty("VCOnt:Author");
	static public Property name				= model.createProperty("VCOnt:Name");

}
