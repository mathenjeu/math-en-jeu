<?php

class AnswerTypeInfo extends Zend_Db_Table_Abstract
{
  protected $_name = 'answer_type_info';
  protected $_primary = array('answer_type_id','language_id');

  protected $_referenceMap    = array(
          'Language' => array(
                  'columns'                     => 'language_id',
                  'refTableClass'               => 'Language',
                  'refColumns'                  => 'language_id'
                  ),
          'AnswerType' => array(
                  'columns'                     => 'answer_type_id',
                  'refTableClass'               => 'AnswerType',
                  'refColumns'                  => 'answer_type_id'
                  )
          );

}
