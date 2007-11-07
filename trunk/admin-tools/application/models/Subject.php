<?php

class Subject extends Zend_Db_Table_Abstract
{
  protected $_name = 'subject';
  
  protected $_dependentTables = array('SubjectInfo');
}