import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * Used to create XML file with list of questions and them properties
 * for QuestionsViewer needs. 
 * @author Oloieri Lilian
 *
 */

public class XMLWriter {

	// D�claration d'un document XML
    private Document objDocumentXML;
    private String xmlString;
    private Element rootElement;
    private SmacUI ui;
    
    // Constructor
	public XMLWriter(SmacUI ui)
	{
		this.ui = ui;
	}
	
	/**
	 * init the xml and construct the root element - questions 
	 * late will add the question elements to the root in the separate method
	 * called after each flash created 
	 */
	public void initXMLDoc()
    {
    	 try
         {
     		// Cr�er une nouvelle instance de DocumentBuilderFactory qui va permettre
     		// de cr�er un document XML dans lequel on va pouvoir �crire et lire des
     		// cha�nes de caract�res format�es en XML
     		DocumentBuilderFactory objXMLFactory = DocumentBuilderFactory.newInstance();
     		
     		// D�finir les propri�t�s de la factory
     		objXMLFactory.setValidating(false);
     		objXMLFactory.setIgnoringComments(true);
     		objXMLFactory.setIgnoringElementContentWhitespace(false);
     		objXMLFactory.setExpandEntityReferences(true);
     		
     		// Cr�er un nouveau DocumentBuilder qui va permettre de cr�er un 
     		// document XML
     		DocumentBuilder objCreateurDocumentXML = objXMLFactory.newDocumentBuilder();
     					
     		// Cr�er un nouveau Document avec le cr�ateur de document XML 
     		// qui va contenir le code XML � retourner au client
     		objDocumentXML = objCreateurDocumentXML.newDocument();
     		
     	    // Cr�er root element du XML
     		rootElement = objDocumentXML.createElement("questions");
     		objDocumentXML.appendChild(rootElement);
         }
 		catch (ParserConfigurationException pce)
 		{
 			ui.outputMessage("erreur_transformation xml...");
 		}
 		
    }
	
	/**
	 * If the questions.xml existe, take it and init the doc xml by him
	 * @param flashFolderName
	 */
	public void getOldXml(String flashFolderName) {
		
		 try
         {
     		// Cr�er une nouvelle instance de DocumentBuilderFactory qui va permettre
     		// de cr�er un document XML dans lequel on va pouvoir �crire et lire des
     		// cha�nes de caract�res format�es en XML
     		DocumentBuilderFactory objXMLFactory = DocumentBuilderFactory.newInstance();
     		
     		// D�finir les propri�t�s de la factory
     		objXMLFactory.setValidating(false);
     		objXMLFactory.setIgnoringComments(true);
     		objXMLFactory.setIgnoringElementContentWhitespace(false);
     		objXMLFactory.setExpandEntityReferences(true);
     		
     		// Cr�er un nouveau DocumentBuilder qui va permettre de cr�er un 
     		// document XML
     		DocumentBuilder objCreateurDocumentXML = objXMLFactory.newDocumentBuilder();
     					
     		// Cr�er un nouveau Document avec le cr�ateur de document XML 
     		// qui va contenir le code XML � retourner au client
     		objDocumentXML = objCreateurDocumentXML.parse(new File(flashFolderName + "questions.xml"));;
     		
     	    // Cr�er root element du XML
     		rootElement = objDocumentXML.getDocumentElement();
     		//objDocumentXML.appendChild(rootElement);
         }
 		catch (ParserConfigurationException pce)
 		{
 			ui.outputMessage("erreur_transformation ..." + pce.getMessage());
 		}
 		catch(IOException ioe)
 		{
 			ui.outputMessage("erreur_transformation ..." + ioe.getMessage());
 		}
 		catch(SAXException saxe)
 		{
 			ui.outputMessage("erreur_transformation ..." + saxe.getMessage());
 		} 
		
		
		
	}
    
	
	/**
	 * Add the question element to the xml root after each flash created
	 * @param nameString
	 * @param idString
	 * @param langueString
	 */
    
    public void addQuestions(String nameString, int idQ, String langueString)
    {
    	//Create the question element
    	Element question = objDocumentXML.createElement("question");
    	rootElement.appendChild(question);
    	
    	// Create name element
    	Element name = objDocumentXML.createElement("name");
    	name.appendChild(objDocumentXML.createTextNode(nameString));
    	question.appendChild(name);
    	
    	// Create id element
    	Element id = objDocumentXML.createElement("id");
    	id.appendChild(objDocumentXML.createTextNode(Integer.toString(idQ)));
    	question.appendChild(id);
    	
    	// Create language element
    	Element lang = objDocumentXML.createElement("lang");
    	lang.appendChild(objDocumentXML.createTextNode(langueString));
    	question.appendChild(lang);
    	
    }
    
    /**
     * Write the final xml containing all the questions in xml file
     * will be executed after all flash's created
     * @param flashFolder
     */
    public void writeXmlFile(String flashFolder)
    {
    	try{
    		//write the content into xml file
    		TransformerFactory transformerFactory = TransformerFactory.newInstance();
    		Transformer transformer = transformerFactory.newTransformer();
    		DOMSource source = new DOMSource(objDocumentXML);
    		StreamResult result =  new StreamResult(new File(flashFolder + "questions.xml"));
    		transformer.transform(source, result);
    	}catch(TransformerException tfe){
    		ui.outputMessage(tfe.getMessage());
    	}
    }
    
    /**
     * Verify if the question with question_id = qid existe in the xml doc
     * @param qid
     * @return
     */
	public boolean verifyQuestion(int qid) {
	   NodeList questions = rootElement.getChildNodes();
	   for(int i = 0; i < questions.getLength(); i++)
	   {
	      Node question = questions.item(i);
	      Node idNode = question.getFirstChild().getNextSibling();
	      String idValue = idNode.getNodeValue();
	      if(Integer.getInteger(idValue) == qid)
	    	  return false;
	   }
		return true;
	}// end method

	
} // end class
