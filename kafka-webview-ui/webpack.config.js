const webpack = require('webpack');

const path = require('path');
const ExtractTextPlugin = require('extract-text-webpack-plugin');

const outputDir = path.join(__dirname, 'src/main/resources/build/');

module.exports = {
  // Entry points are where we start resolving and mapping modules
  entry: {
    app: [
      // Hot server for reloading modules, dev only
      'webpack/hot/only-dev-server',
      // This is specifically for hot loading react components in place, dev only
      'react-hot-loader/patch',
      // This is our actual entry point into our application
      path.resolve('src/main/resources/js/index.js'),
    ],
  },
  // When we build for prod, this is where our files will load
  output: {
    // This is important in dev
    publicPath: 'http://localhost:9000/build/',
    path: outputDir,
    filename: '[name].js',
  },
  module: {
    // Rules tell our various file types how to load and be processed
    rules: [
      // Run all of our javascript and jsx (React stuff) through babel
      {
        test: /\.js$/,
        exclude: /(node_modules|bower_components)/,
        loader: 'babel-loader',
      },
      // Run all of our CSS and SASS files through the sass parser
      {
        test: /\.scss$/,
        use: [
          {
            loader: 'style-loader', // creates style nodes from JS strings
          },
          {
            loader: 'css-loader', // translates CSS into CommonJS
          },
          {
            loader: 'sass-loader', // compiles Sass to CSS
          },
        ],
        // In production we will want to extract our css into a single file,
        // for dev purposes this is commented out
        //test: /\.scss$/,
        //use: ExtractTextPlugin.extract({
        //  fallback: 'style-loader',
        //  use: ['css-loader', 'sass-loader'],
        //}),
      },
      // Handle images...
      {
        test: /\.(png|jpg|gif)$/,
        loader: 'url-loader?limit=8192',
      }, // inline base64 URLs for <=8k images, direct URLs for the rest
      {
        test: /\.(svg|woff|woff2|eot|ttf)(\?v=\d+\.\d+\.\d+)?$/,
        loader: 'file-loader?name=[name].[ext]',
      },
    ],
  },
  plugins: [
    // We need this in prod, not dev, it takes any CSS and munges it into this file in our build folder
    //new ExtractTextPlugin('app.css'),
    // Don't swap in files that we can't compile
    new webpack.NoEmitOnErrorsPlugin(),
    // Shows the relative path of a module when HMR is enabled, it should only be used in dev.
    new webpack.NamedModulesPlugin(),
    // This is for hot module replacement, it should only be loaded in dev.
    new webpack.HotModuleReplacementPlugin(),
    // TODO: Add a minifier for prod, probably babili or whatever it's called now
  ],
  // The dev server handles reloading changes for us while we're working on them
  devServer: {
    hot: true,
    historyApiFallback: true,
    publicPath: '/build/',
    compress: true,
    port: 9000,
    // Prevents us from getting a CORS error when dev'ing
    headers: { 'Access-Control-Allow-Origin': '*' },
  },
  // Our javascript will get translated, this allows us to understand what it's
  // doing from the browser.
  devtool: 'inline-source-map',
};
