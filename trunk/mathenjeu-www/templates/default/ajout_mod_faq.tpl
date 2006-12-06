<td width="90%" valign="top" align="left">
{if $action eq "ajout"}
    <h2>{$lang.faq_ajout}</h2>
{else}
    <h2>{$lang.faq_modification}</h2>
{/if}

<p class=erreur>{$erreur}</p>

{if $action eq "ajout"}
    <form name="faq" action="super-admin.php?action=insertFaq" method="POST">
{else}
    <form name="nouvelle" action="super-admin.php?action=doUpdateFaq&amp;cleFaq={$cle}" method="POST">
{/if}
<table align="left" width="80%">
    <tr>
        <td>{$lang.faq_question}<br>
        <textarea cols="70" rows="5" name="question">{$question}</textarea></td>
    </tr>
    <tr>
        <td>{$lang.faq_reponse}<br>
        <textarea cols="70" rows="5" name="reponse">{$reponse}</textarea></td>
    </tr>
    <tr>
        <td colspan="2">
        <input onclick="window.location.href='super-admin.php?action=faq'" type="button" value="{$lang.bouton_retour_liste}">
        <input type="submit" value="{$lang.bouton_envoyer}">
        </td>
    </tr>
</table>
</form>
</td>
