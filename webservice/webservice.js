'use strict';

const cors = require('cors');
const express = require('express');
const fs = require('fs');
const bodyParser = require('body-parser');
const http = require('http');


const OK = 200;
const CREATED = 201;
const BAD_REQ = 400;
const NOT_FOUND = 404;
const CONFLICT = 409;
const SERVER_ERR = 500;

const PHOTO_URL = "https://iot-corbi.s3.amazonaws.com/detections/"

function serve(port) {
    const app = express();
    app.locals.port = port;

    setupRoutes(app);
    
    const httpServer = http.createServer(app);
    
    httpServer.listen(port, function() {
        console.log("Starting server on port: " + port);
    });
    return httpServer;
}

module.exports = { serve };

function setupRoutes(app) {
    app.use(cors());
    app.use(bodyParser.json());

    app.get('/motion_list/', function(req, res) {
        try {
            let file_data = fs.readFileSync('motion_list.txt', 'utf8');
            res.send(file_data);
        } catch (err) {
            res.send("motion list file does not exist.");
        }
    });
    app.get('/photos/', function(req, res) {
        // send the photo links...
        let photos = fs.readdirSync('/home/ec2-user/s3_mountpoint/detections/');
        let photo_urls = [];
        for (let i = 0; i < photos.length; i++) {
            photo_urls.push(PHOTO_URL + photos[i]);
        }

        res.send({"photos": photo_urls});
    });
    app.delete('/motion_list/', function(req, res) {
        let file_data = "";
        try {
            fs.writeFileSync('motion_list.txt', file_data);
            res.sendStatus(OK);
        } catch (err) {
            res.sendStatus(SERVER_ERR);
        }
    });
    app.delete('/photos/', function(req,res) {
        try {
            // clear the photos directory.
            

            res.sendStatus(OK);
        } catch (err) {
            res.sendStatus(SERVER_ERR);
        }
    });
}
