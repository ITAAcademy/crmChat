<?php
/* @var $this Controller */
$header = new Header();
?>
<!DOCTYPE html>
<html id="ng-app" ng-app="mainApp" xmlns:ng="https://angularjs.org">
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!--[if lte IE 8]>
    <body class="ie8">
    <![endif]-->
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <meta name="language" content="en">
    <meta property="og:type" content="website">
    <meta name="twitter:card" content="summary_large_image">
    <meta name="twitter:site" content="<?php echo Config::getBaseUrl(); ?>">
    <meta name="twitter:image"
          content="<?php echo StaticFilesHelper::createPath('image', 'mainpage', 'intitaLogo.jpg'); ?>">
    <meta property="og:image"
          content="<?php echo StaticFilesHelper::createPath('image', 'mainpage', 'intitaLogo.jpg'); ?>">
    <!-- for tabs -->
    <meta http-equiv="X-UA-Compatible" content="IE=edge">

    <!-- fonts -->
    <link rel="stylesheet" href="<?php echo StaticFilesHelper::fullPathTo('css', 'fontface.css'); ?>"/>
    <!-- fonts -->
    <!-- layouts style -->
    <link rel="stylesheet" type="text/css" href="<?php echo StaticFilesHelper::fullPathTo('css', 'style.css') ?>"/>
    <link rel="stylesheet" href="<?php echo StaticFilesHelper::fullPathTo('css', 'regform.css');; ?>"/>
    <!--   hamburger menu style -->
    <link rel="stylesheet" type="text/css"
          href="<?php echo StaticFilesHelper::fullPathTo('css', 'hamburgerMenu.css'); ?>"/>
    <link rel="shortcut icon" href="<?php echo StaticFilesHelper::fullPathTo('css', 'images/favicon.ico'); ?>"
          type="image/x-icon"/>
    <script type="text/javascript" src="<?php echo StaticFilesHelper::fullPathTo('js', 'jquery-1.8.3.js'); ?>"></script>
    <script type="text/javascript" src="<?php echo StaticFilesHelper::fullPathTo('js', 'openDialog.js'); ?>"></script>
    <!--[if lte IE 8]>
    <script src="<?php echo StaticFilesHelper::fullPathTo('angular', 'js/json3.min.js'); ?>"></script>
    <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.2.29/angular.min.js"></script>
    <script>
        document.createElement('ng-include');
        document.createElement('ng-switch');
        document.createElement('ng-if');
        document.createElement('ng-pluralize');
        document.createElement('ng-view');

        // needed to enable CSS reference
        document.createElement('ng:view');
    </script>
    <script type="text/javascript" src="<?php echo StaticFilesHelper::fullPathTo('js', 'labelForIe.js'); ?>"></script>
    <![endif]-->
    <!-- for tabs -->
    <script src="<?php echo StaticFilesHelper::fullPathTo('angular', 'js/angular.min.js'); ?>"></script>
    <script src="<?php echo StaticFilesHelper::fullPathTo('angular', 'js/paymentsSchemes.js'); ?>"></script>
    <script src="<?php echo StaticFilesHelper::fullPathTo('angular', 'bower_components/angular-resource/angular-resource.min.js'); ?>"></script>
    <script src="<?php echo StaticFilesHelper::fullPathTo('angular', 'bower_components/angular-animate/angular-animate.js'); ?>"></script>
    <script src="<?php echo StaticFilesHelper::fullPathTo('angular', 'js/app.js'); ?>"></script>
    <script src="<?php echo StaticFilesHelper::fullPathTo('angular', 'bower_components/angular-bootstrap/ui-bootstrap-tpls-1.3.3.js'); ?>"></script>
    <title><?php echo CHtml::encode($this->pageTitle); ?></title>
</head>

<body itemscope itemtype="https://schema.org/Product">

<div id="main-wrapper" >
    <div id="mainheader">
        <?php $this->renderPartial('/site/_hamburgermenu'); ?>
        <div id='headerUnderline'>
            <table id="navigation" class="down">
                <tr class="main">
                    <td id="logo_img" class="down">
                        <a href="<?php echo Yii::app()->createUrl('site/index'); ?>">
                            <img id="logo"
                                 src="<?php echo StaticFilesHelper::createPath('image', 'mainpage', 'Logo_small.png'); ?>"/>
                        </a>
                    </td>
                    <td id="menulist">
                        <ul>
                            <li>
                                <a href="<?php echo Config::getBaseUrl() . '/courses'; ?>"><?php echo Yii::t('header', '0016'); ?></a>
                            </li>
                            <li>
                                <a href="<?php echo Config::getBaseUrl() . '/teachers'; ?>"><?php echo Yii::t('header', '0021'); ?></a>
                            </li>
                            <li>
                                <a href="<?php echo Config::getBaseUrl() . '/graduate'; ?>"><?php echo Yii::t('header', '0137'); ?></a>
                            </li>
                            <li><a href="<?php echo Config::getBaseUrl() . '/crmForum'; ?>"
                                   target="_blank"><?php echo Yii::t('header', '0017'); ?></a></li>
                            <li>
                                <a href="<?php echo Config::getBaseUrl() . '/aboutus'; ?>"><?php echo Yii::t('header', '0018'); ?></a>
                            </li>
                            <?php if (!Yii::app()->user->isGuest) { ?>
                            <li>
                                <a href="<?php echo Yii::app()->createUrl('/_teacher/cabinet/index'); ?>"><?php echo Yii::t('profile', '0815'); ?></a>
                            </li>
                            <?php } ?>
                            <li>
                                <a href="http://www.robotamolodi.org/" target="_blank"><?php echo Yii::t('header', '0902'); ?></a>
                            </li>
                            <li>
                                <a href="http://profitday.info/upcomingevents" target="_blank"><?php echo Yii::t('header', '0912'); ?></a>
                            </li>
                        </ul>
                    </td>
                    <td class="emptyTd"></td>
                    <td id="enterButton">
                        <div id="button_border" class="down">
                        </div>
                        <?php if (Yii::app()->user->isGuest) {
                            echo CHtml::link($header->getEnterButton(), '', array('id' => 'enter_button', 'class' => 'down', 'onclick' => 'openSignIn();',));
                        } else {
                            ?>
                            <a id="enter_button" href="<?php echo Config::getBaseUrl(); ?>/site/logout"
                               class="down"><?php echo $header->getLogoutButton(); ?></a>
                        <?php } ?>
                    </td>
                    <td id="lang" class="down">
                        <div class="languageRow">
                            <?php
                            if (Yii::app()->session['lg'] == NULL) Yii::app()->session['lg'] = 'ua';
                            foreach (array("ua", "en", "ru") as $val) {
                                ?>
                                <a href="<?php echo Yii::app()->createUrl('site/changeLang', array('lg' => $val)); ?>" <?php echo (Yii::app()->session['lg'] == $val) ? 'class="selectedLang"' : ''; ?>><?php echo $val; ?></a>
                                <?php
                            }
                            ?>
                        </div>
                    </td>
                </tr>
            </table>
        </div>
    </div>
    <div class="main">
        <div style="height: 5px; width: auto"></div>
        <div class="breadcrumbs">
            <?php if (isset($this->breadcrumbs)): ?>
                <?php $this->widget('zii.widgets.CBreadcrumbs', array(
                    'links' => $this->breadcrumbs,
                    'homeLink' => CHtml::link(Yii::t('breadcrumbs', '0049'), Config::getBaseUrl()),
                    'htmlOptions' => array(
                        'class' => 'my-cool-breadcrumbs'
                    )
                )); ?><!-- breadcrumbs -->
            <?php endif ?>
            <?php if (!Yii::app()->user->isGuest && !(Yii::app()->controller->id == 'site' && Yii::app()->controller->action->id == 'index')
                && !(Yii::app()->controller->id == 'aboutus') && !(Yii::app()->controller->id == 'lesson')
            ) {
                $post = Yii::app()->user->model;
                $statusInfo = $this->beginWidget('UserStatusWidget', ['bigView' => true ,'registeredUser'=>$post]);
                $this->endWidget();
            }
            ?>
        </div>
    </div>
    
    <div id="contentBoxMain">
        <?php echo $content; ?>
        <!--Form Auth-->
        <?php echo $this->decodeWidgets('{{w:AuthorizationFormWidget|dialog=true;id=authFormDialog}}'); ?>
        <!--Form Auth-->
        <!--forgot pass modal-->
        <?php
        $this->beginWidget('zii.widgets.jui.CJuiDialog', array(
            'id' => 'forgotpass',
            'themeUrl' => Config::getBaseUrl() . '/css',
            'cssFile' => 'jquery-ui.css',
            'theme' => 'my',
            'options' => array(
                'width' => 540,
                'autoOpen' => false,
                'modal' => true,
                'resizable' => false
            ),
        ));
        $this->renderPartial('/site/_forgotpass');
        $this->endWidget('zii.widgets.jui.CJuiDialog');
        ?>
        
    </div>
</div>

<div id="mainfooter">
    <div class="footercontent">
        <div class="leftfooter">
            <table>
                <tr>
                    <td>
                        <a href="https://twitter.com/INTITA_EDU" target="_blank" title="Twitter">
                            <img
                                src="<?php echo StaticFilesHelper::createPath('image', 'mainpage', 'twitter.png'); ?>"/>
                        </a>
                    </td>
                    <td>
                        <a href="https://youtube.com" target="_blank" title="Youtube">
                            <img src="<?php echo StaticFilesHelper::createPath('image', 'mainpage', 'youtube.png'); ?>"/>
                        </a>
                    </td>
                    <td>
                        <a href="https://plus.google.com/u/0/116490432477798418410/posts" target="_blank"
                           title="Google+">
                            <img
                                src="<?php echo StaticFilesHelper::createPath('image', 'mainpage', 'googlePlus.png'); ?>"/>
                        </a>
                    </td>
                </tr>
                <tr>
                    <td>
                        <a href="https://www.facebook.com/pages/INTITA/320360351410183" target="_blank"
                           title="Facebook">
                            <img
                                src="<?php echo StaticFilesHelper::createPath('image', 'mainpage', 'facebook.png'); ?>"/>
                        </a>
                    </td>
                    <td>
                        <a href="https://www.linkedin.com/company/intita?trk=biz-companies-cym" target="_blank"
                           title="Linkedin">
                            <img
                                src="<?php echo StaticFilesHelper::createPath('image', 'mainpage', 'inl.png'); ?>"/>
                        </a>
                    </td>
                    <td>
                        <a href="https://vk.com/intita" target="_blank" title="Vkontakte">
                            <img
                                src="<?php echo StaticFilesHelper::createPath('image', 'mainpage', 'vkontakte.png'); ?>"/>
                        </a>
                    </td>
                </tr>
            </table>
        </div>
        <div class="centerfooter">
            <div class="leftpart">
                <div class="footerlogo">
                    <a href="<?php echo Yii::app()->createUrl('site/index'); ?>">
                        <img id="footerLogo" src="<?php echo StaticFilesHelper::createPath('image', 'mainpage', 'Logo_small.png'); ?>">
                        <img id="footerLogo800" src="<?php echo StaticFilesHelper::createPath('image', 'mainpage', 'Logo_small800.png'); ?>">
                    </a>
                </div>
                <div class="footercontact">
                    <p>
                        <span><?php $footer = new Footer(); echo $footer->getTel(); ?></span><br/>
                        <span><?php echo $footer->getMobile(); ?></span><br/>
                        <span><?php echo $footer->getEmail(); ?></span><br/>
                        <span><?php echo $footer->getSkype(); ?></span><br/>
                    </p>
                </div>
            </div>

            <div class="footermenu">
                <ul>
                    <li>
                        <a href="<?php echo Config::getBaseUrl() . '/courses'; ?>"><?php echo Yii::t('header', '0016'); ?></a>
                    </li>
                    <li>
                        <a href="<?php echo Config::getBaseUrl() . '/teachers'; ?>"><?php echo Yii::t('header', '0021'); ?></a>
                    </li>
                    <li>
                        <a href="<?php echo Config::getBaseUrl() . '/graduate'; ?>"><?php echo Yii::t('header', '0137'); ?></a>
                    </li>
                    <li><a href="<?php echo Config::getBaseUrl() . '/crmForum'; ?>"
                           target="_blank"><?php echo Yii::t('header', '0017'); ?></a></li>
                    <li>
                        <a href="<?php echo Config::getBaseUrl() . '/aboutus'; ?>"><?php echo Yii::t('header', '0018'); ?></a>
                    </li>
                    <?php if (!Yii::app()->user->isGuest) { ?>
                        <li>
                            <a href="<?php echo Yii::app()->createUrl('/_teacher/cabinet/index'); ?>"><?php echo Yii::t('profile', '0815'); ?></a>
                        </li>
                    <?php } ?>
                    <li>
                        <a href="http://www.robotamolodi.org/" target="_blank"><?php echo Yii::t('header', '0902'); ?></a>
                    </li>
                    <li>
                        <a href="http://profitday.info/upcomingevents" target="_blank"><?php echo Yii::t('header', '0912'); ?></a>
                    </li>
                </ul>
            </div>
        </div>
        <div class="rightfooter">
            <a onclick='goUp()'><img
                    src="<?php echo StaticFilesHelper::createPath('image', 'mainpage', 'go_up.png'); ?>"></a>
        </div>
    </div>
</div>
<!-- footer -->
<script src="<?php echo StaticFilesHelper::fullPathTo('angular', 'js/select.min.js'); ?>"></script>
<script src="<?php echo StaticFilesHelper::fullPathTo('angular', 'js/select-tpls.min.js'); ?>"></script>
<script src="<?php echo StaticFilesHelper::fullPathTo('angular', 'js/main_app/controllers.js'); ?>"></script>
<!-- Humburger script -->
<script type="text/javascript" src="<?php echo StaticFilesHelper::fullPathTo('js', 'hamburgermenu.js'); ?>"></script>
<script type="text/javascript" src="<?php echo StaticFilesHelper::fullPathTo('js', 'goToTop.js'); ?>"></script>
<!-- trimEmail-->
<script async src="<?php echo StaticFilesHelper::fullPathTo('js', 'trimField.js'); ?>"></script>
<!-- trimEmail -->
<div id="rocket">
    <img src="<?php echo StaticFilesHelper::createPath('image', 'mainpage', 'rocket.png'); ?>"/>
</div>
<div id="exhaust">
    <img src="<?php echo StaticFilesHelper::createPath('image', 'mainpage', 'exhaust.png'); ?>"/>
</div>

<!-- jQuery -->
<!-- passEye, jQuery -->
<script async src="<?php echo StaticFilesHelper::fullPathTo('js', 'jquery.passEye.js'); ?>"></script>
<!-- passEye, jQuery -->
<!-- Placeholder for old browser -->
<script src="<?php echo StaticFilesHelper::fullPathTo('js', 'placeholder.min.js'); ?>"></script>
<!-- Placeholder for old browser -->


<script type="text/javascript" src = "https://localhost:8080/crmChat/lib/angular-dnd.js"></script>



    <script type="text/javascript">
    var app = angular.module('mainApp', ['dnd']);

    app.controller('chat-controller', function($scope){

        this.dragstart = function(){
            console.log('dragstart', arguments);
             var res_elem = $('.draggable');
             res_elem.toggleClass("disable-animation");
        };

        this.drag = function(){
            console.log('drag', arguments);
        };

        this.dragend = function(){
            console.log('dragend', arguments);
            if(!arguments[0]) this.dropped = false;
                         var res_elem = $('.draggable');
             res_elem.toggleClass("disable-animation");
        };

        this.dragenter = function(dropmodel){
            console.log('dragenter', arguments);
            this.active = dropmodel;
        };

        this.dragover = function(){
            console.log('dragover', arguments);
        };

        this.dragleave = function(){
            console.log('dragleave', arguments);
            this.active = undefined;
        };

        this.drop = function(dragmodel, model){
            console.log('drop', arguments);
            this.dropped = model;
        };

        this.isDropped = function(model){
            return this.dropped === model;
        };

        this.isActive = function(model){
            return this.active === model;
        };
        $scope.minimizete = function(){
            if($scope.state == 1)
            {
                $scope.state = 0;
                return;
            }
            $scope.state = 1;
        }
                $scope.fullScreen = function(){
            if($scope.state == 2)
            {
                $scope.state = 0;
                return;
            }
            $scope.state = 2;
        }

        function resizeFunc(e){
var elem = $(window);
    var res_elem = $('.draggable');
    res_elem.css({ top: (elem.height() - 600) + 'px' });
    res_elem.css({ left: (elem.width() - 450) + 'px' });

        }
         $( window ).resize(resizeFunc);
         $( document ).ready(resizeFunc);
          

                $scope.state = 1;

        if(localStorage.getItem("chatState") == undefined)
            $scope.state = 0;
        else
        {
            $scope.state = parseInt(localStorage.getItem("chatState"));
            if($scope.state != 1)
            {
                $(".chat").removeClass("mini");
            }
        }

        $scope.$watch('state', function(){
            localStorage.setItem("chatState", $scope.state);
        })
        $scope.init = true;


    });

   

</script>
<!-- http://www.freeformatter.com/javascript-escape.html#ad-output $('body').append();-->
<link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet"/>
 <style type="text/css">
 .dnd-container{
    position: fixed; z-index: 99999999;top: 0px;left: 0px;width: 100%;height: 100%;transform: none;pointer-events: none;
 }
 .dnd-container .chat{
    width: 100%;
    height: 100%;
    max-height: 600px;
    max-width: 450px;
    top: calc(100% - 600px);
    left: calc(100% - 450px);
    bottom: 0;
    right: 0;
    position: absolute;transform: none;pointer-events: all;
    -webkit-transition: all .5s ease-in-out;
    -moz-transition: all .5s ease-in-out;
    -o-transition: all .5s ease-in-out;
    transition: all .5s ease-in-out;
    margin: 0;
 }
 .dnd-container .chat.mini{
    max-height: 65px;
    max-width: 450px;
    top: calc(100% - 65px) !important;
    left: calc(100% - 450px) !important;
 }
  .dnd-container .chat.full{
    max-height: 100%;
    max-width: 950px;
    right: 0px;
    bottom: 0px;
    top: 0 !important;
    left: 0 !important;
     margin: auto;
 }
 .dnd-container .chat.disable-animation{
     -webkit-transition: initial;
    -moz-transition: initial;
    -o-transition: initial;
    transition: initial;
 }
 .dnd-container .chat .logo{
        background-color: white;
    background-image: url(https://qa.intita.com/images/mainpage/Logo_small.png);
    background-repeat: no-repeat;
    background-size: contain;
    width: 110px;
    height: 30px;
    position: absolute;
    top: 18px;
    left: 25px;
    max-width: 0;
 }
 .dnd-container .chat.mini .logo{
    max-width: 100%;
 }
 .window_panel > *{
    display: inline-block;
    width: 20px; height: 35px;
    color: #425569;
    line-height: 35px;
 }
 .window_panel{
    position:absolute;top: 15px;right: 10px;width: 90px;height: 35px;background: none;color:#fff;
 }
 .handle{
    position:absolute;top: 15px;left: 60px;width: calc(100% - 250px);height: 35px;background: none;color:#fff;cursor: move;
 }
 .dnd-container .chat.mini .handle,
 .dnd-container .chat.full .handle{
    cursor: initial;
 }
 .material-icons{
    -webkit-transition: color .25s ease-in-out;
    -moz-transition: color .25s ease-in-out;
    -o-transition: color .25s ease-in-out;
    transition: color .25s ease-in-out;
 }
 .material-icons:hover{
    cursor: pointer;
    color: #4b75a4;
 }
 @media screen and (min-width:900px) {
    .window_panel{
  top: 15px;right: 10px;
    }
}
  </style>

<div ng-controller="chat-controller as main" class="dnd-container">
            <div ng-show="init" class="draggable chat mini ng-class:{mini: state==1, full: state==2}" ng-click="click(!$dragged &amp;&amp; !$resized &amp;&amp; !$rotated, $dropmodel)" dnd-draggable="state == 0" dnd-draggable-opts="{layer: 'layer1', handle: '.handle'}" dnd-on-dragstart="main.dragstart()" dnd-on-drag="main.drag($dropmodel)" dnd-on-dragend="main.dragend($dropmodel)" dnd-containment="'.dnd-container'" dnd-rect="main.rect3" dnd-model="main.dragmodel">
            <div class="logo"></div>
            <iframe style="width: 100%;height: 100%;border: none;" src="https://localhost:8080/crmChat"></iframe>
            <div class="window_panel" style="">
                <div id="minimize_btn" class="material-icons" ng-click="minimizete()">indeterminate_check_box</div>
                <div id="fullscreen_btn" class="material-icons" ng-click="fullScreen()">web_asset</div>
                <div id="close_btn" class="material-icons" ng-click="state = -1">close</div>
            </div>
            <div class="handle" dnd-draggable-handle="" style=""></div>
            

            </div>
</div>
</body>
</html>
