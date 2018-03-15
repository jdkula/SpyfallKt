if(defined.PRODUCTION) {
    config.mode = "production";
    config.plugins.push(new webpack.DefinePlugin({
        "process.env": {
            NODE_ENV: JSON.stringify("production")
        }
    }));

    config.devtool = 'source-map';

    config.optimization = {
        splitChunks: {
            name: 'common'
        },
        minimize: true
    }
} else {
    config.mode = "development";
    config.plugins.push(new webpack.DefinePlugin({
        "process.env": {
            NODE_ENV: JSON.stringify("development")
        }
    }));
    config.devtool = 'inline-source-map';
}