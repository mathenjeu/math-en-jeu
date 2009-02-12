package ClassesUtilitaires;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;
import Enumerations.Visibilite;
import ServeurJeu.BD.GestionnaireBD;
import ServeurJeu.ComposantesJeu.Cases.Case;
import ServeurJeu.ComposantesJeu.Cases.CaseCouleur;
import ServeurJeu.ComposantesJeu.Cases.CaseSpeciale;
import ServeurJeu.ComposantesJeu.Objets.Magasins.*;
import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.*;
import ServeurJeu.ComposantesJeu.Objets.Pieces.Piece;
import ServeurJeu.ComposantesJeu.ReglesJeu.Regles;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesCaseCouleur;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesCaseSpeciale;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesMagasin;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesObjetUtilisable;

/**
 * @author Jean-Fran�ois Brind'Amour
 */
public final class GenerateurPartie 
{
	
	
	/**
	 * Constructeur par d�faut est priv� pour emp�cher de pourvoir cr�er des 
	 * instances de cette classe.
	 */
    private GenerateurPartie() {}

    /**
     * Cette fonction permet de retourner une matrice � deux dimensions
     * repr�sentant le plateau de jeu qui contient les informations sur 
     * chaque case selon des param�tres.
     * @param objGestionnaireBD 
     * @param max_nb_joueurs 
     * @param lstPointsFinish 
     *
     * @param Regles reglesPartie : L'ensemble des r�gles pour la partie
     * @param int temps : Le temps de la partie
     * @param Vector listePointsCaseLibre : La liste des points des cases 
     * 										libres (param�tre de sortie)
     * @return Case[][] : Un tableau � deux dimensions contenant l'information
     * 					  sur chaque case.
     * @throws NullPointerException : Si la liste pass�e en param�tre qui doit 
     * 								  �tre remplie est nulle
     */
    public static Case[][] genererPlateauJeu(GestionnaireBD objGestionnaireBD, 
    		Regles reglesPartie, int temps, Vector<Point> listePointsCaseLibre,
    		Integer objDernierIdObjets, String butDuJeu, int max_nb_joueurs, 
    		ArrayList<Point> lstPointsFinish) throws NullPointerException
    {
		// Cr�ation d'un objet permettant de g�n�rer des nombres al�atoires
		Random objRandom = new Random();
               
        // Obtention du nombre d'objets maximal en vente par magasin
        int maxNbObjetsAVendre = reglesPartie.getIntMaxSaledObjects();
		
		// D�claration de points
		Point objPoint;
		
		// D�claration d'une liste de points contenant les points qui ont 
		// �t� pass�s
		Vector<Point> lstPointsCasesPresentes = new Vector<Point>();

		// D�claration d'une liste de points contenant les points qui 
		// contiennent des cases sp�ciales
		Vector<Point> lstPointsCasesSpeciales = new Vector<Point>();
		
		// D�claration d'une liste de points contenant les points qui 
		// contiennent des cases de couleur
		Vector<Point> lstPointsCasesCouleur = new Vector<Point>();
		
		// D�claration d'une liste de points contenant les points qui 
		// contiennent des magasins
		Vector<Point> lstPointsMagasins = new Vector<Point>();
		
		// D�claration d'une liste de points contenant les points qui 
		// contiennent des pi�ces
		Vector<Point> lstPointsPieces = new Vector<Point>();
		
		// D�claration d'une liste de points contenant les points qui 
		// contiennent des objets utilisables
		Vector<Point> lstPointsObjetsUtilisables = new Vector<Point>();
		
		// D�clarations du nombre de lignes et de colonnes du vecteur
		int intNbLignes = 0;
		int intNbColonnes = 0;
		
		// D�claration d'un compteur de cases
		int intCompteurCases = 0;

        // D�claration d'un compteur des id des objets
        int intCompteurIdObjet = 1;
        
		// D�claration d'une case dont le type est -1 (�a n'existe pas) qui
		// va nous servir pour identifier les cases qui ont �t� pass�es
		CaseCouleur objCaseParcourue = new CaseCouleur(1);
		
		// Modifier le temps pour qu'il soit au moins le minimum de minutes
		temps = Math.max(temps, reglesPartie.obtenirTempsMinimal());
		
		// Modifier le temps pour qu'il soit au plus le maximum de minutes
		temps = Math.min(temps, reglesPartie.obtenirTempsMaximal());
		
		//to have a more equilibrate dimension of game table
		temps = (int) Math.ceil(temps * 6 / 10) + 10;

		// Le nombre de lignes sera de ceiling(temps / 2) � temps
		intNbLignes = objRandom.nextInt(temps - ((int) Math.ceil(temps / 2)) + 1) + ((int) Math.ceil(temps / 2));

		// Le nombre de colonnes sera de temps � 2 * temps 
		intNbColonnes = (int) Math.ceil((temps * temps ) / ((max_nb_joueurs + 1)*intNbLignes) )*(max_nb_joueurs + 1);

		// D�claration de variables qui vont garder le nombre de trous, 
		// le nombre de cases sp�ciales, le nombres de magasins,
		// le nombre de pi�ces, le nombre de
		int intNbTrous = ((int) Math.ceil(intNbLignes * intNbColonnes * reglesPartie.obtenirRatioTrous()));
		int intNbCasesSpeciales = (int) Math.floor(intNbLignes * intNbColonnes * reglesPartie.obtenirRatioCasesSpeciales());
		int intNbMagasins = (int) Math.floor(intNbLignes * intNbColonnes * reglesPartie.obtenirRatioMagasins());
		int intNbPieces = (int) Math.floor(intNbLignes * intNbColonnes * reglesPartie.obtenirRatioPieces());
		int intNbObjetsUtilisables = (int) Math.floor(intNbLignes * intNbColonnes * reglesPartie.obtenirRatioObjetsUtilisables());

				
		// Maintenant qu'on a le nombre de lignes et de colonnes, on va cr�er
		// le tableau � 2 dimensions repr�sentant le plateau de jeu (null est 
		// mis par d�faut dans chaque �l�ment)
		Case[][] objttPlateauJeu = new Case[intNbLignes][intNbColonnes];	
		
		// we build table of game with the houls for the borders
		for(int x = 0; x < intNbLignes; x++){
			for(int y = 0; y < intNbColonnes; y++){
			
				if(ifNotBorder(x, y, intNbLignes, intNbColonnes, max_nb_joueurs)){
					// Cr�er le point de la case courante
					objPoint = new Point(x, y);
					
					// D�finir la valeur de la case au point sp�cifi� � la case 
					// d'identification
					objttPlateauJeu[objPoint.x][objPoint.y] = objCaseParcourue;
					
					// Ajouter le point dans la file de priorit� et dans la liste des 
					// points pass�s
					//lstFile.add(objPoint);
					lstPointsCasesPresentes.add(objPoint);
					intCompteurCases++;
				}
				
			}
		}// end first for
		
	
		// fill list with points for finish
		for(int i = 0; i < max_nb_joueurs; i++)
		{
			objPoint = (Point) lstPointsCasesPresentes.remove(lstPointsCasesPresentes.size()-1);
			lstPointsFinish.add(objPoint);
		}
		
		
		/* **************************************** !!!!!!!!!!!!!!!!!!!!!!!!!!!
		 
		// Trouver un point al�atoire dans le plateau de jeu et le garder 
		// en m�moire (�a va �tre le point de d�part) 
		objPoint = new Point(0, 0);
		
		// Au point calcul�, on va d�finir la case sp�ciale qui sert 
		// d'identification et dont le type est -1
		objttPlateauJeu[objPoint.x][objPoint.y] = objCaseParcourue;
		
		// Ajouter le point dans la file de priorit� et dans la liste des 
		// points pass�s
		lstFile.add(objPoint);
		lstPointsCasesPresentes.add(objPoint);
		
		// On choisi des cases dans le plateau en leur mettant la case sp�ciale 
		// d'identification comme valeur tant qu'on ne passe pas un certain 
		// pourcentage du nombre de cases totales possibles
		while (intCompteurCases <= (intNbLignes * intNbColonnes) - intNbTrous)
		{
			// S'il y a au moins un point dans la file, alors on va retirer 
			// ce point et le traiter, sinon il faut prendre un point 
			// al�atoirement parmis les points qui ont �t� pass�s
			if (lstFile.size() > 0)
			{
				// Prendre le point au d�but de la file
				objPointFile = (Point) lstFile.remove(0);
			}
			else
			{
				// Obtenir un point al�atoirement parmi les points qui ont 
				// d�j� �t� pass�s
				objPointFile = (Point) lstPointsCasesPresentes.get(objRandom.nextInt(lstPointsCasesPresentes.size()));
			}
			
			
			
			
			// S'il y a une case � gauche, et que cette case n'a pas encore �t�
			// pass�e et que une valeur al�atoire retourne true, alors on va
			// choisir cette case
			
			if ((objPointFile.x - 1 >= 0) && 
				(objttPlateauJeu[objPointFile.x - 1][objPointFile.y] == null) &&
				(objRandom.nextBoolean() == true) && ifNotBorder(objPointFile.x - 1,objPointFile.y, intNbLignes))
			{
				// Cr�er le point � gauche de la case courante
				objPoint = new Point(objPointFile.x - 1, objPointFile.y);
				
				// D�finir la valeur de la case au point sp�cifi� � la case 
				// d'identification
				objttPlateauJeu[objPoint.x][objPoint.y] = objCaseParcourue;
				
				// Ajouter le point dans la file de priorit� et dans la liste des 
				// points pass�s
				lstFile.add(objPoint);
				lstPointsCasesPresentes.add(objPoint);
				
				// Incr�menter le nombre de points pass�s
				intCompteurCases++;
			}
			
			// S'il y a une case � droite, et que cette case n'a pas encore �t�
			// pass�e et que une valeur al�atoire retourne true, alors on va
			// choisir cette case
			if ((objPointFile.x + 1 < intNbLignes) && 
				(objttPlateauJeu[objPointFile.x + 1][objPointFile.y] == null) && 
				(objRandom.nextBoolean() == true) && ifNotBorder(objPointFile.x + 1,objPointFile.y, intNbLignes))
			{
				// Cr�er le point � droite de la case courante
				objPoint = new Point(objPointFile.x + 1, objPointFile.y);
				
				// D�finir la valeur de la case au point sp�cifi� � la case 
				// d'identification
				objttPlateauJeu[objPoint.x][objPoint.y] = objCaseParcourue;
				
				// Ajouter le point dans la file de priorit� et dans la liste des 
				// points pass�s
				lstFile.add(objPoint);
				lstPointsCasesPresentes.add(objPoint);
				
				// Incr�menter le nombre de points pass�s
				intCompteurCases++;
			}
			
			// S'il y a une case en haut, et que cette case n'a pas encore �t�
			// pass�e et que une valeur al�atoire retourne true, alors on va
			// choisir cette case
			if ((objPointFile.y - 1 >= 0) && 
				(objttPlateauJeu[objPointFile.x][objPointFile.y - 1] == null) && 
				(objRandom.nextBoolean() == true)&& ifNotBorder(objPointFile.x ,objPointFile.y - 1, intNbLignes))
			{
				// Cr�er le point en haut de la case courante
				objPoint = new Point(objPointFile.x, objPointFile.y - 1);
				
				// D�finir la valeur de la case au point sp�cifi� � la case 
				// d'identification
				objttPlateauJeu[objPoint.x][objPoint.y] = objCaseParcourue;
				
				// Ajouter le point dans la file de priorit� et dans la liste des 
				// points pass�s
				lstFile.add(objPoint);
				lstPointsCasesPresentes.add(objPoint);
				
				// Incr�menter le nombre de points pass�s
				intCompteurCases++;
			}
			
			// S'il y a une case en bas, et que cette case n'a pas encore �t�
			// pass�e et que une valeur al�atoire retourne true, alors on va
			// choisir cette case
			if ((objPointFile.y + 1 < intNbColonnes) && 
				(objttPlateauJeu[objPointFile.x][objPointFile.y + 1] == null) && 
				(objRandom.nextBoolean() == true) && ifNotBorder(objPointFile.x ,objPointFile.y + 1, intNbLignes))
			{
				// Cr�er le point en bas de la case courante
				objPoint = new Point(objPointFile.x, objPointFile.y + 1);
				
				// D�finir la valeur de la case au point sp�cifi� � la case 
				// d'identification
				objttPlateauJeu[objPoint.x][objPoint.y] = objCaseParcourue;
				
				// Ajouter le point dans la file de priorit� et dans la liste des 
				// points pass�s
				lstFile.add(objPoint);
				lstPointsCasesPresentes.add(objPoint);
				
				// Incr�menter le nombre de points pass�s
				intCompteurCases++;
			}
		}  // end while                 */////////////////
		
				
		// Si on doit afficher des cases sp�ciales dans le plateau de jeu, 
		// alors on fait le code suivant
		if (reglesPartie.obtenirListeCasesSpecialesPossibles().size() > 0)
		{
			// R�initialiser le compteur de cases
			int intCompteurCasesSpeciale = 0;
			
			// Obtenir un it�rateur pour la liste des r�gles de cases sp�ciales
			// tri�es par priorit� (c'est certain que la premi�re fois il y a au 
			// moins une r�gle de case)
			Iterator objIterateurListePriorite = reglesPartie.obtenirListeCasesSpecialesPossibles().iterator();
			
			// On va choisir des cases sp�ciales en commen�ant par la case
			// la plus prioritaire et on va faire �a tant qu'on n'a pas atteint 
			// le pourcentage de cases sp�ciales devant se trouver sur le plateau 
			// de jeu. Si on atteint la fin de la liste de cases sp�ciales, on 
			// recommence depuis le d�but
			while (intCompteurCasesSpeciale < intNbCasesSpeciales)
			{
				// Faire la r�f�rence vers la r�gle de la case sp�ciale 
				// courante
				ReglesCaseSpeciale objReglesCaseSpeciale = (ReglesCaseSpeciale) objIterateurListePriorite.next();
				
				// Obtenir un point al�atoirement parmi les points restants
				// qui n'ont pas de cases sp�ciales et enlever en m�me temps 
				// ce point de la liste
				objPoint = (Point) lstPointsCasesPresentes.remove(objRandom.nextInt(lstPointsCasesPresentes.size()));
				
				// Ajouter le point trouv� dans la liste des points de cases 
				// sp�ciales trouv�es
				lstPointsCasesSpeciales.add(objPoint);

				// D�finir la valeur de la case au point sp�cifi� � la case 
				// d'identification
				objttPlateauJeu[objPoint.x][objPoint.y] = new CaseSpeciale(objReglesCaseSpeciale.obtenirTypeCase());				
				
				// Incr�menter le nombre de cases pass�es
				intCompteurCasesSpeciale++;
				
				// Si on est arriv� � la fin de la liste, alors il faut 
				// retourner au d�but
				if (objIterateurListePriorite.hasNext() == false)
				{
					// Obtenir un autre it�rateur pour la liste
					objIterateurListePriorite = reglesPartie.obtenirListeCasesSpecialesPossibles().iterator();
				}
			}			
		}
		
		// Bloc de code qui va s'assurer de cr�er les cases de couleur dans le 
		// plateau de jeu (il y en a au moins un type)
		{
			// R�initialiser le compteur de cases
			int intCompteurCasesCouleur = 0;
			
			// Obtenir un it�rateur pour la liste des r�gles de cases de couleur
			// tri�es par priorit� (c'est certain que la premi�re fois il y a au 
			// moins une r�gle de case)
			Iterator objIterateurListePriorite = reglesPartie.obtenirListeCasesCouleurPossibles().iterator();
			
			// On va choisir des cases de couleur en commen�ant par la case
			// la plus prioritaire et on va faire �a tant qu'on n'a pas atteint 
			// le pourcentage de cases sp�ciales devant se trouver sur le plateau 
			// de jeu. Si on atteint la fin de la liste de cases de couleur, on 
			// recommence depuis le d�but
			while (intCompteurCasesCouleur < intCompteurCases - intNbCasesSpeciales - max_nb_joueurs)
			{
				
				// Faire la r�f�rence vers la r�gle de la case de couleur 
				// courante
				ReglesCaseCouleur objReglesCaseCouleur = (ReglesCaseCouleur) objIterateurListePriorite.next();
				
				// Obtenir un point al�atoirement parmi les points restants
				// qui n'ont pas de cases sp�ciales et de cases de couleur 
				// et enlever en m�me temps ce point de la liste
				objPoint = (Point) lstPointsCasesPresentes.remove(objRandom.nextInt(lstPointsCasesPresentes.size()));
				
				// Ajouter le point trouv� dans la liste des points de cases 
				// de couleur trouv�es
				lstPointsCasesCouleur.add(objPoint);

				// D�finir la valeur de la case au point sp�cifi� � la case 
				// d'identification
				objttPlateauJeu[objPoint.x][objPoint.y] = new CaseCouleur(objReglesCaseCouleur.obtenirTypeCase());				
				
				// Incr�menter le nombre de cases pass�es
				intCompteurCasesCouleur++;
				
				// Si on est arriv� � la fin de la liste, alors il faut 
				// retourner au d�but
				if (objIterateurListePriorite.hasNext() == false)
				{
					// Obtenir un autre it�rateur pour la liste
					objIterateurListePriorite = reglesPartie.obtenirListeCasesCouleurPossibles().iterator();
				}
			}
			
			// La liste des cases pr�sentes est maintenant la liste des cases 
			// de couleur, car tous les points de la liste ont �t� copi�s dans 
			// l'autre liste
			lstPointsCasesPresentes = lstPointsCasesCouleur;
			lstPointsCasesCouleur = null;
		}
		
		// Si on doit afficher des magasins dans le plateau de jeu, 
		// alors on fait le code suivant
		if (reglesPartie.obtenirListeMagasinsPossibles().size() > 0)
		{
			// R�initialiser le compteur de cases
			intCompteurCases = 1;
			
			// Obtenir un it�rateur pour la liste des r�gles de magasins
			// tri�es par priorit� (c'est certain que la premi�re fois il y a au 
			// moins une r�gle de case)
			Iterator objIterateurListePriorite = reglesPartie.obtenirListeMagasinsPossibles().iterator();
			
			// On va choisir des magasins en commen�ant par la case la plus 
			// prioritaire et on va faire �a tant qu'on n'a pas atteint le 
			// pourcentage de magasins devant se trouver sur le plateau 
			// de jeu. Si on atteint la fin de la liste de magasins, on 
			// recommence depuis le d�but
			while (intCompteurCases <= intNbMagasins)
			{
				// Faire la r�f�rence vers la r�gle du magasin courant 
				ReglesMagasin objReglesMagasin = (ReglesMagasin) objIterateurListePriorite.next();
				
				// Obtenir un point al�atoirement parmi les points restants
				// qui n'ont pas de magasins et enlever en m�me temps ce point 
				// de la liste
				objPoint = (Point) lstPointsCasesPresentes.remove(objRandom.nextInt(lstPointsCasesPresentes.size()));
				
				// Ajouter le point trouv� dans la liste des points de magasins 
				// trouv�s
				lstPointsMagasins.add(objPoint);

				// Si le nom du magasin est Magasin1, alors on met un objet 
				// Magasin(1) sur la case, sinon on fait le m�me genre de 
				// v�rifications pour les autres types de magasins                                
				if (objReglesMagasin.obtenirNomMagasin().equals("Magasin1") || 
						objReglesMagasin.obtenirNomMagasin().equals("Shop1"))
				{
					// D�finir la valeur de la case au point sp�cifi� � la case 
					// d'identification
					((CaseCouleur) objttPlateauJeu[objPoint.x][objPoint.y]).definirObjetCase(new Magasin1());
				}
				else if (objReglesMagasin.obtenirNomMagasin().equals("Magasin2") || 
						objReglesMagasin.obtenirNomMagasin().equals("Shop2"))
				{
					// D�finir la valeur de la case au point sp�cifi� � la case 
					// d'identification
					((CaseCouleur) objttPlateauJeu[objPoint.x][objPoint.y]).definirObjetCase(new Magasin2());
				}else if (objReglesMagasin.obtenirNomMagasin().equals("Magasin3") || 
						objReglesMagasin.obtenirNomMagasin().equals("Shop3"))
				{
					// D�finir la valeur de la case au point sp�cifi� � la case 
					// d'identification
					((CaseCouleur) objttPlateauJeu[objPoint.x][objPoint.y]).definirObjetCase(new Magasin3());
				}else if (objReglesMagasin.obtenirNomMagasin().equals("Magasin4"))
				{
					// D�finir la valeur de la case au point sp�cifi� � la case 
					// d'identification
					((CaseCouleur) objttPlateauJeu[objPoint.x][objPoint.y]).definirObjetCase(new Magasin4());
				}
				
				// Aller chercher une r�f�rence vers le magasin que l'on vient de cr�er
				Magasin objMagasin = (Magasin)((CaseCouleur) objttPlateauJeu[objPoint.x][objPoint.y]).obtenirObjetCase();

				
				// Get the list of items to be sold by shops
				ArrayList<String> listObjects = new ArrayList<String>();
				objGestionnaireBD.fillShopObjects(objReglesMagasin.obtenirNomMagasin(), listObjects);
				
				for(String nomDeLObjet : listObjects)
				{
                   	// Incr�menter le compteur de ID pour les objets
                    intCompteurIdObjet++;

                    
                    // On cr�e un nouvel objet du type correspondant
                    // puis on l'ajoute dans la liste des objets utilisables du magasin
                    if(nomDeLObjet.equals("Livre"))
                    {
                        Livre objAAjouter = new Livre(intCompteurIdObjet, true);
                        objMagasin.ajouterObjetUtilisable((ObjetUtilisable)objAAjouter);
                    }
                    else if(nomDeLObjet.equals("Papillon"))
                    {
                        Papillon objAAjouter = new Papillon(intCompteurIdObjet, true);
                        objMagasin.ajouterObjetUtilisable((ObjetUtilisable)objAAjouter);
                    }
                    else if(nomDeLObjet.equals("Boule"))
                    {
                        Boule objAAjouter = new Boule(intCompteurIdObjet, true);
                        objMagasin.ajouterObjetUtilisable((ObjetUtilisable)objAAjouter);
                    }
                    else if(nomDeLObjet.equals("Telephone"))
                    {
                        Telephone objAAjouter = new Telephone(intCompteurIdObjet, true);
                        objMagasin.ajouterObjetUtilisable((ObjetUtilisable)objAAjouter);
                    }
                    else if(nomDeLObjet.equals("PotionGros"))
                    {
                        PotionGros objAAjouter = new PotionGros(intCompteurIdObjet, true);
                        objMagasin.ajouterObjetUtilisable((ObjetUtilisable)objAAjouter);
                    }
                    else if(nomDeLObjet.equals("PotionPetit"))
                    {
                        PotionPetit objAAjouter = new PotionPetit(intCompteurIdObjet, true);
                        objMagasin.ajouterObjetUtilisable((ObjetUtilisable)objAAjouter);
                    }
                    else if(nomDeLObjet.equals("Banane"))
                    {
                        Banane objAAjouter = new Banane(intCompteurIdObjet, true);
                        objMagasin.ajouterObjetUtilisable((ObjetUtilisable)objAAjouter);
                    }
				}// end for              
				
				// Incr�menter le nombre de cases pass�es
				intCompteurCases++;
				
				// Si on est arriv� � la fin de la liste, alors il faut 
				// retourner au d�but
				if (objIterateurListePriorite.hasNext() == false)
				{
					// Obtenir un autre it�rateur pour la liste
					objIterateurListePriorite = reglesPartie.obtenirListeMagasinsPossibles().iterator();
				}
			}			
		}
		
		// Bloc de code qui va s'assurer de cr�er les pi�ces dans le plateau de jeu
		// R�initialiser le compteur de cases
			intCompteurCases = 1;
			
			// On va choisir des pi�ces dont la valeur est al�atoire selon 
			// une loi normale centr�e � 0 tant qu'on n'a pas atteint le 
			// nombre de pi�ces d�sir�
			while (intCompteurCases <= intNbPieces)
			{
				// Calculer la valeur de la pi�ce � cr�er de fa�on al�atoire 
				// selon une loi normale
				int intValeur = Math.max(Math.abs(ClassesUtilitaires.UtilitaireNombres.genererNbAleatoireLoiNormale(0.0d, Math.pow(((double) reglesPartie.obtenirValeurPieceMaximale()) / 3.0d, 2.0d))), 1);
					
				// Obtenir un point al�atoirement parmi les points restants
				// qui n'ont pas de pi�ces et enlever en m�me temps ce point 
				// de la liste
				objPoint = (Point) lstPointsCasesPresentes.remove(objRandom.nextInt(lstPointsCasesPresentes.size()));
				
				// Ajouter le point trouv� dans la liste des points de pi�ces 
				// trouv�es
				lstPointsPieces.add(objPoint);
				
				// D�finir la valeur de la case au point sp�cifi� � la case 
				// d'identification
				((CaseCouleur) objttPlateauJeu[objPoint.x][objPoint.y]).definirObjetCase(new Piece(intValeur, 1));				
				
				// Incr�menter le nombre de cases pass�es
				intCompteurCases++;
			}			
		
			
		
		// Si on doit afficher des objets utilisables dans le plateau de jeu, 
		// alors on fait le code suivant
		if (reglesPartie.obtenirListeObjetsUtilisablesPossibles().size() > 0)
		{
			// R�initialiser le compteur de cases
			intCompteurCases = 1;
			
			// Obtenir un it�rateur pour la liste des r�gles d'objets utilisables
			// tri�s par priorit� (c'est certain que la premi�re fois il y a au 
			// moins une r�gle de case)
			Iterator objIterateurListePriorite = reglesPartie.obtenirListeObjetsUtilisablesPossibles().iterator();
			
			// On va choisir des objets utilisables en commen�ant par 
			// l'objet le plus prioritaire et on va faire �a tant qu'on n'a 
			// pas atteint le pourcentage d'objets utilisables devant se 
			// trouver sur le plateau de jeu. Si on atteint la fin de la 
			// liste d'objets utilisables, on recommence depuis le d�but
			while (intCompteurCases <= intNbObjetsUtilisables)
			{
				// Faire la r�f�rence vers la r�gle de l'objet utilisable 
				// courant
				ReglesObjetUtilisable objReglesObjetUtilisable = (ReglesObjetUtilisable) objIterateurListePriorite.next();
				
				// D�claration d'une variable qui va permettre de savoir si 
				// l'objet doit �tre visible ou non
				boolean bolEstVisible;
				
				// Si l'objet est toujours visible, alors on va dire qu'il 
				// est visible
				if (objReglesObjetUtilisable.obtenirVisibilite().equals(Visibilite.ToujoursVisible))
				{
					bolEstVisible = true;
				}
				// Si l'objet est jamais visible, alors on va dire qu'il 
				// n'est pas visible
				else if (objReglesObjetUtilisable.obtenirVisibilite().equals(Visibilite.JamaisVisible))
				{
					bolEstVisible = false;
				}
				// Si l'objet doit avoir une visibilit� al�atoire, alors 
				// on va g�n�rer un nombre al�atoire qui va donner soit true 
				// soit false
				else
				{
					bolEstVisible = objRandom.nextBoolean();
				}
				
				// Obtenir un point al�atoirement parmi les points restants
				// qui n'ont pas d'objets utilisables et enlever en m�me temps 
				// ce point de la liste
				objPoint = (Point) lstPointsCasesPresentes.remove(objRandom.nextInt(lstPointsCasesPresentes.size()));
				
				// Ajouter le point trouv� dans la liste des points d'objets 
				// utilisables trouv�s
				lstPointsObjetsUtilisables.add(objPoint);

				// Si le nom de l'objet est Livre, alors on met un objet 
				// Livre sur la case, sinon on fait le m�me genre de 
				// v�rifications pour les autres types de magasins
                                // On d�finit la valeur de la case au point sp�cifi� � la case d'identification
				if (objReglesObjetUtilisable.obtenirNomObjetUtilisable().equals("Livre") || 
						objReglesObjetUtilisable.obtenirNomObjetUtilisable().equals("Book"))
				{
					((CaseCouleur) objttPlateauJeu[objPoint.x][objPoint.y]).definirObjetCase(new Livre(intCompteurIdObjet, bolEstVisible));					
				}
                                else if (objReglesObjetUtilisable.obtenirNomObjetUtilisable().equals("Papillon") ||
                                		objReglesObjetUtilisable.obtenirNomObjetUtilisable().equals("Butterfly"))
				{
					((CaseCouleur) objttPlateauJeu[objPoint.x][objPoint.y]).definirObjetCase(new Papillon(intCompteurIdObjet, bolEstVisible));					
				}
                                else if (objReglesObjetUtilisable.obtenirNomObjetUtilisable().equals("Telephone"))
				{
					((CaseCouleur) objttPlateauJeu[objPoint.x][objPoint.y]).definirObjetCase(new Telephone(intCompteurIdObjet, bolEstVisible));					
				}
                                else if (objReglesObjetUtilisable.obtenirNomObjetUtilisable().equals("Boule") ||
                                		objReglesObjetUtilisable.obtenirNomObjetUtilisable().equals("Sphere"))
				{
					((CaseCouleur) objttPlateauJeu[objPoint.x][objPoint.y]).definirObjetCase(new Boule(intCompteurIdObjet, bolEstVisible));					
				}
                                else if (objReglesObjetUtilisable.obtenirNomObjetUtilisable().equals("PotionGros") ||
                                		objReglesObjetUtilisable.obtenirNomObjetUtilisable().equals("Big mixture"))
				{
					((CaseCouleur) objttPlateauJeu[objPoint.x][objPoint.y]).definirObjetCase(new PotionGros(intCompteurIdObjet, bolEstVisible));					
				}
                                else if (objReglesObjetUtilisable.obtenirNomObjetUtilisable().equals("PotionPetit") ||
                                		objReglesObjetUtilisable.obtenirNomObjetUtilisable().equals("Small mixture"))
				{
					((CaseCouleur) objttPlateauJeu[objPoint.x][objPoint.y]).definirObjetCase(new PotionPetit(intCompteurIdObjet, bolEstVisible));					
				}
                                else if (objReglesObjetUtilisable.obtenirNomObjetUtilisable().equals("Banane") ||
                                		objReglesObjetUtilisable.obtenirNomObjetUtilisable().equals("Banana"))
				{
					((CaseCouleur) objttPlateauJeu[objPoint.x][objPoint.y]).definirObjetCase(new Banane(intCompteurIdObjet, bolEstVisible));					
				}
				
				// Incr�menter le nombre de cases pass�es
				intCompteurCases++;
				
				// Incr�menter le compteur des id des objets
				intCompteurIdObjet++;
				
				// Si on est arriv� � la fin de la liste, alors il faut 
				// retourner au d�but
				if (objIterateurListePriorite.hasNext() == false)
				{
					// Obtenir un autre it�rateur pour la liste
					objIterateurListePriorite = reglesPartie.obtenirListeObjetsUtilisablesPossibles().iterator();
				}
			}			
		}
		
		// Ajouter les points restants dans la liste des points repr�sentant 
		// les cases sans objets et n'�tant pas des cases sp�ciales
		 listePointsCaseLibre.addAll(lstPointsCasesPresentes);
		
		// Indiquer quel a �t� le dernier id des objets
		objDernierIdObjets = intCompteurIdObjet;
		
		return objttPlateauJeu;
    }

    /**
     * Methode used to create the game board
     * @param x
     * @param y
     * @param intNbLignes
     * @param intNbColonnes
     * @param max_nb_joueurs 
     * @return
     */
    private static boolean ifNotBorder(int x, int y, int intNbLignes, int intNbColonnes, int max_nb_joueurs) {
		
    	boolean notborder = true;
		if ( (max_nb_joueurs % 2 == 0) && (y + 1) % (max_nb_joueurs + 1) == 0 ){
			
			if(y % 2 == 0 && x <= intNbLignes - max_nb_joueurs - 1 )
		      notborder = false;
			else if (y % 2 == 1 && x > max_nb_joueurs - 1 )
				 notborder = false;
		
		}else if (max_nb_joueurs % 2 == 1 && (y + 1) % (max_nb_joueurs + 1) == 0 ){
		
			if((y + 1) / (max_nb_joueurs + 1) %  2 == 1 && x <= intNbLignes - max_nb_joueurs - 1 )
			      notborder = false;
			else if ((y + 1) / (max_nb_joueurs + 1) % 2 == 0 && x > max_nb_joueurs - 1 )
					 notborder = false;
		
		}
		
		return notborder;
	}

	/**
     * Cette fonction permet de g�n�rer la position des joueurs. Chaque joueur 
     * est g�n�r� sur une case vide.
     * 
     * @param int nbJoueurs : Le nombre de joueurs dont g�n�rer la position
     * @param Vector listePointsCaseLibre : La liste des points des cases libres
     * @return Point[] : Un tableau de points pour chaque joueur 
     */
    public static Point[] genererPositionJoueurs(int nbJoueurs, Vector<Point> listePointsCaseLibre)
    {
		// Cr�er un tableau contenant les nbJoueurs points
		Point[] objtPositionJoueurs = new Point[nbJoueurs];
		
				
		// Pour tous les joueurs de la partie, on va g�n�rer des positions de joueurs
		for (int i = 0; i < nbJoueurs; i++)
		{
		
			objtPositionJoueurs[i] = new Point(0,i);
		}
		
		return objtPositionJoueurs;
    }
}