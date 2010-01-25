package ServeurJeu.ComposantesJeu.Joueurs;

// Cette classe contient tous les paramètres des objets que
// les joueurs virtuels ont besoin
public class ParametreIAObjet {
	
	// La valeur en points de cet objet
    public int intValeurPoints;
    
    // Une valeur aléatoire pour calculer des points un peu différents
    public int intValeurAleatoire;
    
    // Distance maximal pour ne pas enlever trop de points
    public int intMaxDistance;
    
    // Points à enlever par coup de distance
    public int intPointsEnleverDistance;   
    
    // Points à enlever par quantité déjà en possession
    public int intPointsEnleverQuantite;
    
    // Points à donner lors du calcul des points d'un chemin
    public int intPointsChemin;
    
    // Points à enlever à l'objet dans le calcul des points d'un chemin
    // selon la quantité possédé
    public int intPointsCheminEnleverQuantite;
    
    // Nombre d'objets maximum à posséder par ce joueur virtuel
    public int intQuantiteMax;
    
    // Temps de sûreté de fin de partie (on ne ramasse pas l'objet lorsqu'il
    // reste peu de temps en fin de partie (en secondes)
    public int intTempsSureteRamasser;
    
    // Constructeur de la classe, on passe tous les variables en paramètres
    public ParametreIAObjet(int valeurPoints, int valeurAleatoire, 
        int maxDistance, int pointsEnleverDistance, 
        int pointsEnleverQuantite, int pointsChemin,
        int pointsCheminEnleverQuantite, int quantiteMax,
        int tempsSureteRamasser)
    {
        intValeurPoints = valeurPoints;
	    intValeurAleatoire = valeurAleatoire;
	    intMaxDistance = maxDistance;
	    intPointsEnleverDistance = pointsEnleverDistance;   
	    intPointsEnleverQuantite = pointsEnleverQuantite;
	    intPointsChemin = pointsChemin;
	    intPointsCheminEnleverQuantite = pointsCheminEnleverQuantite;
	    intQuantiteMax = quantiteMax;
	    intTempsSureteRamasser = tempsSureteRamasser;
    }
    
}
