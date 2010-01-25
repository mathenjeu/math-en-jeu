 <td align=left height="90%">
    <table width="90%" border="0" cellspacing="5">
    <tr>
        <td colspan="3" align="center"><h1>{$lang.inscription_admin}</h1></td>
    </tr>
        <td colspan="3" class="erreur">{$erreur}</td>
    </tr>

    <form name="inscription" method="post" action="inscription-admin.php?action=soumettre">
    
    <tr>
        <td>{$lang.prenom}</td>
        <td><input type="text" name="prenom" size="20" value="{$prenom}"></td>
        <td></td>
    </tr>
    <tr>
        <td>{$lang.nom}</td>
        <td><input type="text" name="nom" size="20" value="{$nom}"</td>
    </tr>
    <tr>
        <td>{$lang.courriel}</td>
        <td><input type="text" name="courriel" size="20" value="{$courriel}"></td>
        <td class=commentaire>{$lang.info_courriel}</td>
        <td></td>
    </tr>
    <tr>
        <td>{$lang.alias}</td>
        <td><input type="text" name="alias" size="20" value="{$alias}"></td>
    </tr>
    <tr>
        <td>{$lang.mot_passe}</td>
        <td><input type="password" name="motDePasse" size="20" value="{$motDePasse}"></td>
    </tr>
    </tr>
    <tr>
        <td>{$lang.niveau_scolaire}</td>
        <td>
            <select name="niveau"
                onChange='document.inscription.action="inscription-admin.php?action=etablissement";
                document.inscription.submit()'>
                {html_options values=$niveauID output=$niveauTexte selected=$niveau}
            </select>
        </td>
    </tr>
    
    {if $niveau ne 14}
    <tr>
        <td>{$lang.etablissement}</td>
        <td>
            <select name="etablissement" STYLE="width: 400px">
            {html_options values=$etablissementID output=$etablissementTexte selected=$etablissement}
            </select>
        </td>
    </tr>
    {/if}

    <tr>
        <td><input type="submit" value="Inscription"></td>
    </tr>
    </table>
    </form>
</td>
