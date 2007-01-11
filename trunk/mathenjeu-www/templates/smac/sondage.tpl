<td width="20%" valign="top">
<div class="sondage">
	<div class="centre">
	<form action="{$page}?action=sondage" method="POST">	
    <table width="100%" border="0" cellpadding="0" cellspacing="0" align="center">
    <tr>
        <td></td>
        <td colspan="2" class="titresondage">{$titre}<br></td>
    </tr>
    <tr>
        <td>&nbsp;</td>
    </tr>
    <tr>
        <td></td>
        <td>
        <table width="100%" cellspacing="0"> 
		<tr>
            <td>
            {html_radios name="reponseSondage" values=$reponseID output=$reponseTexte separator="<br />"}
        	</td>
        </tr>
        </table>
        </td>
    </tr>
    <tr>
        <td>&nbsp;</td>
    </tr>
    <tr>
        <td></td>
        <td colspan="2" align="center"><input type="submit" value="{$lang.bouton_repondre}"></td>
    </tr>
    </table>
    </form>
	</div>
</div>

</td>
