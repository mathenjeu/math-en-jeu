package ClassesUtilitaires;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import Enumerations.Visibilite;
import ServeurJeu.BD.GestionnaireBD;
import ServeurJeu.ComposantesJeu.Table;
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
 * @author Jean-François Brind'Amour
 */
public final class GenerateurPartie 
{
	
	
	/**
	 * Constructeur par défaut est privé pour empêcher de pourvoir créer des 
	 * instances de cette classe.
	 */
    private GenerateurPartie() {}

    /**
     * Cette fonction permet de retourner une matrice à deux dimensions
     * représentant le plateau de jeu qui contient les informations sur 
     * chaque case selon des paramètres.
     * @param objGestionnaireBD 
     * @param max_nb_players 
     * @param lstPointsFinish 
     * @param nbColumns 
     * @param nbLines 
     *
     * @param Regles reglesPartie : L'ensemble des règles pour la partie
     * @param int temps : Le temps de la partie
     * @param Vector listePointsCaseLibre : La liste des points des cases 
     * 										libres (paramètre de sortie)
     * @return Case[][] : Un tableau à deux dimensions contenant l'information
     * 					  sur chaque case.
     * @throws NullPointerException : Si la liste passée en paramètre qui doit 
     * 								  être remplie est nulle
     */
    public static Case[][] genererPlateauJeu(GestionnaireBD objGestionnaireBD, 
    		ArrayList<Point> lstPointsCaseLibre, Integer objDernierIdObjets, ArrayList<Point> lstPointsFinish, 
    		Table table) throws NullPointerException
    {
		// Création d'un objet permettant de générer des nombres aléatoires
		Random objRandom = new Random();
        		
		// Déclaration de points
		Point objPoint;
		
		// Déclaration d'une liste de points contenant les points qui ont 
		// été passés
		ArrayList<Point> lstPointsCasesPresentes = new ArrayList<Point>();

		// Déclaration d'une liste de points contenant les points qui 
		// contiennent des cases spéciales
		ArrayList<Point> lstPointsCasesSpeciales = new ArrayList<Point>();
		
		// Déclaration d'une liste de points contenant les points qui 
		// contiennent des cases de couleur
		ArrayList<Point> lstPointsCasesCouleur = new ArrayList<Point>();
		
		// Déclaration d'une liste de points contenant les points qui 
		// contiennent des magasins
		ArrayList<Point> lstPointsMagasins = new ArrayList<Point>();
		
		// Déclaration d'une liste de points contenant les points qui 
		// contiennent des pièces
		ArrayList<Point> lstPointsPieces = new ArrayList<Point>();
		
		// Déclaration d'une liste de points contenant les points qui 
		// contiennent des objets utilisables
		ArrayList<Point> lstPointsObjetsUtilisables = new ArrayList<Point>();
		
		// Déclaration d'une liste de points contenant les points de start
		ArrayList<Point> lstPointsStart = new ArrayList<Point>();
		
		// Déclaration d'une liste de points contenant les points de finish
		ArrayList<Point> lstPointsEnd = new ArrayList<Point>();
		
				
		// Déclaration d'un compteur de cases
		int intCompteurCases = 0;

        // Déclaration d'un compteur des id des objets
        int intCompteurIdObjet = 1;
        
		// Déclaration d'une case dont le type est -1 (ça n'existe pas) qui
		// va nous servir pour identifier les cases qui ont été passées
		CaseCouleur objCaseParcourue = new CaseCouleur(1);
		//System.out.println(objCaseParcourue.obtenirTypeCase());
			
		//int factor = objRandom.nextInt((int) Math.ceil(temps /4)) + (int) Math.ceil(temps /5);
		//if(factor % 2 == 0)
		//   factor = factor + 1;
        
		int intNbColumns;
		int intNbLines;
		Regles reglesPartie = table.getObjSalle().getRegles();
				
		// Obtention du nombre d'objets maximal en vente par magasin
        //int maxNbObjetsAVendre = reglesPartie.getIntMaxSaledObjects();
        
		int temps = table.obtenirTempsTotal();
		
		// calculate number of lines and of columns if gametype = tournament
		if (table.getObjSalle().getGameType().equals("Tournament"))
		{
		   intNbColumns = (reglesPartie.getNbTracks() + 1) * (reglesPartie.obtenirTempsMinimal() * 2 + 1) - 1; // factor - 1;

    	   intNbLines = temps + reglesPartie.obtenirTempsMaximal();     	   //(int) Math.ceil((temps * temps * 3/2 ) / intNbColumns);
		
    	   if (intNbLines < 8)
    		   intNbLines = 8;
		}else { //if gametype = mathenjeu
		
			// Modifier le temps pour qu'il soit au moins le minimum de minutes
			temps = Math.max(temps, reglesPartie.obtenirTempsMinimal());
			
			// Modifier le temps pour qu'il soit au plus le maximum de minutes
			temps = Math.min(temps, reglesPartie.obtenirTempsMaximal());
			
			//to have a more equilibrate dimension of game table
			temps = (int) Math.ceil(temps * 5 / 10) + 10;
			
			// Le nombre de lignes sera de ceiling(temps / 2) à temps
		   intNbLines = objRandom.nextInt(temps - ((int) Math.ceil(temps /2)) + 1) + ((int) Math.ceil(temps /2 ));

		   // Le nombre de colonnes sera de temps à 2 * temps 
		   intNbColumns = (int) Math.floor((temps * temps ) / intNbLines); 
		}
		
		if(table.getNbLines() != 0)
			intNbLines = table.getNbLines();
		if(table.getNbColumns() != 0)
			intNbColumns = table.getNbColumns();
				
		// Déclaration de variables qui vont garder le nombre de trous, 
		// le nombre de cases spéciales, le nombres de magasins,
		// le nombre de pièces, le nombre de
		int intNbTrous = ((int) Math.floor(intNbLines * intNbColumns * reglesPartie.obtenirRatioTrous()));
		int intNbCasesSpeciales = (int) Math.floor(intNbLines * intNbColumns * reglesPartie.obtenirRatioCasesSpeciales());
		int intNbMagasins = (int) Math.floor(intNbLines * intNbColumns * reglesPartie.obtenirRatioMagasins());
		int intNbPieces = (int) Math.floor(intNbLines * intNbColumns * reglesPartie.obtenirRatioPieces());
		int intNbObjetsUtilisables = (int) Math.floor(intNbLines * intNbColumns * reglesPartie.obtenirRatioObjetsUtilisables());

		System.out.println("lignes : " + temps);
		System.out.println("lignes : " + intNbLines);
		System.out.println("colognes : " + intNbColumns);
		
		// to set the correct values on the table
		table.setNbLines(intNbLines);
		table.setNbColumns(intNbColumns);
				
		// Maintenant qu'on a le nombre de lignes et de colonnes, on va créer
		// le tableau à 2 dimensions représentant le plateau de jeu (null est 
		// mis par défaut dans chaque élément)
		Case[][] objttPlateauJeu = new Case[intNbLines][intNbColumns];	
		
		// if game type = tournament (plateau semilineaire)
		if (table.getObjSalle().getGameType().equals("Tournament"))
		{
		   // we build table of game with the houls for the borders
		   intCompteurCases = boardCreation(objRandom, reglesPartie.getNbTracks(), reglesPartie, lstPointsCasesPresentes, 
				   intCompteurCases, objCaseParcourue, intNbColumns, intNbLines, 
				   intNbTrous ,objttPlateauJeu);
		   // fill list with points for finish
			for(int i = 0; i < reglesPartie.getNbTracks(); i++)
			{
				objPoint = (Point) lstPointsCasesPresentes.remove(lstPointsCasesPresentes.size()-1);
				lstPointsFinish.add(objPoint);
			}
			lstPointsCasesPresentes = caseDefinition(reglesPartie, reglesPartie.getNbTracks(),
					objRandom, lstPointsCasesPresentes, lstPointsCasesSpeciales,
					lstPointsCasesCouleur, intCompteurCases, intNbCasesSpeciales,
					objttPlateauJeu);
			
		//else if gametype = mathenjeu			
		}else{ 
			
		   intCompteurCases = boardCreation(objRandom, lstPointsCasesPresentes,
				intCompteurCases, objCaseParcourue, intNbColumns, intNbLines,
				intNbTrous, objttPlateauJeu); 
		   lstPointsCasesPresentes = caseDefinition(reglesPartie, 
					objRandom, lstPointsCasesPresentes, lstPointsCasesSpeciales,
					lstPointsCasesCouleur, intCompteurCases, intNbCasesSpeciales,
					objttPlateauJeu, intNbLines, intNbColumns, intNbTrous);
		}
		
		
		
		
		
		// Si on doit afficher des magasins dans le plateau de jeu, 
		// alors on fait le code suivant
		if (reglesPartie.obtenirListeMagasinsPossibles().size() > 0)
		{
			// Réinitialiser le compteur de cases
			intCompteurCases = 1;
			
			// Obtenir un itérateur pour la liste des règles de magasins
			// triées par priorité (c'est certain que la première fois il y a au 
			// moins une règle de case)
			Iterator objIterateurListePriorite = reglesPartie.obtenirListeMagasinsPossibles().iterator();
			
			// On va choisir des magasins en commençant par la case la plus 
			// prioritaire et on va faire ça tant qu'on n'a pas atteint le 
			// pourcentage de magasins devant se trouver sur le plateau 
			// de jeu. Si on atteint la fin de la liste de magasins, on 
			// recommence depuis le début
			while (intCompteurCases <= intNbMagasins)
			{
				// Faire la référence vers la règle du magasin courant 
				ReglesMagasin objReglesMagasin = (ReglesMagasin) objIterateurListePriorite.next();
				
				// Obtenir un point aléatoirement parmi les points restants
				// qui n'ont pas de magasins et enlever en même temps ce point 
				// de la liste
				objPoint = (Point) lstPointsCasesPresentes.remove(objRandom.nextInt(lstPointsCasesPresentes.size()));
				
				// Ajouter le point trouvé dans la liste des points de magasins 
				// trouvés
				lstPointsMagasins.add(objPoint);

				// Si le nom du magasin est Magasin1, alors on met un objet 
				// Magasin(1) sur la case, sinon on fait le même genre de 
				// vérifications pour les autres types de magasins                                
				if (objReglesMagasin.obtenirNomMagasin().equals("Magasin1") || 
						objReglesMagasin.obtenirNomMagasin().equals("Shop1"))
				{
					// Définir la valeur de la case au point spécifié à la case 
					// d'identification
					((CaseCouleur) objttPlateauJeu[objPoint.x][objPoint.y]).definirObjetCase(new Magasin1());
				}
				else if (objReglesMagasin.obtenirNomMagasin().equals("Magasin2") || 
						objReglesMagasin.obtenirNomMagasin().equals("Shop2"))
				{
					// Définir la valeur de la case au point spécifié à la case 
					// d'identification
					((CaseCouleur) objttPlateauJeu[objPoint.x][objPoint.y]).definirObjetCase(new Magasin2());
				}else if (objReglesMagasin.obtenirNomMagasin().equals("Magasin3") || 
						objReglesMagasin.obtenirNomMagasin().equals("Shop3"))
				{
					// Définir la valeur de la case au point spécifié à la case 
					// d'identification
					((CaseCouleur) objttPlateauJeu[objPoint.x][objPoint.y]).definirObjetCase(new Magasin3());
				}else if (objReglesMagasin.obtenirNomMagasin().equals("Magasin4"))
				{
					// Définir la valeur de la case au point spécifié à la case 
					// d'identification
					((CaseCouleur) objttPlateauJeu[objPoint.x][objPoint.y]).definirObjetCase(new Magasin4());
				}
				
				// Aller chercher une référence vers le magasin que l'on vient de créer
				Magasin objMagasin = (Magasin)((CaseCouleur) objttPlateauJeu[objPoint.x][objPoint.y]).obtenirObjetCase();

				
				// Get the list of items to be sold by shops
				ArrayList<String> listObjects = new ArrayList<String>();
				objGestionnaireBD.fillShopObjects(objReglesMagasin.obtenirNomMagasin(), listObjects);
				
				for(String nomDeLObjet : listObjects)
				{
                   	// Incrémenter le compteur de ID pour les objets
                    intCompteurIdObjet++;

                    
                    // On crée un nouvel objet du type correspondant
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
				
				// Incrémenter le nombre de cases passées
				intCompteurCases++;
				
				// Si on est arrivé à la fin de la liste, alors il faut 
				// retourner au début
				if (objIterateurListePriorite.hasNext() == false)
				{
					// Obtenir un autre itérateur pour la liste
					objIterateurListePriorite = reglesPartie.obtenirListeMagasinsPossibles().iterator();
				}
			}			
		}
		
		// Bloc de code qui va s'assurer de créer les pièces dans le plateau de jeu
		// Réinitialiser le compteur de cases
			intCompteurCases = 1;
			
			// On va choisir des pièces dont la valeur est aléatoire selon 
			// une loi normale centrée à 0 tant qu'on n'a pas atteint le 
			// nombre de pièces désiré
			while (intCompteurCases <= intNbPieces)
			{
				// Calculer la valeur de la pièce à créer de façon aléatoire 
				// selon une loi normale
				int intValeur = Math.max(Math.abs(ClassesUtilitaires.UtilitaireNombres.genererNbAleatoireLoiNormale(0.0d, Math.pow(((double) reglesPartie.obtenirValeurPieceMaximale()) / 3.0d, 2.0d))), 1);
					
				// Obtenir un point aléatoirement parmi les points restants
				// qui n'ont pas de pièces et enlever en même temps ce point 
				// de la liste
				objPoint = (Point) lstPointsCasesPresentes.remove(objRandom.nextInt(lstPointsCasesPresentes.size()));
				
				// Ajouter le point trouvé dans la liste des points de pièces 
				// trouvées
				lstPointsPieces.add(objPoint);
				
				// Définir la valeur de la case au point spécifié à la case 
				// d'identification
				((CaseCouleur) objttPlateauJeu[objPoint.x][objPoint.y]).definirObjetCase(new Piece(intValeur, 1));				
				
				// Incrémenter le nombre de cases passées
				intCompteurCases++;
			}			
		
			
		
		// Si on doit afficher des objets utilisables dans le plateau de jeu, 
		// alors on fait le code suivant
		if (reglesPartie.obtenirListeObjetsUtilisablesPossibles().size() > 0)
		{
			// Réinitialiser le compteur de cases
			intCompteurCases = 1;
			
			// Obtenir un itérateur pour la liste des règles d'objets utilisables
			// triés par priorité (c'est certain que la première fois il y a au 
			// moins une règle de case)
			Iterator objIterateurListePriorite = reglesPartie.obtenirListeObjetsUtilisablesPossibles().iterator();
			
			// On va choisir des objets utilisables en commençant par 
			// l'objet le plus prioritaire et on va faire ça tant qu'on n'a 
			// pas atteint le pourcentage d'objets utilisables devant se 
			// trouver sur le plateau de jeu. Si on atteint la fin de la 
			// liste d'objets utilisables, on recommence depuis le début
			while (intCompteurCases <= intNbObjetsUtilisables)
			{
				// Faire la référence vers la règle de l'objet utilisable 
				// courant
				ReglesObjetUtilisable objReglesObjetUtilisable = (ReglesObjetUtilisable) objIterateurListePriorite.next();
				
				// Déclaration d'une variable qui va permettre de savoir si 
				// l'objet doit être visible ou non
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
				// Si l'objet doit avoir une visibilité aléatoire, alors 
				// on va générer un nombre aléatoire qui va donner soit true 
				// soit false
				else
				{
					bolEstVisible = objRandom.nextBoolean();
				}
				
				// Obtenir un point aléatoirement parmi les points restants
				// qui n'ont pas d'objets utilisables et enlever en même temps 
				// ce point de la liste
				objPoint = (Point) lstPointsCasesPresentes.remove(objRandom.nextInt(lstPointsCasesPresentes.size()));
				
				// Ajouter le point trouvé dans la liste des points d'objets 
				// utilisables trouvés
				lstPointsObjetsUtilisables.add(objPoint);

				// Si le nom de l'objet est Livre, alors on met un objet 
				// Livre sur la case, sinon on fait le même genre de 
				// vérifications pour les autres types de magasins
                                // On définit la valeur de la case au point spécifié à la case d'identification
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
				
				// Incrémenter le nombre de cases passées
				intCompteurCases++;
				
				// Incrémenter le compteur des id des objets
				intCompteurIdObjet++;
				
				// Si on est arrivé à la fin de la liste, alors il faut 
				// retourner au début
				if (objIterateurListePriorite.hasNext() == false)
				{
					// Obtenir un autre itérateur pour la liste
					objIterateurListePriorite = reglesPartie.obtenirListeObjetsUtilisablesPossibles().iterator();
				}
			}			
		}
		
		// Ajouter les points restants dans la liste des points représentant 
		// les cases sans objets et n'étant pas des cases spéciales
		 lstPointsCaseLibre.addAll(lstPointsCasesPresentes);
		
		// Indiquer quel a été le dernier id des objets
		objDernierIdObjets = intCompteurIdObjet;
		
		// if game type = tournament (plateau semilineaire)
		// add the start and end points 
		if (table.getObjSalle().getGameType().equals("Tournament"))
		{
            Iterator objIterateurListePriorite = reglesPartie.obtenirListeCasesCouleurPossibles().iterator();
			ReglesCaseCouleur objReglesCaseCouleur = (ReglesCaseCouleur) objIterateurListePriorite.next();				
			for (int i = 0; i < reglesPartie.getNbTracks(); i++)
			{

				objPoint = new Point(0,i);
				objttPlateauJeu[objPoint.x][objPoint.y] = new CaseCouleur(objReglesCaseCouleur.obtenirTypeCase());
				lstPointsCaseLibre.add(objPoint);
			}
			
			for (int i = 0; i < reglesPartie.getNbTracks(); i++)
			{
				
				objPoint = new Point(intNbLines - 1,intNbColumns - i - 1);
				objttPlateauJeu[objPoint.x][objPoint.y] = new CaseCouleur(objReglesCaseCouleur.obtenirTypeCase());
				lstPointsCaseLibre.add(objPoint);
			}
		}
		
		return objttPlateauJeu;
    }

    /**
     * Method for "mathenjeu" game board
     * @param reglesPartie
     * @param objRandom
     * @param lstPointsCasesPresentes
     * @param lstPointsCasesSpeciales
     * @param lstPointsCasesCouleur
     * @param intCompteurCases
     * @param intNbCasesSpeciales
     * @param objttPlateauJeu
     * @param intNbLignes
     * @param intNbColonnes
     * @param intNbTrous
     * @return
     */
	private static ArrayList<Point> caseDefinition(Regles reglesPartie, 
			Random objRandom, ArrayList<Point> lstPointsCasesPresentes,
			ArrayList<Point> lstPointsCasesSpeciales, 
			ArrayList<Point> lstPointsCasesCouleur, int intCompteurCases,
			int intNbCasesSpeciales, Case[][] objttPlateauJeu, int intNbLignes, int intNbColonnes, int intNbTrous) {
		Point objPoint;
		// Si on doit afficher des cases spéciales dans le plateau de jeu, 
		// alors on fait le code suivant
		if (reglesPartie.obtenirListeCasesSpecialesPossibles().size() > 0)
		{
			// Réinitialiser le compteur de cases
			intCompteurCases = 1;
			
			// Obtenir un itérateur pour la liste des règles de cases spéciales
			// triées par priorité (c'est certain que la première fois il y a au 
			// moins une règle de case)
			Iterator objIterateurListePriorite = reglesPartie.obtenirListeCasesSpecialesPossibles().iterator();
			
			// On va choisir des cases spéciales en commençant par la case
			// la plus prioritaire et on va faire ça tant qu'on n'a pas atteint 
			// le pourcentage de cases spéciales devant se trouver sur le plateau 
			// de jeu. Si on atteint la fin de la liste de cases spéciales, on 
			// recommence depuis le début
			while (intCompteurCases <= intNbCasesSpeciales)
			{
				// Faire la référence vers la règle de la case spéciale 
				// courante
				ReglesCaseSpeciale objReglesCaseSpeciale = (ReglesCaseSpeciale) objIterateurListePriorite.next();
				
				// Obtenir un point aléatoirement parmi les points restants
				// qui n'ont pas de cases spéciales et enlever en même temps 
				// ce point de la liste
				objPoint = (Point) lstPointsCasesPresentes.remove(objRandom.nextInt(lstPointsCasesPresentes.size()));
				
				// Ajouter le point trouvé dans la liste des points de cases 
				// spéciales trouvées
				lstPointsCasesSpeciales.add(objPoint);

				// Définir la valeur de la case au point spécifié à la case 
				// d'identification
				objttPlateauJeu[objPoint.x][objPoint.y] = new CaseSpeciale(objReglesCaseSpeciale.obtenirTypeCase());				
				
				// Incrémenter le nombre de cases passées
				intCompteurCases++;
				
				// Si on est arrivé à la fin de la liste, alors il faut 
				// retourner au début
				if (objIterateurListePriorite.hasNext() == false)
				{
					// Obtenir un autre itérateur pour la liste
					objIterateurListePriorite = reglesPartie.obtenirListeCasesSpecialesPossibles().iterator();
				}
			}			
		}
		
		// Bloc de code qui va s'assurer de créer les cases de couleur dans le 
		// plateau de jeu (il y en a au moins un type)
		{
			// Réinitialiser le compteur de cases
			intCompteurCases = 1;
			
			// Obtenir un itérateur pour la liste des règles de cases de couleur
			// triées par priorité (c'est certain que la première fois il y a au 
			// moins une règle de case)
			Iterator objIterateurListePriorite = reglesPartie.obtenirListeCasesCouleurPossibles().iterator();
			
			// On va choisir des cases de couleur en commençant par la case
			// la plus prioritaire et on va faire ça tant qu'on n'a pas atteint 
			// le pourcentage de cases spéciales devant se trouver sur le plateau 
			// de jeu. Si on atteint la fin de la liste de cases de couleur, on 
			// recommence depuis le début
			while (intCompteurCases <= (intNbLignes * intNbColonnes) - intNbTrous - intNbCasesSpeciales)
			{
				// Faire la référence vers la règle de la case de couleur 
				// courante
				ReglesCaseCouleur objReglesCaseCouleur = (ReglesCaseCouleur) objIterateurListePriorite.next();
				
				// Obtenir un point aléatoirement parmi les points restants
				// qui n'ont pas de cases spéciales et de cases de couleur 
				// et enlever en même temps ce point de la liste
				objPoint = (Point) lstPointsCasesPresentes.remove(objRandom.nextInt(lstPointsCasesPresentes.size()));
				
				// Ajouter le point trouvé dans la liste des points de cases 
				// de couleur trouvées
				lstPointsCasesCouleur.add(objPoint);

				// Définir la valeur de la case au point spécifié à la case 
				// d'identification
				objttPlateauJeu[objPoint.x][objPoint.y] = new CaseCouleur(objReglesCaseCouleur.obtenirTypeCase());				
				
				// Incrémenter le nombre de cases passées
				intCompteurCases++;
				
				// Si on est arrivé à la fin de la liste, alors il faut 
				// retourner au début
				if (objIterateurListePriorite.hasNext() == false)
				{
					// Obtenir un autre itérateur pour la liste
					objIterateurListePriorite = reglesPartie.obtenirListeCasesCouleurPossibles().iterator();
				}
			}
			
			// La liste des cases présentes est maintenant la liste des cases 
			// de couleur, car tous les points de la liste ont été copiés dans 
			// l'autre liste
			lstPointsCasesPresentes = lstPointsCasesCouleur;
			lstPointsCasesCouleur = null;
		}
		return  lstPointsCasesPresentes;
	}

	/**
	 * Method for the "Tournament" game board
	 * @param reglesPartie
	 * @param max_nb_players
	 * @param objRandom
	 * @param lstPointsCasesPresentes
	 * @param lstPointsCasesSpeciales
	 * @param lstPointsCasesCouleur
	 * @param intCompteurCases
	 * @param intNbCasesSpeciales
	 * @param objttPlateauJeu
	 * @return
	 */
	private static ArrayList<Point> caseDefinition(Regles reglesPartie,
			int max_nb_players, Random objRandom,
			ArrayList<Point> lstPointsCasesPresentes,
			ArrayList<Point> lstPointsCasesSpeciales,
			ArrayList<Point> lstPointsCasesCouleur, int intCompteurCases,
			int intNbCasesSpeciales, Case[][] objttPlateauJeu) {
		
		Point objPoint;
		
		// Si on doit afficher des cases spéciales dans le plateau de jeu, 
		// alors on fait le code suivant
		if (reglesPartie.obtenirListeCasesSpecialesPossibles().size() > 0)
		{
			// Réinitialiser le compteur de cases
			int intCompteurCasesSpeciale = 0;
			
			// Obtenir un itérateur pour la liste des règles de cases spéciales
			// triées par priorité (c'est certain que la première fois il y a au 
			// moins une règle de case)
			Iterator objIterateurListePriorite = reglesPartie.obtenirListeCasesSpecialesPossibles().iterator();
			
			// On va choisir des cases spéciales en commençant par la case
			// la plus prioritaire et on va faire ça tant qu'on n'a pas atteint 
			// le pourcentage de cases spéciales devant se trouver sur le plateau 
			// de jeu. Si on atteint la fin de la liste de cases spéciales, on 
			// recommence depuis le début
			while (intCompteurCasesSpeciale < intNbCasesSpeciales)
			{
				// Faire la référence vers la règle de la case spéciale 
				// courante
				ReglesCaseSpeciale objReglesCaseSpeciale = (ReglesCaseSpeciale) objIterateurListePriorite.next();
				
				// Obtenir un point aléatoirement parmi les points restants
				// qui n'ont pas de cases spéciales et enlever en même temps 
				// ce point de la liste
				objPoint = (Point) lstPointsCasesPresentes.remove(objRandom.nextInt(lstPointsCasesPresentes.size()));
				
				// Ajouter le point trouvé dans la liste des points de cases 
				// spéciales trouvées
				lstPointsCasesSpeciales.add(objPoint);

				// Définir la valeur de la case au point spécifié à la case 
				// d'identification
				objttPlateauJeu[objPoint.x][objPoint.y] = new CaseSpeciale(objReglesCaseSpeciale.obtenirTypeCase());				
				
				// Incrémenter le nombre de cases passées
				intCompteurCasesSpeciale++;
				
				// Si on est arrivé à la fin de la liste, alors il faut 
				// retourner au début
				if (objIterateurListePriorite.hasNext() == false)
				{
					// Obtenir un autre itérateur pour la liste
					objIterateurListePriorite = reglesPartie.obtenirListeCasesSpecialesPossibles().iterator();
				}
			}			
		}
		
		// Bloc de code qui va s'assurer de créer les cases de couleur dans le 
		// plateau de jeu (il y en a au moins un type)
		{
			// Réinitialiser le compteur de cases
			int intCompteurCasesCouleur = 0;
			
			// Obtenir un itérateur pour la liste des règles de cases de couleur
			// triées par priorité (c'est certain que la première fois il y a au 
			// moins une règle de case)
			Iterator objIterateurListePriorite = reglesPartie.obtenirListeCasesCouleurPossibles().iterator();
			
			// On va choisir des cases de couleur en commençant par la case
			// la plus prioritaire et on va faire ça tant qu'on n'a pas atteint 
			// le pourcentage de cases spéciales devant se trouver sur le plateau 
			// de jeu. Si on atteint la fin de la liste de cases de couleur, on 
			// recommence depuis le début
						
			while (intCompteurCasesCouleur < intCompteurCases - intNbCasesSpeciales - reglesPartie.getNbTracks()*3)
			{
				
				// Faire la référence vers la règle de la case de couleur 
				// courante
				ReglesCaseCouleur objReglesCaseCouleur = (ReglesCaseCouleur) objIterateurListePriorite.next();
				
				// Obtenir un point aléatoirement parmi les points restants
				// qui n'ont pas de cases spéciales et de cases de couleur 
				// et enlever en même temps ce point de la liste
				objPoint = (Point) lstPointsCasesPresentes.remove(objRandom.nextInt(lstPointsCasesPresentes.size()));
				
				// Ajouter le point trouvé dans la liste des points de cases 
				// de couleur trouvées
				lstPointsCasesCouleur.add(objPoint);

				// Définir la valeur de la case au point spécifié à la case 
				// d'identification
				objttPlateauJeu[objPoint.x][objPoint.y] = new CaseCouleur(objReglesCaseCouleur.obtenirTypeCase());				
				
				// Incrémenter le nombre de cases passées
				intCompteurCasesCouleur++;
				
				// Si on est arrivé à la fin de la liste, alors il faut 
				// retourner au début
				if (objIterateurListePriorite.hasNext() == false)
				{
					// Obtenir un autre itérateur pour la liste
					objIterateurListePriorite = reglesPartie.obtenirListeCasesCouleurPossibles().iterator();
				}
			}
			
			// La liste des cases présentes est maintenant la liste des cases 
			// de couleur, car tous les points de la liste ont été copiés dans 
			// l'autre liste
			lstPointsCasesPresentes = lstPointsCasesCouleur;
			lstPointsCasesCouleur = null;
		}
		return lstPointsCasesPresentes;
	}

    /**
     * Method used to create the game board for the game type "Tournament"
     * @param nbTracks
     * @param lstPointsCasesPresentes
     * @param intCompteurCases
     * @param objCaseParcourue
     * @param intNbColonnes
     * @param intNbLignes
     * @param intNbTrous 
     * @param objttPlateauJeu
     * @return
     */
	private static int boardCreation(Random objRandom, int nbTracks, Regles reglesPartie,
			ArrayList<Point> lstPointsCasesPresentes, int intCompteurCases,
			CaseCouleur objCaseParcourue, int intNbColonnes, int intNbLignes,
			int intNbTrous, Case[][] objttPlateauJeu) {
		
		Point objPoint;
		for(int x = 0; x < intNbLignes; x++){
			for(int y = 0; y < intNbColonnes; y++){
			
				if(ifNotBorder(x, y, intNbLignes, intNbColonnes, nbTracks)){
					// Créer le point de la case courante
					objPoint = new Point(x, y);
					
					// Définir la valeur de la case au point spécifié à la case 
					// d'identification
					objttPlateauJeu[objPoint.x][objPoint.y] = objCaseParcourue;
					
					// Ajouter le point dans la liste des points passés
					lstPointsCasesPresentes.add(objPoint);
					intCompteurCases++;
				}
				
			}
		}
		
		// random holes
		int intCompteur = 0;
		while (intCompteur < intNbTrous)
		{
			
		      objPoint = (Point) lstPointsCasesPresentes.remove(objRandom.nextInt(lstPointsCasesPresentes.size()));
		      objttPlateauJeu[objPoint.x][objPoint.y] = null;
		      
		      intCompteurCases--;
		    
		    if(  objPoint.x % 3 == 0 || objPoint.y % 3 == 0 || objPoint.y == intNbColonnes -1 || objPoint.y == 0 || (objPoint.y < nbTracks && objPoint.x < 6)){     // objPoint.x == 0|| objPoint.x == intNbLignes - 1 ||
		    	objCaseParcourue = new CaseCouleur(1);
		    	objttPlateauJeu[objPoint.x][objPoint.y] = objCaseParcourue;
		     	
		    	lstPointsCasesPresentes.add(objPoint);
		        intCompteurCases++;
		        intCompteur--;
		    }
		    intCompteur++;
		} // end while	
		
		for (int i = 0; i < reglesPartie.getNbTracks(); i++)
		{
			objPoint = new Point(0,i);
			lstPointsCasesPresentes.remove(objPoint);
		}
		for (int i = 0; i < reglesPartie.getNbTracks(); i++)
		{
			objPoint = new Point(intNbLignes - 1 ,intNbColonnes - i - 1);
			lstPointsCasesPresentes.remove(objPoint);
		}
		
		
		
		return intCompteurCases;
	}// end method

	/**
	 * Method used to create the game board for the game type "mathEnJeu"
	 * @param objRandom
	 * @param lstPointsCasesPresentes
	 * @param intCompteurCases
	 * @param objCaseParcourue
	 * @param intNbColonnes
	 * @param intNbLignes
	 * @param intNbTrous
	 * @param objttPlateauJeu
	 * @return
	 */
	private static int boardCreation(Random objRandom,
			ArrayList<Point> lstPointsCasesPresentes, int intCompteurCases,
			CaseCouleur objCaseParcourue, int intNbColonnes, int intNbLignes,
			int intNbTrous, Case[][] objttPlateauJeu) {
		Point objPoint;
        Point objPointFile;
		
		// Déclaration d'une file qui va contenir des points
		ArrayList<Point> lstFile = new ArrayList<Point>();
		
		// Trouver un point aléatoire dans le plateau de jeu et le garder 
		// en mémoire (ça va être le point de départ) 
		objPoint = new Point(objRandom.nextInt(intNbLignes), objRandom.nextInt(intNbColonnes));
		
		// Au point calculé, on va définir la case spéciale qui sert 
		// d'identification et dont le type est -1
		objttPlateauJeu[objPoint.x][objPoint.y] = objCaseParcourue;
		
		// Ajouter le point dans la file de priorité et dans la liste des 
		// points passés
		lstFile.add(objPoint);
		lstPointsCasesPresentes.add(objPoint);
		
		// On choisi des cases dans le plateau en leur mettant la case spéciale 
		// d'identification comme valeur tant qu'on ne passe pas un certain 
		// pourcentage du nombre de cases totales possibles
		while (intCompteurCases <= (intNbLignes * intNbColonnes) - intNbTrous)
		{
			// S'il y a au moins un point dans la file, alors on va retirer 
			// ce point et le traiter, sinon il faut prendre un point 
			// aléatoirement parmis les points qui ont été passés
			if (lstFile.size() > 0)
			{
				// Prendre le point au début de la file
				objPointFile = (Point) lstFile.remove(0);
			}
			else
			{
				// Obtenir un point aléatoirement parmi les points qui ont 
				// déjà été passés
				objPointFile = (Point) lstPointsCasesPresentes.get(objRandom.nextInt(lstPointsCasesPresentes.size()));
			}
			
					
			// S'il y a une case à gauche, et que cette case n'a pas encore été
			// passée et que une valeur aléatoire retourne true, alors on va
			// choisir cette case
			
			if ((objPointFile.x - 1 >= 0) && 
				(objttPlateauJeu[objPointFile.x - 1][objPointFile.y] == null) &&
				(objRandom.nextBoolean() == true))
			{
				// Créer le point à gauche de la case courante
				objPoint = new Point(objPointFile.x - 1, objPointFile.y);
				
				// Définir la valeur de la case au point spécifié à la case 
				// d'identification
				objttPlateauJeu[objPoint.x][objPoint.y] = objCaseParcourue;
				
				// Ajouter le point dans la file de priorité et dans la liste des 
				// points passés
				lstFile.add(objPoint);
				lstPointsCasesPresentes.add(objPoint);
				
				// Incrémenter le nombre de points passés
				intCompteurCases++;
			}
			
			// S'il y a une case à droite, et que cette case n'a pas encore été
			// passée et que une valeur aléatoire retourne true, alors on va
			// choisir cette case
			if ((objPointFile.x + 1 < intNbLignes) && 
				(objttPlateauJeu[objPointFile.x + 1][objPointFile.y] == null) && 
				(objRandom.nextBoolean() == true))
			{
				// Créer le point à droite de la case courante
				objPoint = new Point(objPointFile.x + 1, objPointFile.y);
				
				// Définir la valeur de la case au point spécifié à la case 
				// d'identification
				objttPlateauJeu[objPoint.x][objPoint.y] = objCaseParcourue;
				
				// Ajouter le point dans la file de priorité et dans la liste des 
				// points passés
				lstFile.add(objPoint);
				lstPointsCasesPresentes.add(objPoint);
				
				// Incrémenter le nombre de points passés
				intCompteurCases++;
			}
			
			// S'il y a une case en haut, et que cette case n'a pas encore été
			// passée et que une valeur aléatoire retourne true, alors on va
			// choisir cette case
			if ((objPointFile.y - 1 >= 0) && 
				(objttPlateauJeu[objPointFile.x][objPointFile.y - 1] == null) && 
				(objRandom.nextBoolean() == true))
			{
				// Créer le point en haut de la case courante
				objPoint = new Point(objPointFile.x, objPointFile.y - 1);
				
				// Définir la valeur de la case au point spécifié à la case 
				// d'identification
				objttPlateauJeu[objPoint.x][objPoint.y] = objCaseParcourue;
				
				// Ajouter le point dans la file de priorité et dans la liste des 
				// points passés
				lstFile.add(objPoint);
				lstPointsCasesPresentes.add(objPoint);
				
				// Incrémenter le nombre de points passés
				intCompteurCases++;
			}
			
			// S'il y a une case en bas, et que cette case n'a pas encore été
			// passée et que une valeur aléatoire retourne true, alors on va
			// choisir cette case
			if ((objPointFile.y + 1 < intNbColonnes) && 
				(objttPlateauJeu[objPointFile.x][objPointFile.y + 1] == null) && 
				(objRandom.nextBoolean() == true))
			{
				// Créer le point en bas de la case courante
				objPoint = new Point(objPointFile.x, objPointFile.y + 1);
				
				// Définir la valeur de la case au point spécifié à la case 
				// d'identification
				objttPlateauJeu[objPoint.x][objPoint.y] = objCaseParcourue;
				
				// Ajouter le point dans la file de priorité et dans la liste des 
				// points passés
				lstFile.add(objPoint);
				lstPointsCasesPresentes.add(objPoint);
				
				// Incrémenter le nombre de points passés
				intCompteurCases++;
			}
		}
		return intCompteurCases;
	}

    /**
     * Method used to create the semilinear game board
     * @param x
     * @param y
     * @param intNbLignes
     * @param intNbColonnes
     * @param nbTracks 
     * @return boolean if the point will be or not used
     */
    private static boolean ifNotBorder(int x, int y, int intNbLignes, int intNbColonnes, int nbTracks) {
		
    	boolean notborder = true;
		if ( (nbTracks % 2 == 0) && (y + 1) % (nbTracks + 1) == 0 ){
			
			if(y % 2 == 0 && x <= intNbLignes - nbTracks - 1 )
		      notborder = false;
			else if (y % 2 == 1 && x > nbTracks - 1 )
				 notborder = false;
		
		}else if (nbTracks % 2 == 1 && (y + 1) % (nbTracks + 1) == 0 ){
		
			if((y + 1) / (nbTracks + 1) %  2 == 1 && x <= intNbLignes - nbTracks - 1 )
			      notborder = false;
			else if ((y + 1) / (nbTracks + 1) % 2 == 0 && x > nbTracks - 1 )
					 notborder = false;
		
		}
		
		return notborder;
	}

	/**
     * Cette fonction permet de générer la position des joueurs. Chaque joueur 
     * est généré sur une case vide.
     * 
     * @param int nbJoueurs : Le nombre de joueurs dont générer la position
     * @return Point[] : Un tableau de points pour chaque joueur 
     */
    public static Point[] genererPositionJoueurs(int nbJoueurs, int nbTracks)
    {
		// Créer un tableau contenant les nbJoueurs points
		Point[] objtPositionJoueurs = new Point[nbJoueurs];
		
		int i = 0;
				
		// Pour tous les joueurs de la partie, on va générer des positions de joueurs
		for (int row = 0; row < Math.ceil(nbJoueurs/nbTracks) + 1; row++)
		{
			for (int col = 0; col < nbTracks; col++)
			{
				if(i < nbJoueurs)
				{
					objtPositionJoueurs[i] = new Point(0,col);
					i++;
					//System.out.println("i" + i);
				}
				//System.out.println("col" + col);
			}
			//System.out.println("row" + row);
		}
		
		return objtPositionJoueurs;
    }
    
    /**
     * Cette fonction permet de générer la position des joueurs. Chaque joueur 
     * est généré sur une case vide.
     * 
     * @param int nbJoueurs : Le nombre de joueurs dont générer la position
     * @param Vector listePointsCaseLibre : La liste des points des cases libres
     * @return Point[] : Un tableau de points pour chaque joueur 
     */
    public static Point[] genererPositionJoueurs(int nbJoueurs, ArrayList<Point> lstPointsCaseLibre)
    {
    	// Créer un tableau contenant les nbJoueurs points
		Point[] objtPositionJoueurs = new Point[nbJoueurs];
		
		// Création d'un objet permettant de générer des nombres aléatoires
		Random objRandom = new Random();
		
		// Pour tous les joueurs de la partie, on va générer des positions de joueurs
		for (int i = 0; i < nbJoueurs; i++)
		{
			// Obtenir un point aléatoirement
			objtPositionJoueurs[i] = (Point) lstPointsCaseLibre.remove(objRandom.nextInt(lstPointsCaseLibre.size()));
		}
		
		return objtPositionJoueurs;
    }
}