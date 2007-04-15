package ServeurJeu.ComposantesJeu.Objets.Magasins;

import java.util.Vector;
import ServeurJeu.ComposantesJeu.Objets.Objet;
import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.ObjetUtilisable;
import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.Reponse;
import ClassesUtilitaires.IntObj;


/**
 * @author Jean-François Brind'Amour
 */
public abstract class Magasin extends Objet
{
	// Déclaration d'une liste d'objets utilisables qui va servir à savoir 
	// quels objets le magasin vend
	protected Vector lstObjetsUtilisables;
	
	/**
	 * Constructeur de la classe Magasin qui permet d'initialiser
	 * la liste des ObjetsUtilisables.
	 */
	protected Magasin()
	{
		// Créer une nouvelle liste d'objets utilisables
		lstObjetsUtilisables = new Vector();
	}

	/**
	 * Cette fonction permet de retourner la liste des objets utilisables 
	 * que le magasin vend.
	 * 
	 * @return Vector : La liste des ObjetsUtilisables que le magasin vend
	 */
	public Vector obtenirListeObjetsUtilisables()
	{
	   return lstObjetsUtilisables;
	}
	
	/* 
	 * Cette fonction permet d'ajouter à la liste des objets utilisables
	 * un objet utilisable.
	 */
	public void ajouterObjetUtilisable(ObjetUtilisable objObjetUtilisable)
	{
		lstObjetsUtilisables.add(objObjetUtilisable);
	}
	
	/* 
	 * Cette fonction permet d'enlever de la liste des objets utilisables
	 * un objet utilisable
	 *
	 * @param: L'identifiant de l'objet qui permet d'identifier lequel
	 *         enlever
	 */
	public ObjetUtilisable enleverObjetUtilisable(int intObjetId)
	{
		for (int i = 0 ; i < lstObjetsUtilisables.size(); i++)
		{
			// Aller chercher l'objet
			ObjetUtilisable objObjetUtilisable = (ObjetUtilisable) lstObjetsUtilisables.get(i);
		
		    // Vérifier son id généré
		    if (objObjetUtilisable.obtenirId() == intObjetId)
		    {
		    	// Enlever l'objet
		    	lstObjetsUtilisables.remove(i);
		    	return objObjetUtilisable;
		    }
		}
		
		return null;
	}

	/* Cette fonction permet de savoir si un certain objet existe pour
	 * le magasin. On se sert de cette fonction lors des achats pour vérifier
	 * que le client envoie les bonnes données
	 */
	public boolean objetExiste(int intObjetId, String strTypeObjet)
	{
		for (int i = 0 ; i < lstObjetsUtilisables.size(); i++)
		{
			// Aller chercher l'objet
			ObjetUtilisable objObjetUtilisable = (ObjetUtilisable) lstObjetsUtilisables.get(i);
		
		    // Vérifier son id généré et son type
		    if (objObjetUtilisable.obtenirId() == intObjetId && 
		        objObjetUtilisable.obtenirTypeObjet().equals(strTypeObjet))
		    {
		    	// Objet existant
		    	return true;
		    }
		}
		
		// Objet inexistant
		return false;
	}
	
	public boolean objetExiste(int intObjetId)
	{
		for (int i = 0 ; i < lstObjetsUtilisables.size(); i++)
		{
			// Aller chercher l'objet
			ObjetUtilisable objObjetUtilisable = (ObjetUtilisable) lstObjetsUtilisables.get(i);
		
		    // Vérifier son id généré et son type
		    if (objObjetUtilisable.obtenirId() == intObjetId)
		    {
		    	// Objet existant
		    	return true;
		    }
		}
		
		// Objet inexistant
		return false;
	}	

	
	public ObjetUtilisable obtenirObjet(int intObjetId)
	{
		for (int i = 0 ; i < lstObjetsUtilisables.size(); i++)
		{
			// Aller chercher l'objet
			ObjetUtilisable objObjetUtilisable = (ObjetUtilisable) lstObjetsUtilisables.get(i);
		
		    // Vérifier son id généré
		    if (objObjetUtilisable.obtenirId() == intObjetId)
		    {
		    	// Objet existant
		    	return objObjetUtilisable;
		    }
		}
		
		// Objet inexistant
		return null;	
	}
	
	public ObjetUtilisable acheterObjet(int intObjetId, IntObj objProchainId)
	{

    	// Enlever l'objet de la liste du magasin
    	ObjetUtilisable objObjetAchete = enleverObjetUtilisable(intObjetId);
    	
    	// Une fois enlevé, on vérifie si l'objet est en vente illimité
        // auquel cas on en ajoute un nouveau
    	if (objObjetAchete.obtenirEstLimite() == false)
    	{
    		ObjetUtilisable objObjetRemplacement = null;
    		
    		if (objObjetAchete instanceof Reponse)
    		{
    			// Créer un nouvel objet de type Reponse
    			synchronized (objProchainId)
    			{
    				objObjetRemplacement = new Reponse(objProchainId.intValue, true);
    			}

    		}
    		
            // L'ajouter au magasin
    		ajouterObjetUtilisable((ObjetUtilisable)objObjetRemplacement);
    		
    		// Incrémenter le ID pour le prochain objet
    		synchronized (objProchainId)
            {
            	objProchainId.intValue++;
            
            }

    	}
    	
    	return objObjetAchete;
    	
	}
		
}