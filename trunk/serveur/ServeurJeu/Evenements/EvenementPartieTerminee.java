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
import org.w3c.dom.Text;

import java.util.TreeMap;
import java.util.Iterator;

import ClassesUtilitaires.UtilitaireXML;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;

/**
 * @author Marc
 *
 * 
 */
public class EvenementPartieTerminee  extends Evenement
{
	private TreeMap lstJoueurs;
	
	public EvenementPartieTerminee( TreeMap joueurs )
	{
		super();
		lstJoueurs = joueurs;
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
			
//			 Créer le noeud du paramètre
			Element objNoeudParametre = objDocumentXML.createElement("parametre");
			
			// On ajoute un attribut type qui va contenir le type
			// du paramètre
			objNoeudParametre.setAttribute("type", "StatistiqueJoueur");
			
			Iterator it = lstJoueurs.values().iterator();
			while( it.hasNext() )
			{
				JoueurHumain joueur = (JoueurHumain)it.next();
				String nomUtilisateur = joueur.obtenirNomUtilisateur();
				int pointage = joueur.obtenirPartieCourante().obtenirPointage();
			
				
				Element objNoeudJoueur = objDocumentXML.createElement("joueur");
				objNoeudJoueur.setAttribute("utilisateur", nomUtilisateur);
				objNoeudJoueur.setAttribute("pointage", new Integer( pointage).toString());
				
				/*Element objNoeudNom = objDocumentXML.createElement("utilisateur");
				Text objNoeudTexteNom = objDocumentXML.createTextNode( nomUtilisateur );
				objNoeudNom.appendChild( objNoeudTexteNom );
				
				Element objNoeudPoint = objDocumentXML.createElement("pointage");
				Text objNoeudTextePoint = objDocumentXML.createTextNode( new Integer( pointage).toString() );
				objNoeudPoint.appendChild( objNoeudTextePoint );*/
				
				objNoeudParametre.appendChild( objNoeudJoueur );

				// Ajouter le noeud paramètre au noeud de commande
				objNoeudCommande.appendChild(objNoeudParametre);
			}
			
			// Ajouter le noeud de commande au noeud racine dans le document
			objDocumentXML.appendChild(objNoeudCommande);

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
		
		return strCodeXML;
	}
}
