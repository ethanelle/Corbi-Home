'use strict';

const {promisify} = require('util');
const readFile = promisify(require('fs').readFile);
const writeFile = promisify(require('fs').writeFile);
const Path = require('path');
const mqtt = require('mqtt');

const server = require('./webservice');

function usage() {
    console.error(`Here's a usage message... `);
    process.exit(1);
}

function getPort(portArg) {
    let port = Number(portArg);
    if(!port) {
        console.error(`Bad port argument: ${port}`)
        usage();
    }
    return port;
}

async function shutdown(event, resources) {
    if (Object.keys(resources).length > 0) {
        console.log(`Shutting down event: ${event}`);
        if (resources.server) {
            await resources.server.close();
            delete resources.server;
        }
        if (resources.timer) {
            clearInterval(resources, timer);
            delete resources.timer;
        }
    }
}

function cleanupResources(resources) {
    const events = [ 'SIGINT', 'SIGTERM', 'exit' ];
    for (const event of events) {
      process.on(event, async () => await shutdown(event, resources));
    }
  }

async function go(args) {
    const resources = {};

    try {
        const port = getPort(args);
        // run the webservice
        process.title = "corbiWebApp";
        process.on('SIGINT', function() {
            console.log("Shutting down server.");
            process.exit();
        });
        server.serve(port);
        
    } catch (err) {
        console.error(err);
    }

    finally {
        cleanupResources(resources);
    }
}

go(process.argv.slice(2));