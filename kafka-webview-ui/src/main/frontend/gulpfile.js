'use strict'

var gulp = require('gulp');

require('require-dir')('./gulp-tasks');

gulp.paths = {
  dist: 'dist',
  vendors: 'dist/vendors'
};

gulp.task('default', ['build:dist']);
