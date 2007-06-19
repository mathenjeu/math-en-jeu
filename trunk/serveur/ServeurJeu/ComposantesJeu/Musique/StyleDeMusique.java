package ServeurJeu.ComposantesJeu.Musique;

import java.util.List;
import ServeurJeu.Configuration.GestionnaireConfiguration;

/**
 *
 * @author Fran�ois Gingras
 */
public class StyleDeMusique
{
    // Nom du style
    private String nomDuStyle;
    
    // Liste des chansons
    List listeDeChansons;
    
    public StyleDeMusique(String style)
    {
        nomDuStyle = style;
        GestionnaireConfiguration config = GestionnaireConfiguration.obtenirInstance();
        listeDeChansons = config.obtenirListe("musique." + nomDuStyle + ".chanson");
    }
    
    public String getNomDuStyle()
    {
        return nomDuStyle;
    }
    
    public List getListeDeChansons()
    {
        return listeDeChansons;
    }
}
