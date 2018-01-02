package parser.ProtocolValidationTest;

import java.util.ArrayList;

import java.util.List;
import java.util.Map;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import protocolosv2.Protocolosv2Package;
import protocolosv2.Operation;
import protocolosv2.Protocol;

public class SequenceParser {
	private Protocol protocol;	
	
	List<String> elements = new ArrayList<String>();
	
	//Constructor: initialize the protocol with XMI file.
	public SequenceParser(String file) {
		Protocolosv2Package.eINSTANCE.eClass();
		
		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
        Map<String, Object> m = reg.getExtensionToFactoryMap();
        m.put("xmi", new XMIResourceFactoryImpl());
    
        ResourceSet resSet = new ResourceSetImpl();
        Resource resouce = resSet.getResource(URI.createURI(file), true);
        this.protocol = (Protocol) resouce.getContents().get(0);
	}
	
	//Function to verify whether there are sequences with the same output step
	public boolean isThereSameOutputStep() {
		for (int i = 0; i < protocol.getSequence().size(); i++) {
			for (int j = i+1; j < protocol.getSequence().size(); j++) {
				if(protocol.getSequence().get(i).getOutputStep() == protocol.getSequence().get(j).getOutputStep()) {
					return true;
				}
			}
		}
		return false;
	}

	//Function to verify if the sequences with the same output step have the same condition guard
	//Use the choco solver to get possible solutions and return them
	public List<Solution> findNonDeterminism() {
		//Lista de operandos
		List<BoolVar>  boolVars = new ArrayList<BoolVar>();
		//List of operations with the operations of each sequence
		List<Operation> operations = new ArrayList<Operation>();
		//Initialize choco solver model
		Model model = new Model("Same Guard Condition");
		//Loop to get sequences with the same output step
		for (int i = 0; i < protocol.getSequence().size(); i++) {
			//Clauses to find possible solutions with choco solver
			//These clauses are the conditions guard
			List<LogOp> clauses = new ArrayList<LogOp>();
			
			for (int j = i+1; j < protocol.getSequence().size(); j++) {
				if(protocol.getSequence().get(i).getOutputStep() == protocol.getSequence().get(j).getOutputStep()) {	
					Operation opi = protocol.getSequence().get(i).getOperation();
					Operation opj = protocol.getSequence().get(j).getOperation();
					
					if(!operations.contains(opi)) {
						operations.add(opi);
						createClauses(boolVars, clauses, model, opi);
					}
					if(!operations.contains(opj)) {
						operations.add(opj);
						createClauses(boolVars, clauses, model, opj);
					}
				}
			}
			if (!clauses.isEmpty()) {
				//Add clauses list to the model
				List<LogOp> XorClauses = new ArrayList<LogOp>();
				System.out.println(clauses);
				for(int k = 1; k < clauses.size(); k++) {
					if(XorClauses.isEmpty()) {
						XorClauses.add(LogOp.xor(clauses.get(k-1), clauses.get(k)));
					}else {
						XorClauses.add(0, LogOp.xor(XorClauses.get(0), clauses.get(k)));
					}
					System.out.println(XorClauses.get(0));
				}
				System.out.println("add clauses to model...");
				model.addClauses(XorClauses.get(0));
				System.out.println("finally");
				List<Solution> solutions = new ArrayList<Solution>();
				//Get possible solutions
				while(model.getSolver().solve()) {
					solutions.add(model.getSolver().findSolution());
				}
				return solutions;
			}
		}
		return null;
	}
	
	
	//Create Clauses for one operation
	public void createClauses(List<BoolVar> boolVars, List<LogOp> clauses, Model model, Operation operation){
		int index[] = null;
		//Verify the operator of each operation.
		//Create the clauses according to the operator and 
		//add them on the clauses list
		switch (operation.getOperator()) {
			case AND:
			case OR:
			case IMPLIES:
			case XOR:
				index = createBoolVars(boolVars, model, operation);
				
				calcBoolVar(clauses, operation, boolVars.get(index[0]), boolVars.get(index[1]));
				break;
			
			case EQUAL:
			case EQUAL_OR_GREATER:
			case EQUAL_OR_SMALLER:
			case BIGGER_THAN:
			case SMALLER_THAN:
				IntVar[] intVars = createIntVars(model, 2);//ATENCAO
				
				calcIntVar(model, operation, intVars[0], intVars[1]);								
				break;
			case SUM:								
				break;							
			case MINUS:								
				break;								
			case MULTIPLICATION:								
				break;								
			case DIVISION:							
				break;								
			case NOT:
				index = createBoolVars(boolVars, model, operation);
				clauses.add(LogOp.nand(boolVars.get(index[0]), boolVars.get(index[0])));
				break;							
			case AFFIRMATION:								
				break;					
			default:
				index = createBoolVars(boolVars, model, operation);
				clauses.add(LogOp.nand(boolVars.get(index[0]), boolVars.get(index[0])));
				break;
		}
	}
	
	//create the IntVars
	public IntVar[] createIntVars(Model model, int k) {
		IntVar intA = model.intVar("A"+k,0,Integer.MAX_VALUE);
		findDeadlock(intA.getName(), elements);
		existVar(intA, null);
		
		IntVar intB = model.intVar("B"+k,0,Integer.MAX_VALUE); 
		findDeadlock(intB.getName(), elements);
		existVar(intB, null);
		
		IntVar[] intVars = {intA, intB};
		
		return intVars;
	}
	
	//create the BoolVars and return the index of the operation's operands.
	public int[] createBoolVars(List<BoolVar> boolVars, Model model, Operation operation) {
		int index[] = new int[operation.getOperand().size()];
		for(int i = 0; i < operation.getOperand().size(); i++) {
			Model model_aux = new Model("Axiliary Model");
			BoolVar boolVar = model_aux.boolVar(operation.getOperand().get(i).getName());
			if(!boolVarsContain(boolVars, boolVar)) {
				boolVars.add(model.boolVar(operation.getOperand().get(i).getName()));
				index[i] = boolVars.size() -1;
			}else {
				index[i] = boolVarIndexOf(boolVars, boolVar);
			}
		}
		return index;
	}
	
	private void existVar(IntVar intVar, BoolVar boolVar) {
		if(intVar != null) {
			
		}
		else if(boolVar != null) {
			
		}
	}

	//Find deadlock
	public boolean findDeadlock(String elem,List<String> elements){
		for (String object : elements) {
			if(elem.equals(object)) {
				System.out.println("DEADLOCK");
				return true;
			}
		}
		
		elements.add(elem);
		
		return false;
	}
	
	//calculate the int variables on choco solver
	public void calcIntVar(Model model, Operation op, IntVar intA, IntVar intB) {
		switch(op.getOperator()) {
			case EQUAL:
				model.arithm(intA,"==",intB);
				break;
			case EQUAL_OR_GREATER:
				model.arithm(intA,">=",intB);
				break;
			case EQUAL_OR_SMALLER:
				model.arithm(intA,"<=",intB);
				break;
			case BIGGER_THAN:
				model.arithm(intA,">",intB);
				break;
			case SMALLER_THAN:
				model.arithm(intA,"<",intB);
				break;
			default:
				break;
		}
	}
	
	//calculate the bool variables on choco solver
	public void calcBoolVar(List<LogOp> clauses, Operation op, BoolVar boolA, BoolVar boolB){
		switch(op.getOperator()) {
			case AND:
				clauses.add(LogOp.and(boolA,boolB));
				break;
			case OR:
				clauses.add(LogOp.or(boolA,boolB));
				break;
			case IMPLIES:
				clauses.add(LogOp.implies(boolA, boolB));
				break;
			case XOR:
				clauses.add(LogOp.xor(boolA,boolB));
				break;
			default:
				break;
		}
	}
	
	//Verify whether list boolVars already has the boolvar
	public boolean boolVarsContain(List<BoolVar> boolVars, BoolVar boolVar) {
		String bName = boolVar.getName();
		for(int i = 0; i < boolVars.size(); i++) {
			String bNames = boolVars.get(i).getName();
			if(bNames.equals(bName)) {
				return true;
			}
		}
		return false;
	}
	
	public int boolVarIndexOf(List<BoolVar> boolVars, BoolVar boolVar) {
		String bName = boolVar.getName();
		for(int i = 0; i < boolVars.size(); i++) {
			String bNames = boolVars.get(i).getName();
			if(bNames.equals(bName)) {
				return i;
			}
		}
		return -1;
	}
}
