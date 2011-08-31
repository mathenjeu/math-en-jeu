package ServeurJeu.Evenements;

import java.util.ArrayList;

/**
 * @author Jean-Fran�ois Brind'Amour
 */
public class Evenement
{
	// D�claration d'une liste de InformationDestination
	protected ArrayList<InformationDestination> lstInformationDestination;// = new ArrayList<InformationDestination>();

	private String messageXml;

	public Evenement()
	{
		lstInformationDestination = new ArrayList<InformationDestination>();
		messageXml = "";
	}

	/**
	 * Cette fonction permet d'ajouter un nouveau InformationDestination � la
	 * liste d'InformationDestionation qui sert � savoir � qui envoyer 
	 * l'�v�nement courant.
	 * 
	 * @param InformationDestionation information : Un objet contenant le num�ro
	 * 						de commande ainsi que le ProtocoleJoueur du joueur
	 * 						� qui envoyer l'�v�nement courant.
	 */
	public void ajouterInformationDestination(InformationDestination information)
	{
		// Ajouter l'InformationDestination � la fin de la liste
		lstInformationDestination.add(information);		
	}

	/**
	 * Cette fonction permet de g�n�rer le code XML de l'�v�nement d'un nouveau
	 * joueur et de le retourner.
	 * 
	 * @param InformationDestination information : Les informations � qui 
	 * 					envoyer l'�v�nement
	 * @return String : Le code XML de l'�v�nement � envoyer
	 */
	protected  String genererCodeXML(InformationDestination information)
	{
		return messageXml;		
	}

	public void addXML(String messXML)
	{
		messageXml = messXML;
	}

	/**
	 * Cette m�thode permet d'envoyer l'�v�nement courant � tous les joueurs
	 * se trouvant dans la liste des InformationDestination.
	 */
	public void envoyerEvenement()
	{
		// Passer tous les InformationDestination se trouvant dans la liste de
		// l'�v�nement courant et envoyer � chacun l'�v�nement courant

		for (int i = 0; i < lstInformationDestination.size(); i++)
		{
			// Faire la r�f�rence vers l'objet InformationDestination courant
			InformationDestination information =  lstInformationDestination.get(i);    



			// Envoyer l'�v�nement au joueur courant
			if(messageXml.equals(""))
			{
				String strTemp = genererCodeXML(information);
				information.obtenirProtocoleJoueur().envoyerMessage(strTemp);
			}
			else
				information.obtenirProtocoleJoueur().envoyerMessage(messageXml);

		}

	}
}
