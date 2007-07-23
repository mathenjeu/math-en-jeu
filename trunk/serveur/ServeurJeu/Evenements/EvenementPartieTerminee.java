/*
 * Created on 2006-03-17
 *
 * 
 */
package ServeurJeu.Evenements;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.TreeMap;
import java.util.Iterator;
import java.util.Vector;

import ClassesUtilitaires.UtilitaireXML;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurVirtuel;
import ServeurJeu.Configuration.GestionnaireMessages;

/**
 * @author Marc
 *
 * 
 */
public class EvenementPartieTerminee  extends Evenement
{
	private TreeMap lstJoueurs;
	private Vector lstJoueursVirtuels;
        private String joueurGagnant;
	
	public EvenementPartieTerminee( TreeMap joueurs, Vector joueursVirtuels, String joueurGagnant)
	{
		super();
		lstJoueurs = joueurs;
		lstJoueursVirtuels = joueursVirtuels;
                this.joueurGagnant = joueurGagnant;
	}
	
	protected String genererCodeXML(InformationDestination information)
	{
	    // Déclaration d'une variable qui va contenir le code XML à retourner
	    String strCodeXML = "";
	    
		try
		{
	        // Appeler une fonction qui va créer un document XML dans lequel 
		    // on peut ajouter des noeuds
	        Document objDocumentXML = UtilitaireXML.obtenirDocumentXML();

			// Créer le noeud de commande à retourner
			Element objNoeudCommande = objDocumentXML.createElement("commande");
			
			// Créer un noeud contenant le nom d'utilisateur du noeud paramètre
			
			// Définir les attributs du noeud de commande
			objNoeudCommande.setAttribute("no", Integer.toString(information.obtenirNoCommande()));
			objNoeudCommande.setAttribute("type", "Evenement");
			objNoeudCommande.setAttribute("nom", "PartieTerminee");
			
                        // Créer le noeud du paramètre
			Element objNoeudParametre = objDocumentXML.createElement("parametre");
			objNoeudParametre.setAttribute("type", "StatistiqueJoueur");
                        
                        Element objNoeudARejointLeWinTheGame = objDocumentXML.createElement("joueurWinTheGame");
                        objNoeudARejointLeWinTheGame.setAttribute("nom", joueurGagnant);
                        objNoeudCommande.appendChild(objNoeudARejointLeWinTheGame);
			
			Iterator it = lstJoueurs.values().iterator();
			while( it.hasNext() )
			{
				JoueurHumain joueur = (JoueurHumain)it.next();
				String nomUtilisateur = joueur.obtenirNomUtilisateur();
				int pointage = joueur.obtenirPartieCourante().obtenirPointage();
				
				Element objNoeudJoueur = objDocumentXML.createElement("joueur");
				objNoeudJoueur.setAttribute("utilisateur", nomUtilisateur);
				objNoeudJoueur.setAttribute("pointage", new Integer( pointage).toString());
				objNoeudParametre.appendChild( objNoeudJoueur );

				// Ajouter le noeud paramètre au noeud de commande
				objNoeudCommande.appendChild(objNoeudParametre);
			}
			
			if (lstJoueursVirtuels != null)
			{
				for (int i = 0; i < lstJoueursVirtuels.size(); i++)
				{
                                    JoueurVirtuel joueur = (JoueurVirtuel) lstJoueursVirtuels.get(i);
                                    String nomUtilisateur = joueur.obtenirNom();
                                    int pointage = joueur.obtenirPointage();

                                    Element objNoeudJoueur = objDocumentXML.createElement("joueur");
                                    objNoeudJoueur.setAttribute("utilisateur", nomUtilisateur);
                                    objNoeudJoueur.setAttribute("pointage", new Integer( pointage).toString());
				    objNoeudParametre.appendChild(objNoeudJoueur);

				    // Ajouter le noeud paramètre au noeud de commande
				    objNoeudCommande.appendChild(objNoeudParametre);
					
				}
			}
			
			// Ajouter le noeud de commande au noeud racine dans le document
			objDocumentXML.appendChild(objNoeudCommande);

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
		
            System.out.println(strCodeXML);
		return strCodeXML;
	}
}
