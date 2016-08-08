package experiment.inference;

import java.util.ArrayList;
import java.util.List;

public class InferClass {
	public double[][] nodeBel;
	public double[][][] edgeBel;
	public double logZ;
	private int maxIter;
	
	public InferClass (int nNodes, int nStates, int nEdges, int maxIter) {
		nodeBel = new double[nNodes][nStates];
		for (int i = 0; i < nNodes; i++) {
			for (int j = 0; j < nStates; j++) {
				nodeBel[i][j] = 0;
			}
		}
		edgeBel = new double[nStates][nStates][nEdges];
		for (int i = 0; i < nStates; i++) {
			for (int j = 0; j < nStates; j++) {
				for (int k = 0; k < nEdges; k++) {
					edgeBel[i][j][k] = 0;
				}
			}
		}
		this.maxIter = maxIter;
		logZ = 0;
	}
	
	public List<String> DecodeLoopyBP(double[][] nodePot, double[][][] edgePot, EdgeStructClass edgeStruct) {
		int nEdges = edgeStruct.nEdges;
		int nNodes = edgeStruct.nNodes;
		List<int[]> edgeEnds = edgeStruct.edgeEnds;
		int[] V = edgeStruct.V;
		int[] E = edgeStruct.E;
		int nStates = edgeStruct.nStates;
		
		boolean maximize = true;
		double[][] new_msg = LoopyBP(nodePot, edgePot, edgeStruct, maximize);  // new_msg = double[nStates][nEdges*2]
		
		// Compute nodeBel
		for (int i = 0; i < nNodes; i++) {
			int[] edges = GetEdges(E, V[i], V[i+1]-1);
			double[] prod_of_msgs = GetNodePot(nodePot, i, nStates);
			for (int j = 0; j < edges.length; j++) {
				int e = edges[j];
				int n1 = edgeEnds.get(e)[0];
				int n2 = edgeEnds.get(e)[1];

				if (i == n2) {
					for (int k = 0; k < prod_of_msgs.length; k++)
						prod_of_msgs[k] *= new_msg[k][e];
				}
				else {
					for (int k = 0; k < prod_of_msgs.length; k++)
						prod_of_msgs[k] *= new_msg[k][e+nEdges];
				}
			}

			// normalize node belief
			double sumVal = sum(prod_of_msgs);
			if (sumVal != 0) {
				for (int k = 0; k < nStates; k++)
					nodeBel[i][k] = prod_of_msgs[k] / sumVal;
			}
			else {
				for (int k = 0; k < nStates; k++)
					nodeBel[i][k] = 1.0 / (double) nStates;
			}
		}
		
		// Output labels
		List<String> output = new ArrayList<String>();
		for (int i = 0; i < nNodes; i++) {
			double maxPot = Double.MIN_VALUE;
			int maxStateID = -1;
			for (int j = 0; j < nStates; j++) {
				double pot = nodeBel[i][j];
				if (pot >= maxPot) {
					maxPot = pot;
					maxStateID = j;
				}
			}
			String val = String.valueOf(maxStateID) + "\t" + String.valueOf(maxPot);
			output.add(val);
		}
		return output;
	}
	
	/**
	 * Step 4.b. Inference by loopy BP, and output the nodeBel, edgeBel and logZ
	 * @param nodePot: node potential computed on each state
	 * @param edgePot: edge potential computed on each pair of states
	 * @param edgeStruct: edge structure encoding the graph
	 */
	public void InferLoopyBP(double[][] nodePot, double[][][] edgePot, EdgeStructClass edgeStruct) {
		int nEdges = edgeStruct.nEdges;
		int nNodes = edgeStruct.nNodes;
		List<int[]> edgeEnds = edgeStruct.edgeEnds;
		int[] V = edgeStruct.V;
		int[] E = edgeStruct.E;
		int nStates = edgeStruct.nStates;
		
		boolean maximize = false;
		double[][] new_msg = LoopyBP(nodePot, edgePot, edgeStruct, maximize); // new_msg = double[nStates][nEdges*2]
		
		// Compute nodeBel
		for (int i = 0; i < nNodes; i++) {
			int[] edges = GetEdges(E, V[i], V[i+1]-1);
			double[] prod_of_msgs = GetNodePot(nodePot, i, nStates);
			for (int j = 0; j < edges.length; j++) {
				int e = edges[j];
				int n1 = edgeEnds.get(e)[0];
				int n2 = edgeEnds.get(e)[1];
				
				if (i == n2) {
					for (int k = 0; k < prod_of_msgs.length; k++)
						prod_of_msgs[k] *= new_msg[k][e];
				}
				else {
					for (int k = 0; k < prod_of_msgs.length; k++)
						prod_of_msgs[k] *= new_msg[k][e+nEdges];
				}
			}
			
			// normalize node belief
			double sumVal = sum(prod_of_msgs);
			if (sumVal != 0) {
				for (int k = 0; k < nStates; k++)
					nodeBel[i][k] = prod_of_msgs[k] / sumVal;
			}
			else {
				for (int k = 0; k < nStates; k++)
					nodeBel[i][k] = 1.0 / (double) nStates;
			}
		}
		
		// Compute edgeBel
		for (int e = 0; e < nEdges; e++) {
			int n1 = edgeEnds.get(e)[0];
			int n2 = edgeEnds.get(e)[1];
			double[] belN1 = new double[nStates];
			double[] belN2 = new double[nStates];
			for (int j = 0; j < nStates; j++) {
				if (new_msg[j][e+nEdges] != 0) {
					belN1[j] = nodeBel[n1][j] / new_msg[j][e+nEdges];
				}
				if (new_msg[j][e] != 0) {
					belN2[j] = nodeBel[n2][j] / new_msg[j][e];
				}
			}
			
			double[][] eb = new double[nStates][nStates];
			double sumVal = 0;
			for (int j = 0; j < nStates; j++) {
				for (int k = 0; k < nStates; k++) {
					double[][] pot_ij = GetEdgePot(edgePot, e, nStates);
					eb[j][k] = belN1[j] * belN2[k] * pot_ij[j][k];
					sumVal += eb[j][k];
				}
			}
			
			if (sumVal != 0) {
				for (int j = 0; j < nStates; j++) {
					for (int k = 0; k < nStates; k++) {
						edgeBel[j][k][e] = eb[j][k] / sumVal;
					}
				}
			}
			else {
				for (int j = 0; j < nStates; j++) {
					for (int k = 0; k < nStates; k++) {
						edgeBel[j][k][e] = 1.0 / (double) nStates;
					}
				}
			}
		}
		
		// Compute logZ: refer to (Yedidia et al. "Bethe free energy, Kikuchi approximations, and belief propagation algorithms", Eqs.(24-25))
		double energy1 = 0, energy2 = 0, entropy1 = 0, entropy2 = 0;
		for (int i = 0; i < nNodes; i++) {
			int[] edges = GetEdges(E, V[i], V[i+1]-1);
			int nNbrs = edges.length;
			
			// node entropy
			double sumVal = 0;
			for (int j = 0; j < nStates; j++) {
				sumVal += nodeBel[i][j] * Math.log(nodeBel[i][j]);
			}
			entropy1 += (nNbrs-1) * sumVal;
			
			// node energy
			sumVal = 0;
			for (int j = 0; j < nStates; j++) {
				sumVal += nodeBel[i][j] * Math.log(nodePot[i][j]);
			}
			energy1 -= sumVal;
		}
		
		for (int e = 0; e < nEdges; e++) {
			int n1 = edgeEnds.get(e)[0];
			int n2 = edgeEnds.get(e)[1];
			
			// pairwise entropy
			double sumVal = 0;
			for (int j = 0; j < nStates; j++) {
				for (int k = 0; k < nStates; k++) {
					sumVal += edgeBel[j][k][e] * Math.log(edgeBel[j][k][e]);
				}
			}
			entropy2 -= sumVal;
			
			// pairwise energy
			sumVal = 0;
			for (int j = 0; j < nStates; j++) {
				for (int k = 0; k < nStates; k++) {
					sumVal += edgeBel[j][k][e] * Math.log(edgePot[j][k][e]);
				}
			}
			energy2 -= sumVal;
		}
		
		double F = (energy1 + energy2) - (entropy1 + entropy2);
		logZ = -F;
	}
	
	public double[][] InitPara(int m, int n, double val) {
		double[][] a = new double[m][n];
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				a[i][j] = val;
			}
		}
		return a;
	}
	
	/**
	 * Step 4.c. The loopy BP implementation based on message passing [sum-product and max-product]
	 * @param nodePot: node potential computed on each state 
	 * @param edgePot: edge potential computed on each pair of states
	 * @param edgeStruct: edge structure encoding the graph
	 * @param maximize: false = sum-product, true = max-product
	 * @return
	 */
	public double[][] LoopyBP(double[][] nodePot, double[][][] edgePot, EdgeStructClass edgeStruct, boolean maximize) {
		int nNodes = edgeStruct.nNodes;
		int nEdges = edgeStruct.nEdges;
		int nStates = edgeStruct.nStates;
		int[] V = edgeStruct.V;
		int[] E = edgeStruct.E;
		List<int[]> edgeEnds = edgeStruct.edgeEnds;
		
		// Initialization
		double val = 1.0 / (double) nStates; // can be improved
		double[][] new_msg = InitPara(nStates, nEdges*2, val); // new_msg[][e]: e.n1->e.n2, new_msg[][e+nEdges]: e.n2->e.n1
		double[][] old_msg = InitPara(nStates, nEdges*2, 0);
		
		int iter = 0;
		double diff = 0;
		for (iter = 0; iter < maxIter; iter++) {
			for (int i = 0; i < nNodes; i++) {
				// Find neighbors
				int[] edges = GetEdges(E, V[i], V[i+1]-1);
				
				// Send a message to each neighbor
				for (int j = 0; j < edges.length; j++) {
					int e = edges[j];
					int n1 = edgeEnds.get(e)[0];
					int n2 = edgeEnds.get(e)[1];
					
					double[][] pot_ij;
					if (i == n2)
						pot_ij = GetEdgePot(edgePot, e, nStates);
					else
						pot_ij = transpose(GetEdgePot(edgePot, e, nStates), nStates, nStates);
					
					// compute temp = product of all incoming msgs except j
					double[] temp = GetNodePot(nodePot, i, nStates);
					for (int k = 0; k < edges.length; k++) {
						int e2 = edges[k];
						if (e2 != e) {
							if (i == edgeEnds.get(e2)[1]) {
								for (int l = 0; l < temp.length; l++)
									temp[l] *= new_msg[l][e2];
							}
							else {
								for (int l = 0; l < temp.length; l++)
									temp[l] *= new_msg[l][e2+nEdges];
							}
						}
					}
					
					// compute new message
					double[] newm;
					if (maximize)
						newm = max_mult(pot_ij, temp, nStates, nStates);
					else
						newm = sum_mult(pot_ij, temp, nStates, nStates);
					
					double sumVal = sum(newm);
					if (sumVal != 0) {
						if (i == n2) {
							for (int k = 0; k < newm.length; k++)
								new_msg[k][e+nEdges] = newm[k] / sumVal; 
						}
						else {
							for (int k = 0; k < newm.length; k++)
								new_msg[k][e] = newm[k] / sumVal;  
						}
					}
				}
			}
			
			// check convergence
			diff = 0;
			for (int i = 0; i < nStates; i++) {
				for (int j = 0; j < nEdges*2; j++) {
					diff += Math.abs(new_msg[i][j] - old_msg[i][j]);
				}
			}
			if (diff < 1) // 0.1
				break;
			
			for (int i = 0; i < nStates; i++) {
				for (int j = 0; j < nEdges*2; j++) {
					old_msg[i][j] = new_msg[i][j];
				}
			}
		}
		
		if (iter == maxIter) {
			System.err.println("BP did not converge: diff = " + diff);
		}
		
		return new_msg;
	}
	
	/**
	 * Return the sum of a vector
	 * @param newm
	 * @return
	 */
	private double sum(double[] newm) {
		double val = 0;
		for (int i = 0; i < newm.length; i++)
			val += newm[i];
		return val;
	}
	
	/**
	 * Similar to multiplication of a matrix and a vector, except that the "sum" operation is replaced as "max" operation
	 * @param pot_ij: input matrix
	 * @param temp: input vector
	 * @param m: row number of input matrix
	 * @param n: column number of input matrix
	 * @return
	 */
	private double[] max_mult(double[][] pot_ij, double[] temp, int m, int n) {
		double[] res = new double[m];
		for (int i = 0; i < m; i++) {
			double max = -1000000;
			for (int j = 0; j < n; j++) {
				double tmp = pot_ij[i][j] * temp[j];
				if (tmp > max) 
					max = tmp;
			}
			res[i] = max;
		}
		return res;
	}
	
	/**
	 * Multiplication of a matrix and a vector
	 * @param pot_ij: input matrix
	 * @param temp: input vector
	 * @param m: row number of input matrix
	 * @param n: column number of input matrix
	 * @return
	 */
	private double[] sum_mult(double[][] pot_ij, double[] temp, int m, int n) {
		double[] res = new double[m];
		for (int i = 0; i < m; i++) {
			double tmp = 0;
			for (int j = 0; j < n; j++) {
				tmp += pot_ij[i][j] * temp[j];
			}
			res[i] = tmp;
		}
		return res;
	}
	
	/**
	 * Matrix transpose
	 * @param pot_ij: input matrix
	 * @param m: row number of input matrix
	 * @param n: column number of input matrix
	 * @return
	 */
	private double[][] transpose(double[][] pot_ij, int m, int n) {
		double[][] pot_ij_trans = new double[n][m];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				pot_ij_trans[i][j] = pot_ij[j][i];
			}
		}
		return pot_ij_trans;
	}
	
	/**
	 * Return a sub-array of the input array 
	 * @param E: input array
	 * @param s1: sub-array start index
	 * @param s2: sub-array end index
	 * @return
	 */
	private int[] GetEdges(int[] E, int s1, int s2) {
		int[] edges = new int[s2 - s1 + 1];
		for (int i = 0; i < s2-s1+1; i++)
			edges[i] = E[s1 + i];
		return edges;
	}
	
	/**
	 * Return the potential for an edge
	 * @param edgePot: all the edge potentials
	 * @param e: the edge ID
	 * @param nStates: number of states
	 * @return
	 */
	private double[][] GetEdgePot(double[][][] edgePot, int e, int nStates) {
		double[][] pot = new double[nStates][nStates]; 
		for (int i = 0; i < nStates; i++) {
			for (int j = 0; j < nStates; j++) {
				pot[i][j] = edgePot[i][j][e];
			}
		}
		return pot;
	}
	
	/**
	 * Return the potential of a node
	 * @param nodePot: node potentials
	 * @param n: node ID
	 * @param nStates: number of states
	 * @return
	 */
	private double[] GetNodePot(double[][] nodePot, int n, int nStates) {
		double [] pot = new double[nStates];
		for (int i = 0; i < nStates; i++) {
			pot[i] = nodePot[n][i];
		}
		return pot;
	}
}