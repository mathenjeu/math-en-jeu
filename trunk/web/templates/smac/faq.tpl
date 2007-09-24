<td align="left" valign="top">
<h2>{$lang.faq_titre}</h2>
<div style="width:90%;align:center" class="hr"><p><br>
	<ol start="1">
	{section name=faq loop=$faqs}
		<li><a href="{$page}#faq{$smarty.section.faq.index+1}">{$faqs[faq].question}</a></li>
	{/section}
	</ol>
	<br>
	<ol start="1">
	{section name=faq loop=$faqs}
		<!--<div id="{$smarty.section.faq.index+1}">-->
			<li id="faq{$smarty.section.faq.index+1}"><b>{$faqs[faq].question}</b>
			<p>{$faqs[faq].reponse}</p>
			</li>
		<!--</div>-->
	{/section}
	</ol>
</div>
</td>