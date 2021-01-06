import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.StringTokenizer;


/**
 * this class represent a Node in the Bayesian Network
 * @author Gofna Ivry
 *
 */

public class Node implements Comparable<Node>{

	public String name;
	public ArrayList<String> childs, parents, possibleVals;
	public ArrayList<String[]> CPT;
	public HashMap<String, Integer> indexInCPT;// key for node-name and Integer for index in CPT
	public int indexInBN; //the index by the order from the file

	public Node() {

	}

	/**
	 * Constructor
	 * @param name the name of the node
	 */
	public Node(String name) {
		this.name = name;
		this.childs = new ArrayList<String>();
		this.parents = new ArrayList<String>();
		this.possibleVals = new ArrayList<String>();
		this.CPT = new ArrayList<String[]>();
		this.indexInCPT = new HashMap<String, Integer>();
	}

	public Node(Node n) {
		this.name = n.name;
		this.childs = n.childs;
		this.parents = n.parents;
		this.possibleVals = n.possibleVals;
		this.CPT = new ArrayList<String[]>(n.CPT);
		this.indexInCPT = new HashMap<String, Integer>(n.indexInCPT);
	}

	public void addParents(String parents) {
		String[] p = parents.split(",");
		for (int i = 0; i < p.length; i++) {
			String parent = p[i];
			if(!parent.contains("none")) {
				this.parents.add(parent);
				Ex1.BN.get(parent).addChild(this.getName());

			}
		}
	}

	public void addChild(String child) {
		childs.add(child); 
	}

	public void addValues(StringTokenizer values) {
		while(values.hasMoreTokens()) {
			this.possibleVals.add(values.nextToken());
		}
	}

	public void buildCPT(String cptValues) {
		if (cptValues.length() == 0)return;

		ArrayList<String> givenValues = new ArrayList<String>(possibleVals);
		double lastP =1;

		StringTokenizer data =  new StringTokenizer(cptValues, ",");
		String tempEvidences[] =  new String[this.parents.size()];
		int col=0, row=this.CPT.size()-1;

		for(int i=0; data.hasMoreTokens(); i++) {

			String value = data.nextToken();
			if (i < parents.size()){
				tempEvidences[i] = value ;
			}
			else if (value.contains("=")){
				CPT.add(new String[parents.size() + 2]);

				row++;
				for (String evidence : tempEvidences) {
					CPT.get(row)[col++] = evidence;
				}

				CPT.get(row)[col++] = value.substring(1);
				givenValues.remove(value.substring(1)); // to stay with the last value that is not given
			}
			else{
//				if(Double.parseDouble(value) == 0) return; //CHECK!!!!!!!!!!!!!!!!!!!!!!!!!
				CPT.get(row)[col] = value;
				lastP = lastP - Double.parseDouble(value);
				col=0;

			}
		}
		CPT.add(new String[parents.size() + 2]);
		row++;	
		for (String evidence : tempEvidences) {
			CPT.get(row)[col++] = evidence;
		}

		CPT.get(row)[col++] = givenValues.get(0);
		CPT.get(row)[col] = "" + (lastP);
	}

	/**
	 *
	 * @param n the Node name
	 * @return the column index of specific Node in CPT
	 */
	public int getPos(String n){
		Integer result = indexInCPT.get(n);
		if (result != null)
			return this.indexInCPT.get(n);
		if (n.equals(this.name))
			result = this.parents.size();
		else
			result = this.parents.indexOf(n);
		
		this.indexInCPT.put(n, result);
		return result;
	}

	public void fillRow(String value, int col){
		CPT.get(CPT.size() - 1)[col] = value;
	}

	public void addEmptyRow(){
		CPT.add(new String[parents.size() + 2]);
	}

	public String getName() { return name; }

	public ArrayList<String> getParents() { return this.parents; }

	public ArrayList<String> getChilds() { return this.childs; }

	public ArrayList<String[]> getCPT() { return this.CPT; }

	public int getNumberOfValues()	{ return this.possibleVals.size(); }
	
	@Override
	public String toString() 
	{
		String result = "Var [mName=" + name + ", mParents=" + parents + ", mChilds=" + childs + ", mValues=" + possibleVals +
				", mCPT= " + CPT.size() + "]\nCPT:";

		for (String s[] : CPT)
			result += '\n' + Arrays.toString(s);

		return result;
	}


	@Override
	public int compareTo(Node n) {
		if((this.childs.size()+this.parents.size()) > (n.childs.size()+n.parents.size())) {
			return 1;
		}
		else if((this.childs.size()+this.parents.size()) < (n.childs.size()+n.parents.size())){
			return -1;
		}
		else {
			return 0;
		}
	}


}




