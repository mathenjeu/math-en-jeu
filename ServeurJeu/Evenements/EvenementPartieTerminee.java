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
import java.util.TreeSet;
import ClassesUtilitaires.UtilitaireXML;
import ServeurJeu.ControleurJeu;
import ServeurJeu.ComposantesJeu.Joueurs.StatisticsPlayer;
import ServeurJeu.ComposantesJeu.Tables.Table;
import ServeurJeu.Configuration.GestionnaireMessages;

/**
 * @author Marc
 *
 * 
 */
public class EvenementPartieTerminee  extends Evenement
{
	private String joueurGagnant;
    private TreeSet<StatisticsPlayer> ourResults;
    private String messxml;
	
	public EvenementPartieTerminee( Table table, TreeSet<StatisticsPlayer> ourResults, String joueurGagnant)
	{
        this.joueurGagnant = joueurGagnant;
        this.ourResults = ourResults;
        messxml = "";
        generateString();
	}
	
	protected String genererCodeXML(InformationDestination information)
	{
		return messxml;
	}
	
	private void generateString()
	{
		try
		{
	        // Appeler une fonction qui va créer un document XML dans lequel 
		    // on peut ajouter des noeuds
	        Document objDocumentXML = UtilitaireXML.obtenirDocumentXML();

			// Créer le noeud de commande à retourner
			Element objNoeudCommande = objDocumentXML.createElement("commande");
			
			// Créer un noeud contenant le nom d'utilisateur du noeud paramètre
			
			// Définir les attributs du noeud de commande
			objNoeudCommande.setAttribute("no", Integer.toString(0));
			objNoeudCommande.setAttribute("type", "Evenement");
			objNoeudCommande.setAttribute("nom", "PartieTerminee");
			
                        // Créer le noeud du paramètre
			Element objNoeudParametre = objDocumentXML.createElement("parametre");
			objNoeudParametre.setAttribute("type", "StatistiqueJoueur");
                        
                        //Element objNoeudARejointLeWinTheGame = objDocumentXML.createElement("joueurWinTheGame");
                        //objNoeudARejointLeWinTheGame.setAttribute("nom", joueurGagnant);
                        //objNoeudCommande.appendChild(objNoeudARejointLeWinTheGame);
			
			int i = ourResults.size();
			for(StatisticsPlayer s: ourResults)
			{
				
				String nomUtilisateur = s.getUsername();
				int pointage = s.getPoints();
								
				Element objNoeudJoueur = objDocumentXML.createElement("joueur");
				objNoeudJoueur.setAttribute("utilisateur", nomUtilisateur);
				objNoeudJoueur.setAttribute("pointage", new Integer( pointage).toString());
				objNoeudJoueur.setAttribute("position", new Integer(i).toString());
				
				objNoeudParametre.appendChild( objNoeudJoueur );

				// Ajouter le noeud paramètre au noeud de commande
				objNoeudCommande.appendChild(objNoeudParametre);
				
				System.out.println("Stats : " + nomUtilisateur + " " + pointage + " " + i);
				
				i--;
			}
						
			// Ajouter le noeud de commande au noeud racine dans le document
			objDocumentXML.appendChild(objNoeudCommande);

			// Transformer le document XML en code XML
			messxml = UtilitaireXML.transformerDocumentXMLEnString(objDocumentXML);
		}
		catch (TransformerConfigurationException tce)
		{
			System.out.println(GestionnaireMessages.message("evenement.XML_transformation"));
		}
		catch (TransformerException te)
		{
			System.out.println(GestionnaireMessages.message("evenement.XML_conversion"));
		}
		
		if(ControleurJeu.modeDebug) System.out.println("EvenementPartieTerminee: " + messxml);	
	}
}
