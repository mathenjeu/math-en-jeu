package ServeurJeu.Evenements;

import java.util.Vector;
import java.io.IOException;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * @author Jean-François Brind'Amour
 */
public abstract class Evenement
{
	// Déclaration d'une liste de InformationDestination
	protected Vector lstInformationDestination = new Vector();
	
	/**
	 * Cette fonction permet d'ajouter un nouveau InformationDestination à la
	 * liste d'InformationDestionation qui sert à savoir à qui envoyer 
	 * l'événement courant.
	 * 
	 * @param InformationDestionation information : Un objet contenant le numéro
	 * 						de commande ainsi que le ProtocoleJoueur du joueur
	 * 						à qui envoyer l'événement courant.
	 */
	public void ajouterInformationDestination(InformationDestination information)
	{
	    // Ajouter l'InformationDestination à la fin de la liste
		lstInformationDestination.add(information);
	}
	
	/**
	 * Cette fonction permet de générer le code XML de l'événement d'un nouveau
	 * joueur et de le retourner.
	 * 
	 * @param InformationDestination information : Les informations à qui 
	 * 					envoyer l'événement
	 * @return String : Le code XML de l'événement à envoyer
	 */
	protected abstract String genererCodeXML(InformationDestination information);
	
	/**
	 * Cette méthode permet d'envoyer l'événement courant à tous les joueurs
	 * se trouvant dans la liste des InformationDestination.
	 */
	public void envoyerEvenement()
	{
	    // Passer tous les InformationDestination se trouvant dans la liste de
	    // l'événement courant et envoyer à chacun l'événement courant
		
	    for (int i = 0; i < lstInformationDestination.size(); i++)
	    {
	        // Faire la référence vers l'objet InformationDestination courant
	        InformationDestination information = (InformationDestination) lstInformationDestination.get(i);
	        
	        try
	        {
                
                String strTemp = genererCodeXML(information);
		        
		        // Envoyer l'événement au joueur courant
		        information.obtenirProtocoleJoueur().envoyerMessage(strTemp);	            
   
	        }
	        catch (IOException ioe)
	        {
				//System.out.println("L'evenement courant n'a pas pu etre envoye");
	        }
	    }
	    
	}
}
