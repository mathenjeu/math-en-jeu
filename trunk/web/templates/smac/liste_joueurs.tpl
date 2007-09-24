<td width="90%" align="left" valign="top">
    <table class="tableau" width="100%" cellpadding="3" cellspacing="1">
        <tr class="tableau_head" align="center">
            <th>{$lang.prenom}</th>
            <th>{$lang.nom}</th>
            <th>{$lang.alias}</th>
            <th>{$lang.groupe_joueur_nb_parties_jouees}</th>
            <th>{$lang.groupe_joueur_temps_joues}</th>
            <th>{$lang.groupe_joueur_nb_victoire}</th>
            <th>{$lang.groupe}</th>
            <th></th>
            <th></th>
        </tr>
        
        {section name=joueur loop=$joueurs}
        <tr onmouseover="this.className='tableau_mouse_over';"
            onmouseout="this.className='{cycle name="mouseout" values="tableau1,tableau2"}';"
            class="{cycle name="bgcolor" values="tableau1,tableau2"}">
            
        <td>{$joueurs[joueur].prenom}</td>
        <td>{$joueurs[joueur].nom}</td>
        <td>{$joueurs[joueur].alias}</td>
        <td align="right">{$joueurs[joueur].partieCompletes}</td>
        <td align="right">{$joueurs[joueur].tempsPartie}</td>
        <td align="right">{$joueurs[joueur].nbVictoire}</td>
        <td><a class="lien_sur_couleur" href="portail-admin.php?action=detailGroupe&amp;cleGroupe={$joueurs[joueur].cleGroupe}">{$joueurs[joueur].nomGroupe}</a></td>

        <td align="center"><!--<a title="{$lang.afficher_detail_joueur}" href="portail-admin.php?action=detailJoueur&amp;cleJoueur={$joueurs[joueur].cle}"><img alt="{$lang.afficher_detail_joueur}" border="0" src="img/modifier.png"></a>--></td>
        <td align="center"><a title="{$lang.supprimer_joueur}" onclick="return validerSupprimer('{$lang.valider_supression}')" href="portail-admin.php?action=deleteJoueur&amp;cleJoueur={$joueurs[joueur].cle}"><img alt="{$lang.supprimer_joueur}" border="0" src="img/delete.png"></a></td>
        </tr>
        {/section}
        
        </table>
</td>
