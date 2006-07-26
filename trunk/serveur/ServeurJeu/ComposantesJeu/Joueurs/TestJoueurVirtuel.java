package ServeurJeu.ComposantesJeu.Joueurs;

import java.util.Date;
import java.util.TreeMap;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.awt.Point;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import Enumerations.RetourFonctions.ResultatAuthentification;
import ServeurJeu.BD.GestionnaireBD;
import ServeurJeu.Communications.GestionnaireCommunication;
import ServeurJeu.Communications.ProtocoleJoueur;
import ServeurJeu.ComposantesJeu.Salle;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.Evenements.EvenementJoueurDeconnecte;
import ServeurJeu.Evenements.EvenementJoueurConnecte;
import ServeurJeu.Evenements.GestionnaireEvenements;
import ServeurJeu.Evenements.InformationDestination;
import ServeurJeu.Temps.GestionnaireTemps;
import ServeurJeu.Temps.TacheSynchroniser;
import ClassesUtilitaires.Espion;
import ServeurJeu.ControleurJeu;
import ServeurJeu.ComposantesJeu.Table;
import ServeurJeu.ComposantesJeu.ReglesJeu.Regles;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurVirtuel;

import ServeurJeu.ComposantesJeu.Cases.Case;
import ServeurJeu.ComposantesJeu.Cases.CaseCouleur;
import ServeurJeu.ComposantesJeu.Objets.Pieces.Piece;
import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.Reponse;
import java.io.File;
import java.io.FileWriter;

import ClassesUtilitaires.UtilitaireEncodeurDecodeur;
import ClassesUtilitaires.GenerateurPartie;





import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import ClassesUtilitaires.UtilitaireXML;
import ServeurJeu.ComposantesJeu.Objets.Magasins.Magasin;
import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.ObjetUtilisable;



/**
 * @author Jean-Fran�ois Fournier
 */
public class TestJoueurVirtuel {
    
    
    
    public TestJoueurVirtuel(ControleurJeu controleur)
    {
        GestionnaireEvenements ev = controleur.obtenirGestionnaireEvenements();
        GestionnaireBD bd = controleur.obtenirGestionnaireBD();
        TreeMap lstSalles = controleur.obtenirListeSalles();
        Salle salleGenerale = null;
        int noTable = 1;
        String nomCreateur = "Test Createur";
        int tempsPartie = 60;
        Regles reglesTable = null;
        GestionnaireTemps t = controleur.obtenirGestionnaireTemps();
        TacheSynchroniser ts = controleur.obtenirTacheSynchroniser();
        Table objTable = null;
        
        // Obtenir la salle g�n�rale
        Set lstEnsembleSalles = lstSalles.entrySet();
        Iterator objIterateurListeSalles = lstEnsembleSalles.iterator();      
        salleGenerale = (Salle)(((Map.Entry)(objIterateurListeSalles.next())).getValue());
       
       
        TreeMap lstTables = salleGenerale.obtenirListeTables();
        reglesTable = salleGenerale.obtenirRegles();
        
        synchronized (lstTables)
        {
            // Cr�er une table
            objTable = new Table(bd, salleGenerale, noTable, nomCreateur, 
                tempsPartie, reglesTable, t, ts, controleur);
            
            objTable.creation();
            
            // Ajouter la table dans la liste des tables
            lstTables.put(new Integer(objTable.obtenirNoTable()), objTable);

        }

        //-----------------------------------------
            System.out.println("Table cr��e");
        //-----------------------------------------
        
        /*String tNoms[] = objTable.obtenirNomsJoueursVirtuels(10);
        for (int i = 0; i < 10; i++)
        {
        	System.out.println("" + tNoms[i]);
        }*/
        
        
        // Cr�er le plateau de jeu
        Vector lstPointsCaseLibre = objTable.genererPlateauJeu();
        
        //-----------------------------------------
            System.out.println("Plateau de jeu cr��");
        //-----------------------------------------   
             
        // Ajouter un joueur virtuel dans la table
        JoueurVirtuel jv = new JoueurVirtuel("Test Bot 1 - The DeStRuCtOr", 1, objTable, ev, controleur);
        
        Point ptJv[] = GenerateurPartie.genererPositionJoueurs(1, lstPointsCaseLibre);
        jv.definirPositionJoueurVirtuel(new Point(ptJv[0].x,ptJv[0].y));
        //-----------------------------------------
            System.out.println("Joueur virtuel cr��");
        //-----------------------------------------
        
        outputPlateau(objTable.obtenirPlateauJeuCourant());
        
        //-----------------------------------------
            System.out.println("Plateau de jeu dans plateau.txt");
        //-----------------------------------------       
        
        // Minuterie
        objTable.demarrerMinuterie();
        
        //-----------------------------------------
            System.out.println("Minuterie cr��");
        //-----------------------------------------
        
        // D�marrer le joueur virtuel
        Thread threadJv = new Thread(jv);
        threadJv.start();
        
        //-----------------------------------------
            System.out.println("Thread du joueur virtuel d�marr�");
        //-----------------------------------------

/*
        // Test: generer Plateau jeu
        double dblStart = System.currentTimeMillis();
        String codeXML = genererCodeXML(objTable.obtenirPlateauJeuCourant());
        double dblEnd = System.currentTimeMillis();
        System.out.println("Time taken : " + (dblEnd - dblStart) + " ms.");
        
        try
        {
            // �crire dans le fichier
            FileWriter writer = new FileWriter("codeXML.xml");
            writer.write(codeXML);
            writer.close();

        }
        catch( Exception e)
        {
            System.out.println("Erreur d'�criture dans le fichier codeXML.");
            //objLogger.info("Erreur d'�criture dans le fichier plateau.");
            //objLogger.error(e.getMessage());
        }


        // Test encoderUTF8
        dblStart = System.currentTimeMillis();
        String codeXMLencode = UtilitaireEncodeurDecodeur.encodeToUTF8(codeXML);
        dblEnd = System.currentTimeMillis();
        System.out.println("Time taken: " + (dblEnd - dblStart) + " ms.");
        
        try
        {
            // �crire dans le fichier
            FileWriter writer = new FileWriter("codeXMLencode.xml");
            writer.write(codeXML);
            writer.close();

        }
        catch( Exception e)
        {
            System.out.println("Erreur d'�criture dans le fichier codeXML.");
            //objLogger.info("Erreur d'�criture dans le fichier plateau.");
            //objLogger.error(e.getMessage());
        } 
*/

    }
    
    public static void outputPlateau(Case[][] pj)
    {
        int nbLignes = pj.length;
        int nbColonnes = pj[0].length;
        StringBuffer strTemp  = new StringBuffer();
        
        int x = 0;
        
        strTemp.append("  ");
        for (int j = 0; j < nbColonnes; j++)
        {
            strTemp.append(x);
            x = (x+1)%10;
        }
        strTemp.append(System.getProperty("line.separator"));
        
        x = 0;
        
        for (int i = 0; i < nbLignes; i++)
        {
            strTemp.append(x);
            x = (x+1)%10;
            strTemp.append(" ");
            
            for (int j = 0; j < nbColonnes; j++)
            {
                if (pj[i][j] == null)
                {
                    strTemp.append(" ");
                }
                else 
                {
                    if (pj[i][j] instanceof CaseCouleur)
                    {
                        if (((CaseCouleur)pj[i][j]).obtenirObjetCase() instanceof Piece)
                        {
                            strTemp.append("P");
                        }
                        else if (((CaseCouleur)pj[i][j]).obtenirObjetCase() instanceof Magasin)
                        {
                        	strTemp.append("M"); 
                        }
                        else if (((CaseCouleur)pj[i][j]).obtenirObjetCase() instanceof Reponse)
                        {
                        	if ((((ObjetUtilisable)((CaseCouleur)pj[i][j]).obtenirObjetCase())).estVisible())
                        	{
                        		strTemp.append("R");
                        	}
                        	else
                        	{
                        		strTemp.append("r");
                        	}
                        }
                        else
                        {
                            strTemp.append("C");    
                        }
                    }  
                    else
                    {
                        strTemp.append("A");
                    }
                }
            }
            
            strTemp.append(System.getProperty("line.separator"));
        }
        
            
        try
        {
            // �crire dans le fichier
            FileWriter writer = new FileWriter("plateau.txt");
            writer.write(strTemp.toString());
            writer.close();

        }
        catch( Exception e)
        {
            System.out.println("Erreur d'�criture dans le fichier plateau.");
            //objLogger.info("Erreur d'�criture dans le fichier plateau.");
            //objLogger.error(e.getMessage());
        }
        
           
    }
    
    
    
    
    
    
    
    
    
    
	public String genererCodeXML(Case[][] objttPlateauJeu)
	{
	    // D�claration d'une variable qui va contenir le code XML � retourner
	    String strCodeXML = "";
	    
	    Document objDocumentXML = null;
	    Element objNoeudCommande = null;
	    int intTempsPartie = 15;
	    
		try
		{
			if( objDocumentXML == null )
			{
		        // Appeler une fonction qui va cr�er un document XML dans lequel 
			    // on peut ajouter des noeuds
		        objDocumentXML = UtilitaireXML.obtenirDocumentXML();
	
				// Cr�er le noeud de commande � retourner
				objNoeudCommande = objDocumentXML.createElement("commande");
				
				// Cr�er les noeuds de param�tre et de taille
				Element objNoeudParametreTempsPartie = objDocumentXML.createElement("parametre");
				Element objNoeudParametreTaillePlateauJeu = objDocumentXML.createElement("parametre");
				Element objNoeudParametrePositionJoueurs = objDocumentXML.createElement("parametre");
				Element objNoeudParametrePlateauJeu = objDocumentXML.createElement("parametre");
				Element objNoeudParametreTaille = objDocumentXML.createElement("taille");
				
				// Cr�er un noeud contenant le temps de la partie
				Text objNoeudTexte = objDocumentXML.createTextNode(Integer.toString(intTempsPartie));
				
				// Ajouter le noeud texte au noeud du param�tre
				objNoeudParametreTempsPartie.appendChild(objNoeudTexte);
				
				// D�finir les attributs du noeud de commande
				objNoeudCommande.setAttribute("type", "Evenement");
				objNoeudCommande.setAttribute("nom", "PartieDemarree");
				
				// On ajoute un attribut type qui va contenir le type
				// du param�tre
				objNoeudParametreTempsPartie.setAttribute("type", "TempsPartie");
				objNoeudParametreTaillePlateauJeu.setAttribute("type", "Taille");
				objNoeudParametrePositionJoueurs.setAttribute("type", "PositionJoueurs");
				objNoeudParametrePlateauJeu.setAttribute("type", "PlateauJeu");
				
				// Cr�er les informations concernant la taille
				objNoeudParametreTaille.setAttribute("nbLignes", Integer.toString(objttPlateauJeu.length));
				objNoeudParametreTaille.setAttribute("nbColonnes", Integer.toString(objttPlateauJeu[0].length));
				
				// Ajouter les noeuds enfants aux noeuds param�tres
				objNoeudParametreTaillePlateauJeu.appendChild(objNoeudParametreTaille);
				
                /*
				// Cr�er un ensemble contenant tous les tuples de la liste 
				// des positions de joueurs (chaque �l�ment est un Map.Entry)
				Set lstEnsemblePositionJoueurs = lstPositionJoueurs.entrySet();
				
				// Obtenir un it�rateur pour l'ensemble contenant les positions 
				// des joueurs
				Iterator objIterateurListe = lstEnsemblePositionJoueurs.iterator();
				
				// Passer tous les positions des joueurs et cr�er leur code XML
				while (objIterateurListe.hasNext() == true)
				{
					// D�claration d'une r�f�rence vers l'objet cl� valeur courant
					Map.Entry objPositionJoueur = (Map.Entry) objIterateurListe.next();
					
					// Cr�er une r�f�rence vers la position du joueur courant
					Point objPosition = (Point) objPositionJoueur.getValue();
					
					// Cr�er un noeud de case en passant le bon nom
					Element objNoeudPositionJoueur = objDocumentXML.createElement("position");
					
					// D�finir les attributs du noeud courant
					objNoeudPositionJoueur.setAttribute("nom", (String) objPositionJoueur.getKey());
					objNoeudPositionJoueur.setAttribute("x", Integer.toString(objPosition.x));
					objNoeudPositionJoueur.setAttribute("y", Integer.toString(objPosition.y));
					
					// Ajouter le noeud de position courant au noeud param�tre
					objNoeudParametrePositionJoueurs.appendChild(objNoeudPositionJoueur);
				}
                */
				// Cr�er un noeud de case en passant le bon nom
				Element objNoeudPositionJoueur = objDocumentXML.createElement("position");
				
				// D�finir les attributs du noeud courant
				objNoeudPositionJoueur.setAttribute("nom", "Bobinette");
				objNoeudPositionJoueur.setAttribute("x", Integer.toString(8));
				objNoeudPositionJoueur.setAttribute("y", Integer.toString(8));
				
				// Ajouter le noeud de position courant au noeud param�tre
				objNoeudParametrePositionJoueurs.appendChild(objNoeudPositionJoueur);
				
				// Passer toutes les lignes du plateau de jeu et cr�er toutes 
				// les cases
				for (int i = 0; i < objttPlateauJeu.length; i++)
				{
					// Passer toutes les colonnes du plateau de jeu
					for (int j = 0; j < objttPlateauJeu[0].length; j++)
					{
						// S'il y a une case au point courant, alors on peut la 
						// cr�er en XML, sinon on ne fait rien
						if (objttPlateauJeu[i][j] != null)
						{
							// D�claration d'un noeud de case
							Element objNoeudCase;
							
							// Si la classe de l'objet courant est CaseCouleur,
							// alors on va cr�er l'�l�ment en passant le bon nom
							if (objttPlateauJeu[i][j] instanceof CaseCouleur)
							{
								// Cr�er le noeud de case en passant le bon nom
								objNoeudCase = objDocumentXML.createElement("caseCouleur");
							}
							else
							{
								// Cr�er le noeud de case en passant le bon nom
								objNoeudCase = objDocumentXML.createElement("caseSpeciale");		
							}
							
							// Cr�er les informations de la case
							objNoeudCase.setAttribute("x", Integer.toString(i));
							objNoeudCase.setAttribute("y", Integer.toString(j));
							objNoeudCase.setAttribute("type", Integer.toString(objttPlateauJeu[i][j].obtenirTypeCase()));
							
							// Si la case courante est une case couleur, alors
							// on d�finit son objet, sinon on ne fait rien de 
							// plus pour une case sp�ciale
							if (objttPlateauJeu[i][j] instanceof CaseCouleur)
							{
								// Cr�er une r�f�rence vers la case couleur 
								// courante
								CaseCouleur objCaseCouleur = (CaseCouleur) objttPlateauJeu[i][j];
								
								// S'il y a un objet sur la case, alors on va 
								// cr�er le code XML pour cet objet (il ne peut 
								// y en avoir qu'un seul)
								if (objCaseCouleur.obtenirObjetCase() != null)
								{
									// D�claration d'un noeud d'objet
									Element objNoeudObjet;
									
									// Si l'objet sur la case est un magasin
									if (objCaseCouleur.obtenirObjetCase() instanceof Magasin)
									{
										// Cr�er le noeud d'objet
										objNoeudObjet = objDocumentXML.createElement("magasin");
										
										// Mettre le nom de la classe de l'objet comme attribut
										objNoeudObjet.setAttribute("nom", objCaseCouleur.obtenirObjetCase().getClass().getSimpleName());
									}
									else if (objCaseCouleur.obtenirObjetCase() instanceof ObjetUtilisable)
									{
										// Cr�er le noeud d'objet
										objNoeudObjet = objDocumentXML.createElement("objetUtilisable");
										
										// D�finir les attributs de l'objet
										objNoeudObjet.setAttribute("id", Integer.toString(((ObjetUtilisable) objCaseCouleur.obtenirObjetCase()).obtenirId()));
										objNoeudObjet.setAttribute("nom", objCaseCouleur.obtenirObjetCase().getClass().getSimpleName());
										objNoeudObjet.setAttribute("visible", Boolean.toString(((ObjetUtilisable) objCaseCouleur.obtenirObjetCase()).estVisible()));
									}
									else
									{
										// Cr�er le noeud d'objet
										objNoeudObjet = objDocumentXML.createElement("piece");
										
										// D�finir la valeur de l'objet
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
				
				// Ajouter le noeud param�tre au noeud de commande
				objNoeudCommande.appendChild(objNoeudParametreTempsPartie);
				objNoeudCommande.appendChild(objNoeudParametreTaillePlateauJeu);
				objNoeudCommande.appendChild(objNoeudParametrePositionJoueurs);
				objNoeudCommande.appendChild(objNoeudParametrePlateauJeu);
	
				// Ajouter le noeud de commande au noeud racine dans le document
				objDocumentXML.appendChild(objNoeudCommande);
			}
			
			objNoeudCommande.setAttribute("no", Integer.toString(99));

			// Transformer le document XML en code XML
			strCodeXML = UtilitaireXML.transformerDocumentXMLEnString(objDocumentXML);
		}
		catch (TransformerConfigurationException tce)
		{
			System.out.println("Une erreur est survenue lors de la transformation du document XML en chaine de caracteres");
		}
		catch (TransformerException te)
		{
			System.out.println("Une erreur est survenue lors de la conversion du document XML en chaine de caracteres");
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		
		return strCodeXML;
	}
    
    
    
    
    
    
    
    
    
    
    
    
}
