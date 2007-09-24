<td class="centre" width="90%" align="left" valign="top">
<div class="centre">
	<h2>{$lang.faq_titre_gestion}</h2>
	<p>
	{$lang.faq_direction} <img border="0" src="{$template}img/up.gif" alt="{$lang.haut}"> {$lang.et} <img border="0" src="{$template}img/down.gif" alt="{$lang.bas}">.
	</p>
    <table class="tableau" width="100%">
        <tr class="tableau_head" align="center" bgcolor="#D5EED5">
            <th></th>
            <th></th>
            <th>{$lang.faq_question}</th>
            <th>{$lang.faq_reponse}</th>
            <th></th>
            <th></th>
        </tr>
        
        {section name=faq loop=$faqs}
        <tr onmouseover="this.className='tableau_mouse_over';"
                onmouseout="this.className='{cycle name="mouseout" values="tableau1,tableau2"}';"
                class="{cycle name="bgcolor" values="tableau1,tableau2"}">
            
        <td align="center">
			{if $faqs[faq].numero neq 1}
				<a title="{$lang.faq_haut}" href="admin_faq.php?action=faqMove&amp;numero={$faqs[faq].numero}"><img border="0" src="{$template}img/up.gif" alt="{$lang.faq_haut}"></a>
			{/if}
		</td>
        <td align="center">
        	{if $faqs[faq].numero neq $nb_faq}
				<a title="{$lang.faq_bas}" href="admin_faq.php?action=faqMove&amp;numero=-{$faqs[faq].numero}"><img border="0" src="{$template}img/down.gif" alt="{$lang.faq_bas}"></a>
			{/if}
		</td>
        <td width="40%">{$faqs[faq].question}</td>
        <td width="40%">{$faqs[faq].reponse}</td>
        <td align="center"><a title="{$lang.faq_detail}" href="admin_faq.php?action=detailFaq&amp;cleFaq={$faqs[faq].cle}"><img alt="{$lang.detail_faq}" border="0" src="{$template}img/modifier.png"></a></td>
        <td align="center"><a title="{$lang.faq_supprimer}" onclick="return validerSupprimer('{$lang.valider_supression}')" href="admin_faq.php?action=deleteFaq&amp;cleFaq={$faqs[faq].cle}"><img alt="{$lang.faq_supprimer}" border="0" src="{$template}img/delete.png"></a></td>
        </tr>
        {/section}
    </table>
    <br>
	<input onclick="window.location.href='admin_faq.php?action=ajoutFaq'" type="button" value="{$lang.bouton_ajout_faq}"></td>
</div>
</td>