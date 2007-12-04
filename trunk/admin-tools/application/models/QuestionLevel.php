<?php

class QuestionLevel extends Zend_Db_Table_Abstract
{
  protected $_name = 'question_level';

  
  protected $_referenceMap    = array(
        'Level' => array(
            'columns'           => 'level_id',
            'refTableClass'     => 'Level'
        ));

}