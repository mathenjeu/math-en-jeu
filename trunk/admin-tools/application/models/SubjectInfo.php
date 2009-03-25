<?php

class SubjectInfo extends Zend_Db_Table_Abstract
{
  protected $_name = 'subject_info';
  protected $_primary = array('subject_id','language_id');

  protected $_referenceMap    = array(
          'Language' => array(
                  'columns'                     => 'language_id',
                  'refTableClass'               => 'Language',
                  'refColumns'                  => 'language_id'
                  ),
          'Category' => array(
                  'columns'                     => 'category_id',
                  'refTableClass'               => 'Category',
                  'refColumns'                  => 'category_id'
                  )
          );
}
