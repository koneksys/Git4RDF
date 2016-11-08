package mainPackage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

public class SparqlScriptAnalyser {

	private static SparqlScript sparqlScript;
	private static CommitModificationMemory commitModiMem;

	//Constructor
	public SparqlScriptAnalyser(Model currentDisplayedModel, String inputString){
		sparqlScript = new SparqlScript();
		commitModiMem = new CommitModificationMemory();
		convertStringToScript(currentDisplayedModel,inputString);
	}

	public SparqlScriptAnalyser(SparqlScript inputSparqlScript){
		sparqlScript = new SparqlScript();
		commitModiMem = new CommitModificationMemory();
		sparqlScript = inputSparqlScript;
		commitModiMem.addDeleteTriples(inputSparqlScript.getDeleteTriples());
		commitModiMem.addInsertTriples(inputSparqlScript.getInsertTriples());
		analyseModificationStatements();
	}

	//compares triples for detection of editing actions
	private static List<String[]> getModifiedStatements(List<String[]> deleteTriples, List<String[]> insertTriples){
		List<String[]> modificationTriples = new ArrayList<String[]>();
		//compare triples
		for(String[] deleteStatement : deleteTriples){
			for(String[] insertStatement : insertTriples){
				if(Objects.equals(deleteStatement[0],insertStatement[0])){//subjects are equal
					if(Objects.equals(deleteStatement[1],insertStatement[1])){//predicates are equal
						//triple is modified
						modificationTriples.add(deleteStatement);
						modificationTriples.add(insertStatement);
					}
				}
			}
		}
		return modificationTriples;
	}

	//parses the triples of a file and creates the modification record
	private static void analyseModificationStatements(){
		List<String[]> modificationTriples = new ArrayList<String[]>();
		List<Integer> deleteStatementIndicator = new ArrayList<Integer>();
		List<Integer> insertStatementIndicator = new ArrayList<Integer>();

		if((!(commitModiMem.getDeleteTriples().isEmpty()))&&(!(commitModiMem.getInsertTriples().isEmpty()))){//both indeces were modified --> both clauses exists
			modificationTriples = getModifiedStatements(commitModiMem.getDeleteTriples(),commitModiMem.getInsertTriples());
			//compare delete and modification triples and prepare conforming triples for deletion in delete list
			for(String[] deleteTriple : commitModiMem.getDeleteTriples()){
				for(String[] modificationTriple : modificationTriples){
					if(Objects.equals(deleteTriple, modificationTriple)){
						deleteStatementIndicator.add(commitModiMem.getDeleteTriples().indexOf(deleteTriple));
					}
				}
			}
			//compare insert and modification triples and prepare conforming triples for deletion in insert list
			for(String[] insertTriple : commitModiMem.getInsertTriples()){
				for(String[] modificationTriple : modificationTriples){
					if(Objects.equals(insertTriple, modificationTriple)){
						insertStatementIndicator.add(commitModiMem.getInsertTriples().indexOf(insertTriple));
					}
				}
			}
			//delete conforming deletion triples in delete list
			List<String[]> deleteTriples2 = new ArrayList<String[]>();
			List<String[]> insertTriples2 = new ArrayList<String[]>();
			Iterator<Integer> indicatorIter = deleteStatementIndicator.iterator();
			int counter = 0;
			int nextIndicator = 0;
			while(indicatorIter.hasNext()){
				nextIndicator = indicatorIter.next();
				while(counter<=nextIndicator){
					if(counter<nextIndicator){
						deleteTriples2.add(commitModiMem.getDeleteTriples().get(counter));
					}
					counter++;
				}
			}
			while(counter<commitModiMem.getDeleteTriples().size()){
				deleteTriples2.add(commitModiMem.getDeleteTriples().get(counter));
				counter++;
			}
			//delete conforming insertion triples in insert list
			indicatorIter = insertStatementIndicator.iterator();
			counter = 0;
			nextIndicator = 0;
			while(indicatorIter.hasNext()){
				nextIndicator = indicatorIter.next();
				while(counter<=nextIndicator){
					if(counter<nextIndicator){
						insertTriples2.add(commitModiMem.getInsertTriples().get(counter));
					}
					counter++;
				}
			}
			while(counter<commitModiMem.getInsertTriples().size()){
				insertTriples2.add(commitModiMem.getInsertTriples().get(counter));
				counter++;
			}
			commitModiMem.updateDeleteTriples(deleteTriples2);
			commitModiMem.updateInsertTriples(insertTriples2);
			commitModiMem.addModifiedTriples(modificationTriples);	
		}
	}

	//compares script to respective model and deletes unnecessary triples
	private static void compareScriptToModel(Model currentDisplayedModel){
		SparqlScript spSc = new SparqlScript();
		spSc.addDeleteTriples(sparqlScript.getDeleteTriples());
		spSc.addInsertTriples(sparqlScript.getInsertTriples());
		List<String[]> deleteTriples = sparqlScript.getDeleteTriples();
		List<String[]> insertTriples = sparqlScript.getInsertTriples();
		List<String[]> deleteTriples2 = spSc.getDeleteTriples();
		List<String[]> insertTriples2 = spSc.getInsertTriples();
		
		Iterator<String[]> deleteIter = deleteTriples.listIterator();
		Iterator<String[]> insertIter = insertTriples.listIterator();
		StmtIterator stmtIter = currentDisplayedModel.listStatements();
		//check delete triples: if triple intended to be deleted is not included in the model, ignore it
		while(deleteIter.hasNext()){
			stmtIter = currentDisplayedModel.listStatements();
			boolean found = false;
			String[] nextDeleteTriple = deleteIter.next();
			String[] tempTriple = new String[3];
			tempTriple[0] = nextDeleteTriple[0];
			tempTriple[1] = nextDeleteTriple[1];
			tempTriple[2] = nextDeleteTriple[2];
			if(tempTriple[0].startsWith("<")) tempTriple[0] = tempTriple[0].substring(1,tempTriple[0].lastIndexOf(">"));
			if(tempTriple[1].startsWith("<")) tempTriple[1] = tempTriple[1].substring(1,tempTriple[1].lastIndexOf(">"));
			if(tempTriple[2].startsWith("<")) tempTriple[2] = tempTriple[2].substring(1,tempTriple[2].lastIndexOf(">"));
			if(tempTriple[2].startsWith("\"")) tempTriple[2] = tempTriple[2].substring(1,tempTriple[2].lastIndexOf("\""));
			while(stmtIter.hasNext()){
				Statement nextStmt = stmtIter.nextStatement();
				//compare sparql script triples and model triples
				if(Objects.equals(tempTriple[0], nextStmt.getSubject().toString())){
					if(Objects.equals(tempTriple[1], nextStmt.getPredicate().toString())){
						if(Objects.equals(tempTriple[2], nextStmt.getObject().toString())){
							found = true;
							break;
						}
					}
				}
			}
			if(!found){
				deleteTriples2.remove(nextDeleteTriple);
			}
		}
		
		//check insert triples: if triple intended to be inserted already exists in the model, ignore it
		stmtIter = currentDisplayedModel.listStatements();
		while(insertIter.hasNext()){
			stmtIter = currentDisplayedModel.listStatements();
			String[] nextInsertTriple = insertIter.next();
			String[] tempTriple = new String[3];
			tempTriple[0] = nextInsertTriple[0];
			tempTriple[1] = nextInsertTriple[1];
			tempTriple[2] = nextInsertTriple[2];
			if(tempTriple[0].startsWith("<")) tempTriple[0] = tempTriple[0].substring(1,tempTriple[0].lastIndexOf(">"));
			if(tempTriple[1].startsWith("<")) tempTriple[1] = tempTriple[1].substring(1,tempTriple[1].lastIndexOf(">"));
			if(tempTriple[2].startsWith("<")) tempTriple[2] = tempTriple[2].substring(1,tempTriple[2].lastIndexOf(">"));
			if(tempTriple[2].startsWith("\"")) tempTriple[2] = tempTriple[2].substring(1,tempTriple[2].lastIndexOf("\""));
			while(stmtIter.hasNext()){
				Statement nextStmt = stmtIter.nextStatement();
				if(Objects.equals(tempTriple[0], nextStmt.getSubject().toString())){
					if(Objects.equals(tempTriple[1], nextStmt.getPredicate().toString())){
						if(Objects.equals(tempTriple[2], nextStmt.getObject().toString())){
							insertTriples2.remove(nextInsertTriple);
						}
					}
				}
			}
		}
		
		sparqlScript.updateDeleteTriples(deleteTriples2);
		sparqlScript.updateInsertTriples(insertTriples2);
		commitModiMem.updateDeleteTriples(deleteTriples2);
		commitModiMem.updateInsertTriples(insertTriples2);
	}
	
	//scans clauses for triples and divides them into subparts (s,p,o)
	private static List<String[]> getStatements(String inputString){
		inputString = inputString.substring(inputString.indexOf("<"));
		String[] triples = inputString.split(" .\n|\n");
		if(triples[triples.length-1].contains("<")){
			triples[triples.length-1] = triples[triples.length-1].split(". }|}")[0];
		}
		List<String[]> tripleParts = new ArrayList<String[]>();
		boolean tripleSegment = false;
		String[] correctedTriple = new String[3];

		for(String string : triples){
			List<String> temp = new ArrayList<String>();
			if(string.contains("<")){
				if(!(string.contains("\""))){
					String[] currentTriple = string.split("<");
					for(String triplePart : currentTriple){
						if(triplePart.contains(">")){
							tripleSegment=true;
						}
						triplePart = "<" + triplePart.split(">")[0] + ">";
						if(tripleSegment){
							temp.add(triplePart);
							tripleSegment=false;
						}
					}
					temp.toArray(correctedTriple);
				}else{
					String[] objectDivider = string.split("\"");
					String[] subjectPredicateDivider = objectDivider[0].split("<");
					for(String triplePart : subjectPredicateDivider){
						if(triplePart.contains(">")){
							tripleSegment=true;
						}
						triplePart = "<" + triplePart.split(">")[0] + ">";
						if(tripleSegment){
							temp.add(triplePart);
							tripleSegment=false;
						}
					}
					String literal = "\"" + objectDivider[1].split("\"")[0] + "\"";
					temp.add(literal);
					temp.toArray(correctedTriple);
				}
				tripleParts.add(correctedTriple);
				correctedTriple = new String[3];
			}
		}
		return tripleParts;
	}

	//scans a file for delete and insert clauses
	private static int[] getActionClauses(String sparqlString,List<Integer> splitStringPositions){
		int prefixIndex = -1;//position of prefix triples in split array
		int deleteIndex = -1;//position of delete triples in split array
		int insertIndex = -1;//position of insert triples in split array
		int prefixPos = -8;//position of prefix clause in string
		int deletePos = -8;//position of delete clause in string
		int insertPos = -8;//position of insert clause in string

		//get string position of prefix statements
		if(sparqlString.contains("prefix"))
		{
			prefixPos = sparqlString.lastIndexOf("prefix");
		}
		//get string position of delete statements
		if(sparqlString.contains("delete"))
		{
			deletePos = sparqlString.lastIndexOf("delete");
		}
		//get string position of insert statements
		if(sparqlString.contains("insert"))
		{
			insertPos = sparqlString.lastIndexOf("insert");
		}
		//get indices list of delete and insert strings
		Iterator<Integer> stringPosIter = splitStringPositions.iterator();
		while(stringPosIter.hasNext()){
			int nextPos = stringPosIter.next();
			int prefixArea = prefixPos - nextPos;
			int deleteArea = deletePos - nextPos;
			int insertArea = insertPos - nextPos;	
			if(Math.abs(prefixArea)<7){
				prefixIndex = splitStringPositions.indexOf(nextPos);
			}
			if(Math.abs(deleteArea)<7){
				deleteIndex = splitStringPositions.indexOf(nextPos);
			}
			if(Math.abs(insertArea)<7){
				insertIndex = splitStringPositions.indexOf(nextPos);
			}
		}
		int[] statementIndices= {prefixIndex,deleteIndex,insertIndex};
		return statementIndices;
	}

	//analyse a sparql string and convert it to a sparql script object
	private static void convertStringToScript(Model currentDisplayedModel, String sparqlString){
		List<Integer> splitStringPositions = new ArrayList<Integer>();//positions of split strings in string
		String[] fileStatements = sparqlString.split("prefix|delete|insert|where");
		List<String[]> deleteTriples = new ArrayList<String[]>();
		List<String[]> insertTriples = new ArrayList<String[]>();

		//get start position of splitted strings
		for(String string : fileStatements){
			splitStringPositions.add(sparqlString.lastIndexOf(string));
		}
		//get start positions of important strings
		int[] statementIndices = getActionClauses(sparqlString,splitStringPositions);
		//add prefix clauses to sparql script object
		if(statementIndices[0]!=-1){//prefixIndex was changed from initital value, prefix string exists
			sparqlScript.addPrefixStrings(fileStatements[statementIndices[0]]);
		}
		//store single action triples
		if(statementIndices[1]!=-1){//deleteIndex was changed from initital value, delete clause exists
			deleteTriples = getStatements(fileStatements[statementIndices[1]]);
		}
		if(statementIndices[2]!=-1){//insertIndex was changed from initital value, insert clause exists
			insertTriples = getStatements(fileStatements[statementIndices[2]]);
		}
		
		//insert triples in sparql script
		sparqlScript.addDeleteTriples(deleteTriples);
		sparqlScript.addInsertTriples(insertTriples);
		commitModiMem.addDeleteTriples(deleteTriples);
		commitModiMem.addInsertTriples(insertTriples);

		//delete unnecessary triples
		compareScriptToModel(currentDisplayedModel);
		
		//analyse delete and insert triples to find modified triples
		analyseModificationStatements();
	}

	/**
	 * returns analysed SparqlScript
	 * @return
	 */
	public SparqlScript getSparqlScript(){
		return sparqlScript;
	}

	/**
	 * returns analysed SparqlScript
	 * @return
	 */
	public CommitModificationMemory getCommitModificationMemory(){
		return commitModiMem;
	}
}
