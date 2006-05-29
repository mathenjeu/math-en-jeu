package ClassesUtilitaires;

import org.apache.log4j.Logger;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.text.DateFormat;
import ServeurJeu.ControleurJeu;
import ServeurJeu.Communications.GestionnaireCommunication;
import ServeurJeu.Communications.ProtocoleJoueur;
import ServeurJeu.ComposantesJeu.Salle;
import ServeurJeu.ComposantesJeu.Table;
import java.util.Vector;
import java.lang.System;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import ServeurJeu.Temps.Minuterie;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;



public class Espion implements Runnable{
	
	// D�claration d'une constante contenant ls version de l'espion
	private final String VERSION = "Espion Math-En-Jeu Version 1.0";
	
	// D�claraction d'une variable contenant le nombre de millisecondes entre
	// chaque mise � jour. 
	private int intDelaiMaj;
	
	// D�claration de la variable contenant le nom du fichier espion
	private String strNomFichier;
	
	// D�claration de l'objet logger qui permettra d'afficher des messages
	// d'erreurs dans le fichier log si n�cessaire
	static private Logger objLogger = Logger.getLogger( Espion.class );
	
	// D�claration d'une constante pour le mode "Rien" qui met l'espion au repos
	public static final int MODE_RIEN = 0;
	
	// D�claration d'une constante pour le mode "Fichier texte" qui va mettre
	// le r�sultat de la mise � jour dans un fichier texte
	public static final int MODE_FICHIER_TEXTE = 1;

    // Cette variable contiendra le mode dans lequel l'espion est
    private int intModeEspion;
	
	// D�claration d'un objet pour faire r�f�rence au Contr�leur de jeu
	ControleurJeu objControleurJeu;
	
	// D�claration d'une variable qui contiendra le s�parateur de ligne
	private String strFinLigne;
	
	// D�claration d'une variable qui nous permettra d'arr�ter l'espion
	private boolean bolArreterEspion;
	
	/* Contructeur de la classe
	 * @param String nomFichier: Le fichier dans lequel l'espion �crira p�riodiquement
	 * 
	 * @param int delaiMaj: Le d�lai en millisecondes en chaque maj
	 */
	public Espion(ControleurJeu controleur, String nomFichier, int delaiMaj, int mode)
	{
		intModeEspion = mode;
		objControleurJeu = controleur;
		strFinLigne = System.getProperty("line.separator");
		strNomFichier = nomFichier;
		intDelaiMaj = delaiMaj;
		bolArreterEspion = false;
	}


    /* Changer le mode de l'espion, on peut le mettre au mode RIEN, mais si
     * on veut l'arr�ter pour de bon, on doit utiliser arreterEspion()
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
    
    
    /* Arr�ter l'espion pour de bon
     */
    public void arreterEspion()
    {
    	bolArreterEspion = true;
    }
    
    /*
     * Effectuer une mise � jour imm�diate, on peut se servir de cette fonction
     * pour faire des mise � jour � certains moments pr�cis.
     */
    public void faireMajImmediate()
    {
    	faireMaj();
    }
    
    /* Thread run, met � jour les informations du serveur p�riodiquement
     */
    public void run()
    {   
    	try
    	{
    		while (bolArreterEspion == false)
    		{
                // Effectuer une mise � jour des informations
                faireMaj();

                // Mettre l'espion
               	Thread.sleep(intDelaiMaj);
    		}
    		objLogger.info("Espion arr�t�");
    	}
		catch( Exception e )
		{
			//System.out.println("Erreur dans la thread de l'espion.");
			//System.out.println(e.getMessage());
			objLogger.info("Erreur dans la thread de l'espion.");
			objLogger.error( e.getMessage() );
		}
    	
    }
    
    
    private void faireMaj()
    {
        switch(intModeEspion)
        {
        
        case MODE_RIEN: 
        
            // Ne rien faire
            break;
                
                        
        case MODE_FICHIER_TEXTE:   
        
            // �crire dans le fichier en texte

            traiterFichierTexte();
            break;

            
        }	
    }
    
    /* traiterFichierTexte
     * 
     * Cette fonction �cris dans le fichier [strNomFichier] les informations
     * du serveur.
     */
    private void traiterFichierTexte()
    {
    	
	    // D�claration de la variable qui contiendra le r�sultat
	    // de l'espion pour cette mise � jour
	    StringBuffer strResultat = new StringBuffer();
	    
	    // D�claration des diff�rentes parties du r�sultat
	    StringBuffer strEntete = new StringBuffer();
	    StringBuffer strDerniereMAJ = new StringBuffer();
	    StringBuffer strJoueursConnectes = new StringBuffer();
	    StringBuffer strSalles = new StringBuffer();
	    StringBuffer strTables = new StringBuffer();
	    
	    // D�claraction d'un objet qui contiendra une r�f�rence vers la liste des joueurs
	    Vector lstProtocoleJoueur = objControleurJeu.obtenirGestionnaireCommunication().obtenirListeProtocoleJoueur();
	    
		// D�claration d'une liste de ProtocoleJoueur qui va contenir une copie
		Vector lstCopieProtocoleJoueur = null;
		
		// D�claration d'un objet qui contiendra une r�f�rence vers la liste des salles
		TreeMap lstSalles = objControleurJeu.obtenirListeSalles();
		
	    // D�claraction d'un objet qui contiendra une r�f�rence vers la liste des tables
	    // pour une certaine salle
	    TreeMap lstTables;
		
		// D�claration d'un objet qui contiendra une r�f�rence vers la liste des joueurs
		// pour une table
		TreeMap lstJoueurs;
		
		
		// Emp�cher d'autres threads de toucher � la liste des protocoles 
		// de joueur
		synchronized (lstProtocoleJoueur)
		{
			// Faire une copie de la liste des ProtocoleJoueur
			lstCopieProtocoleJoueur = (Vector) lstProtocoleJoueur.clone();
		}
		

	    // Ent�te 
	    // Ajouter la version de l'espion
	    strEntete.append("(");
	    strEntete.append(VERSION);
	    strEntete.append(")");
	    
	    
	    // Derni�re mise � jour
	    // Ajouter la date et l'heure de la derni�re mise � jour
        Date objToday = new Date();
        String strDate;
        strDate = DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.MEDIUM).format(objToday);
        strDerniereMAJ.append("Derni�re mise � jour: ");
        strDerniereMAJ.append(strDate);


	    // Joueurs connect�s
	    // Ajouter le nombre de joueurs connect�s
	    strJoueursConnectes.append("Joueurs connect�s : ");
	    strJoueursConnectes.append(lstCopieProtocoleJoueur.size());

        // S'il y a des joueurs de connect�s, on va ajouter leurs noms      
        if (lstCopieProtocoleJoueur.size() > 0)
        {
        	// Parcourir tous les joueurs connect�s et ajouter les noms
        	strJoueursConnectes.append(strFinLigne);
        	strJoueursConnectes.append("    ");
        	
			// Passer tous les objets ProtocoleJoueur et ajouter leur nom
			for (int i = 0; i < lstCopieProtocoleJoueur.size(); i++)
			{
				if (i > 0 )
				{
					strJoueursConnectes.append(",");
				}
				
				// Ajouter le nom du joueur humain
				strJoueursConnectes.append(((ProtocoleJoueur) lstCopieProtocoleJoueur.get(i)).obtenirJoueurHumain().obtenirNomUtilisateur());
			}
        	
        }
        

	    // Salles
        // Pr�paration pour parcourir le TreeMap des salles
        Set lstEnsembleSalles = lstSalles.entrySet();
        Iterator objIterateurListeSalles = lstEnsembleSalles.iterator();	  
          
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
					strSalles.append(",");
				}
				
				intCompteur++;
								
				// Ajouter le nom de la salle
				Salle objSalle = (Salle)(((Map.Entry)(objIterateurListeSalles.next())).getValue());
				strSalles.append(objSalle.obtenirNomSalle());
			}	
	    }
	    
        // Pr�paration pour parcourir le TreeMap des salles
        lstEnsembleSalles = lstSalles.entrySet();
        objIterateurListeSalles = lstEnsembleSalles.iterator();	  
        
        
	    // Tables
	    // On va parcourir chaque salle, et pour chaque salle, on va afficher la
	    // liste des tables avec leur d�tails
	    if (lstEnsembleSalles.size() > 0)
	    {
        	
        	// Boucle du parcours de la liste des salles 
			while (objIterateurListeSalles.hasNext() == true)
			{
				// Aller chercher l'objet Salle
                Salle objSalle = (Salle)(((Map.Entry)(objIterateurListeSalles.next())).getValue());				
				
				// Aller chercher la liste des tables
				lstTables = (TreeMap)(objSalle.obtenirListeTables());
				
				// Pr�paration pour parcourir le TreeMap des tables
				Set lstEnsembleTables = lstTables.entrySet();
				Iterator objIterateurListeTables = lstEnsembleTables.iterator();

                // Ajouter le nom de la salle et le nombre de tables
         	    strTables.append("Tables pour la salle ");
         	    strTables.append(objSalle.obtenirNomSalle());
         	    strTables.append(" : ");
         	    strTables.append(lstTables.size());
         	    
         	    // Pour chaque table, ajouter les informations pour celle-ci
         	    if (lstEnsembleTables.size() > 0 )
         	    {
         	    	strTables.append(strFinLigne);
	         	    
	         	    // Boucle de parcour de la liste de tables pour la salle courante
	         	    while(objIterateurListeTables.hasNext() == true)
	         	    {
	         	    	 // Aller chercher l'objet Table
	         	    	 Table objTable = (Table)(((Map.Entry)(objIterateurListeTables.next())).getValue());	
	         	   	     
	         	   	     // Ajouter les informations sur cette table
	         	   	     strTables.append("    Num�ro de table : ");
	         	   	     strTables.append(objTable.obtenirNoTable());
	         	   	     strTables.append(strFinLigne);
	         	   	     strTables.append("    Temps : ");
	         	   	     strTables.append(objTable.obtenirTempsTotal());
	         	   	     strTables.append(" minutes");
	         	   	     strTables.append(strFinLigne);
	         	   	     strTables.append("    �tat : ");
	         	   	         
	         	   	     // Traiter l'�tat de la table
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
	         	   	     		strTables.append("Partie termin�e");
	         	   	     	}
	         	   	     }
	         	   	     
	         	   	     strTables.append(strFinLigne);
	         	   	     
	         	   	     // Si une partie est en cours, ajouter le temps restant
	         	   	     if (objTable.estCommencee() == true && objTable.estArretee() == false)
	         	   	     {
	         	   	     	strTables.append("    Temps Restant : ");
	         	   	     	strTables.append(objTable.obtenirTempsRestant());
	         	   	     	strTables.append(" secondes");
	         	   	     	strTables.append(strFinLigne);
	         	   	     }
	         	   	     
	         	   	     // Obtenir la liste joueurs
	         	   	     lstJoueurs = objTable.obtenirListeJoueurs();
	         	   	     
	         	   	     // Pr�paration pour parcourir la liste des joueurs
				         Set lstEnsembleJoueurs = lstJoueurs.entrySet();
				         Iterator objIterateurListeJoueurs = lstEnsembleJoueurs.iterator();
	         	   	     
	         	   	     // Ajouter le nom de chaque joueurs sur la table
	         	   	     strTables.append("    Joueurs : ");
	         	   	     int intCompteur = 0;
	         	   	     while(objIterateurListeJoueurs.hasNext() == true)
	         	   	     {
	         	   	     	if (intCompteur> 0)
	         	   	     	{
	         	   	     		strTables.append(",");
	         	   	     	}
	         	   	     	intCompteur++;
	         	   	     	
	         	   	     	// Aller chercher l'objet JoueurHumain
	         	   	     	JoueurHumain objJoueurHumain = (JoueurHumain)(((Map.Entry)(objIterateurListeJoueurs.next())).getValue());
	         	   	     	
	         	   	     	// Ajouter le nom d'utilisateur du joueur
	         	   	     	strTables.append(objJoueurHumain.obtenirNomUtilisateur());

                         }
                         
                         // TODO: Ajouter la liste des joueurs virtuels
                                                  
                         // Ligne vide entre chaque block de tables
                         strTables.append(strFinLigne);
                         strTables.append(strFinLigne);
	         	   	     
	         	    }
         	    }
         	       

			}	
	    }
	    
    	// Concatener chaque partie pour former le r�sultat final
    	strResultat.append(strEntete);
    	strResultat.append(strFinLigne);
    	strResultat.append(strDerniereMAJ);
    	strResultat.append(strFinLigne);
    	strResultat.append(strFinLigne);
    	strResultat.append(strJoueursConnectes);
    	strResultat.append(strFinLigne);
    	strResultat.append(strFinLigne);
    	strResultat.append(strSalles);
    	strResultat.append(strFinLigne);
    	strResultat.append(strFinLigne);
    	strResultat.append(strTables);
    	strResultat.append(strFinLigne);
    	    
    	try
    	{
    		// �crire dans le fichier
			FileWriter writer = new FileWriter(strNomFichier);
			writer.write(strResultat.toString());
			writer.close();

	    }
	    catch( Exception e)
	    {
			//System.out.println("Erreur d'�criture dans le fichier espion.");
		    objLogger.info("Erreur d'�criture dans le fichier espion.");
			objLogger.error(e.getMessage());
	    }
	
    }
}
