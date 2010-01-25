package ServeurJeu.ComposantesJeu.ReglesJeu;

import java.util.TreeSet;

/**
 * @author Jean-François Brind'Amour
 */
public class ReglesMagasin extends ReglesObjet
{
	// Déclaration d'une variable qui va contenir le nom du magasin
	private String strNomMagasin;
  
  private TreeSet<ReglesObjetUtilisable> mObjetUtilisable;
	
	/**
	 * Constructeur de la classe ReglesMagasin qui permet d'initialiser
	 * la priorité et le nom du magasin.
	 * 
	 * @param String nom : Le nom du magasin 
	 */
	public ReglesMagasin(String nom)
	{
		super();
		
		// Initialiser les propriétés de la règle du magasin
		strNomMagasin = nom;
	}
	
	/**
	 * Constructeur de la classe ReglesMagasin qui permet d'initialiser
	 * la priorité et le nom du magasin.
	 * 
	 * @param int priorite : La priorité de cette règle
	 * @param String nom : Le nom du magasin
	 */
	public ReglesMagasin(int priorite, String nom)
	{
		super(priorite);
    
    ReglesComparator objReglesComparator = new ReglesComparator();
    mObjetUtilisable = new TreeSet<ReglesObjetUtilisable>(objReglesComparator);
    
		
		// Initialiser les propriétés de la règle du magasin
		strNomMagasin = nom;
	}
	
	/**
	 * Cette fonction permet de retourner le nom du magasin.
	 * 
	 * @return String : Le nom du magasin
	 */
	public String obtenirNomMagasin()
	{
	   return strNomMagasin;
	}
  
  public void ajouterReglesObjetUtilisable(ReglesObjetUtilisable pRegleObjet) {
    mObjetUtilisable.add(pRegleObjet);
  }
  
  public TreeSet<ReglesObjetUtilisable> getReglesObjetUtilisable () {
    return mObjetUtilisable;
  }
}