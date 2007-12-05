<?php

class QuestionController extends Zend_Controller_Action {
	
	
  function preDispatch() {
    $auth = Zend_Auth::getInstance();
    if (!$auth->hasIdentity()) {
      $this->_redirect('auth/login');
    }
  }

  function init() {
    $this->view->baseUrl = $this->_request->getBaseUrl();
    $this->view->user = Zend_Auth::getInstance()->getIdentity();
    Zend_Loader::loadClass('Zend_Filter');
    
    Zend_Loader::loadClass('Image');
    Zend_Loader::loadClass('Question');
    Zend_Loader::loadClass('Level');
    Zend_Loader::loadClass('AnswerType');
    Zend_Loader::loadClass('AnswerTypeInfo');
    Zend_Loader::loadClass('Category');
    Zend_Loader::loadClass('CategoryInfo');
    Zend_Loader::loadClass('Language');
    Zend_Loader::loadClass('Subject');
    Zend_Loader::loadClass('SubjectInfo');
    Zend_Loader::loadClass('QuestionInfo');
    Zend_Loader::loadClass('QuestionGroupQuestion');
    Zend_Loader::loadClass('QuestionLevel');
    Zend_Loader::loadClass('QuestionFile');
    //Zend_Session_Namespace
    
    $registry = Zend_Registry::getInstance();
    $config = $registry->get('config');
    
    $this->view->flashUrl = $config->file->flash->url;
  }
  
  function listAction() {
    $question = new Question();
    $limit = 10;
    $offset = 0;
    if ($this->_request->isPost()) {
      if ($this->_request->getPost('from') != null) {
        $offset = $this->_request->getPost('from');
      }
    } else {
      if ($this->_request->getParam('from') != null) {
        $offset = $this->_request->getParam('from');
      }
    }
    
    $this->view->questions = $question->fetchAll(null, 'question_id asc',$limit, $offset);
  }
  
  function indexAction() {
    $this->view->title = "Questions index";
    $this->listAction();
    
    
  }
  
  function addAction() {

    $session = new Zend_Session_Namespace('addQuestion');
    
    if ($this->_request->isPost()) {
      if ($this->_request->getPost('step') != null) {
        if ($this->_request->getPost('step') == 1) {
          
          $language = new Language();
          
          $session->language_id = $this->_request->getPost('language');
          $this->view_language_id = $session->language_id;
          
          $row = $language->fetchRow('language_id=' . $session->language_id);
          $session->language_name = utf8_encode($row->name);
          $this->view->language_name = $session->language_name;
          
          $session->language_short_name = $row->short_name;
          $this->view->language_short_name = $session->language_short_name;
          
          $subject = new SubjectInfo();
          $this->view->subjects = $subject->fetchAll("language_id=" . $session->language_id);
          
          $answerTypes = new AnswerTypeInfo();
          $this->view->answerTypes = $answerTypes->fetchAll("language_id=" . $session->language_id);
          
          $this->view->step = 2;
        } elseif ($this->_request->getPost('step') == 2) {
          
          $this->view->language_name = $session->language_name;
          //$this->view->language_short_name = $session->language_short_name;
                    
          //set the value to the session
          $session->subject_id = $this->_request->getPost('subject');
          $this->view->subject_id = $session->subject_id;
          
          $session->answer_type_id = $this->_request->getPost('answerType');
          $this->view->answer_type_id = $session->answer_type_id;
          
          //fetch the name for the subject and the answerType
          $subject = new SubjectInfo();
          $row = $subject->fetchRow('subject_id=' . $session->subject_id . " and language_id=" . $session->language_id);
          $session->subject_name = utf8_encode($row->name);
          $this->view->subject_name = $session->subject_name;
          
          $answerTypes = new AnswerTypeInfo();
          $row = $answerTypes->fetchRow('answer_type_id=' . $session->answer_type_id . " and language_id=" . $session->language_id);
          $session->answer_type_tag = $row->findParentAnswerType()->tag;
          $this->view->answer_type_tag = $session->answer_type_tag;
          
          $session->answer_type_name = utf8_encode($row->name);
          $this->view->answer_type_name = $session->answer_type_name;
          
          $answerTypes = new AnswerType();
          $row = $answerTypes->fetchRow('answer_type_id=' . $session->answer_type_id);
          //$session->answer_type_tag = $row->tag;
          
          //load the category for this subject and language
          $category = new CategoryInfo();
          $this->view->categories = $category->fetchAll("subject_id=" . $session->subject_id . " and language_id=" . $session->language_id);
          
          //set the current step
          $this->view->step = 3;
          
        } elseif ($this->_request->getPost('step') == 3) {
          
          $this->view->language_name = $session->language_name;
          $this->view->answer_type_name = $session->answer_type_name;
          $this->view->answer_type_tag = $session->answer_type_tag;
          $this->view->subject_name = $session->subject_name;
          
          $session->category_id = $this->_request->getPost('category');
          $this->view->category_id = $this->_request->getPost('category');
          
          $category = new CategoryInfo();
          $row = $category->fetchRow('category_id=' . $session->category_id . " and language_id=" . $session->language_id);
          $session->category_name = utf8_encode($row->name);
          $this->view->category_name = $row->name;
          
          //load the possible level
          $level = new Level();
          $this->view->levels = $level->fetchAll('language_id=' . $session->language_id);
          
          $this->view->step = 4;
        } elseif ($this->_request->getPost('step') == 4) {
                    
          $question = new Question();
          $data = array('category_id' => $session->category_id,
                        'answer_type_id' => $session->answer_type_id);

          
          $question_id = $question->insert($data);
          unset($data);
          

          //create the question info data
          $data = array(
          	'question_id' => $question_id,
            'language_id' => $session->language_id,
            'creation_date' => new Zend_Db_Expr('CURDATE()'),
            'question_latex' => $this->_request->getPost('questionLatex'),
            'feedback_latex' => $this->_request->getPost('feedbackLatex'),
            'good_answer' => $this->_request->getPost('goodAnswer'),
            'user_id' => $this->view->user->user_id);
          
          $extraHeaderTex = "";
          $extraTex = "";
          if($this->_request->getPost('choiceA') != null) {
            $data['answer_a_latex'] = $this->_request->getPost('choiceA');
            $data['answer_b_latex'] = $this->_request->getPost('choiceB');
            $data['answer_c_latex'] = $this->_request->getPost('choiceC');
            $data['answer_d_latex'] = $this->_request->getPost('choiceD');
            $extraHeaderTex = '\renewcommand{\labelenumi}{\alph{enumi})}';
            
            $extraTex = '\begin{enumerate}';
            $extraTex .= '\item ' . $this->_request->getPost('choiceA');
            $extraTex .= '\item ' . $this->_request->getPost('choiceB');
            $extraTex .= '\item ' . $this->_request->getPost('choiceC');
            $extraTex .= '\item ' . $this->_request->getPost('choiceD');
            $extraTex .= '\end{enumerate}';
          }
          
          $registry = Zend_Registry::getInstance();
          $config = $registry->get('config');
    
          //try to generate the swf's for this question and feedback
          $filename = $config->file->tempdir . DIRECTORY_SEPARATOR . "Q-" . $question_id . "-" . $session->language_short_name . ".tex";
          
          $questionFile = new QuestionFile($filename);
          $questionFile->addText($config->latex->header);
          $questionFile->addText($extraHeaderTex);
          $questionFile->addText($this->_request->getPost('questionLatex'));
          $questionFile->addText($extraTex);
          $questionFile->addText($config->latex->footer);
          $questionFile->close();
          
          $filename = $config->file->tempdir . DIRECTORY_SEPARATOR . "F-" . $question_id . "-" . $session->language_short_name . ".tex";
          $feedbackFile = new QuestionFile($filename);
          $feedbackFile->addText($config->latex->header);
          $feedbackFile->addText($this->_request->getPost('feedbackLatex'));
          $feedbackFile->addText($config->latex->footer);
          $feedbackFile->close();
          
          //check for eps to include in this swf
          $this->copyImage($this->_request->getPost('questionLatex'));
          $this->copyImage($this->_request->getPost('feedbackLatex'));

          
          $result=0;
          $output = array();
          
          
          $convertmethod = "pdf";
          if ($this->_request->getPost('usejpeg') != null) {
            $convertmethod = "jpeg";
          }
          
          //run the script that build the swf for the question
          exec($config->file->scriptdir . "/tex2swf.sh " . $questionFile->getFullPath() . " " . $config->file->flash->dir . " " . $convertmethod);
          if (file_exists($config->file->flash->dir . DIRECTORY_SEPARATOR . "Q-" . $question_id . "-" . $session->language_short_name . ".swf")) {
            $data['question_flash_file'] = "Q-" . $question_id . "-" . $session->language_short_name . ".swf";
          }
          
          //run the script that build the swf for the feedback
          exec($config->file->scriptdir . "/tex2swf.sh " . $feedbackFile->getFullPath() . " " . $config->file->flash->dir . " " . $convertmethod);
          if (file_exists($config->file->flash->dir . DIRECTORY_SEPARATOR . "F-" . $question_id . "-" . $session->language_short_name . ".swf")) {
            $data['feedback_flash_file'] = "F-" . $question_id . "-" . $session->language_short_name . ".swf";
          }
          
          $questionInfo = new QuestionInfo();
          $questionInfo->insert($data);
          unset($data);
          
          
          //get the level information
          $level = new Level();
          $levels = $level->fetchAll('language_id=' . $session->language_id);
          //$levels = $this->_request->getPost('level');
          $levelValues = $this->_request->getPost('levelValue');
          //add the level value for this question to the database
          if ($levels != null) {
            foreach($levels as $level) {
              $value = $levelValues[$level->level_id];
              $questionLevel = new QuestionLevel();
              $data = array('value' => $value,
                            'level_id' => $level->level_id,
                            'question_id' => $question_id);
              $questionLevel->insert($data);
              unset($data);
            }
          }
          $this->_redirect('question/view/question_id/' . $question_id . '/language_id/' . $session->language_id);
          
        }
      } 
    } else {
      $this->view->step=1;
      $session = new Zend_Session_Namespace('addQuestion');
      $language = new Language();
      $this->view->languages = $language->fetchAll();
    }
    
    $this->view->session = $session;
    
  }
  
  /**
   * Copy the eps found in the text to the temp directory
   *
   * @param unknown_type $text
   */
  private function copyImage($text) {
    $registry = Zend_Registry::getInstance();
    $config = $registry->get('config');
    
    $n = preg_match_all('/\includegraphics.*\{(.*\.eps)\}/i', $text  , $matches);
    for ($i=0;$i < $n; $i++) {
       copy($config->file->image->dir . DIRECTORY_SEPARATOR . $matches[1][$i], 
             $config->file->tempdir . DIRECTORY_SEPARATOR . $matches[1][$i]);
    }

  }
  
  function editAction() {
    $this->view->action = "edit";
    
    if (!$this->_request->isPost()) {
      $question_id = Zend_Filter::get($this->_request->getParam('question_id'), 'Digits');
      $language_id = Zend_Filter::get($this->_request->getParam('language_id'), 'Digits');
      
      
      if ($question_id > 0 && $language_id > 0) {
        
        $question = new Question();
        $questionInfo = new QuestionInfo();
        $rowQ = $question->fetchRow('question_id=' . $question_id);
        $this->view->question_id = $question_id;
        
        $row = $questionInfo->fetchRow('question_id=' . $question_id . ' and ' . 'language_id=' . $language_id);
        
        $this->view->question_latex = $row->question_latex;
        $this->view->feedback_latex = $row->feedback_latex;
        
        $this->view->answer_a_latex = $row->answer_a_latex;
        $this->view->answer_b_latex = $row->answer_b_latex;
        $this->view->answer_c_latex = $row->answer_c_latex;
        $this->view->answer_d_latex = $row->answer_d_latex;

        //assign answer_type_tag
        $answerType = new AnswerTypeInfo();
        $rowAT = $answerType->fetchRow('answer_type_id=' . $rowQ->answer_type_id . ' and language_id=' . $language_id);
        $this->view->answer_type_tag = $rowAT->findParentAnswerType()->tag;
        $this->view->answer_type_name = utf8_encode($rowAT->name);
        
        //assign category
        $categoryInfo = new CategoryInfo();
        $rowSI = $categoryInfo->fetchRow('category_id=' . $rowQ->category_id . ' and language_id=' . $language_id);
        $this->view->category_name = utf8_encode($rowSI->name);
        
        //assign subject
        //$this->view->subject_name = $rowSI->findParentCategory()->findParentSubject()->subject_id;
        
        
        //assign language
        $lang =  new Language();
        $this->view->language_name = utf8_encode($lang->fetchRow('language_id=' . $language_id)->name);
        
        
        //assign level
        $level = new QuestionLevel();
        $this->view->levels = $level->fetchAll('level_id in (select level_id from level where language_id=' . $language_id . ")" .
            " and question_id = " . $question_id);
        
        
      }
    }
  }
  
  function showCommentAction() {
    
  }
  
  function upload() {
     
  }
  
  function deleteAction() {
    
    if (!$this->_request->isPost()) {
      $question_id = Zend_Filter::get($this->_request->getParam('question_id'), 'Digits');
      $language_id = Zend_Filter::get($this->_request->getParam('language_id'), 'Digits');
      if ($question_id > 0 && $language_id > 0) {

        //delete this question from all the group
        $questionGroupQuestion = new QuestionGroupQuestion();
        $where = "question_id=" . $question_id . " and language_id=" . $language_id;
        $questionGroupQuestion->delete($where);
        
        $questionInfo = new QuestionInfo();
        $row = $questionInfo->fetchRow($where);
                
        //delete the file
        $registry = Zend_Registry::getInstance();
        $config = $registry->get('config');
        if (!@unlink($config->file->flash->dir . DIRECTORY_SEPARATOR . $row->question_flash_file)
            || !@unlink($config->file->flash->dir . DIRECTORY_SEPARATOR . $row->feedback_flash_file)) 
        {
          $this->view->message = "Unable to physicaly remove the flash files : <strong>" .
            $config->file->flash->dir . DIRECTORY_SEPARATOR . $row->question_flash_file . "</strong> and " .
            $config->file->flash->dir . DIRECTORY_SEPARATOR . $row->feedback_flash_file . "</strong>";
        } else {

          $questionInfo->delete($where);
          
          //delete information about levels for this question
          $level = new QuestionLevel();
          $where = "level_id in (select level_id from level where level.language_id=" . $language_id 
            . ") and question_id=" . $question_id;
          $level->delete($where);
            
          //delete the Question if no more question info exist for this question
          //$question = new Question();
          if(count($questionInfo->fetchAll('question_id=' . $question_id)) == 0) {
            $question = new Question();
            $question->delete('question_id=' . $question_id);
          }
          $this->view->message = "Question deleted.";
        }
      }
    }
    
    
  }
  
  function viewAction() {
    $question = new Question();
    $questionInfo = new QuestionInfo();
    
    $questionId = 0;
    $languageId = 0;
    if ($this->_request->isPost()) {
      $questionId = $this->_request->getPost('question_id');
      $languageId = $this->_request->getPost('language_id');
    } else {
      $questionId = $this->_request->getParam('question_id');
      $languageId = $this->_request->getParam('language_id');
    }
    
    
    $this->view->question = $question->fetchRow('question_id=' . $questionId);
    $this->view->question_info = $questionInfo->fetchRow('question_id=' . $questionId . ' and language_id=' . $languageId);
    
    $this->view->previous_question = $questionInfo->fetchRow('question_id<' . $questionId . ' and language_id=' . $languageId, 'question_id desc');
    $this->view->next_question = $questionInfo->fetchRow('question_id>' . $questionId . ' and language_id=' . $languageId, 'question_id asc');
    
    
  }
  
  function translateAction() {
    $session = new Zend_Session_Namespace('translateQuestion');
    
    if ($this->_request->isPost()) {
      if ($this->_request->getPost('step') != null) {
        if ($this->_request->getPost('step') == 1) {
          $this->view->step=2;
          $session->from_language_id = $this->_request->getParam('from_language_id');
          $session->to_language_id = $this->_request->getParam('to_language_id');
          
          $questionId = $session->question_id;
          
          $question = new Question();
          $questionInfo = new QuestionInfo();
          
          

          $this->view->question = $question->fetchRow('question_id=' . $questionId);
          $this->view->question_info_from = $questionInfo->fetchRow('question_id=' . $questionId . ' and language_id=' . $session->from_language_id);
          $this->view->question_info_to = $questionInfo->fetchRow('question_id=' . $questionId . ' and language_id=' . $session->to_language_id);
    
          $answerType = new AnswerType();
          $row = $answerType->fetchRow('answer_type_id=' . $this->view->question->answer_type_id);
          $this->view->answer_type_tag = $row->tag;
          
          //load question level informations
          $level = new QuestionLevel();
          $this->view->levels_from = $level->fetchAll('level_id in (select level_id from level where language_id=' . $session->from_language_id . ")" .
            " and question_id = " . $questionId);

          $level = new Level();
          $this->view->levels_to = $level->fetchAll('language_id=' . $session->to_language_id);
          
        } elseif ($this->_request->getPost('step') == 2) {
          
          
          
          
        }
        
      }
      
    } else {
      $this->view->step=1;
      //$session = new Zend_Session_Namespace('translateQuestion');
      $session->question_id = $this->_request->getParam('question_id');
      $languages = new Language();
      $this->view->to_languages = $languages->fetchAll(" language_id not in (select language_id from question_info where question_id=" . $this->_request->getParam('question_id') . ")");  
      $this->view->from_languages = $languages->fetchAll(" language_id in (select language_id from question_info where question_id=" . $this->_request->getParam('question_id') . ")");
    }
    
    
  }
	
}
