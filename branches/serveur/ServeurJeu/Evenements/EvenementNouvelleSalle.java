package ServeurJeu.Evenements;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import ClassesUtilitaires.UtilitaireXML;
import ServeurJeu.Configuration.GestionnaireMessages;

/**
 * @author Oloieri Lilian
 */

public class EvenementNouvelleSalle extends Evenement {

	// Cette variable va contenir le nom de la salle a declare
	private String strNomSalle;

	// Cette variable va si la salle est protege 
	private boolean hasPassword;
	
	// Cette variable va contenir le nom d'utilisateur du cr�ateur de cette salle
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
     * d'initialiser le num�ro de la salle. 
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
                   
    }
    
	/**
	 * Cette fonction permet de g�n�rer le code XML de l'�v�nement d'une 
	 * nouvelle salle et de le retourner.
	 * 
	 * @param InformationDestination information : Les informations � qui 
	 * 					envoyer l'�v�nement
	 * @return String : Le code XML de l'�v�nement � envoyer
	 */
	protected String genererCodeXML(InformationDestination information) {
		
		// D�claration d'une variable qui va contenir le code XML � retourner
	    String strCodeXML = "";
	    
		try
		{ // Appeler une fonction qui va cr�er un document XML dans lequel 
		    // on peut ajouter des noeuds
	        Document objDocumentXML = UtilitaireXML.obtenirDocumentXML();

			// Cr�er le noeud de commande � retourner
			Element objNoeudCommande = objDocumentXML.createElement("commande");
			
			// Cr�er les noeuds de param�tre
			Element objNoeudParametreNoSalle = objDocumentXML.createElement("parametre");
			Element objNoeudParametreNomSalle = objDocumentXML.createElement("parametre");
			Element objNoeudParametreProtegeeSalle = objDocumentXML.createElement("parametre");
			Element objNoeudParametreCreatorUserName = objDocumentXML.createElement("parametre");
			Element objNoeudParametreMasterTime = objDocumentXML.createElement("parametre");
			Element objNoeudParametreRoomDescriptions = objDocumentXML.createElement("parametre");
			Element objNoeudParametreRoomType = objDocumentXML.createElement("parametre");
			Element objNoeudParametreGameTypes = objDocumentXML.createElement("parametre");
			
			// Cr�er des noeuds contenant le num�ro de la table du noeud 
			// param�tre ainsi que le temps de la partie
			Text objNoeudTexteNoSalle = objDocumentXML.createTextNode(Integer.toString(roomID));
			Text objNoeudTexteNomSalle = objDocumentXML.createTextNode(strNomSalle);
			Text objNoeudTexteProtegeeSalle = objDocumentXML.createTextNode(Boolean.toString(hasPassword));
			Text objNoeudTexteCreatorUserName = objDocumentXML.createTextNode(strCreatorUserName);
			Text objNoeudTexteMasterTime = objDocumentXML.createTextNode(Integer.toString(masterTime));
			Text objNoeudTexteRoomDescriptions = objDocumentXML.createTextNode(roomDescription);
			Text objNoeudTexteRoomType = objDocumentXML.createTextNode(roomType);
			Text objNoeudTexteGameTypes = objDocumentXML.createTextNode(gameTypes);
			
			// D�finir les attributs du noeud de commande
			objNoeudCommande.setAttribute("no", Integer.toString(information.obtenirNoCommande()));
			objNoeudCommande.setAttribute("type", "Evenement");
			objNoeudCommande.setAttribute("nom", "NouvelleSalle");
						
			// On ajoute un attribut type qui va contenir le type
			// du param�tre
			objNoeudParametreNoSalle.setAttribute("type", "NoSalle");
			objNoeudParametreNomSalle.setAttribute("type", "NomSalle");
			objNoeudParametreProtegeeSalle.setAttribute("type", "ProtegeeSalle");
			objNoeudParametreCreatorUserName.setAttribute("type", "CreatorUserName");
			objNoeudParametreMasterTime.setAttribute("type", "MasterTime");
			objNoeudParametreRoomDescriptions.setAttribute("type", "RoomDescriptions");
			objNoeudParametreRoomType.setAttribute("type", "RoomType");
			objNoeudParametreGameTypes.setAttribute("type", "GameTypes");
			
			
			// Ajouter les noeuds texte aux noeuds de param�tre
			objNoeudParametreNoSalle.appendChild(objNoeudTexteNoSalle);
			objNoeudParametreNomSalle.appendChild(objNoeudTexteNomSalle);
			objNoeudParametreProtegeeSalle.appendChild(objNoeudTexteProtegeeSalle);
			objNoeudParametreCreatorUserName.appendChild(objNoeudTexteCreatorUserName);
			objNoeudParametreMasterTime.appendChild(objNoeudTexteMasterTime);
			objNoeudParametreRoomDescriptions.appendChild(objNoeudTexteRoomDescriptions);
			objNoeudParametreRoomType.appendChild(objNoeudTexteRoomType);
			objNoeudParametreGameTypes.appendChild(objNoeudTexteGameTypes);
			
			// Ajouter les noeuds param�tre au noeud de commande
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
