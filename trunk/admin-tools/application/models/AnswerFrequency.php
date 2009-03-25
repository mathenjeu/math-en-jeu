<?php

class AnswerFrequency extends Zend_Db_Table_Abstract
{
  protected $_name = 'answer_frequency';
  protected $_primary = array('answer_id','level_id');

  protected $_referenceMap    = array(
          'Level' => array(
                  'columns'                     => 'level_id',
                  'refTableClass'               => 'Level',
                  'refColumns'                  => 'level_id'
                  ),
          'Answer' => array(
                  'columns'                     => 'answer_id',
                  'refTableClass'               => 'Answer',
                  'refColumns'                  => 'answer_id'
                  )
          );
}
