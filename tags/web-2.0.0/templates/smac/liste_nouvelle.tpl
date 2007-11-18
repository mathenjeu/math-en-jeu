<td class="centre" width="90%" valign="top" align="left">
<div class="centre">
	<h2>{$lang.gestion_nouvelle}</h2>
    <table class="tableau" width="100%" border="0">
    <tr class="tableau_head" align=center>
        <th width="10%">{$lang.date}</th>
        <th width="30%">{$lang.titre}</th>
        <th width="50%">{$lang.texte}</th>
        <th width="10%">{$lang.destinataire}</th>
        <th></th>
        <th></th>
    </tr>
    {section name=nouvelle loop=$nouvelles}
    <tr onmouseover="this.className='tableau_mouse_over';"
                        onmouseout="this.className='{cycle name="mouseout" values="tableau1,tableau2"}';"
                        class="{cycle name="bgcolor" values="tableau1,tableau2"}">
        <td>{$nouvelles[nouvelle].date}</td>
        <td>{$nouvelles[nouvelle].titre}</td>
        <td>{$nouvelles[nouvelle].nouvelle}</td>
        <td>{$nouvelles[nouvelle].destinataire}</td>
        <td>
            <a title="{$lang.modification_nouvelle}" href="admin_nouvelles.php?action=updateNouvelle&cleNouvelle={$nouvelles[nouvelle].cle}">
            <img border="0" src="{$template}img/modifier.png"></a>
        </td>
        <td>
            <a title="{$lang.suppression_nouvelle}" onclick="return validerSupprimer('{$lang.valider_supression}')" href="admin_nouvelles.php?action=deleteNouvelle&cleNouvelle={$nouvelles[nouvelle].cle}">
            <img border="0" src="{$template}img/delete.png"></a>
        </td>
    </tr>
    {/section}
    </table>
    <br>
    <input onclick="window.location.href='admin_nouvelles.php?action=ajoutNouvelle'" type="button" value="{$lang.ajout_nouvelle}" /></td>
</div>
</td>

