<td width="80%" align="left" valign="top">
<form action="portail-admin.php?action=doAjoutGroupe" method=post>
    <table width="100%" border="0">
    <thead>
    <tr>
        <th colspan="2" align="left"><h2>{$lang.groupe_ajout}</h3></th>
    </tr>
    <tr>
        <th colspan="2" class="erreur">{$erreur}</th>
    </tr>
    </thead>
    <tr>
        <td width="25%">{$lang.groupe_nom}</td>
        <td><input type="text" name="nom" value="{$nom}"/></td>
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
            {if $clavardage eq 1}
                <option value=1 selected>{$lang.oui}</option>
            {else}
                <option value=1>{$lang.oui}</option>
            {/if}
            </select>
        </td>
    </tr>
    <tr>
        <td><p/></td>
    </tr>
    <tr>
        <td><input type="submit" value="{$lang.bouton_creer_groupe}"/></td>
        <td>
            <input onclick="window.location.href='portail-admin.php?action=groupes'" type="button" value="{$lang.bouton_retour_liste}" />
        </td>
    </tr>
    </table>
</form>
</td>
    
