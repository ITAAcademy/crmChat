'use strict';

var path = require('path');
var gulp = require('gulp');
var conf = require('./conf');
var prefix = require('gulp-prefix');

var $ = require('gulp-load-plugins')({
    pattern: ['gulp-*', 'main-bower-files']
});

var _ = require('lodash');

gulp.task('dev-fonts', function() {
    return gulp.src($.mainBowerFiles())
        .pipe($.filter('**/*.{eot,svg,ttf,woff,woff2}'))
        .pipe($.flatten())
        .pipe(gulp.dest(path.join(conf.paths.devDist, 'fonts')));
});

gulp.task('dev-copy-lib', function() {
    var assets = require('wiredep')(_.extend({}, conf.wiredep));
    var srcList = [];
    srcList.push.apply(srcList, assets.js);
    srcList.push.apply(srcList, assets.css);
    return gulp
        .src(srcList /*, { base: '.' }*/ )
        /*      .pipe($.rename(function (p) {
                p.dirname = p.dirname.replace(/\\/g, '/').replace('bower_components/', '');
                if (p.dirname.indexOf('/') !== -1) {
                  p.dirname = p.dirname.substr(0, p.dirname.indexOf('/'));
                }
              }))*/
        .pipe(gulp.dest(path.join(conf.paths.devDist, 'lib')));
});

var partialsInjectFile = gulp.src(path.join(conf.paths.tmp, '/partials/templateCacheHtml.js'), { read: false });
var partialsInjectOptions = {
    starttag: '<!-- inject:partials -->',
    ignorePath: path.join(conf.paths.tmp, '/partials'),
    addRootSlash: false
};


gulp.task('partials', function() {
    return gulp.src([
            path.join(conf.paths.src, '/app/**/*.html'),
            path.join(conf.paths.tmp, '/serve/app/**/*.html')
        ])
        .pipe($.minifyHtml({
            empty: true,
            spare: true,
            quotes: true
        }))
        .pipe($.angularTemplatecache('templateCacheHtml.js', {
            module: 'BlurAdmin',
            root: 'app'
        }))
        .pipe(prefix("admin-panel/dev-release", null, '{{'))
        .pipe(gulp.dest(conf.paths.tmp + '/partials/'));
});


gulp.task('html-dev', ['inject', 'partials'], function() {
    console.log("START HTML");
    var partialsInjectFile = gulp.src(path.join(conf.paths.tmp, '/partials/templateCacheHtml.js'), { read: false });
    var partialsInjectOptions = {
        starttag: '<!-- inject:partials -->',
        ignorePath: path.join(conf.paths.tmp, '/partials'),
        addRootSlash: false
    };

    return gulp.src(path.join(conf.paths.tmp, '/serve/*.html'))
        .pipe($.inject(partialsInjectFile, partialsInjectOptions))
        .pipe(prefix("admin-panel/dev-release", null, '{{'))
        .pipe(gulp.dest(conf.paths.devDist));
});

gulp.task('html-copy', ['partials'], function() {
    return gulp.src(path.join(conf.paths.tmp, '/partials/templateCacheHtml.js'))
        .pipe($.inject(partialsInjectFile, partialsInjectOptions))
        .pipe(gulp.dest(conf.paths.devDist));
});


gulp.task('dev-css-replace', ['dev-copy-assets'], function() {
    return gulp.src(path.join(conf.paths.devDist, '*.html'))
        .pipe($.replace(/<link rel="stylesheet" href="\.\.\/bower_components\/.*\/(.*)"\s*?\/>/g, '<link rel="stylesheet" href="lib/$1" >'))
        .pipe(prefix("admin-panel/dev-release", null, '{{'))
        .pipe(gulp.dest(conf.paths.devDist));
});

gulp.task('dev-js-replace', ['dev-copy-assets'], function() {
    return gulp.src(path.join(conf.paths.devDist, '.html'))
        .pipe($.replace(/<script src="\.\.\/bower_components\/.*\/(.*)"\s*?>/g, '<script src="lib/$1">'))
        .pipe(prefix("admin-panel/dev-release", null, '{{'))
        .pipe(gulp.dest(conf.paths.devDist));
});

gulp.task('dev-copy-assets', ['inject', 'dev-copy-lib', 'dev-fonts'], function() {
    return gulp
        .src([
            conf.paths.src + '/**/*',
            path.join(conf.paths.tmp, '/serve/**/*')
        ])
        .pipe(gulp.dest(conf.paths.devDist));
});

gulp.task('dev-copy-assets-mini', [], function() {
    return gulp
        .src([conf.paths.src + '/app/**/*', path.join(conf.paths.tmp, '/serve/app/**/*')])
        //.pipe($.filter((file) => { file.base = '.'; return file.event === 'change'; }))
        .pipe(gulp.dest(conf.paths.devDist + /app/));
});



gulp.task('watch-dev', [], function() {

    gulp.watch([path.join(conf.paths.src, '/*.html'), 'bower.json'], ['inject-reload']);

    gulp.watch([
        path.join(conf.paths.src, '/sass/**/*.css'),
        path.join(conf.paths.src, '/sass/**/*.scss')
    ], function(event) {
        gulp.run('styles-reload','dev-copy-assets-mini');
    });

    gulp.watch(path.join(conf.paths.src, '/app/**/*.js'), function(event) {
        gulp.run('dev-copy-assets-mini');
    });

    gulp.watch(path.join(conf.paths.src, '/app/**/*.html'), function(event) {
        gulp.run('html-copy');
    });
});


gulp.task('dev-release', ['dev-css-replace', 'dev-js-replace'], function() {
    gulp.run('html-dev', 'html-copy', 'watch-dev');
});
