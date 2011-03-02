package Enumerations;

/**
 * @author Jean-François Brind'Amour
 */
public enum Commande
{

    Connexion,
    Deconnexion,
    ObtenirListeJoueurs,
    ObtenirListeSalles,
    EntrerSalle,
    QuitterSalle,
    ObtenirListeJoueursSalle,
    ObtenirListeTables,
    CreerTable,
    EntrerTable,
    QuitterTable,
    DemarrerMaintenant,
    DemarrerPartie,
    PlayerCanceledPicture,
    PlayerSelectedNewPicture,
    DeplacerPersonnage,
    RepondreQuestion,
    Pointage,
    RejoindrePartie,
    NePasRejoindrePartie,
    UtiliserObjet,
    AcheterObjet,
    Argent,
    ChatMessage,
    CreateRoom,
    UpdateRoom,
    DeleteRoom,
    CloseRoom,
    ReportRoom,
    CancelQuestion,
    ReportBugQuestion,
    ObtenirListeSallesRetour,
    ObtenirListeSallesProf,
    ConnexionProf;

    public static Commande get(String s) {
        for (Commande cmd: Commande.values()) {
            if (cmd.toString().equals(s)) {
                return cmd;
            }
        }
        return null;
    }
}