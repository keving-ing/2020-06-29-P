package it.polito.tdp.PremierLeague.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.mariadb.jdbc.internal.com.send.authentication.ed25519.math.ed25519.Ed25519LittleEndianEncoding;

import it.polito.tdp.PremierLeague.db.PremierLeagueDAO;

public class Model {
	
	PremierLeagueDAO dao;
	Graph<Match, DefaultWeightedEdge> grafo;
	List<Match> vertici;
	Map<Integer, Match> idMapVERTICI;
	List<Adiacenza> archi;
	List<Match> migliore;
	int pesoMigliore;
	
	public Model()
	{
		dao = new PremierLeagueDAO();
		idMapVERTICI = new HashMap<Integer, Match>();
	}
	
	public String creaGrafo(int m, int min)
	{
		grafo = new SimpleWeightedGraph<Match, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		vertici = dao.listMatchesVERTICI(m, idMapVERTICI);
		Graphs.addAllVertices(grafo, vertici);
		
		archi = dao.listArchi(m, min);
		for(Adiacenza a:archi)
		{
			Graphs.addEdgeWithVertices(grafo, idMapVERTICI.get(a.getM1()), idMapVERTICI.get(a.getM2()), a.getPeso());
		}
		
		return "Grafo creato\n#VERTICI: " + grafo.vertexSet().size() + "\n# ARCHI: " + grafo.edgeSet().size();
	}
	
	public List<Adiacenza> getConnessioneMax()
	{
		int max = 0;
		List<Adiacenza> result = new LinkedList<Adiacenza>();
		for(DefaultWeightedEdge e:grafo.edgeSet())
		{
			if(grafo.getEdgeWeight(e)>max)
			{
				max = (int) grafo.getEdgeWeight(e);
			}
		}
		
		for(DefaultWeightedEdge e:grafo.edgeSet())
		{
			if(grafo.getEdgeWeight(e)==max)
			{
				result.add(new Adiacenza(grafo.getEdgeSource(e).getMatchID(), grafo.getEdgeTarget(e).getMatchID(), (int) grafo.getEdgeWeight(e)));
			}
		}
		
		return result;
		
	}
	
	public List<Match> calcolaPercorso(Match sorg, Match dest)
	{
		migliore = new LinkedList<Match>();
		List<Match> parziale = new LinkedList<>();
		parziale.add(sorg);
		cercaRicorsiva(parziale, dest);
		this.pesoMigliore = this.pesoTot(migliore);
		return migliore;
	}

	private void cercaRicorsiva(List<Match> parziale, Match dest) {
		 
				//condizione di terminazione
				if(parziale.get(parziale.size()-1).equals(dest))
				{
					int pesoParziale = pesoTot(parziale);
					if(pesoParziale > pesoTot(migliore))//la strada piú lunga é la migliore
					{
						migliore = new LinkedList<>(parziale);
					}
					return;
				}
				
				for(Match v:Graphs.neighborListOf(this.grafo, parziale.get(parziale.size()-1))) //scorro sui vicini dell'ultimo nodo sulla lista
				{
					if(!parziale.contains(v))
					{
						if((v.teamHomeID != parziale.get(parziale.size()-1).getTeamHomeID() && v.teamAwayID != parziale.get(parziale.size()-1).teamAwayID)
							||  (v.teamHomeID != parziale.get(parziale.size()-1).getTeamAwayID() && v.teamAwayID != parziale.get(parziale.size()-1).teamHomeID))
						{
							parziale.add(v);
							cercaRicorsiva(parziale, dest);
							parziale.remove(parziale.size()-1);
						}
						
					}
					
				}
		
	}

	private int pesoTot(List<Match> parziale) {
		
		int peso = 0;
		for(int i = 0; i<parziale.size()-1; i++)
		{
			DefaultWeightedEdge e = grafo.getEdge(parziale.get(i), parziale.get(i+1));
			peso += grafo.getEdgeWeight(e);
		}
		System.out.println(peso);
		return peso;
	}

	public int getPesoMigliore() {
		return pesoMigliore;
	}

	public List<Match> getVertici() {
		return vertici;
	}
	
	
	
}
