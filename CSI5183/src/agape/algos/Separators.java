package agape.algos;

import agape.tools.Components;
import agape.tools.Operations;
import edu.uci.ics.jung.graph.Graph;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * This class provides methods for ab-separators problem.
 * @author V. Levorato
 * @author J.-F. Lalande
 * @author P. Berthome
 * @param <V> Vertices type
 * @param <E> Edges type
 */
public class Separators<V,E> extends Algorithms<V,E> {

	public Separators() { };

	/**
	 * Returns a minimal ab-separator between vertices a and b.
	 * @param g graph
	 * @param a vertex a
	 * @param b vertex b
	 * @return a set of minimal ab-separator.
	 */
	public Set<Set<V>> getABSeparators(Graph<V,E> g, V a, V b)
	{

		Set<Set<V>> SS=new HashSet<Set<V>>();
		
		int minimal = 999;

		if(!g.getNeighbors(a).contains(b))
		{
			Set<V> Na=new HashSet<V>();
			Na.addAll(g.getNeighbors(a));
			Na.add(a);

			ArrayList<Set<V>> CC=Components.getAllConnectedComponent(g,Na);

			for(Set<V> C : CC)
			{
				if(C.contains(b))
				{
					Set<V> Nc=Operations.getNeighbors(g, C);
					if(!Nc.isEmpty()) {
						if (Nc.size() <= minimal){							
							minimal = Nc.size();
						}
						SS.add(Nc);
					}
				}
			}

			
			
			Set<Set<V>> T=new HashSet<Set<V>>();
			Set<Set<V>> SdT=new HashSet<Set<V>>(SS);
			HashSet<V> temp = new HashSet<V>(g.getNeighbors(b));
			temp.remove(b);
			SS.add(temp);
			
			while(!SdT.isEmpty())
			{
				Set<V> S=SdT.iterator().next();

				for(V x : S)
				{
					Set<V> SNx=new HashSet<V>(S);
					SNx.addAll(g.getNeighbors(x));

					CC=Components.getAllConnectedComponent(g,SNx);


					for(Set<V> C : CC)
					{

						Set<V> Nc=Operations.getNeighbors(g, C);
						if(!Nc.isEmpty() && C.contains(b) && Nc.size() <= minimal)
//						if(!Nc.isEmpty() && C.contains(b))
							SS.add(Nc);
					}
				}

				T.add(S);

				SdT=new HashSet<Set<V>>(SS);
				SdT.removeAll(T);

			}
			if(minimal < 6){
				SS.remove(temp);
			}
		}
		
		return SS;
	}

}