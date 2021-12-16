'use strict'

var gulp = require('gulp');
var sass = require('gulp-sass');
var cssmin = require('gulp-cssmin')
var rename = require('gulp-rename');
var runSequence = require('run-sequence');

require('require-dir')('./gulp-tasks');

gulp.paths = {
  dist: 'dist',
  vendors: 'dist/vendors'
};

var paths = gulp.paths;


gulp.task('sass', function () {
  return gulp.src('./scss/style.scss')
  .pipe(sass())
  .pipe(gulp.dest('./css'))
  .pipe(browserSync.stream());
});

gulp.task('sass:watch', function () {
  gulp.watch('./scss/**/*.scss');
});

gulp.task('default', ['build']);
