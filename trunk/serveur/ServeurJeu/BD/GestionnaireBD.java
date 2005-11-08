package ServeurJeu.BD;

import java.util.Vector;

import ServeurJeu.ControleurJeu;
import ServeurJeu.ComposantesJeu.Salle;
import ClassesUtilitaires.GenerateurPartie;
import Enumerations.Visibilite;
import ServeurJeu.ComposantesJeu.Cases.Case;
import ServeurJeu.ComposantesJeu.Cases.CaseCouleur;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.ComposantesJeu.Objets.Magasins.Magasin;
import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.ObjetUtilisable;
import ServeurJeu.ComposantesJeu.Objets.Pieces.Piece;
import ServeurJeu.ComposantesJeu.ReglesJeu.Regles;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesCaseCouleur;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesCaseSpeciale;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesMagasin;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesObjetUtilisable;
import ServeurJeu.Evenements.GestionnaireEvenements;

/**
 * @author Jean-François Brind'Amour
 */
public class GestionnaireBD 
{
	// Déclaration d'une référence vers le contrôleur de jeu
	private ControleurJeu objControleurJeu;
	
	/**
	 * Constructeur de la classe GestionnaireBD qui permet de garder la 
	 * référence vers le contrôleur de jeu
	 */
	public GestionnaireBD(ControleurJeu controleur)
	{
		super();
		
		// Garder la référence vers le contrôleur de jeu
		objControleurJeu = controleur;
	}
	
	/**
	 * Cette fonction permet de chercher dans la BD si le joueur dont le nom
	 * d'utilisateur et le mot de passe sont passés en paramètres existe.
	 * 
	 * @param String nomUtilisateur : Le nom d'utilisateur du joueur
	 * @param String motDePasse : Le mot de passe du joueur
	 * @return true  : si le joueur existe et que son mot de passe est correct
	 * 		   false : si le joueur n'existe pas ou que son mot de passe n'est 
	 * 				   pas correct
	 */
	public boolean joueurExiste(String nomUtilisateur, String motDePasse)
	{
		if ((nomUtilisateur.equals("Jeff") && motDePasse.equals("jeff")) ||
			(nomUtilisateur.equals("Chriss") && motDePasse.equals("ti-chriss")) ||
			(nomUtilisateur.equals("Simon") && motDePasse.equals("si")) ||
			(nomUtilisateur.equals("Sylvain") && motDePasse.equals("halle")))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Cette fonction permet de chercher dans la BD le joueur et de remplir
	 * les champs restants du joueur.
	 * 
	 * @param JoueurHumain joueur : Le joueur duquel il faut trouver les
	 * 								informations et les définir dans l'objet
	 */
	public void remplirInformationsJoueur(JoueurHumain joueur)
	{
		if (joueur.obtenirNomUtilisateur().equals("Jeff"))
		{
			joueur.definirPrenom("Jean-François");
			joueur.definirNomFamille("Brind'Amour");
		}
		else if (joueur.obtenirNomUtilisateur().equals("Sylvain"))
		{
			joueur.definirPrenom("Sylvain");
			joueur.definirNomFamille("Hallé");
		}
		else if (joueur.obtenirNomUtilisateur().equals("Christian"))
		{
			joueur.definirPrenom("Christian");
			joueur.definirNomFamille("Dompierre");
		}
		else if (joueur.obtenirNomUtilisateur().equals("Simon"))
		{
			joueur.definirPrenom("Simon");
			joueur.definirNomFamille("Paquette");
		}
	}

	/**
	 * Cette méthode permet de charger les salles en mémoire dans la liste
	 * des salles du contrôleur de jeu.
	 * 
	 * @param GestionnaireEvenements gestionnaireEv : Le gestionnaire d'événements
	 * 				qu'on doit fournir à chaque salle pour qu'elles puissent 
	 * 				envoyer des événements
	 */
	public void chargerSalles(GestionnaireEvenements gestionnaireEv)
	{
		Regles objReglesSalle = new Regles();
		
		objReglesSalle.obtenirListeCasesCouleurPossibles().add(new ReglesCaseCouleur(2, 1));
		objReglesSalle.obtenirListeCasesCouleurPossibles().add(new ReglesCaseCouleur(1, 2));
		objReglesSalle.obtenirListeCasesCouleurPossibles().add(new ReglesCaseCouleur(3, 3));
		objReglesSalle.obtenirListeCasesCouleurPossibles().add(new ReglesCaseCouleur(4, 4));
		objReglesSalle.obtenirListeCasesCouleurPossibles().add(new ReglesCaseCouleur(5, 5));
		/*objReglesSalle.obtenirListeCasesCouleurPossibles().add(new ReglesCaseCouleur(6, 6));
		objReglesSalle.obtenirListeCasesCouleurPossibles().add(new ReglesCaseCouleur(8, 7));
		objReglesSalle.obtenirListeCasesCouleurPossibles().add(new ReglesCaseCouleur(2, 8));*/
		
		objReglesSalle.obtenirListeCasesSpecialesPossibles().add(new ReglesCaseSpeciale(1, 1));
		/*objReglesSalle.obtenirListeCasesSpecialesPossibles().add(new ReglesCaseSpeciale(2, 2));
		objReglesSalle.obtenirListeCasesSpecialesPossibles().add(new ReglesCaseSpeciale(3, 3));
		objReglesSalle.obtenirListeCasesSpecialesPossibles().add(new ReglesCaseSpeciale(4, 4));
		objReglesSalle.obtenirListeCasesSpecialesPossibles().add(new ReglesCaseSpeciale(5, 5));
		objReglesSalle.obtenirListeCasesSpecialesPossibles().add(new ReglesCaseSpeciale(6, 6));
		objReglesSalle.obtenirListeCasesSpecialesPossibles().add(new ReglesCaseSpeciale(7, 7));
		objReglesSalle.obtenirListeCasesSpecialesPossibles().add(new ReglesCaseSpeciale(8, 8));
		objReglesSalle.obtenirListeCasesSpecialesPossibles().add(new ReglesCaseSpeciale(9, 9));
		objReglesSalle.obtenirListeCasesSpecialesPossibles().add(new ReglesCaseSpeciale(10, 10));*/
		
		objReglesSalle.obtenirListeMagasinsPossibles().add(new ReglesMagasin(1, "Magasin1"));
		objReglesSalle.obtenirListeMagasinsPossibles().add(new ReglesMagasin(2, "Magasin2"));
		
		objReglesSalle.obtenirListeObjetsUtilisablesPossibles().add(new ReglesObjetUtilisable(1, "Reponse", Visibilite.Aleatoire));
		
		objReglesSalle.definirPermetChat(true);
		objReglesSalle.definirRatioTrous(0.30f);
		objReglesSalle.definirRatioMagasins(0.05f);
		objReglesSalle.definirRatioCasesSpeciales(0.15f);
		objReglesSalle.definirRatioPieces(0.10f);
		objReglesSalle.definirRatioObjetsUtilisables(0.05f);
		objReglesSalle.definirValeurPieceMaximale(25);
		objReglesSalle.definirTempsMinimal(10);
		objReglesSalle.definirTempsMaximal(60);
		
		Case[][] asdf = GenerateurPartie.genererPlateauJeu(objReglesSalle, 10, new Vector());
		for (int i = 0; i < asdf.length; i++)
		{
			for (int j = 0; j < asdf[i].length; j++)
			{
				if (asdf[i][j] == null)
				{
					System.out.println("(" + i + ", " + j + ") -> null");	
				}
				else if (asdf[i][j] instanceof CaseCouleur)
				{
					System.out.print("(" + i + ", " + j + ") -> case couleur:" + asdf[i][j].obtenirTypeCase() + ", objet:");
					
					if (((CaseCouleur) asdf[i][j]).obtenirObjetCase() == null)
					{
						System.out.print("null\n");
					}
					else if (((CaseCouleur) asdf[i][j]).obtenirObjetCase() instanceof Magasin)
					{
						System.out.print(((CaseCouleur) asdf[i][j]).obtenirObjetCase().getClass().getName() + "\n");
					}
					else if (((CaseCouleur) asdf[i][j]).obtenirObjetCase() instanceof ObjetUtilisable)
					{
						System.out.print(((CaseCouleur) asdf[i][j]).obtenirObjetCase().getClass().getName() + ", visible:" + ((ObjetUtilisable) ((CaseCouleur) asdf[i][j]).obtenirObjetCase()).estVisible() + "\n");
					}
					else
					{
						System.out.print("Piece, valeur:" + ((Piece) ((CaseCouleur) asdf[i][j]).obtenirObjetCase()).obtenirValeur() + "\n");
					}
				}
				else
				{
					System.out.println("(" + i + ", " + j + ") -> case speciale:" + asdf[i][j].obtenirTypeCase());
				}
			}
		}
		
	    Salle objSalle = new Salle(gestionnaireEv, "Générale", "Jeff", "", objReglesSalle);
	    Salle objSalle2 = new Salle(gestionnaireEv, "Privée", "Jeff", "jeff", objReglesSalle);
	    
	    objControleurJeu.ajouterNouvelleSalle(objSalle);
	    objControleurJeu.ajouterNouvelleSalle(objSalle2);
	}
	
	/**
	 * Cette méthode permet de fermer la connexion de base de données qui 
	 * est ouverte.
	 */
	public void arreterGestionnaireBD()
	{
		// TODO Fermer la connexion de BD et peut-être d'autres choses
	}
}
