<?php

class QuestionController extends Zend_Controller_Action {


	function preDispatch() {
		$auth = Zend_Auth::getInstance();
		if (!$auth->hasIdentity()) {
			$this->_redirect('auth/login');
		}
	}

	function init() {
		Zend_Loader::loadClass('Zend_Filter');
		Zend_Loader::loadClass('Image');
		Zend_Loader::loadClass('Language');
		Zend_Loader::loadClass('Level');
		Zend_Loader::loadClass('Question');
		Zend_Loader::loadClass('QuestionInfo');
		Zend_Loader::loadClass('Source');
		Zend_Loader::loadClass('Title');
		Zend_Loader::loadClass('SubjectInfo');
		Zend_Loader::loadClass('CategoryInfo');
		Zend_Loader::loadClass('AnswerTypeInfo');
		Zend_Loader::loadClass('Comment');
		Zend_Loader::loadClass('QuestionFile');
		Zend_Loader::loadClass('QuestionLevel');

                $registry = Zend_Registry::getInstance();
                $config = $registry->get('config');
		$this->view->flashUrl = $config->file->flash->url;
                $this->view->user = Zend_Auth::getInstance()->getIdentity();
                
                $session = new Zend_Session_Namespace('Question');

                //The form return point is used when exiting from the addsource and
                //addtitle actions
                if (!isset($session->formReturnPoint))
                        $session->formReturnPoint='question/';

                if (!isset($session->questionFilter))
                        $session->questionFilter=1;
                if (!isset($session->selectedFilterOptions))
                        $session->selectedFilterOptions = Array('sources'=>array(),
                                                                'titles'=>array(),
                                                                'categories'=>array(),
                                                                'answer_types'=>array(),
                                                                'languages'=>array(),
                                                                'creators'=>array(),
                                                                'keywords'=>array(),
                                                                'is_valid'=>array('0','1'),
                                                                'is_animated'=>array('0','1'));
                        
                //This section populates the filter options with values from the DB
                //Because we don't want to make unnecessary queries to the DB
                //this should only be done on the first index request and
                //after each add/delete/edit/translate operations performed on the questions.
                if (!isset($session->allFilterOptions)) {
                        $language_id = $this->view->user->language_id;
                        $allFilterOptions = array();

                        //Get all _allowable_ languages
                        $languageTable = new Language();
                        $query = $languageTable->select()
                                 ->setIntegrityCheck(false)
                                 ->from($languageTable)
                                 ->joinLeft('language_info',"language.language_id=language_info.language_id && language_info.translation_language_id=$language_id",'name')
                                 ->order('language_info.name');
                        $allFilterOptions['languages'] = $languageTable->fetchAll($query)->toArray();

                        //Get all _allowable_ levels
                        $levelTable = new Level();
                        $allFilterOptions['levels'] = $levelTable->fetchAll("language_id=$language_id", "level_id asc")->toArray();
                        
                        //Get all _allowable_ sources
                        $sourceTable = new Source();
                        $allFilterOptions['sources'] = $sourceTable->fetchAll(null, "name asc")->toArray();

                        //Get all _allowable_ titles
                        $titleTable = new Title();
                        $allFilterOptions['titles'] = $titleTable->fetchAll(null, "name asc")->toArray();

                        //Get all _allowable_ answer_types
                        $answerTypeTable = new AnswerType();
                        $query = $answerTypeTable->select()
                                 ->setIntegrityCheck(false)
                                 ->from($answerTypeTable)
                                 ->joinLeft('answer_type_info',"answer_type.answer_type_id=answer_type_info.answer_type_id && answer_type_info.language_id=$language_id",'name')
                                 ->order('name');
                        $allFilterOptions['answer_types'] = $answerTypeTable->fetchAll($query)->toArray();

                        //Get all _allowable_ (subjects,category) pairs.
                        $categoryTable = new Category();
                        $query = $categoryTable->select()
                                 ->setIntegrityCheck(false)
                                 ->from($categoryTable)
                                 ->joinLeft('subject_info',"category.subject_id=subject_info.subject_id && subject_info.language_id=$language_id", Array('subject_name'=>'name'))
                                 ->joinLeft('category_info',"category.category_id=category_info.category_id && category_info.language_id=$language_id", Array('category_name'=>'name'))
                                 ->order('subject_name')
                                 ->order('subject_id')
                                 ->order('category_name')
                                 ->order('category_id');
                        $allFilterOptions['categories'] = $categoryTable->fetchAll($query)->toArray();

                        //Get all _specified_ question creators
                        //Here getting the _allowable_ values would be silly.  So we only retrieve users who have
                        //actually created questions.
                        $qinfoTable = new QuestionInfo();
                        $query = $qinfoTable->select()
                                 ->setIntegrityCheck(false)
                                 ->from($qinfoTable, 'user_id')
                                 ->group('user_id')
                                 ->joinLeftUsing('user','user_id','username')
                                 ->order('username')
                                 ->order('user_id');
                        $allFilterOptions['question_creators'] = $qinfoTable->fetchAll($query)->toArray();
                        $session->allFilterOptions = $allFilterOptions;
                }
        }

        //Unset the filteredQuestions and allFilterOptions array.
        //This will force them to be set again for the next request.
        private function unsetFilteredInfo() {
                $session = new Zend_Session_Namespace('Question');
                unset($session->allFilterOptions);
                unset($session->filteredQuestions);
                
        }
        
        //Sets the array: selectedFilterOptions with values taken from the view: question/_filter.phtml
        //Sets the variable: questionFilter which is a string that can be use as a 'where' clause to query the database for all question satisfying the filter.
        private function setSelectedFilterOptions(&$request) {
                $selectedFilterOptions = Array('sources'=>array(),
                                               'titles'=>array(),
                                               'categories'=>array(),
                                               'answer_types'=>array(),
                                               'languages'=>array(),
                                               'creators'=>array(),
                                               'keywords'=>array(),
                                               'is_valid'=>array('0','1'),
                                               'is_animated'=>array('0','1'));
                if ($request && $request->isPost()) {
                        $language_array = $request->getPost('languages');
                        $source_array = $request->getPost('sources');
                        $title_array = $request->getPost('titles');
                        $category_array = $request->getPost('categories');
                        $answer_type_array = $request->getPost('answer_types');
                        $creator_array = $request->getPost('creators');
                        $keyword_array = str_word_count($request->getPost('keywords'), 1);
                        $where_array = array();
                        //Get the filter selection for the sources
                        if (!empty($source_array)) {
                                $selectedFilterOptions['sources'] = $source_array;
                                $where_array[] = "question.source_id in (" . implode(',', array_values($source_array)) . ")";
                        }
                        //Get the filter selection for the titles
                        if (!empty($title_array)) {
                                $selectedFilterOptions['titles'] = $title_array;
                                $where_array[] = "question.title_id in (" . implode(',', array_values($title_array)) . ")";
                        }
                        //Get the filter selection for the (subject,category)
                        if (!empty($category_array)) {
                                $selectedFilterOptions['categories'] = $category_array;
                                $where_array[] = "question.category_id in (" . implode(',', array_values($category_array)) . ")";
                        }
                        //Get the filter selection for the answer types
                        if (!empty($answer_type_array)) {
                                $selectedFilterOptions['answer_types'] = $answer_type_array;
                                $where_array[] = "question.answer_type_id in (" . implode(',', array_values($answer_type_array)) . ")";
                        }
                        //Get the filter selection for the languages
                        if (!empty($language_array)) {
                                $selectedFilterOptions['languages'] = $language_array;
                                $where_array[] = "question_info.language_id in (" . implode(',', array_values($language_array)) . ")";
                        }
                        //Get the filter selection for the question creators
                        if (!empty($creator_array)) {
                                $selectedFilterOptions['creators'] = $creator_array;
                                $where_array[] = "question_info.user_id in (" . implode(',', array_values($creator_array)) . ")";
                        }
                        //Get the filter selection for the question creators
                        if (!empty($keyword_array)) {
                                $selectedFilterOptions['keywords'] = $keyword_array;
                                $keywordWhere = "(";
                                $size = count($keyword_array);
                                for($i=0; $i<$size; ++$i) {
                                       $keywordWhere .= "question_info.question_latex like '%".$keyword_array[$i]."%'";
                                       if($i+1 < $size)
                                               $keywordWhere .= " && ";
                                }
                                $keywordWhere .= ")";
                                $where_array[] = $keywordWhere;
                        }
                        //Get the filter selection for 'enabled' questions.
                        $is_valid_array = array();
                        if ($request->getPost('disabled') != null) $is_valid_array[] = "0";
                        if ($request->getPost('enabled') != null) $is_valid_array[] = "1";
                        if (!empty($is_valid_array)) {
                                $selectedFilterOptions['is_valid'] = $is_valid_array;
                                $where_array[] = "question_info.is_valid in (" . implode(',', array_values($is_valid_array)) . ")";
                        }
                        //Get the filter selection for 'animated' questions.
                        $is_animated_array = array();
                        if ($request->getPost('unanimated') != null) $is_animated_array[] = "0";
                        if ($request->getPost('animated') != null) $is_animated_array[] = "1";
                        if (!empty($is_animated_array)) {
                                $selectedFilterOptions['is_animated'] = $is_animated_array;
                                $where_array[] = "question_info.is_animated in (" . implode(',', array_values($is_animated_array)) . ")";
                        }
                        $where_clause = implode(" && ", array_values($where_array));
                }
                if (empty($where_clause))
                        $where_clause="1";
                $session = new Zend_Session_Namespace('Question');
                $session->selectedFilterOptions = $selectedFilterOptions;
                $session->questionFilter = $where_clause;
        }

        //Returns the number of rows satisfying the specified question filter.
        private function countQuestions($questionFilter) {
                $questionTable = new Question();
                $questionQuery = $questionTable->select()
                                 ->setIntegrityCheck(false)
                                 ->from($questionTable,Array())
                                 ->join('question_info','question.question_id=question_info.question_id',Array())
                                 ->joinLeft('category','question.category_id=category.category_id',Array())
                                 ->joinLeft('subject_info','category.subject_id=subject_info.subject_id && question_info.language_id=subject_info.language_id',Array())
                                 ->joinLeft('category_info','question.category_id=category_info.category_id && question_info.language_id=category_info.language_id',Array())
                                 ->joinLeft('source','question.source_id=source.source_id',Array())
                                 ->joinLeft('title','question.title_id=title.title_id',Array())
                                 ->joinLeft('answer_type_info','question.answer_type_id=answer_type_info.answer_type_id && question_info.language_id=answer_type_info.language_id',Array())
                                 ->joinLeft('user','question_info.user_id=user.user_id',Array())
                                 ->where($questionFilter)
                                 ->columns('COUNT(1) as n');
                return $questionTable->fetchRow($questionQuery)->n;
        }

        //Sets the array: filteredQuestions by querying the database with the given filter.
        //The questions are sorted according to the $sortby value
        //Only $limit questions are retreived starting at index $offset (that's because it is
        //unpractical to carry around to many questions in memory).
        private function setFilteredQuestions($questionFilter, $sortby, $limit, $offset) {
                $questionTable = new Question();
                $questionQuery = $questionTable->select()
                                 ->setIntegrityCheck(false)
                                 ->from($questionTable,Array('question_id','category_id','source_id','title_id','answer_type_id'))
                                 ->join('question_info','question.question_id=question_info.question_id',Array('language_id','user_id','question_latex','is_valid','is_animated'))
                                 ->joinLeft('category','question.category_id=category.category_id',Array('subject_id'))
                                 ->joinLeft('subject_info','category.subject_id=subject_info.subject_id && question_info.language_id=subject_info.language_id',Array('subject_name'=>'name'))
                                 ->joinLeft('category_info','question.category_id=category_info.category_id && question_info.language_id=category_info.language_id',Array('category_name'=>'name'))
                                 ->joinLeft('source','question.source_id=source.source_id',Array('source_name'=>'name'))
                                 ->joinLeft('title','question.title_id=title.title_id',Array('title_name'=>'name'))
                                 ->joinLeft('answer_type_info','question.answer_type_id=answer_type_info.answer_type_id && question_info.language_id=answer_type_info.language_id',Array('answer_type_name'=>'name'))
                                 ->joinLeft('user','question_info.user_id=user.user_id',Array('username'))
                                 ->where($questionFilter)
                                 ->order($sortby)
                                 ->limit($limit, $offset);
                $session = new Zend_Session_Namespace('Question');
                $session->filteredQuestions = $questionTable->fetchAll($questionQuery)->toArray();
                $session->numberOfFilteredQuestions = $this->countQuestions($session->questionFilter);
        }


	function indexAction() {
		$this->view->title = "Questions index";
                $session = new Zend_Session_Namespace('Question');

                //This loads the list of all possible options.  It is needed
                //to populate the filter boxes.  It should always be set since
                //it is set up by init().
                $this->view->allFilterOptions = $session->allFilterOptions;
                
                //Store the current page.  There are usually lots of questions
                //satisfying the current filter options so we don't want to return
                //to page 1 every time we click on something.
                if (!isset($session->currentPageNumber))
                        $session->currentPageNumber = 1;

                //Store the first page offset.  Because there are so many questions
                //in the DB, we only retreive a few pages at the time.  This variable
                //stores the index of the first page currently stored.  The actual
                //number of page stored in memory at any time is controlled with the
                //$limit variable below.
                if (!isset($session->firstStoredPage))
                        $session->firstStoredPage = 1;
                
                //We keep an array of allowable 'sort keys'.  If any other sort key
                //is received by the get method, it will be ignored.
                if (!isset($session->sortableKeys))
                        $session->sortableKeys = Array('question_id','source_name','title_name','category_name','subject_name','answer_type_name','username','question_latex');

                //The sortby array stores the sort key and direction.
                if (!isset($session->sortby))
                        $session->sortby = Array('key'=>'question_id','dir'=>'asc');


                //Set the number of questions to display on screen
                $questionsPerPage=10;
                //Set the number of pages of questions to retreive during each query.  We want a number that is large
                //enough so that DB queries don't happen too often but small enough so that it is not a drag to carry
                //into memory.  A rule of thumb is to never carry more then 200 questions, so for a page size of 10
                //we should carry between 1 and 20 pages of questions.
                $limit = 5*$questionsPerPage;


                $request = $this->getRequest();
                //When a sorting request is received, we check if it is valid
                //and re-fetch the questions with the new sorting order;
                if ($request->isGet() && $request->getParam('sortby') != null) {
                        $sortby = $request->getParam('sortby');
                        if (in_array($sortby, $session->sortableKeys))
                        {
                                $currentSort = $session->sortby;
                                if ($sortby == $currentSort['key']) {
                                        if ($currentSort['dir'] == 'asc')
                                                $currentSort['dir'] = 'desc';
                                        else
                                                $currentSort['dir'] = 'asc';
                                }
                                else {
                                        $currentSort['key'] = $sortby;
                                        $currentSort['dir'] = 'asc';
                                }
                                $session->sortby = $currentSort;
                                $session->firstStoredPage = 1;
                                $this->setFilteredQuestions($session->questionFilter, $session->sortby['key'] . " " . $session->sortby['dir'], $limit, 0);
                        }
                }

                //This happens when the "GO!" button is pressed in the question filter box. 
                if ($request->isPost() && $request->getPost('question_filter') != null) {
                        $session->firstStoredPage = 1;
                        $this->setSelectedFilterOptions($request);
                        $this->setFilteredQuestions($session->questionFilter, $session->sortby['key'] . " " .  $session->sortby['dir'], $limit, 0);
                }
                //This happens when the "RESET" button is pressed in the question filter box. 
                if ($request->isPost() && $request->getPost('filter_reset') != null) {
                        $this->setSelectedFilterOptions($no_filter); //the variable $no_filter does not exist anywhere but we must pass something...
                }
                
                //If the questions list has to be updated, get the new list and save them into the session namespace
                //This happens when add/delete/edit/translate/disable actions happen.
                if (!isset($session->filteredQuestions)) {
                        $this->setFilteredQuestions($session->questionFilter, $session->sortby['key'] . " " .  $session->sortby['dir'], $limit, ($session->firstStoredPage-1)*$questionsPerPage);
                }

                $numberOfPages=ceil($session->numberOfFilteredQuestions/$questionsPerPage);
                if ($request->isPost()) {
                        if ($request->getPost('prev') != null)
                                $session->currentPageNumber = max(1,$session->currentPageNumber-1);
                        else if ($request->getPost('next') != null)
                                $session->currentPageNumber = min($numberOfPages,$session->currentPageNumber+1);
                        else if ($request->getPost('page') != null)
                                $session->currentPageNumber = $request->getPost('page');
                        else if ($request->getPost('first') != null)
                                $session->currentPageNumber = 1;
                        else if ($request->getPost('last') != null)
                                $session->currentPageNumber = $numberOfPages;
                        else
                                $session->currentPageNumber = 1;
                }

                //This is executed when we navigate 'past' the current set of stored questions.  When it happens
                //we need to issue a query to get a new buffer of stored questions.
                if ($session->currentPageNumber < $session->firstStoredPage || $session->currentPageNumber > $session->firstStoredPage+$limit/$questionsPerPage-1) {
                        $session->firstStoredPage = $session->currentPageNumber;
                        $this->setFilteredQuestions($session->questionFilter, $session->sortby['key'] . " " .  $session->sortby['dir'], $limit, ($session->firstStoredPage-1)*$questionsPerPage);
                }

                //We pass the number of pages because it is needed to display to combo box that allows 'jumping' to a specified page.
                //We pass the current page so that the combo box knows what the current page is.
                //We pass the number of questions so that we can display that number as information to the user.
                $this->view->numberOfPages = $numberOfPages; 
                $this->view->currentPageNumber = $session->currentPageNumber;
                $this->view->numberOfQuestions = $session->numberOfFilteredQuestions;
                
                //We don't want to display all 'stored' questions so here we specify the RELATIVE index of the questions to be
                //displayed with respect to the buffered question array.  ONLY the questions in [First,last) will be
                //displayed.  Another solution would be to create a sub-array consisting of only the questions to be displayed,
                //perhaps that solution would be more elegant but this will do.
                $this->view->indexOfFirstQuestion = ($session->currentPageNumber-$session->firstStoredPage)*$questionsPerPage;
                $this->view->indexOfLastQuestion = min($session->currentPageNumber*$questionsPerPage, $session->numberOfFilteredQuestions)-($session->firstStoredPage-1)*$questionsPerPage;

                //Obviously we must pass the actual questions and current filter so that everything can be displayed properly.
                $this->view->questions = $session->filteredQuestions;
                $this->view->selectedFilterOptions = $session->selectedFilterOptions;
        }

        //Add a source to the DB if it isn't already present in it.
        function addsourceAction() {
                $this->view->title = "Create a source";
                $session = new Zend_Session_Namespace('Question');
                $this->view->formReturnPoint = $session->formReturnPoint;

                $request = $this->getRequest();
                if ($request->isPost() && $request->getPost('addSource') != null) {
                        $name = trim($request->getPost('sourceName'));
                        if ($name != "") {
                                $sourceTable = new Source();
                                $source = $sourceTable->fetchRow("name='$name'");
                                if ($source == null) {
                                        $sourceTable->insert(array('name'=>$name));
                                        $this->unsetFilteredInfo();
                                }
                        }
                        $this->_redirect($session->formReturnPoint);
                }
        }

        //Add a title to the DB if it isn't already present in it.
        function addtitleAction() {
                $this->view->title = "Create a title";
                $session = new Zend_Session_Namespace('Question');
                $this->view->formReturnPoint = $session->formReturnPoint;

                $request = $this->getRequest();
                if ($request->isPost() && $request->getPost('addTitle') != null) {
                        $name = trim($request->getPost('titleName'));
                        if ($name != "") {
                                $titleTable = new Title();
                                $title = $titleTable->fetchRow("name='$name'");
                                if ($title == null) {
                                        $titleTable->insert(array('name'=>$name));
                                        $this->unsetFilteredInfo();
                                }
                        }
                        $this->_redirect($session->formReturnPoint);
                }
        }

        function addAction() {
                $this->view->title = "Add a question to the database";
                $session = new Zend_Session_Namespace('Question');
                //this is used so that we can return to the proper controller after a user
                //creates a 'source' or a 'title'.  Creating a source or title is done via
                //the view: question/_form.phtml
                $session->formReturnPoint = 'question/add';

                //By default we start with 2 empty choices in a multiple choice question.
                $answers = array('0'=>array(), '1'=>array());
                $question = array('answer_type_id'=>1);
                $levels = array();
                
                $request = $this->getRequest();
                if ($request->isPost())
                {
                        //This reads in all the data gathered from the view: question/_form.phtml
                        $this->getQuestionInfoFromDisplayForm($request, $question, $answers, $levels, $conversionOptions);
                        if ($request->getPost('addQuestionToDB') != null) {
                                $errorMessage = $this->validateQuestionInfo($question, $answers, $levels);
                                if (empty($errorMessage)) {
                                        $question_id = $this->insertQuestionInfoInDatabase($question, $answers, $levels);
                                        $this->generateFlashFiles($question_id, $question['language_id'], $conversionOptions);
                                        $this->unsetFilteredInfo();
                                        $this->_redirect('question/view/question_id/'.$question_id.'/language_id/'.$question['language_id']);
                                }
                                $this->view->warning = $errorMessage;
                        }
                }

                $this->view->question = $question;
                $this->view->answers = $answers;
                $this->view->levels = $levels;
                //This loads the list of all possible options.  It is needed
                //to populate the list boxes used to select the question information.
                $this->view->allFilterOptions = $session->allFilterOptions;
        }

	function editAction() {
                $this->view->title = "Modify a question";
                $request = $this->getRequest();
                $session = new Zend_Session_Namespace('Question');
                
                //The request is a Post request only when the user changes the answer type
                //(that's because the form looks different for different answer types and must
                //be redisplayed.) or when the "Write to DB" button is pressed.
                if ($request->isPost()) {
                        $this->getQuestionInfoFromDisplayForm($request, $question, $answers, $levels, $conversionOptions);
                        if ($request->getPost('updateQuestion') != null) {
                                $errorMessage = $this->validateQuestionInfo($question, $answers, $levels);
                                if (empty($errorMessage)) {
                                        $this->updateQuestionInfoInDatabase($question, $answers, $levels);
                                        $this->view->warning=$this->generateFlashFiles($question['question_id'], $question['language_id'], $conversionOptions);
                                        $this->unsetFilteredInfo();
                                        $this->_redirect('question/view/question_id/'.$question['question_id'].'/language_id/'.$question['language_id']);
                                }
                                else
                                        $this->view->warning = $errorMessage;
                        }
                }
                //This is the normal entry point.  An edit request was made via the url, most
                //likely by following a link, but possibly when a user manually edited the url.
                else {
                        $question_id = Zend_Filter::get($request->getParam('question_id'), 'Digits');
                        $language_id = Zend_Filter::get($request->getParam('language_id'), 'Digits');
                        if ($question_id == null || $language_id == null)
                                $this->_redirect('question/');
                        $session->formReturnPoint = "question/edit/question_id/$question_id/language_id/$language_id";
                        $question = $this->findQuestionInfo($question_id, $language_id);
                        $answers = $this->findQuestionAnswers($question_id, $language_id);
                        $levels = $this->findQuestionLevels($question_id);
                }
                $this->view->question = $question;
                $this->view->answers = $answers;
                $this->view->levels = $levels;
                //This loads the list of all possible options.  It is needed
                //to populate the list boxes used to edit the question information.
                $this->view->allFilterOptions = $session->allFilterOptions;
	}


	function translateAction() {
                $this->view->title = "Translate a question";
                $request = $this->getRequest();                
                $session = new Zend_Session_Namespace('Question');
                
                if ($request->isPost()) {
                        $this->getQuestionInfoFromDisplayForm($request, $question, $answers, $levels, $conversionOptions);
                        if ($request->getPost('translateQuestion') != null) {
                                $errorMessage = $this->validateQuestionInfo($question, $answers, $levels);
                                if (empty($errorMessage)) {
                                        $this->insertQuestionInfoInDatabase($question, $answers, $levels);
                                        $this->generateFlashFiles($question['question_id'], $question['language_id'], $conversionOptions);
                                        $this->unsetFilteredInfo();
                                        $this->_redirect('question/view/question_id/'.$question['question_id'].'/language_id/'.$question['language_id']);
                                }
                                else
                                        $this->view->warning = $errorMessage;
                        }
                } else {
                        $question_id = Zend_Filter::get($request->getParam('question_id'), 'Digits');
                        $from_language_id = Zend_Filter::get($request->getParam('language_id'), 'Digits');
                        if ($question_id == null || $from_language_id == null)
                                $this->_redirect('question/');
                        $to_language_id = 3-$from_language_id;
                        $translatedQuestion = $this->findQuestionInfo($question_id, $to_language_id);
                        if (isset($translatedQuestion['language_id']))
                                $this->_redirect('question/edit/question_id/'.$question_id.'/language_id/'.$to_language_id);
                        
                        $session->formReturnPoint = "question/translate/question_id/$question_id/language_id/$from_language_id";
                        $question = $this->findQuestionInfo($question_id, $from_language_id);
                        if ($question == null)
                                $this->_redirect('question/');
                        $question['language_id'] = $to_language_id;
                        $question['question_latex'] = "";
                        $question['question_flash_file'] = "";
                        $question['feedback_latex'] = "";
                        $question['feedback_flash_file'] = "";
                        $question['comment'] = array();
                        $answers = $this->findQuestionAnswers($question_id, $from_language_id);
                        foreach ($answers as &$answer) {
                                $answer['answer_latex'] = "";
                                $answer['answer_flash_file'] = "";
                        }
                        $levels = $this->findQuestionLevels($question_id);
                }
                $this->view->question = $question;
                $this->view->answers = $answers;
                $this->view->levels = $levels;
                //This loads the list of all possible options.  It is needed
                //to populate the list boxes used to edit the question information.
                $this->view->allFilterOptions = $session->allFilterOptions;
        }

	function deleteAction() {
                $this->view->title = "Delete a question from the database";
                $request = $this->getRequest();
                $question_id = Zend_Filter::get($request->getParam('question_id'), 'Digits');
                $language_id = Zend_Filter::get($request->getParam('language_id'), 'Digits');
                if ($question_id == null || $language_id == null)
                        $this->_redirect('question/');

		$questionInfoTable = new QuestionInfo();
		$questionInfoRow = $questionInfoTable->fetchRow('question_id=' . $question_id . ' && language_id=' . $language_id);

                if (!isset($questionInfoRow))
                        $this->_redirect('question/');
                
                //There are two cases to be checked.
                //1) If we are deleting the last translation of a question.  We remove the entire question from the DB
                //2) If a translation exist, we only delete the requested translation from the DB.
                if ($request->getPost('deleteQuestion') != null) {
                        //delete the flash files
                        $registry = Zend_Registry::getInstance();
                        $config = $registry->get('config');
                        $filesToDelete = array();
                        if ($questionInfoRow->question_flash_file != null)
                                $filesToDelete[] = $config->file->flash->dir . DIRECTORY_SEPARATOR . $questionInfoRow->question_flash_file;
                        if ($questionInfoRow->feedback_flash_file != null)
                                $filesToDelete[] = $config->file->flash->dir . DIRECTORY_SEPARATOR . $questionInfoRow->feedback_flash_file;
                        $answerInfoTable = new AnswerInfo();
                        $answerInfoRowSet = $answerInfoTable->fetchAll('question_id=' . $questionInfoRow->question_id . ' && language_id=' . $questionInfoRow->language_id);
                        foreach($answerInfoRowSet as $answerInfoRow)
                                if ($answerInfoRow->answer_flash_file != null)
                                        $filesToDelete[] = $config->file->flash->dir . DIRECTORY_SEPARATOR . $answerInfoRow->answer_flash_file;
                        $message = $this->deleteFiles($filesToDelete);

                        //delete the DB data
                        $siblingsRowSet = $questionInfoTable->fetchAll('question_id=' . $question_id);
                        if (count($siblingsRowSet) == 1) { //1 means there's only one child i.e. this question is not translated
                                $questionTable = new Question();
                                $questionTable->fetchRow('question_id='.$question_id)->delete(); //this cascades in all tables thanks to InnoDB
                        }
                        else
                                //Because 'answer_info(question_id,language_id)' is a foreign key reference to 'question_info(question_id,language_id)'
                                //this cascades in 'answer_info' thanks to InnoDB
                                $questionInfoRow->delete();

                        //Unset the filtered data to force a recompute
                        $this->unsetFilteredInfo();
                        if (!empty($message))
                                $this->view->warning = $message;
                        else
                                $this->_redirect('question/');
                }
                else if ($request->getPost('keepQuestion') != null)
                        $this->_redirect('question/');
                
                //We only get here when delete page is initially requested
                $this->view->question = $this->findQuestionInfo($question_id, $language_id);
                $this->view->answers = $this->findQuestionAnswers($question_id, $language_id);
	}

	function viewAction() {
                $this->view->title = "Question viewer";
                $request = $this->getRequest();

                $session = new Zend_Session_Namespace('Question');
                if ($request->isPost()) {
                        $question_id = $request->getPost('question_id');
                        $language_id = $request->getPost('language_id');
                        if ($request->getPost('edit') != null)
                                $this->_redirect('question/edit/question_id/'.$question_id.'/language_id/'.$language_id);
                        else if ($request->getPost('translate') != null)
                                $this->_redirect('question/translate/question_id/'.$question_id.'/language_id/'.$language_id);
                        else if ($request->getPost('delete') != null)
                                $this->_redirect('question/delete/question_id/'.$question_id.'/language_id/'.$language_id);
                        else if ($request->getPost('disable') != null)
                                $this->_redirect('question/disable/question_id/'.$question_id.'/language_id/'.$language_id);
                        else if ($request->getPost('upload') != null)
                                $this->_redirect('question/upload/question_id/'.$question_id.'/language_id/'.$language_id);
                        else if ($request->getPost('prev') != null) {
                                $row_number = $this->lookupFilteredQuestionIndex($question_id, $language_id);
                                $n = max(0, $row_number-1);
                                $question = $session->filteredQuestions[$n];
                                $question_id = $question['question_id'];
                                $language_id = $question['language_id'];
                        }                        
                        else if ($request->getPost('next') != null) {
                                $row_number = $this->lookupFilteredQuestionIndex($question_id, $language_id);
                                $n = min(count($session->filteredQuestions)-1, $row_number+1);
                                $question = $session->filteredQuestions[$n];
                                $question_id = $question['question_id'];
                                $language_id = $question['language_id'];
                        }
                        else if ($request->getPost('first') != null) {
                                $question = $session->filteredQuestions[0];
                                $question_id = $question['question_id'];
                                $language_id = $question['language_id'];
                        }
                        else if ($request->getPost('last') != null) {
                                $n = count($session->filteredQuestions)-1;
                                $question = $session->filteredQuestions[$n];
                                $question_id = $question['question_id'];
                                $language_id = $question['language_id'];
                        }

                }
                else {
                        $question_id = Zend_Filter::get($request->getParam('question_id'), 'Digits');
                        $language_id = Zend_Filter::get($request->getParam('language_id'), 'Digits');
                        if ($question_id == NULL || $language_id == NULL)
                                $this->_redirect('question/');
                }
                
                $this->view->question = $this->findQuestionInfo($question_id, $language_id);
                $this->view->answers = $this->findQuestionAnswers($question_id, $language_id);
                $this->view->show_nav_controls = isset($session->filteredQuestions);
        }

        function disableAction() {
                $request = $this->getRequest();
                $question_id = Zend_Filter::get($request->getParam('question_id'), 'Digits');
                $language_id = Zend_Filter::get($request->getParam('language_id'), 'Digits');
                if ($question_id == NULL || $language_id == NULL)
                        $this->_redirect('question/');
                $questionInfoTable = new QuestionInfo();
		$questionInfoRow = $questionInfoTable->fetchRow('question_id=' . $question_id . ' && language_id=' . $language_id);
                if ($questionInfoRow == null)
                        $this->_redirect('question/');
                $questionInfoRow->is_valid = 1-$questionInfoRow->is_valid;
                $questionInfoRow->save();
                $this->unsetFilteredInfo();
                $this->_redirect('question/');
        }

        function uploadAction() {
		$this->view->title = "Upload a swf file";

                $request = $this->getRequest();
                $question_id = Zend_Filter::get($request->getParam('question_id'), 'Digits');
                $language_id = Zend_Filter::get($request->getParam('language_id'), 'Digits');
                if ($question_id == NULL || $language_id == NULL) 
                        $this->_redirect('question/');

                $questionInfoTable = new QuestionInfo();
		$qinfoRow = $questionInfoTable->fetchRow('question_id=' . $question_id . ' && language_id=' . $language_id);
                if ($qinfoRow == null)
                        $this->_redirect('question/');

                $message = "";
                if ($request->isPost()) {
 			if (is_uploaded_file($_FILES["file"]["tmp_name"])) {
                                $registry = Zend_Registry::getInstance();
                                $config = $registry->get('config');
                                $path_info = pathinfo($_FILES["file"]["name"]);
                                $filename = $config->file->flash->dir . DIRECTORY_SEPARATOR . $path_info['basename'];
                                if (move_uploaded_file($_FILES["file"]["tmp_name"], $filename)) {
                                        switch ($request->getPost("column")) {
                                          case "question":
                                            $qinfoRow->question_flash_file=$path_info['basename'];
                                            break;
                                          case "feedback":
                                            $qinfoRow->feedback_flash_file=$path_info['basename'];
                                            break;
                                        }
                                        $qinfoRow->save();
                                        $this->_redirect('question/view/question_id/'.$question_id.'/language_id/'.$language_id);
                                }
                        }
                        $message .= "SWF file $filename could not be uploaded";
                }
                $this->view->message = $message;
                $this->view->question = $this->findQuestionInfo($question_id, $language_id);
                $this->view->answers = $this->findQuestionAnswers($question_id, $language_id);
        }
        
        //Read information gathered from views/scripts/question/_form.phtml
        private function getQuestionInfoFromDisplayForm(&$request, &$question, &$answers, &$levels, &$conversionOptions) {

                //The information should only be gathered if a button has been pressed.
                if (!$request->isPost()) return;
                $question = array();
                //Read in the question information
                $question['question_id'] = $request->getPost('question_id'); //this could be null
                $question['language_id'] = $request->getPost('language_id');
                $question['source_id'] = $request->getPost('source_id');
                $question['title_id'] = $request->getPost('title_id');
                $question['category_id'] = $request->getPost('category_id');
                $question['answer_type_id'] = $request->getPost('answer_type_id');
                $question['question_latex'] = trim($request->getPost('question_latex'));
                $question['feedback_latex'] = trim($request->getPost('feedback_latex'));
                $question['comment'] = $request->getPost('comment');
                //Read in the answers information
                //For True/False questions
                //   $answers[0]['is_right'] is one of 0 or 1 meaning false and true respectively.
                //For Short Answer questions
                //   $answers[0]['answer_latex'] is the string representing the answer.
                //For Multiple choice questions
                //   $answers is an array of answers (each answer being an array in itself)
                //   and an additional parameter called 'rightAnswer' is also defined.
                //In all cases 'answer_id' may or may not be defined.
                $answers = $request->getPost('answers');
                if ($question['answer_type_id'] == 1) {
                        $right_answer = $request->getPost('rightAnswer');
                        foreach(array_keys($answers) as $index)
                                if ($right_answer != null && $index == $right_answer)
                                        $answers[$index]['is_right'] = 1;
                                else
                                        $answers[$index]['is_right'] = 0;
                }

                //Read in the level information.  $levels is an array that associates a
                //level_id to a difficulty value taken in {0,1,2,...,6}.
                $levels = $request->getPost('levels');

                //Read the conversion options.
                if ($request->getPost('noswf') != null)
                        $conversionOptions = 'noswf';
                else if ($request->getPost('usejpeg') != null)
                        $conversionOptions = 'usejpeg';
                else
                        $conversionOptions = 'pdf';
                
                //A request was made to increase the number of answers for a multiple choice question
                //The request is granted only if the number of answer is less than 8
                if ($request->getPost('addMCAnswer') != null) {
                        if (count($answers) < 8)
                                $answers[] = array();
                }
                //A request was made to decrease the number of answers for a multiple choice question
                //The request is granted only if the number of answer is greater than 2
                else if ($request->getPost('deleteMCAnswer') != null) {
                        if (count($answers) > 2) {
                                $index = array_keys($request->getPost('deleteMCAnswer'));
                                $index = $index[0];
                                unset($answers[$index]);
                        }
                }
                //The answer type has changed.  Reset the answer to the default for each answer type.
                else if ($request->getPost('answerTypeChanged') != null) {
                        switch ($question['answer_type_id']) {
                          case 1: $answers = array('0'=>array(), '1'=>array()); break;
                          case 2: $answers = array('0'=>array('is_right'=>1)); break;
                          case 3: $answers = array('0'=>array('answer_latex'=>"")); break;
                        }
                }
        }

        //Make sure the question, answer and level all contain valid data so that the DB can be updated.
        private function validateQuestionInfo($question, $answers, $levels) {
                if ($question['question_latex']=="") return "Invalid question: No LaTeX specified for question.";
                if ($question['feedback_latex']=="") return "Invalid question: No LaTeX specified for feedback.";
                $answertype = $question['answer_type_id'];
                switch ($answertype) {
                  case 1: //Multiple choice
                    $rightAnswerCount=0;
                    $i=1;
                    foreach ($answers as $answer) {
                            if (strlen(trim($answer['answer_latex'])) == 0) return "Invalid question: No LaTeX specified for answer#".$i;
                            $rightAnswerCount += $answer['is_right'];
                            ++$i;
                    }
                    if ($rightAnswerCount != 1) return "Invalid question: You must specify exactly one right answer for multiple choice questions";
                    break;
                  case 2: //True or false
                    if ($answers[0]['is_right'] != "0" && $answers[0]['is_right'] != "1") return "Invalid question: Answers for true/false question must be either 0 or 1";
                    break;
                  case 3: //Short answser
                    if (strlen(trim($answers[0]['answer_latex'])) == 0) return "Invalid question: Short answer not specified";
                    break;
                }
        }


        //Updates the DB with the given data.  It is assumed that the parameters have been validated
        //via the validateQuestionInfo function.
        private function updateQuestionInfoInDatabase($question, $answers, $levels) {
                $questionTable = new Question();
                $originalQuestion = $questionTable->fetchRow('question_id='.$question['question_id'])->toArray();
                $questionTable->update(array('category_id' => $question['category_id'],
                                             'answer_type_id' => $question['answer_type_id'],
                                             'source_id' =>  $question['source_id'],
                                             'title_id' => $question['title_id']),
                                       array('question_id='.$question['question_id']));

                $questionInfoTable = new QuestionInfo();
                $questionInfoTable->update(array('question_latex' => $question['question_latex'],
                                                 'feedback_latex' => $question['feedback_latex'],
                                                 'last_modified' => new Zend_Db_Expr('CURDATE()')),
                                           array('question_id='.$question['question_id'],'language_id='.$question['language_id']));

                $answerTable = new Answer();
                //if the answer type has changed all answers in the db for this question are invalid so delete them
                $filesToDelete = array();
                if ($originalQuestion['answer_type_id'] != $question['answer_type_id']) {
                        $answerInfoTable = new AnswerInfo();
                        foreach ($answerInfoTable->fetchAll('question_id='.$question['question_id']) as $answerInfoRow)
                                if ($answerInfoRow->answer_flash_file != null)
                                        $filesToDelete[] = $answerInfoRow->answer_flash_file;
                        $answerTable->delete('question_id='.$question['question_id']); //this cascades into all tables thanks to InnoDB
                }
                $originalAnswerRowSet = $answerTable->fetchAll('question_id='.$question['question_id']);
                //if some of the answers were removed (from the webpage itself this is only possible by clicking on an answer's "delete" 
                //button in question/_form.phtml), delete them from the answer table.
                //WARNING: ugly quadratic search.  Fortunately most questions have 1-5 answers.  Max is 8.
                foreach ($originalAnswerRowSet as $originalAnswerRow) {
                        $found = false;
                        foreach($answers as $n_answer) {
                                if ($originalAnswerRow->answer_id == $n_answer['answer_id']) {
                                        $found = true;
                                        break;
                                }
                        }
                        if (!$found) {
                                $answerInfoTable = new AnswerInfo();
                                foreach ($answerInfoTable->fetchAll('question_id='.$question['question_id']) as $answerInfoRow)
                                        if ($answerInfoRow->answer_flash_file != null)
                                                $filesToDelete[] = $answerInfoRow->answer_flash_file;
                                $originalAnswerRow->delete(); //this cascades into all tables thanks to InnoDB
                        }
                }

                $this->deleteFiles($filesToDelete);

                //At this point all the answers in $answers are either new or already exist in the DB **AND**
                //there are no answers in the DB for this question that are not in $answers.
                $answerInfoTable = new AnswerInfo();
                switch ($question['answer_type_id']) {
                  case 1: //Multiple choices
                    foreach ($answers as $answer) {
                            //answer id isn't known means this is a new answer
                            if ($answer['answer_id'] == null) { 
                                    $answer_id = $answerTable->insert(array('question_id'=>$question['question_id'],
                                                                            'is_right'=>$answer['is_right'],
                                                                            'label'=>$answer['label']));
                                    $answerInfoTable->insert(array('answer_id' => $answer_id,
                                                                   'question_id' => $question['question_id'],
                                                                   'language_id' => $question['language_id'],
                                                                   'answer_latex' => $answer['answer_latex']));
                            }
                            else {
                                    $answerTable->update(array('is_right'=>$answer['is_right'],
                                                               'label'=>$answer['label']),
                                                         array('answer_id='.$answer['answer_id']));
                                    $answerInfoTable->update(array('answer_latex' => $answer['answer_latex']),
                                                             array('answer_id='.$answer['answer_id'],'language_id='.$question['language_id']));
                            }
                    }
                    break;
                  case 2: //True or false
                    if ($answers[0]['answer_id'] == null)
                            $answerTable->insert(array('question_id' => $question['question_id'],
                                                       'is_right' => $answers[0]['is_right']));
                    else
                            $answerTable->update(array('is_right' => $answers[0]['is_right']),
                                                 array('answer_id='.$answers[0]['answer_id']));
                    break;
                  case 3: //Short answer
                    if ($answers[0]['answer_id'] == null) {
                            $answer_id = $answerTable->insert(array('question_id' => $question['question_id'],
                                                                    'is_right' => 1));
                            $answerInfoTable->insert(array('answer_id' => $answer_id,
                                                           'question_id' => $question['question_id'],
                                                           'language_id' => $question['language_id'],
                                                           'answer_latex' => $answers[0]['answer_latex']));
                    }
                    else
                            $answerInfoTable->update(array('answer_latex' => $answers[0]['answer_latex']),
                                                     array('answer_id='.$answers[0]['answer_id'],'language_id='.$question['language_id']));
                    break;
                }

                //Update the data to the 'question_level' table.
                //We only store levels for which the value is non-zero.
                $questionLevelTable = new QuestionLevel();
                $questionLevelTable->delete('question_id='.$question['question_id']);
                foreach($levels as $level_id=>$value) {
                        if ($value != 0)
                                $questionLevelTable->insert(array('question_id'=>$question['question_id'],
                                                                  'level_id'=>$level_id,
                                                                  'value'=>$value));
                }
        }
        
        //Inserts the given data in the DB.  It is assumed that the parameters have been validated
        //via the validateQuestionInfo function.
	private function insertQuestionInfoInDatabase($question, $answers, $levels) {
                //Add the data to the 'question' table if it doesn't already exists.
                //The data already exists when this function is called via 'translateAction'
                //and it does not already exists when it is called via 'addAction'
                $questionTable = new Question();
                if ($question['question_id'] != null)
                        $question_id = $question['question_id'];
                else
                        $question_id = $questionTable->insert(array('category_id' => $question['category_id'],
                                                                    'answer_type_id' => $question['answer_type_id'],
                                                                    'source_id' =>  $question['source_id'],
                                                                    'title_id' => $question['title_id']));

                //Add the data to the 'question_info' table
                $questionInfoTable = new QuestionInfo();
                $questionInfoTable->insert(array('question_id' => $question_id,
                                                 'language_id' => $question['language_id'],
                                                 'question_latex' => $question['question_latex'],
                                                 'feedback_latex' => $question['feedback_latex'],
                                                 'is_valid' => 0,
                                                 'user_id' => $this->view->user->user_id,
                                                 'creation_date' => new Zend_Db_Expr('CURDATE()'),
                                                 'is_animated' => 0));
                
                //Add the data to the 'answer' and 'answer_info' tables
                //For True/False questions
                //        'answer'.'is_right' tells you if the answer is true or false
                //        'answer'.'label' is unused
                //        there is no need to put anything in the 'answer_info table
                //For Short Answer questions 
                //        'answer'.'is_right' is always true
                //        'answer'.'label' is unused
                //        'answer_info'.'answer_latex' contains the answer, although it is not formatted for latex.
                $answerTable = new Answer();
                $answerInfoTable = new AnswerInfo();
                switch ($question['answer_type_id']) {
                  case 1: //Multiple choices
                    foreach ($answers as $answer) {
                            //Add the answer to the 'answer' table if it doesn't already exists.
                            //The answer already exists when this function is called via 'translateAction'
                            //and it does not already exists when it is called via 'addAction'
                            if ($answer['answer_id'] != null)
                                    $answer_id = $answer['answer_id'];
                            else
                                    $answer_id = $answerTable->insert(array('question_id'=>$question_id,
                                                                            'is_right'=>$answer['is_right'],
                                                                            'label'=>$answer['label']));
                            $answerInfoTable->insert(array('answer_id' => $answer_id,
                                                           'question_id'=> $question_id,
                                                           'language_id' => $question['language_id'],
                                                           'answer_latex' => $answer['answer_latex']));
                    }
                    break;
                  case 2: //True or false
                    if ($answers[0]['answer_id'] == null)
                            $answerTable->insert(array('question_id' => $question_id,
                                                       'is_right' => $answers[0]['is_right']));
                    break;
                  case 3: //Short answer
                    if ($answers[0]['answer_id'] != null)
                            $answer_id = $answers[0]['answer_id'];
                    else
                            $answer_id = $answerTable->insert(array('question_id' => $question_id,
                                                                    'is_right' => 1));
                    $answerInfoTable->insert(array('answer_id' => $answer_id,
                                                   'question_id' => $question_id,
                                                   'language_id' => $question['language_id'],
                                                   'answer_latex' => $answers[0]['answer_latex']));
                    break;
                }
                
                if ($question['question_id'] == null) {
                        //Add the data to the 'question_level' table.
                        //We only store levels for which the value is non-zero.
                        $questionLevelTable = new QuestionLevel();
                        foreach($levels as $level_id=>$value) {
                                if ($value != 0)
                                        $questionLevelTable->insert(array('question_id'=>$question_id,
                                                                          'level_id'=>$level_id,
                                                                          'value'=>$value));
                        }
                }
                return $question_id;
        }

        private function deleteFiles($filenames) {
                $message="";
                foreach ($filenames as $filename)
                        if (!@unlink($filename))
                                $message .= "Unable to physicaly remove the files : <strong>" . $filename . "</strong><br/>";
                return $message;
        }

        private function generateFlashFiles($question_id, $language_id, $conversionOptions) {
        
                if ($conversionOptions == 'noswf')
                        return;

                $questionTable = new Question();
                $questionRow = $questionTable->fetchRow("question_id=$question_id");

                $questionInfoTable = new QuestionInfo();
		$qinfo = $questionInfoTable->fetchRow('question_id=' . $question_id . ' && language_id=' . $language_id);
                if ($qinfo == null)
                        return;
                $languageTable = new Language();
                $language = $languageTable->fetchRow('language_id='.$language_id);

                $registry = Zend_Registry::getInstance();
                $config = $registry->get('config');

                $this->deleteFiles(array($config->file->flash->dir.$qinfo->feedback_flash_file, $config->file->flash->dir.$qinfo->question_flash_file));
                
                //////////////////////////////////////////////////////////
                /// Generate the feedback swf
                $filename = $config->file->tempdir . "Q-" . $question_id . "-F-" . $language->short_name . ".tex";
                $questionFile = new QuestionFile($filename);
                $questionFile->addText($config->latex->header . "\n");
                $questionFile->addText($qinfo->feedback_latex . "\n");
                $questionFile->addText($config->latex->footer . "\n");
                $questionFile->close();
                $this->copyImage($qinfo->feedback_latex);
                //run the script that build the swf for the question
                exec($config->file->scriptdir . "tex2swf.sh " . $questionFile->getFullPath() . " " . $config->file->flash->dir . " " . $conversionOptions);
                if (file_exists($config->file->flash->dir . "Q-" . $question_id . "-F-" . $language->short_name . ".swf"))
                        $qinfo->feedback_flash_file = "Q-" . $question_id . "-F-" . $language->short_name . ".swf";
                else
                        $qinfo->feedback_flash_file = "echec.swf";
                //////////////////////////////////////////////////////////
                /// Generate the question swf (with answers)
                $filename = $config->file->tempdir . "Q-" . $question_id . "-" . $language->short_name . ".tex";
                $questionFile = new QuestionFile($filename);
                $questionFile->addText($config->latex->header . "\n");
                $questionFile->addText($qinfo->question_latex . "\n");
                switch($questionRow->answer_type_id) {
                        case 1: //Multiple choices
                                $answerTable = new Answer();
                                $answerRowSet = $answerTable->fetchAll("question_id=$question_id");
                                $questionFile->addText("\begin{mej-enumerate}");
                                foreach($answerRowSet as $answerRow) {
                                        $answerInfo = $answerRow->findAnswerInfo($answerTable->select()->where("language_id=$language_id"))->current();
                                        if ($answerInfo == null)
                                                continue;
                                        $questionFile->addText("\item " . $answerInfo->answer_latex . "\n");
                                }
                                $questionFile->addText("\end{mej-enumerate}");
                                break;
                        case 2: //True of false
                                $questionFile->addText("\begin{mej-enumerate}");
                                $questionFile->addText("\item " . (($language_id==1)?"Vrai":"True"));
                                $questionFile->addText("\item " . (($language_id==1)?"Faux":"False"));
                                $questionFile->addText("\end{mej-enumerate}");
                                break;
                        case 3: //Short answer
                                break;
                }
                $questionFile->addText($config->latex->footer . "\n");
                $questionFile->close();
                $this->copyImage($qinfo->question_latex);
                //run the script that build the swf for the question
                exec($config->file->scriptdir . "tex2swf.sh " . $questionFile->getFullPath() . " " . $config->file->flash->dir . " " . $conversionOptions);
                if (file_exists($config->file->flash->dir . "Q-" . $question_id . "-" . $language->short_name . ".swf"))
                        $qinfo->question_flash_file = "Q-" . $question_id . "-" . $language->short_name . ".swf";
                else
                        $qinfo->question_flash_file = "echec.swf";
                
                $qinfo->save();
                /// Generate the answers swf
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
		for ($i=0; $i<$n; $i++) {
			copy($config->file->image->dir . DIRECTORY_SEPARATOR . $matches[1][$i],
                             $config->file->tempdir . DIRECTORY_SEPARATOR . $matches[1][$i]);
		}
	}

        //Returns an array filled with the question information for this $question_id and $language_id
        private function findQuestionInfo($question_id, $language_id) {
                $questionTable = new Question();
                $questionRow = $questionTable->fetchRow("question_id=$question_id");
                $question = array();
                if ($questionRow != null) {
                        $question = $questionRow->toArray();
                        $questionInfoRow = $questionRow->findQuestionInfo($questionTable->select()->where("language_id=$language_id"))->current();
                        if ($questionInfoRow != null) {
                                $question['question_id'] = $questionInfoRow->question_id;
                                $question['language_id'] = $questionInfoRow->language_id;
                                $question['answer_type_id'] = $questionRow->answer_type_id;
                                $question['question_latex'] = $questionInfoRow->question_latex;
                                $question['question_flash_file'] = $questionInfoRow->question_flash_file;
                                $question['feedback_latex'] = $questionInfoRow->feedback_latex;
                                $question['feedback_flash_file'] = $questionInfoRow->feedback_flash_file;
                                $question['is_valid'] = $questionInfoRow->is_valid;
                                $commentRowSet = $questionInfoRow->findComment();
                                $question['comment'] = array();
                                foreach ($commentRowSet as $comment)
                                        $question['comment'][] = $comment->toArray();
                        }
                }
                return $question;
        }
        
        //Find the answer information for this $question_id and $language_id
        //The return value is an array of answers (with each answers being an array)
        //For short answers and true/false the array contains a single answer.
        //For multiple choice questions the array contains at least two answers.
        private function findQuestionAnswers($question_id, $language_id) {
                $questionTable = new Question();
                $questionRow = $questionTable->fetchRow("question_id=$question_id");
                if ($questionRow == null) return array();

                $answers=array();
                $answerTable = new Answer();
                $answerInfoTable = new AnswerInfo();
                switch($questionRow->answer_type_id) {
                  case 1: //Multiple choices
                    $answerRowSet = $answerTable->fetchAll("question_id=$question_id");
                    foreach($answerRowSet as $answerRow) {
                            $answerInfoRow = $answerRow->findAnswerInfo($answerTable->select()->where("language_id=$language_id"))->current();
                            $answers[] = array('answer_id'=>$answerRow->answer_id,
                                               'label'=>$answerRow->label,
                                               'is_right'=>$answerRow->is_right,
                                               'answer_latex'=>$answerInfoRow->answer_latex,
                                               'answer_flash_file'=>$answerInfoRow->answer_flash_file);
                    }
                    break;
                  case 2: //True/false
                    $answerRow = $answerTable->fetchRow("question_id=$question_id");
                    if ($answerRow != null)
                            $answers[] = array('answer_id'=>$answerRow->answer_id,
                                               'label'=>"",
                                               'is_right'=>$answerRow->is_right,
                                               'answer_latex'=>$this->generateTFAnswer($language_id, $answerRow->is_right),
                                               'answer_flash_file'=>"");
                    break;
                  case 3: //Short answer
                    $query = $answerInfoTable->select()
                      ->setIntegrityCheck(false)
                      ->from($answerInfoTable)
                      ->join('answer', "answer.answer_id=answer_info.answer_id && answer.question_id=$question_id")
                      ->where("language_id=$language_id");
                    $answerInfoRow = $answerInfoTable->fetchRow($query);
                    if ($answerInfoRow != null) {
                            $answers[] = array('answer_id'=>$answerInfoRow->answer_id,
                                               'label'=>"",
                                               'is_right'=>1,
                                               'answer_latex'=>$answerInfoRow->answer_latex,
                                               'answer_flash_file'=>$answerInfoRow->answer_flash_file);
                    }
                    break;
                }
                return $answers;
        }

        //Returns an array with the level information for this $question_id
        //The entries in the array look like: 'l'=>'v' where 'l' is a level_id and 'v' is the difficulty value.
        private function findQuestionLevels($question_id) {
                $questionTable = new Question();
                $questionRow = $questionTable->fetchRow("question_id=$question_id");
                $levels = array();
                if ($questionRow != null) {
                        $questionLevelRowSet = $questionRow->findQuestionLevel();
                        foreach ($questionLevelRowSet as $qlRow)
                                $levels[$qlRow->level_id] = $qlRow->value;
                }
                return $levels;
        }

        //Generate a true/false answer for the specified language and answer.
        private function generateTFAnswer($language_id, $is_right) {
                switch ($language_id) {
                  case 1: //french
                    if ($is_right) return "La réponse est 'Vrai'";
                    return "La réponse est 'Faux'";
                  case 2: //english
                    if ($is_right) return "The answer is 'True'";
                    return "The answer is 'False'";
                }
        }

        //This should be implemented with a lookup table to speed it up a bit...
        private function lookupFilteredQuestionIndex($question_id, $language_id) {
                $session = new Zend_Session_Namespace('Question');
                foreach($session->filteredQuestions as $index=>$question) {
                        if ($question['question_id'] == $question_id && $question['language_id'] == $language_id)
                                return $index;
                }
                return 0;
        }

}