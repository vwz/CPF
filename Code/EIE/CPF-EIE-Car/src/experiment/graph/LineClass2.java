package experiment.graph;


public class LineClass2 extends LineClassConsistency {
	public boolean hasPrev;
	public boolean hasNext;
	public int Prev;
	public int Next;

	public LineClass2() {
		hasPrev = false;
		hasNext = false;
		Prev = -1;
		Next = -1;
	}
}
