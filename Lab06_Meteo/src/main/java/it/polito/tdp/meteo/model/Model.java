package it.polito.tdp.meteo.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import it.polito.tdp.meteo.DAO.MeteoDAO;

public class Model {
	
	private final static int COST = 100;
	private final static int NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN = 3;
	private final static int NUMERO_GIORNI_CITTA_MAX = 6;
	private final static int NUMERO_GIORNI_TOTALI = 15;
	private List <Citta> citta;
	private MeteoDAO meteodao;
	private int minimo;
	private List <Rilevamento> bestSequenza;

	public Model() {
		meteodao = new MeteoDAO();
		citta = new ArrayList<>(); 
		this.popolaCitta();
	}

	// of course you can change the String output with what you think works best
	public String getUmiditaMedia(int mese) {
		String str="";
		for (Citta c : citta) {
			str+=c.getNome()+": "+c.getMediaUmiditaPerMese(mese)+"\n";
		}
		if (str.equals(""))
			return "Non è stato possibile trovare umidita medie per questo mese";
		else
			return str;
	}
	
	// of course you can change the String output with what you think works best
	public String trovaSequenza(int mese) {
		
		minimo=300*NUMERO_GIORNI_TOTALI;
		this.bestSequenza = new ArrayList<>();
		List <Rilevamento> parziale = new LinkedList<> ();
		cerca(parziale, 0, mese);
		
		String str="";
		for (Rilevamento r : this.bestSequenza) {
			str+=r.getGiorno()+ " " + r.getLocalita()+" "+r.getUmidita()+"\n";
		}
		
		return "Costo minimo: "+minimo+"\n"+str;
	}
	
	
	private void cerca(List <Rilevamento> parziale, int somma, int mese) {
		//caso terminale
		if (parziale.size() == NUMERO_GIORNI_TOTALI && this.tutteLeCittaPresenti(parziale) && controlloGiorniMassimo(parziale)) {
			if (somma < this.minimo) {
				this.minimo = somma; 
				this.bestSequenza = new ArrayList <> (parziale) ;
			}
		}
		
		//creazione sottoproblemi
		for (Rilevamento r : meteodao.getAllRilevamentiPerGiornoMese(parziale.size()+1, mese)) {
			if (parziale.isEmpty() && r.getGiorno()==parziale.size()+1) {
				somma = 0;
				parziale.add(r);
				somma += r.getUmidita();
				cerca(parziale, somma, mese);
				somma = 0;
				parziale.remove(r);
			}
			
			else {
				if (r.getLocalita().equals(parziale.get(parziale.size()-1).getLocalita()) && parziale.size()+1 == r.getGiorno()) {
					if (somma + r.getUmidita() < this.minimo && controlloGiorniMassimo(parziale)) {
						parziale.add(r);
						somma += r.getUmidita();
						cerca(parziale, somma, mese);
						somma = somma -r.getUmidita();
						parziale.remove(r);
				}}
				else {
					if (possoCambiareLocalita(parziale) && parziale.size()+1 == r.getGiorno()) {
						if (somma + r.getUmidita() < this.minimo && controlloGiorniMassimo(parziale)) {
							parziale.add(r);
							somma += COST + r.getUmidita();
							cerca(parziale, somma, mese);
							parziale.remove(r);
							somma = somma - COST - r.getUmidita();
					}}
				}
					
			}
		}

	}
	
	private void popolaCitta() {
		List <String> elencoLocalita = this.meteodao.getLocalita();
		for (String localita : elencoLocalita) {
			List <Rilevamento> rilevamenti = meteodao.getAllRilevamentiPerLocalita(localita);
			this.citta.add(new Citta(localita, rilevamenti));
		}
	}
	
	
	private int giorniLocalita (String localita, List <Rilevamento> rilevamenti ) {
		int cont = 0;
		for (Rilevamento r : rilevamenti ) {
			if (r.getLocalita().equals(localita))
				cont++;
		}
		return cont;
	}
	
	private boolean controlloGiorniMassimo (List <Rilevamento> rilevamenti) {
		boolean temp = true ; 
		for (Citta citta : this.citta) {
			if (giorniLocalita(citta.getNome(), rilevamenti) > NUMERO_GIORNI_CITTA_MAX)
				temp = false;
		}
		return temp;
	}
	
	private boolean tutteLeCittaPresenti (List <Rilevamento> rilevamenti ) {
		List <Citta> cittaPresenti = new ArrayList <> () ; 
		for (Rilevamento r : rilevamenti) {
			if (!cittaPresenti.contains(new Citta (r.getLocalita()) ))
				cittaPresenti.add(new Citta (r.getLocalita()));
		}
		return cittaPresenti.containsAll(this.citta);
	}
	
	private boolean possoCambiareLocalita (List <Rilevamento> rilevamenti) {
		if (rilevamenti.size()<NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN)
			return false;
		
		if (rilevamenti.size()==NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN) {
			if (rilevamenti.get(0).getLocalita().equals(rilevamenti.get(1).getLocalita()) &&
				rilevamenti.get(0).getLocalita().equals(rilevamenti.get(2).getLocalita()))
				return true;
		}
		if (rilevamenti.get(rilevamenti.size()-1).getLocalita().equals(rilevamenti.get(rilevamenti.size()-2).getLocalita()) && 
			rilevamenti.get(rilevamenti.size()-1).getLocalita().equals(rilevamenti.get(rilevamenti.size()-3).getLocalita()) &&
			rilevamenti.get(rilevamenti.size()-1).getLocalita().equals(rilevamenti.get(rilevamenti.size()-4).getLocalita())) {
			return true;
		}
		return false;
	}


	
	

}
