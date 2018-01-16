package parser.ProtocolValidationTest;

import java.util.List;


import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import protocolosv2.Operation;
import protocolosv2.Sequence;

public class IntVarOperations {
	
	//return a sequence representation as a BoolVar.
	//op is the operation of each sequence with the same output step.
	//intVars is a list of the numeric operands that make up all operation in the protocol.
	//index is a vector with the index of the operands in the list intVars that are used in the operation op. 
	public BoolVar createBoolVarSequence(Operation op, List<IntVar> intVars, int[] index) {
		Model auxModel = new Model("Axiliary IntVar Model");
		
		switch(op.getOperator()) {
			case EQUAL:
				return auxModel.arithm(intVars.get(index[0]),"==",intVars.get(index[1])).reify();
			case EQUAL_OR_GREATER:
				return auxModel.arithm(intVars.get(index[0]),">=",intVars.get(index[1])).reify();
			case EQUAL_OR_SMALLER:
				return auxModel.arithm(intVars.get(index[0]),"<=",intVars.get(index[1])).reify();
			case BIGGER_THAN:
				return auxModel.arithm(intVars.get(index[0]),">",intVars.get(index[1])).reify();
			case SMALLER_THAN:
				return auxModel.arithm(intVars.get(index[0]),"<",intVars.get(index[1])).reify();
			default:
				return null;
		}
	}
	
	public IntVar calculate(Operation op, List<IntVar> intVars, int[] index) {
		Model auxModel = new Model("Axiliary IntVar Model");
		IntVar result = null;
		
		switch(op.getOperator()) {
			case SUM:
				return auxModel.arithm(intVars.get(index[0]),"+",intVars.get(index[1])).reify();
			case MINUS:							
				return auxModel.arithm(intVars.get(index[0]),"-",intVars.get(index[1])).reify();
			case MULTIPLICATION:			
				auxModel.times(intVars.get(index[0]), intVars.get(index[1]), result).reify();
				return result;
			case DIVISION:			
				auxModel.div(intVars.get(index[0]), intVars.get(index[1]), result).reify();
				return result;
			default:
				return null;
		}
	}
		
	//get the operands from the operation and put it in the list intVars if the list do not contain it.
	//return the index of the operands of the operation in the intVars.
	public int[] operandsIntoIntVarList(List<IntVar> intVars, Model model, Operation operation) {		
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
		return indexes;
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
