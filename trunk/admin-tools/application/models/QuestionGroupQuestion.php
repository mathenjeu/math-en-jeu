<?php

class QuestionGroupQuestion extends Zend_Db_Table_Abstract
{
  protected $_name = 'question_group_question';

  
  protected $_referenceMap    = array(
  				'QuestionGroup' => array(
            'columns'           => array('question_group_id', 'language_id'),
            'refTableClass'     => 'QuestionGroup',
            'refColumns'        => array('question_group_id', 'language_id')
        ),
        	'QuestionInfo' => array(
            'columns'           => array('question_id', 'language_id'),
            'refTableClass'     => 'QuestionInfo',
            'refColumns'        => array('question_id', 'language_id')
        ));
  
}