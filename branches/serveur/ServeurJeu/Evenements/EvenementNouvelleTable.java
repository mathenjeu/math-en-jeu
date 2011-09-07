package ServeurJeu.Evenements;

import org.w3c.dom.Element;
import org.w3c.dom.Text;
import ServeurJeu.ComposantesJeu.Tables.Table;

/**
 * @author Jean-François Brind'Amour
 */
public class EvenementNouvelleTable extends Evenement
{
	// Déclaration d'une variable qui va garder le numéro de la table qui a 
	// été créée
	private final int intNoTable;

	// Déclaration d'une variable qui va permettre de garder le temps de la partie
	private final int intTempsPartie;

	// Variable for the table name
	private final String tableName;

	// Variable for the maximal number of players in the table
	private final int maxNbPlayers;

	private final String gameType;

	/**
	 * Constructeur de la classe EvenementNouvelleTable qui permet 
	 * d'initialiser le numéro de la table. 
	 */
	public EvenementNouvelleTable(Table table)
	{
		// Définir le numéro de la table qui a été créée et le temps de la partie
		this.intNoTable = table.obtenirNoTable();
		this.intTempsPartie = table.obtenirTempsTotal();
		this.tableName = table.getTableName();
		this.maxNbPlayers = table.getMaxNbPlayers();
		this.gameType = table.getGameType().toString();
		generateXML();
	}


	private void generateXML()
	{
		// Créer le noeud de commande à retourner
		Element objNoeudCommande = objDocumentXML.createElement("commande");

		// Créer les noeuds de paramètre
		Element objNoeudParametreNoTable = objDocumentXML.createElement("parametre");
		Element objNoeudParametreTempsPartie = objDocumentXML.createElement("parametre");
		Element objNoeudParametreNomPartie = objDocumentXML.createElement("parametre");
		Element objNoeudParametreMaxNbPlayers = objDocumentXML.createElement("parametre");
		Element objNoeudParametreGameType = objDocumentXML.createElement("parametre");

		// Créer des noeuds contenant le numéro de la table du noeud 
		// paramètre ainsi que le temps de la partie
		Text objNoeudTexteNoTable = objDocumentXML.createTextNode(Integer.toString(intNoTable));
		Text objNoeudTexteTempsPartie = objDocumentXML.createTextNode(Integer.toString(intTempsPartie));
		Text objNoeudTexteNomPartie = objDocumentXML.createTextNode(tableName);
		Text objNoeudTexteMaxNbPlayers = objDocumentXML.createTextNode(Integer.toString(maxNbPlayers));
		Text objNoeudTexteGameType = objDocumentXML.createTextNode(gameType);

		// Définir les attributs du noeud de commande
		objNoeudCommande.setAttribute("type", "Evenement");
		objNoeudCommande.setAttribute("nom", "NouvelleTable");

		// On ajoute un attribut type qui va contenir le type
		// du paramètre
		objNoeudParametreNoTable.setAttribute("type", "No");
		objNoeudParametreTempsPartie.setAttribute("type", "Temps");
		objNoeudParametreNomPartie.setAttribute("type", "TablName");
		objNoeudParametreMaxNbPlayers.setAttribute("type", "MaxNbPlayers");
		objNoeudParametreGameType.setAttribute("type", "gameType");

		// Ajouter les noeuds texte aux noeuds de paramètre
		objNoeudParametreNoTable.appendChild(objNoeudTexteNoTable);
		objNoeudParametreTempsPartie.appendChild(objNoeudTexteTempsPartie);
		objNoeudParametreNomPartie.appendChild(objNoeudTexteNomPartie);
		objNoeudParametreMaxNbPlayers.appendChild(objNoeudTexteMaxNbPlayers);
		objNoeudParametreGameType.appendChild(objNoeudTexteGameType);

		// Ajouter les noeuds paramètre au noeud de commande
		objNoeudCommande.appendChild(objNoeudParametreNoTable);
		objNoeudCommande.appendChild(objNoeudParametreTempsPartie);
		objNoeudCommande.appendChild(objNoeudParametreNomPartie);
		objNoeudCommande.appendChild(objNoeudParametreMaxNbPlayers);
		objNoeudCommande.appendChild(objNoeudParametreGameType);

		// Ajouter le noeud de commande au noeud racine dans le document
		objDocumentXML.appendChild(objNoeudCommande);
	}
}
