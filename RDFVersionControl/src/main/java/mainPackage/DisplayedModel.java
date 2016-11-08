package mainPackage;

import java.io.File;
import java.util.List;

import org.apache.jena.rdf.model.Model;

public class DisplayedModel {

	private String modelIdentifier;
	private String fileName;

	//Constructor
	public DisplayedModel(Model inputModel,String dir,List<String> numbers){
		setModelIdentifier(numbers);
		fileName = "File-displayedModel1" + modelIdentifier;
		SparqlUtils.model2File(inputModel,dir + File.separator + fileName);
	}

	//Methods
	
	private void setModelIdentifier(List<String> numbers){
		modelIdentifier = SparqlUtils.createHash(numbers);
	}

	public String getFileName(){
		return fileName;
	}

	public String getModelIdentifier(){
		return modelIdentifier;
	}

}
