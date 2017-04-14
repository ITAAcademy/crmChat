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
var templateCache = require('gulp-angular-templatecache');
var inject = require('gulp-inject');
var merge = require('merge-stream');
var concat = require('gulp-concat');
var concatCss = require('gulp-concat-css');
var angularFilesort = require('gulp-angular-filesort');
var order = require("gulp-order");
//var ignorableJs = ["ngDialog.min.js"];//js which not to be minified

var uglifyOptions = {
    mangling: false
};


gulp.task('scripts-sub', function() {
    var res = gulp.src('source_resources/js/**/*.js', { since: gulp.lastRun('scripts') })
        .pipe(plumber())
        .pipe(babel({ presets: ['es2015'] }))
        .pipe(debug({ title: 'babel:' }));
    res.pipe(gulp.dest('temp/js'));
    return res;
});

gulp.task('scripts-bot', function() {
    var res = gulp.src('source_resources/js/bot/*.js', { since: gulp.lastRun('scripts') })
        .pipe(plumber())
        .pipe(babel({ presets: ['es2015'] }))
        .pipe(debug({ title: 'babel:' }));
    res.pipe(gulp.dest('resources/static/js/bot'));
    return res;
});

gulp.task('scripts-build', function() {
    var res = gulp.src('temp/js/*.js').pipe(angularFilesort())
        .pipe(plumber()).pipe(order([
            "globalFunc.js",
            "app.js",
            "services.js",
            "directives.js",
            "/**/*.js",
        ]))
    if (process.argv[2] == "release") {
        var ngAnnotate = require('gulp-ng-annotate');
        res = res.pipe(concat('app.js'))
            .pipe(ngAnnotate())
            .pipe(uglify(uglifyOptions))

    }
    res.pipe(gulp.dest('resources/static/js'));
    return res;
});

gulp.task('scripts', gulp.series('scripts-sub', 'scripts-build', 'scripts-bot'));

gulp.task('lib', function() {
    return gulp.src('source_resources/lib/**/*.*', { since: gulp.lastRun('lib') })
        .pipe(plumber())
        .pipe(debug({ title: 'lib:' })).pipe(gulp.dest('resources/static/lib'));
});
gulp.task('styles', function() {
    var res = gulp.src('source_resources/css/*.{css,sass}', { since: gulp.lastRun('styles') })
        .pipe(plumber())
        .pipe(debug({ title: 'styles:' })).pipe(autoprefixer());
    if (process.argv[2] == "release") {
        res = res.pipe(concatCss('style.css')).pipe(cssmin());
    }
    res.pipe(gulp.dest('resources/static/css'));
    return res;
});
gulp.task('assets', function() {
    return gulp.src('source_resources/{images,fonts,data}/**/*.*', { since: gulp.lastRun('assets') })
        .pipe(plumber())
        .pipe(debug({ title: 'assets:' })).pipe(gulp.dest('resources/static/'));
});

gulp.task('templates:static', function() {
    return gulp.src('source_resources/static_templates/*.*', { since: gulp.lastRun('templates:static') })
        .pipe(plumber())
        .pipe(debug({ title: 'templates:static:' })).pipe(gulp.dest('resources/static/static_templates'));
});

gulp.task('templates:static:angular', function() {
    var create = gulp.src('source_resources/static_templates/*.*')
        .pipe(templateCache({ root: "static_templates/", module: "chatTemplates" }))
        .pipe(gulp.dest('resources/static/js'));
    return create;
});

gulp.task('templates:static:angular:index', function() {
    var sources = gulp.src(['resources/static/js/templates.js'], { read: true });
    var injectIndex = gulp.src('resources/templates/index.html').pipe(inject(sources, {
        relative: true,
        starttag: '<!-- inject:template -->',
        endtag: '<!-- endinject -->',
        transform: function(filePath, file) {
            //            return '<script>\n' + file.contents.toString('utf8') + '\n</script>';
            return '<script src="js/' + file.basename + '"></script>';
        }
    })).pipe(gulp.dest('resources/templates'));
    return injectIndex;
});

gulp.task('inject-css', function() {
    var sources = gulp.src(['resources/static/css/*.css', '!resources/static/css/chat.css'], { read: true });
    var injectIndex = gulp.src('resources/templates/index.html').pipe(inject(sources, {
        relative: true,
        transform: function(filePath, file) {
            //            return '<script>\n' + file.contents.toString('utf8') + '\n</script>';
            return '<link href="css/' + file.basename + '" rel="stylesheet" />';
        }
    })).pipe(gulp.dest('resources/templates'));
    return injectIndex;
});

gulp.task('inject-js', function() {
    var sources = gulp.src(['resources/static/js/*.js'], { read: true }).pipe(angularFilesort());

    var injectIndex = gulp.src('resources/templates/index.html').pipe(inject(sources, {
        relative: true,
        transform: function(filePath, file) {
            //            return '<script>\n' + file.contents.toString('utf8') + '\n</script>';
            return '<script src="js/' + file.basename + '"></script>';
        }
    })).pipe(gulp.dest('resources/templates'));
    return injectIndex;
});



gulp.task('templates', function() {
    return gulp.src('source_resources/templates/*.*', { since: gulp.lastRun('templates') })
        .pipe(plumber())
        .pipe(debug({ title: 'templates:' })).pipe(gulp.dest('resources/templates'));
});

gulp.task('clean', function() {
    var clean = require('gulp-clean');
    return gulp.src(['resources/static/css', 'resources/static/js'], { read: false })
        .pipe(clean());
});

gulp.task('build', gulp.series('styles', 'assets', 'templates:static', 'templates', 'scripts', 'lib', 'inject-css', 'inject-js'));
gulp.task('watch', function() {
    watch('source_resources/css/*.{css,sass}', gulp.series('styles'));
    gulp.watch('source_resources/images/**/*.*', gulp.series('assets'));
    gulp.watch('source_resources/static_templates/*.*', gulp.series('templates:static'));
    gulp.watch('source_resources/templates/*.*', gulp.series('templates'));
    gulp.watch('source_resources/js/**/*.*', gulp.series('scripts'));
    gulp.watch('source_resources/lib/**/*.*', gulp.series('lib'));
    gutil.log('Start watching!');
});
gulp.task('dev', gulp.series('build', 'watch'));
gulp.task('release', gulp.series('clean', 'build', 'templates:static:angular', 'templates:static:angular:index'));
gulp.task('test', gulp.series('templates', 'templates:static:angular', 'templates:static:angular:index'));
