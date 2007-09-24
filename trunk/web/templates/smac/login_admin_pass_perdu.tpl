<td class="centre" align="center" height="90%">
<div class="centre">
    <form name="pass_perdu" action="login-admin.php?action=envoi_info" method="post">
    <table width="100%" border="0" align="center">
    <tr>
        <td colspan="2" align="center"><h2>{$lang.recuperation_mot_passe}</H2></td>
    </tr>
    <tr>
        <td colspan="2" align="center"><p class="erreur">{$erreur}</td>
    </tr>
    <tr>
        <td colspan="2" align="center">
        {$lang.admin_pass_perdu_direction}<p>
        </td>
    </tr>
    <tr>
        <td colspan="2" width="50%" align="center"><input size="40" type="text" name="courriel"/></td>
    </tr>
    <tr>
        <td colspan="2" align="center"><input type="submit" value="{$lang.bouton_envoyer}"/></td>
    </tr>
    </table>
    </form>
    <script language="JavaScript">
        document.pass_perdu.courriel.focus();
    </script>
</div>
</td>

