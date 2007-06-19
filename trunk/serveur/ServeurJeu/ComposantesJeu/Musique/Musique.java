package ServeurJeu.ComposantesJeu.Musique;

import ServeurJeu.Configuration.GestionnaireConfiguration;
import java.util.List;
import ServeurJeu.ComposantesJeu.Musique.StyleDeMusique;

/**
 *
 * @author François Gingras
 */
public class Musique
{
    // Une liste dont chaque élément est une liste correspondant à un style
    private List banqueDeChansons;
    
    // L'URL où se trouvent les répertoires (styles) contenant les fichiers MP3
    private String url;
    
    public Musique()
    {
        GestionnaireConfiguration config = GestionnaireConfiguration.obtenirInstance();
        List listeDeStyles;
        
        listeDeStyles = config.obtenirListe("musique.style");
        url = config.obtenirString("musique.url");
        banqueDeChansons = config.obtenirListe("musique.style");
        banqueDeChansons.clear();
        for(int i=0; i<listeDeStyles.size(); i++)
        {
            StyleDeMusique style = new StyleDeMusique((String)listeDeStyles.get(i));
            banqueDeChansons.add(style);
        }
    }
    
    public List getListeURL(List styles)
    {
        List listeDeChansons = GestionnaireConfiguration.obtenirInstance().obtenirListe("musique.style");
        listeDeChansons.clear();
        String styleTemp;
        
        for(int i=0; i < styles.size(); i++)
        {
            styleTemp = (String)styles.get(i);
            List listeTemp = getListeChansonsPourUnStyle(styleTemp);
            for(int j=0; j<listeTemp.size(); j++)
            {
                listeDeChansons.add(url + styleTemp + "/" + listeTemp.get(j));
            }
        }
        return listeDeChansons;
    }
    
    public List getListeChansonsPourUnStyle(String style)
    {
        List listeARetourner = GestionnaireConfiguration.obtenirInstance().obtenirListe("musique.style");
        listeARetourner.clear();
        for(int i=0; i<banqueDeChansons.size(); i++)
        {
            StyleDeMusique styleTemp = (StyleDeMusique)banqueDeChansons.get(i);
            if(styleTemp.getNomDuStyle().equals(style))
            {
                listeARetourner = styleTemp.getListeDeChansons();
            }
        }
        return listeARetourner;
    }
}
