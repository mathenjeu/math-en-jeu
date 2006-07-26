package ClassesUtilitaires;

import java.awt.Point;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;
import Enumerations.Visibilite;
import ServeurJeu.ComposantesJeu.Cases.Case;
import ServeurJeu.ComposantesJeu.Cases.CaseCouleur;
import ServeurJeu.ComposantesJeu.Cases.CaseSpeciale;
import ServeurJeu.ComposantesJeu.Objets.Magasins.Magasin1;
import ServeurJeu.ComposantesJeu.Objets.Magasins.Magasin2;
import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.Reponse;
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
    public static Case[][] genererPlateauJeu(Regles reglesPartie, int temps, Vector listePointsCaseLibre) throws NullPointerException
    {
		// Cr�ation d'un objet permettant de g�n�rer des nombres al�atoires
		Random objRandom = new Random();
		
		// D�claration de points
		Point objPoint;
		Point objPointFile;
		
		// D�claration d'une file qui va contenir des points
		Vector lstFile = new Vector();
		
		// D�claration d'une liste de points contenant les points qui ont 
		// �t� pass�s
		Vector lstPointsCasesPresentes = new Vector();

		// D�claration d'une liste de points contenant les points qui 
		// contiennent des cases sp�ciales
		Vector lstPointsCasesSpeciales = new Vector();
		
		// D�claration d'une liste de points contenant les points qui 
		// contiennent des cases de couleur
		Vector lstPointsCasesCouleur = new Vector();
		
		// D�claration d'une liste de points contenant les points qui 
		// contiennent des magasins
		Vector lstPointsMagasins = new Vector();
		
		// D�claration d'une liste de points contenant les points qui 
		// contiennent des pi�ces
		Vector lstPointsPieces = new Vector();
		
		// D�claration d'une liste de points contenant les points qui 
		// contiennent des objets utilisables
		Vector lstPointsObjetsUtilisables = new Vector();
		
		// D�clarations du nombre de lignes et de colonnes du vecteur
		int intNbLignes = 0;
		int intNbColonnes = 0;
		
		// D�claration d'un compteur de cases
		int intCompteurCases = 1;
		
		// D�claration d'une case dont le type est -1 (�a n'existe pas) qui
		// va nous servir pour identifier les cases qui ont �t� pass�es
		CaseCouleur objCaseParcourue = new CaseCouleur(1);
		
		// Modifier le temps pour qu'il soit au moins le minimum de minutes
		temps = Math.max(temps, reglesPartie.obtenirTempsMinimal());
		
		// Modifier le temps pour qu'il soit au plus le maximum de minutes
		temps = Math.min(temps, reglesPartie.obtenirTempsMaximal());

		// Le nombre de lignes sera de ceiling(temps / 2) � temps
		intNbLignes = objRandom.nextInt(temps - ((int) Math.ceil(temps / 2)) + 1) + ((int) Math.ceil(temps / 2));

		// Le nombre de colonnes sera de temps � 2 * temps 
		intNbColonnes = (int) Math.ceil((temps * temps) / intNbLignes);

		// D�claration de variables qui vont garder le nombre de trous, 
		// le nombre de cases sp�ciales, le nombres de magasins,
		// le nombre de pi�ces, le nombre de
		int intNbTrous = (intNbLignes * intNbColonnes) - ((int) Math.ceil(intNbLignes * intNbColonnes * (1 - reglesPartie.obtenirRatioTrous())));
		int intNbCasesSpeciales = (int) Math.floor(intNbLignes * intNbColonnes * reglesPartie.obtenirRatioCasesSpeciales());
		int intNbMagasins = (int) Math.floor(intNbLignes * intNbColonnes * reglesPartie.obtenirRatioMagasins());
		int intNbPieces = (int) Math.floor(intNbLignes * intNbColonnes * reglesPartie.obtenirRatioPieces());
		int intNbObjetsUtilisables = (int) Math.floor(intNbLignes * intNbColonnes * reglesPartie.obtenirRatioObjetsUtilisables());

		// Maintenant qu'on a le nombre de lignes et de colonnes, on va cr�er
		// le tableau � 2 dimensions repr�sentant le plateau de jeu (null est 
		// mis par d�faut dans chaque �l�ment)
		Case[][] objttPlateauJeu = new Case[intNbLignes][intNbColonnes];		
		
		// Trouver un point al�atoire dans le plateau de jeu et le garder 
		// en m�moire (�a va �tre le point de d�part)
		objPoint = new Point(objRandom.nextInt(intNbLignes), objRandom.nextInt(intNbColonnes));
		
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
				(objRandom.nextBoolean() == true))
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
				(objRandom.nextBoolean() == true))
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
				(objRandom.nextBoolean() == true))
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
				(objRandom.nextBoolean() == true))
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
		}
		
		/*// Passer tous les points qui n'ont pas �t� trait�s et les remettre � 
		// null dans le plateau de jeu
		for (int i = 0; i < lstFile.size(); i++)
		{
			// Faire la r�f�rence vers le point courant
			objPointFile = (Point) lstFile.get(i);
			
			// Enlever le point courant dans la liste des cases pr�sentes (car 
			// il ne doit pas �tre disponible, il doit �tre libre)
			lstPointsCasesPresentes.remove(objPointFile);
			
			// On remet null dans la case courante
			objttPlateauJeu[objPointFile.x][objPointFile.y] = null;			
		}*/
		
		// Si on doit afficher des cases sp�ciales dans le plateau de jeu, 
		// alors on fait le code suivant
		if (reglesPartie.obtenirListeCasesSpecialesPossibles().size() > 0)
		{
			// R�initialiser le compteur de cases
			intCompteurCases = 1;
			
			// Obtenir un it�rateur pour la liste des r�gles de cases sp�ciales
			// tri�es par priorit� (c'est certain que la premi�re fois il y a au 
			// moins une r�gle de case)
			Iterator objIterateurListePriorite = reglesPartie.obtenirListeCasesSpecialesPossibles().iterator();
			
			// On va choisir des cases sp�ciales en commen�ant par la case
			// la plus prioritaire et on va faire �a tant qu'on n'a pas atteint 
			// le pourcentage de cases sp�ciales devant se trouver sur le plateau 
			// de jeu. Si on atteint la fin de la liste de cases sp�ciales, on 
			// recommence depuis le d�but
			while (intCompteurCases <= intNbCasesSpeciales)
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
				intCompteurCases++;
				
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
			intCompteurCases = 1;
			
			// Obtenir un it�rateur pour la liste des r�gles de cases de couleur
			// tri�es par priorit� (c'est certain que la premi�re fois il y a au 
			// moins une r�gle de case)
			Iterator objIterateurListePriorite = reglesPartie.obtenirListeCasesCouleurPossibles().iterator();
			
			// On va choisir des cases de couleur en commen�ant par la case
			// la plus prioritaire et on va faire �a tant qu'on n'a pas atteint 
			// le pourcentage de cases sp�ciales devant se trouver sur le plateau 
			// de jeu. Si on atteint la fin de la liste de cases de couleur, on 
			// recommence depuis le d�but
			while (intCompteurCases <= (intNbLignes * intNbColonnes) - intNbTrous - intNbCasesSpeciales)
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
				intCompteurCases++;
				
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
				// Magasin1 sur la case, sinon on fait le m�me genre de 
				// v�rifications pour les autres types de agasins
				if (objReglesMagasin.obtenirNomMagasin().equals("Magasin1"))
				{
					// D�finir la valeur de la case au point sp�cifi� � la case 
					// d'identification
					((CaseCouleur) objttPlateauJeu[objPoint.x][objPoint.y]).definirObjetCase(new Magasin1());					
				}
				else if (objReglesMagasin.obtenirNomMagasin().equals("Magasin2"))
				{
					// D�finir la valeur de la case au point sp�cifi� � la case 
					// d'identification
					((CaseCouleur) objttPlateauJeu[objPoint.x][objPoint.y]).definirObjetCase(new Magasin2());
				}
				
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
		
		// Bloc de code qui va s'assurer de cr�er les pi�ces dans le plateau 
		// de jeu
		{
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
				((CaseCouleur) objttPlateauJeu[objPoint.x][objPoint.y]).definirObjetCase(new Piece(intValeur));				
				
				// Incr�menter le nombre de cases pass�es
				intCompteurCases++;
			}			
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
				// Si l'objet est doit avoir une visibilit� al�atoire, alors 
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

				// Si le nom de l'objet est Reponse, alors on met un objet 
				// Reponse sur la case, sinon on fait le m�me genre de 
				// v�rifications pour les autres types de agasins
				if (objReglesObjetUtilisable.obtenirNomObjetUtilisable().equals("Reponse"))
				{
					// D�finir la valeur de la case au point sp�cifi� � la case 
					// d'identification
					((CaseCouleur) objttPlateauJeu[objPoint.x][objPoint.y]).definirObjetCase(new Reponse(intCompteurCases, bolEstVisible));					
				}
				
				// Incr�menter le nombre de cases pass�es
				intCompteurCases++;
				
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
		
		return objttPlateauJeu;
    }
    
    public static Case[][] genererPlateauJeu2(Regles reglesPartie, int temps, Vector listePointsCaseLibre) throws NullPointerException
    {
//    	 Modifier le temps pour qu'il soit au moins le minimum de minutes
		temps = Math.max(temps, reglesPartie.obtenirTempsMinimal());
		
		// Modifier le temps pour qu'il soit au plus le maximum de minutes
		temps = Math.min(temps, reglesPartie.obtenirTempsMaximal());

		// Le nombre de lignes sera de ceiling(temps / 2) � temps
		int intNbLignes = UtilitaireNombres.genererNbAleatoire( temps - ((int) Math.ceil(temps / 2)) + 1) + ((int) Math.ceil(temps / 2) );
		// Le nombre de colonnes sera de temps � 2 * temps 
		int intNbColonnes = (int) Math.ceil((temps * temps) / intNbLignes);

		// D�claration de variables qui vont garder le nombre de trous, 
		// le nombre de cases sp�ciales, le nombres de magasins,
		// le nombre de pi�ces, le nombre de
		int intNbTrous = (intNbLignes * intNbColonnes) - ((int) Math.ceil(intNbLignes * intNbColonnes * (1 - reglesPartie.obtenirRatioTrous())));
		int intNbCasesSpeciales = (int) Math.floor(intNbLignes * intNbColonnes * reglesPartie.obtenirRatioCasesSpeciales());
		int intNbMagasins = (int) Math.floor(intNbLignes * intNbColonnes * reglesPartie.obtenirRatioMagasins());
		int intNbPieces = (int) Math.floor(intNbLignes * intNbColonnes * reglesPartie.obtenirRatioPieces());
		int intNbObjetsUtilisables = (int) Math.floor(intNbLignes * intNbColonnes * reglesPartie.obtenirRatioObjetsUtilisables());

		// Maintenant qu'on a le nombre de lignes et de colonnes, on va cr�er
		// le tableau � 2 dimensions repr�sentant le plateau de jeu (null est 
		// mis par d�faut dans chaque �l�ment)
		Case[][] objPlateauJeu = new Case[intNbLignes][intNbColonnes];
		
		for( int i = 0; i < intNbLignes; i++ )
		{
			for( int j = 0; j < intNbColonnes; j++ )
			{
				listePointsCaseLibre.add( new Point( i, j ) );
			}
		}
		
		//vecteur avec les points
		//enlever les trous aleatoirement
		for( int i = 0; i < intNbTrous; i++ )
		{
			listePointsCaseLibre.remove(ClassesUtilitaires.UtilitaireNombres.genererNbAleatoire( listePointsCaseLibre.size() ) );
		}
		//enlever les cases speciales aleatoirement
		Point point = null;
		for( int i = 0; i < intNbCasesSpeciales; i++ )
		{
			point = (Point)listePointsCaseLibre.remove(ClassesUtilitaires.UtilitaireNombres.genererNbAleatoire( listePointsCaseLibre.size() ) );
			//generer case special
			objPlateauJeu[point.x][point.y] = new CaseSpeciale(1);
		}
		//le reste sont des case couleur
		int nbCaseCouleur = listePointsCaseLibre.size();
		for( int i = 0; i < nbCaseCouleur; i++ )
		{
			point = (Point)listePointsCaseLibre.get(ClassesUtilitaires.UtilitaireNombres.genererNbAleatoire( listePointsCaseLibre.size() ) );
			//generer case special
			objPlateauJeu[point.x][point.y] = new CaseCouleur(1);
		}
		//enlever celles avec magasins aleatoirement
		for( int i = 0; i < intNbMagasins; i++ )
		{
			point = (Point)listePointsCaseLibre.remove(ClassesUtilitaires.UtilitaireNombres.genererNbAleatoire( listePointsCaseLibre.size() ) );
			//generer case couleur avec magasin
			((CaseCouleur) objPlateauJeu[point.x][point.y]).definirObjetCase(new Magasin1());
		}
		//enlever celles avec pieces aleatoirement
		for( int i = 0; i < intNbPieces; i++ )
		{
			point = (Point)listePointsCaseLibre.remove(ClassesUtilitaires.UtilitaireNombres.genererNbAleatoire( listePointsCaseLibre.size() ) );
			//generer case couleur avec magasin
			((CaseCouleur) objPlateauJeu[point.x][point.y]).definirObjetCase(new Piece( 10 ));
		}
		//enlever celles avec objets aleatoirement
		for( int i = 0; i < intNbObjetsUtilisables; i++ )
		{
			point = (Point)listePointsCaseLibre.remove(ClassesUtilitaires.UtilitaireNombres.genererNbAleatoire( listePointsCaseLibre.size() ) );
			//generer case couleur avec magasin
			((CaseCouleur) objPlateauJeu[point.x][point.y]).definirObjetCase(new Reponse( 1, true ));
		}
		
    	return objPlateauJeu;
    }

    /**
     * Cette fonction permet de g�n�rer la position des joueurs. Chaque joueur 
     * est g�n�r� sur une case vide.
     * 
     * @param int nbJoueurs : Le nombre de joueurs dont g�n�rer la position
     * @param Vector listePointsCaseLibre : La liste des points des cases libres
     * @return Point[] : Un tableau de points pour chaque joueur 
     */
    public static Point[] genererPositionJoueurs(int nbJoueurs, Vector listePointsCaseLibre)
    {
		// Cr�er un tableau contenant les nbJoueurs points
		Point[] objtPositionJoueurs = new Point[nbJoueurs];
		
		// Cr�ation d'un objet permettant de g�n�rer des nombres al�atoires
		Random objRandom = new Random();
		
		// Pour tous les joueurs de la partie, on va g�n�rer des positions de joueurs
		for (int i = 0; i < nbJoueurs; i++)
		{
			// Obtenir un point al�atoirement
			objtPositionJoueurs[i] = (Point) listePointsCaseLibre.remove(objRandom.nextInt(listePointsCaseLibre.size()));
		}
		
		return objtPositionJoueurs;
    }
}