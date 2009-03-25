<?php
class User extends Zend_Db_Table_Abstract
{
  protected $_name = 'user';
  
  protected $_dependentTables = array('QuestionInfo','Comment','Image');

  protected $_referenceMap = array(
          'Language' => array(
                  'columns'                     => 'language_id',
                  'refTableClass'               => 'Language',
                  'refColumns'                  => 'language_id'
                   )
          );
}