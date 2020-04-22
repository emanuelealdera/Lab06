package it.polito.tdp.meteo.model;

import java.util.ArrayList;
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
		
		minimo=200*NUMERO_GIORNI_TOTALI;
		this.bestSequenza = new ArrayList<>();
		List <Rilevamento> parziale = new ArrayList<> ();
		List <Rilevamento> tutte = meteodao.getAllRilevamentiPerMese(mese);
		cerca(tutte, parziale, 1, 0);
		
		String str="";
		for (Rilevamento r : this.bestSequenza) {
			str+= r.getLocalita()+" "+r.getUmidita()+"\n";
		}
		
		return "Costo minimo: "+minimo+"\n"+str;
	}
	
	
	private void cerca(List <Rilevamento> tutte, List <Rilevamento> parziale, int livello, int somma) {
		//caso terminale
		if (livello == NUMERO_GIORNI_TOTALI +1 && this.tutteLeCittaPresenti(parziale) && controlloGiorniMassimo(parziale)) {
			if (somma < this.minimo) {
				this.minimo = somma; 
				this.bestSequenza = new ArrayList <> (parziale) ;
			}
		}
		
		//creazione sottoproblemi
		for (Rilevamento r : tutte) {
			//considero solo i rilevamenti dove il livello corrisponde al giorno giusto
			if (livello == r.getGiorno()) {
				// caso in cui non possa cambiare localita perche non ci sono rimasto abbastanza
				if (possoCambiareLocalita(parziale)==false) {
					if (parziale.isEmpty() || r.getLocalita().equals(parziale.get(parziale.size()-1).getLocalita())) {
						parziale.add(r);
						somma += r.getUmidita();
						if (somma < this.minimo || controlloGiorniMassimo(parziale)) {
							cerca (tutte, parziale, livello +1, somma);
							//backtracking
							somma = somma - r.getUmidita();
							parziale.remove(parziale.size()-1);
						}
					}
				}
				
				//caso in cui posso cambiare localita
				//qui provo prima a cambiarla e poi, dopo il backtracking, a non cambiarla
				
				else {
					//cambio localita
					if (!r.getLocalita().equals(parziale.get(parziale.size()-1).getLocalita())) {
						parziale.add(r);
						somma += COST + r.getUmidita();
						if (somma < this.minimo || controlloGiorniMassimo(parziale)) {
							cerca (tutte, parziale, livello +1, somma );
							//backtracking
							somma = somma - COST - r.getUmidita();
							parziale.remove(parziale.get(parziale.size()-1));
						}
					}
					//non cambio localita
					else {
						parziale.add(r);
						somma += r.getUmidita();
						if (somma < this.minimo || controlloGiorniMassimo(parziale)) {
							cerca (tutte, parziale, livello +1, somma );
							//backtracking
							somma = somma - r.getUmidita();
							parziale.remove(parziale.get(parziale.size()-1));
						}
					}

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
	
	private boolean controlloGiorniMassimo (List <Rilevamento> rilevamenti ) {
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
