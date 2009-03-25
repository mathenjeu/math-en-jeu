<?php

class AnswerInfo extends Zend_Db_Table_Abstract
{
  protected $_name = 'answer_info';
  protected $_primary = array('answer_id','language_id');

  protected $_referenceMap    = array(
          'Language' => array(
                  'columns'                     => 'language_id',
                  'refTableClass'               => 'Language',
                  'refColumns'                  => 'language_id'
                  ),
          'Answer' => array(
                  'columns'                     => 'answer_id',
                  'refTableClass'               => 'Answer',
                  'refColumns'                  => 'answer_id'
                  )
          );
}
