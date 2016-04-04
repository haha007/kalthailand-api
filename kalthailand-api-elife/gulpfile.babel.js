import gulp from 'gulp';
import {create} from 'browser-sync';

const browserSync = create();

gulp.task('reload', () => {
  browserSync.reload();
});

gulp.task('serve', () => {
  browserSync.init({
    server: {
      baseDir: 'src/main/resources/static'
    }
  });
});

gulp.task('watch', () => {
  gulp.watch([
    'src/main/resources/static/**/*.htm',
    'src/main/resources/static/**/*.html',
    'src/main/resources/static/**/*.js',
    'src/main/resources/static/**/*.css'
  ], ['reload'])
});

gulp.task('default', ['serve', 'watch']);
