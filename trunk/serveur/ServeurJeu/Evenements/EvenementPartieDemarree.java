package ServeurJeu.Evenements;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.awt.Point;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import ServeurJeu.ComposantesJeu.Cases.Case;
import ServeurJeu.ComposantesJeu.Cases.CaseCouleur;
import ServeurJeu.ComposantesJeu.Objets.Magasins.Magasin;
import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.ObjetUtilisable;
import ServeurJeu.ComposantesJeu.Objets.Pieces.Piece;
import ServeurJeu.ControleurJeu;
import ServeurJeu.Monitoring.Moniteur;
import ClassesUtilitaires.UtilitaireXML;
import ServeurJeu.ComposantesJeu.Table;
import ServeurJeu.Configuration.GestionnaireMessages;

/**
 * @author Jean-François Brind'Amour
 */
public class EvenementPartieDemarree extends Evenement
{
	// Déclaration d'une variable qui va garder le temps de la partie
    private int intTempsPartie;
    
	// Déclaration d'un tableau à 2 dimensions qui va contenir les informations 
	// sur les cases du jeu
	private Case[][] objttPlateauJeu;
	
	// Déclaration d'une liste contenant les positions des joueurs et dont la 
	// clé est le nom d'utilisateur du joueur
	private TreeMap lstPositionJoueurs;
	
	private Document objDocumentXML;
	private Element objNoeudCommande;
        private Table table;
    
    /**
     * Constructeur de la classe EvenementPartieDemarree qui permet 
     * d'initialiser le numéro de la table et le plateau de jeu pour la partie 
     * qui vient de débuter.
     *
     * @param int tempsPartie : Le temps total de la partie
     * @param TreeMap listePositionJoueurs : La liste des positions des joueurs
     * @param Case[][] plateauJeu : Un tableau à 2 dimensions représentant le 
     * 								plateau de jeu
     */
    public EvenementPartieDemarree(int tempsPartie, TreeMap listePositionJoueurs, Case[][] plateauJeu, Table table)
    {
        // Définir le temps de la partie, le plateau de jeu et la liste des
    	// positions des joueurs
    	intTempsPartie = tempsPartie;
        objttPlateauJeu = plateauJeu;
        this.table = table;
        lstPositionJoueurs = listePositionJoueurs;
        objDocumentXML = null;
        objNoeudCommande = null;
    }
	
	/**
	 * Cette fonction permet de générer le code XML de l'événement du début 
	 * d'une partie et de le retourner.
	 * 
	 * @param InformationDestination information : Les informations à qui 
	 * 					envoyer l'événement
	 * @return String : Le code XML de l'événement à envoyer
	 */
	protected String genererCodeXML(InformationDestination information)
	{
		Moniteur.obtenirInstance().debut( "EvenementPartieDemarree.genererCodeXML" );
	    // Déclaration d'une variable qui va contenir le code XML à retourner
	    String strCodeXML = "";
	 
		try
		{
			if( objDocumentXML == null )
			{
		        // Appeler une fonction qui va créer un document XML dans lequel 
			    // on peut ajouter des noeuds
		        objDocumentXML = UtilitaireXML.obtenirDocumentXML();
	
				// Créer le noeud de commande à retourner
				objNoeudCommande = objDocumentXML.createElement("commande");
				
				// Créer les noeuds de paramètre et de taille
				Element objNoeudParametreTempsPartie = objDocumentXML.createElement("parametre");
				Element objNoeudParametreTaillePlateauJeu = objDocumentXML.createElement("parametre");
				Element objNoeudParametrePositionJoueurs = objDocumentXML.createElement("parametre");
				Element objNoeudParametrePlateauJeu = objDocumentXML.createElement("parametre");
				Element objNoeudParametreTaille = objDocumentXML.createElement("taille");
				
				// Créer un noeud contenant le temps de la partie
				Text objNoeudTexte = objDocumentXML.createTextNode(Integer.toString(intTempsPartie));
				
				// Ajouter le noeud texte au noeud du paramètre
				objNoeudParametreTempsPartie.appendChild(objNoeudTexte);
				
				// Définir les attributs du noeud de commande
				objNoeudCommande.setAttribute("type", "Evenement");
				objNoeudCommande.setAttribute("nom", "PartieDemarree");
				
				// On ajoute un attribut type qui va contenir le type
				// du paramètre
				objNoeudParametreTempsPartie.setAttribute("type", "TempsPartie");
				objNoeudParametreTaillePlateauJeu.setAttribute("type", "Taille");
				objNoeudParametrePositionJoueurs.setAttribute("type", "PositionJoueurs");
				objNoeudParametrePlateauJeu.setAttribute("type", "PlateauJeu");
                                
						/*
						 * *****************
						 * TODO 
						 * REPLACE THIS CODE TO 
						 * USE THE WIN THE GAME AGAIN
						 * *****************
						 */
                                
                                if(!table.obtenirButDuJeu().equals("original"))
                                {
                                    // Créer le noeud contenant la position initiale du WinTheGame
                                    {
                                        int z=0;
                                        while((table.obtenirPositionWinTheGame().x == -1 || table.obtenirPositionWinTheGame().y == -1) && z<100)
                                        {
                                            table.definirNouvellePositionWinTheGame();
                                            z++;
                                        }
                                    }

                                    Element objNoeudParametrePositionWinTheGame = objDocumentXML.createElement("parametre");
                                    objNoeudParametrePositionWinTheGame.setAttribute("type", "positionWinTheGame");
                                    objNoeudParametrePositionWinTheGame.setAttribute("x", Integer.toString(table.obtenirPositionWinTheGame().x));
                                    objNoeudParametrePositionWinTheGame.setAttribute("y", Integer.toString(table.obtenirPositionWinTheGame().y));
                                     objNoeudParametrePositionWinTheGame.setAttribute("pointageRequis", Integer.toString(table.pointageRequisPourAllerSurLeWinTheGame()));
                                    objNoeudCommande.appendChild(objNoeudParametrePositionWinTheGame);
                                }
                                 
				
				
				// Créer les informations concernant la taille
				objNoeudParametreTaille.setAttribute("nbLignes", Integer.toString(objttPlateauJeu.length));
				objNoeudParametreTaille.setAttribute("nbColonnes", Integer.toString(objttPlateauJeu[0].length));
				
				// Ajouter les noeuds enfants aux noeuds paramètres
				objNoeudParametreTaillePlateauJeu.appendChild(objNoeudParametreTaille);
				
				// Créer un ensemble contenant tous les tuples de la liste 
				// des positions de joueurs (chaque élément est un Map.Entry)
				Set lstEnsemblePositionJoueurs = lstPositionJoueurs.entrySet();
				
				// Obtenir un itérateur pour l'ensemble contenant les positions 
				// des joueurs
				Iterator objIterateurListe = lstEnsemblePositionJoueurs.iterator();
				
				// Passer tous les positions des joueurs et créer leur code XML
				while (objIterateurListe.hasNext() == true)
				{
					// Déclaration d'une référence vers l'objet clé valeur courant
					Map.Entry objPositionJoueur = (Map.Entry) objIterateurListe.next();
					
					// Créer une référence vers la position du joueur courant
					Point objPosition = (Point) objPositionJoueur.getValue();
					
					// Créer un noeud de case en passant le bon nom
					Element objNoeudPositionJoueur = objDocumentXML.createElement("position");
					
					// Définir les attributs du noeud courant
					objNoeudPositionJoueur.setAttribute("nom", (String) objPositionJoueur.getKey());
					objNoeudPositionJoueur.setAttribute("x", Integer.toString(objPosition.x));
					objNoeudPositionJoueur.setAttribute("y", Integer.toString(objPosition.y));
					
					// Ajouter le noeud de position courant au noeud paramètre
					objNoeudParametrePositionJoueurs.appendChild(objNoeudPositionJoueur);
				}
				
				// Passer toutes les lignes du plateau de jeu et créer toutes 
				// les cases
				for (int i = 0; i < objttPlateauJeu.length; i++)
				{
					// Passer toutes les colonnes du plateau de jeu
					for (int j = 0; j < objttPlateauJeu[0].length; j++)
					{
						// S'il y a une case au point courant, alors on peut la 
						// créer en XML, sinon on ne fait rien
						if (objttPlateauJeu[i][j] != null)
						{
							// Déclaration d'un noeud de case
							Element objNoeudCase;
							
							// Si la classe de l'objet courant est CaseCouleur,
							// alors on va créer l'élément en passant le bon nom
							if (objttPlateauJeu[i][j] instanceof CaseCouleur)
							{
								// Créer le noeud de case en passant le bon nom
								objNoeudCase = objDocumentXML.createElement("caseCouleur");
							}
							else
							{
								// Créer le noeud de case en passant le bon nom
								objNoeudCase = objDocumentXML.createElement("caseSpeciale");		
							}
							
							// Créer les informations de la case
							objNoeudCase.setAttribute("x", Integer.toString(i));
							objNoeudCase.setAttribute("y", Integer.toString(j));
							objNoeudCase.setAttribute("type", Integer.toString(objttPlateauJeu[i][j].obtenirTypeCase()));
							
							// Si la case courante est une case couleur, alors
							// on définit son objet, sinon on ne fait rien de 
							// plus pour une case spéciale
							if (objttPlateauJeu[i][j] instanceof CaseCouleur)
							{
								// Créer une référence vers la case couleur 
								// courante
								CaseCouleur objCaseCouleur = (CaseCouleur) objttPlateauJeu[i][j];
								
								// S'il y a un objet sur la case, alors on va 
								// créer le code XML pour cet objet (il ne peut 
								// y en avoir qu'un seul)
								if (objCaseCouleur.obtenirObjetCase() != null)
								{
									// Déclaration d'un noeud d'objet
									Element objNoeudObjet;
									
									// Si l'objet sur la case est un magasin
									if (objCaseCouleur.obtenirObjetCase() instanceof Magasin)
									{
										// Créer le noeud d'objet
										objNoeudObjet = objDocumentXML.createElement("magasin");
										
										// Mettre le nom de la classe de l'objet comme attribut
										objNoeudObjet.setAttribute("nom", objCaseCouleur.obtenirObjetCase().getClass().getSimpleName());
									}
									else if (objCaseCouleur.obtenirObjetCase() instanceof ObjetUtilisable)
									{
										// Créer le noeud d'objet
										objNoeudObjet = objDocumentXML.createElement("objetUtilisable");
										
										// Définir les attributs de l'objet
										objNoeudObjet.setAttribute("id", Integer.toString(((ObjetUtilisable) objCaseCouleur.obtenirObjetCase()).obtenirId()));
										objNoeudObjet.setAttribute("nom", objCaseCouleur.obtenirObjetCase().getClass().getSimpleName());
										objNoeudObjet.setAttribute("visible", Boolean.toString(((ObjetUtilisable) objCaseCouleur.obtenirObjetCase()).estVisible()));
									}
									else
									{
										// Créer le noeud d'objet
										objNoeudObjet = objDocumentXML.createElement("piece");
										
										// Définir la valeur de l'objet
										objNoeudObjet.setAttribute("valeur", Integer.toString(((Piece) objCaseCouleur.obtenirObjetCase()).obtenirValeur()));										
									}
									
									// Ajouter le noeud objet au noeud de la case
									objNoeudCase.appendChild(objNoeudObjet);
								}
							}
							
							// Ajouter la case courante au noeud du plateau de 
							// jeu
							objNoeudParametrePlateauJeu.appendChild(objNoeudCase);
						}
					}
				}
                                
				// Ajouter le noeud paramètre au noeud de commande
				objNoeudCommande.appendChild(objNoeudParametreTempsPartie);
				objNoeudCommande.appendChild(objNoeudParametreTaillePlateauJeu);
				objNoeudCommande.appendChild(objNoeudParametrePositionJoueurs);
				objNoeudCommande.appendChild(objNoeudParametrePlateauJeu);
	
				// Ajouter le noeud de commande au noeud racine dans le document
				objDocumentXML.appendChild(objNoeudCommande);
			}
			
			objNoeudCommande.setAttribute("no", Integer.toString(information.obtenirNoCommande()));

			// Transformer le document XML en code XML
			strCodeXML = UtilitaireXML.transformerDocumentXMLEnString(objDocumentXML);
		}
		catch (TransformerConfigurationException tce)
		{
			System.out.println(GestionnaireMessages.message("evenement.XML_transformation"));
		}
		catch (TransformerException te)
		{
			System.out.println(GestionnaireMessages.message("evenement.XML_conversion"));
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		
		Moniteur.obtenirInstance().fin();
                if(ControleurJeu.modeDebug) System.out.println("EvenementPartieDemarrer: " + strCodeXML);
		return strCodeXML;
	}
}
