package ServeurJeu.Evenements;

import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * @author Oloieri Lilian
 */

public class EvenementNouvelleSalle extends Evenement {

	// Cette variable va contenir le nom de la salle a declare
	private String strNomSalle;

	// Cette variable va si la salle est protege 
	private boolean hasPassword;

	// Cette variable va contenir le nom d'utilisateur du créateur de cette salle
	private String strCreatorUserName;

	//Room short description
	private String roomDescription;

	// ID in DB.  
	private int roomID;

	//default time for the room 
	private int masterTime;

	// General or profsType
	private String roomType;

	private String gameTypes;

	/**
	 * Constructeur de la classe EvenementNouvelleSalle qui permet 
	 * d'initialiser le numéro de la salle. 
	 */
	public EvenementNouvelleSalle(String roomName, boolean protegee, String strCreatorUserName,  
			String roomDescription, int masterTime, int roomID, String roomType, String gameTypes)
	{
		strNomSalle = roomName;
		hasPassword = protegee;
		this.strCreatorUserName = strCreatorUserName;
		this.roomDescription = roomDescription;
		this.roomID = roomID;
		this.masterTime = masterTime;
		this.roomType = roomType;
		this.gameTypes = gameTypes;

		generateXML();                   
	}

	private void generateXML()
	{
		// Créer le noeud de commande à retourner
		Element objNoeudCommande = objDocumentXML.createElement("commande");

		// Créer les noeuds de paramètre
		Element objNoeudParametreNoSalle = objDocumentXML.createElement("parametre");
		Element objNoeudParametreNomSalle = objDocumentXML.createElement("parametre");
		Element objNoeudParametreProtegeeSalle = objDocumentXML.createElement("parametre");
		Element objNoeudParametreCreatorUserName = objDocumentXML.createElement("parametre");
		Element objNoeudParametreMasterTime = objDocumentXML.createElement("parametre");
		Element objNoeudParametreRoomDescriptions = objDocumentXML.createElement("parametre");
		Element objNoeudParametreRoomType = objDocumentXML.createElement("parametre");
		Element objNoeudParametreGameTypes = objDocumentXML.createElement("parametre");

		// Créer des noeuds contenant le numéro de la table du noeud 
		// paramètre ainsi que le temps de la partie
		Text objNoeudTexteNoSalle = objDocumentXML.createTextNode(Integer.toString(roomID));
		Text objNoeudTexteNomSalle = objDocumentXML.createTextNode(strNomSalle);
		Text objNoeudTexteProtegeeSalle = objDocumentXML.createTextNode(Boolean.toString(hasPassword));
		Text objNoeudTexteCreatorUserName = objDocumentXML.createTextNode(strCreatorUserName);
		Text objNoeudTexteMasterTime = objDocumentXML.createTextNode(Integer.toString(masterTime));
		Text objNoeudTexteRoomDescriptions = objDocumentXML.createTextNode(roomDescription);
		Text objNoeudTexteRoomType = objDocumentXML.createTextNode(roomType);
		Text objNoeudTexteGameTypes = objDocumentXML.createTextNode(gameTypes);

		// Définir les attributs du noeud de commande
		objNoeudCommande.setAttribute("type", "Evenement");
		objNoeudCommande.setAttribute("nom", "NouvelleSalle");

		// On ajoute un attribut type qui va contenir le type
		// du paramètre
		objNoeudParametreNoSalle.setAttribute("type", "NoSalle");
		objNoeudParametreNomSalle.setAttribute("type", "NomSalle");
		objNoeudParametreProtegeeSalle.setAttribute("type", "ProtegeeSalle");
		objNoeudParametreCreatorUserName.setAttribute("type", "CreatorUserName");
		objNoeudParametreMasterTime.setAttribute("type", "MasterTime");
		objNoeudParametreRoomDescriptions.setAttribute("type", "RoomDescriptions");
		objNoeudParametreRoomType.setAttribute("type", "RoomType");
		objNoeudParametreGameTypes.setAttribute("type", "GameTypes");


		// Ajouter les noeuds texte aux noeuds de paramètre
		objNoeudParametreNoSalle.appendChild(objNoeudTexteNoSalle);
		objNoeudParametreNomSalle.appendChild(objNoeudTexteNomSalle);
		objNoeudParametreProtegeeSalle.appendChild(objNoeudTexteProtegeeSalle);
		objNoeudParametreCreatorUserName.appendChild(objNoeudTexteCreatorUserName);
		objNoeudParametreMasterTime.appendChild(objNoeudTexteMasterTime);
		objNoeudParametreRoomDescriptions.appendChild(objNoeudTexteRoomDescriptions);
		objNoeudParametreRoomType.appendChild(objNoeudTexteRoomType);
		objNoeudParametreGameTypes.appendChild(objNoeudTexteGameTypes);

		// Ajouter les noeuds paramètre au noeud de commande
		objNoeudCommande.appendChild(objNoeudParametreNoSalle);
		objNoeudCommande.appendChild(objNoeudParametreNomSalle);
		objNoeudCommande.appendChild(objNoeudParametreProtegeeSalle);
		objNoeudCommande.appendChild(objNoeudParametreCreatorUserName);
		objNoeudCommande.appendChild(objNoeudParametreMasterTime);
		objNoeudCommande.appendChild(objNoeudParametreRoomDescriptions);
		objNoeudCommande.appendChild(objNoeudParametreRoomType);
		objNoeudCommande.appendChild(objNoeudParametreGameTypes);

		// Ajouter le noeud de commande au noeud racine dans le document
		objDocumentXML.appendChild(objNoeudCommande);

	}
}
