<?php
class Zend_View_Helper_ActionName
{
  function actionName()
  {
    $fcontroller = Zend_Controller_Front::getInstance();
    return $fcontroller->getRequest()->getActionName();
  }
}
