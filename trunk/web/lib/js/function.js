/*******************************************************************************
Fichier : function.js
Auteur : Maxime Bégin
Description : regroupe les fonctions javascript utilisé sur le site web
********************************************************************************
31-07-2006 Maxime Bégin - Modification de la fonction prev_nouvelle pour 
	conserver les nouvelles lignes. 
16-06-2006 Maxime Bégin - Version initiale
*******************************************************************************/

/*******************************************************************************
Fonction : validerSupprimer
Paramètre :
    - question : la question à poser
Description : demande une confirmation à l'usager et retourne vrai s'il confirme
    faux sinon.
*******************************************************************************/
function validerSupprimer(question)
{
  return (confirm(question));
}

/*******************************************************************************
Fonction : afficherImage
Paramètre :
Description : afficher une image dans les sections statistiques des super-admin
*******************************************************************************/
function afficherImage()
{
    graphique=document.stats.typeStat[document.stats.typeStat.selectedIndex].value;
    param=parseInt(document.stats.param.value,10);
    typeGraph = document.stats.typeGraph[document.stats.typeGraph.selectedIndex].value;
    if(param<=0 || param>1000)
        param=0;
        
    document.images.graph.src=
        'lib/statistique.class.php?graphique=' + graphique
        + '&opt=' + param + '&type=' + typeGraph;

    return false;
}

function afficherCacher(control){
	
	if(control.style.display == '')
	{
		control.style.display = 'none';
	}
	else
	{
		control.style.display = '';
	}
}

/*******************************************************************************
Fonction : prev_nouvelle
Paramètre : les informations qui compose une nouvelle
Description : ouvrir la prévisualisation d'une nouvelle dans une autre fenêtre
*******************************************************************************/
function prev_nouvelle(date,titre,nouvelle,image)
{
  var news = nouvelle
  news = news.replace(/\r\n/g,"<br>");
  news = news.replace(/\r/g,"<br>");
  news = news.replace(/\n/g,"<br>");
  
  url = 'prev_nouvelle.php?titre=' + titre + '&date=' + date +
    '&nouvelle=' + news + '&image=' + image;
  window.open(url,'','left=20,top=20,width=800,height=600,toolbar=0,resizable=1');
  return false;
}

