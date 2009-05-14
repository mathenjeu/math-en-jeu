package ServeurJeu.ComposantesJeu.ReglesJeu;

import java.util.Comparator;

/**
 * @author Jean-François Brind'Amour
 */
public class ReglesComparator implements Comparator<Object>
{
	/**
	 * Constructeur de la classe ReglesComparator.
	 */
	public ReglesComparator() {}
	
	/**
	 * Cette fonction retourne une valeur permettant de déterminer quel objet 
	 * est plus grand que l'autre.
	 * 
	 * @return int : Une valeur négative si le premier objet est plus petit 
	 * 				 que le deuxième
	 * 				 Zero si les deux sont égaux
	 * 				 Une valeur positive si le premier objet est plus grand 
	 * 				 que le deuxième
	 */
	public int compare(Object reglesObjet1, Object reglesObjet2)
	{
		// Faire la référence vers l'objet reglesObjet1 et reglesObjet2
		ReglesObjet objReglesObjet1 = (ReglesObjet) reglesObjet1;
		ReglesObjet objReglesObjet2 = (ReglesObjet) reglesObjet2;
		
		// Si les deux priorités sont pareilles, alors on retourne 0
		if (objReglesObjet1.obtenirPriorite() == objReglesObjet2.obtenirPriorite())
		{
			return 0;
		}
		// Si le premier objet a une priorité de -1 ou qu'il est plus grand 
		// que le deuxième, alors on retourne 1
		else if (objReglesObjet1.obtenirPriorite() == -1 || (objReglesObjet1.obtenirPriorite() > objReglesObjet2.obtenirPriorite()))
		{
			return 1;
		}
		// Si le deuxième objet a une priorité de -1 ou qu'il est plus grand 
		// que le premier, alors on retourne -1
		else
		{
			return -1;
		}
	}
}
