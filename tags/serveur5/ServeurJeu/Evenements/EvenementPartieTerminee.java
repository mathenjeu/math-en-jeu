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
import ServeurJeu.ComposantesJeu.Table;
import ServeurJeu.ComposantesJeu.Joueurs.StatisticsPlayer;
import ServeurJeu.Configuration.GestionnaireMessages;
import ServeurJeu.Monitoring.Moniteur;

/**
 * @author Marc
 *
 * 
 */
public class EvenementPartieTerminee  extends Evenement
{
	//private HashMap<String, JoueurHumain> lstJoueurs;
	//private ArrayList<JoueurVirtuel> lstJoueursVirtuels;
    private String joueurGagnant;
    private TreeSet<StatisticsPlayer> ourResults;
	
	public EvenementPartieTerminee( Table table, TreeSet<StatisticsPlayer> ourResults, String joueurGagnant)
	{
		super();
		//this.lstJoueurs = table.obtenirListeJoueurs();
		//lstJoueursVirtuels = table.obtenirListeJoueursVirtuels();
        this.joueurGagnant = joueurGagnant;
        this.ourResults = ourResults;
	}
	
	protected String genererCodeXML(InformationDestination information)
	{
		Moniteur.obtenirInstance().debut( "EvenementPartieTerminee.genererCodeXML" );
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
		
		Moniteur.obtenirInstance().fin();
        if(ControleurJeu.modeDebug) System.out.println("EvenementPartieTerminee: " + strCodeXML);
		return strCodeXML;
	}
}
