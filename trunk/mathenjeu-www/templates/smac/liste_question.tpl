<td width="90%" valign="top" align="left">
<div class="centre">
	<h2>{$lang.question_resultat_recherche}</h2>
	<table class="tableau" width="100%" border="0">
	    <thead>
	    <tr class="tableau_head" align="center">
	        <th>{$lang.question_numero}</th>
	        <th>{$lang.question_flash_question}</th>
	        <th>{$lang.question_flash_reponse}</th>
	        <th>{$lang.question_eps_question}</th>
	        <th>{$lang.question_eps_reponse}</th>
	        <th>{$lang.question_ps_question}</th>
	        <th>{$lang.question_ps_reponse}</th>
	        <th>{$lang.question_createur}</th>
	        <th>{$lang.question_modifier}</th>
	        <th>{$lang.question_valide_non_valide}</th>
	        <th>{$lang.question_upload}</th>
	    </tr>
	    </thead>
	    {section name=question loop=$questions}
	        <tr onmouseover="this.className='tableau_mouse_over';"
	            onmouseout="this.className='{cycle name="mouseout" values="tableau1,tableau2"}';"
	            class="{cycle name="bgcolor" values="tableau1,tableau2"}">
	        <td>{$questions[question].cle}</td>
	        <td>
				<a target="_blank" href="{$dossier_flash}{$questions[question].qFlash}">{$questions[question].qFlash}</a>
			</td>
	        <td>
				<a target="_blank" href="{$dossier_flash}{$questions[question].rFlash}">{$questions[question].rFlash}</a>
			</td>
	        <td>
				<a href="{$dossier_eps}{$questions[question].qEps}">{$questions[question].qEps}</a>
			</td>
	        <td>
				<a href="{$dossier_eps}{$questions[question].rEps}">{$questions[question].rEps}</a>
			</td>
			<td>
				<a href="{$dossier_eps}{$questions[question].qPs}">{$questions[question].qPs}</a>
			</td>
			<td>
				<a href="{$dossier_eps}{$questions[question].rPs}">{$questions[question].rPs}</a>
			</td>
	        <td align="center">
				{$questions[question].alias}
			</td>
	        {if ($questions[question].alias == $alias || $acces >= 2) && $questions[question].typeReponse eq 0}
	        	<td align="center"><a title="{$lang.question_modifier}" href="question.php?action=modification&amp;cleQuestion={$questions[question].cle}"><img border="0" alt="{$lang.question_modifier}" src="{$template}img/modifier.png"></a></td>
	        {else}
	        	<td></td>
	        {/if}
	        {if $acces >= 2}
	        	{if $questions[question].valide eq 0}
	        		<td align="center"><a title="{$lang.question_valide}" href="question.php?action=valide&amp;cleQuestionV={$questions[question].cle}&amp;from={$from}&amp;texteQ={$texteQ}&amp;texteR={$texteR}&amp;limit={$limit}&amp;cleQuestion={$cleQuestion}&amp;categorie={$categorie}&amp;nonvalide={$nonvalide}&amp;cleJoueur={$cleJoueur}"><img border="0" alt="{$lang.question_valide}" src="{$template}img/delete.png"></a></td>
	        	{else}
	        		<td align="center"><a title="{$lang.question_non_valide}" href="question.php?action=non_valide&amp;cleQuestionV={$questions[question].cle}&amp;from={$from}&amp;texteQ={$texteQ}&amp;texteR={$texteR}&amp;limit={$limit}&amp;cleQuestion={$cleQuestion}&amp;categorie={$categorie}&amp;nonvalide={$nonvalide}&amp;cleJoueur={$cleJoueur}"><img border="0" alt="{$lang.question_non_valide}" src="{$template}img/s_okay.png"></a></td>
	        	{/if}
	        {else}
	        	{if $questions[question].valide eq 0}
	        		<td align="center"><img border="0" alt="{$lang.question_valide}" src="{$template}img/delete.png"></td>
	        	{else}
	        		<td align="center"><img border="0" alt="{$lang.question_non_valide}" src="{$template}img/s_okay.png"></td>
	        	{/if}
	        {/if}
	        {if $acces >= 5}
	        	<td align="center"><a href="question.php?action=upload&amp;cleQuestion={$questions[question].cle}"><img border="0" alt="{$lang.question_upload}" src="{$template}img/upload.png"></a></td>
	        {else}
	        	<td></td>
	        {/if}
	    </tr>
	    {/section}
	</table>
	<table width="100%" border="0">
		<tr align="center">
			<td width="25%">
				&nbsp;
				{if $from > 0}
			    	<a href=question.php?action=chercher&amp;from={0}&amp;texteQ={$texteQ}&amp;texteR={$texteR}&amp;limit={$limit}&amp;cleQuestion={$cleQuestion}&amp;categorie={$categorie}&amp;nonvalide={$nonvalide}&amp;cleJoueur={$cleJoueur}>{$lang.question_debut}</a>
				{/if}
			</td>
			<td width="25%">
				&nbsp;
				{if $from > 0}
			    	<a href=question.php?action=chercher&amp;from={$from-$limit}&amp;texteQ={$texteQ}&amp;texteR={$texteR}&amp;limit={$limit}&amp;cleQuestion={$cleQuestion}&amp;categorie={$categorie}&amp;nonvalide={$nonvalide}&amp;cleJoueur={$cleJoueur}>{$lang.question_precedent}</a>
				{/if}
			</td>
			<td width="25%">
				&nbsp;
				{if $max_enr > ($nb_enr+$from)}
			    	<a href=question.php?action=chercher&amp;from={$nb_enr+$from}&amp;texteQ={$texteQ}&amp;texteR={$texteR}&amp;limit={$limit}&amp;cleQuestion={$cleQuestion}&amp;categorie={$categorie}&amp;nonvalide={$nonvalide}&amp;cleJoueur={$cleJoueur}>{$lang.question_suivant}</a>
				{/if}
			</td>
			<td width="25%">
				&nbsp;
				{if $max_enr > ($nb_enr+$from)}
			    	<a href=question.php?action=chercher&amp;from={$max_enr-$limit}&amp;texteQ={$texteQ}&amp;texteR={$texteR}&amp;limit={$limit}&amp;cleQuestion={$cleQuestion}&amp;categorie={$categorie}&amp;nonvalide={$nonvalide}&amp;cleJoueur={$cleJoueur}>{$lang.question_fin}</a>
				{/if}
			</td>
		</tr>
	</table>
	<br>
</div>
</td>