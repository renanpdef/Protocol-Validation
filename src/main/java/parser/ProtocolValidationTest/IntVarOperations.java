package parser.ProtocolValidationTest;

import java.util.List;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import protocolosv2.Operation;

public class IntVarOperations {
	private int[] indexes = null;	
	
	public int[] getIndexes() {
		return indexes;
	}
	
	//Add a boolVar variables on a list of sequences.
	//sequences is a list of boolvar variables where the operation of each sequence with the same output step will be stored.
	//op is the operation of each sequence with the same output step.
	//intVars is a list of the integer operands that make up all operation in the protocol.
	//index is a vector with the index of the operands in the intVars that are used in the operation op.
	public void addSequences(List<BoolVar> sequences, Operation op, List<IntVar> intVars, int[] index) {
		Model auxModel = new Model("Axiliary IntVar Model");
		BoolVar boolVar = null;
		
		switch(op.getOperator()) {
			case EQUAL:
				boolVar = auxModel.arithm(intVars.get(index[0]),"==",intVars.get(index[1])).reify();
				sequences.add(boolVar);
				break;
			case EQUAL_OR_GREATER:
				boolVar = auxModel.arithm(intVars.get(index[0]),">=",intVars.get(index[1])).reify();
				sequences.add(boolVar);
				break;
			case EQUAL_OR_SMALLER:
				boolVar = auxModel.arithm(intVars.get(index[0]),"<=",intVars.get(index[1])).reify();
				sequences.add(boolVar);
				break;
			case BIGGER_THAN:
				boolVar = auxModel.arithm(intVars.get(index[0]),">",intVars.get(index[1])).reify();
				sequences.add(boolVar);
				break;
			case SMALLER_THAN:
				boolVar = auxModel.arithm(intVars.get(index[0]),"<",intVars.get(index[1])).reify();
				sequences.add(boolVar);
				break;
			default:
				break;
		}
	}
		
	//get the operands from the operation and put it in the list intVars if the list do not contain it.
	//return the index of the operands of the operation in the intVars.
	public void createIntVars(List<IntVar> intVars, Model model, Operation operation) {		
		int indexes[] = new int[operation.getOperand().size()];
		for(int i = 0; i < operation.getOperand().size(); i++) {
			Model auxModel = new Model("Auxiliary Model");
			IntVar intVar = auxModel.intVar(operation.getOperand().get(i).getName(), 0, Integer.MAX_VALUE);
			if(!contains(intVars, intVar)) {
				intVars.add(model.intVar(operation.getOperand().get(i).getName(), 0, Integer.MAX_VALUE));
				indexes[i] = intVars.size() -1;
			}else {
				indexes[i] = indexOf(intVars, intVar);
			}
		}
		this.indexes = indexes;
	}
	
	//Verify whether list intVars already has the intVar
	public boolean contains(List<IntVar> intVars, IntVar intVar) {
		String name = intVar.getName();
		for(int i = 0; i < intVars.size(); i++) {
			String bName = intVars.get(i).getName();
			if(bName.equals(name)) {
				return true;
			}
		}
		return false;
	}
	//retun the index of the intVar from the list intVars.
	public int indexOf(List<IntVar> intVars, IntVar intVar) {
		String name = intVar.getName();
		for(int i = 0; i < intVars.size(); i++) {
			String bName = intVars.get(i).getName();
			if(bName.equals(name)) {
				return i;
			}
		}
		return -1;
	}
}
