<td width="65%" align="left" valign="top">
<form action="portail-admin.php?action=doModificationGroupe" method="post">
    <input name="cleGroupe" type="hidden" value="{$cle}">
    <table class="tableau_admin" width="100%" border="0">
    <thead>
    <tr>
        <th colspan="3"><h3>{$lang.groupe_detail_titre}</h3></th>
    </tr>
    </thead>
    <tr>
        <td width="25%">{$lang.groupe_nom}</td>
        <td><input type="text" name="nom" value="{$nom}" /></td>
        <td></td>
    </tr>
    <tr>
        <td>{$lang.groupe_duree_partie}</td>
        <td><input type="text" name="duree" value="{$duree}"/></td>
    </tr>
    <tr>
        <td>{$lang.groupe_clavardage}?</td>
        <td>
            <select name="clavardage">
            <option value="0">{$lang.non}</option>
            <option value="1" {if $clavardage eq 1}selected{/if}>{$lang.oui}</option>
            </select>
        </td>
    </tr>
    <tr>
        <td><input type="submit" value="{$lang.bouton_enregistrer}"/></td>
        </form>
        <td><input onClick="window.location.href='portail-admin.php?action=groupes'" type="button" value="{$lang.bouton_retour_liste}" /></td>
    </tr>
    <tr>
        <td><p></td>
    </tr>
    </table>
<br>
{$lang.groupe_detail_direction}<img alt=" " src="img/s_okay.png"> {$lang.et} <img alt=" " src="img/delete.png">
