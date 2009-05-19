package ClassesUtilitaires;

import org.apache.log4j.Logger;
import java.io.FileWriter;
import java.util.Date;
import java.text.DateFormat;
import ServeurJeu.ControleurJeu;
import ServeurJeu.Communications.ProtocoleJoueur;
import ServeurJeu.ComposantesJeu.Salle;
import ServeurJeu.ComposantesJeu.Table;
import java.util.Vector;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurVirtuel;
import ServeurJeu.Configuration.GestionnaireMessages;

/**
 * L'espion qui écrit dans un fichier périodiquement les informations du serveur
 * @author Jean-François Fournier
 */
public class Espion implements Runnable{
	
	// Déclaration d'une constante contenant la version du serveur
	private String strVersion;
	
	// Déclaration d'une variable contenant le nombre de millisecondes entre
	// chaque mise à jour
	private int intDelaiMaj;
	
	// Déclaration de la variable contenant le nom du fichier espion
	private String strNomFichier;
	
	// Déclaration de l'objet logger qui permettra d'afficher des messages
	// d'erreurs dans le fichier log si nécessaire
	static private Logger objLogger = Logger.getLogger( Espion.class );
	
	// Déclaration d'une constante pour le mode "Rien" qui met l'espion au repos
	public static final int MODE_RIEN = 0;
	
	// Déclaration d'une constante pour le mode "Fichier texte" qui va mettre
	// le résultat de la mise à jour dans un fichier texte
	public static final int MODE_FICHIER_TEXTE = 1;

    // Cette variable contiendra le mode dans lequel l'espion est
    private int intModeEspion;
	
	// Déclaration d'un objet pour faire référence au Contrôleur de jeu
	private ControleurJeu objControleurJeu;
	
	// Déclaration d'une variable qui contiendra le séparateur de ligne
	private String strFinLigne;
	
	// Déclaration d'une variable qui nous permettra d'arrêter l'espion
	private boolean bolArreterEspion;
	
	/* Contructeur de la classe
	 * @param String nomFichier: Le fichier dans lequel l'espion écrira périodiquement
	 * 
	 * @param int delaiMaj: Le délai en millisecondes entre chaque maj
	 */
	public Espion(ControleurJeu controleur, String nomFichier, int delaiMaj, int mode)
	{
		String strArr[] = System.getProperty("java.class.path").split(":");
		strVersion = "Serveur: " + strArr[0];
		    
		intModeEspion = mode;
		objControleurJeu = controleur;
		strFinLigne = System.getProperty("line.separator");
		strNomFichier = nomFichier;
		intDelaiMaj = delaiMaj;
		bolArreterEspion = false;
	}


    /* Changer le mode de l'espion, on peut le mettre en mode RIEN, mais si
     * on veut l'arrêter pour de bon, on doit utiliser arreterEspion()
     */
    public void changerMode(int nouveauMode)
    {
    	intModeEspion = nouveauMode;
    }
    
    /*
     * changerDelaiMaj
     */
    public void changerDelaiMaj(int nouveauDelai)
    {
        intDelaiMaj = nouveauDelai;	
    }
    
    
    /* Arrêter l'espion pour de bon
     */
    public void arreterEspion()
    {
    	bolArreterEspion = true;
    }
    
    /*
     * Effectuer une mise à jour immédiate, on peut se servir de cette fonction
     * pour faire des mise à jour à certains moments précis.
     */
    public void faireMajImmediate()
    {
    	faireMaj();
    }
    
    /* Thread run, met à jour les informations du serveur périodiquement
     */
    public void run()
    {   
    
    	while (bolArreterEspion == false)
    	{
    	
	    	try
	    	{
	
                // Effectuer une mise à jour des informations
                faireMaj();

                // Bloquer la thread jusqu'à la prochaine mise à jour
               	Thread.sleep(intDelaiMaj);
	               	
	    	}
			catch( Exception e )
			{
				//System.out.println("Erreur dans la thread de l'espion.");
				//System.out.println(e.getMessage());
				objLogger.info(GestionnaireMessages.message("espion.erreur_thread"));
				objLogger.error( e.getMessage() );
				e.printStackTrace();
			}
		}
		
	    objLogger.info(GestionnaireMessages.message("espion.arrete"));
	}
    
    
    private void faireMaj()
    {
        switch(intModeEspion)
        {
        
        case MODE_RIEN: 
        
            // Ne rien faire
            break;
                
                        
        case MODE_FICHIER_TEXTE:   
        
            // Écrire dans le fichier texte
            traiterFichierTexte();
            break;

            
        }	
    }
    
    /* traiterFichierTexte
     * 
     * Cette fonction écrit dans le fichier strNomFichier les informations
     * du serveur.
     */
    private void traiterFichierTexte()
    {
    	
	    // Déclaration de la variable qui contiendra le résultat
	    // de l'espion pour cette mise à jour
	    StringBuilder strResultat = new StringBuilder();
	    
	    // Déclaration des différentes parties du résultat
	    StringBuilder strEntete = new StringBuilder();
	    StringBuilder strDerniereMAJ = new StringBuilder();
	    StringBuilder strJoueursConnectes = new StringBuilder();
	    StringBuilder strJoueursDeconnectes = new StringBuilder();
	    StringBuilder strSalles = new StringBuilder();
	    StringBuilder strTables = new StringBuilder();
	    
	    // Déclaration d'un objet qui contiendra une référence vers la liste des joueurs
	    Vector<ProtocoleJoueur> lstProtocoleJoueur = objControleurJeu.obtenirGestionnaireCommunication().obtenirListeProtocoleJoueur();
	    
		// Déclaration d'une liste de ProtocoleJoueur qui va contenir une copie
		Vector<ProtocoleJoueur> lstCopieProtocoleJoueur = null;
		
		// Déclaration d'un objet qui contiendra une référence vers la liste des joueurs déconnectés
		TreeMap<String,JoueurHumain> lstJoueursDeconnectes = objControleurJeu.obtenirListeJoueursDeconnectes();
		
		
		// Déclaration d'un objet qui contiendra une référence vers la liste des salles
		TreeMap<String, Salle> lstSalles = objControleurJeu.obtenirListeSalles("");
		
	    // Déclaration d'un objet qui contiendra une référence vers la liste des tables
	    // pour une certaine salle
	    TreeMap<Integer, Table> lstTables;
		
		// Déclaration d'un objet qui contiendra une référence vers la liste des joueurs
		// pour une table
		TreeMap<String, JoueurHumain> lstJoueurs;

        // Déclaration d'un objet qui contiendra une référence vers la liste des 
        // jouers connectés au serveur
        TreeMap<String, JoueurHumain> lstJoueursConnectes = objControleurJeu.obtenirListeJoueurs();		
		
		// Permet de calculer le nombre de connexion refusé
		int intConnexionRefusee = 0;
		
		
		// Empêcher d'autres threads de toucher à la liste des protocoles 
		// de joueur
		synchronized (lstProtocoleJoueur)
		{
			// Faire une copie de la liste des ProtocoleJoueur
			lstCopieProtocoleJoueur = (Vector<ProtocoleJoueur>) lstProtocoleJoueur.clone();
		}
		

	    // Entête 
	    // Ajouter la version du serveur
	    strEntete.append(strVersion);
		strEntete.append(strFinLigne);
	    
	    
	    // Dernière mise à jour
	    // Ajouter la date et l'heure de la dernière mise à jour
        Date objToday = new Date();
        String strDate;
        strDate = DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.MEDIUM).format(objToday);
        strDerniereMAJ.append("Dernière mise à jour: ");
        strDerniereMAJ.append(strDate);


	    // Joueurs connectés au gestionnaire de communication
	    // Ajouter le nombre de joueurs connectés
	    strJoueursConnectes.append("Joueurs connectés (protocole): ");
	    strJoueursConnectes.append(lstCopieProtocoleJoueur.size());
		strJoueursConnectes.append(strFinLigne);
		
        // S'il y a des joueurs connectés, on va ajouter leurs noms      
        if (lstCopieProtocoleJoueur.size() > 0)
        {
			// Passer tous les objets ProtocoleJoueur et ajouter les noms d'utilisateur
			// des joueurs
			for (int i = 0; i < lstCopieProtocoleJoueur.size(); i++)
			{

                ProtocoleJoueur objProtocoleJoueur = (ProtocoleJoueur) lstCopieProtocoleJoueur.get(i);
                
                if (objProtocoleJoueur != null)
                {
                	JoueurHumain objJoueurHumain = objProtocoleJoueur.obtenirJoueurHumain();
                	
                	// Le joueur humain peut être nul si la connexion est refusée 
                	if (objJoueurHumain != null)
                	{
						if (i == 0)
						{
        	                strJoueursConnectes.append("    ");	
						}
						
						if (i > 0 )
						{
							strJoueursConnectes.append(", ");
						}

						
						// Ajouter le nom du joueur humain
						strJoueursConnectes.append(objJoueurHumain.obtenirNomUtilisateur());
					}
					else
					{
						intConnexionRefusee++;
					}
				}

			}
			
	        strJoueursConnectes.append(strFinLigne);
        	
        }

        // Joueurs connectés au serveur
	    // Préparation pour parcourir le TreeMap des joueurs connectés
	    Set<Map.Entry<String,JoueurHumain>> lstEnsembleJoueursConnectes = lstJoueursConnectes.entrySet();
	    Iterator<Entry<String, JoueurHumain>> objIterateurListeJoueursConnectes = lstEnsembleJoueursConnectes.iterator();
	    
	    // Ajouter le nombre de joueurs connectés
	    strJoueursConnectes.append(strFinLigne);
	    strJoueursConnectes.append("Joueurs connectés (serveur): ");
	    strJoueursConnectes.append(lstEnsembleJoueursConnectes.size());
        
        // Afficher la liste des joueurs connectés
        if (lstEnsembleJoueursConnectes.size() > 0)
        {
        	strJoueursConnectes.append(strFinLigne);
        	strJoueursConnectes.append("    ");
        	
			// Passer tous les joueurs connectés et ajouter leur nom
			int intCompteur = 0;
			while (objIterateurListeJoueursConnectes.hasNext() == true)
			{
				if (intCompteur > 0)
				{
					strJoueursConnectes.append(", ");
				}
				
				strJoueursConnectes.append(((JoueurHumain)((Map.Entry<String,JoueurHumain>)objIterateurListeJoueursConnectes.next()).getValue()).obtenirNomUtilisateur());
			    intCompteur++;
			}
        }
        
        // S'il y a des connexions refusées, on les ajouter ici
        if (intConnexionRefusee > 0)
        {
        	strJoueursConnectes.append(strFinLigne);
        	strJoueursConnectes.append(strFinLigne);
        	strJoueursConnectes.append("Connexion refusée: ");
        	strJoueursConnectes.append(intConnexionRefusee);
        }
        
        
        // Joueurs déconnectés
        // Préparation pour parcourir le TreeMap des joueurs déconnectés
        Set<Map.Entry<String,JoueurHumain>> lstEnsembleJoueursDeconnectes = lstJoueursDeconnectes.entrySet();
        Iterator<Entry<String, JoueurHumain>> objIterateurListeJoueursDeconnectes = lstEnsembleJoueursDeconnectes.iterator();
        
        // Afficher le nombre de joueurs déconnectés
        strJoueursDeconnectes.append("Joueurs déconnectés: ");
        strJoueursDeconnectes.append(lstEnsembleJoueursDeconnectes.size());

        // Afficher la liste des joueurs déconnectés
        if (lstEnsembleJoueursDeconnectes.size() > 0)
        {
        	strJoueursDeconnectes.append(strFinLigne);
        	strJoueursDeconnectes.append("    ");
        	
			// Passer tous les joueurs déconnectés et ajouter leur nom
			int intCompteur = 0;
			while (objIterateurListeJoueursDeconnectes.hasNext() == true)
			{
				if (intCompteur > 0)
				{
					strJoueursDeconnectes.append(", ");
				}
				
				strJoueursDeconnectes.append(((JoueurHumain)((Map.Entry<String,JoueurHumain>)objIterateurListeJoueursDeconnectes.next()).getValue()).obtenirNomUtilisateur());
			    intCompteur++;
			}
        }

        
	    // Salles
        // Préparation pour parcourir le TreeMap des salles
        Set<Map.Entry<String,Salle>> lstEnsembleSalles = lstSalles.entrySet();
        Iterator<Entry<String, Salle>> objIterateurListeSalles = lstEnsembleSalles.iterator();	  
          
	    // Afficher le nombre de salles 
	    strSalles.append("Salles : ");
	    strSalles.append(lstEnsembleSalles.size());


        // Afficher la liste des salles     
	    if (lstEnsembleSalles.size() > 0)
	    {
        	strSalles.append(strFinLigne);
        	strSalles.append("    ");
        	
			// Passer toutes les salles et ajouter leur nom
			int intCompteur = 0;
			while (objIterateurListeSalles.hasNext() == true)
			{
				if (intCompteur > 0 )
				{
					strSalles.append(", ");
				}
				
				intCompteur++;
								
				// Ajouter le nom de la salle
				Salle objSalle = (Salle)(((Map.Entry<String,Salle>)(objIterateurListeSalles.next())).getValue());
				strSalles.append(objSalle.getRoomName(""));
			}	
	    }
	    
        // Préparation pour parcourir le TreeMap des salles
        lstEnsembleSalles = lstSalles.entrySet();
        objIterateurListeSalles = lstEnsembleSalles.iterator();	  
        
        
	    // Tables
	    // On va parcourir chaque salle, et pour chaque salle, on va afficher la
	    // liste des tables avec leur détails
	    if (lstEnsembleSalles.size() > 0)
	    {
        	
        	// Boucle du parcours de la liste des salles 
			while (objIterateurListeSalles.hasNext() == true)
			{
				// Aller chercher l'objet Salle
                Salle objSalle = (Salle)(((Map.Entry<String,Salle>)(objIterateurListeSalles.next())).getValue());				
				
				// Aller chercher la liste des tables
				lstTables = (TreeMap<Integer,Table>)(objSalle.obtenirListeTables());
				
				// Préparation pour parcourir le TreeMap des tables
				Set<Map.Entry<Integer,Table>> lstEnsembleTables = lstTables.entrySet();
				Iterator<Entry<Integer, Table>> objIterateurListeTables = lstEnsembleTables.iterator();

                // Ajouter le nom de la salle et le nombre de tables
         	    strTables.append("Tables pour la salle ");
         	    strTables.append(objSalle.getRoomName(""));
         	    strTables.append(" : ");
         	    strTables.append(lstTables.size());
         	    
         	    // Pour chaque table, ajouter les informations pour celle-ci
         	    if (lstEnsembleTables.size() > 0 )
         	    {
         	    	strTables.append(strFinLigne);
	         	    
	         	    // Boucle de parcours de la liste de tables pour la salle courante
	         	    while(objIterateurListeTables.hasNext() == true)
	         	    {
	         	    	 // Aller chercher l'objet Table
	         	    	 Table objTable = (Table)(((Map.Entry<Integer, Table>)(objIterateurListeTables.next())).getValue());	
	         	   	     
	         	   	     // Ajouter les informations sur cette table
	         	   	     strTables.append("    Numéro de table : ");
	         	   	     strTables.append(objTable.obtenirNoTable());
	         	   	     strTables.append(strFinLigne);
	         	   	     strTables.append("    Temps : ");
	         	   	     strTables.append(objTable.obtenirTempsTotal());
	         	   	     if (objTable.obtenirTempsTotal() <= 1)
	         	   	     {
	         	   	     	strTables.append(" minute");
	         	   	     }
	         	   	     else
	         	   	     {
	         	   	     	strTables.append(" minutes");
	         	   	     }
	         	   	     
	         	   	     strTables.append(strFinLigne);
	         	   	     strTables.append("    État : ");
	         	   	         
	         	   	     // Traiter l'état de la table
	         	   	     if (objTable.estCommencee() == false)
	         	   	     {
	         	   	     	strTables.append("En attente de joueurs");
	         	   	     }
	         	   	     else
	         	   	     {
	         	   	     	if (objTable.estArretee() == false)
	         	   	     	{
	         	   	     		strTables.append("Partie en cours");
	         	   	     	}
	         	   	     	else
	         	   	     	{
	         	   	     		strTables.append("Partie terminée");
	         	   	     	}
	         	   	     }
	         	   	     
	         	   	     strTables.append(strFinLigne);
	         	   	     
	         	   	     // Si une partie est en cours, ajouter le temps restant
	         	   	     if (objTable.estCommencee() == true && objTable.estArretee() == false)
	         	   	     {
	         	   	     	strTables.append("    Temps Restant : ");
	         	   	     	strTables.append(objTable.obtenirTempsRestant());
	         	   	     	if (objTable.obtenirTempsRestant() <= 1)
	         	   	     	{
	         	   	     		strTables.append(" seconde");
	         	   	     	}
	         	   	     	else
	         	   	     	{
	         	   	     		strTables.append(" secondes");
	         	   	     	}
	         	   	     		
	         	   	     	strTables.append(strFinLigne);
	         	   	     }
	         	   	     
	         	   	     // Obtenir la liste joueurs
	         	   	     lstJoueurs = objTable.obtenirListeJoueurs();
	         	   	     
	         	   	     // Préparation pour parcourir la liste des joueurs
				         Set<Map.Entry<String,JoueurHumain>> lstEnsembleJoueurs = lstJoueurs.entrySet();
				         Iterator<Entry<String, JoueurHumain>> objIterateurListeJoueurs = lstEnsembleJoueurs.iterator();
	         	   	     
	         	   	     // Ajouter le nom de chaque joueur sur la table
	         	   	     strTables.append("    Joueurs : ");
	         	   	     int intCompteur = 0;
	         	   	     while(objIterateurListeJoueurs.hasNext() == true)
	         	   	     {
	         	   	     	if (intCompteur > 0)
	         	   	     	{
	         	   	     		strTables.append(", ");
	         	   	     	}
	         	   	     	intCompteur++;
	         	   	     	
	         	   	     	// Aller chercher l'objet JoueurHumain
	         	   	     	JoueurHumain objJoueurHumain = (JoueurHumain)(((Map.Entry<String,JoueurHumain>)(objIterateurListeJoueurs.next())).getValue());
	         	   	     	
	         	   	     	// Ajouter le nom d'utilisateur du joueur
	         	   	     	strTables.append(objJoueurHumain.obtenirNomUtilisateur());

                         }
                         
                         // Obtenir la liste des joueurs virtuels
                         if (objTable.obtenirListeJoueursVirtuels() != null)
                         {
                             Vector<JoueurVirtuel> lstJoueursVirtuels = objTable.obtenirListeJoueursVirtuels();
                             if (intCompteur > 0)
                             {
                             	strTables.append(", ");
                             }
                             
                             for (int i = 0; i < lstJoueursVirtuels.size(); i++)
                             {
                                 
                             	 if (i > 0)
                             	 {
                             	     strTables.append(", ");
                             	 }
                             	 
                                 JoueurVirtuel objJoueurVirtuel = (JoueurVirtuel) lstJoueursVirtuels.get(i);
                                 strTables.append(objJoueurVirtuel.obtenirNom());
                             }   
                         }
                         
                         // Fin de la liste des joueurs                         
                         strTables.append(strFinLigne);

                         // Obtenir la liste des joueurs déconnectés
                         if (objTable.obtenirListeJoueursDeconnectes() != null && 
                             objTable.obtenirListeJoueursDeconnectes().size() > 0)
                         {
                             strTables.append("    Joueurs déconnectés : ");
                             
	                         for (int i = 0; i < objTable.obtenirListeJoueursDeconnectes().size(); i++)
	                         {
	                         	if (i > 0)
	                         	{
	                         		strTables.append(", ");
	                         	}
	                         	
	                         	strTables.append((String) objTable.obtenirListeJoueursDeconnectes().get(i));
	                         }
	                         
                             strTables.append(strFinLigne);
                         }

                         
                         // Ligne vide entre chaque block de table
                         strTables.append(strFinLigne);
	         	   	     
	         	    }
         	    }
         	       

			}	
	    }
	    
    	// Concatener chaque partie pour former le résultat final
    	strResultat.append(strEntete);
    	strResultat.append(strFinLigne);
    	strResultat.append(strDerniereMAJ);
    	strResultat.append(strFinLigne);
    	strResultat.append(strFinLigne);
    	strResultat.append(strJoueursConnectes);
    	strResultat.append(strFinLigne);
    	strResultat.append(strFinLigne);
        strResultat.append(strJoueursDeconnectes);
    	strResultat.append(strFinLigne);
    	strResultat.append(strFinLigne);
    	strResultat.append(strSalles);
    	strResultat.append(strFinLigne);
    	strResultat.append(strFinLigne);
    	strResultat.append(strTables);
    	strResultat.append(strFinLigne);
    	    
    	try
    	{
    		// Écrire dans le fichier
			FileWriter writer = new FileWriter(strNomFichier);
			writer.write(strResultat.toString());
			//writer.write(strFinLigne + "(" + writer.getEncoding() + ")");
			writer.close();

	    }
	    catch( Exception e)
	    {
			//System.out.println("Erreur d'écriture dans le fichier espion.");
		    objLogger.info(GestionnaireMessages.message("espion.erreur_fichier"));
			objLogger.error(e.getMessage());
	    }
	
    }
}
