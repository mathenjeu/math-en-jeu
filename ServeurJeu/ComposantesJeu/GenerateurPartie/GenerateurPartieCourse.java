package ServeurJeu.ComposantesJeu.GenerateurPartie;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import Enumerations.Visibilite;
import ServeurJeu.ComposantesJeu.Cases.Case;
import ServeurJeu.ComposantesJeu.Cases.CaseCouleur;
import ServeurJeu.ComposantesJeu.Cases.CaseSpeciale;
import ServeurJeu.ComposantesJeu.Objets.Magasins.Magasin;
import ServeurJeu.ComposantesJeu.Objets.Magasins.Magasin1;
import ServeurJeu.ComposantesJeu.Objets.Magasins.Magasin2;
import ServeurJeu.ComposantesJeu.Objets.Magasins.Magasin3;
import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.Banane;
import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.Boule;
import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.Brainiac;
import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.Livre;
import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.ObjetUtilisable;
import ServeurJeu.ComposantesJeu.Objets.Pieces.Piece;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesMagasin;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesObjetUtilisable;
import ServeurJeu.ComposantesJeu.Tables.Table;

/**
 * @author Oloieri Lilian 31.12.2009 
 */

public class GenerateurPartieCourse extends GenerateurPartie {

	public GenerateurPartieCourse() {
		super();
	}

	
	/**
     * Cette fonction permet de retourner une matrice à deux dimensions
     * représentant le plateau de jeu qui contient les informations sur 
     * chaque case selon des paramètres.
     * @param lstPointsFinish 
     * @param nbColumns 
     * @param nbLines 
     * @param Regles reglesPartie : L'ensemble des règles pour la partie
     * @param Vector listePointsCaseLibre : La liste des points des cases 
     * 										libres (paramètre de sortie)
     * @return Case[][] : Un tableau à deux dimensions contenant l'information
     * 					  sur chaque case.
     * @throws NullPointerException : Si la liste passée en paramètre qui doit 
     * 								  être remplie est nulle
     */
    public Case[][] genererPlateauJeu(ArrayList<Point> lstPointsCaseLibre, ArrayList<Point> lstPointsFinish, 
    		Table table) throws NullPointerException
    {
		
    	//*********** for all new game we need to have a null start point
    	this.lstPointsCasesPresentes = new LinkedList<Point>();
		this.lstPointsCasesSpeciales = new LinkedList<Point>();
		this.lstPointsCasesCouleur = new LinkedList<Point>();
		this.lstPointsMagasins = new LinkedList<Point>();
		this.lstPointsPieces = new LinkedList<Point>();
		this.lstPointsObjetsUtilisables = new LinkedList<Point>();
		this.intCompteurCases = 0;
		this.intCompteurIdObjet = 1;
		this.objCaseParcourue = new CaseCouleur(1);
    	
    	this.reglesPartie = table.getRegles();
		this.objGestionnaireBD = table.getObjGestionnaireBD();
		this.objSalle = table.getObjSalle();
    	    	
		int temps = table.obtenirTempsTotal();
		
		// calculate number of lines and of columns
		// holes - is number of holes between tracks
		int holes = 2;
		if(intNbColumns == 0)
	       intNbColumns = (reglesPartie.getNbTracks() + holes) * (reglesPartie.obtenirTempsMinimal() * 2 + 1) - holes; // factor - 1;
		if(intNbLines == 0)
   	       intNbLines = temps + reglesPartie.obtenirTempsMaximal();     	 
    	
   	    if (intNbLines < 8)
    		  intNbLines = 8;
								
		// Déclaration de variables qui vont garder le nombre de trous, 
		// le nombre de cases spéciales, le nombres de magasins,
		// le nombre de pièces, le nombre de
		int intNbTrous = ((int) Math.floor(intNbLines * intNbColumns * reglesPartie.obtenirRatioTrous()));
		int intNbCasesSpeciales = (int) Math.floor(intNbLines * intNbColumns * reglesPartie.obtenirRatioCasesSpeciales());
		int intNbMagasins = (int) Math.floor(intNbLines * intNbColumns * reglesPartie.obtenirRatioMagasins());
		int intNbPieces = (int) Math.floor(intNbLines * intNbColumns * reglesPartie.obtenirRatioPieces());
		int intNbObjetsUtilisables = (int) Math.floor(intNbLines * intNbColumns * reglesPartie.obtenirRatioObjetsUtilisables());
         
		//System.out.println("temps : " + temps);
		//System.out.println("lignes : " + intNbLines);
		//System.out.println("colognes : " + intNbColumns);
						
		// Maintenant qu'on a le nombre de lignes et de colonnes, on va créer
		// le tableau à 2 dimensions représentant le plateau de jeu (null est 
		// mis par défaut dans chaque élément)
		Case[][] objttPlateauJeu = new Case[intNbLines][intNbColumns];	
		
		// we build table of game with the houls for the borders
		this.boardCreation(intNbTrous ,objttPlateauJeu);
		// fill list with points for finish
		for(int i = 0; i < reglesPartie.getNbTracks(); i++)
		{
			//objPoint = (Point) lstPointsCasesPresentes.remove(lstPointsCasesPresentes.size()-1);
			objPoint = new Point(intNbLines - 1,intNbColumns - i - 1);
			lstPointsFinish.add(objPoint);
		}
		this.caseDefinition(intNbCasesSpeciales, objttPlateauJeu);
			
				
		// Si on doit afficher des magasins dans le plateau de jeu, 
		// alors on fait le code suivant
		if (reglesPartie.obtenirListeMagasinsPossibles().size() > 0)
		{
			// Réinitialiser le compteur de cases
			intCompteurCases = 1;
			
			// Obtenir un itérateur pour la liste des règles de magasins
			// triées par priorité (c'est certain que la première fois il y a au 
			// moins une règle de case)
			Iterator<ReglesMagasin> objIterateurListePriorite = reglesPartie.obtenirListeMagasinsPossibles().iterator();
			
			// On va choisir des magasins en commençant par la case la plus 
			// prioritaire et on va faire ça tant qu'on n'a pas atteint le 
			// pourcentage de magasins devant se trouver sur le plateau 
			// de jeu. Si on atteint la fin de la liste de magasins, on 
			// recommence depuis le début
			while (intCompteurCases <= intNbMagasins)
			{
				// Faire la référence vers la règle du magasin courant 
				ReglesMagasin objReglesMagasin = objIterateurListePriorite.next();
				
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
                    else if(nomDeLObjet.equals("Boule"))
                    {
                        Boule objAAjouter = new Boule(intCompteurIdObjet, true);
                        objMagasin.ajouterObjetUtilisable((ObjetUtilisable)objAAjouter);
                    }
                    else if(nomDeLObjet.equals("Banane"))
                    {
                        Banane objAAjouter = new Banane(intCompteurIdObjet, true);
                        objMagasin.ajouterObjetUtilisable((ObjetUtilisable)objAAjouter);
                    }
                    else if(nomDeLObjet.equals("Brainiac"))
                    {
                        Brainiac objAAjouter = new Brainiac(intCompteurIdObjet, false);
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
				int intValeur = 1;//Math.max(Math.abs(ClassesUtilitaires.UtilitaireNombres.genererNbAleatoireLoiNormale(0.0d, Math.pow(((double) reglesPartie.obtenirValeurPieceMaximale()) / 3.0d, 2.0d))), 1);
					
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
			Iterator<ReglesObjetUtilisable> objIterateurListePriorite = reglesPartie.obtenirListeObjetsUtilisablesPossibles().iterator();
			
			// On va choisir des objets utilisables en commençant par 
			// l'objet le plus prioritaire et on va faire ça tant qu'on n'a 
			// pas atteint le pourcentage d'objets utilisables devant se 
			// trouver sur le plateau de jeu. Si on atteint la fin de la 
			// liste d'objets utilisables, on recommence depuis le début
			while (intCompteurCases <= intNbObjetsUtilisables)
			{
				// Faire la référence vers la règle de l'objet utilisable 
				// courant
				ReglesObjetUtilisable objReglesObjetUtilisable = objIterateurListePriorite.next();
				
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
				lstPointsObjetsUtilisables.addLast(objPoint);

				// Si le nom de l'objet est Livre, alors on met un objet 
				// Livre sur la case, sinon on fait le même genre de 
				// vérifications pour les autres types de magasins
                                // On définit la valeur de la case au point spécifié à la case d'identification
				if (objReglesObjetUtilisable.obtenirNomObjetUtilisable().equals("Livre") || 
						objReglesObjetUtilisable.obtenirNomObjetUtilisable().equals("Book"))
				{
					((CaseCouleur) objttPlateauJeu[objPoint.x][objPoint.y]).definirObjetCase(new Livre(intCompteurIdObjet, bolEstVisible));					
				}                           
                                else if (objReglesObjetUtilisable.obtenirNomObjetUtilisable().equals("Boule") ||
                                		objReglesObjetUtilisable.obtenirNomObjetUtilisable().equals("Sphere"))
				{
					((CaseCouleur) objttPlateauJeu[objPoint.x][objPoint.y]).definirObjetCase(new Boule(intCompteurIdObjet, bolEstVisible));					
				}                                
                                else if (objReglesObjetUtilisable.obtenirNomObjetUtilisable().equals("Banane") ||
                                		objReglesObjetUtilisable.obtenirNomObjetUtilisable().equals("Banana"))
				{
					((CaseCouleur) objttPlateauJeu[objPoint.x][objPoint.y]).definirObjetCase(new Banane(intCompteurIdObjet, bolEstVisible));					
				}
                                else if (objReglesObjetUtilisable.obtenirNomObjetUtilisable().equals("Brainiac"))
				{
					((CaseCouleur) objttPlateauJeu[objPoint.x][objPoint.y]).definirObjetCase(new Brainiac(intCompteurIdObjet, bolEstVisible));					
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
		table.setObjProchainIdObjet(intCompteurIdObjet);
		
		// if game type = tournament (plateau semilineaire)
		// add the start and end points 
		int reglesSizeTrack =  reglesPartie.getNbTracks();     			
		for (int i = 0; i < reglesSizeTrack; i++)
		{

			objPoint = new Point(0,i);
			/// type = 1 for the Cases - we don't have for the moment diff cases
			objttPlateauJeu[objPoint.x][objPoint.y] = new CaseCouleur(1);   
			lstPointsCaseLibre.add(objPoint);
		}
		
		for (int i = 0; i < reglesSizeTrack; i++)
		{
				
			objPoint = new Point(intNbLines - 1,intNbColumns - i - 1);
			objttPlateauJeu[objPoint.x][objPoint.y] = new CaseCouleur(1);
			lstPointsCaseLibre.add(objPoint);
		}
	
		
		return objttPlateauJeu;
    } // end methode
    
    
    /**
	 * Method for the "Tournament" game board
	 * @param intNbCasesSpeciales
	 * @param objttPlateauJeu
	 */
	private void caseDefinition(int intNbCasesSpeciales, Case[][] objttPlateauJeu) {
		
		//int max_nb_players = reglesPartie.getNbTracks();
		Point objPoint;
		
		// Si on doit afficher des cases spéciales dans le plateau de jeu, 
		// alors on fait le code suivant
		//if (reglesPartie.obtenirListeCasesSpecialesPossibles().size() > 0)
		//{
			// Réinitialiser le compteur de cases
			int intCompteurCasesSpeciale = 0;
			
			// Obtenir un itérateur pour la liste des règles de cases spéciales
			// triées par priorité (c'est certain que la première fois il y a au 
			// moins une règle de case)
			//Iterator objIterateurListePriorite = reglesPartie.obtenirListeCasesSpecialesPossibles().iterator();
			
			// On va choisir des cases spéciales en commençant par la case
			// la plus prioritaire et on va faire ça tant qu'on n'a pas atteint 
			// le pourcentage de cases spéciales devant se trouver sur le plateau 
			// de jeu. Si on atteint la fin de la liste de cases spéciales, on 
			// recommence depuis le début
			while (intCompteurCasesSpeciale < intNbCasesSpeciales)
			{
				// Faire la référence vers la règle de la case spéciale 
				// courante
				//ReglesCaseSpeciale objReglesCaseSpeciale = (ReglesCaseSpeciale) objIterateurListePriorite.next();
				
				// Obtenir un point aléatoirement parmi les points restants
				// qui n'ont pas de cases spéciales et enlever en même temps 
				// ce point de la liste
				objPoint = (Point) lstPointsCasesPresentes.remove(objRandom.nextInt(lstPointsCasesPresentes.size()));
				
				// Ajouter le point trouvé dans la liste des points de cases 
				// spéciales trouvées
				lstPointsCasesSpeciales.addLast(objPoint);

				// Définir la valeur de la case au point spécifié à la case 
				// d'identification
				objttPlateauJeu[objPoint.x][objPoint.y] = new CaseSpeciale(1);				
				
				// Incrémenter le nombre de cases passées
				intCompteurCasesSpeciale++;
				
				// Si on est arrivé à la fin de la liste, alors il faut 
				// retourner au début
				//if (objIterateurListePriorite.hasNext() == false)
				//{
					// Obtenir un autre itérateur pour la liste
					//objIterateurListePriorite = reglesPartie.obtenirListeCasesSpecialesPossibles().iterator();
				//}
			//}			
		}
		
		// Bloc de code qui va s'assurer de créer les cases de couleur dans le 
		// plateau de jeu (il y en a au moins un type)
		{
			// Réinitialiser le compteur de cases
			int intCompteurCasesCouleur = 0;
						
			// On va choisir des cases de couleur en commençant par la case
			// la plus prioritaire et on va faire ça tant qu'on n'a pas atteint 
			// le pourcentage de cases spéciales devant se trouver sur le plateau 
			// de jeu. Si on atteint la fin de la liste de cases de couleur, on 
			// recommence depuis le début
						
			while (intCompteurCasesCouleur < intCompteurCases - intNbCasesSpeciales - reglesPartie.getNbTracks()*3)
			{
				
				// Faire la référence vers la règle de la case de couleur 
				// courante
				//ReglesCaseCouleur objReglesCaseCouleur = (ReglesCaseCouleur) objIterateurListePriorite.next();
				
				// Obtenir un point aléatoirement parmi les points restants
				// qui n'ont pas de cases spéciales et de cases de couleur 
				// et enlever en même temps ce point de la liste
				objPoint = (Point) lstPointsCasesPresentes.remove(objRandom.nextInt(lstPointsCasesPresentes.size()));
				
				// Ajouter le point trouvé dans la liste des points de cases 
				// de couleur trouvées
				lstPointsCasesCouleur.addLast(objPoint);

				// Définir la valeur de la case au point spécifié à la case 
				// d'identification
				objttPlateauJeu[objPoint.x][objPoint.y] = new CaseCouleur(1);				
				
				// Incrémenter le nombre de cases passées
				intCompteurCasesCouleur++;
				
				// Si on est arrivé à la fin de la liste, alors il faut 
				// retourner au début
				//if (objIterateurListePriorite.hasNext() == false)
				//{
					// Obtenir un autre itérateur pour la liste
					//objIterateurListePriorite = reglesPartie.obtenirListeCasesCouleurPossibles().iterator();
				//}
			}
			
			// La liste des cases présentes est maintenant la liste des cases 
			// de couleur, car tous les points de la liste ont été copiés dans 
			// l'autre liste
			lstPointsCasesPresentes = lstPointsCasesCouleur;
			lstPointsCasesCouleur = null;
		}
		
	}// end method

	 /**
     * Method used to create the game board for the game type "Course" and "Tournament"
     * @param intNbTrous 
     * @param objttPlateauJeu
     */
	private void boardCreation(int intNbTrous, Case[][] objttPlateauJeu) {
		
		Point objPoint;
		int nbTracks = reglesPartie.getNbTracks();
		for(int x = 0; x < intNbLines; x++){
			for(int y = 0; y < intNbColumns; y++){
			
				if(isCase(x, y, intNbLines, intNbColumns, nbTracks )){
					// Créer le point de la case courante
					objPoint = new Point(x, y);
					
					// Définir la valeur de la case au point spécifié à la case 
					// d'identification
					objttPlateauJeu[objPoint.x][objPoint.y] = objCaseParcourue;
					
					// Ajouter le point dans la liste des points passés
					lstPointsCasesPresentes.addLast(objPoint);
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
		    
		    if(  objPoint.x % 3 == 0 || objPoint.y % 3 == 0 || objPoint.y == intNbColumns -1 || objPoint.y == 0 || (objPoint.y < nbTracks && objPoint.x < 6)){     // objPoint.x == 0|| objPoint.x == intNbLignes - 1 ||
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
			objPoint = new Point(intNbLines - 1 ,intNbColumns - i - 1);
			lstPointsCasesPresentes.remove(objPoint);
		}
				
	}// end method
	
	/**
     * Method used to create the semilinear game board
     * @param x
     * @param y
     * @param intNbLignes
     * @param intNbColonnes
     * @param nbTracks 
     * @return boolean if the point will be or not used
     */
    private boolean isCase(int x, int y, int intNbLignes, int intNbColonnes, int nbTracks) {
		
    	boolean isCase = true;
		if ( nbTracks % 2 == 0 ){
			
			if((y + 1) %(nbTracks + 2) == 0){
				if((y + 1) / (nbTracks + 2) % 2 == 1 && x <= intNbLignes - nbTracks - 1 )
					isCase = false;
				else if ((y + 1) / (nbTracks + 2) % 2 == 0 && x > nbTracks - 1 )
					isCase = false;
			}else if((y + 2) % (nbTracks + 2) == 0){
				if((y + 2) / (nbTracks + 2) % 2 == 1 && x <= intNbLignes - nbTracks - 1)
					isCase = false;
				else if ((y + 2) / (nbTracks + 2) % 2 == 0 && x > nbTracks - 1 )
					isCase = false;
			}
		
		}else if (nbTracks % 2 == 1 ){
		
			if((y + 1) % (nbTracks + 2) == 0){
				if((y + 1) % 2 == 1 && x <= intNbLignes - nbTracks - 1 )
					isCase = false;
				else if ((y + 1) % 2 == 0 && x > nbTracks - 1 )
					isCase = false;
			}else if((y + 2) % (nbTracks + 2) == 0){
				if((y + 1) % 2 == 0 && x <= intNbLignes - nbTracks - 1 )
					isCase = false;
				else if ((y + 1) % 2 == 1 && x > nbTracks - 1 )
					isCase = false;
			}
		
		}
		
		return isCase;
	} //end method
    
    /**
     * Cette fonction permet de générer la position des joueurs. Chaque joueur 
     * est généré sur une case vide.
     * 
     * @param int nbJoueurs : Le nombre de joueurs dont générer la position
     * @return Point[] : Un tableau de points pour chaque joueur 
     */
    public Point[] genererPositionJoueurs(Table table, int nbJoueurs, ArrayList<Point> lstPointsCaseLibre)
    {
    	int nbTracks = table.getRegles().getNbTracks();
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
    


}
