package ServeurJeu.ComposantesJeu.ReglesJeu;

import java.util.Comparator;

/**
 * @author Jean-François Brind'Amour
 */
public class ReglesComparator implements Comparator<ReglesObjet>
{
	/**
	 * Constructeur de la classe ReglesComparator.
	 */
	public ReglesComparator() {}
	
	/**
	 * Cette fonction retourne une valeur permettant de déterminer quel objet 
	 * est plus grand que l'autre.
	 * 
         * @param reglesObjet1 l'objet à gauche de l'(in)égalitée.
         * @param reglesObjet2 l'objet à droite de l'(in)égalitée.
         * @return int : Une valeur négative si le premier objet est plus petit
	 * 				 que le deuxième
	 * 				 Zero si les deux sont égaux
	 * 				 Une valeur positive si le premier objet est plus grand 
	 * 				 que le deuxième
	 */
	public int compare(ReglesObjet reglesObjet1, ReglesObjet reglesObjet2)
	{
		// Si les deux priorités sont pareilles, alors on retourne 0
		if (reglesObjet1.obtenirPriorite() == reglesObjet2.obtenirPriorite())
		{
			return 0;
		}
		// Si le premier objet a une priorité de -1 ou qu'il est plus grand 
		// que le deuxième, alors on retourne 1
		else if (reglesObjet1.obtenirPriorite() == -1 || (reglesObjet1.obtenirPriorite() > reglesObjet2.obtenirPriorite()))
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
