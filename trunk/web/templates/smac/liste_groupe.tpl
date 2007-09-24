<td width="90%" align="left" valign="top">
        <table class="tableau" width="100%">
            <tr class="tableau_head" align="center">
                <th>{$lang.groupe_nom}</th>
                <th>{$lang.groupe_nb_joueur}</th>
                <th>{$lang.groupe_duree_partie}</th>
                <th>{$lang.groupe_clavardage}</th>
                <th>{$lang.groupe_banque_question}</th>
                <th></th>
                <th></th>
            </tr>
            {section name=groupe loop=$groupes}
            <tr onmouseover="this.className='tableau_mouse_over';"
                        onmouseout="this.className='{cycle name="mouseout" values="tableau1,tableau2"}';"
                        class="{cycle name="bgcolor" values="tableau1,tableau2"}">
                    <td>{$groupes[groupe].nom}</td>
                    <td align="right">{$groupes[groupe].nbJoueur}</td>
                    <td align="right">{$groupes[groupe].duree}</td>
                    <td align="center">{if $groupes[groupe].clavardage eq 0 }{$lang.non}{else}{$lang.oui}{/if}</td>
                    <td></td>
                    <td valign="middle" align="center"><a title="{$lang.groupe_mod_detail}" href="portail-admin.php?action=detailGroupe&amp;cleGroupe={$groupes[groupe].cle}"><img alt="{$lang.groupe_mod_detail}" border="0" src="img/modifier.png" /></a></td>
                    <td valign="middle" align="center"><a title="{$lang.groupe_supprimer}" onclick="return validerSupprimer('{$lang.valider_supression}')" href="portail-admin.php?action=doDeleteGroupe&amp;cleGroupe={$groupes[groupe].cle}"><img alt="{$lang.groupe_supprimer}" border="0" src="img/delete.png" /></a></td>
            </tr>
            {/section}
            </table>
            <br>
            <input onclick="window.location.href='portail-admin.php?action=ajoutGroupe'" type="button" value="{$lang.bouton_ajout_groupe}" />
<br>
</td>
