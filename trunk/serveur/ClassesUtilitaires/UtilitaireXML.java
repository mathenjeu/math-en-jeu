package ClassesUtilitaires;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import ServeurJeu.Configuration.GestionnaireMessages;

/**
 * @author Jean-François Brind'Amour
 */
public final class UtilitaireXML 
{
	/**
	 * Constructeur par défaut est privé pour empêcher de pourvoir créer des 
	 * instances de cette classe.
	 */
    private UtilitaireXML() {}
    
    /**
     * Cette fonction permet de créer et de retourner un document XML vide.
     * 
     * @return Document : Un document XML vide permettant j'ajouter des noeuds
     */
    public static Document obtenirDocumentXML()
    {
        // Déclaration d'un document XML
        Document objDocumentXML = null;
        
        try
        {
    		// Créer une nouvelle instance de DocumentBuilderFactory qui va permettre
    		// de créer un document XML dans lequel on va pouvoir écrire et lire des
    		// chaînes de caractères formatées en XML
    		DocumentBuilderFactory objXMLFactory = DocumentBuilderFactory.newInstance();
    		
    		// Définir les propriétés de la factory
    		objXMLFactory.setValidating(false);
    		objXMLFactory.setIgnoringComments(false);
    		objXMLFactory.setIgnoringElementContentWhitespace(false);
    		objXMLFactory.setExpandEntityReferences(true);
    		
    		// Créer un nouveau DocumentBuilder qui va permettre de créer un 
    		// document XML
    		DocumentBuilder objCreateurDocumentXML = objXMLFactory.newDocumentBuilder();
    					
    		// Créer un nouveau Document avec le créateur de document XML 
    		// qui va contenir le code XML à retourner au client
    		objDocumentXML = objCreateurDocumentXML.newDocument();
        }
		catch (ParserConfigurationException pce)
		{
			System.out.println(GestionnaireMessages.message("erreur_transformation"));
		}
		
		return objDocumentXML;
    }
    
    /**
     * Cette fonction permet de créer et de retourner un document XML contenant
     * le code XML passé en paramètres.
     * 
     * @return Document : Un document XML contenant le code XML passé en 
     * 					  paramètres
     */
    public static Document obtenirDocumentXML(String codeXML)
    {
        // Déclaration d'un document XML
        Document objDocumentXML = null;
        
        try
        {
    		// Créer une nouvelle instance de DocumentBuilderFactory qui va permettre
    		// de créer un document XML dans lequel on va pouvoir écrire et lire des
    		// chaînes de caractères formatées en XML
    		DocumentBuilderFactory objXMLFactory = DocumentBuilderFactory.newInstance();
    		
    		// Définir les propriétés de la factory
    		objXMLFactory.setValidating(false);
    		objXMLFactory.setIgnoringComments(false);
    		objXMLFactory.setIgnoringElementContentWhitespace(false);
    		objXMLFactory.setExpandEntityReferences(true);
    		
    		// Créer un nouveau DocumentBuilder qui va permettre de créer un 
    		// document XML
    		DocumentBuilder objCreateurDocumentXML = objXMLFactory.newDocumentBuilder();
    					
    		// Créer un nouveau Document avec le créateur de document XML 
    		// qui va contenir le code XML à retourner au client
    		objDocumentXML = objCreateurDocumentXML.parse(new InputSource(new StringReader(codeXML.trim())));
        }
		catch (ParserConfigurationException pce)
		{
			System.out.println(GestionnaireMessages.message("erreur_transformation"));
		}
		catch (IOException ioe)
		{
			System.out.println(GestionnaireMessages.message("erreur_io"));
		}
		catch (SAXException saxe)
		{
			System.out.println(GestionnaireMessages.message("erreur_sax"));
		}
		
		return objDocumentXML;
    }

	/**
	 * Cette fonction permet de transformer un document XML passé en paramètres
	 * en une chaîne de caractères XML.
	 * 
	 * @param Document documentXML : le document à convertir en chaîne de 
	 * 								 caractères
	 * @return String : le document XML converti en une chaîne de caractères XML
	 * @throws TransformerConfigurationException : S'il y a une erreur lors de la
	 * 											   conversion d'un document XML en
	 * 											   une chaîne de caractères
	 * @throws TransformerException : S'il y a une erreur conversion entre XML et
	 *								  une chaîne de caractères
	 */
	public static String transformerDocumentXMLEnString(Document documentXML) throws TransformerConfigurationException,
																			   		 TransformerException
	{
		// Créer une factory qui va permettre de créer un transformeur de 
		// document XML en chaîne de caractères, puis créer le transformateur
		TransformerFactory objTransformeurXMLFactory = TransformerFactory.newInstance();
		Transformer objTransformeurXML = objTransformeurXMLFactory.newTransformer();
        
        // Créer un objet qui va permettre d'écrire le document XML sous forme 
		// de chaîne de caractères
        StringWriter objEcrivainChaine = new StringWriter();
		
        // Transformer le document XML en une chaîne de caractères
        objTransformeurXML.transform(new DOMSource(documentXML), 
        							 new StreamResult(objEcrivainChaine));
        
        // Retourner le document XML ne contenant pas l'entête XML ajoutée 
        // par défaut par le transformateur
		return retirerEnteteXML(objEcrivainChaine.toString());
	}
	
	/**
	 * Cette fonction permet de retourner la chaîne de caractère représentant le
	 * code XML à retourner au client ne contenant pas l'entête XML commençant par
	 * <? et terminant par ?>. Si aucune entête n'est trouvée, alors on retourne
	 * le message originel. On suppose que l'entête se trouve toujours au début
	 * du message.
	 * 
	 * @param String message : le code XML contenant l'entête à enlever
	 * @return String : le code XML du message à retourner ne contenant pas 
	 *                  l'entête XML
	 */
	private static String retirerEnteteXML(String message)
	{
		// Si le message ne contient pas les balises d'une entête XML, alors
		// on retourne le message directement, sinon on l'enlève
		if (message.contains("<?") == false || message.contains("?>") == false)
		{
			return message;		
		}
		else
		{
			// On retourne le message sans l'entête XML
			return (message.substring(message.indexOf("?>") + 2));
		}
	}
}
