<br>
    <table class="tableau" cellpadding="3" cellspacing="1" width="100%">
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
        {if $cleGroupe eq $joueurs[joueur].cleGroupeJoueur}
            <tr class="tableau_joueur_selectioner" onmouseover="this.className='tableau_mouse_over';"
            onmouseout="this.className='tableau_joueur_selectioner';">
        {else}
            <tr onmouseover="this.className='tableau_mouse_over';"
            onmouseout="this.className='{cycle name="mouseout" values="tableau1,tableau2"}';"
            class="{cycle name="bgcolor" values="tableau1,tableau2"}">
        {/if}
        <td>{$joueurs[joueur].prenom}</td>
        <td>{$joueurs[joueur].nom}</td>
        <td>{$joueurs[joueur].alias}</td>
        <td align=right>{$joueurs[joueur].partieCompletes}</td>
        <td align=right>{$joueurs[joueur].tempsPartie}</td>
        <td align=right>{$joueurs[joueur].nbVictoire}</td>
        
        <td><a class="lien_sur_couleur" href="portail-admin.php?action=detailGroupe&amp;cleGroupe={$joueurs[joueur].cleGroupeJoueur}">{$joueurs[joueur].nomGroupe}</a></td>

        <td align="center"><a title="{$lang.groupe_ajout_joueur}" href="portail-admin.php?action=ajoutJoueurGroupe&amp;cleJoueur={$joueurs[joueur].cle}&amp;cleGroupe={$cleGroupe}"><img alt="{$lang.groupe_ajout_joueur}" border="0" src="img/s_okay.png"></a></td>

        {if $cleGroupe eq $joueurs[joueur].cleGroupeJoueur}
            <td align="center"><a title="{$lang.groupe_supprimer_joueur}" href="portail-admin.php?action=deleteJoueurGroupe&amp;cleJoueur={$joueurs[joueur].cle}&amp;cleGroupe={$cleGroupe}"><img alt="{$lang.groupe_supprimer_joueur}" border="0" src="img/delete.png"></a></td>
        {else}
            <td></td>
        {/if}

        </tr>
        {/section}
    </table>
</td>
