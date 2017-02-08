'use strict';
const gulp = require('gulp');
/*
var gulpLoadPlugins = require('gulp-load-plugins');
var plugins = gulpLoadPlugins();
*/
var gutil = require('gulp-util');
const cssmin = require('gulp-cssmin');
const uncss = require('gulp-uncss');
const uglify = require('gulp-uglify');
const babel = require('gulp-babel');
const autoprefixer = require('gulp-autoprefixer');
const debug = require('gulp-debug');
const path = require('path');
const watch = require('gulp-watch');
var plumber = require('gulp-plumber');

var gulpIf = require('gulp-if');
//var ignorableJs = ["ngDialog.min.js"];//js which not to be minified

var uglifyOptions =  {
    mangling: false
};

gulp.task('scripts',function(){
    return gulp.src('source_resources/js/**/*.js',{since:gulp.lastRun('scripts')})
        .pipe(plumber())
        .pipe(babel({presets:['es2015']}))
        .pipe(debug({title: 'babel:'}))
      /*  .pipe(uglify(uglifyOptions))*/
        .pipe(gulp.dest('resources/static/js'));
});
gulp.task('lib',function(){
    return gulp.src('source_resources/lib/**/*.*',{since:gulp.lastRun('lib')})
        .pipe(plumber())
        .pipe(debug({title: 'lib:'})).pipe(gulp.dest('resources/static/lib'));
});
gulp.task('styles',function(){
    return gulp.src('source_resources/css/*.{css,sass}',{since:gulp.lastRun('styles')})
        .pipe(plumber())
        .pipe(debug({title: 'styles:'})).pipe(autoprefixer())/*.pipe(cssmin())*/.pipe(gulp.dest('resources/static/css'));
});
gulp.task('assets',function(){
    return gulp.src('source_resources/{images,fonts,data}/**/*.*',{since:gulp.lastRun('assets')})
        .pipe(plumber())
        .pipe(debug({title: 'assets:'})).pipe(gulp.dest('resources/static/'));
});
gulp.task('templates:static',function(){
    return gulp.src('source_resources/static_templates/*.*',{since:gulp.lastRun('templates:static')})
        .pipe(plumber())
        .pipe(debug({title: 'templates:static:'})).pipe(gulp.dest('resources/static/static_templates'));
});
gulp.task('templates',function(){
    return gulp.src('source_resources/templates/*.*',{since:gulp.lastRun('templates')})
        .pipe(plumber())
        .pipe(debug({title: 'templates:'})).pipe(gulp.dest('resources/templates'));
});

gulp.task('build',gulp.series('styles','assets','templates:static','templates','scripts','lib'));
gutil.log('Start watching!');
gulp.task('watch',function(){
    watch('source_resources/css/*.{css,sass}',gulp.series('styles'));
    gulp.watch('source_resources/images/**/*.*',gulp.series('assets'));
    gulp.watch('source_resources/static_templates/*.*',gulp.series('templates:static'));
    gulp.watch('source_resources/templates/*.*',gulp.series('templates'));
    gulp.watch('source_resources/js/**/*.*',gulp.series('scripts'));
    gulp.watch('source_resources/lib/*.*',gulp.series('lib'));
});
gulp.task('dev',gulp.series('build','watch'));
